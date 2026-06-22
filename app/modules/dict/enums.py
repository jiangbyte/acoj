"""字典模块枚举定义，值与 sys_dict 字典条目一致（大写+下划线体系）。"""

from enum import StrEnum


class DictGroupCategory(StrEnum):
    """字典组分类，对应 sys_dict.category 字段的取值。"""

    SYS = "SYS"
    BIZ = "BIZ"
