import {
  accountApi,
  bannerApi,
  deptApi,
  fileApi,
  groupApi,
  messageApi,
  positionApi,
  resourceApi,
  resourceModuleApi,
  roleApi,
  sessionApi,
  sysDictApi,
} from '@/api'

export type FieldType =
  | 'text'
  | 'password'
  | 'textarea'
  | 'number'
  | 'switch'
  | 'select'
  | 'radio'
  | 'image'
  | 'datetime'
  | 'hidden'

export interface OptionItem {
  label: string
  value: string
}

export interface FieldConfig {
  prop: string
  label: string
  type?: FieldType
  placeholder?: string
  dictCode?: string
  options?: OptionItem[]
  required?: boolean
  readonly?: boolean
  createOnly?: boolean
  updateOnly?: boolean
  defaultValue?: any
}

export interface GrantActionConfig {
  key: string
  label: string
  icon: string
  permission: string
  mode: 'ids' | 'dept' | 'resource' | 'permission'
  ownApi: string
  grantApi: string
  idField: string
  listField?: string
  selectedField?: string
  targetResource?: ResourceKey
}

export interface RowActionConfig {
  key: string
  label: string
  icon: string
  permission: string
  type:
    | 'detail'
    | 'edit'
    | 'delete'
    | 'grant'
    | 'buttons'
    | 'publish'
    | 'revoke'
    | 'start'
    | 'complete'
    | 'cancel'
  grant?: GrantActionConfig
}

export interface ResourceConfig {
  key: ResourceKey
  title: string
  code: string
  icon: string
  apiPrefix: string
  pagePermission: string
  createPermission?: string
  updatePermission?: string
  deletePermission?: string
  tree?: boolean
  searchFields: FieldConfig[]
  formFields: FieldConfig[]
  detailFields: FieldConfig[]
  cardFields: string[]
  primaryField: string
  descriptionField?: string
  statusField?: string
  actions: RowActionConfig[]
}

export type ResourceKey =
  | 'account'
  | 'role'
  | 'dept'
  | 'group'
  | 'position'
  | 'resource'
  | 'resourceModule'
  | 'dict'
  | 'banner'
  | 'file'
  | 'session'
  | 'notification'
  | 'messageThread'
  | 'messageGroup'
  | 'todo'

