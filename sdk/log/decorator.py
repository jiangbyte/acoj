import functools
import inspect
import json
import logging
from datetime import datetime
from typing import Any, Optional

from fastapi import Request

from sdk.auth.enums import RealmID
from sdk.auth.realm import infer_realm_id_from_path, realm_from_id
from sdk.utils import get_client_ip, get_city_info, generate_id
from sdk.utils.trace_utils import get_trace_id
from .persistence import LogEntry, get_op_user_resolver, save_log
from .utils import parse_user_agent, extract_params_json, get_result_json, generate_log_signature

logger = logging.getLogger(__name__)


def _get_request(*args, **kwargs) -> Optional[Request]:
    request = kwargs.get('request')
    if request:
        return request
    for arg in args:
        if isinstance(arg, Request):
            return arg
    return None


async def _get_op_user(request: Request) -> Optional[str]:
    """Get the current operator's username from the active user."""
    try:
        realm_id = infer_realm_id_from_path(request.url.path) or RealmID.BUSINESS
        user_id = await realm_from_id(realm_id).get_login_id(request)
        if not user_id:
            return None

        resolver = get_op_user_resolver()
        if resolver is None:
            return None
        return resolver(user_id)
    except Exception:
        return None


def sys_log(name: str = "未命名"):
    """
    Decorator to record operation log on route handlers.
    Logs both success (OPERATE) and exception (EXCEPTION) outcomes.

    Usage:
        @router.get("/api/v1/sys/xxx")
        @SysLog("日志名称")
        @CheckPermission("sys:xxx:page")
        async def handler(request: Request, ...):
            ...
    """
    def decorator(func):
        @functools.wraps(func)
        async def wrapper(*args, **kwargs):
            request = _get_request(*args, **kwargs)
            if not request:
                return await func(*args, **kwargs)

            start_time = datetime.now()
            params_json = extract_params_json(request, kwargs)

            try:
                result = func(*args, **kwargs)
                if inspect.isawaitable(result):
                    result = await result
                await _save_log(
                    request=request,
                    func=func,
                    name=name,
                    category="OPERATE",
                    exe_status="SUCCESS",
                    exe_message=None,
                    params_json=params_json,
                    result_json=get_result_json(result),
                    start_time=start_time,
                )
                return result
            except Exception as e:
                await _save_log(
                    request=request,
                    func=func,
                    name=name,
                    category="EXCEPTION",
                    exe_status="FAIL",
                    exe_message=str(e)[:2000],
                    params_json=params_json,
                    result_json=None,
                    start_time=start_time,
                )
                request.state._exception_logged = True
                raise
        return wrapper
    return decorator


async def _save_log(request: Request, func, name: str, category: str,
              exe_status: str, exe_message: Optional[str],
              params_json: str, result_json: Optional[str],
              start_time: datetime) -> None:
    """Persist the log entry to database."""
    try:
        user_agent = request.headers.get("user-agent", "")
        browser, os_name = parse_user_agent(user_agent)
        op_user = await _get_op_user(request)

        op_ip = get_client_ip(request)
        entry = LogEntry(
            id=generate_id(),
            category=category,
            name=name,
            exe_status=exe_status,
            exe_message=exe_message,
            trace_id=get_trace_id(),
            op_ip=op_ip,
            op_address=get_city_info(op_ip),
            op_browser=browser,
            op_os=os_name,
            class_name=func.__module__,
            method_name=func.__qualname__,
            req_method=request.method,
            req_url=str(request.url),
            param_json=params_json,
            result_json=result_json,
            op_time=start_time,
            op_user=op_user,
            sign_data="",
        )

        sign_data = generate_log_signature({
            "category": category,
            "name": name,
            "exe_status": exe_status,
            "op_ip": entry.op_ip,
            "op_time": start_time.isoformat(),
            "op_user": op_user,
            "trace_id": entry.trace_id,
        })
        entry.sign_data = sign_data

        save_log(entry)
    except Exception as e:
        logger.warning(f"Failed to save operation log: {e}")


async def save_exception_log(request: Request, exc: Exception, name: Optional[str] = None) -> None:
    """Save an unhandled exception to the database (sys_log table).

    Designed to be called from global exception handlers so that ALL
    exceptions are recorded in the database, even for handlers without
    the @SysLog decorator.  Creates its own DB session so it is
    independent of the request's transaction.
    """
    try:
        now = datetime.now()
        user_agent = request.headers.get("user-agent", "")
        browser, os_name = parse_user_agent(user_agent)
        op_user = await _get_op_user(request)
        op_ip = get_client_ip(request)
        log_name = name or f"{request.method} {request.url.path}"

        entry = LogEntry(
            id=generate_id(),
            category="EXCEPTION",
            name=log_name,
            exe_status="FAIL",
            exe_message=str(exc)[:2000],
            trace_id=get_trace_id(),
            op_ip=op_ip,
            op_address=get_city_info(op_ip),
            op_browser=browser,
            op_os=os_name,
            class_name=type(exc).__module__,
            method_name=type(exc).__qualname__,
            req_method=request.method,
            req_url=str(request.url),
            param_json="",
            result_json=None,
            op_time=now,
            op_user=op_user,
            sign_data="",
        )

        sign_data = generate_log_signature({
            "category": "EXCEPTION",
            "name": log_name,
            "exe_status": "FAIL",
            "op_ip": entry.op_ip,
            "op_time": now.isoformat(),
            "op_user": op_user,
            "trace_id": entry.trace_id,
        })
        entry.sign_data = sign_data

        save_log(entry)
    except Exception as e:
        logger.warning(f"Failed to save exception log: {e}")


def record_auth_log(request: Request, name: str, category: str,
                    exe_status: str = "SUCCESS",
                    exe_message: Optional[str] = None,
                    op_user: Optional[str] = None) -> None:
    """Public API for recording auth-related logs (login/logout) programmatically.

    Unlike the ``@sys_log`` decorator, this does NOT need a function context
    and accepts the operator name directly — which is essential for login
    events where there is no active auth token yet.
    """
    try:
        now = datetime.now()
        user_agent = request.headers.get("user-agent", "")
        browser, os_name = parse_user_agent(user_agent)
        op_ip = get_client_ip(request)
        entry = LogEntry(
            id=generate_id(),
            category=category,
            name=name,
            exe_status=exe_status,
            exe_message=exe_message,
            trace_id=get_trace_id(),
            op_ip=op_ip,
            op_address=get_city_info(op_ip),
            op_browser=browser,
            op_os=os_name,
            class_name="",
            method_name="",
            req_method=request.method,
            req_url=str(request.url),
            param_json="",
            result_json=None,
            op_time=now,
            op_user=op_user,
            sign_data="",
        )
        sign_data = generate_log_signature({
            "category": category, "name": name, "exe_status": exe_status,
            "op_ip": entry.op_ip, "op_time": now.isoformat(),
            "op_user": op_user, "trace_id": entry.trace_id,
        })
        entry.sign_data = sign_data
        save_log(entry)
    except Exception as e:
        logger.warning(f"Failed to save auth log: {e}")
