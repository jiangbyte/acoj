"""Admin OJ language helpers — catalog from acoj_sandbox_lang (single source).

Business layer only decides which keys to expose (e.g. exclude testlib checker).
"""

from __future__ import annotations

from typing import Any

from acoj_sandbox_lang import LanguageId, iter_language_ids, parse_language_id

from app.core.exceptions.business import BusinessError

# Not used as a problem submission language.
_EXCLUDED_KEYS = frozenset({LanguageId.TESTLIB_CHECKER_CPP.value})


def list_sandbox_languages(*, include_checker: bool = False) -> list[dict[str, Any]]:
    """Frontend select options; keys/labels/extensions come from LanguageId."""
    items: list[dict[str, Any]] = []
    for lang in iter_language_ids():
        if not include_checker and lang.value in _EXCLUDED_KEYS:
            continue
        items.append(
            {
                "key": lang.value,
                "label": lang.label,
                "extension": lang.extension,
                "source_filename": lang.source_filename,
            }
        )
    return items


def is_valid_sandbox_language_key(language_key: str, *, allow_checker: bool = False) -> bool:
    key = (language_key or "").strip()
    if not key:
        return False
    try:
        lang = parse_language_id(key)
    except ValueError:
        return False
    if not allow_checker and lang.value in _EXCLUDED_KEYS:
        return False
    return True


def ensure_sandbox_language_key(language_key: str) -> str:
    key = (language_key or "").strip()
    if not is_valid_sandbox_language_key(key):
        raise BusinessError(f"不支持的语言标识（须与 sandbox LanguageId 对齐）: {language_key}")
    return key
