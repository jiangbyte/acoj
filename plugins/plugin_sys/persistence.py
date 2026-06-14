"""
Log persistence — saves operation logs to the database.

Mirrors hei-gin's ``plugins/plugin-sys/persistence.go``.
"""

from __future__ import annotations

import logging

from sdk.log import LogEntry
from plugins.plugin_sys.log.models import SysLog

logger = logging.getLogger(__name__)


class DbLogPersister:
    """Persists operation logs to the database.

    Mirrors hei-gin's logPersister struct.
    """

    def __init__(self, session_factory):
        self._session_factory = session_factory

    def save_log(self, entry: LogEntry) -> None:
        db = self._session_factory()
        try:
            db.add(
                SysLog(
                    id=entry.id,
                    category=entry.category,
                    name=entry.name,
                    exe_status=entry.exe_status,
                    exe_message=entry.exe_message,
                    trace_id=entry.trace_id,
                    op_ip=entry.op_ip,
                    op_address=entry.op_address,
                    op_browser=entry.op_browser,
                    op_os=entry.op_os,
                    class_name=entry.class_name,
                    method_name=entry.method_name,
                    req_method=entry.req_method,
                    req_url=entry.req_url,
                    param_json=entry.param_json,
                    result_json=entry.result_json,
                    op_time=entry.op_time,
                    op_user=entry.op_user,
                    sign_data=entry.sign_data,
                )
            )
            db.commit()
        except Exception:
            logger.exception("[LogPersister] Failed to save log")
            db.rollback()
        finally:
            db.close()
