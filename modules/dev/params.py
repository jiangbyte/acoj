from typing import Optional, List
from pydantic import BaseModel, ConfigDict
from core.pojo import PageBounds


# ========== GenBasic 参数 ==========

class GenBasicVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: Optional[str] = None
    db_table: str
    db_table_key: str
    module_name: str
    table_prefix: Optional[str] = None
    generate_type: Optional[str] = "PRO"
    module: Optional[str] = None
    menu_pid: Optional[str] = None
    class_name: str
    form_layout: Optional[str] = "col"
    grid_whether: Optional[str] = "NO"
    package_name: Optional[str] = None
    author_name: Optional[str] = None
    gen_type: Optional[str] = "TABLE"
    tree_parent_field: Optional[str] = None
    tree_name_field: Optional[str] = None
    sub_db_table: Optional[str] = None
    sub_db_table_key: Optional[str] = None
    sub_foreign_key: Optional[str] = None
    sub_class_name: Optional[str] = None
    sub_function_name: Optional[str] = None
    sub_bus_name: Optional[str] = None
    sort_code: Optional[int] = None


class GenBasicPageParam(PageBounds):
    sort_field: Optional[str] = None
    sort_order: Optional[str] = None


class GenBasicIdParam(BaseModel):
    id: str


class GenBasicTableColumnParam(BaseModel):
    table_name: str


class GenBasicSelectorMenuParam(BaseModel):
    module: str


# ========== GenConfig 参数 ==========

class GenConfigVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: Optional[str] = None
    basic_id: Optional[str] = None
    is_table_key: str = "NO"
    field_name: str
    field_remark: str
    field_type: str
    field_language_type: Optional[str] = "str"
    effect_type: str = "input"
    dict_type_code: Optional[str] = None
    whether_table: str = "YES"
    whether_retract: str = "NO"
    whether_add_update: str = "YES"
    whether_required: str = "NO"
    whether_unique: str = "NO"
    query_whether: str = "NO"
    query_type: Optional[str] = None
    table_type: Optional[str] = "MAIN"
    sort_code: Optional[int] = None


class GenConfigListParam(BaseModel):
    basic_id: str
    table_type: Optional[str] = None
    sort_field: Optional[str] = None
    sort_order: Optional[str] = None


class GenConfigIdParam(BaseModel):
    id: str


# ========== 结果 ==========

class GenBasicTableResult(BaseModel):
    table_name: str
    table_remark: str


class GenBasicTableColumnResult(BaseModel):
    column_name: str
    type_name: str
    column_remark: str


class GenBasicPreviewResult(BaseModel):
    class GenBasicCodeResult(BaseModel):
        code_file_name: str
        code_file_with_path_name: str
        code_file_content: str

    gen_basic_code_sql_result_list: List[GenBasicCodeResult] = []
    gen_basic_code_frontend_result_list: List[GenBasicCodeResult] = []
    gen_basic_code_backend_result_list: List[GenBasicCodeResult] = []


