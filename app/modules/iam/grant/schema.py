from datetime import datetime

from pydantic import Field

from app.core.config.enums import DataScope, GrantEffect, GrantMode, GrantSubjectType
from app.core.schema.base import ApiSchema


class SubjectResourceGrantRequest(ApiSchema):
    subject_type: GrantSubjectType
    subject_id: str
    resource_id: str
    grant_mode: GrantMode = GrantMode.CASCADE
    effect: GrantEffect = GrantEffect.ALLOW
    description: str | None = None
    expired_at: datetime | None = None


class SubjectPermissionGrantRequest(ApiSchema):
    subject_type: GrantSubjectType
    subject_id: str
    permission_key: str
    data_scope: DataScope = DataScope.SELF
    custom_scope_dept_ids: list[str] = Field(default_factory=list)
    effect: GrantEffect = GrantEffect.ALLOW
    description: str | None = None
    reason: str | None = None
    expired_at: datetime | None = None


class SysSubjectResourceGrantRelSchema(ApiSchema):
    id: str
    subject_type: GrantSubjectType
    subject_id: str
    resource_id: str
    grant_mode: GrantMode
    effect: GrantEffect
    status: str
    description: str | None = None
    expired_at: datetime | None = None
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class SysSubjectPermissionGrantRelSchema(ApiSchema):
    id: str
    subject_type: GrantSubjectType
    subject_id: str
    permission_key: str
    data_scope: DataScope
    custom_scope_dept_ids: list[str]
    effect: GrantEffect
    status: str
    description: str | None = None
    reason: str | None = None
    expired_at: datetime | None = None
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class PermissionGrantResponse(ApiSchema):
    permission_key: str
    data_scope: DataScope
    custom_scope_dept_ids: list[str]
    source_type: GrantSubjectType | str
    source_id: str
