"""Shared lightweight option DTOs for OJ admin select lists."""

from app.core.schema.base import ApiSchema


class OjNamedOption(ApiSchema):
    """id/code/name option for selects — not a paged admin record."""

    id: str
    code: str
    name: str
