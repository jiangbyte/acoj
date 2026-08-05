"""Team business logic."""

from __future__ import annotations

import secrets
import string

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, to_schema, to_schema_list
from app.core.security.session import SessionPayload
from app.modules.biz.clazz.repository import OjClassRepository
from app.modules.biz.course.repository import OjCourseRepository
from app.modules.biz.team.enums import TeamMemberRole, TeamScope, TeamStatus, TeamVisibility
from app.modules.biz.team.model import OjTeam
from app.modules.biz.team.repository import OjTeamRepository
from app.modules.biz.team.schema import (
    OjTeamAdminPageQuery,
    OjTeamCreateCourseRequest,
    OjTeamCreateIndependentRequest,
    OjTeamJoinRequest,
    OjTeamMemberAddRequest,
    OjTeamMemberRemoveRequest,
    OjTeamMemberSchema,
    OjTeamOwnerUpdateRequest,
    OjTeamPortalPageQuery,
    OjTeamPublicSchema,
    OjTeamSchema,
    OjTeamUpdateRequest,
    OjTeamUserSearchItem,
)
from app.modules.biz.teach_im import add_portal_member, create_bound_group, get_conversation_id, remove_portal_member
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id
from app.platform.storage.url import resolve_file_url

_INVITE_ALPHABET = string.ascii_uppercase + string.digits


def _generate_invite_code() -> str:
    return "".join(secrets.choice(_INVITE_ALPHABET) for _ in range(8))


