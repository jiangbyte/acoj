"""
Code generation scaffolding tool for hei-fastapi plugins.

Usage:

    python -m cli.codegen list
    python -m cli.codegen scaffold plugin_demo
    python -m cli.codegen add-module plugin_demo article
"""

from __future__ import annotations

import argparse
import ast
import logging
import re
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger("codegen")

REPO_ROOT = Path(__file__).resolve().parent.parent
PLUGINS_DIR = REPO_ROOT / "plugins"
PLUGIN_NAME_RE = re.compile(r"^plugin_[a-z][a-z0-9_]*$")
MODULE_NAME_RE = re.compile(r"^[a-z][a-z0-9_]*$")


@dataclass(frozen=True)
class PluginNames:
    name: str
    package: str
    short: str
    pascal: str


@dataclass(frozen=True)
class ModuleNames:
    name: str
    pascal: str
    snake: str
    table: str
    route: str
    service_var: str
    service_getter: str
    repository_cls: str
    service_cls: str
    model_cls: str
    vo_cls: str
    create_param_cls: str
    modify_param_cls: str
    page_param_cls: str


def normalize_plugin_name(name: str) -> str:
    value = name.strip().replace("-", "_")
    if not value.startswith("plugin_"):
        value = f"plugin_{value}"
    if not PLUGIN_NAME_RE.match(value):
        raise ValueError("plugin name must match plugin_xxx, using lowercase letters, digits and underscores")
    return value


def normalize_module_name(name: str) -> str:
    value = name.strip().replace("-", "_")
    if not MODULE_NAME_RE.match(value):
        raise ValueError("module name must use lowercase letters, digits and underscores")
    return value


def pascal_case(value: str) -> str:
    return "".join(part.capitalize() for part in value.split("_") if part) or "Demo"


def plugin_names(name: str) -> PluginNames:
    normalized = normalize_plugin_name(name)
    short = normalized.removeprefix("plugin_")
    return PluginNames(name=normalized, package=normalized, short=short, pascal=f"{pascal_case(short)}Plugin")


def module_names(plugin: PluginNames, name: str) -> ModuleNames:
    normalized = normalize_module_name(name)
    pascal = pascal_case(normalized)
    return ModuleNames(
        name=normalized,
        pascal=pascal,
        snake=normalized,
        table=f"{plugin.short}_{normalized}",
        route=f"{plugin.short}/{normalized}",
        service_var=f"{normalized}_service",
        service_getter=f"get_{normalized}_service",
        repository_cls=f"{pascal}Repository",
        service_cls=f"{pascal}Service",
        model_cls=pascal,
        vo_cls=f"{pascal}VO",
        create_param_cls=f"{pascal}CreateParam",
        modify_param_cls=f"{pascal}ModifyParam",
        page_param_cls=f"{pascal}PageParam",
    )


