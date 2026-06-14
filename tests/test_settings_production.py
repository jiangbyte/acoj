from __future__ import annotations

import pytest

from sdk.config.settings import Settings


def test_validate_runtime_rejects_weak_production_defaults() -> None:
    settings = Settings.model_validate(
        {
            "app": {"env": "prod", "debug": False, "host": "0.0.0.0", "port": 18886},
            "db": {"host": "127.0.0.1", "port": 3306, "user": "root", "password": "123456", "database": "hei"},
            "redis": {"host": "127.0.0.1", "port": 6379, "password": "change-me", "database": 1},
            "token": {"expire_seconds": 3600, "token_name": "Authorization"},
            "sm2": {"private_key": "", "public_key": ""},
            "cors": {"allow_origins": ["*"]},
            "user": {"reset_password": "123456"},
            "swagger": {"enabled": False},
            "auth": {"business_register_enabled": False},
        }
    )

    with pytest.raises(ValueError, match="invalid production config"):
        settings.validate_runtime(redis_required=True)


def test_validate_runtime_accepts_hardened_production_config() -> None:
    settings = Settings.model_validate(
        {
            "app": {"env": "prod", "debug": False, "host": "0.0.0.0", "port": 18886},
            "db": {
                "host": "127.0.0.1",
                "port": 3306,
                "user": "hei",
                "password": "s3cure-db-password",
                "database": "hei",
            },
            "redis": {
                "host": "127.0.0.1",
                "port": 6379,
                "password": "s3cure-redis-password",
                "database": 1,
            },
            "token": {"expire_seconds": 3600, "token_name": "Authorization"},
            "sm2": {"private_key": "private-key", "public_key": "public-key"},
            "cors": {"allow_origins": ["https://admin.example.com"]},
            "user": {"reset_password": "temporary-passphrase"},
            "swagger": {"enabled": False},
            "auth": {"business_register_enabled": False},
        }
    )

    settings.validate_runtime(redis_required=True)