class OjTeamService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjTeamRepository(db)
        self.class_repo = OjClassRepository(db)
        self.course_repo = OjCourseRepository(db)

    async def _unique_invite_code(self) -> str:
        for _ in range(20):
            code = _generate_invite_code()
            if not await self.repo.invite_code_exists(code):
                return code
        raise BusinessError("无法生成唯一邀请码，请重试")

    async def _to_schema(
        self, team: OjTeam, account_id: str | None = None, *, include_secrets: bool = True
    ) -> OjTeamSchema:
        schema = to_schema(OjTeamSchema, team)
        if include_secrets and team.im_group_id:
            schema.conversation_id = await get_conversation_id(self.db, team.im_group_id)
        if account_id:
            schema.is_member = (await self.repo.get_member(team.id, account_id)) is not None
        if not include_secrets:
            schema.invite_code = None
            schema.im_group_id = None
            schema.conversation_id = None
            schema.extra = {}
        return schema

    async def _to_public(self, team: OjTeam, is_member: bool = False) -> OjTeamPublicSchema:
        return OjTeamPublicSchema(
            id=team.id,
            name=team.name,
            description=team.description,
            status=team.status,
            visibility=team.visibility,
            max_members=team.max_members,
            member_count=team.member_count,
            created_at=team.created_at,
            is_member=is_member,
        )

    async def page_public(
        self, query: OjTeamPortalPageQuery, session: SessionPayload | None = None
    ) -> PageData[OjTeamPublicSchema]:
        items, total = await self.repo.page_public_independent(
            query.keyword, query.pagination.offset, query.pagination.size
        )
        member_ids: set[str] = set()
        if session and session.account_type == AccountType.PORTAL:
            for item in items:
                if await self.repo.get_member(item.id, session.account_id):
                    member_ids.add(item.id)
        schemas = [await self._to_public(item, is_member=item.id in member_ids) for item in items]
        return build_page(query.pagination, total, schemas)

    async def _sync_member_to_im(self, team: OjTeam, account_id: str) -> None:
        if team.im_group_id:
            await add_portal_member(self.db, team.im_group_id, account_id)

    async def _add_members_internal(
        self, team: OjTeam, account_ids: list[str], role: str = TeamMemberRole.MEMBER.value
    ) -> int:
        added = 0
        for account_id in dict.fromkeys(account_ids):
            if await self.repo.get_member(team.id, account_id):
                continue
            if team.member_count + 1 > team.max_members:
                raise BusinessError("小组已满")
            await self.repo.add_member(team.id, account_id, role)
            await self._sync_member_to_im(team, account_id)
            added += 1
        if added:
            await self.repo.increment_member_count(team.id, added)
        return added

    # --- independent (portal) ---

    async def create_independent(self, payload: OjTeamCreateIndependentRequest, session: SessionPayload) -> str:
        if session.account_type != AccountType.PORTAL:
            raise BusinessError("仅 Portal 用户可创建独立小组")
        invite_code = await self._unique_invite_code()
        team_id = generate_snowflake_id()
        async with transactional(self.db):
            group_id = await create_bound_group(
                self.db,
                name=payload.name,
                owner_account_type=AccountType.PORTAL.value,
                owner_account_id=session.account_id,
                source="TEAM",
                source_id=team_id,
                max_members=payload.max_members,
            )
            team = OjTeam(
                id=team_id,
                scope=TeamScope.INDEPENDENT.value,
                course_id=None,
                class_id=None,
                name=payload.name,
                description=payload.description,
                owner_id=session.account_id,
                invite_code=invite_code,
                im_group_id=group_id,
                status=TeamStatus.ENABLED.value,
                visibility=TeamVisibility.PRIVATE.value,
                max_members=payload.max_members,
                member_count=1,
            )
            await self.repo.create(team)
            await self.repo.add_member(team.id, session.account_id, TeamMemberRole.OWNER.value)
        return team.id

    async def join_by_invite(self, payload: OjTeamJoinRequest, session: SessionPayload) -> str:
        team = await self.repo.get_by_invite_code(payload.invite_code.upper())
        if team is None:
            raise NotFoundError("邀请码无效")
        if team.scope == TeamScope.COURSE.value:
            from app.modules.biz.course.enums import CourseAccessScope

            course = await self.course_repo.get_course_required(team.course_id)  # type: ignore[arg-type]
            if course.access_scope != CourseAccessScope.OPEN.value:
                if not await self.course_repo.is_account_in_course(team.course_id, session.account_id):  # type: ignore[arg-type]
                    raise BusinessError("您不是该课程关联班级的成员")
        if await self.repo.get_member(team.id, session.account_id):
            return team.id
        async with transactional(self.db):
            await self._add_members_internal(team, [session.account_id])
        return team.id

    async def leave(self, team_id: str, session: SessionPayload) -> None:
        team = await self.repo.get_required(team_id)
        member = await self.repo.get_member(team_id, session.account_id)
        if member is None:
            raise BusinessError("您不是小组成员")
        if member.role == TeamMemberRole.OWNER.value:
            raise BusinessError("组长不能退出，请先解散或转让")
        async with transactional(self.db):
            await self.repo.remove_member(team_id, session.account_id)
            await self.repo.increment_member_count(team_id, -1)
            if team.im_group_id:
                await remove_portal_member(self.db, team.im_group_id, session.account_id)

    async def dissolve(self, team_id: str, session: SessionPayload) -> None:
        team = await self.repo.get_required(team_id)
        if team.owner_id != session.account_id:
            raise BusinessError("仅组长可解散小组")
        async with transactional(self.db):
            team.status = TeamStatus.DISSOLVED.value
            await self.repo.update_entity(team)

    async def my_teams(self, session: SessionPayload) -> list[OjTeamSchema]:
        items = await self.repo.list_my_teams(session.account_id)
        return [await self._to_schema(t, session.account_id) for t in items]

    async def _ensure_owner(self, team_id: str, account_id: str) -> OjTeam:
        team = await self.repo.get_required(team_id)
        if team.status != TeamStatus.ENABLED.value:
            raise BusinessError("小组不可用")
        if team.owner_id != account_id:
            raise BusinessError("仅组长可操作")
        return team

    async def update_by_owner(self, payload: OjTeamOwnerUpdateRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            team = await self._ensure_owner(payload.id, session.account_id)
            if payload.visibility is not None and team.scope != TeamScope.INDEPENDENT.value:
                raise BusinessError("课内小组不可修改公开性")
            if payload.max_members is not None and payload.max_members < team.member_count:
                raise BusinessError("人数上限不能小于当前成员数")
            if payload.name is not None:
                team.name = payload.name
            if payload.description is not None:
                team.description = payload.description
            if payload.max_members is not None:
                team.max_members = payload.max_members
            if payload.visibility is not None:
                team.visibility = payload.visibility.value
            await self.repo.update_entity(team)

    async def add_members_by_owner(
        self, payload: OjTeamMemberAddRequest, session: SessionPayload
    ) -> None:
        await self._ensure_owner(payload.team_id, session.account_id)
        await self.add_members(payload)

    async def remove_member_by_owner(
        self, payload: OjTeamMemberRemoveRequest, session: SessionPayload
    ) -> None:
        await self._ensure_owner(payload.team_id, session.account_id)
        await self.remove_members(payload)

    async def refresh_invite(self, team_id: str, session: SessionPayload) -> str:
        async with transactional(self.db):
            team = await self._ensure_owner(team_id, session.account_id)
            code = await self._unique_invite_code()
            team.invite_code = code
            await self.repo.update_entity(team)
            return code

    async def search_portal_users(
        self, keyword: str, session: SessionPayload, *, limit: int = 20
    ) -> list[OjTeamUserSearchItem]:
        from sqlalchemy import or_, select

        from app.modules.iam.account.model import SysAccount, SysAccountIdentity
        from app.modules.iam.enums import AccountIdentityType
        from app.modules.user.portal.model import PortalUserProfile

        keyword = keyword.strip()
        if not keyword:
            return []
        like = f"%{keyword}%"
        results: list[OjTeamUserSearchItem] = []
        seen: set[str] = set()
        self_id = session.account_id

        profile_stmt = (
            select(PortalUserProfile)
            .where(
                or_(
                    PortalUserProfile.name.ilike(like),
                    PortalUserProfile.nickname.ilike(like),
                )
            )
            .limit(limit)
        )
        for profile in (await self.db.execute(profile_stmt)).scalars().all():
            if profile.account_id == self_id or profile.account_id in seen:
                continue
            seen.add(profile.account_id)
            results.append(
                OjTeamUserSearchItem(
                    account_id=profile.account_id,
                    nickname=profile.nickname or profile.name,
                    avatar=resolve_file_url(profile.avatar),
                )
            )
            if len(results) >= limit:
                return results

        id_stmt = (
            select(SysAccountIdentity.account_id, SysAccountIdentity.identifier)
            .join(SysAccount, SysAccountIdentity.account_id == SysAccount.id)
            .where(
                SysAccountIdentity.identity_type == AccountIdentityType.ACCOUNT.value,
                SysAccount.account_type == AccountType.PORTAL.value,
                SysAccountIdentity.identifier.ilike(like),
            )
            .limit(limit)
        )
        for account_id, username in (await self.db.execute(id_stmt)).all():
            if account_id == self_id or account_id in seen:
                continue
            seen.add(account_id)
            profile = await self.db.get(PortalUserProfile, account_id)
            results.append(
                OjTeamUserSearchItem(
                    account_id=account_id,
                    username=username,
                    nickname=(profile.nickname or profile.name) if profile else None,
                    avatar=resolve_file_url(profile.avatar) if profile else None,
                )
            )
            if len(results) >= limit:
                break
        return results

    # --- course-scoped (admin) ---

    async def create_course_team(
        self, payload: OjTeamCreateCourseRequest, session: SessionPayload
    ) -> str:
        course = await self.course_repo.get_course_required(payload.course_id)
        class_ids = await self.course_repo.list_class_ids(course.id)
        invite_code = await self._unique_invite_code()
        team_id = generate_snowflake_id()
        async with transactional(self.db):
            group_id = await create_bound_group(
                self.db,
                name=payload.name,
                owner_account_type=str(session.account_type),
                owner_account_id=session.account_id,
                source="TEAM",
                source_id=team_id,
                max_members=payload.max_members,
            )
            team = OjTeam(
                id=team_id,
                scope=TeamScope.COURSE.value,
                course_id=course.id,
                class_id=class_ids[0] if class_ids else None,
                name=payload.name,
                description=payload.description,
                owner_id=payload.member_account_ids[0] if payload.member_account_ids else session.account_id,
                invite_code=invite_code,
                im_group_id=group_id,
                status=TeamStatus.ENABLED.value,
                visibility=payload.visibility.value,
                max_members=payload.max_members,
                member_count=0,
            )
            await self.repo.create(team)
            if payload.member_account_ids:
                from app.modules.biz.course.enums import CourseAccessScope

                for aid in payload.member_account_ids:
                    if course.access_scope == CourseAccessScope.OPEN.value:
                        continue
                    if not await self.course_repo.is_account_in_course(course.id, aid):
                        raise BusinessError(f"账户 {aid} 不是该课程关联班级的成员")
                await self._add_members_internal(team, payload.member_account_ids)
        return team.id

    async def list_by_course_portal(self, course_id: str, session: SessionPayload) -> list[OjTeamSchema]:
        from app.modules.biz.course.enums import CourseAccessScope

        course = await self.course_repo.get_course_required(course_id)
        if course.access_scope != CourseAccessScope.OPEN.value:
            if not await self.course_repo.is_account_in_course(course_id, session.account_id):
                raise BusinessError("您不是该课程关联班级的成员")
        items = await self.repo.list_by_course(course_id)
        return [await self._to_schema(t, session.account_id) for t in items]

    # --- admin ---

    async def page_admin(self, query: OjTeamAdminPageQuery) -> PageData[OjTeamSchema]:
        items, total = await self.repo.page_admin(query)
        schemas = [await self._to_schema(t) for t in items]
        return build_page(query.pagination, total, schemas)

    async def detail(self, query: IdQuery) -> OjTeamSchema:
        return await self._to_schema(await self.repo.get_required(query.id))

    async def update(self, payload: OjTeamUpdateRequest) -> None:
        async with transactional(self.db):
            team = await self.repo.get_required(payload.id)
            for key, value in payload.model_dump(exclude={"id"}, exclude_none=True).items():
                setattr(team, key, value.value if hasattr(value, "value") else value)
            await self.repo.update_entity(team)

    async def disable(self, team_id: str) -> None:
        async with transactional(self.db):
            team = await self.repo.get_required(team_id)
            team.status = TeamStatus.DISABLED.value
            await self.repo.update_entity(team)

    async def admin_dissolve(self, team_id: str) -> None:
        async with transactional(self.db):
            team = await self.repo.get_required(team_id)
            team.status = TeamStatus.DISSOLVED.value
            await self.repo.update_entity(team)

    async def add_members(self, payload: OjTeamMemberAddRequest) -> None:
        team = await self.repo.get_required(payload.team_id)
        if team.status != TeamStatus.ENABLED.value:
            raise BusinessError("小组不可用")
        async with transactional(self.db):
            if team.scope == TeamScope.COURSE.value and team.course_id:
                from app.modules.biz.course.enums import CourseAccessScope

                course = await self.course_repo.get_course_required(team.course_id)
                for aid in payload.account_ids:
                    if course.access_scope == CourseAccessScope.OPEN.value:
                        continue
                    if not await self.course_repo.is_account_in_course(team.course_id, aid):
                        raise BusinessError(f"账户 {aid} 不是该课程关联班级的成员")
            await self._add_members_internal(team, payload.account_ids)

    async def remove_members(self, payload: OjTeamMemberRemoveRequest) -> None:
        team = await self.repo.get_required(payload.team_id)
        member = await self.repo.get_member(payload.team_id, payload.account_id)
        if member is None:
            raise BusinessError("成员不在小组中")
        if member.role == TeamMemberRole.OWNER.value:
            raise BusinessError("不能移除组长")
        async with transactional(self.db):
            await self.repo.remove_member(payload.team_id, payload.account_id)
            await self.repo.increment_member_count(payload.team_id, -1)
            if team.im_group_id:
                await remove_portal_member(self.db, team.im_group_id, payload.account_id)

    async def member_list(self, team_id: str, session: SessionPayload | None = None, admin: bool = False) -> list[OjTeamMemberSchema]:
        if not admin:
            if session is None or not await self.repo.get_member(team_id, session.account_id):
                raise BusinessError("您不是小组成员")
        return to_schema_list(OjTeamMemberSchema, await self.repo.list_members(team_id))

    async def detail_for_member(self, team_id: str, session: SessionPayload) -> OjTeamSchema:
        if not await self.repo.get_member(team_id, session.account_id):
            raise BusinessError("您不是小组成员")
        return await self._to_schema(await self.repo.get_required(team_id), session.account_id)

    async def detail_portal(self, team_id: str, session: SessionPayload | None = None) -> OjTeamSchema:
        team = await self.repo.get_required(team_id)
        if team.status == TeamStatus.DISSOLVED.value:
            raise BusinessError("小组已解散")
        if team.scope == TeamScope.COURSE.value:
            if session is None or session.account_type != AccountType.PORTAL:
                raise BusinessError("课内小组需登录后访问")
            from app.modules.biz.course.enums import CourseAccessScope

            course = await self.course_repo.get_course_required(team.course_id)  # type: ignore[arg-type]
            if course.access_scope != CourseAccessScope.OPEN.value:
                if not await self.course_repo.is_account_in_course(team.course_id, session.account_id):  # type: ignore[arg-type]
                    raise BusinessError("您不是该课程关联班级的成员")
            is_member = (await self.repo.get_member(team_id, session.account_id)) is not None
            return await self._to_schema(team, session.account_id, include_secrets=is_member)

        # Independent: PUBLIC overview for non-members; secrets only for members
        account_id = session.account_id if session and session.account_type == AccountType.PORTAL else None
        is_member = bool(account_id and await self.repo.get_member(team_id, account_id))
        if not is_member and team.visibility != TeamVisibility.PUBLIC.value:
            raise BusinessError("小组未公开")
        return await self._to_schema(team, account_id, include_secrets=is_member)
