import logging
from datetime import datetime, timezone

from sqlalchemy import and_, or_, select, union
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import joinedload

from app.core.config.enums import AccountType
from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, PageQuery
from app.core.schema.base import IdsRequest
from app.core.security.session import SessionPayload
from app.modules.message.enums import FriendRequestStatus, FriendStatus
from app.modules.message.friend.model import MsgFriend, MsgFriendRequest
from app.modules.message.friend.schema import (
    FriendApplyRequest,
    FriendHandleRequest,
    FriendRemoveRequest,
    FriendRequestCountResponse,
    FriendRequestSchema,
    FriendSchema,
    FriendSearchSchema,
    FriendSetRemarkRequest,
)
from app.modules.user.admin.model import AdminUserProfile
from app.modules.user.portal.model import PortalUserProfile
from app.platform.db.base import Base
from app.platform.id_generator.snowflake import generate_snowflake_id

logger = logging.getLogger(__name__)

_PROFILE_MODEL_MAP: dict[str, type[Base]] = {
    AccountType.ADMIN.value: AdminUserProfile,
    AccountType.PORTAL.value: PortalUserProfile,
}

_ID_MAP: dict[str, str] = {
    AccountType.ADMIN.value: "admin",
    AccountType.PORTAL.value: "portal",
}


def _profile_model(account_type: str) -> type[Base]:
    model = _PROFILE_MODEL_MAP.get(account_type)
    if model is None:
        raise BusinessError(f"Unsupported account type: {account_type}")
    return model


