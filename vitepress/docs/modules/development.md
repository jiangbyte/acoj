# 模块开发规范

Hei FastAPI 采用垂直切片（Vertical Slice）架构组织业务模块。每个模块独立包含模型、参数定义、Repository、业务逻辑和 API 层，具有高内聚低耦合的特点。

## 模块结构约定

每个业务模块遵循统一的结构约定：

```
plugins/<domain>/<module>/
├── models.py        # SQLAlchemy ORM 模型
├── params.py        # Pydantic v2 请求/响应模型
├── repository.py    # Repository 层
├── service.py       # 业务逻辑层
└── api/v1/api.py    # FastAPI 路由定义 + Controller
```

### 文件名说明

| 文件 | 必选 | 说明 |
|------|------|------|
| `models.py` | 是 | SQLAlchemy ORM 模型定义（Mapped + mapped_column） |
| `params.py` | 是 | Pydantic v2 请求参数和响应模型 |
| `repository.py` | 是 | Repository 层 |
| `service.py` | 是 | 业务逻辑层 |
| `api/v1/api.py` | 是 | FastAPI APIRouter 路由定义 + Controller |

## 文件模板

### models.py - ORM 模型

```python
from datetime import datetime
from sqlalchemy import String, Text, Integer, DateTime, func
from sqlalchemy.orm import Mapped, mapped_column
from sdk.db.mysql import Base


class SysDemo(Base):
    """示例表"""
    __tablename__ = "sys_demo"
    __table_args__ = {"comment": "示例表"}

    id: Mapped[str] = mapped_column(String(32), primary_key=True, comment="主键")
    name: Mapped[str] = mapped_column(String(100), comment="名称")
    code: Mapped[str] = mapped_column(String(50), unique=True, comment="编码")
    status: Mapped[int] = mapped_column(Integer, default=1, comment="状态：1-启用 0-禁用")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    sort_code: Mapped[int] = mapped_column(Integer, default=0, comment="排序码")

    # 系统字段
    created_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), comment="创建时间"
    )
    created_by: Mapped[str | None] = mapped_column(String(32), comment="创建人")
    updated_at: Mapped[datetime | None] = mapped_column(
        DateTime, onupdate=func.now(), comment="更新时间"
    )
    updated_by: Mapped[str | None] = mapped_column(String(32), comment="更新人")
```

### params.py - 参数定义

```python
from datetime import datetime
from pydantic import BaseModel, Field
from typing import Optional, List
from sdk.pojo.id_params import IdParam


# ---------- 请求参数 ----------

class SysDemoPageParam(BaseModel):
    """示例表分页查询参数"""
    current: int = Field(default=1, description="页码")
    size: int = Field(default=10, description="每页条数")
    keyword: Optional[str] = Field(default=None, description="关键词")
    status: Optional[int] = Field(default=None, description="状态")


class SysDemoCreateParam(BaseModel):
    """创建示例参数"""
    name: str = Field(description="名称")
    code: str = Field(description="编码")
    status: int = Field(default=1, description="状态")
    description: Optional[str] = Field(default=None, description="描述")
    sort_code: int = Field(default=0, description="排序码")


class SysDemoModifyParam(IdParam):
    """修改示例参数"""
    name: Optional[str] = Field(default=None, description="名称")
    status: Optional[int] = Field(default=None, description="状态")
    description: Optional[str] = Field(default=None, description="描述")
    sort_code: Optional[int] = Field(default=None, description="排序码")


# ---------- 响应模型 ----------

class SysDemoVO(BaseModel):
    """示例表响应"""
    id: str
    name: str
    code: str
    status: int
    description: Optional[str] = None
    sort_code: int
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True
```

### repository.py - Repository 层

```python
from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete as sa_delete
from sdk.utils import generate_id
from .models import SysDemo


class SysDemoRepository:
    """示例表 Repository"""

    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[SysDemo]:
        return self.db.execute(
            select(SysDemo).where(SysDemo.id == id)
        ).scalar_one_or_none()

    def insert(self, entity: SysDemo) -> SysDemo:
        if not entity.id:
            entity.id = generate_id()
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def update(self, entity: SysDemo) -> SysDemo:
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysDemo).where(SysDemo.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected

    # ---- custom query ----

    def find_page_by_filters(self, keyword: Optional[str] = None,
                             status: Optional[int] = None,
                             current: int = 1, size: int = 10) -> Dict[str, Any]:
        filters = []
        if keyword:
            keyword_like = f"%{keyword}%"
            filters.append(SysDemo.name.ilike(keyword_like))

        if status is not None:
            filters.append(SysDemo.status == status)

        offset = (max(1, current) - 1) * max(1, size)
        count_stmt = select(func.count()).select_from(SysDemo).where(*filters)
        total = self.db.execute(count_stmt).scalar() or 0

        stmt = (select(SysDemo).where(*filters)
                .order_by(SysDemo.sort_code.asc(), SysDemo.created_at.desc())
                .offset(offset).limit(size))
        records = list(self.db.execute(stmt).scalars().all())
        return {"records": records, "total": total}
```

