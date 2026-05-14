from datetime import datetime, date
from typing import Any, Dict
from pydantic import field_validator, model_serializer


class DateTimeValidatorMixin:
    @field_validator('*', mode='before')
    @classmethod
    def parse_datetime(cls, v):
        if isinstance(v, str):
            for fmt in ['%Y-%m-%d %H:%M:%S', '%Y-%m-%dT%H:%M:%S', '%Y-%m-%d']:
                try:
                    return datetime.strptime(v, fmt)
                except ValueError:
                    continue
        return v

    @model_serializer
    def serialize(self) -> Dict[str, Any]:
        result = {}
        for field_name in self.model_fields:
            value = getattr(self, field_name)
            if isinstance(value, datetime):
                result[field_name] = value.strftime('%Y-%m-%d %H:%M:%S')
            elif isinstance(value, date):
                result[field_name] = value.isoformat()
            else:
                result[field_name] = value
        return result
