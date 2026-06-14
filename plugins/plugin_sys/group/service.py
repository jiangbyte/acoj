"""Group service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy import select, update as sa_update
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.utils.resolve_utils import resolve_name_path
from sdk.utils.tree_utils import build_tree, collect_descendant_ids
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from ..org.models import SysOrg
from ..user.models import SysUser
from .models import SysGroup
from .params import GroupPageParam, GroupTreeParam, GroupTreeVO, GroupVO, UnionTreeNode
from .repository import GroupRepository


def _normalize_parent_id(parent_id: Optional[str]) -> Optional[str]:
    return parent_id if parent_id not in (None, "", "0") else None


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


def _tree_node(entity: SysGroup) -> GroupTreeVO:
    return GroupTreeVO.model_validate(entity)


async def _enrich_vo(db: AsyncSession, vo: GroupVO) -> None:
    vo.org_names = await resolve_name_path(vo.org_id, db, SysOrg)


async def _batch_enrich(db: AsyncSession, vo_list: list[GroupVO]) -> None:
    for vo in vo_list:
        await _enrich_vo(db, vo)


async def _check_circular_parent(db: AsyncSession, entity_id: str, new_parent_id: Optional[str]) -> None:
    if not new_parent_id:
        return
    all_rows = (await db.execute(select(SysGroup))).scalars().all()
    parent_map = {row.id: row.parent_id for row in all_rows}
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点")
        current = parent_map.get(current)
        if not current or current == "0":
            break


async def _collect_descendant_ids(db: AsyncSession, ids: list[str]) -> list[str]:
    all_rows = (await db.execute(select(SysGroup))).scalars().all()
    return collect_descendant_ids(
        all_rows,
        ids,
        get_id=lambda row: row.id,
        get_parent_id=lambda row: row.parent_id or "",
    )


class GroupService:
    def __init__(self, repository: GroupRepository):
        self.repository = repository
        self.db = repository.db

    async def page(self, param: GroupPageParam) -> dict:
        page = map_page_data(
            await self.repository.find_page_by_filters(param),
            GroupVO.model_validate,
            param.current,
            param.size,
        )
        await _batch_enrich(self.db, page["records"])
        return page

    async def detail(self, id: str) -> Optional[GroupVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        vo = GroupVO.model_validate(entity)
        await _enrich_vo(self.db, vo)
        return vo

    async def tree(self, param: GroupTreeParam) -> list[GroupTreeVO]:
        all_rows = (await self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc()))).scalars().all()
        filtered = all_rows
        if param.category:
            filtered = [row for row in filtered if row.category == param.category]
        nodes = [_tree_node(row) for row in filtered]
        return build_tree(
            nodes,
            get_id=lambda node: node.id or "",
            get_parent_id=lambda node: node.parent_id or "",
            get_children=lambda node: node.children,
            get_sort_code=lambda node: node.sort_code,
        )

    async def union_tree(self) -> list[UnionTreeNode]:
        org_rows = (await self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc()))).scalars().all()
        group_rows = (await self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc()))).scalars().all()
        org_nodes: dict[str, UnionTreeNode] = {}
        for row in org_rows:
            node = UnionTreeNode(
                id=row.id,
                code=row.code,
                name=row.name,
                category=row.category,
                parent_id=row.parent_id,
                status=row.status,
                sort_code=row.sort_code,
                type="org",
            )
            org_nodes[row.id] = node
        group_nodes: dict[str, UnionTreeNode] = {}
        for row in group_rows:
            node = UnionTreeNode(
                id=row.id,
                code=row.code,
                name=row.name,
                category=row.category,
                parent_id=row.parent_id,
                org_id=row.org_id,
                status=row.status,
                sort_code=row.sort_code,
                type="group",
            )
            group_nodes[row.id] = node
        for node in group_nodes.values():
            parent_id = node.parent_id or ""
            if parent_id and parent_id in group_nodes:
                group_nodes[parent_id].children.append(node)
        orphan_groups: dict[str, list[UnionTreeNode]] = {}
        for node in group_nodes.values():
            parent_id = node.parent_id or ""
            if not parent_id or parent_id not in group_nodes:
                orphan_groups.setdefault(node.org_id or "", []).append(node)
        for org_id, node in org_nodes.items():
            if org_id in orphan_groups:
                node.children = orphan_groups[org_id] + node.children
        roots: list[UnionTreeNode] = []
        for node in org_nodes.values():
            parent_id = node.parent_id or ""
            if parent_id and parent_id in org_nodes:
                org_nodes[parent_id].children.append(node)
            else:
                roots.append(node)
        roots.sort(key=lambda item: item.sort_code or 0)
        return roots

    async def create(self, vo: GroupVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysGroup(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category,
            org_id=vo.org_id or "",
            parent_id=_normalize_parent_id(vo.parent_id),
            status=vo.status or "ENABLED",
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
            description=vo.description,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        await self.repository.insert(entity)

    async def modify(self, vo: GroupVO, actor: Optional[ActorContext] = None) -> None:
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        if vo.parent_id is not None and vo.parent_id != entity.parent_id:
            await _check_circular_parent(self.db, vo.id, vo.parent_id)
        actor_user_id = _actor_user_id(actor)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "org_id": vo.org_id or "",
            "parent_id": _normalize_parent_id(vo.parent_id),
            "status": vo.status,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "description": vo.description,
            "extra": vo.extra,
        }
        if actor_user_id:
            updates["updated_by"] = actor_user_id
        await self.db.execute(sa_update(SysGroup).where(SysGroup.id == vo.id).values(**updates))
        await self.db.commit()

    async def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        all_ids = await _collect_descendant_ids(self.db, ids)
        await self.db.execute(sa_update(SysUser).where(SysUser.group_id.in_(all_ids)).values(group_id=None))
        await self.db.commit()
        await self.repository.delete_by_ids(all_ids)

    async def options(self) -> list[GroupVO]:
        rows = (await self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc()))).scalars().all()
        return [GroupVO.model_validate(row) for row in rows]


def get_group_service(db: AsyncSession = Depends(get_db)) -> GroupService:
    return GroupService(GroupRepository(db))