### service.py - 业务逻辑层

```python
from typing import Optional, List
from sqlalchemy.orm import Session
from sdk.utils import generate_id
from sdk.exception import BusinessException
from .repository import SysDemoRepository
from .params import (
    SysDemoPageParam, SysDemoCreateParam,
    SysDemoModifyParam, SysDemoVO
)
from .models import SysDemo


class SysDemoService:
    """示例表业务逻辑层"""

    def __init__(self, db: Session):
        self.repository = SysDemoRepository(db)

    def page(self, param: SysDemoPageParam):
        """分页查询"""
        result = self.repository.find_page_by_filters(
            keyword=param.keyword,
            status=param.status,
            current=param.current,
            size=param.size
        )
        records = [SysDemoVO.model_validate(r) for r in result["records"]]
        return result["total"], records

    def create(self, param: SysDemoCreateParam, user_id: str) -> SysDemo:
        """创建"""
        entity = SysDemo(
            id=generate_id(),
            name=param.name,
            code=param.code,
            status=param.status,
            description=param.description,
            sort_code=param.sort_code,
            created_by=user_id,
        )
        return self.repository.insert(entity)

    def modify(self, param: SysDemoModifyParam, user_id: str) -> SysDemo:
        """修改"""
        entity = self.repository.find_by_id(param.id)
        if not entity:
            raise BusinessException("记录不存在", 404)

        update_data = param.model_dump(exclude={"id"}, exclude_none=True)
        for key, value in update_data.items():
            setattr(entity, key, value)
        entity.updated_by = user_id
        return self.repository.update(entity)

    def remove(self, ids: List[str]):
        """删除"""
        self.repository.delete_by_ids(ids)

    def detail(self, id: str) -> Optional[SysDemo]:
        """详情"""
        return self.repository.find_by_id(id)
```

### api/v1/api.py - 路由与 Controller

```python
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sdk.db.mysql import get_db
from sdk.result import success, page_data
from sdk.auth.decorator import HeiCheckPermission
from sdk.log import SysLog
from ..service import SysDemoService
from ..params import (
    SysDemoPageParam, SysDemoCreateParam,
    SysDemoModifyParam, IdsParam
)

router = APIRouter(prefix="/api/v1/sys/demo", tags=["示例管理"])


@router.get("/page")
@HeiCheckPermission("sys:demo:page")
async def page(param: SysDemoPageParam = Depends(), db: Session = Depends(get_db)):
    """分页查询"""
    service = SysDemoService(db)
    total, records = service.page(param)
    return page_data(total, records, param.current, param.size)


@router.post("/create")
@SysLog("新增示例")
@HeiCheckPermission("sys:demo:create")
async def create(param: SysDemoCreateParam, db: Session = Depends(get_db)):
    """新增示例"""
    service = SysDemoService(db)
    # 从 request 中获取当前用户 ID（通过 AuthMiddleware 注入）
    entity = service.create(param, user_id="system")
    return success(entity)


@router.post("/modify")
@SysLog("修改示例")
@HeiCheckPermission("sys:demo:modify")
async def modify(param: SysDemoModifyParam, db: Session = Depends(get_db)):
    """修改示例"""
    service = SysDemoService(db)
    entity = service.modify(param, user_id="system")
    return success(entity)


@router.post("/remove")
@SysLog("删除示例")
@HeiCheckPermission("sys:demo:remove")
async def remove(param: IdsParam, db: Session = Depends(get_db)):
    """删除示例"""
    service = SysDemoService(db)
    service.remove(param.ids)
    return success()


@router.get("/detail")
@HeiCheckPermission("sys:demo:detail")
async def detail(id: str, db: Session = Depends(get_db)):
    """示例详情"""
    service = SysDemoService(db)
    entity = service.detail(id)
    return success(entity)
```

