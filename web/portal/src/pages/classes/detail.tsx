import { useEffect, useMemo, useState } from 'react'
import { Button, Empty, Spin, Table, Tabs, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { MessageOutlined, TeamOutlined } from '@ant-design/icons'
import { Link, useLocation, useParams } from 'react-router-dom'
import { clazzCourses, clazzDetail, clazzMembers } from '@/api/clazz'
import type { PortalClassBrief, PortalClassMember } from '@/api/clazz'
import type { PortalCourseBrief } from '@/api/course'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/time'

const statusLabel: Record<string, string> = {
  ENABLED: '正常',
  DISABLED: '已停用',
}

const roleLabel: Record<string, string> = {
  STUDENT: '学生',
  ASSISTANT: '助教',
}

export function ClassDetailPage() {
  const { id = '' } = useParams()
  const location = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)
  const loginHref = `/auth/login?redirect=${encodeURIComponent(`${location.pathname}${location.search}`)}`
  const [loading, setLoading] = useState(true)
  const [clazz, setClazz] = useState<PortalClassBrief | null>(null)
  const [courses, setCourses] = useState<PortalCourseBrief[]>([])
  const [members, setMembers] = useState<PortalClassMember[]>([])
  const [coursesLoading, setCoursesLoading] = useState(false)
  const [membersLoading, setMembersLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('overview')

  useEffect(() => {
    void (async () => {
      setLoading(true)
      try {
        const res = await clazzDetail(id)
        setClazz(res.data)
      } catch {
        setClazz(null)
      } finally {
        setLoading(false)
      }
    })()
  }, [id])

  async function loadCourses() {
    if (!clazz?.joined) return
    setCoursesLoading(true)
    try {
      const res = await clazzCourses(id)
      setCourses(res.data ?? [])
    } finally {
      setCoursesLoading(false)
    }
  }

  async function loadMembers() {
    if (!clazz?.joined) return
    setMembersLoading(true)
    try {
      const res = await clazzMembers(id)
      setMembers(res.data ?? [])
    } finally {
      setMembersLoading(false)
    }
  }

  const courseColumns: ColumnsType<PortalCourseBrief> = useMemo(
    () => [
      {
        title: '课程',
        dataIndex: 'name',
        render: (name: string, record: PortalCourseBrief) => (
          <Link to={`/courses/${record.id}`} className="font-medium text-[var(--ant-color-primary)]">
            {name}
          </Link>
        ),
      },
      {
        title: '状态',
        dataIndex: 'status',
        width: 100,
        render: (status: string) => {
          const map: Record<string, { color: string; label: string }> = {
            PUBLISHED: { color: 'success', label: '已发布' },
            DRAFT: { color: 'default', label: '草稿' },
            ARCHIVED: { color: 'default', label: '已归档' },
          }
          const item = map[status] ?? { color: 'default', label: status }
          return <Tag color={item.color}>{item.label}</Tag>
        },
      },
      {
        title: '简介',
        dataIndex: 'summary',
        render: (summary: string | null) => (
          <Typography.Text type="secondary" className="text-sm">
            {summary || '-'}
          </Typography.Text>
        ),
      },
    ],
    [],
  )

  const memberColumns: ColumnsType<PortalClassMember> = useMemo(
    () => [
      {
        title: '账号 ID',
        dataIndex: 'account_id',
        render: (accountId: string) => (
          <Link to={`/profile?account_id=${accountId}`} className="font-mono text-xs">
            {accountId}
          </Link>
        ),
      },
      {
        title: '角色',
        dataIndex: 'role',
        width: 100,
        render: (role: string) => roleLabel[role] ?? role,
      },
      {
        title: '加入时间',
        dataIndex: 'joined_at',
        width: 180,
        render: (value: string) => formatDateTime(value),
      },
    ],
    [],
  )

  if (loading && !clazz) {
    return (
      <div className="page-shell flex justify-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!clazz) {
    return (
      <div className="page-shell w-full">
        <div className="panel rounded-xl py-16">
          <Empty description="班级不存在或不可用" />
        </div>
      </div>
    )
  }

  if (!clazz.joined) {
    return (
      <div className="page-shell w-full">
        <header className="panel mb-5 rounded-xl p-6">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold">{clazz.name}</h1>
              <Tag>{clazz.code}</Tag>
            </div>
            <p className="muted-text mt-2 text-sm">{clazz.summary || '暂无简介'}</p>
            <div className="muted-text mt-3 flex items-center gap-2 text-sm">
              <TeamOutlined />
              {clazz.member_count} 人
            </div>
          </div>
        </header>
        <div className="panel flex flex-col items-center justify-center gap-3 rounded-xl py-12">
          <Empty
            description={
              isLogin()
                ? '你还不是该班级成员，请使用邀请码加入后查看课程与成员'
                : '登录并用邀请码加入后，可查看课程、成员与群聊'
            }
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
          {!isLogin() ? (
            <Link to={loginHref} className="text-sm text-[var(--ant-color-primary)]">
              去登录
            </Link>
          ) : (
            <Link to="/classes" className="text-sm text-[var(--ant-color-primary)]">
              返回班级列表加入
            </Link>
          )}
        </div>
      </div>
    )
  }

  const tabItems = [
    {
      key: 'overview',
      label: '概览',
      children: (
        <div className="space-y-4 px-1 py-2">
          <div>
            <div className="mb-2 text-base font-semibold">班级简介</div>
            <Typography.Text type="secondary">{clazz.summary || '暂无简介'}</Typography.Text>
          </div>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            <div className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-3">
              <div className="muted-text text-xs">班级代码</div>
              <div className="mt-1 font-mono text-sm">{clazz.code}</div>
            </div>
            <div className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-3">
              <div className="muted-text text-xs">成员人数</div>
              <div className="mt-1 text-sm">{clazz.member_count} 人</div>
            </div>
            <div className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-3">
              <div className="muted-text text-xs">创建时间</div>
              <div className="mt-1 text-sm">{formatDateTime(clazz.created_at)}</div>
            </div>
          </div>
        </div>
      ),
    },
    {
      key: 'courses',
      label: `课程${courses.length ? ` (${courses.length})` : ''}`,
      children: (
        <Table
          rowKey="id"
          loading={coursesLoading}
          columns={courseColumns}
          dataSource={courses}
          pagination={false}
        />
      ),
    },
    {
      key: 'members',
      label: `成员${members.length ? ` (${members.length})` : ''}`,
      children: (
        <Table
          rowKey="id"
          loading={membersLoading}
          columns={memberColumns}
          dataSource={members}
          pagination={false}
        />
      ),
    },
    {
      key: 'chat',
      label: '进入群聊',
      children: clazz.conversation_id ? (
        <div className="flex flex-col items-start gap-4 px-1 py-6">
          <Typography.Text type="secondary">班级已绑定群聊，可直接进入消息中心会话。</Typography.Text>
          <Link to={`/messages?conversation=${clazz.conversation_id}`}>
            <Button type="primary" icon={<MessageOutlined />}>
              进入群聊
            </Button>
          </Link>
        </div>
      ) : (
        <Empty description="暂无班级群聊" />
      ),
    },
  ]

  return (
    <div className="page-shell w-full">
      <header className="panel mb-5 rounded-xl p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold">{clazz.name}</h1>
              <Tag>{clazz.code}</Tag>
              <Tag color={clazz.status === 'ENABLED' ? 'success' : 'default'}>
                {statusLabel[clazz.status] ?? clazz.status}
              </Tag>
            </div>
            <p className="muted-text mt-2 text-sm">{clazz.summary || '暂无简介'}</p>
          </div>
          <div className="flex items-center gap-2 text-sm text-[var(--ant-color-text-secondary)]">
            <TeamOutlined />
            {clazz.member_count} 人
          </div>
        </div>
      </header>

      <div className="panel overflow-hidden rounded-xl px-4 pb-4 pt-2">
        <Tabs
          activeKey={activeTab}
          onChange={(key) => {
            setActiveTab(key)
            if (key === 'courses' && !courses.length) void loadCourses()
            if (key === 'members' && !members.length) void loadMembers()
          }}
          items={tabItems}
        />
      </div>
    </div>
  )
}
