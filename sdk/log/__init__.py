from .decorator import sys_log, record_auth_log, save_exception_log
from .persistence import (
    LogPersistenceAPI,
    LogEntry,
    set_log_persister,
    get_log_persister,
    set_op_user_resolver,
    get_op_user_resolver,
    configure_async_log_persister,
    start_log_persister,
    stop_log_persister,
    log_persister_snapshot,
    save_log,
)

# Uppercase alias for use as @SysLog("名称")
SysLog = sys_log

__all__ = [
    "sys_log", "SysLog", "record_auth_log", "save_exception_log",
    "LogPersistenceAPI", "LogEntry", "set_log_persister",
    "get_log_persister", "set_op_user_resolver", "get_op_user_resolver",
    "configure_async_log_persister", "start_log_persister", "stop_log_persister",
    "log_persister_snapshot", "save_log",
]