## 路由注册

在模块创建完成后，需要在 `sdk/app/router.py` 中注册模块路由：

```python
# sdk/app/router.py

def register_routers(app: FastAPI):
    """注册所有模块路由"""
    
    # 已注册的模块...
    
    # 注册新模块
    from modules.sys.demo.api.v1.api import router as demo_router
    app.include_router(demo_router)
```

## 创建新模块的完整步骤

### 第一步：创建模块目录

```bash
mkdir -p plugins/sys/<module>/api/v1
```

### 第二步：实现 models.py

定义 SQLAlchemy ORM 模型，使用 `Mapped` + `mapped_column` 声明式映射。

### 第三步：实现 params.py

定义 Pydantic v2 请求参数和响应模型。

### 第四步：实现 repository.py

编写独立的 Repository 类，实现数据访问逻辑。参考现有模块的 Repository 模式。

### 第五步：实现 service.py

编写独立的业务逻辑类，组合 Repository 完成业务操作。

### 第六步：实现 api/v1/api.py

定义 FastAPI APIRouter 和路由 Handler。

### 第七步：在 router.py 中注册

将模块 APIRouter 注册到应用中。

## 权限代码命名规范

权限代码使用统一的命名规范：`<模块>:<操作>`。

### 标准操作

| 权限代码 | 说明 |
|---------|------|
| `<module>:page` | 分页查询 |
| `<module>:list` | 列表查询 |
| `<module>:create` | 新增 |
| `<module>:modify` | 修改 |
| `<module>:remove` | 删除 |
| `<module>:detail` | 详情查询 |
| `<module>:export` | 导出 |
| `<module>:import` | 导入 |

### 示例

| 模块 | 权限代码示例 |
|------|------------|
| 用户管理 | `sys:user:page`, `sys:user:create`, `sys:user:remove` |
| 角色管理 | `sys:role:page`, `sys:role:grant-permission` |
| 配置管理 | `sys:config:page`, `sys:config:edit` |
| 字典管理 | `sys:dict:page`, `sys:dict:create`, `sys:dict:remove` |
| C 端用户 | `client:user:page`, `client:user:create` |

## 技术要点

### 统一响应

所有 API 响应必须使用 `core.result` 包提供的函数：

```python
from sdk.result import success, failure, page_data

# 成功响应
success(data)

# 失败响应
failure("错误信息")

# 分页响应
page_data(records, total, page, size)
```

### 业务异常

业务错误使用 `raise BusinessException` 模式：

```python
from sdk.exception import BusinessException

# 抛出业务异常
raise BusinessException("用户名已存在", 400)

# 全局异常处理器会自动捕获并返回标准错误响应
```

### 获取当前登录用户

```python
from sdk.auth.auth.hei_auth_tool import HeiAuthTool

# 获取当前登录用户 ID
user_id = await HeiAuthTool.getLoginId(request)
if not user_id:
    raise BusinessException("未登录", 401)
```

### 分页查询

```python
# 使用 SQLAlchemy select 构建查询
from sqlalchemy import select, func
from sqlalchemy.orm import Session

def find_page_by_filters(self, keyword=None, status=None, current=1, size=10):
    filters = []
    if keyword:
        filters.append(SysDemo.name.ilike(f"%{keyword}%"))
    if status is not None:
        filters.append(SysDemo.status == status)

    offset = (max(1, current) - 1) * max(1, size)
    total = self.db.execute(
        select(func.count()).select_from(SysDemo).where(*filters)
    ).scalar() or 0

    stmt = (select(SysDemo).where(*filters)
            .order_by(SysDemo.sort_code.asc())
            .offset(offset).limit(size))
    records = list(self.db.execute(stmt).scalars().all())
    return {"records": records, "total": total}
```

### 生成雪花 ID

```python
from sdk.utils import generate_id

# 生成分布式唯一 ID
id = generate_id()
```

## 现有模块参考

编写新模块时，可以参考以下现有模块的实现：

- **sys/auth/username**：认证模块，包含登录、注册、登出
- **sys/user**：用户管理，标准的 CRUD 模块
- **sys/role**：角色管理，包含权限分配
- **sys/org**：组织管理，树形结构
- **sys/banner**：Banner 管理，包含文件上传
- **sys/config**：系统配置，包含批量编辑

每个模块都遵循相同的 `models.py` + `params.py` + `repository.py` + `service.py` + `api/v1/api.py` 结构。
