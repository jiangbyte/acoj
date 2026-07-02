from pydantic import Field

from app.core.config.enums import AccountType
from app.core.schema.base import ApiSchema


class AccountRef(ApiSchema):
    account_type: AccountType
    account_id: str = Field(min_length=1, max_length=64)
