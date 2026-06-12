"""Migration registration for plugin_sys.user."""

from __future__ import annotations

import asyncio
import logging

import bcrypt
from sqlalchemy import select

from core.db import SessionLocal, register_model, register_seed
from core.enums import UserStatusEnum
from core.utils import generate_id
from .models import SysUser, RelUserRole, RelUserPermission
from ..role.models import RelRolePermission, RelRoleResource

logger = logging.getLogger(__name__)


def register_models() -> None:
    register_model(SysUser)
    register_model(RelUserRole)
    register_model(RelUserPermission)
    register_model(RelRolePermission)
    register_model(RelRoleResource)


def seed_admin_user() -> None:
    db = SessionLocal()
    try:
        existing = db.scalar(select(SysUser).where(SysUser.username == "admin"))
        if existing:
            logger.info("[Seed] Admin user already exists, skipped")
            return

        hashed_password = asyncio.run(
            asyncio.to_thread(
                lambda: bcrypt.hashpw("123456".encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
            )
        )

        user_id = generate_id()
        user = SysUser(
            id=user_id,
            username="admin",
            password=hashed_password,
            nickname="超管",
            status=UserStatusEnum.ACTIVE.value,
            created_by=user_id,
            updated_by=user_id,
        )
        db.add(user)
        db.commit()
        logger.info("[Seed] Admin user created (username: admin, password: 123456)")
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


register_models()
register_seed("admin user", seed_admin_user)
