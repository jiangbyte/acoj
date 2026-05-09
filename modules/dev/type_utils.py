import re

from .models import GenConfig

# MySQL type pattern -> (python_type, sqlalchemy_type_template)
TYPE_MAP = [
    (r"^bigint(\((\d+)\))?.*", "int", "BigInteger"),
    (r"^int(\((\d+)\))?.*", "int", "Integer"),
    (r"^smallint(\((\d+)\))?.*", "int", "SmallInteger"),
    (r"^tinyint(\((\d+)\))?.*", "int", "SmallInteger"),
    (r"^decimal(\((\d+),(\d+)\))?.*", "float", "Numeric({len})"),
    (r"^float(\((\d+),(\d+)\))?.*", "float", "Float"),
    (r"^double(\((\d+),(\d+)\))?.*", "float", "Float"),
    (r"^varchar\((\d+)\).*", "str", "VARCHAR({len})"),
    (r"^char\((\d+)\).*", "str", "CHAR({len})"),
    (r"^text.*", "str", "Text"),
    (r"^mediumtext.*", "str", "Text"),
    (r"^longtext.*", "str", "Text"),
    (r"^datetime.*", "datetime", "DateTime"),
    (r"^timestamp.*", "datetime", "DateTime"),
    (r"^date.*", "date", "Date"),
    (r"^time.*", "time", "Time"),
    (r"^json.*", "dict", "JSON"),
    (r"^blob.*", "bytes", "LargeBinary"),
    (r"^tinyblob.*", "bytes", "LargeBinary"),
    (r"^mediumblob.*", "bytes", "LargeBinary"),
    (r"^longblob.*", "bytes", "LargeBinary"),
]


def parse_type(raw_type: str):
    raw_lower = raw_type.lower()
    for pattern, py_type, sa_type in TYPE_MAP:
        m = re.match(pattern, raw_lower)
        if m:
            length = m.group(2) if m.lastindex and m.group(2) else None
            sa_formatted = sa_type.format(len=length) if length else sa_type
            return py_type, sa_formatted
    return "str", "VARCHAR(255)"


def to_pascal_case(s: str) -> str:
    parts = re.split(r'[_\s]', s)
    return "".join(p.capitalize() for p in parts if p)


def to_snake_case(s: str) -> str:
    s = re.sub(r'(?<!^)(?=[A-Z])', '_', s).lower()
    return s


AUDIT_FIELDS = {"created_at", "updated_at", "created_by", "updated_by"}
EXCLUDED_IN_VO = {"id", "is_deleted", "created_at", "created_by", "updated_at", "updated_by"}


def gen_config_to_column(gen_config: GenConfig) -> dict:
    """Convert a GenConfig record to a column dict compatible with Jinja2 templates."""
    field_name = gen_config.field_name or ""
    raw_type = gen_config.field_type or "VARCHAR(255)"
    py_type = gen_config.field_language_type or "str"
    _, sa_type = parse_type(raw_type)

    name_lower = field_name.lower()
    is_audit = name_lower in AUDIT_FIELDS
    is_soft_delete = name_lower == "is_deleted"
    is_password = name_lower == "password"
    is_pk = gen_config.is_table_key == "YES"

    return {
        "name": field_name,
        "raw_type": raw_type,
        "python_type": py_type,
        "sa_type": sa_type,
        "nullable": True,
        "is_pk": is_pk,
        "comment": gen_config.field_remark or "",
        "is_audit": is_audit,
        "is_soft_delete": is_soft_delete,
        "is_password": is_password,
        "has_default": False,
        "default_raw": None,
    }
