from enum import Enum


class PageDataField(str, Enum):
    RECORDS = "records"
    TOTAL = "total"
    PAGE = "page"
    SIZE = "size"
    PAGES = "pages"
