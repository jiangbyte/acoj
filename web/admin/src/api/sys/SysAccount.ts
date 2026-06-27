const accounts: any[] = [
  {
    id: '10001',
    account: 'super',
    password_hash: 'mock-hash-super',
    account_type: 'ADMIN',
    account_status: 'ENABLED',
    name: '超级管理员',
    nickname: 'Super',
    avatar: '',
    signature: '拥有系统全部权限',
    phone: '13800000001',
    email: 'super@acoj.dev',
    is_superuser: true,
    cancelled_at: null,
    cancelled_by: null,
    cancel_reason: null,
    last_login_ip: '127.0.0.1',
    last_login_address: '本机',
    last_login_time: '2026-06-18 14:20:00',
    last_login_device: 'Chrome / Windows',
    latest_login_ip: '127.0.0.1',
    latest_login_address: '本机',
    latest_login_time: '2026-06-27 09:12:30',
    latest_login_device: 'Chrome / Windows',
    created_at: '2026-01-12 09:20:00',
    created_by: 'system',
    updated_at: '2026-06-27 09:12:30',
    updated_by: 'super',
  },
  {
    id: '10002',
    account: 'admin',
    password_hash: 'mock-hash-admin',
    account_type: 'ADMIN',
    account_status: 'ENABLED',
    name: '系统管理员',
    nickname: 'Admin',
    avatar: '',
    signature: '负责系统基础配置',
    phone: '13800000002',
    email: 'admin@acoj.dev',
    is_superuser: false,
    cancelled_at: null,
    cancelled_by: null,
    cancel_reason: null,
    last_login_ip: '10.1.0.12',
    last_login_address: '浙江杭州',
    last_login_time: '2026-06-17 16:22:08',
    last_login_device: 'Edge / Windows',
    latest_login_ip: '10.1.0.15',
    latest_login_address: '浙江杭州',
    latest_login_time: '2026-06-26 18:35:42',
    latest_login_device: 'Edge / Windows',
    created_at: '2026-02-03 10:12:30',
    created_by: 'super',
    updated_at: '2026-06-26 18:35:42',
    updated_by: 'admin',
  },
  {
    id: '10003',
    account: 'alice',
    password_hash: 'mock-hash-alice',
    account_type: 'PORTAL',
    account_status: 'ENABLED',
    name: 'Alice',
    nickname: 'Alice',
    avatar: '',
    signature: '保持练习',
    phone: '13800000003',
    email: 'alice@example.com',
    is_superuser: false,
    cancelled_at: null,
    cancelled_by: null,
    cancel_reason: null,
    last_login_ip: '172.16.4.21',
    last_login_address: '上海',
    last_login_time: '2026-06-12 20:41:36',
    last_login_device: 'Safari / macOS',
    latest_login_ip: '172.16.4.21',
    latest_login_address: '上海',
    latest_login_time: '2026-06-25 21:04:18',
    latest_login_device: 'Safari / macOS',
    created_at: '2026-03-01 08:30:00',
    created_by: 'admin',
    updated_at: '2026-06-25 21:04:18',
    updated_by: 'admin',
  },
  {
    id: '10004',
    account: 'bob',
    password_hash: 'mock-hash-bob',
    account_type: 'PORTAL',
    account_status: 'DISABLED',
    name: 'Bob',
    nickname: 'Bob',
    avatar: '',
    signature: '',
    phone: '13800000004',
    email: 'bob@example.com',
    is_superuser: false,
    cancelled_at: null,
    cancelled_by: null,
    cancel_reason: null,
    last_login_ip: '172.16.5.31',
    last_login_address: '江苏南京',
    last_login_time: '2026-05-30 10:26:18',
    last_login_device: 'Chrome / Android',
    latest_login_ip: '172.16.5.31',
    latest_login_address: '江苏南京',
    latest_login_time: '2026-06-01 08:42:05',
    latest_login_device: 'Chrome / Android',
    created_at: '2026-03-15 15:27:51',
    created_by: 'admin',
    updated_at: '2026-06-01 08:42:05',
    updated_by: 'admin',
  },
  {
    id: '10005',
    account: 'carol',
    password_hash: 'mock-hash-carol',
    account_type: 'PORTAL',
    account_status: 'CANCELLED',
    name: 'Carol',
    nickname: 'Carol',
    avatar: '',
    signature: '账号已注销',
    phone: '13800000005',
    email: 'carol@example.com',
    is_superuser: false,
    cancelled_at: '2026-06-10 11:00:00',
    cancelled_by: 'admin',
    cancel_reason: '用户主动申请注销',
    last_login_ip: '172.16.6.41',
    last_login_address: '广东深圳',
    last_login_time: '2026-05-21 19:12:27',
    last_login_device: 'Firefox / Linux',
    latest_login_ip: '172.16.6.41',
    latest_login_address: '广东深圳',
    latest_login_time: '2026-05-21 19:12:27',
    latest_login_device: 'Firefox / Linux',
    created_at: '2026-04-11 11:16:32',
    created_by: 'admin',
    updated_at: '2026-06-10 11:00:00',
    updated_by: 'admin',
  },
]