def write_file(path: Path, content: str, *, force: bool = False) -> None:
    if path.exists() and not force:
        raise FileExistsError(f"file already exists: {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.lstrip("\n"), encoding="utf-8")


def format_python(paths: list[Path]) -> None:
    black = shutil.which("black")
    if black:
        subprocess.run([black, "--quiet", *map(str, paths)], cwd=REPO_ROOT, check=False)


def cmd_list() -> None:
    if not PLUGINS_DIR.exists():
        logger.info("No plugins directory found.")
        return
    logger.info("Discovered plugins:")
    for entry in sorted(PLUGINS_DIR.iterdir(), key=lambda item: item.name):
        if not entry.is_dir() or entry.name.startswith("_") or entry.name == "__pycache__":
            continue
        status = "[plugin.py]" if (entry / "plugin.py").exists() else "[no plugin.py]"
        modules = sorted(
            item.name
            for item in entry.iterdir()
            if item.is_dir() and (item / "models.py").exists() and (item / "api" / "v1" / "api.py").exists()
        )
        suffix = f" modules={','.join(modules)}" if modules else ""
        logger.info("  %s  %s%s", entry.name, status, suffix)


def cmd_scaffold(name: str, *, module: str = "demo") -> None:
    plugin = plugin_names(name)
    module_info = module_names(plugin, module)
    target = PLUGINS_DIR / plugin.name
    if target.exists():
        raise FileExistsError(f"plugin already exists: {target}")

    created = create_plugin_files(target, plugin, module_info)
    created.extend(create_module_files(target, plugin, module_info))
    update_plugin_assembly(target, plugin, module_info)
    update_plugin_migrate(target, plugin, module_info)
    format_python(created + [target / "assembly.py", target / "migrate.py", target / "plugin.py"])

    logger.info("Created plugin scaffold: %s", plugin.name)
    for path in sorted(created):
        logger.info("  %s", path.relative_to(PLUGINS_DIR))


def cmd_add_module(plugin_name: str, module_name: str) -> None:
    plugin = plugin_names(plugin_name)
    module_info = module_names(plugin, module_name)
    target = PLUGINS_DIR / plugin.name
    if not target.is_dir():
        raise FileNotFoundError(f"plugin not found: {target}")
    if not (target / "assembly.py").is_file():
        raise FileNotFoundError(f"assembly.py not found in {target}")
    if not (target / "migrate.py").is_file():
        raise FileNotFoundError(f"migrate.py not found in {target}")

    created = create_module_files(target, plugin, module_info)
    update_plugin_assembly(target, plugin, module_info)
    update_plugin_migrate(target, plugin, module_info)
    format_python(created + [target / "assembly.py", target / "migrate.py"])

    logger.info("Added module %s to %s", module_info.name, plugin.name)
    for path in sorted(created):
        logger.info("  %s", path.relative_to(PLUGINS_DIR))


def create_plugin_files(target: Path, plugin: PluginNames, module_info: ModuleNames) -> list[Path]:
    files = {
        "__init__.py": plugin_init_template(plugin),
        "plugin.py": plugin_py_template(plugin, module_info),
        "assembly.py": assembly_template(plugin, module_info),
        "migrate.py": migrate_template(plugin, module_info),
    }
    created: list[Path] = []
    for rel_path, content in files.items():
        path = target / rel_path
        write_file(path, content)
        created.append(path)
    return created


def create_module_files(target: Path, plugin: PluginNames, module_info: ModuleNames) -> list[Path]:
    module_dir = target / module_info.name
    if module_dir.exists():
        raise FileExistsError(f"module already exists: {module_dir}")

    files = {
        "__init__.py": module_init_template(module_info),
        "models.py": model_template(module_info),
        "params.py": params_template(module_info),
        "repository.py": repository_template(module_info),
        "service.py": service_template(module_info),
        "api/__init__.py": api_init_template(),
        "api/v1/__init__.py": api_v1_init_template(),
        "api/v1/api.py": api_template(plugin, module_info),
    }
    created: list[Path] = []
    for rel_path, content in files.items():
        path = module_dir / rel_path
        write_file(path, content)
        created.append(path)
    return created


def update_plugin_assembly(plugin_dir: Path, plugin: PluginNames, module_info: ModuleNames) -> None:
    path = plugin_dir / "assembly.py"
    if not path.exists():
        write_file(path, assembly_template(plugin, module_info))
        return

    module_alias = f"{module_info.snake}_router"
    import_line = f"from .{module_info.name} import router as {module_alias}"
    routers_entry = f"    {module_alias},"
    content = path.read_text(encoding="utf-8")

    if import_line not in content:
        content = add_import_after_last_from(content, import_line)

    if routers_entry not in content:
        lines = content.splitlines()
        idx = len(lines) - 1
        for i in range(len(lines) - 1, -1, -1):
            if lines[i].strip().startswith("#") or lines[i].strip() == "":
                continue
            if lines[i].strip() == ")":
                idx = i
                break
        else:
            idx = len(lines)
        lines.insert(idx, routers_entry)
        content = "\n".join(lines) + "\n"

    path.write_text(content, encoding="utf-8")


def update_plugin_migrate(plugin_dir: Path, plugin: PluginNames, module_info: ModuleNames) -> None:
    path = plugin_dir / "migrate.py"
    if not path.exists():
        write_file(path, migrate_template(plugin, module_info))
        return

    import_line = f"from .{module_info.name}.models import {module_info.model_cls}"
    call_line = f"    register_model({module_info.model_cls})"
    content = path.read_text(encoding="utf-8")
    if import_line not in content:
        content = add_import_after_last_from(content, import_line)
    if call_line not in content:
        content = insert_function_line(content, "register_all_models", call_line)
    path.write_text(content, encoding="utf-8")


def add_import_after_last_from(content: str, import_line: str) -> str:
    lines = content.splitlines()
    insert_at = 0
    for idx, line in enumerate(lines):
        if line.startswith("from ") or line.startswith("import "):
            insert_at = idx + 1
    lines.insert(insert_at, import_line)
    return "\n".join(lines) + "\n"




def insert_function_line(content: str, function_name: str, line_to_insert: str) -> str:
    lines = content.splitlines()
    try:
        tree = ast.parse(content)
    except SyntaxError as exc:
        raise RuntimeError(f"cannot parse migrate.py: {exc}") from exc

    fn = next((node for node in tree.body if isinstance(node, ast.FunctionDef) and node.name == function_name), None)
    if fn is None:
        raise RuntimeError(f"cannot find {function_name}() in migrate.py")
    insert_at = fn.end_lineno or len(lines)
    lines.insert(insert_at, line_to_insert)
    return "\n".join(lines) + "\n"


def plugin_init_template(plugin: PluginNames) -> str:
    return f'''
"""
{plugin.name} plugin.
"""

from .assembly import register

__all__ = ["register"]
'''


_PLUGIN_PY_BOILERPLATE = """from __future__ import annotations

import logging

from sdk.kernel.plugin import HeiPlugin, PluginInfo

from .migrate import register_all_models
from .assembly import ROUTERS

logger = logging.getLogger(__name__)


class {pascal}(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name="{name}", version="1.0.0", description="{short} plugin")

    @classmethod
    def routers(cls) -> tuple:
        return ROUTERS

    def on_init(self) -> None:
        register_all_models()
        logger.info("[{pascal}] Models registered")
"""


def plugin_py_template(plugin: PluginNames, module_info: ModuleNames) -> str:
    return _PLUGIN_PY_BOILERPLATE.format(
        pascal=plugin.pascal,
        name=plugin.name,
        short=plugin.short,
    )


def assembly_template(plugin: PluginNames, module_info: ModuleNames) -> str:
    return f'''
from __future__ import annotations

from .{module_info.name} import router as {module_info.snake}_router

ROUTERS = (
    {module_info.snake}_router,
)
'''


def migrate_template(plugin: PluginNames, module_info: ModuleNames) -> str:
    return f'''
from __future__ import annotations

from sdk.infra.db import register_model

from .{module_info.name}.models import {module_info.model_cls}


def register_all_models() -> None:
    register_model({module_info.model_cls})
'''


def module_init_template(module_info: ModuleNames) -> str:
    return f'''
from .api import v1_router as router
from .models import {module_info.model_cls}
from .params import {module_info.create_param_cls}, {module_info.modify_param_cls}, {module_info.page_param_cls}, {module_info.vo_cls}
from .repository import {module_info.repository_cls}
from .service import {module_info.service_cls}, {module_info.service_getter}

__all__ = [
    "router",
    "{module_info.model_cls}",
    "{module_info.vo_cls}",
    "{module_info.create_param_cls}",
    "{module_info.modify_param_cls}",
    "{module_info.page_param_cls}",
    "{module_info.repository_cls}",
    "{module_info.service_cls}",
    "{module_info.service_getter}",
]
'''


def model_template(module_info: ModuleNames) -> str:
    return f'''
from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import DateTime, Index, Integer, String, text
from sqlalchemy.orm import Mapped, mapped_column

from sdk.kernel.registry import HeiBase


class {module_info.model_cls}(HeiBase):
    __tablename__ = "{module_info.table}"
    __table_args__ = (Index("idx_{module_info.table}_name", "name"), {{"comment": "{module_info.pascal}"}})

    id: Mapped[str] = mapped_column(String(32), primary_key=True, comment="主键")
    name: Mapped[str] = mapped_column(String(128), default="", server_default=text("''"), comment="名称")
    status: Mapped[str] = mapped_column(String(16), default="ENABLED", server_default=text("'ENABLED'"), comment="状态")
    sort_code: Mapped[int] = mapped_column(Integer, default=0, server_default=text("0"), comment="排序")
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, comment="创建时间")
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, comment="更新时间")
'''


def params_template(module_info: ModuleNames) -> str:
    return f'''
from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict


class {module_info.vo_cls}(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    name: str = ""
    status: str = "ENABLED"
    sort_code: int = 0
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class {module_info.create_param_cls}(BaseModel):
    name: str
    status: str = "ENABLED"
    sort_code: int = 0


class {module_info.modify_param_cls}(BaseModel):
    id: str
    name: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = None


class {module_info.page_param_cls}(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    status: Optional[str] = None
'''


def repository_template(module_info: ModuleNames) -> str:
    return f'''
from __future__ import annotations

from datetime import datetime
from typing import Any, Optional

from sqlalchemy import delete as sa_delete
from sqlalchemy import func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from .models import {module_info.model_cls}
from .params import {module_info.page_param_cls}


class {module_info.repository_cls}:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def find_by_id(self, id: str) -> Optional[{module_info.model_cls}]:
        return (await self.db.execute(
            select({module_info.model_cls}).where({module_info.model_cls}.id == id)
        )).scalar_one_or_none()

    async def find_page(self, param: {module_info.page_param_cls}) -> dict[str, Any]:
        filters = []
        if param.keyword:
            keyword = f"%{{param.keyword}}%"
            filters.append(or_({module_info.model_cls}.name.ilike(keyword)))
        if param.status:
            filters.append({module_info.model_cls}.status == param.status)

        offset = (max(1, param.current) - 1) * min(max(1, param.size), 100)
        size = min(max(1, param.size), 100)

        stmt = (
            select({module_info.model_cls})
            .where(*filters)
            .order_by({module_info.model_cls}.created_at.desc())
            .offset(offset)
            .limit(size)
        )
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = (await self.db.execute(count_stmt)).scalar() or 0
        records = list(
            (await self.db.execute(stmt)).scalars().all()
        )
        return {{"records": records, "total": total}}

    async def insert(self, entity: {module_info.model_cls}) -> {module_info.model_cls}:
        now = datetime.now()
        entity.created_at = entity.created_at or now
        entity.updated_at = now
        self.db.add(entity)
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def update(self, entity: {module_info.model_cls}) -> {module_info.model_cls}:
        entity.updated_at = datetime.now()
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def delete_by_ids(self, ids: list[str]) -> int:
        if not ids:
            return 0
        affected = (await self.db.execute(
            sa_delete({module_info.model_cls}).where({module_info.model_cls}.id.in_(ids))
        )).rowcount
        await self.db.commit()
        return affected
'''


def service_template(module_info: ModuleNames) -> str:
    return f'''
from __future__ import annotations

from datetime import datetime

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import update as sa_update

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.shared.types import IdsParam
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import {module_info.model_cls}
from .params import {module_info.create_param_cls}, {module_info.modify_param_cls}, {module_info.page_param_cls}, {module_info.vo_cls}
from .repository import {module_info.repository_cls}


class {module_info.service_cls}:
    def __init__(self, repository_or_db):
        self.repository = (
            repository_or_db
            if isinstance(repository_or_db, {module_info.repository_cls})
            else {module_info.repository_cls}(repository_or_db)
        )
        self.db = self.repository.db

    async def page(self, param: {module_info.page_param_cls}) -> dict:
        result = await self.repository.find_page(param)
        return map_page_data(result, {module_info.vo_cls}.model_validate, param.current, param.size)

    async def detail(self, id: str) -> {module_info.vo_cls} | None:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return {module_info.vo_cls}.model_validate(entity)

    async def create(self, param: {module_info.create_param_cls}, actor: ActorContext | None = None) -> None:
        entity = {module_info.model_cls}(
            id=generate_id(),
            name=param.name,
            status=param.status,
            sort_code=param.sort_code,
        )
        await self.repository.insert(entity)

    async def modify(self, param: {module_info.modify_param_cls}, actor: ActorContext | None = None) -> None:
        entity = await self.repository.find_by_id(param.id)
        if entity is None:
            raise BusinessException("数据不存在")
        updates = {{}}
        for field in ("name", "status", "sort_code"):
            value = getattr(param, field, None)
            if value is not None:
                updates[field] = value
        if updates:
            updates["updated_at"] = datetime.now()
            await self.db.execute(
                sa_update({module_info.model_cls}).where({module_info.model_cls}.id == param.id).values(**updates)
            )
            await self.db.commit()

    async def remove(self, param: IdsParam) -> None:
        if not param.ids:
            return
        await self.repository.delete_by_ids(param.ids)


def {module_info.service_getter}(db: AsyncSession = Depends(get_db)) -> {module_info.service_cls}:
    return {module_info.service_cls}(db)
'''


def api_init_template() -> str:
    return 'from .v1 import router as v1_router\n\n__all__ = ["v1_router"]\n'


def api_v1_init_template() -> str:
    return 'from .api import router\n\n__all__ = ["router"]\n'


def api_template(plugin: PluginNames, module_info: ModuleNames) -> str:
    return f'''
from fastapi import APIRouter, Depends, Query

from sdk.auth.decorator import CheckLogin
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.web.result import PageData, Result, success

from ...params import {module_info.create_param_cls}, {module_info.modify_param_cls}, {module_info.page_param_cls}, {module_info.vo_cls}
from ...service import {module_info.service_cls}, {module_info.service_getter}

router = APIRouter()


@router.get("/api/v1/{module_info.route}/page", summary="{module_info.pascal}分页", response_model=Result[PageData[{module_info.vo_cls}]])
@CheckLogin
async def page(param: {module_info.page_param_cls} = Depends(), service: {module_info.service_cls} = Depends({module_info.service_getter})):
    return success(await service.page(param))


@router.post("/api/v1/{module_info.route}/create", summary="创建{module_info.pascal}", response_model=Result[{module_info.vo_cls}])
@CheckLogin
async def create(
    param: {module_info.create_param_cls},
    service: {module_info.service_cls} = Depends({module_info.service_getter}),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(param, actor)
    return success()


@router.post("/api/v1/{module_info.route}/modify", summary="修改{module_info.pascal}", response_model=Result[{module_info.vo_cls}])
@CheckLogin
async def modify(
    param: {module_info.modify_param_cls},
    service: {module_info.service_cls} = Depends({module_info.service_getter}),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(param, actor)
    return success()


@router.post("/api/v1/{module_info.route}/remove", summary="删除{module_info.pascal}", response_model=Result)
@CheckLogin
async def remove(param: IdsParam, service: {module_info.service_cls} = Depends({module_info.service_getter})):
    await service.remove(param)
    return success()


@router.get("/api/v1/{module_info.route}/detail", summary="{module_info.pascal}详情", response_model=Result[{module_info.vo_cls}])
@CheckLogin
async def detail(id: str = Query(...), service: {module_info.service_cls} = Depends({module_info.service_getter})):
    return success(await service.detail(id))
'''


def run_self_test() -> None:
    with tempfile.TemporaryDirectory(prefix="hei_codegen_") as tmp:
        tmp_root = Path(tmp)
        fake_plugins = tmp_root / "plugins"
        fake_plugins.mkdir()
        global PLUGINS_DIR
        old_plugins_dir = PLUGINS_DIR
        PLUGINS_DIR = fake_plugins
        try:
            cmd_scaffold("plugin_codegen_demo")
            cmd_add_module("plugin_codegen_demo", "article")
            generated = fake_plugins / "plugin_codegen_demo"
            py_files = [str(path) for path in generated.rglob("*.py")]
            for py_file in py_files:
                result = subprocess.run(
                    [sys.executable, "-m", "py_compile", py_file],
                    capture_output=True, text=True, cwd=REPO_ROOT,
                )
                if result.returncode != 0:
                    raise RuntimeError(
                        f"compile failed: {py_file}: {result.stderr.strip()}"
                    )
            assembly = (generated / "assembly.py").read_text(encoding="utf-8")
            plugin_py = (generated / "plugin.py").read_text(encoding="utf-8")
            migrate = (generated / "migrate.py").read_text(encoding="utf-8")
            assert "from .assembly import ROUTERS" in plugin_py, "Missing ROUTERS import in plugin.py"
            assert "demo_router" in assembly and "article_router" in assembly, "Missing routers in assembly.py"
            assert "Demo" in migrate and "Article" in migrate, "Missing models in migrate.py"
        finally:
            PLUGINS_DIR = old_plugins_dir
    logger.info("self-test passed")


def main() -> None:
    parser = argparse.ArgumentParser(description="hei-fastapi code generation tool")
    sub = parser.add_subparsers(dest="command")
    sub.add_parser("list", help="list all plugins")

    scaffold = sub.add_parser("scaffold", help="create new plugin scaffold")
    scaffold.add_argument("name", help="plugin name, e.g. plugin_demo or demo")
    scaffold.add_argument("--module", default="demo", help="initial module name, default: demo")

    plugin_cmd = sub.add_parser("plugin", help="create new plugin scaffold")
    plugin_cmd.add_argument("name", help="plugin name, e.g. plugin_demo or demo")
    plugin_cmd.add_argument("--module", default="demo", help="initial module name, default: demo")

    add_module = sub.add_parser("add-module", help="add a sub-module to an existing plugin")
    add_module.add_argument("plugin", help="plugin name")
    add_module.add_argument("module", help="module name")

    module_cmd = sub.add_parser("module", help="add a sub-module to an existing plugin")
    module_cmd.add_argument("plugin", help="plugin name")
    module_cmd.add_argument("module", help="module name")

    sub.add_parser("self-test", help="run codegen self test")

    args = parser.parse_args()
    try:
        if args.command == "list":
            cmd_list()
        elif args.command in {"scaffold", "plugin"}:
            cmd_scaffold(args.name, module=args.module)
        elif args.command in {"add-module", "module"}:
            cmd_add_module(args.plugin, args.module)
        elif args.command == "self-test":
            run_self_test()
        else:
            parser.print_help()
    except Exception as exc:
        logger.error("%s", exc)
        raise SystemExit(1) from exc


if __name__ == "__main__":
    main()
