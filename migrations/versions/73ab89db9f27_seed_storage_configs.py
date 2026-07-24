"""seed preset storage configs (local, minio, s3, oss)"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = '73ab89db9f27'
down_revision: str | Sequence[str] | None = '6f2e5eb8dcf7'
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

PRESETS = [
    {
        "id": "200101",
        "name": "本地存储",
        "provider": "local",
        "bucket": "",
        "endpoint": "",
        "access_key": "",
        "secret_key": "",
        "region": "",
        "use_ssl": False,
        "base_url": "",
        "public_path": "/api/v1/files",
        "local_root": "storage",
        "is_default": True,
        "remark": "本地文件系统存储",
        "sort_code": 0,
    },
    {
        "id": "200102",
        "name": "MinIO",
        "provider": "minio",
        "bucket": "my-bucket",
        "endpoint": "http://127.0.0.1:9000",
        "access_key": "minioadmin",
        "secret_key": "minioadmin",
        "region": "",
        "use_ssl": False,
        "base_url": "",
        "public_path": "/api/v1/files",
        "local_root": "storage",
        "is_default": False,
        "remark": "MinIO 对象存储",
        "sort_code": 10,
    },
    {
        "id": "200103",
        "name": "Amazon S3",
        "provider": "s3",
        "bucket": "my-bucket",
        "endpoint": "",
        "access_key": "",
        "secret_key": "",
        "region": "us-east-1",
        "use_ssl": True,
        "base_url": "",
        "public_path": "/api/v1/files",
        "local_root": "storage",
        "is_default": False,
        "remark": "Amazon Simple Storage Service",
        "sort_code": 20,
    },
    {
        "id": "200104",
        "name": "阿里云 OSS",
        "provider": "oss",
        "bucket": "my-bucket",
        "endpoint": "https://oss-cn-hangzhou.aliyuncs.com",
        "access_key": "",
        "secret_key": "",
        "region": "cn-hangzhou",
        "use_ssl": True,
        "base_url": "",
        "public_path": "/api/v1/files",
        "local_root": "storage",
        "is_default": False,
        "remark": "阿里云对象存储 OSS",
        "sort_code": 30,
    },
]


def upgrade() -> None:
    conn = op.get_bind()
    if conn.dialect.name.startswith("postgresql"):
        for preset in PRESETS:
            exists = conn.execute(
                sa.text("SELECT 1 FROM sys_storage_config WHERE id = :id"),
                {"id": preset["id"]},
            ).scalar()
            if not exists:
                conn.execute(
                    sa.text("""
                        INSERT INTO sys_storage_config (
                            id, name, provider, bucket, endpoint, access_key, secret_key,
                            region, use_ssl, base_url, public_path, local_root, is_default,
                            remark, sort_code
                        ) VALUES (
                            :id, :name, :provider, :bucket, :endpoint, :access_key, :secret_key,
                            :region, :use_ssl, :base_url, :public_path, :local_root, :is_default,
                            :remark, :sort_code
                        )
                    """),
                    preset,
                )


def downgrade() -> None:
    conn = op.get_bind()
    if conn.dialect.name.startswith("postgresql"):
        ids = [p["id"] for p in PRESETS]
        conn.execute(
            sa.text("DELETE FROM sys_storage_config WHERE id = ANY(:ids)"),
            {"ids": ids},
        )
