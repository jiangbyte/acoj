"""Thin adapter: admin options + judge payloads from acoj-sandbox-lang.

Do not redefine language metadata here — use LanguageId / worker_image helpers.
"""

from __future__ import annotations

from typing import Any

from acoj_sandbox_lang import (
    iter_worker_image_languages,
    require_worker_image_language,
    worker_image_commands,
)

from app.core.exceptions.business import BusinessError


def list_worker_languages() -> list[dict[str, Any]]:
    """Options for GET /biz/problem/language/options (all admin selectors)."""
    return [
        {
            "key": lang.value,
            "label": lang.label,
            "extension": lang.extension,
            "source_filename": lang.source_filename,
        }
        for lang in iter_worker_image_languages()
    ]


def ensure_worker_language_key(language_key: str) -> str:
    try:
        return require_worker_image_language(language_key).value
    except ValueError as exc:
        raise BusinessError(f"不支持的语言标识（须为 worker 镜像已启用语言）: {language_key}") from exc


def get_worker_language_payload(language_key: str) -> dict[str, Any]:
    """Language dict for Celery JudgePayload."""
    lang = None
    try:
        lang = require_worker_image_language(language_key)
        compile_cmd, run_cmd = worker_image_commands(lang)
    except ValueError as exc:
        raise BusinessError(f"不支持的语言标识（须为 worker 镜像已启用语言）: {language_key}") from exc
    return {
        "key": lang.value,
        "name": lang.label,
        "extension": lang.extension,
        "compile_command": compile_cmd or None,
        "run_command": run_cmd,
    }
