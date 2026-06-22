"""reseed dict and banner with uppercase values

Fixes the issue where dict child values and banner reference fields used
mixed-case (e.g. 'home', 'carousel', 'home_top') instead of the proper
uppercase-with-underscore convention (e.g. 'HOME', 'CAROUSEL', 'HOME_TOP').
Expands dict coverage for IAM, file, notice, and other business modules.
Adds more banner entries for both portal and admin sides.
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

from app.platform.id_generator.snowflake import generate_snowflake_id

revision: str = "20260621_0008"
down_revision: str | Sequence[str] | None = "20260621_0007"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

# ---------------------------------------------------------------------------
# Dict groups
# (root_code, root_label, category, children)
# each child: (code, label, value, color)
# ---------------------------------------------------------------------------

DICT_GROUPS = [
    # ============ 通用 ============
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
            ("COMMON_BOOLEAN_YES", "是", "TRUE", "#52c41a"),
            ("COMMON_BOOLEAN_NO", "否", "FALSE", "#8c8c8c"),
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
            ("COMMON_AUDIT_ACTION_IMPORT", "导入", "IMPORT", "#722ed1"),
            ("COMMON_AUDIT_ACTION_EXPORT", "导出", "EXPORT", "#13c2c2"),
        ],
    ),
    (
        "COMMON_YES_NO",
        "是/否",
        "SYS",
        [
            ("COMMON_YES_NO_YES", "是", "YES", "#52c41a"),
            ("COMMON_YES_NO_NO", "否", "NO", "#ff4d4f"),
        ],
    ),
    # ============ Banner ============
    (
        "BANNER_CATEGORY",
        "Banner 分类",
        "BIZ",
        [
            ("BANNER_CATEGORY_HOME", "首页", "HOME", "#1677ff"),
            ("BANNER_CATEGORY_LOGIN", "登录页", "LOGIN", "#722ed1"),
            ("BANNER_CATEGORY_WORKPLACE", "工作台", "WORKPLACE", "#13c2c2"),
            ("BANNER_CATEGORY_NOTICE", "公告", "NOTICE", "#faad14"),
            ("BANNER_CATEGORY_ADMIN_DASHBOARD", "管理控制台", "ADMIN_DASHBOARD", "#eb2f96"),
            ("BANNER_CATEGORY_SYSTEM_UPGRADE", "系统升级", "SYSTEM_UPGRADE", "#fa541c"),
        ],
    ),
    (
        "BANNER_TYPE",
        "Banner 类型",
        "BIZ",
        [
            ("BANNER_TYPE_CAROUSEL", "轮播", "CAROUSEL", "#1677ff"),
            ("BANNER_TYPE_HERO", "横幅", "HERO", "#13c2c2"),
            ("BANNER_TYPE_NOTICE", "公告条", "NOTICE", "#faad14"),
            ("BANNER_TYPE_CARD", "卡片", "CARD", "#722ed1"),
            ("BANNER_TYPE_POPUP", "弹窗", "POPUP", "#eb2f96"),
            ("BANNER_TYPE_SIDEBAR", "侧栏", "SIDEBAR", "#52c41a"),
        ],
    ),
    (
        "BANNER_POSITION",
        "Banner 位置",
        "BIZ",
        [
            ("BANNER_POSITION_HOME_TOP", "首页顶部", "HOME_TOP", "#1677ff"),
            ("BANNER_POSITION_HOME_MIDDLE", "首页中部", "HOME_MIDDLE", "#13c2c2"),
            ("BANNER_POSITION_HOME_BOTTOM", "首页底部", "HOME_BOTTOM", "#722ed1"),
            ("BANNER_POSITION_LOGIN_SIDE", "登录页侧栏", "LOGIN_SIDE", "#722ed1"),
            ("BANNER_POSITION_WORKPLACE_TOP", "工作台顶部", "WORKPLACE_TOP", "#13c2c2"),
            ("BANNER_POSITION_NOTICE_AREA", "公告区域", "NOTICE_AREA", "#faad14"),
            ("BANNER_POSITION_ADMIN_TOP", "管理端顶部", "ADMIN_TOP", "#eb2f96"),
            ("BANNER_POSITION_ADMIN_SIDEBAR", "管理端侧栏", "ADMIN_SIDEBAR", "#52c41a"),
        ],
    ),
    (
        "BANNER_DISPLAY_SCOPE",
        "Banner 显示端",
        "BIZ",
        [
            ("BANNER_DISPLAY_SCOPE_PORTAL", "门户端", "PORTAL", "#1677ff"),
            ("BANNER_DISPLAY_SCOPE_ADMIN", "管理端", "ADMIN", "#722ed1"),
            ("BANNER_DISPLAY_SCOPE_APP", "移动端", "APP", "#13c2c2"),
        ],
    ),
    (
        "BANNER_LINK_TYPE",
        "Banner 链接类型",
        "BIZ",
        [
            ("BANNER_LINK_TYPE_URL", "外链", "URL", "#1677ff"),
            ("BANNER_LINK_TYPE_ROUTE", "路由", "ROUTE", "#13c2c2"),
            ("BANNER_LINK_TYPE_NONE", "无跳转", "NONE", "#8c8c8c"),
        ],
    ),
    # ============ 用户 ============
    (
        "USER_ACCOUNT_TYPE",
        "账户类型",
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
            ("USER_ACCOUNT_STATUS_LOCKED", "已锁定", "LOCKED", "#faad14"),
            ("USER_ACCOUNT_STATUS_EXPIRED", "已过期", "EXPIRED", "#722ed1"),
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
        "USER_CERTIFICATION_LEVEL",
        "实名认证等级",
        "BIZ",
        [
            ("USER_CERTIFICATION_LEVEL_NONE", "未认证", "NONE", "#8c8c8c"),
            ("USER_CERTIFICATION_LEVEL_BASIC", "基础认证", "BASIC", "#faad14"),
            ("USER_CERTIFICATION_LEVEL_ADVANCED", "高级认证", "ADVANCED", "#52c41a"),
        ],
    ),
    # ============ 菜单 / 资源 ============
    (
        "MENU_RESOURCE_TYPE",
        "资源类型",
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
        "菜单可见",
        "SYS",
        [
            ("MENU_VISIBLE_SHOW", "显示", "TRUE", "#52c41a"),
            ("MENU_VISIBLE_HIDE", "隐藏", "FALSE", "#8c8c8c"),
        ],
    ),
    (
        "MENU_CACHE",
        "菜单缓存",
        "SYS",
        [
            ("MENU_CACHE_ENABLED", "缓存", "TRUE", "#52c41a"),
            ("MENU_CACHE_DISABLED", "不缓存", "FALSE", "#8c8c8c"),
        ],
    ),
    (
        "MENU_AFFIX",
        "菜单固定标签",
        "SYS",
        [
            ("MENU_AFFIX_ENABLED", "固定", "TRUE", "#52c41a"),
            ("MENU_AFFIX_DISABLED", "不固定", "FALSE", "#8c8c8c"),
        ],
    ),
    # ============ IAM ============
    (
        "IAM_DATA_SCOPE",
        "数据范围",
        "SYS",
        [
            ("IAM_DATA_SCOPE_ALL", "全部", "ALL", "#1677ff"),
            ("IAM_DATA_SCOPE_DEPT_AND_CHILD", "部门及子部门", "DEPT_AND_CHILD", "#13c2c2"),
            ("IAM_DATA_SCOPE_DEPT", "本部门", "DEPT", "#722ed1"),
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
    (
        "IAM_ROLE_CATEGORY",
        "角色分类",
        "SYS",
        [
            ("IAM_ROLE_CATEGORY_SYSTEM", "系统角色", "SYSTEM", "#1677ff"),
            ("IAM_ROLE_CATEGORY_BUSINESS", "业务角色", "BUSINESS", "#13c2c2"),
            ("IAM_ROLE_CATEGORY_OPERATION", "运营角色", "OPERATION", "#722ed1"),
            ("IAM_ROLE_CATEGORY_AUDIT", "审计角色", "AUDIT", "#faad14"),
        ],
    ),
    (
        "IAM_DEPT_CATEGORY",
        "部门类别",
        "SYS",
        [
            ("IAM_DEPT_CATEGORY_COMPANY", "公司", "COMPANY", "#1677ff"),
            ("IAM_DEPT_CATEGORY_DEPARTMENT", "部门", "DEPARTMENT", "#13c2c2"),
            ("IAM_DEPT_CATEGORY_SUB_DEPT", "子部门", "SUB_DEPT", "#722ed1"),
            ("IAM_DEPT_CATEGORY_TEAM", "团队", "TEAM", "#faad14"),
            ("IAM_DEPT_CATEGORY_VIRTUAL", "虚拟组织", "VIRTUAL", "#8c8c8c"),
        ],
    ),
    (
        "IAM_POSITION_CATEGORY",
        "职位类别",
        "SYS",
        [
            ("IAM_POSITION_CATEGORY_MANAGEMENT", "管理岗", "MANAGEMENT", "#1677ff"),
            ("IAM_POSITION_CATEGORY_TECHNICAL", "技术岗", "TECHNICAL", "#13c2c2"),
            ("IAM_POSITION_CATEGORY_OPERATION", "运营岗", "OPERATION", "#722ed1"),
            ("IAM_POSITION_CATEGORY_SALES", "销售岗", "SALES", "#faad14"),
            ("IAM_POSITION_CATEGORY_FINANCE", "财务岗", "FINANCE", "#eb2f96"),
            ("IAM_POSITION_CATEGORY_HR", "人事岗", "HR", "#52c41a"),
        ],
    ),
    # ============ 文件 ============
    (
        "FILE_TYPE",
        "文件类型",
        "BIZ",
        [
            ("FILE_TYPE_IMAGE", "图片", "IMAGE", "#1677ff"),
            ("FILE_TYPE_DOCUMENT", "文档", "DOCUMENT", "#13c2c2"),
            ("FILE_TYPE_VIDEO", "视频", "VIDEO", "#722ed1"),
            ("FILE_TYPE_AUDIO", "音频", "AUDIO", "#faad14"),
            ("FILE_TYPE_ARCHIVE", "压缩包", "ARCHIVE", "#eb2f96"),
            ("FILE_TYPE_OTHER", "其他", "OTHER", "#8c8c8c"),
        ],
    ),
    (
        "FILE_STORAGE_TYPE",
        "存储类型",
        "BIZ",
        [
            ("FILE_STORAGE_TYPE_LOCAL", "本地存储", "LOCAL", "#1677ff"),
            ("FILE_STORAGE_TYPE_S3", "S3 对象存储", "S3", "#13c2c2"),
            ("FILE_STORAGE_TYPE_MINIO", "MinIO", "MINIO", "#722ed1"),
            ("FILE_STORAGE_TYPE_OSS", "阿里云 OSS", "OSS", "#faad14"),
            ("FILE_STORAGE_TYPE_COS", "腾讯云 COS", "COS", "#eb2f96"),
        ],
    ),
    (
        "FILE_UPLOAD_SOURCE",
        "上传来源",
        "BIZ",
        [
            ("FILE_UPLOAD_SOURCE_PORTAL", "门户端", "PORTAL", "#1677ff"),
            ("FILE_UPLOAD_SOURCE_ADMIN", "管理端", "ADMIN", "#722ed1"),
            ("FILE_UPLOAD_SOURCE_APP", "移动端", "APP", "#13c2c2"),
            ("FILE_UPLOAD_SOURCE_API", "Open API", "API", "#faad14"),
        ],
    ),
    # ============ 通知 ============
    (
        "NOTICE_TYPE",
        "通知类型",
        "BIZ",
        [
            ("NOTICE_TYPE_SYSTEM", "系统通知", "SYSTEM", "#1677ff"),
            ("NOTICE_TYPE_BIZ", "业务通知", "BIZ", "#13c2c2"),
            ("NOTICE_TYPE_MAINTENANCE", "维护通知", "MAINTENANCE", "#faad14"),
            ("NOTICE_TYPE_SECURITY", "安全通知", "SECURITY", "#ff4d4f"),
        ],
    ),
    (
        "NOTICE_PRIORITY",
        "通知优先级",
        "BIZ",
        [
            ("NOTICE_PRIORITY_LOW", "低", "LOW", "#8c8c8c"),
            ("NOTICE_PRIORITY_NORMAL", "普通", "NORMAL", "#1677ff"),
            ("NOTICE_PRIORITY_HIGH", "高", "HIGH", "#faad14"),
            ("NOTICE_PRIORITY_URGENT", "紧急", "URGENT", "#ff4d4f"),
        ],
    ),
    (
        "NOTICE_PUBLISH_STATUS",
        "通知发布状态",
        "BIZ",
        [
            ("NOTICE_PUBLISH_STATUS_DRAFT", "草稿", "DRAFT", "#8c8c8c"),
            ("NOTICE_PUBLISH_STATUS_PUBLISHED", "已发布", "PUBLISHED", "#52c41a"),
            ("NOTICE_PUBLISH_STATUS_CANCELLED", "已撤回", "CANCELLED", "#ff4d4f"),
            ("NOTICE_PUBLISH_STATUS_SCHEDULED", "定时发布", "SCHEDULED", "#faad14"),
        ],
    ),
    # ============ 日志 ============
    (
        "LOG_MODULE",
        "日志模块",
        "SYS",
        [
            ("LOG_MODULE_AUTH", "认证", "AUTH", "#1677ff"),
            ("LOG_MODULE_IAM", "权限", "IAM", "#13c2c2"),
            ("LOG_MODULE_USER", "用户", "USER", "#722ed1"),
            ("LOG_MODULE_FILE", "文件", "FILE", "#faad14"),
            ("LOG_MODULE_BANNER", "Banner", "BANNER", "#eb2f96"),
            ("LOG_MODULE_SYSTEM", "系统", "SYSTEM", "#8c8c8c"),
        ],
    ),
]

# ---------------------------------------------------------------------------
# Banners
# ---------------------------------------------------------------------------

BANNERS = [
    # ---- 门户首页轮播 ----
    {
        "title": "平台能力总览",
        "image": "https://images.unsplash.com/photo-1551288049-bebda4e38f71",
        "url": "/dashboard/analysis",
        "link_type": "ROUTE",
        "summary": "统一查看运营、治理和系统健康指标",
        "description": "面向门户首页的核心能力入口，帮助用户快速进入分析看板。",
        "category": "HOME",
        "type": "CAROUSEL",
        "position": "HOME_TOP",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "权限治理中心",
        "image": "https://images.unsplash.com/photo-1563986768609-322da13575f3",
        "url": "/iam/resources",
        "link_type": "ROUTE",
        "summary": "资源、角色、账号与数据范围统一治理",
        "description": "展示权限体系能力，引导管理员关注授权治理。",
        "category": "HOME",
        "type": "CAROUSEL",
        "position": "HOME_TOP",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    {
        "title": "文件服务管控",
        "image": "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
        "url": "/file",
        "link_type": "ROUTE",
        "summary": "统一管理上传文件、存储服务和访问地址",
        "description": "文件模块能力展示，强调存储接入、文件审计和访问管理。",
        "category": "HOME",
        "type": "CAROUSEL",
        "position": "HOME_TOP",
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
        "category": "HOME",
        "type": "CAROUSEL",
        "position": "HOME_TOP",
        "display_scope": "PORTAL",
        "sort": 4,
    },
    {
        "title": "全新的工作台体验",
        "image": "https://images.unsplash.com/photo-1497366216548-37526070297c",
        "url": "/dashboard/workplace",
        "link_type": "ROUTE",
        "summary": "个性化工作台，聚焦核心任务",
        "description": "展示新版工作台的高效操作体验。",
        "category": "HOME",
        "type": "CAROUSEL",
        "position": "HOME_TOP",
        "display_scope": "PORTAL",
        "sort": 5,
    },
    # ---- 首页中部 ----
    {
        "title": "快速接入指南",
        "image": "https://images.unsplash.com/photo-1516321318423-f06f85e504b3",
        "url": "/docs/guide",
        "link_type": "ROUTE",
        "summary": "新手必读：五分钟完成接入配置",
        "description": "新用户引导卡片，快速了解平台核心操作流程。",
        "category": "HOME",
        "type": "CARD",
        "position": "HOME_MIDDLE",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "安全巡检建议",
        "image": "https://images.unsplash.com/photo-1555949963-aa79dcee981c",
        "url": "/dashboard/analysis",
        "link_type": "ROUTE",
        "summary": "定期检查账号权限和登录设备",
        "description": "安全巡检提示卡片，引导用户关注账号安全。",
        "category": "HOME",
        "type": "CARD",
        "position": "HOME_MIDDLE",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    # ---- 首页底部 ----
    {
        "title": "平台更新日志",
        "image": "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b",
        "url": "/changelog",
        "link_type": "ROUTE",
        "summary": "查看最新功能发布和改进",
        "description": "首页底部展示更新日志入口。",
        "category": "HOME",
        "type": "NOTICE",
        "position": "HOME_BOTTOM",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    # ---- 工作台 ----
    {
        "title": "工作台待办提醒",
        "image": "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b",
        "url": "/dashboard/workplace",
        "link_type": "ROUTE",
        "summary": "集中查看任务、告警和审批进展",
        "description": "工作台顶部提示，帮助用户聚焦日常处理事项。",
        "category": "WORKPLACE",
        "type": "HERO",
        "position": "WORKPLACE_TOP",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "团队协作空间",
        "image": "https://images.unsplash.com/photo-1522071820081-009f0129c71c",
        "url": "/dashboard/workplace",
        "link_type": "ROUTE",
        "summary": "与团队成员实时协作、共享资源",
        "description": "工作台顶部辅助 banner，激发团队效率。",
        "category": "WORKPLACE",
        "type": "HERO",
        "position": "WORKPLACE_TOP",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    # ---- 公告 ----
    {
        "title": "版本更新公告",
        "image": "https://images.unsplash.com/photo-1516321318423-f06f85e504b3",
        "url": "/dashboard/workplace",
        "link_type": "ROUTE",
        "summary": "查看近期功能更新和优化说明",
        "description": "用于工作台或公告位展示版本更新。",
        "category": "NOTICE",
        "type": "NOTICE",
        "position": "NOTICE_AREA",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "系统维护计划",
        "image": "https://images.unsplash.com/photo-1519389950473-47ba0277781c",
        "url": "/notice/maintenance",
        "link_type": "ROUTE",
        "summary": "2026年Q3系统升级与维护安排",
        "description": "公告区域展示系统维护计划详情。",
        "category": "NOTICE",
        "type": "NOTICE",
        "position": "NOTICE_AREA",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    {
        "title": "安全合规公告",
        "image": "https://images.unsplash.com/photo-1555949963-aa79dcee981c",
        "url": "/notice/security",
        "link_type": "ROUTE",
        "summary": "数据安全与隐私保护最新政策",
        "description": "安全合规要求更新通知。",
        "category": "NOTICE",
        "type": "NOTICE",
        "position": "NOTICE_AREA",
        "display_scope": "PORTAL",
        "sort": 3,
    },
    # ---- 登录页 ----
    {
        "title": "统一身份认证",
        "image": "https://images.unsplash.com/photo-1519389950473-47ba0277781c",
        "url": "/auth/login",
        "link_type": "ROUTE",
        "summary": "统一入口、统一会话、统一权限校验",
        "description": "登录页身份能力展示 banner。",
        "category": "LOGIN",
        "type": "HERO",
        "position": "LOGIN_SIDE",
        "display_scope": "PORTAL",
        "sort": 1,
    },
    {
        "title": "安全登录提醒",
        "image": "https://images.unsplash.com/photo-1555949963-aa79dcee981c",
        "url": "/auth/login",
        "link_type": "ROUTE",
        "summary": "保护账号安全，定期检查登录设备",
        "description": "登录页侧栏 banner，强调账号安全和审计能力。",
        "category": "LOGIN",
        "type": "HERO",
        "position": "LOGIN_SIDE",
        "display_scope": "PORTAL",
        "sort": 2,
    },
    {
        "title": "企业级权限管控",
        "image": "https://images.unsplash.com/photo-1563986768609-322da13575f3",
        "url": "/auth/login",
        "link_type": "ROUTE",
        "summary": "细粒度权限模型，满足多组织合规要求",
        "description": "登录页品牌能力展示横幅。",
        "category": "LOGIN",
        "type": "HERO",
        "position": "LOGIN_SIDE",
        "display_scope": "PORTAL",
        "sort": 3,
    },
    {
        "title": "多因素认证保障",
        "image": "https://images.unsplash.com/photo-1555949963-aa79dcee981c",
        "url": "/auth/login",
        "link_type": "ROUTE",
        "summary": "支持 TOTP / SMS / 邮件多种二次认证方式",
        "description": "增强登录安全的 MFA 能力介绍。",
        "category": "LOGIN",
        "type": "SIDEBAR",
        "position": "LOGIN_SIDE",
        "display_scope": "PORTAL",
        "sort": 4,
    },
    # ---- 管理端 ----
    {
        "title": "系统运行概览",
        "image": "https://images.unsplash.com/photo-1551288049-bebda4e38f71",
        "url": "/dashboard/analysis",
        "link_type": "ROUTE",
        "summary": "实时监控系统健康度与核心指标",
        "description": "管理端首页顶部提示条，引导管理员关注系统整体状态。",
        "category": "ADMIN_DASHBOARD",
        "type": "HERO",
        "position": "ADMIN_TOP",
        "display_scope": "ADMIN",
        "sort": 1,
    },
    {
        "title": "审计日志待处理",
        "image": "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
        "url": "/log/audit",
        "link_type": "ROUTE",
        "summary": "未处理的异常审计事件",
        "description": "管理端顶部告警 banner，提醒安全审计事项。",
        "category": "ADMIN_DASHBOARD",
        "type": "HERO",
        "position": "ADMIN_TOP",
        "display_scope": "ADMIN",
        "sort": 2,
    },
    {
        "title": "存储空间使用预警",
        "image": "https://images.unsplash.com/photo-1460925895917-afdab827c52f",
        "url": "/file/overview",
        "link_type": "ROUTE",
        "summary": "存储占用已超过 80%，建议及时清理",
        "description": "管理端侧栏告警 banner，提醒存储资源监控。",
        "category": "ADMIN_DASHBOARD",
        "type": "SIDEBAR",
        "position": "ADMIN_SIDEBAR",
        "display_scope": "ADMIN",
        "sort": 1,
    },
    # ---- 移动端 ----
    {
        "title": "移动办公新体验",
        "image": "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c",
        "url": "/mobile",
        "link_type": "ROUTE",
        "summary": "随时随地处理审批和任务",
        "description": "移动端首页引导 banner。",
        "category": "HOME",
        "type": "HERO",
        "position": "HOME_TOP",
        "display_scope": "APP",
        "sort": 1,
    },
    # ---- 系统升级 ----
    {
        "title": "平台版本升级预告",
        "image": "https://images.unsplash.com/photo-1516321318423-f06f85e504b3",
        "url": "/notice/upgrade",
        "link_type": "ROUTE",
        "summary": "v3.0 即将发布，新增权限审计模块",
        "description": "系统升级预告弹窗 banner，引导用户关注新版本。",
        "category": "SYSTEM_UPGRADE",
        "type": "POPUP",
        "position": "HOME_TOP",
        "display_scope": "PORTAL",
        "sort": 1,
    },
]


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _collect_dict_codes() -> list[str]:
    codes: list[str] = []
    for root_code, _, _, children in DICT_GROUPS:
        codes.append(root_code)
        codes.extend(child[0] for child in children)
    return codes


def _delete_banners() -> None:
    """Delete all banners inserted by this seed."""
    # Collect unique (title, display_scope) pairs for deletion.
    # The old seed (0004) used lowercase category/position values
    # so we match on title + display_scope to catch both old and new.
    pairs = {(item["title"], item["display_scope"]) for item in BANNERS}
    for title, display_scope in pairs:
        op.execute(
            sa.text(
                """
                delete from sys_banner
                where title = :title
                  and display_scope = :display_scope
                """
            ).bindparams(title=title, display_scope=display_scope)
        )


def _delete_dicts() -> None:
    """Delete all dict entries by code."""
    codes = _collect_dict_codes()
    stmt = sa.text("delete from sys_dict where code in :codes").bindparams(
        sa.bindparam("codes", expanding=True)
    )
    op.get_bind().execute(stmt, {"codes": codes})


# ---------------------------------------------------------------------------
# Migration
# ---------------------------------------------------------------------------

def upgrade() -> None:
    # Step 1: remove old data (from 0004)
    _delete_banners()
    _delete_dicts()

    # Step 2: insert dict groups
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

    # Step 3: insert banners
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
    _delete_banners()
    _delete_dicts()
