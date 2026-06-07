"""
Pluggable log persistence interface — mirrors hei-gin's ``api/log.go`` + ``sdk/log/syslog.go``.

Defines ``LogPersistenceAPI`` and a global ``log_persister`` callback,
making the log storage backend swappable (e.g. DB, Redis, external service).

Usage::

    from core.log.persistence import LogPersistenceAPI, set_log_persister

    class MyLogStore(LogPersistenceAPI):
        def save_log(self, entry: LogEntry) -> None:
            db.session.add(LogModel(**entry))

    set_log_persister(MyLogStore())
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Optional, Protocol


@dataclass
class LogEntry:
    """A single operation log entry.

    Mirrors hei-gin's ``api.LogEntry``.
    """
    id: str = ""
    category: str = "OPERATE"        # OPERATE | EXCEPTION | LOGIN | LOGOUT
    name: str = ""
    exe_status: str = "SUCCESS"      # SUCCESS | FAIL
    exe_message: str = ""
    op_ip: str = ""
    op_address: str = ""
    op_browser: str = ""
    op_os: str = ""
    op_user: str = ""
    trace_id: str = ""
    sign_data: str = ""
    req_method: str = ""
    req_url: str = ""
    param_json: str = ""
    result_json: str = ""
    op_time: str = ""
    class_name: str = ""
    method_name: str = ""


class LogPersistenceAPI(Protocol):
    """Interface for log persistence backends.

    Mirrors hei-gin's ``api.LogPersistenceAPI``.
    """

    def save_log(self, entry: LogEntry) -> None:
        """Persist a log entry."""
        ...


# ── Global persister ────────────────────────────────────────────────

_log_persister: Optional[LogPersistenceAPI] = None


def set_log_persister(persister: LogPersistenceAPI) -> None:
    """Set the global log persistence backend.

    Mirrors hei-gin's pattern of assigning ``log.LogPersistence = fn``.
    """
    global _log_persister
    _log_persister = persister


def get_log_persister() -> Optional[LogPersistenceAPI]:
    """Return the current log persistence backend."""
    return _log_persister


def save_log(entry: LogEntry) -> None:
    """Persist a log entry via the configured backend.

    If no backend is configured, the log is written to Python's logger.
    """
    persister = _log_persister
    if persister is not None:
        persister.save_log(entry)
    else:
        import logging
        logging.getLogger(__name__).warning(
            "No log persister configured — dropping log: %s [%s] %s",
            entry.category, entry.exe_status, entry.name,
        )
