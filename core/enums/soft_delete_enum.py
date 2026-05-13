class _SoftValue(str):
    """Soft-delete value string that also has a .value property (like an enum member)."""

    def __new__(cls, s=""):
        return super().__new__(cls, s)

    @property
    def value(self):
        return str(self)


class _SoftDeleteMeta(type):
    """Metaclass that lazily resolves SoftDeleteEnum.NO/.YES from settings."""

    def __getattr__(cls, name):
        if name == 'NO':
            try:
                from config.settings import settings as _s
                return _SoftValue(_s.db.soft_delete_value_not_deleted)
            except (ImportError, AttributeError, RuntimeError):
                return _SoftValue("NO")
        elif name == 'YES':
            try:
                from config.settings import settings as _s
                return _SoftValue(_s.db.soft_delete_value_deleted)
            except (ImportError, AttributeError, RuntimeError):
                return _SoftValue("YES")
        raise AttributeError(name)


class SoftDeleteEnum(metaclass=_SoftDeleteMeta):
    """Soft-delete values, dynamically read from DB__SOFT_DELETE_* settings.

    Usage:
        SoftDeleteEnum.NO   → configured "not deleted" value (e.g. 'NO', '0', 'FALSE')
        SoftDeleteEnum.YES  → configured "deleted" value (e.g. 'YES', '1', 'TRUE')
        SoftDeleteEnum.NO.value  → same raw string (for model/param defaults)

    Lazily resolves via __getattr__ at access time, so it works even
    during the circular import between settings.py and this module.
    """