export async function page(params: any) {
  const current = Number(params?.current ?? 1)
  const size = Number(params?.size ?? 20)
  const records = filterAccounts(params)
  const start = (current - 1) * size

  return {
    data: {
      records: records.slice(start, start + size).map(cloneAccount),
      total: records.length,
      current,
      size,
    },
  }
}

export async function detail(params: any) {
  const account = accounts.find((item) => item.id === params?.id)
  return {
    data: account ? cloneAccount(account) : null,
  }
}

export async function create(data: any) {
  const now = formatDateTime(new Date())
  const account = {
    id: String(Date.now()),
    account: data.account,
    password_hash: createPasswordHash(data.password),
    account_type: data.account_type,
    account_status: data.account_status,
    name: data.name,
    nickname: data.nickname ?? null,
    avatar: data.avatar ?? null,
    signature: data.signature ?? null,
    phone: data.phone ?? null,
    email: data.email ?? null,
    is_superuser: Boolean(data.is_superuser),
    cancelled_at: null,
    cancelled_by: null,
    cancel_reason: null,
    last_login_ip: null,
    last_login_address: null,
    last_login_time: null,
    last_login_device: null,
    latest_login_ip: null,
    latest_login_address: null,
    latest_login_time: null,
    latest_login_device: null,
    created_at: now,
    created_by: 'mock',
    updated_at: now,
    updated_by: 'mock',
  }
  accounts.unshift(account)

  return {
    data: cloneAccount(account),
  }
}

export async function update(data: any) {
  const index = accounts.findIndex((item) => item.id === data.id)
  if (index < 0) {
    return {
      data: null,
    }
  }

  const now = formatDateTime(new Date())
  const current = accounts[index]
  const next = {
    ...current,
    ...data,
    password_hash: data.password ? createPasswordHash(data.password) : current.password_hash,
    updated_at: now,
    updated_by: 'mock',
  }
  delete next.password
  accounts[index] = next

  return {
    data: cloneAccount(next),
  }
}

export async function remove(data: any) {
  const ids = new Set<string>(data?.ids ?? [])
  for (let index = accounts.length - 1; index >= 0; index -= 1) {
    if (ids.has(accounts[index].id)) {
      accounts.splice(index, 1)
    }
  }

  return {
    data: true,
  }
}

function filterAccounts(params: any) {
  const account = String(params?.account ?? '').trim().toLowerCase()
  const name = String(params?.name ?? '').trim().toLowerCase()
  const phone = String(params?.phone ?? '').trim().toLowerCase()
  const email = String(params?.email ?? '').trim().toLowerCase()

  return accounts.filter((item) => {
    const matchAccount = account ? item.account.toLowerCase().includes(account) : true
    const matchName = name ? item.name.toLowerCase().includes(name) : true
    const matchPhone = phone ? String(item.phone ?? '').toLowerCase().includes(phone) : true
    const matchEmail = email ? String(item.email ?? '').toLowerCase().includes(email) : true
    const matchType = params?.account_type ? item.account_type === params.account_type : true
    const matchStatus = params?.account_status
      ? item.account_status === params.account_status
      : true

    return matchAccount && matchName && matchPhone && matchEmail && matchType && matchStatus
  })
}

function cloneAccount(account: any) {
  return JSON.parse(JSON.stringify(account))
}

function createPasswordHash(password: string) {
  return `mock-hash-${password}`
}

function formatDateTime(date: Date) {
  const parts = [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate()),
    pad(date.getHours()),
    pad(date.getMinutes()),
    pad(date.getSeconds()),
  ]

  return `${parts[0]}-${parts[1]}-${parts[2]} ${parts[3]}:${parts[4]}:${parts[5]}`
}

function pad(value: number) {
  return String(value).padStart(2, '0')
}
