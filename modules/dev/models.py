from typing import Optional
import datetime

from sqlalchemy import DateTime, Integer, text
from sqlalchemy.dialects.mysql import VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class GenBasic(Base):
    __tablename__ = 'gen_basic'
    __table_args__ = {'comment': '代码生成基础'}

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    db_table: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='主表名称')
    db_table_key: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='主表主键')
    module_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='模块名')
    table_prefix: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='移除表前缀')
    generate_type: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='生成方式')
    module: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='所属模块')
    menu_pid: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='上级目录')
    class_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='类名')
    form_layout: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='表单布局')
    grid_whether: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='使用栅格')
    package_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='包名')
    author_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='作者')
    gen_type: Mapped[Optional[str]] = mapped_column(VARCHAR(50, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'TABLE'"), comment='生成类型')
    tree_parent_field: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='树父级字段')
    tree_name_field: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='树显示名称字段')
    sub_db_table: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='子表名称')
    sub_db_table_key: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='子表主键')
    sub_foreign_key: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='子表外键')
    sub_class_name: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='子表类名')
    sub_function_name: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='子表功能名')
    sub_bus_name: Mapped[Optional[str]] = mapped_column(VARCHAR(200, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='子表业务名')
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, comment='排序')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')


class GenConfig(Base):
    __tablename__ = 'gen_config'
    __table_args__ = {'comment': '代码生成配置'}

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    basic_id: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='基础ID')
    is_table_key: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='是否主键')
    field_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字段名')
    field_remark: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字段注释')
    field_type: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='数据库类型')
    field_language_type: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='语言数据类型')
    effect_type: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='作用类型')
    dict_type_code: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字典编码')
    whether_table: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'YES'"), comment='列表显示')
    whether_retract: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='列省略')
    whether_add_update: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'YES'"), comment='是否增改')
    whether_required: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='必填')
    whether_unique: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='唯一')
    query_whether: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='是否查询')
    query_type: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='查询方式')
    table_type: Mapped[Optional[str]] = mapped_column(VARCHAR(20, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'MAIN'"), comment='所属表类型')
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, comment='排序')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')
