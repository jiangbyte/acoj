from enum import Enum


class ExportTypeEnum(str, Enum):
    CURRENT = "current"
    SELECTED = "selected"
    ALL = "all"
    
    @property
    def desc(self) -> str:
        descriptions = {
            ExportTypeEnum.CURRENT: "本页",
            ExportTypeEnum.SELECTED: "选中数据",
            ExportTypeEnum.ALL: "全部"
        }
        return descriptions.get(self, "")
