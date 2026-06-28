import { http } from '@/utils'

const rolePrefix = '/api/v1/admin/sys/roles'

export function page(params: any) {
  return http.get<any>(`${rolePrefix}/page`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${rolePrefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${rolePrefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${rolePrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${rolePrefix}/delete`, data)
}

const mockResourceModules = [
  {
    id: 'admin',
    title: 'Admin',
    menu: [
      {
        id: 'dashboard-workplace',
        module: 'admin',
        parentId: 'dashboard',
        parentName: 'Dashboard',
        title: '工作台',
        button: [
          { id: 'dashboard:view', title: '查看' },
          { id: 'dashboard:export', title: '导出' },
        ],
      },
      {
        id: 'iam-role',
        module: 'admin',
        parentId: 'iam',
        parentName: 'IAM',
        title: '角色管理',
        button: [
          { id: 'iam:role:page', title: '查询' },
          { id: 'iam:role:create', title: '新增' },
          { id: 'iam:role:update', title: '编辑' },
          { id: 'iam:role:delete', title: '删除' },
        ],
      },
      {
        id: 'iam-account',
        module: 'admin',
        parentId: 'iam',
        parentName: 'IAM',
        title: '账号管理',
        button: [
          { id: 'iam:account:page', title: '查询' },
          { id: 'iam:account:grantrole', title: '授权角色' },
        ],
      },
    ],
  },
  {
    id: 'system',
    title: 'System',
    menu: [
      {
        id: 'sys-dict',
        module: 'system',
        parentId: 'sys',
        parentName: 'System',
        title: '字典管理',
        button: [
          { id: 'sys:dict:page', title: '查询' },
          { id: 'sys:dict:create', title: '新增' },
          { id: 'sys:dict:update', title: '编辑' },
        ],
      },
      {
        id: 'sys-banner',
        module: 'system',
        parentId: 'sys',
        parentName: 'System',
        title: 'Banner 管理',
        button: [
          { id: 'sys:banner:page', title: '查询' },
          { id: 'sys:banner:create', title: '新增' },
        ],
      },
    ],
  },
]

const mockResourceGrants = [
  {
    menuId: 'iam-role',
    buttonInfo: ['iam:role:page', 'iam:role:update'],
  },
  {
    menuId: 'sys-dict',
    buttonInfo: ['sys:dict:page'],
  },
]

const mockPermissionApis = [
  '/api/v1/admin/sys/roles/page[角色分页]',
  '/api/v1/admin/sys/roles/detail[角色详情]',
  '/api/v1/admin/sys/accounts/page[账号分页]',
  '/api/v1/admin/sys/dicts/page[字典分页]',
  '/api/v1/admin/file/page[文件分页]',
]

const mockPermissionGrants = [
  {
    apiUrl: '/api/v1/admin/sys/roles/page',
    scopeCategory: 'SCOPE_ALL',
    scopeDefineOrgIdList: [],
  },
  {
    apiUrl: '/api/v1/admin/sys/roles/detail',
    scopeCategory: 'SCOPE_SELF',
    scopeDefineOrgIdList: [],
  },
]

const mockPermissionKeys = ['iam:role:page', 'iam:role:detail']
const mockAccountIds = ['1']

const mockPermissions = [
  {
    permission_key: 'iam:role:page',
    module: 'iam',
    methods: ['GET'],
    path: '/api/v1/admin/sys/roles/page',
    data_scope: 'SCOPE_ALL',
  },
  {
    permission_key: 'iam:role:detail',
    module: 'iam',
    methods: ['GET'],
    path: '/api/v1/admin/sys/roles/detail',
    data_scope: 'SCOPE_SELF',
  },
  {
    permission_key: 'iam:account:page',
    module: 'iam',
    methods: ['GET'],
    path: '/api/v1/admin/sys/accounts/page',
    data_scope: 'SCOPE_ORG_CHILD',
  },
  {
    permission_key: 'sys:dict:page',
    module: 'sys',
    methods: ['GET'],
    path: '/api/v1/admin/sys/dicts/page',
    data_scope: 'SCOPE_ALL',
  },
]

const mockUsers = [
  {
    id: '1',
    account: 'superadmin',
    name: 'Administrator',
    org_id: 'platform',
    avatar: '',
    account_type: 'ADMIN',
    account_status: 'ENABLED',
  },
  {
    id: '2',
    account: 'operator',
    name: 'Operator',
    org_id: 'platform',
    avatar: '',
    account_type: 'ADMIN',
    account_status: 'ENABLED',
  },
  {
    id: '3',
    account: 'portal_user',
    name: 'Portal User',
    org_id: 'portal',
    avatar: '',
    account_type: 'PORTAL',
    account_status: 'DISABLED',
  },
]

export function ownResources(roleId: string) {
  return Promise.resolve({
    data: {
      roleId,
      modules: mockResourceModules,
      grantInfoList: mockResourceGrants,
    },
  })
}

export function grantResources(data: any) {
  return Promise.resolve({ data })
}

export function ownPermissions(roleId: string) {
  return Promise.resolve({
    data: {
      roleId,
      permissions: mockPermissions,
      apis: mockPermissionApis,
      grantInfoList: mockPermissionGrants,
      permissionKeys: mockPermissionKeys,
      dataScopeMap: Object.fromEntries(
        mockPermissions.map((item) => [item.permission_key, item.data_scope]),
      ),
    },
  })
}

export function grantPermissions(data: any) {
  return Promise.resolve({ data })
}

export function ownUsers(roleId: string) {
  return Promise.resolve({
    data: {
      roleId,
      users: mockUsers,
      accountIds: mockAccountIds,
    },
  })
}

export function grantUsers(data: any) {
  return Promise.resolve({ data })
}
