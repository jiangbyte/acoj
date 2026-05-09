from typing import Set, Optional, List
from core.constants.base_fields import BASE_SYSTEM_FIELDS


def strip_system_fields(data: dict, extra_fields: Optional[Set[str]] = None) -> dict:
    """Remove system audit fields from a VO model_dump() dict.

    Args:
        data: The dict from VO.model_dump().
        extra_fields: Additional fields to strip (e.g. {'role_ids'}).

    Returns:
        A new dict without system/protected fields.
    """
    exclude = BASE_SYSTEM_FIELDS | (extra_fields or set())
    return {k: v for k, v in data.items() if k not in exclude}


def apply_update(entity, update_data: dict, extra_protected: Optional[Set[str]] = None):
    """Apply update_data to an ORM entity, skipping system fields.

    Args:
        entity: The ORM model instance to update.
        update_data: Dict from VO.model_dump(exclude_unset=True).
        extra_protected: Additional fields to protect (e.g. {'code', 'password'}).
    """
    protected = BASE_SYSTEM_FIELDS | (extra_protected or set())
    for key, value in update_data.items():
        if key not in protected:
            setattr(entity, key, value)


def make_template(model_class, extra_exclude: Optional[Set[str]] = None) -> List[dict]:
    """Generate an import template with all model fields except system ones.

    Args:
        model_class: The SQLAlchemy model class.
        extra_exclude: Additional fields to exclude from the template.

    Returns:
        A list with a single dict mapping field names to empty strings.
    """
    exclude = BASE_SYSTEM_FIELDS | (extra_exclude or set())
    fields = [c.name for c in model_class.__table__.columns if c.name not in exclude]
    return [{field: "" for field in fields}]