class FriendService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def list_my_friends(self, session: SessionPayload) -> list[FriendSchema]:
        stmt = select(MsgFriend).where(
            MsgFriend.account_type == session.account_type,
            MsgFriend.account_id == session.account_id,
            MsgFriend.status == FriendStatus.ACTIVE.value,
        ).order_by(MsgFriend.friend_at.desc())
        result = await self.db.execute(stmt)
        rows = result.scalars().all()

        friends: list[FriendSchema] = []
        for row in rows:
            profile = await self._get_profile(row.friend_account_type, row.friend_account_id)
            friends.append(FriendSchema(
                id=row.id,
                account_type=row.account_type,
                account_id=row.account_id,
                friend_account_type=row.friend_account_type,
                friend_account_id=row.friend_account_id,
                remark=row.remark,
                status=row.status,
                friend_at=row.friend_at,
                friend_name=profile.get("name") or profile.get("nickname") if profile else None,
                friend_avatar=profile.get("avatar") if profile else None,
                friend_title=profile.get("title") if profile else None,
                friend_department=profile.get("department") if profile else None,
            ))
        return friends

    async def search_users(
        self, keyword: str, session: SessionPayload, max_results: int = 20,
    ) -> list[FriendSearchSchema]:
        account_type = session.account_type
        account_id = session.account_id

        results: list[FriendSearchSchema] = []

        # Search admin profiles
        if account_type != AccountType.ADMIN.value:
            admin_results = await self._search_profile(
                AdminUserProfile, AccountType.ADMIN.value, keyword, account_type, account_id, max_results,
            )
            results.extend(admin_results)

        # Search portal profiles
        portal_results = await self._search_profile(
            PortalUserProfile, AccountType.PORTAL.value, keyword, account_type, account_id, max_results,
        )
        results.extend(portal_results)

        # Mark existing friends
        friend_set = await self._get_friend_set(account_type, account_id)
        for item in results:
            if (item.account_type, item.account_id) in friend_set:
                item.is_friend = True

        return results[:max_results]

    async def apply_friend(
        self, payload: FriendApplyRequest, session: SessionPayload,
    ) -> None:
        applicant_type = session.account_type
        applicant_id = session.account_id
        recipient_type = payload.friend_account_type.value
        recipient_id = payload.friend_account_id

        if applicant_type == recipient_type and applicant_id == recipient_id:
            raise BusinessError("不能添加自己为好友")

        # Check existing active friendship
        existing = await self._get_friend_record(
            applicant_type, applicant_id, recipient_type, recipient_id,
        )
        if existing and existing.status == FriendStatus.ACTIVE.value:
            raise BusinessError("已经是好友了")

        # Check pending request
        stmt = select(MsgFriendRequest).where(
            MsgFriendRequest.applicant_type == applicant_type,
            MsgFriendRequest.applicant_id == applicant_id,
            MsgFriendRequest.recipient_type == recipient_type,
            MsgFriendRequest.recipient_id == recipient_id,
            MsgFriendRequest.status == FriendRequestStatus.PENDING.value,
        )
        result = await self.db.execute(stmt)
        if result.scalar_one_or_none():
            raise BusinessError("已发送过好友申请，请等待对方处理")

        request = MsgFriendRequest(
            id=generate_snowflake_id(),
            applicant_type=applicant_type,
            applicant_id=applicant_id,
            recipient_type=recipient_type,
            recipient_id=recipient_id,
            message=payload.message,
            status=FriendRequestStatus.PENDING.value,
        )
        self.db.add(request)
        await self.db.flush()

    async def handle_friend_request(
        self, payload: FriendHandleRequest, session: SessionPayload,
    ) -> None:
        stmt = select(MsgFriendRequest).where(
            MsgFriendRequest.id == payload.request_id,
            MsgFriendRequest.recipient_type == session.account_type,
            MsgFriendRequest.recipient_id == session.account_id,
            MsgFriendRequest.status == FriendRequestStatus.PENDING.value,
        )
        result = await self.db.execute(stmt)
        request = result.scalar_one_or_none()
        if not request:
            raise BusinessError("申请不存在或已处理")

        now = datetime.now(timezone.utc)
        request.status = FriendRequestStatus.ACCEPTED.value if payload.accept else FriendRequestStatus.REJECTED.value
        request.handled_at = now

        if payload.accept:
            # Create bidirectional friendship
            for (a_type, a_id, f_type, f_id) in [
                (request.applicant_type, request.applicant_id, request.recipient_type, request.recipient_id),
                (request.recipient_type, request.recipient_id, request.applicant_type, request.applicant_id),
            ]:
                existing = await self._get_friend_record(a_type, a_id, f_type, f_id)
                if existing:
                    existing.status = FriendStatus.ACTIVE.value
                else:
                    self.db.add(MsgFriend(
                        id=generate_snowflake_id(),
                        account_type=a_type,
                        account_id=a_id,
                        friend_account_type=f_type,
                        friend_account_id=f_id,
                        status=FriendStatus.ACTIVE.value,
                        friend_at=now,
                    ))

        await self.db.flush()

    async def remove_friend(
        self, payload: FriendRemoveRequest, session: SessionPayload,
    ) -> None:
        """双向删除好友关系（软删除）。"""
        account_type = session.account_type
        account_id = session.account_id
        friend_type = payload.friend_account_type.value
        friend_id = payload.friend_account_id

        for (a_type, a_id, f_type, f_id) in [
            (account_type, account_id, friend_type, friend_id),
            (friend_type, friend_id, account_type, account_id),
        ]:
            record = await self._get_friend_record(a_type, a_id, f_type, f_id)
            if record:
                record.status = FriendStatus.DELETED.value

        await self.db.flush()

    async def set_remark(
        self, payload: FriendSetRemarkRequest, session: SessionPayload,
    ) -> None:
        record = await self._get_friend_record(
            session.account_type, session.account_id,
            payload.friend_account_type.value, payload.friend_account_id,
        )
        if not record or record.status != FriendStatus.ACTIVE.value:
            raise BusinessError("好友关系不存在")
        record.remark = payload.remark
        await self.db.flush()

    async def my_requests(
        self, session: SessionPayload,
    ) -> list[FriendRequestSchema]:
        """获取我的好友申请（收到的 + 发出的）。"""
        stmt = select(MsgFriendRequest).where(
            or_(
                and_(
                    MsgFriendRequest.recipient_type == session.account_type,
                    MsgFriendRequest.recipient_id == session.account_id,
                ),
                and_(
                    MsgFriendRequest.applicant_type == session.account_type,
                    MsgFriendRequest.applicant_id == session.account_id,
                ),
            ),
        ).order_by(MsgFriendRequest.created_at.desc())
        result = await self.db.execute(stmt)
        rows = result.scalars().all()

        items: list[FriendRequestSchema] = []
        for row in rows:
            applicant_profile = await self._get_profile(row.applicant_type, row.applicant_id)
            items.append(FriendRequestSchema(
                id=row.id,
                applicant_type=row.applicant_type,
                applicant_id=row.applicant_id,
                recipient_type=row.recipient_type,
                recipient_id=row.recipient_id,
                message=row.message,
                status=row.status,
                applicant_name=applicant_profile.get("name") or applicant_profile.get("nickname") if applicant_profile else None,
                applicant_avatar=applicant_profile.get("avatar") if applicant_profile else None,
                created_at=row.created_at,
                handled_at=row.handled_at,
            ))
        return items

    async def pending_request_count(
        self, session: SessionPayload,
    ) -> FriendRequestCountResponse:
        stmt = select(MsgFriendRequest).where(
            MsgFriendRequest.recipient_type == session.account_type,
            MsgFriendRequest.recipient_id == session.account_id,
            MsgFriendRequest.status == FriendRequestStatus.PENDING.value,
        )
        result = await self.db.execute(stmt)
        count = len(result.scalars().all())
        return FriendRequestCountResponse(count=count)

    async def _get_profile(self, account_type: str, account_id: str) -> dict | None:
        model = _profile_model(account_type)
        stmt = select(model).where(model.account_id == account_id)  # type: ignore
        result = await self.db.execute(stmt)
        row = result.scalar_one_or_none()
        if row is None:
            return None
        return {
            "name": getattr(row, "name", None) or getattr(row, "nickname", None),
            "nickname": getattr(row, "nickname", None),
            "avatar": getattr(row, "avatar", None),
            "title": getattr(row, "title", None) or getattr(row, "bio", None),
            "department": getattr(row, "department", None),
        }

    async def _search_profile(
        self, model: type[Base], search_type: str,
        keyword: str, self_type: str, self_id: str,
        limit: int,
    ) -> list[FriendSearchSchema]:
        # Skip self
        if self_type == search_type:
            stmt = select(model).where(
                model.account_id != self_id,  # type: ignore
                or_(
                    model.name.ilike(f"%{keyword}%"),  # type: ignore
                    model.nickname.ilike(f"%{keyword}%"),  # type: ignore
                ),
            ).limit(limit)
        else:
            stmt = select(model).where(
                or_(
                    model.name.ilike(f"%{keyword}%"),  # type: ignore
                    model.nickname.ilike(f"%{keyword}%"),  # type: ignore
                ),
            ).limit(limit)

        result = await self.db.execute(stmt)
        rows = result.scalars().all()
        return [
            FriendSearchSchema(
                account_type=search_type,
                account_id=row.account_id,
                name=row.name or row.nickname,
                avatar=row.avatar,
                title=getattr(row, "title", None) or getattr(row, "bio", None),
                department=getattr(row, "department", None),
            )
            for row in rows
        ]

    async def _get_friend_record(
        self, a_type: str, a_id: str, f_type: str, f_id: str,
    ) -> MsgFriend | None:
        stmt = select(MsgFriend).where(
            MsgFriend.account_type == a_type,
            MsgFriend.account_id == a_id,
            MsgFriend.friend_account_type == f_type,
            MsgFriend.friend_account_id == f_id,
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def _get_friend_set(
        self, account_type: str, account_id: str,
    ) -> set[tuple[str, str]]:
        stmt = select(MsgFriend).where(
            MsgFriend.account_type == account_type,
            MsgFriend.account_id == account_id,
            MsgFriend.status == FriendStatus.ACTIVE.value,
        )
        result = await self.db.execute(stmt)
        rows = result.scalars().all()
        return {(row.friend_account_type, row.friend_account_id) for row in rows}
