"""Class business logic."""

from __future__ import annotations

import secrets
import string

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.core.security.session import SessionPayload
from app.modules.biz.clazz.enums import ClassStatus, ClassVisibility
from app.modules.biz.clazz.model import OjClass
from app.modules.biz.clazz.repository import OjClassRepository
from app.modules.biz.clazz.schema import (
    OjClassAdminPageQuery,
    OjClassCreateRequest,
    OjClassJoinRequest,
    OjClassMemberAddRequest,
    OjClassMemberRemoveRequest,
    OjClassMemberSchema,
    OjClassPortalPageQuery,
    OjClassPublicSchema,
    OjClassRefreshInviteRequest,
    OjClassSchema,
    OjClassUpdateRequest,
)
from app.modules.biz.teach_im import add_portal_member, create_bound_group, get_conversation_id, remove_portal_member
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id

_INVITE_ALPHABET = string.ascii_uppercase + string.digits


def _generate_invite_code() -> str:
    return "".join(secrets.choice(_INVITE_ALPHABET) for _ in range(8))


class OjClassService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjClassRepository(db)

    async def _unique_invite_code(self) -> str:
        for _ in range(20):
            code = _generate_invite_code()
            if not await self.repo.invite_code_exists(code):
                return code
        raise BusinessError("无法生成唯一邀请码，请重试")

    async def _to_schema(self, clazz: OjClass, *, include_secrets: bool = True, joined: bool = False) -> OjClassSchema:
        schema = to_schema(OjClassSchema, clazz)
        schema.joined = joined
        if include_secrets and clazz.im_group_id:
            schema.conversation_id = await get_conversation_id(self.db, clazz.im_group_id)
        if not include_secrets:
            schema.invite_code = None
            schema.im_group_id = None
            schema.conversation_id = None
            schema.extra = {}
        return schema

    async def _to_public(self, clazz: OjClass, joined: bool = False) -> OjClassPublicSchema:
        return OjClassPublicSchema(
            id=clazz.id,
            code=clazz.code,
            name=clazz.name,
            summary=clazz.summary,
            status=clazz.status,
            visibility=clazz.visibility,
            member_count=clazz.member_count,
            created_at=clazz.created_at,
            joined=joined,
        )

    async def page_public(
        self, query: OjClassPortalPageQuery, session: SessionPayload | None = None
    ) -> PageData[OjClassPublicSchema]:
        items, total = await self.repo.page_public(
            query.keyword, query.pagination.offset, query.pagination.size
        )
        joined_ids: set[str] = set()
        if session and session.account_type == AccountType.PORTAL:
            for item in items:
                if await self.repo.is_member(item.id, session.account_id):
                    joined_ids.add(item.id)
        schemas = [await self._to_public(item, joined=item.id in joined_ids) for item in items]
        return build_page(query.pagination, total, schemas)

    async def create(self, payload: OjClassCreateRequest, session: SessionPayload) -> str:
        invite_code = await self._unique_invite_code()
        entity_id = generate_snowflake_id()
        async with transactional(self.db):
            group_id = await create_bound_group(
                self.db,
                name=payload.name,
                owner_account_type=str(session.account_type),
                owner_account_id=session.account_id,
                source="CLASS",
                source_id=entity_id,
            )
            entity = OjClass(
                id=entity_id,
                code=payload.code,
                name=payload.name,
                summary=payload.summary,
                invite_code=invite_code,
                status=ClassStatus.ENABLED.value,
                visibility=payload.visibility.value,
                im_group_id=group_id,
                member_count=0,
                extra=payload.extra,
            )
            await self.repo.create(entity)
        return entity.id

    async def update(self, payload: OjClassUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjClassSchema:
        return await self._to_schema(await self.repo.get_required(query.id))

    async def page_admin(self, query: OjClassAdminPageQuery) -> PageData[OjClassSchema]:
        items, total = await self.repo.page_admin(query)
        schemas = [await self._to_schema(item) for item in items]
        return build_page(query.pagination, total, schemas)

    async def add_members(self, payload: OjClassMemberAddRequest) -> None:
        clazz = await self.repo.get_required(payload.class_id)
        if clazz.status != ClassStatus.ENABLED.value:
            raise BusinessError("班级已禁用")
        async with transactional(self.db):
            added = 0
            for account_id in dict.fromkeys(payload.account_ids):
                if await self.repo.get_member(payload.class_id, account_id):
                    continue
                await self.repo.add_member(payload.class_id, account_id, payload.role.value)
                if clazz.im_group_id:
                    await add_portal_member(self.db, clazz.im_group_id, account_id)
                added += 1
            if added:
                await self.repo.increment_member_count(payload.class_id, added)

    async def remove_members(self, payload: OjClassMemberRemoveRequest) -> None:
        clazz = await self.repo.get_required(payload.class_id)
        if not await self.repo.get_member(payload.class_id, payload.account_id):
            raise BusinessError("成员不在班级中")
        async with transactional(self.db):
            await self.repo.remove_member(payload.class_id, payload.account_id)
            await self.repo.increment_member_count(payload.class_id, -1)
            if clazz.im_group_id:
                await remove_portal_member(self.db, clazz.im_group_id, payload.account_id)

    async def refresh_invite_code(self, payload: OjClassRefreshInviteRequest) -> str:
        invite_code = await self._unique_invite_code()
        async with transactional(self.db):
            clazz = await self.repo.get_required(payload.id)
            clazz.invite_code = invite_code
            await self.db.flush()
        return invite_code

    async def join_by_invite(self, payload: OjClassJoinRequest, session: SessionPayload) -> str:
        if session.account_type != AccountType.PORTAL:
            raise BusinessError("仅 Portal 用户可加入班级")
        clazz = await self.repo.get_by_invite_code(payload.invite_code.upper())
        if clazz is None:
            raise NotFoundError("邀请码无效")
        if clazz.status != ClassStatus.ENABLED.value:
            raise BusinessError("班级已禁用")
        if await self.repo.get_member(clazz.id, session.account_id):
            return clazz.id
        async with transactional(self.db):
            await self.repo.add_member(clazz.id, session.account_id, "STUDENT")
            await self.repo.increment_member_count(clazz.id)
            if clazz.im_group_id:
                await add_portal_member(self.db, clazz.im_group_id, session.account_id)
        return clazz.id

    async def my_classes(self, session: SessionPayload) -> list[OjClassSchema]:
        items = await self.repo.list_my_classes(session.account_id)
        return [await self._to_schema(item, joined=True) for item in items]

    async def detail_portal(self, class_id: str, session: SessionPayload | None = None) -> OjClassSchema:
        clazz = await self.repo.get_required(class_id)
        if clazz.status != ClassStatus.ENABLED.value:
            raise BusinessError("班级不可用")
        joined = False
        if session and session.account_type == AccountType.PORTAL:
            joined = await self.repo.is_member(class_id, session.account_id)
        if not joined and clazz.visibility != ClassVisibility.PUBLIC.value:
            raise BusinessError("班级未公开")
        return await self._to_schema(clazz, include_secrets=joined, joined=joined)

    async def detail_for_member(self, class_id: str, session: SessionPayload) -> OjClassSchema:
        if not await self.repo.is_member(class_id, session.account_id):
            raise BusinessError("您不是该班级成员")
        return await self._to_schema(await self.repo.get_required(class_id), joined=True)

    async def member_list(self, class_id: str, session: SessionPayload | None = None, admin: bool = False) -> list[OjClassMemberSchema]:
        if not admin:
            if session is None or not await self.repo.is_member(class_id, session.account_id):
                raise BusinessError("您不是该班级成员")
        members = await self.repo.list_members(class_id)
        return to_schema_list(OjClassMemberSchema, members)

    async def ensure_member(self, class_id: str, account_id: str) -> None:
        if not await self.repo.is_member(class_id, account_id):
            raise BusinessError("您不是该班级成员")

    async def get_class_id_for_member_check(self, class_id: str) -> OjClass:
        return await self.repo.get_required(class_id)
