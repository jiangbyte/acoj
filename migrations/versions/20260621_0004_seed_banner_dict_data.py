"""seed banner and dict data"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

from app.platform.id_generator.snowflake import generate_snowflake_id

revision: str = "20260621_0004"
down_revision: str | Sequence[str] | None = "20260621_0003"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


DICT_GROUPS = [
    (
        "COMMON_STATUS",
        "通用状态",
        "SYS",
        [
            ("COMMON_STATUS_ENABLED", "启用", "ENABLED", "#52c41a"),
            ("COMMON_STATUS_DISABLED", "停用", "DISABLED", "#8c8c8c"),
        ],
    ),
    (
        "COMMON_BOOLEAN",
        "是否选项",
        "SYS",
        [
            ("COMMON_BOOLEAN_YES", "是", "true", "#52c41a"),
            ("COMMON_BOOLEAN_NO", "否", "false", "#8c8c8c"),
        ],
    ),
    (
        "COMMON_AUDIT_ACTION",
        "审计动作",
        "SYS",
        [
            ("COMMON_AUDIT_ACTION_CREATE", "创建", "CREATE", "#1677ff"),
            ("COMMON_AUDIT_ACTION_UPDATE", "更新", "UPDATE", "#faad14"),
            ("COMMON_AUDIT_ACTION_DELETE", "删除", "DELETE", "#ff4d4f"),
            ("COMMON_AUDIT_ACTION_LOGIN", "登录", "LOGIN", "#52c41a"),
            ("COMMON_AUDIT_ACTION_LOGOUT", "登出", "LOGOUT", "#8c8c8c"),
        ],
    ),
    (
        "BANNER_CATEGORY",
        "Banner 分类",
        "BIZ",
        [
            ("BANNER_CATEGORY_HOME", "首页", "home", "#1677ff"),
            ("BANNER_CATEGORY_LOGIN", "登录页", "login", "#722ed1"),
            ("BANNER_CATEGORY_WORKPLACE", "工作台", "workplace", "#13c2c2"),
            ("BANNER_CATEGORY_NOTICE", "公告", "notice", "#faad14"),
        ],
    ),
    (
        "BANNER_TYPE",
        "Banner 类型",
        "BIZ",
        [
            ("BANNER_TYPE_CAROUSEL", "轮播", "carousel", "#1677ff"),
            ("BANNER_TYPE_HERO", "横幅", "hero", "#13c2c2"),
            ("BANNER_TYPE_NOTICE", "公告", "notice", "#faad14"),
            ("BANNER_TYPE_CARD", "卡片", "card", "#722ed1"),
        ],
    ),
    (
        "BANNER_POSITION",
        "Banner 位置",
        "BIZ",
        [
            ("BANNER_POSITION_HOME_TOP", "首页顶部", "home_top", "#1677ff"),
            ("BANNER_POSITION_LOGIN_SIDE", "登录页侧栏", "login_side", "#722ed1"),
            ("BANNER_POSITION_WORKPLACE_TOP", "工作台顶部", "workplace_top", "#13c2c2"),
            ("BANNER_POSITION_NOTICE_AREA", "公告区域", "notice_area", "#faad14"),
        ],
    ),
    (
        "BANNER_DISPLAY_SCOPE",
        "Banner 显示端",
        "BIZ",
        [
            ("BANNER_DISPLAY_SCOPE_PORTAL", "门户端", "PORTAL", "#1677ff"),
            ("BANNER_DISPLAY_SCOPE_ADMIN", "管理端", "ADMIN", "#722ed1"),
        ],
    ),
    (
        "BANNER_LINK_TYPE",
        "Banner 链接类型",
        "BIZ",
        [
            ("BANNER_LINK_TYPE_URL", "URL", "URL", "#1677ff"),
            ("BANNER_LINK_TYPE_ROUTE", "路由", "ROUTE", "#13c2c2"),
            ("BANNER_LINK_TYPE_NONE", "无跳转", "NONE", "#8c8c8c"),
        ],
    ),
    (
        "USER_ACCOUNT_TYPE",
        "用户类型",
        "SYS",
        [
            ("USER_ACCOUNT_TYPE_ADMIN", "管理员", "ADMIN", "#1677ff"),
            ("USER_ACCOUNT_TYPE_PORTAL", "门户用户", "PORTAL", "#13c2c2"),
            ("USER_ACCOUNT_TYPE_APP", "App 用户", "APP", "#722ed1"),
            ("USER_ACCOUNT_TYPE_MERCHANT", "商户", "MERCHANT", "#faad14"),
            ("USER_ACCOUNT_TYPE_PARTNER", "合作方", "PARTNER", "#eb2f96"),
        ],
    ),
    (
        "USER_ACCOUNT_STATUS",
        "账号状态",
        "SYS",
        [
            ("USER_ACCOUNT_STATUS_ENABLED", "启用", "ENABLED", "#52c41a"),
            ("USER_ACCOUNT_STATUS_DISABLED", "停用", "DISABLED", "#8c8c8c"),
            ("USER_ACCOUNT_STATUS_CANCELLED", "已注销", "CANCELLED", "#ff4d4f"),
        ],
    ),
    (
        "USER_GENDER",
        "性别",
        "SYS",
        [
            ("USER_GENDER_MALE", "男", "MALE", "#1677ff"),
            ("USER_GENDER_FEMALE", "女", "FEMALE", "#eb2f96"),
            ("USER_GENDER_UNKNOWN", "未知", "UNKNOWN", "#8c8c8c"),
        ],
    ),
    (
        "MENU_RESOURCE_TYPE",
        "菜单资源类型",
        "SYS",
        [
            ("MENU_RESOURCE_TYPE_CATALOG", "目录", "CATALOG", "#1677ff"),
            ("MENU_RESOURCE_TYPE_MENU", "菜单", "MENU", "#13c2c2"),
            ("MENU_RESOURCE_TYPE_PAGE", "页面", "PAGE", "#722ed1"),
            ("MENU_RESOURCE_TYPE_BUTTON", "按钮", "BUTTON", "#faad14"),
            ("MENU_RESOURCE_TYPE_ACTION", "动作", "ACTION", "#eb2f96"),
            ("MENU_RESOURCE_TYPE_API_GROUP", "接口分组", "API_GROUP", "#8c8c8c"),
        ],
    ),
    (
        "MENU_VISIBLE",
        "菜单显示",
        "SYS",
        [
            ("MENU_VISIBLE_SHOW", "显示", "true", "#52c41a"),
            ("MENU_VISIBLE_HIDE", "隐藏", "false", "#8c8c8c"),
        ],
    ),
    (
        "MENU_CACHE",
        "菜单缓存",
        "SYS",
        [
            ("MENU_CACHE_ENABLED", "缓存", "true", "#52c41a"),
            ("MENU_CACHE_DISABLED", "不缓存", "false", "#8c8c8c"),
        ],
    ),
    (
        "MENU_AFFIX",
        "菜单固定标签",
        "SYS",
        [
            ("MENU_AFFIX_ENABLED", "固定", "true", "#52c41a"),
            ("MENU_AFFIX_DISABLED", "不固定", "false", "#8c8c8c"),
        ],
    ),
    (
        "IAM_DATA_SCOPE",
        "数据范围",
        "SYS",
        [
            ("IAM_DATA_SCOPE_ALL", "全部", "ALL", "#1677ff"),
            ("IAM_DATA_SCOPE_DEPT_AND_CHILD", "部门及子部门", "DEPT_AND_CHILD", "#13c2c2"),
            ("IAM_DATA_SCOPE_DEPT", "部门", "DEPT", "#722ed1"),
            ("IAM_DATA_SCOPE_SELF", "本人", "SELF", "#faad14"),
            ("IAM_DATA_SCOPE_CUSTOM", "自定义", "CUSTOM", "#eb2f96"),
        ],
    ),
    (
        "IAM_GRANT_EFFECT",
        "授权效果",
        "SYS",
        [
            ("IAM_GRANT_EFFECT_ALLOW", "允许", "ALLOW", "#52c41a"),
            ("IAM_GRANT_EFFECT_DENY", "拒绝", "DENY", "#ff4d4f"),
        ],
    ),
    (
        "IAM_GRANT_MODE",
        "授权模式",
        "SYS",
        [
            ("IAM_GRANT_MODE_DIRECT", "直接", "DIRECT", "#1677ff"),
            ("IAM_GRANT_MODE_CASCADE", "级联", "CASCADE", "#13c2c2"),
        ],
    ),
]


BANNERS = [
    {
        "title": "平台能力总览",
        "image": "https://images.unsplash.com/photo-1551288049-bebda4e38f71",
        "url": "/dashboard/analysis",
        "link_type": "ROUTE",
        "summary": "统一查看运营、治理和系统健康指标",
        "description": "面向门户首页的核心能力入口，帮助用户快速进入分析看板。",
        "category": "home",
        "type": "carousel",
        "position": "home_top",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "权限治理中心",
        "image": "https://images.unsplash.com/photo-1563986768609-322da13575f3",
        "url": "/iam/resources",
        "link_type": "ROUTE",
        "summary": "资源、角色、账号与数据范围统一治理",
        "description": "展示权限体系能力，适合在门户或运营首页引导管理员关注授权治理。",
        "category": "home",
        "type": "carousel",
        "position": "home_top",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    {
        "title": "文件服务管控",
        "image": "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
        "url": "/file",
        "link_type": "ROUTE",
        "summary": "统一管理上传文件、存储服务和访问地址",
        "description": "用于展示文件模块能力，强调存储接入、文件审计和访问管理。",
        "category": "home",
        "type": "carousel",
        "position": "home_top",
        "display_scope": "PORTAL",
        "sort": 3,
    },
    {
        "title": "数据运营洞察",
        "image": "https://images.unsplash.com/photo-1460925895917-afdab827c52f",
        "url": "/dashboard/analysis",
        "link_type": "ROUTE",
        "summary": "追踪趋势、风险、健康度与待处理事项",
        "description": "面向运营分析场景的推广 banner。",
        "category": "home",
        "type": "carousel",
        "position": "home_top",
        "display_scope": "PORTAL",
        "sort": 4,
    },
    {
        "title": "工作台待办提醒",
        "image": "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b",
        "url": "/dashboard/workplace",
        "link_type": "ROUTE",
        "summary": "集中查看任务、告警和审批进展",
        "description": "工作台顶部提示，帮助用户聚焦日常处理事项。",
        "category": "workplace",
        "type": "hero",
        "position": "workplace_top",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "版本更新公告",
        "image": "https://images.unsplash.com/photo-1516321318423-f06f85e504b3",
        "url": "/dashboard/workplace",
        "link_type": "ROUTE",
        "summary": "查看近期功能更新和优化说明",
        "description": "用于工作台或公告位展示版本更新。",
        "category": "notice",
        "type": "notice",
        "position": "notice_area",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    {
        "title": "安全登录提醒",
        "image": "https://images.unsplash.com/photo-1555949963-aa79dcee981c",
        "url": "/auth/login",
        "link_type": "ROUTE",
        "summary": "保护账号安全，定期检查登录设备",
        "description": "登录页侧栏 banner，强调账号安全和审计能力。",
        "category": "login",
        "type": "hero",
        "position": "login_side",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "统一身份认证",
        "image": "https://images.unsplash.com/photo-1519389950473-47ba0277781c",
        "url": "/auth/login",
        "link_type": "ROUTE",
        "summary": "统一入口、统一会话、统一权限校验",
        "description": "登录页身份能力展示 banner。",
        "category": "login",
        "type": "hero",
        "position": "login_side",
        "display_scope": "PORTAL",
        "sort": 2,
    },
]


def _dict_codes() -> list[str]:
    codes: list[str] = []
    for root_code, _, _, children in DICT_GROUPS:
        codes.append(root_code)
        codes.extend(child[0] for child in children)
    return codes


def _banner_keys() -> list[dict[str, str]]:
    return [
        {
            "title": item["title"],
            "category": item["category"],
            "position": item["position"],
            "display_scope": item["display_scope"],
        }
        for item in BANNERS
    ]


def _delete_seed_banners() -> None:
    for item in _banner_keys():
        op.execute(
            sa.text(
                """
                delete from sys_banner
                where title = :title
                  and category = :category
                  and position = :position
                  and display_scope = :display_scope
                """
            ).bindparams(**item)
        )


def _delete_seed_dicts() -> None:
    stmt = sa.text("delete from sys_dict where code in :codes").bindparams(
        sa.bindparam("codes", expanding=True)
    )
    op.get_bind().execute(stmt, {"codes": _dict_codes()})


def upgrade() -> None:
    _delete_seed_banners()
    _delete_seed_dicts()

    dict_table = sa.table(
        "sys_dict",
        sa.column("id", sa.String),
        sa.column("code", sa.String),
        sa.column("label", sa.String),
        sa.column("value", sa.String),
        sa.column("color", sa.String),
        sa.column("category", sa.String),
        sa.column("parent_id", sa.String),
        sa.column("status", sa.String),
        sa.column("sort", sa.Integer),
    )
    dict_rows = []
    for root_sort, (root_code, root_label, category, children) in enumerate(DICT_GROUPS, start=1):
        root_id = generate_snowflake_id()
        dict_rows.append(
            {
                "id": root_id,
                "code": root_code,
                "label": root_label,
                "value": root_code,
                "color": None,
                "category": category,
                "parent_id": None,
                "status": "ENABLED",
                "sort": root_sort,
            }
        )
        for child_sort, (code, label, value, color) in enumerate(children, start=1):
            dict_rows.append(
                {
                    "id": generate_snowflake_id(),
                    "code": code,
                    "label": label,
                    "value": value,
                    "color": color,
                    "category": category,
                    "parent_id": root_id,
                    "status": "ENABLED",
                    "sort": child_sort,
                }
            )
    op.bulk_insert(dict_table, dict_rows)

    banner_table = sa.table(
        "sys_banner",
        sa.column("id", sa.String),
        sa.column("title", sa.String),
        sa.column("image", sa.String),
        sa.column("url", sa.String),
        sa.column("link_type", sa.String),
        sa.column("summary", sa.String),
        sa.column("description", sa.Text),
        sa.column("category", sa.String),
        sa.column("type", sa.String),
        sa.column("position", sa.String),
        sa.column("display_scope", sa.String),
        sa.column("sort", sa.Integer),
        sa.column("interaction_count", sa.BigInteger),
        sa.column("status", sa.String),
    )
    op.bulk_insert(
        banner_table,
        [
            {
                **item,
                "id": generate_snowflake_id(),
                "interaction_count": 0,
                "status": "ENABLED",
            }
            for item in BANNERS
        ],
    )


def downgrade() -> None:
    _delete_seed_banners()
    _delete_seed_dicts()
