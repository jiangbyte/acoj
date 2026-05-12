import os
import logging
from pathlib import Path
from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
from fastapi import Request
from jinja2 import Environment, FileSystemLoader, StrictUndefined

from .models import GenBasic, GenConfig
from .params import (
    GenBasicVO, GenBasicPageParam,
    GenBasicIdParam, GenBasicTableColumnParam,
    GenConfigListParam, GenConfigIdParam,
    GenBasicTableResult, GenBasicTableColumnResult, GenBasicPreviewResult,
)
from .dao import GenBasicDao
from .gen_config_service import GenConfigService
from .type_utils import parse_type, to_snake_case, to_pascal_case, gen_config_to_column, EXCLUDED_IN_VO
from .gen_category import GenCategoryEnum
from core.result import page_data
from core.exception import BusinessException
from core.auth import HeiAuthTool
from core.utils import strip_system_fields, apply_update

logger = logging.getLogger(__name__)

TEMPLATE_DIR = Path(__file__).parent / "templates"


class GenBasicService:
    def __init__(self, db: Session):
        self.dao = GenBasicDao(db)
        self.gen_config_service = GenConfigService(db)
        self.db = db
        self.jinja_env = Environment(
            loader=FileSystemLoader(str(TEMPLATE_DIR)),
            undefined=StrictUndefined,
            trim_blocks=True,
            lstrip_blocks=True,
            keep_trailing_newline=True,
        )

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: GenBasicPageParam) -> dict:
        result = self.dao.find_page(param)
        records = []
        for r in result["records"]:
            records.append({
                "id": r.id,
                "db_table": r.db_table,
                "db_table_key": r.db_table_key,
                "module_name": r.module_name,
                "table_prefix": r.table_prefix,
                "generate_type": r.generate_type,
                "module": r.module,
                "menu_pid": r.menu_pid,
                "class_name": r.class_name,
                "gen_type": r.gen_type,
                "author_name": r.author_name,
                "sort_code": r.sort_code,
                "is_deleted": r.is_deleted,
                "created_at": r.created_at,
                "created_by": r.created_by,
                "updated_at": r.updated_at,
                "updated_by": r.updated_by,
            })
        return page_data(
            records=records,
            total=result["total"],
            page=param.current,
            size=param.size,
        )

    async def create(self, vo: GenBasicVO, request: Optional[Request] = None) -> dict:
        created_by = await self._get_current_user_id(request)
        entity = GenBasic(**strip_system_fields(vo.model_dump()))
        entity.created_by = created_by
        self.dao.insert(entity)
        self._add_gen_config(entity)
        return {"id": entity.id}

    async def modify(self, vo: GenBasicVO, request: Optional[Request] = None) -> None:
        updated_by = await self._get_current_user_id(request)
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        if vo.db_table != entity.db_table:
            self.gen_config_service.delete_by_basic_id(vo.id)
            update_data = vo.model_dump(exclude_unset=True)
            apply_update(entity, update_data)
            entity.updated_by = updated_by
            self.dao.update(entity)
            self._add_gen_config(entity)
        elif (vo.gen_type and GenCategoryEnum.is_dual_table(vo.gen_type) and
              vo.sub_db_table != entity.sub_db_table):
            for cfg in self.gen_config_service.list(GenConfigListParam(basic_id=vo.id, table_type="SUB")):
                self.gen_config_service.delete(GenConfigIdParam(id=cfg["id"]))
            update_data = vo.model_dump(exclude_unset=True)
            apply_update(entity, update_data)
            entity.updated_by = updated_by
            self.dao.update(entity)
            self._add_sub_gen_config(entity)
        else:
            update_data = vo.model_dump(exclude_unset=True)
            apply_update(entity, update_data)
            entity.updated_by = updated_by
            self.dao.update(entity)

    def delete(self, param_list: List[GenBasicIdParam]) -> None:
        ids = [p.id for p in param_list]
        if not ids:
            return
        self.gen_config_service.delete_by_basic_ids(ids)
        self.dao.delete_by_ids(ids)

    def detail(self, param: GenBasicIdParam) -> Optional[dict]:
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        return {
            "id": entity.id,
            "db_table": entity.db_table,
            "db_table_key": entity.db_table_key,
            "module_name": entity.module_name,
            "table_prefix": entity.table_prefix,
            "generate_type": entity.generate_type,
            "module": entity.module,
            "menu_pid": entity.menu_pid,
            "class_name": entity.class_name,
            "form_layout": entity.form_layout,
            "grid_whether": entity.grid_whether,
            "package_name": entity.package_name,
            "author_name": entity.author_name,
            "gen_type": entity.gen_type,
            "tree_parent_field": entity.tree_parent_field,
            "tree_name_field": entity.tree_name_field,
            "sub_db_table": entity.sub_db_table,
            "sub_db_table_key": entity.sub_db_table_key,
            "sub_foreign_key": entity.sub_foreign_key,
            "sub_class_name": entity.sub_class_name,
            "sub_function_name": entity.sub_function_name,
            "sub_bus_name": entity.sub_bus_name,
            "sort_code": entity.sort_code,
            "is_deleted": entity.is_deleted,
            "created_at": entity.created_at,
            "created_by": entity.created_by,
            "updated_at": entity.updated_at,
            "updated_by": entity.updated_by,
        }

    # ---- Database inspection ----

    def tables(self) -> List[GenBasicTableResult]:
        try:
            result = self.db.execute(
                text("SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) "
                     "ORDER BY TABLE_NAME")
            ).fetchall()
            return [
                GenBasicTableResult(
                    table_name=row[0],
                    table_remark=row[1] or row[0],
                )
                for row in result
            ]
        except SQLAlchemyError as e:
            logger.error(f"Failed to fetch tables: {e}")
            raise BusinessException("获取数据库表失败")

    def table_columns(self, param: GenBasicTableColumnParam) -> List[GenBasicTableColumnResult]:
        try:
            result = self.db.execute(
                text("SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT "
                     "FROM information_schema.COLUMNS "
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = :t "
                     "ORDER BY ORDINAL_POSITION"),
                {"t": param.table_name},
            ).fetchall()
            return [
                GenBasicTableColumnResult(
                    column_name=row[0],
                    type_name=row[1].upper(),
                    column_remark=row[2] or row[0],
                )
                for row in result
            ]
        except SQLAlchemyError as e:
            logger.error(f"Failed to fetch columns for {param.table_name}: {e}")
            raise BusinessException(f"获取数据库表字段失败，表名称：{param.table_name}")

    # ---- Auto-generate gen_config from table ----

    def _add_gen_config(self, gen_basic: GenBasic) -> None:
        try:
            columns = self.db.execute(
                text("SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT "
                     "FROM information_schema.COLUMNS "
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = :t "
                     "ORDER BY ORDINAL_POSITION"),
                {"t": gen_basic.db_table},
            ).fetchall()
        except SQLAlchemyError as e:
            logger.error(f"Failed to inspect table {gen_basic.db_table}: {e}")
            raise BusinessException(f"表 {gen_basic.db_table} 不存在或无法访问")

        db_table_key_lower = (gen_basic.db_table_key or "id").lower()
        for i, row in enumerate(columns):
            col_name = row[0]
            col_type = row[1].upper()
            col_comment = row[2] or col_name
            py_type, _ = parse_type(col_type)

            is_pk = col_name.lower() == db_table_key_lower
            is_system = col_name.lower() in ("is_deleted", "created_at", "created_by", "updated_at", "updated_by")

            cfg = GenConfig()
            cfg.basic_id = gen_basic.id
            cfg.is_table_key = "YES" if is_pk else "NO"
            cfg.field_name = col_name
            cfg.field_remark = col_comment
            cfg.field_type = col_type
            cfg.field_language_type = py_type
            cfg.effect_type = "input"
            cfg.sort_code = i
            cfg.table_type = "MAIN"
            if is_pk or is_system:
                cfg.whether_table = "NO"
                cfg.whether_add_update = "NO"
            else:
                cfg.whether_table = "YES"
                cfg.whether_add_update = "YES"
            cfg.whether_retract = "NO"
            cfg.whether_required = "NO"
            cfg.whether_unique = "NO"
            cfg.query_whether = "NO"
            self.gen_config_service.dao.insert(cfg)

        if gen_basic.gen_type and GenCategoryEnum.is_dual_table(gen_basic.gen_type):
            self._add_sub_gen_config(gen_basic)

    def _add_sub_gen_config(self, gen_basic: GenBasic) -> None:
        if not gen_basic.sub_db_table:
            return
        try:
            columns = self.db.execute(
                text("SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT "
                     "FROM information_schema.COLUMNS "
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = :t "
                     "ORDER BY ORDINAL_POSITION"),
                {"t": gen_basic.sub_db_table},
            ).fetchall()
        except SQLAlchemyError:
            logger.warning(f"Sub table {gen_basic.sub_db_table} not found, skipping")
            return

        sub_key_lower = (gen_basic.sub_db_table_key or "id").lower()
        for j, row in enumerate(columns):
            col_name = row[0]
            col_type = row[1].upper()
            col_comment = row[2] or col_name
            py_type, _ = parse_type(col_type)

            is_pk = col_name.lower() == sub_key_lower
            is_system = col_name.lower() in ("is_deleted", "created_at", "created_by", "updated_at", "updated_by")

            cfg = GenConfig()
            cfg.basic_id = gen_basic.id
            cfg.is_table_key = "YES" if is_pk else "NO"
            cfg.field_name = col_name
            cfg.field_remark = col_comment
            cfg.field_type = col_type
            cfg.field_language_type = py_type
            cfg.effect_type = "input"
            cfg.sort_code = j
            cfg.table_type = "SUB"
            if is_pk or is_system:
                cfg.whether_table = "NO"
                cfg.whether_add_update = "NO"
            else:
                cfg.whether_table = "YES"
                cfg.whether_add_update = "YES"
            cfg.whether_retract = "NO"
            cfg.whether_required = "NO"
            cfg.whether_unique = "NO"
            cfg.query_whether = "NO"
            self.gen_config_service.dao.insert(cfg)

    # ---- Code Generation ----

    def _build_context(self, gen_basic: GenBasic) -> dict:
        configs = self.gen_config_service.list(GenConfigListParam(basic_id=gen_basic.id, table_type="MAIN"))
        columns = [gen_config_to_column(GenConfig(**c)) for c in configs]
        if not columns:
            raise BusinessException("请先配置字段信息")

        entity_name = to_snake_case(gen_basic.class_name or gen_basic.module_name or "")
        EntityName = to_pascal_case(entity_name)
        module_path = f"{gen_basic.module}/{gen_basic.module_name}" if gen_basic.module else gen_basic.module_name or ""
        route_prefix = module_path.replace("\\", "/")
        table_name = gen_basic.db_table or ""

        has_date = any(c["python_type"] == "date" for c in columns)
        has_datetime = any(c["python_type"] == "datetime" for c in columns)
        has_password = any(c["is_password"] for c in columns)
        has_last_login_at = any(c["name"].lower() == "last_login_at" for c in columns)

        return {
            "table_name": table_name,
            "entity_name": entity_name,
            "EntityName": EntityName,
            "entity_name_upper": entity_name.upper(),
            "module_path": module_path,
            "route_prefix": route_prefix,
            "columns": columns,
            "model_fields": columns,
            "vo_business_fields": [c for c in columns if c["name"].lower() not in EXCLUDED_IN_VO],
            "has_date": has_date,
            "has_datetime": has_datetime,
            "has_password": has_password,
            "has_last_login_at": has_last_login_at,
            "excluded_in_vo": EXCLUDED_IN_VO,
        }

    def _render(self, template_name: str, context: dict) -> str:
        template = self.jinja_env.get_template(template_name)
        return template.render(**context)

    def _write_file(self, filepath: str, content: str):
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        logger.info(f"Generated: {filepath}")

    def preview_gen(self, param: GenBasicIdParam) -> GenBasicPreviewResult:
        gen_basic = self.dao.find_by_id(param.id)
        if not gen_basic:
            raise BusinessException("代码生成基础不存在")
        ctx = self._build_context(gen_basic)

        module_name = self._sanitize_module_name(gen_basic.module_name or "")
        base_dir = Path(__file__).parent.parent.parent / "modules" / module_name
        files = [
            ("model.py.jinja2", base_dir / "models.py"),
            ("params.py.jinja2", base_dir / "params.py"),
            ("dao.py.jinja2", base_dir / "dao.py"),
            ("service.py.jinja2", base_dir / "service.py"),
            ("api.py.jinja2", base_dir / "api" / "v1" / "api.py"),
            ("module___init__.py.jinja2", base_dir / "__init__.py"),
            ("api___init__.py.jinja2", base_dir / "api" / "__init__.py"),
            ("api_v1___init__.py.jinja2", base_dir / "api" / "v1" / "__init__.py"),
        ]

        backend_results = []
        for tmpl, outpath in files:
            content = self._render(tmpl, ctx)
            rel_path = str(outpath.relative_to(Path(__file__).parent.parent.parent))
            code_result = GenBasicPreviewResult.GenBasicCodeResult(
                code_file_name=outpath.name,
                code_file_with_path_name=rel_path,
                code_file_content=content,
            )
            backend_results.append(code_result)

        return GenBasicPreviewResult(
            gen_basic_code_backend_result_list=backend_results,
        )

    def _sanitize_module_name(self, name: str) -> str:
        """Prevent path traversal by ensuring module_name stays within modules/."""
        safe = name.replace("\\", "/")
        if ".." in safe.split("/") or safe.startswith("/"):
            raise BusinessException("非法模块名称")
        return safe

    def exec_gen_pro(self, param: GenBasicIdParam) -> None:
        gen_basic = self.dao.find_by_id(param.id)
        if not gen_basic:
            raise BusinessException("代码生成基础不存在")
        ctx = self._build_context(gen_basic)

        module_name = self._sanitize_module_name(gen_basic.module_name or "")
        base_dir = Path(__file__).parent.parent.parent / "modules" / module_name
        files = [
            ("model.py.jinja2", base_dir / "models.py"),
            ("params.py.jinja2", base_dir / "params.py"),
            ("dao.py.jinja2", base_dir / "dao.py"),
            ("service.py.jinja2", base_dir / "service.py"),
            ("api.py.jinja2", base_dir / "api" / "v1" / "api.py"),
            ("module___init__.py.jinja2", base_dir / "__init__.py"),
            ("api___init__.py.jinja2", base_dir / "api" / "__init__.py"),
            ("api_v1___init__.py.jinja2", base_dir / "api" / "v1" / "__init__.py"),
        ]

        for tmpl, outpath in files:
            content = self._render(tmpl, ctx)
            self._write_file(str(outpath), content)
