"""Gunicorn configuration for production.

Runs multiple Uvicorn workers per container for process-level concurrency on
top of each worker's async event loop. Each worker owns its own async + sync
SQLAlchemy engine and Redis pool, so size ``WEB_CONCURRENCY`` such that
``(db.pool_size + db.max_overflow) * workers`` stays under MySQL
``max_connections``.
"""

from __future__ import annotations

import os

from sdk.config.settings import settings

# ── Networking ────────────────────────────────────────────────────────
bind = f"{settings.app.host}:{settings.app.port}"
# Trust X-Forwarded-* from the upstream proxy (uvicorn --proxy-headers equiv).
forwarded_allow_ips = os.environ.get("FORWARDED_ALLOW_IPS", "*")

# ── Workers ───────────────────────────────────────────────────────────
workers = int(os.environ.get("WEB_CONCURRENCY", "4"))
worker_class = "uvicorn.workers.UvicornWorker"

# ── Timeouts / lifecycle ──────────────────────────────────────────────
timeout = int(os.environ.get("GUNICORN_TIMEOUT", "60"))
graceful_timeout = int(os.environ.get("GUNICORN_GRACEFUL_TIMEOUT", "30"))
keepalive = settings.app.timeout_keep_alive

# Recycle workers periodically to bound memory/fd leaks over long runtimes.
max_requests = int(os.environ.get("GUNICORN_MAX_REQUESTS", "2000"))
max_requests_jitter = int(os.environ.get("GUNICORN_MAX_REQUESTS_JITTER", "200"))

# ── Logging ───────────────────────────────────────────────────────────
accesslog = os.environ.get("GUNICORN_ACCESSLOG", "-")
errorlog = os.environ.get("GUNICORN_ERRORLOG", "-")
loglevel = os.environ.get("GUNICORN_LOGLEVEL", "info")


def post_fork(server, worker):
    """Give each worker a unique Snowflake instance id.

    The in-process lock in ``snowflake_utils`` cannot protect across processes,
    so distinct instance ids are what prevent cross-worker id collisions on the
    same host. Operators must still give each *host* a distinct base instance
    via ``SNOWFLAKE__INSTANCE`` for cross-host uniqueness.
    """
    from sdk.utils.snowflake_utils import configure_instance

    base = settings.snowflake.instance
    configure_instance((base + worker.age) % 1024)
