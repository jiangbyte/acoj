import { useEffect, useMemo, useState } from 'react'
import { Empty, Spin, Table, Tabs, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useLocation, useParams } from 'react-router-dom'
import { Markdown } from '@/components/common/Markdown'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/time'
import { courseApi, teamApi } from '@/api'

const courseStatusLabel: Record<string, { color: string; label: string }> = {
  PUBLISHED: { color: 'success', label: '已发布' },
  DRAFT: { color: 'default', label: '草稿' },
  ARCHIVED: { color: 'default', label: '已归档' },
}

const taskStatusLabel: Record<string, string> = {
  PUBLISHED: '进行中',
  CLOSED: '已结束',
  DRAFT: '未发布',
}

const progressLabel: Record<string, string> = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  DONE: '已完成',
}

export function CourseDetailPage() {
  const { id = '' } = useParams()
  const location = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)
  const loggedIn = isLogin()
  const loginHref = `/auth/login?redirect=${encodeURIComponent(`${location.pathname}${location.search}`)}`
  const [loading, setLoading] = useState(false)
  const [course, setCourse] = useState<any>(null)
  const [announcements, setAnnouncements] = useState<any[]>([])
  const [tasks, setTasks] = useState<any[]>([])
  const [teams, setTeams] = useState<any[]>([])
  const [announcementsLoading, setAnnouncementsLoading] = useState(false)
  const [tasksLoading, setTasksLoading] = useState(false)
  const [teamsLoading, setTeamsLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('announcements')

  useEffect(() => {
    void (async () => {
      setLoading(true)
      try {
        const res = await courseApi.courseDetail(id)
        setCourse(res.data)
      } catch {
        setCourse(null)
      } finally {
        setLoading(false)
      }
    })()
  }, [id])

  async function loadAnnouncements() {
    setAnnouncementsLoading(true)
    try {
      const res = await courseApi.courseAnnouncementList(id)
      setAnnouncements(res.data ?? [])
    } finally {
      setAnnouncementsLoading(false)
    }
  }

  async function loadTasks() {
    setTasksLoading(true)
    try {
      const res = await courseApi.courseTaskList(id)
      setTasks(res.data ?? [])
    } finally {
      setTasksLoading(false)
    }
  }

  async function loadTeams() {
    if (!isLogin()) {
      setTeams([])
      return
    }
    setTeamsLoading(true)
    try {
      const res = await teamApi.teamCourseList(id)
      setTeams(res.data ?? [])
    } finally {
      setTeamsLoading(false)
    }
  }

  useEffect(() => {
    void loadAnnouncements()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const taskColumns: ColumnsType<any> = useMemo(
    () => [
      {
        title: '任务',
        dataIndex: 'title',
        render: (title: string, record: any) => (
          <Link
            to={`/courses/${id}/tasks/${record.id}`}
            className="font-medium text-[var(--ant-color-primary)]"
          >
            {title}
          </Link>
        ),
      },
      {
        title: '模式',
        dataIndex: 'mode',
        width: 100,
        render: (mode: string) => (mode === 'REALTIME' ? '限时' : '异步'),
      },
      {
        title: '状态',
        dataIndex: 'status',
        width: 100,
        render: (status: string) => taskStatusLabel[status] ?? status,
      },
      {
        title: '进度',
        width: 120,
        render: (_: unknown, record) => {
          const p = record.my_progress
          if (!p) return '-'
          return `${p.solved_count}/${p.total_count} · ${progressLabel[p.status] ?? p.status}`
        },
      },
      {
        title: '截止时间',
        dataIndex: 'due_at',
        width: 170,
        render: (_: unknown, record) => {
          const time = record.due_at || record.close_at
          return formatDateTime(time)
        },
      },
    ],
    [id],
  )

  const teamColumns: ColumnsType<any> = useMemo(
    () => [
      {
        title: '小组',
        dataIndex: 'name',
        render: (name: string, record: any) => (
          <Link to={`/teams/${record.id}`} className="font-medium text-[var(--ant-color-primary)]">
            {name}
          </Link>
        ),
      },
      {
        title: '成员',
        dataIndex: 'member_count',
        width: 90,
        render: (count: number, record: any) => `${count}/${record.max_members}`,
      },
      {
        title: '状态',
        dataIndex: 'status',
        width: 100,
        render: (status: string) => {
          const map: Record<string, string> = {
            ENABLED: '正常',
            DISABLED: '已停用',
            DISSOLVED: '已解散',
          }
          return map[status] ?? status
        },
      },
      {
        title: '简介',
        dataIndex: 'description',
        render: (desc: string | null) => (
          <Typography.Text type="secondary" className="text-sm">
            {desc || '-'}
          </Typography.Text>
        ),
      },
    ],
    [],
  )

  const tabItems = [
    {
      key: 'announcements',
      label: `公告${announcements.length ? ` (${announcements.length})` : ''}`,
      children: (
        <Spin spinning={announcementsLoading}>
          {announcements.length ? (
            <div className="space-y-3 px-1 py-2">
              {announcements.map((item) => (
                <article
                  key={item.id}
                  className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="font-semibold">{item.title}</div>
                    <span className="muted-text text-xs">
                      {formatDateTime(item.published_at ?? item.created_at)}
                    </span>
                  </div>
                  {item.content ? (
                    <div className="mt-3 text-sm">
                      <Markdown content={item.content} />
                    </div>
                  ) : (
                    <Typography.Text type="secondary" className="mt-2 block text-sm">
                      暂无内容
                    </Typography.Text>
                  )}
                </article>
              ))}
            </div>
          ) : (
            <Empty description="暂无公告" />
          )}
        </Spin>
      ),
    },
    {
      key: 'tasks',
      label: `任务${tasks.length ? ` (${tasks.length})` : ''}`,
      children: (
        <Table
          rowKey="id"
          loading={tasksLoading}
          columns={taskColumns}
          dataSource={tasks}
          pagination={false}
          locale={{ emptyText: <Empty description="暂无任务" /> }}
        />
      ),
    },
    {
      key: 'teams',
      label: `课内小组${teams.length ? ` (${teams.length})` : ''}`,
      children: !loggedIn ? (
        <div className="flex flex-col items-center gap-2 py-10">
          <Empty description="登录后可查看并加入课内小组" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          <Link to={loginHref} className="text-sm text-[var(--ant-color-primary)]">
            去登录
          </Link>
        </div>
      ) : (
        <Table
          rowKey="id"
          loading={teamsLoading}
          columns={teamColumns}
          dataSource={teams}
          pagination={false}
          locale={{ emptyText: <Empty description="暂无课内小组" /> }}
        />
      ),
    },
  ]

  if (loading) {
    return (
      <div className="page-shell flex w-full justify-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!course) {
    return (
      <div className="page-shell w-full">
        <div className="panel flex flex-col items-center justify-center gap-2 rounded-xl py-16">
          <Empty description="课程不存在或无权访问" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          <Link to="/courses" className="text-sm text-[var(--ant-color-primary)]">
            返回课程列表
          </Link>
        </div>
      </div>
    )
  }

  const statusMeta = courseStatusLabel[course.status] ?? {
    color: 'default',
    label: course.status,
  }
  const isOpen = course.access_scope === 'OPEN'
  const needLoginToParticipate = isOpen && !loggedIn

  return (
    <div className="page-shell w-full">
      <header className="panel mb-5 rounded-xl p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold">{course.name}</h1>
              <Tag color={isOpen ? 'processing' : 'default'}>{isOpen ? '公开课' : '私有课'}</Tag>
              <Tag color={statusMeta.color}>{statusMeta.label}</Tag>
            </div>
            <p className="muted-text mt-2 text-sm">{course.summary || '暂无简介'}</p>
            {needLoginToParticipate ? (
              <p className="mt-2 text-sm">
                浏览开放；交作业与课内小组需
                <Link to={loginHref} className="mx-1 text-[var(--ant-color-primary)]">
                  登录
                </Link>
                后参与
              </p>
            ) : null}
            {course.classes?.length ? (
              <div className="mt-2 flex flex-wrap gap-3 text-sm">
                {course.classes.map((clazz: any) => (
                  <Link
                    key={clazz.id}
                    to={`/classes/${clazz.id}`}
                    className="text-[var(--ant-color-primary)]"
                  >
                    {clazz.name}
                    {clazz.code ? `（${clazz.code}）` : ''}
                  </Link>
                ))}
              </div>
            ) : course.class_id ? (
              <Link
                to={`/classes/${course.class_id}`}
                className="mt-2 inline-block text-sm text-[var(--ant-color-primary)]"
              >
                返回班级
              </Link>
            ) : null}
          </div>
        </div>
      </header>

      <div className="panel overflow-hidden rounded-xl px-4 pb-4 pt-2">
        <Tabs
          activeKey={activeTab}
          onChange={(key) => {
            setActiveTab(key)
            if (key === 'tasks' && !tasks.length) void loadTasks()
            if (key === 'teams' && !teams.length) void loadTeams()
          }}
          items={tabItems}
        />
      </div>
    </div>
  )
}
