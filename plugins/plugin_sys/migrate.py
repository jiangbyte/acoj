"""Centralized migration registration for plugin_sys."""

from __future__ import annotations

import asyncio
import logging

import bcrypt
from sqlalchemy import select

from plugins.plugin_sys.banner.models import SysBanner
from plugins.plugin_sys.config.models import SysConfig
from plugins.plugin_sys.dict.models import SysDict
from plugins.plugin_sys.file.models import SysFile
from plugins.plugin_sys.group.models import SysGroup
from plugins.plugin_sys.home.models import SysQuickAction
from plugins.plugin_sys.log.models import SysLog
from plugins.plugin_sys.notice.models import SysNotice
from plugins.plugin_sys.org.models import SysOrg
from plugins.plugin_sys.position.models import SysPosition
from plugins.plugin_sys.resource.models import SysModule, SysResource
from plugins.plugin_sys.role.models import SysRole, RelRolePermission, RelRoleResource
from plugins.plugin_sys.user.models import SysUser, RelUserRole, RelUserPermission
from sdk.infra.db import register_model
from sdk.infra.db import SessionLocal, register_seed
from sdk.enums import UserStatusEnum
from sdk.utils import generate_id

logger = logging.getLogger(__name__)


def register_all_models() -> None:
    register_model(SysBanner)
    register_model(SysConfig)
    register_model(SysDict)
    register_model(SysFile)
    register_model(SysGroup)
    register_model(SysQuickAction)
    register_model(SysLog)
    register_model(SysNotice)
    register_model(SysOrg)
    register_model(SysPosition)
    register_model(SysModule)
    register_model(SysResource)
    register_model(SysRole)
    register_model(RelRolePermission)
    register_model(RelRoleResource)
    register_model(SysUser)
    register_model(RelUserRole)
    register_model(RelUserPermission)


def register_all_seeds() -> None:
    register_seed("admin user", seed_admin_user)


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