export const fallbackDicts: Record<string, OptionItem[]> = {
  COMMON_STATUS: [
    { label: '启用', value: 'ENABLED' },
    { label: '禁用', value: 'DISABLED' },
  ],
  ACCOUNT_TYPE: [
    { label: '管理端', value: 'ADMIN' },
    { label: '门户端', value: 'PORTAL' },
  ],
  ACCOUNT_STATUS: [
    { label: '启用', value: 'ENABLED' },
    { label: '禁用', value: 'DISABLED' },
    { label: '锁定', value: 'LOCKED' },
    { label: '已注销', value: 'CANCELLED' },
  ],
  RESOURCE_TYPE: [
    { label: '目录', value: 'CATALOG' },
    { label: '菜单', value: 'MENU' },
    { label: '页面', value: 'PAGE' },
    { label: '按钮', value: 'BUTTON' },
    { label: '接口', value: 'API' },
  ],
  RESOURCE_MODULE_CLIENT: [
    { label: '管理端', value: 'ADMIN' },
    { label: '门户端', value: 'PORTAL' },
  ],
  DATA_SCOPE: [
    { label: '本人', value: 'SELF' },
    { label: '本部门', value: 'DEPT' },
    { label: '本部门及以下', value: 'DEPT_AND_CHILD' },
    { label: '自定义部门', value: 'CUSTOM_DEPT' },
    { label: '全部', value: 'ALL' },
  ],
  SYS_BIZ_CATEGORY: [
    { label: '系统', value: 'SYSTEM' },
    { label: '业务', value: 'BUSINESS' },
  ],
  ROLE_SCOPE_TYPE: [
    { label: '默认', value: 'DEFAULT' },
    { label: '自定义', value: 'CUSTOM' },
  ],
  BANNER_LINK_TYPE: [
    { label: 'URL', value: 'URL' },
    { label: '无链接', value: 'NONE' },
  ],
  BANNER_CATEGORY: [
    { label: '默认', value: 'DEFAULT' },
    { label: '首页', value: 'HOME' },
  ],
  BANNER_TYPE: [
    { label: '图片', value: 'IMAGE' },
    { label: '公告', value: 'NOTICE' },
  ],
  BANNER_POSITION: [
    { label: '顶部', value: 'TOP' },
    { label: '中部', value: 'MIDDLE' },
    { label: '底部', value: 'BOTTOM' },
  ],
  BANNER_DISPLAY_SCOPE: [
    { label: '全部', value: 'ALL' },
    { label: '管理端', value: 'ADMIN' },
    { label: '门户端', value: 'PORTAL' },
  ],
  NOTIFICATION_SEVERITY: [
    { label: '普通', value: 'INFO' },
    { label: '成功', value: 'SUCCESS' },
    { label: '警告', value: 'WARNING' },
    { label: '错误', value: 'ERROR' },
  ],
  NOTIFICATION_STATUS: [
    { label: '草稿', value: 'DRAFT' },
    { label: '已发布', value: 'PUBLISHED' },
    { label: '已撤回', value: 'REVOKED' },
  ],
  MESSAGE_TARGET_SCOPE: [
    { label: '指定账号', value: 'SPECIFIC' },
    { label: '全部', value: 'ALL' },
    { label: '账号类型', value: 'ACCOUNT_TYPE' },
  ],
  MESSAGE_THREAD_TYPE: [
    { label: '系统', value: 'SYSTEM' },
    { label: '用户', value: 'USER' },
  ],
  TODO_PRIORITY: [
    { label: '低', value: 'LOW' },
    { label: '普通', value: 'NORMAL' },
    { label: '高', value: 'HIGH' },
    { label: '紧急', value: 'URGENT' },
  ],
  TODO_STATUS: [
    { label: '待处理', value: 'PENDING' },
    { label: '进行中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '已取消', value: 'CANCELLED' },
  ],
}

const baseAction = (
  resource: string,
  grants: GrantActionConfig[] = [],
  extra: RowActionConfig[] = []
): RowActionConfig[] => [
  {
    key: 'detail',
    label: '详情',
    icon: 'eye',
    permission: `${resource}:detail`,
    type: 'detail',
  },
  {
    key: 'edit',
    label: '编辑',
    icon: 'edit-pen',
    permission: `${resource}:update`,
    type: 'edit',
  },
  ...grants.map((grant) => ({
    key: grant.key,
    label: grant.label,
    icon: grant.icon,
    permission: grant.permission,
    type: 'grant' as const,
    grant,
  })),
  ...extra,
  {
    key: 'delete',
    label: '删除',
    icon: 'trash',
    permission: `${resource}:delete`,
    type: 'delete',
  },
]

export const resourceConfigs: Record<ResourceKey, ResourceConfig> = {
  account: {
    key: 'account',
    title: '账号管理',
    code: 'account',
    icon: 'account',
    apiPrefix: '/sys/accounts',
    pagePermission: 'iam:account:page',
    createPermission: 'iam:account:create',
    updatePermission: 'iam:account:update',
    deletePermission: 'iam:account:delete',
    primaryField: 'account',
    descriptionField: 'name',
    statusField: 'account_status',
    cardFields: ['account_type', 'phone', 'email', 'updated_at'],
    searchFields: [
      { prop: 'account', label: '账号' },
      { prop: 'name', label: '姓名' },
      { prop: 'phone', label: '手机号' },
      { prop: 'email', label: '邮箱' },
      {
        prop: 'account_type',
        label: '账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
      },
      {
        prop: 'account_status',
        label: '状态',
        type: 'select',
        dictCode: 'ACCOUNT_STATUS',
      },
    ],
    formFields: [
      { prop: 'account', label: '账号', required: true },
      {
        prop: 'password',
        label: '密码',
        type: 'password',
        required: true,
        createOnly: true,
      },
      { prop: 'password', label: '新密码', type: 'password', updateOnly: true },
      {
        prop: 'account_type',
        label: '账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
        required: true,
        defaultValue: 'ADMIN',
      },
      {
        prop: 'account_status',
        label: '状态',
        type: 'select',
        dictCode: 'ACCOUNT_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'name', label: '姓名' },
      { prop: 'nickname', label: '昵称' },
      { prop: 'avatar', label: '头像', type: 'image' },
      { prop: 'signature', label: '签名', type: 'textarea' },
      { prop: 'phone', label: '手机号' },
      {
        prop: 'phone_login_enabled',
        label: '允许手机登录',
        type: 'switch',
        defaultValue: false,
      },
      { prop: 'email', label: '邮箱' },
      {
        prop: 'email_login_enabled',
        label: '允许邮箱登录',
        type: 'switch',
        defaultValue: false,
      },
      { prop: 'employee_no', label: '工号' },
      { prop: 'title', label: '职务' },
      { prop: 'level', label: '级别' },
      { prop: 'bio', label: '简介', type: 'textarea' },
      { prop: 'remark', label: '备注', type: 'textarea' },
    ],
    detailFields: [
      'id',
      'account',
      'account_type',
      'account_status',
      'name',
      'nickname',
      'phone',
      'email',
      'title',
      'employee_no',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('iam:account', [
      {
        key: 'grantRole',
        label: '角色',
        icon: 'tags',
        permission: 'iam:account:grantrole',
        mode: 'ids',
        ownApi: '/sys/accounts/own-role',
        grantApi: '/sys/accounts/grant-role',
        idField: 'role_ids',
        listField: 'roles',
        selectedField: 'role_ids',
        targetResource: 'role',
      },
      {
        key: 'grantGroup',
        label: '用户组',
        icon: 'grid',
        permission: 'iam:account:grantgroup',
        mode: 'ids',
        ownApi: '/sys/accounts/own-group',
        grantApi: '/sys/accounts/grant-group',
        idField: 'group_ids',
        listField: 'groups',
        selectedField: 'group_ids',
        targetResource: 'group',
      },
      {
        key: 'grantDept',
        label: '部门',
        icon: 'map',
        permission: 'iam:account:grantdept',
        mode: 'dept',
        ownApi: '/sys/accounts/own-dept',
        grantApi: '/sys/accounts/grant-dept',
        idField: 'dept_ids',
        targetResource: 'dept',
      },
      {
        key: 'grantResource',
        label: '资源',
        icon: 'list-dot',
        permission: 'iam:account:grantresource',
        mode: 'resource',
        ownApi: '/sys/accounts/own-resource',
        grantApi: '/sys/accounts/grant-resource',
        idField: 'resource_ids',
      },
    ]),
  },
  role: {
    key: 'role',
    title: '角色管理',
    code: 'role',
    icon: 'tags',
    apiPrefix: '/sys/roles',
    pagePermission: 'iam:role:page',
    createPermission: 'iam:role:create',
    updatePermission: 'iam:role:update',
    deletePermission: 'iam:role:delete',
    primaryField: 'name',
    descriptionField: 'code',
    statusField: 'status',
    cardFields: ['category', 'scope_type', 'sort', 'updated_at'],
    searchFields: [
      { prop: 'name', label: '角色名称' },
      { prop: 'code', label: '角色编码' },
      {
        prop: 'category',
        label: '分类',
        type: 'select',
        dictCode: 'SYS_BIZ_CATEGORY',
      },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      { prop: 'code', label: '角色编码', required: true },
      { prop: 'name', label: '角色名称', required: true },
      {
        prop: 'category',
        label: '分类',
        type: 'select',
        dictCode: 'SYS_BIZ_CATEGORY',
      },
      {
        prop: 'scope_type',
        label: '范围类型',
        type: 'select',
        dictCode: 'ROLE_SCOPE_TYPE',
      },
      { prop: 'owner_dept_id', label: '所属部门 ID' },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 99 },
      {
        prop: 'is_builtin',
        label: '内置角色',
        type: 'switch',
        defaultValue: false,
      },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'description', label: '描述', type: 'textarea' },
    ],
    detailFields: [
      'id',
      'code',
      'name',
      'category',
      'scope_type',
      'owner_dept_id',
      'sort',
      'is_builtin',
      'status',
      'description',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('iam:role', [
      {
        key: 'grantUser',
        label: '用户',
        icon: 'account',
        permission: 'iam:role:grantuser',
        mode: 'ids',
        ownApi: '/sys/roles/own-user',
        grantApi: '/sys/roles/grant-user',
        idField: 'account_ids',
        listField: 'users',
        selectedField: 'account_ids',
        targetResource: 'account',
      },
      {
        key: 'grantResource',
        label: '资源',
        icon: 'list-dot',
        permission: 'iam:role:grantresource',
        mode: 'resource',
        ownApi: '/sys/roles/own-resource',
        grantApi: '/sys/roles/grant-resource',
        idField: 'resource_ids',
      },
    ]),
  },
  dept: simpleOrgConfig(
    'dept',
    '部门管理',
    'iam:dept',
    '/sys/depts',
    'map',
    [
      { prop: 'name', label: '部门名称', required: true },
      { prop: 'code', label: '部门编码', required: true },
      {
        prop: 'category',
        label: '部门分类',
        type: 'select',
        dictCode: 'DEPT_CATEGORY',
        required: true,
      },
      { prop: 'parent_id', label: '上级部门 ID' },
      { prop: 'master_id', label: '负责人 ID' },
      { prop: 'deputy_master_id', label: '副负责人 ID' },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 99 },
      {
        prop: 'is_virtual',
        label: '虚拟部门',
        type: 'switch',
        defaultValue: false,
      },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
    ],
    true
  ),
  group: {
    key: 'group',
    title: '用户组管理',
    code: 'group',
    icon: 'grid',
    apiPrefix: '/sys/groups',
    pagePermission: 'iam:group:page',
    createPermission: 'iam:group:create',
    updatePermission: 'iam:group:update',
    deletePermission: 'iam:group:delete',
    primaryField: 'name',
    descriptionField: 'description',
    statusField: 'status',
    cardFields: ['owner_dept_id', 'updated_at'],
    searchFields: [
      { prop: 'name', label: '用户组名称' },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      { prop: 'name', label: '用户组名称', required: true },
      { prop: 'owner_dept_id', label: '所属部门 ID' },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'description', label: '描述', type: 'textarea' },
    ],
    detailFields: [
      'id',
      'name',
      'owner_dept_id',
      'status',
      'description',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('iam:group', [
      {
        key: 'grantUser',
        label: '用户',
        icon: 'account',
        permission: 'iam:group:grantuser',
        mode: 'ids',
        ownApi: '/sys/groups/own-user',
        grantApi: '/sys/groups/grant-user',
        idField: 'account_ids',
        listField: 'users',
        selectedField: 'account_ids',
        targetResource: 'account',
      },
      {
        key: 'grantRole',
        label: '角色',
        icon: 'tags',
        permission: 'iam:group:grantrole',
        mode: 'ids',
        ownApi: '/sys/groups/own-role',
        grantApi: '/sys/groups/grant-role',
        idField: 'role_ids',
        listField: 'roles',
        selectedField: 'role_ids',
        targetResource: 'role',
      },
      {
        key: 'grantResource',
        label: '资源',
        icon: 'list-dot',
        permission: 'iam:group:grantresource',
        mode: 'resource',
        ownApi: '/sys/groups/own-resource',
        grantApi: '/sys/groups/grant-resource',
        idField: 'resource_ids',
      },
    ]),
  },
  position: simpleOrgConfig(
    'position',
    '岗位管理',
    'iam:position',
    '/sys/positions',
    'bookmark',
    [
      { prop: 'name', label: '岗位名称', required: true },
      { prop: 'code', label: '岗位编码', required: true },
      {
        prop: 'category',
        label: '岗位分类',
        type: 'select',
        dictCode: 'POSITION_CATEGORY',
        required: true,
      },
      { prop: 'owner_dept_id', label: '所属部门 ID' },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 99 },
      {
        prop: 'is_virtual',
        label: '虚拟岗位',
        type: 'switch',
        defaultValue: false,
      },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'description', label: '描述', type: 'textarea' },
    ]
  ),
  resource: {
    key: 'resource',
    title: '资源管理',
    code: 'resource',
    icon: 'list-dot',
    apiPrefix: '/sys/resources',
    pagePermission: 'iam:resource:page',
    createPermission: 'iam:resource:create',
    updatePermission: 'iam:resource:update',
    deletePermission: 'iam:resource:delete',
    tree: true,
    primaryField: 'name',
    descriptionField: 'code',
    statusField: 'status',
    cardFields: ['resource_type', 'module_id_name', 'path', 'color', 'sort'],
    searchFields: [
      { prop: 'code', label: '资源编码' },
      { prop: 'name', label: '资源名称' },
      {
        prop: 'resource_type',
        label: '资源类型',
        type: 'select',
        dictCode: 'RESOURCE_TYPE',
      },
      {
        prop: 'module_client',
        label: '客户端',
        type: 'select',
        dictCode: 'RESOURCE_MODULE_CLIENT',
      },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      { prop: 'name', label: '资源名称', required: true },
      { prop: 'code', label: '资源编码', required: true },
      {
        prop: 'resource_type',
        label: '资源类型',
        type: 'select',
        dictCode: 'RESOURCE_TYPE',
        required: true,
      },
      { prop: 'parent_id', label: '父资源 ID' },
      { prop: 'module_id', label: '模块 ID' },
      { prop: 'path', label: '路径' },
      { prop: 'component', label: '组件' },
      { prop: 'redirect', label: '重定向' },
      { prop: 'icon', label: '图标' },
      { prop: 'color', label: '颜色' },
      { prop: 'href', label: '外链' },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 99 },
      {
        prop: 'is_visible',
        label: '显示菜单',
        type: 'switch',
        defaultValue: true,
      },
      {
        prop: 'is_cache',
        label: '缓存页面',
        type: 'switch',
        defaultValue: false,
      },
      {
        prop: 'is_affix',
        label: '固定页签',
        type: 'switch',
        defaultValue: false,
      },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'description', label: '描述', type: 'textarea' },
    ],
    detailFields: [
      'id',
      'module_id_name',
      'code',
      'name',
      'resource_type',
      'parent_id',
      'path',
      'component',
      'redirect',
      'icon',
      'color',
      'href',
      'sort',
      'is_visible',
      'is_cache',
      'is_affix',
      'status',
      'description',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction(
      'iam:resource',
      [],
      [
        {
          key: 'buttons',
          label: '按钮权限',
          icon: 'fingerprint',
          permission: 'iam:resource:grant',
          type: 'buttons',
        },
      ]
    ),
  },
  resourceModule: {
    key: 'resourceModule',
    title: '资源模块',
    code: 'resource_module',
    icon: 'grid-fill',
    apiPrefix: '/sys/resource-modules',
    pagePermission: 'iam:resourcemodule:page',
    createPermission: 'iam:resourcemodule:create',
    updatePermission: 'iam:resourcemodule:update',
    deletePermission: 'iam:resourcemodule:delete',
    primaryField: 'name',
    descriptionField: 'code',
    statusField: 'status',
    cardFields: ['client', 'color', 'sort', 'updated_at'],
    searchFields: [
      { prop: 'name', label: '模块名称' },
      { prop: 'code', label: '模块编码' },
      {
        prop: 'client',
        label: '客户端',
        type: 'select',
        dictCode: 'RESOURCE_MODULE_CLIENT',
      },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      { prop: 'name', label: '模块名称', required: true },
      { prop: 'code', label: '模块编码', required: true },
      {
        prop: 'client',
        label: '客户端',
        type: 'select',
        dictCode: 'RESOURCE_MODULE_CLIENT',
        required: true,
        defaultValue: 'ADMIN',
      },
      { prop: 'icon', label: '图标' },
      { prop: 'color', label: '颜色' },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 99 },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'description', label: '描述', type: 'textarea' },
    ],
    detailFields: [
      'id',
      'name',
      'code',
      'client',
      'icon',
      'color',
      'sort',
      'status',
      'description',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('iam:resourcemodule'),
  },
  dict: {
    key: 'dict',
    title: '字典管理',
    code: 'dict',
    icon: 'order',
    apiPrefix: '/sys/dicts',
    pagePermission: 'sys:dict:page',
    createPermission: 'sys:dict:create',
    updatePermission: 'sys:dict:update',
    deletePermission: 'sys:dict:delete',
    tree: true,
    primaryField: 'label',
    descriptionField: 'code',
    statusField: 'status',
    cardFields: ['value', 'category', 'sort', 'updated_at'],
    searchFields: [
      { prop: 'code', label: '编码' },
      {
        prop: 'category',
        label: '分类',
        type: 'select',
        dictCode: 'SYS_BIZ_CATEGORY',
      },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      {
        prop: 'category',
        label: '分类',
        type: 'select',
        dictCode: 'SYS_BIZ_CATEGORY',
      },
      { prop: 'parent_id', label: '父字典 ID' },
      { prop: 'code', label: '编码', required: true },
      { prop: 'label', label: '标签', required: true },
      { prop: 'value', label: '值' },
      { prop: 'color', label: '颜色' },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 0 },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
    ],
    detailFields: [
      'id',
      'code',
      'label',
      'value',
      'color',
      'category',
      'parent_id_name',
      'sort',
      'status',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('sys:dict'),
  },
  banner: {
    key: 'banner',
    title: '展示图管理',
    code: 'banner',
    icon: 'photo',
    apiPrefix: '/sys/banners',
    pagePermission: 'sys:banner:page',
    createPermission: 'sys:banner:create',
    updatePermission: 'sys:banner:update',
    deletePermission: 'sys:banner:delete',
    primaryField: 'title',
    descriptionField: 'summary',
    statusField: 'status',
    cardFields: ['category', 'type', 'position', 'display_scope'],
    searchFields: [
      {
        prop: 'category',
        label: '分类',
        type: 'select',
        dictCode: 'BANNER_CATEGORY',
      },
      { prop: 'type', label: '类型', type: 'select', dictCode: 'BANNER_TYPE' },
      {
        prop: 'position',
        label: '位置',
        type: 'select',
        dictCode: 'BANNER_POSITION',
      },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      { prop: 'title', label: '标题', required: true },
      { prop: 'image', label: '图片', type: 'image', required: true },
      { prop: 'url', label: '目标 URL' },
      {
        prop: 'link_type',
        label: '链接类型',
        type: 'select',
        dictCode: 'BANNER_LINK_TYPE',
        defaultValue: 'URL',
      },
      {
        prop: 'category',
        label: '分类',
        type: 'select',
        dictCode: 'BANNER_CATEGORY',
        required: true,
      },
      {
        prop: 'type',
        label: '类型',
        type: 'select',
        dictCode: 'BANNER_TYPE',
        required: true,
      },
      {
        prop: 'position',
        label: '位置',
        type: 'select',
        dictCode: 'BANNER_POSITION',
        required: true,
      },
      {
        prop: 'display_scope',
        label: '展示范围',
        type: 'select',
        dictCode: 'BANNER_DISPLAY_SCOPE',
        required: true,
      },
      { prop: 'sort', label: '排序', type: 'number', defaultValue: 0 },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
      { prop: 'start_at', label: '开始时间', type: 'datetime' },
      { prop: 'end_at', label: '结束时间', type: 'datetime' },
      { prop: 'summary', label: '摘要' },
      { prop: 'description', label: '描述', type: 'textarea' },
    ],
    detailFields: [
      'id',
      'title',
      'image',
      'url',
      'link_type',
      'category',
      'type',
      'position',
      'display_scope',
      'sort',
      'status',
      'start_at',
      'end_at',
      'summary',
      'description',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('sys:banner'),
  },
  file: {
    key: 'file',
    title: '文件管理',
    code: 'file',
    icon: 'file-text',
    apiPrefix: '/sys/file',
    pagePermission: 'sys:file:page',
    updatePermission: 'sys:file:update',
    deletePermission: 'sys:file:delete',
    primaryField: 'original_name',
    descriptionField: 'object_name',
    cardFields: ['storage_provider', 'content_type', 'size', 'updated_at'],
    searchFields: [
      { prop: 'original_name', label: '文件名' },
      { prop: 'object_name', label: '对象名' },
      { prop: 'content_type', label: '内容类型' },
    ],
    formFields: [{ prop: 'original_name', label: '文件名', required: true }],
    detailFields: [
      'id',
      'original_name',
      'object_name',
      'storage_provider',
      'bucket',
      'content_type',
      'size',
      'url',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('sys:file').filter(
      (item) => item.type !== 'edit' || item.permission === 'sys:file:update'
    ),
  },
  session: {
    key: 'session',
    title: '会话管理',
    code: 'session',
    icon: 'clock',
    apiPrefix: '/auth/sessions',
    pagePermission: 'auth:session:page',
    primaryField: 'account',
    descriptionField: 'device_label',
    cardFields: ['account_type', 'client_ip', 'created_at', 'updated_at'],
    searchFields: [
      { prop: 'account_id', label: '账号 ID' },
      {
        prop: 'account_type',
        label: '账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
      },
    ],
    formFields: [],
    detailFields: [
      'id',
      'account_id',
      'account_type',
      'token_hash',
      'client_ip',
      'user_agent',
      'device_label',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: [
      {
        key: 'detail',
        label: '详情',
        icon: 'eye',
        permission: 'auth:session:detail',
        type: 'detail',
      },
    ],
  },
  notification: {
    key: 'notification',
    title: '通知管理',
    code: 'notification',
    icon: 'bell',
    apiPrefix: '/message/notifications',
    pagePermission: 'message:notification:page',
    createPermission: 'message:notification:create',
    updatePermission: 'message:notification:update',
    deletePermission: 'message:notification:delete',
    primaryField: 'title',
    descriptionField: 'content',
    statusField: 'status',
    cardFields: ['severity', 'target_scope', 'publish_at', 'updated_at'],
    searchFields: [
      { prop: 'title', label: '标题' },
      {
        prop: 'severity',
        label: '级别',
        type: 'select',
        dictCode: 'NOTIFICATION_SEVERITY',
      },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'NOTIFICATION_STATUS',
      },
    ],
    formFields: [
      { prop: 'title', label: '标题', required: true },
      { prop: 'content', label: '内容', type: 'textarea', required: true },
      {
        prop: 'severity',
        label: '级别',
        type: 'select',
        dictCode: 'NOTIFICATION_SEVERITY',
        defaultValue: 'INFO',
      },
      {
        prop: 'target_scope',
        label: '目标范围',
        type: 'select',
        dictCode: 'MESSAGE_TARGET_SCOPE',
        defaultValue: 'SPECIFIC',
      },
      {
        prop: 'target_account_type',
        label: '目标账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
      },
      { prop: 'target_account_id', label: '目标账号 ID' },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'NOTIFICATION_STATUS',
        defaultValue: 'DRAFT',
      },
    ],
    detailFields: [
      'id',
      'title',
      'content',
      'severity',
      'target_scope',
      'target_account_type',
      'target_account_id',
      'status',
      'publish_at',
      'revoked_at',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction(
      'message:notification',
      [],
      [
        {
          key: 'publish',
          label: '发布',
          icon: 'play-right',
          permission: 'message:notification:publish',
          type: 'publish',
        },
        {
          key: 'revoke',
          label: '撤回',
          icon: 'pause',
          permission: 'message:notification:revoke',
          type: 'revoke',
        },
      ]
    ),
  },
  messageThread: {
    key: 'messageThread',
    title: '站内信',
    code: 'message',
    icon: 'chat',
    apiPrefix: '/message/threads',
    pagePermission: 'message:thread:page',
    primaryField: 'title',
    descriptionField: 'last_message',
    statusField: 'status',
    cardFields: [
      'thread_type',
      'unread_count',
      'last_message_at',
      'updated_at',
    ],
    searchFields: [
      {
        prop: 'thread_type',
        label: '类型',
        type: 'select',
        dictCode: 'MESSAGE_THREAD_TYPE',
      },
    ],
    formFields: [
      { prop: 'title', label: '主题', required: true },
      { prop: 'content', label: '内容', type: 'textarea', required: true },
      {
        prop: 'target_account_type',
        label: '目标账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
      },
      { prop: 'target_account_id', label: '目标账号 ID' },
    ],
    detailFields: [
      'id',
      'title',
      'thread_type',
      'status',
      'unread_count',
      'last_message_at',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: [
      {
        key: 'detail',
        label: '详情',
        icon: 'eye',
        permission: 'message:thread:detail',
        type: 'detail',
      },
    ],
  },
  messageGroup: {
    key: 'messageGroup',
    title: '消息组',
    code: 'message_group',
    icon: 'account-fill',
    apiPrefix: '/message/groups',
    pagePermission: 'message:group:page',
    createPermission: 'message:group:create',
    updatePermission: 'message:group:update',
    deletePermission: 'message:group:delete',
    primaryField: 'name',
    descriptionField: 'description',
    statusField: 'status',
    cardFields: ['member_count', 'updated_at'],
    searchFields: [
      { prop: 'name', label: '组名' },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields: [
      { prop: 'name', label: '组名', required: true },
      { prop: 'description', label: '描述', type: 'textarea' },
      {
        prop: 'status',
        label: '状态',
        type: 'radio',
        dictCode: 'COMMON_STATUS',
        defaultValue: 'ENABLED',
      },
    ],
    detailFields: [
      'id',
      'name',
      'description',
      'status',
      'member_count',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction('message:group'),
  },
  todo: {
    key: 'todo',
    title: '待办管理',
    code: 'todo',
    icon: 'checkbox-mark',
    apiPrefix: '/message/todos',
    pagePermission: 'message:todo:page',
    createPermission: 'message:todo:create',
    updatePermission: 'message:todo:update',
    deletePermission: 'message:todo:delete',
    primaryField: 'title',
    descriptionField: 'content',
    statusField: 'status',
    cardFields: ['priority', 'target_scope', 'due_at', 'updated_at'],
    searchFields: [
      { prop: 'title', label: '标题' },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'TODO_STATUS',
      },
      {
        prop: 'target_account_type',
        label: '账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
      },
    ],
    formFields: [
      { prop: 'title', label: '标题', required: true },
      { prop: 'content', label: '内容', type: 'textarea' },
      {
        prop: 'priority',
        label: '优先级',
        type: 'select',
        dictCode: 'TODO_PRIORITY',
        defaultValue: 'NORMAL',
      },
      {
        prop: 'target_scope',
        label: '目标范围',
        type: 'select',
        dictCode: 'MESSAGE_TARGET_SCOPE',
        defaultValue: 'SPECIFIC',
      },
      {
        prop: 'target_account_type',
        label: '目标账号类型',
        type: 'select',
        dictCode: 'ACCOUNT_TYPE',
      },
      { prop: 'target_account_id', label: '目标账号 ID' },
      { prop: 'due_at', label: '截止时间', type: 'datetime' },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'TODO_STATUS',
        defaultValue: 'PENDING',
      },
    ],
    detailFields: [
      'id',
      'title',
      'content',
      'priority',
      'target_scope',
      'target_account_type',
      'target_account_id',
      'status',
      'assignee_status',
      'due_at',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction(
      'message:todo',
      [],
      [
        {
          key: 'start',
          label: '开始',
          icon: 'play-right',
          permission: 'message:todo:update',
          type: 'start',
        },
        {
          key: 'complete',
          label: '完成',
          icon: 'checkbox-mark',
          permission: 'message:todo:update',
          type: 'complete',
        },
        {
          key: 'cancel',
          label: '取消',
          icon: 'close-circle',
          permission: 'message:todo:cancel',
          type: 'cancel',
        },
      ]
    ),
  },
}

export const resourceCodeMap: Record<string, ResourceKey> = {
  account: 'account',
  role: 'role',
  dept: 'dept',
  group: 'group',
  position: 'position',
  resource: 'resource',
  resource_module: 'resourceModule',
  resourcemodule: 'resourceModule',
  dict: 'dict',
  banner: 'banner',
  file: 'file',
  session: 'session',
  notification: 'notification',
  message: 'messageThread',
  thread: 'messageThread',
  message_group: 'messageGroup',
  group_message: 'messageGroup',
  todo: 'todo',
}

function detailLabel(prop: string) {
  const labels: Record<string, string> = {
    id: 'ID',
    code: '编码',
    name: '名称',
    label: '标签',
    value: '值',
    account: '账号',
    account_type: '账号类型',
    account_status: '账号状态',
    phone: '手机号',
    email: '邮箱',
    status: '状态',
    title: '标题',
    content: '内容',
    description: '描述',
    created_at: '创建时间',
    updated_at: '更新时间',
  }
  return labels[prop] ?? prop.replace(/_/g, ' ')
}

function simpleOrgConfig(
  key: ResourceKey,
  title: string,
  permissionPrefix: string,
  apiPrefix: string,
  icon: string,
  formFields: FieldConfig[],
  tree = false
): ResourceConfig {
  return {
    key,
    title,
    code: key,
    icon,
    apiPrefix,
    pagePermission: `${permissionPrefix}:page`,
    createPermission: `${permissionPrefix}:create`,
    updatePermission: `${permissionPrefix}:update`,
    deletePermission: `${permissionPrefix}:delete`,
    tree,
    primaryField: 'name',
    descriptionField: 'code',
    statusField: 'status',
    cardFields: ['category', 'sort', 'is_virtual', 'updated_at'],
    searchFields: [
      { prop: 'name', label: '名称' },
      { prop: 'code', label: '编码' },
      { prop: 'category', label: '分类' },
      {
        prop: 'status',
        label: '状态',
        type: 'select',
        dictCode: 'COMMON_STATUS',
      },
    ],
    formFields,
    detailFields: [
      'id',
      'name',
      'code',
      'category',
      'parent_id',
      'owner_dept_id',
      'sort',
      'is_virtual',
      'status',
      'description',
      'created_at',
      'updated_at',
    ].map((prop) => ({ prop, label: detailLabel(prop) })),
    actions: baseAction(permissionPrefix),
  }
}

export const adminResourceApis = {
  account: accountApi,
  role: roleApi,
  dept: deptApi,
  group: groupApi,
  position: positionApi,
  resource: resourceApi,
  resourceModule: resourceModuleApi,
  dict: sysDictApi,
  banner: bannerApi,
  file: fileApi,
  session: sessionApi,
  notification: {
    page: messageApi.notificationPage,
    detail: messageApi.notificationDetail,
    create: messageApi.createNotification,
    update: messageApi.updateNotification,
    remove: messageApi.removeNotification,
  },
  messageThread: {
    page: messageApi.threadPage,
    detail: (params: any) =>
      messageApi.threadMessage({ thread_id: params.id, current: 1, size: 20 }),
    create: messageApi.sendSystemMessage,
  },
  messageGroup: {
    page: messageApi.groupPage,
    detail: messageApi.groupDetail,
    create: messageApi.createGroup,
    update: messageApi.updateGroup,
    remove: messageApi.removeGroup,
  },
  todo: {
    page: messageApi.todoPage,
    detail: messageApi.todoDetail,
    create: messageApi.createTodo,
    update: messageApi.updateTodo,
    remove: messageApi.removeTodo,
  },
} as const
