import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Button,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  CalendarOutlined,
  ClockCircleOutlined,
  SendOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import { Link, useNavigate, useParams } from 'react-router-dom'
import dayjs from 'dayjs'
import {
  contestClarifications,
  contestCreateThread,
  contestDetail,
  contestEnter,
  contestMySubmissions,
  contestMyThreads,
  contestProblems,
  contestRegister,
  contestScoreboard,
  contestUnregister,
} from '@/api/contest'
import type {
  PortalClarificationThread,
  PortalClarification,
  PortalContestBrief,
  PortalContestProblemMeta,
  PortalContestSubmission,
} from '@/api/contest'
import { Markdown } from '@/components/common/Markdown'
import { ContestStatusBadge } from '@/components/oj/ContestStatusBadge'
import { ScoreboardTable } from '@/components/oj/ScoreboardTable'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { ProblemSelector, type ProblemOption } from '@/components/selector/ProblemSelector'
import { useDict } from '@/hooks/useDict'
import { useAuthStore } from '@/stores/auth'
import { dictTypeData } from '@/utils/dict'
import { formatDateTime } from '@/utils/time'

const formatTime = (value: string | null) => formatDateTime(value)

/** 封面：优先 cover_url，其次 extra.cover_url（后续扩展位） */
function resolveCoverUrl(contest: PortalContestBrief | null): string | null {
  if (!contest) return null
  if (contest.cover_url) return contest.cover_url
  const fromExtra = contest.extra?.cover_url
  return typeof fromExtra === 'string' && fromExtra ? fromExtra : null
}

type RemainParts = { days: number; hours: number; minutes: number; seconds: number; label: string }

function calcRemain(contest: PortalContestBrief | null, nowMs: number): RemainParts {
  const empty = { days: 0, hours: 0, minutes: 0, seconds: 0, label: '时间待定' }
  if (!contest?.start_time || !contest?.end_time) return empty

  const now = dayjs(nowMs)
  const start = dayjs(contest.start_time)
  const end = dayjs(contest.end_time)

  let target = end
  let label = '距离比赛结束'
  if (contest.lifecycle_status === 'SCHEDULED' || now.isBefore(start)) {
    target = start
    label = '距离比赛开始'
  } else if (contest.lifecycle_status === 'ENDED' || now.isAfter(end)) {
    return { days: 0, hours: 0, minutes: 0, seconds: 0, label: '比赛已结束' }
  }

  const total = Math.max(0, target.diff(now, 'second'))
  return {
    days: Math.floor(total / 86400),
    hours: Math.floor((total % 86400) / 3600),
    minutes: Math.floor((total % 3600) / 60),
    seconds: total % 60,
    label,
  }
}

function pad2(n: number) {
  return String(n).padStart(2, '0')
}

export function ContestDetailPage() {
  useDict()
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const isLogin = useAuthStore((s) => s.isLogin)

  const [contest, setContest] = useState<PortalContestBrief | null>(null)
  const [joinLoading, setJoinLoading] = useState(false)
  const [accessCode, setAccessCode] = useState('')
  const [joinModalOpen, setJoinModalOpen] = useState(false)

  const [problems, setProblems] = useState<PortalContestProblemMeta[]>([])
  const [problemsLoading, setProblemsLoading] = useState(true)
  const [scoreboard, setScoreboard] = useState<Awaited<ReturnType<typeof contestScoreboard>>['data'] | null>(null)
  const [scoreboardLoading, setScoreboardLoading] = useState(false)
  const [clarifications, setClarifications] = useState<PortalClarification[]>([])
  const [threads, setThreads] = useState<PortalClarificationThread[]>([])
  const [qaLoading, setQaLoading] = useState(true)
  const [mySubmissions, setMySubmissions] = useState<PortalContestSubmission[]>([])
  const [mySubmissionsLoading, setMySubmissionsLoading] = useState(true)

  const [threadModalOpen, setThreadModalOpen] = useState(false)
  const [threadSubmitting, setThreadSubmitting] = useState(false)
  const [threadProblemPickerOpen, setThreadProblemPickerOpen] = useState(false)
  const [threadForm] = Form.useForm()
  const threadProblemId = Form.useWatch('problem_id', threadForm) as string | undefined
  const [activeTab, setActiveTab] = useState('overview')
  const [nowMs, setNowMs] = useState(() => Date.now())

  const threadProblemLabel = useMemo(() => {
    const p = problems.find((item) => item.problem_id === threadProblemId)
    if (!p?.problem_name) return ''
    return `${p.label}. ${p.problem_name}`
  }, [problems, threadProblemId])

  const loadContestProblemPage = useCallback(
    async ({ current, size, keyword }: { current: number; size: number; keyword?: string }) => {
      const q = (keyword || '').trim().toLowerCase()
      const filtered = problems
        .filter((p) => p.problem_name)
        .filter((p) => {
          if (!q) return true
          const hay = `${p.label} ${p.problem_code || ''} ${p.problem_name || ''}`.toLowerCase()
          return hay.includes(q)
        })
        .map(
          (p): ProblemOption => ({
            id: p.problem_id,
            code: String(p.label || p.problem_code || ''),
            name: p.problem_name || '',
          }),
        )
      const start = (current - 1) * size
      return { records: filtered.slice(start, start + size), total: filtered.length }
    },
    [problems],
  )

  useEffect(() => {
    const timer = window.setInterval(() => setNowMs(Date.now()), 1000)
    return () => window.clearInterval(timer)
  }, [])

  async function refreshDetail() {
    const res = await contestDetail(id)
    setContest(res.data)
  }

  async function loadProblems() {
    try {
      const res = await contestProblems(id)
      setProblems(res.data)
    } finally {
      setProblemsLoading(false)
    }
  }

  async function loadQa() {
    try {
      const clarRes = await contestClarifications(id)
      setClarifications(clarRes.data)
      if (isLogin()) {
        const threadRes = await contestMyThreads(id)
        setThreads(threadRes.data)
      } else {
        setThreads([])
      }
    } finally {
      setQaLoading(false)
    }
  }

  async function loadMySubmissions() {
    try {
      const res = await contestMySubmissions(id)
      setMySubmissions(res.data)
    } finally {
      setMySubmissionsLoading(false)
    }
  }

  useEffect(() => {
    void (async () => {
      const res = await contestDetail(id)
      setContest(res.data)
    })()
  }, [id])

  useEffect(() => {
    if (!contest) return
    void loadProblems()
    if (contest.use_clarifications) {
      void loadQa()
    }
    if (isLogin()) {
      void loadMySubmissions()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, contest?.joined])

  async function loadScoreboard() {
    setScoreboardLoading(true)
    try {
      const res = await contestScoreboard(id)
      setScoreboard(res.data)
    } finally {
      setScoreboardLoading(false)
    }
  }

  async function handleRegister() {
    if (!isLogin()) {
      navigate(`/auth/login?redirect=${encodeURIComponent(`/contests/${id}`)}`)
      return
    }
    if (contest?.is_private) {
      message.info('私有竞赛需管理员添加后才能参加')
      return
    }
    if (contest?.requires_access_code) {
      setJoinModalOpen(true)
      return
    }
    await doRegister(null)
  }

  async function doRegister(code: string | null) {
    setJoinLoading(true)
    try {
      const res = await contestRegister(id, { access_code: code })
      setContest(res.data)
      const status = res.data.registration_status
      if (status === 'PENDING') {
        message.success('已提交报名，等待审核')
      } else if (status === 'APPROVED' && res.data.lifecycle_status === 'RUNNING') {
        message.success('报名成功，正在进入比赛')
        setJoinModalOpen(false)
        setAccessCode('')
        const enterRes = await contestEnter(id)
        await refreshDetail()
        if (enterRes.data.first_problem_id) {
          navigate(`/contests/${id}/problems/${enterRes.data.first_problem_id}`)
        } else {
          setActiveTab('problems')
        }
        return
      } else {
        message.success('报名成功')
      }
      setJoinModalOpen(false)
      setAccessCode('')
    } finally {
      setJoinLoading(false)
    }
  }

  async function handleEnter(goFirst: boolean) {
    if (!isLogin()) {
      navigate(`/auth/login?redirect=${encodeURIComponent(`/contests/${id}`)}`)
      return
    }
    setJoinLoading(true)
    try {
      const res = await contestEnter(id)
      message.success('已进入比赛')
      await refreshDetail()
      if (goFirst && res.data.first_problem_id) {
        navigate(`/contests/${id}/problems/${res.data.first_problem_id}`)
      } else {
        setActiveTab('problems')
      }
    } finally {
      setJoinLoading(false)
    }
  }

  async function handleLeave() {
    setJoinLoading(true)
    try {
      await contestUnregister(id)
      message.success('已取消报名')
      await refreshDetail()
    } finally {
      setJoinLoading(false)
    }
  }

  async function handleCreateThread() {
    const values = await threadForm.validateFields()
    setThreadSubmitting(true)
    try {
      await contestCreateThread(id, {
        title: values.title,
        body: values.body,
        problem_id: values.problem_id || null,
      })
      message.success('提问已提交')
      setThreadModalOpen(false)
      threadForm.resetFields()
      await loadQa()
    } finally {
      setThreadSubmitting(false)
    }
  }

  const remain = useMemo(() => calcRemain(contest, nowMs), [contest, nowMs])
  const coverUrl = resolveCoverUrl(contest)

  const problemColumns: ColumnsType<PortalContestProblemMeta> = [
    {
      title: '#',
      dataIndex: 'label',
      width: 70,
      render: (label: string) => <span className="font-medium">{label}</span>,
    },
    {
      title: '题号',
      dataIndex: 'problem_code',
      width: 120,
      render: (code: string | null) => <span className="font-mono text-sm">{code || '-'}</span>,
    },
    {
      title: '题目',
      dataIndex: 'problem_name',
      render: (name: string | null, record) =>
        name ? (
          <Link
            to={`/contests/${id}/problems/${record.problem_id}`}
            className="text-sm text-[var(--ant-color-primary)]"
          >
            {name}
          </Link>
        ) : (
          <span className="muted-text text-sm">比赛开始后可见</span>
        ),
    },
    {
      title: '分值',
      dataIndex: 'points',
      width: 90,
      align: 'right',
      render: (points: number) => <span>{points}</span>,
    },
  ]

  const mySubmissionColumns: ColumnsType<PortalContestSubmission> = [
    {
      title: '提交',
      dataIndex: 'submission_id',
      width: 100,
      render: (subId: string) => (
        <Link to={`/submissions/${subId}`} className="font-mono text-xs text-[var(--ant-color-primary)]">
          {subId.length > 12 ? subId.slice(0, 12) : subId}
        </Link>
      ),
    },
    {
      title: '题目',
      dataIndex: 'problem_id',
      width: 90,
      render: (_: string, record) => {
        const meta = problems.find((p) => p.problem_id === record.problem_id)
        return <span>{meta?.label ?? '-'}</span>
      },
    },
    {
      title: '结果',
      dataIndex: 'result',
      width: 110,
      render: (result: string | null, record) => (
        <VerdictBadge status={record.status} result={result} />
      ),
    },
    {
      title: '得分',
      dataIndex: 'score',
      width: 70,
      align: 'right',
      render: (score: number) => <span>{score}</span>,
    },
    {
      title: '提交时间',
      dataIndex: 'created_at',
      render: (createdAt: string | null) => (
        <Typography.Text type="secondary" className="text-xs">
          {formatTime(createdAt)}
        </Typography.Text>
      ),
    },
  ]

  const tabItems = [
    {
      key: 'overview',
      label: '概览',
      children: (
        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
          <div className="px-1 py-2">
            <div className="mb-3 text-base font-semibold">竞赛简介</div>
            {contest?.description ? (
              <Markdown content={contest.description} />
            ) : (
              <Typography.Text type="secondary">{contest?.summary || '暂无简介'}</Typography.Text>
            )}
          </div>
          <div className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-4">
            <div className="mb-3 text-sm font-semibold">竞赛公告</div>
            {clarifications.length ? (
              <div className="space-y-3">
                {clarifications.slice(0, 5).map((item) => (
                  <div key={item.id}>
                    <div className="text-sm font-medium">{item.title}</div>
                    <div className="muted-text mt-1 line-clamp-2 text-xs">{item.body}</div>
                  </div>
                ))}
              </div>
            ) : (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无公告" />
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'problems',
      label: `题目${problems.length ? ` (${problems.length})` : ''}`,
      children: (
        <Table
          rowKey="id"
          loading={problemsLoading}
          columns={problemColumns}
          dataSource={problems}
          pagination={false}
        />
      ),
    },
    {
      key: 'scoreboard',
      label: '榜单',
      children: scoreboardLoading ? (
        <div className="flex justify-center py-10">
          <Spin />
        </div>
      ) : scoreboard ? (
        <ScoreboardTable board={scoreboard} />
      ) : (
        <Empty description="榜单不可用">
          <Button type="primary" onClick={() => void loadScoreboard()}>
            查看榜单
          </Button>
        </Empty>
      ),
    },
    {
      key: 'qa',
      label: '答疑',
      children: contest?.use_clarifications ? (
        <Spin spinning={qaLoading}>
          <div className="flex flex-col gap-5">
            <div>
              <div className="mb-2 text-sm font-semibold">公告</div>
              {clarifications.length === 0 ? (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无公告" />
              ) : (
                <div className="space-y-2">
                  {clarifications.map((item) => (
                    <div key={item.id} className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-3">
                      <div className="flex items-center justify-between gap-2">
                        <div className="font-medium">{item.title}</div>
                        <span className="muted-text text-xs">{formatTime(item.published_at)}</span>
                      </div>
                      <div className="muted-text mt-1 whitespace-pre-wrap text-sm">{item.body}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div>
              <div className="mb-2 flex items-center justify-between">
                <div className="text-sm font-semibold">我的提问</div>
                <Button
                  type="primary"
                  onClick={() => {
                    if (!isLogin()) {
                      navigate(`/auth/login?redirect=${encodeURIComponent(`/contests/${id}`)}`)
                      return
                    }
                    setThreadModalOpen(true)
                  }}
                >
                  新建提问
                </Button>
              </div>
              {threads.length === 0 ? (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无提问" />
              ) : (
                <List
                  dataSource={threads}
                  renderItem={(thread) => (
                    <List.Item>
                      <div className="w-full">
                        <div className="flex items-center gap-2">
                          <Typography.Text strong>{thread.title}</Typography.Text>
                          {thread.status === 'CLOSED' ? <Tag>已关闭</Tag> : null}
                        </div>
                        <div className="mt-2 space-y-2">
                          {thread.messages.map((msg) => (
                            <div
                              key={msg.id}
                              className="rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2 text-sm"
                            >
                              <div className="muted-text mb-1 text-xs">
                                {msg.is_staff ? '管理员' : '我'} · {formatTime(msg.created_at)}
                              </div>
                              <div className="whitespace-pre-wrap">{msg.body}</div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </List.Item>
                  )}
                />
              )}
            </div>
          </div>
        </Spin>
      ) : (
        <Empty description="本场竞赛未开启答疑" />
      ),
    },
    {
      key: 'my-submissions',
      label: `我的提交${mySubmissions.length ? ` (${mySubmissions.length})` : ''}`,
      children: isLogin() ? (
        <Table
          rowKey="submission_id"
          loading={mySubmissionsLoading}
          columns={mySubmissionColumns}
          dataSource={mySubmissions}
          pagination={false}
        />
      ) : (
        <Empty description="登录后查看我的提交">
          <Link to="/auth/login">
            <Button type="primary">去登录</Button>
          </Link>
        </Empty>
      ),
    },
  ]

  return (
    <div className="page-shell w-full">
      {/* 页面级 Hero：固定色块/封面，不随滚动渐隐 */}
      <header className="relative w-full overflow-hidden" style={{ minHeight: 280 }}>
        {coverUrl ? (
          <img src={coverUrl} alt="" className="absolute inset-0 h-full w-full object-cover" />
        ) : (
          <div className="absolute inset-0 bg-[var(--ant-color-primary)]" />
        )}
        {/* 装饰圆 */}
        <div className="pointer-events-none absolute -left-16 top-8 h-48 w-48 rounded-full bg-white/10" />
        <div className="pointer-events-none absolute bottom-[-40px] left-1/3 h-40 w-40 rounded-full bg-white/10" />
        <div className="pointer-events-none absolute right-24 top-6 h-28 w-28 rounded-full bg-white/15" />
        {coverUrl ? <div className="absolute inset-0 bg-black/35" /> : null}

        <div className="relative z-[1] grid gap-8 px-6 py-10 md:px-8 lg:grid-cols-[minmax(0,1fr)_320px] lg:items-end">
          <div className="min-w-0 text-white">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-2xl font-semibold leading-snug md:text-3xl">
                {contest?.name ?? '加载中…'}
              </h1>
              <ContestStatusBadge status={contest?.lifecycle_status} />
              {contest?.is_rated ? (
                <Tag color="gold">{dictTypeData('CONTEST_TYPE', 'RATED') || '计分'}</Tag>
              ) : null}
              {contest?.is_private ? (
                <Tag color="orange">{dictTypeData('CONTEST_TYPE', 'PRIVATE') || '私有'}</Tag>
              ) : null}
            </div>

            <div className="mt-5 space-y-2 text-sm text-white/90">
              <div className="flex items-start gap-2">
                <CalendarOutlined className="mt-0.5" />
                <span>
                  比赛时间：{formatTime(contest?.start_time ?? null)} ~ {formatTime(contest?.end_time ?? null)}
                </span>
              </div>
              <div className="flex items-start gap-2">
                <ClockCircleOutlined className="mt-0.5" />
                <span>
                  赛制：
                  {dictTypeData('CONTEST_FORMAT', contest?.format_name) || contest?.format_name || '-'}
                </span>
              </div>
              <div className="flex items-start gap-2">
                <TeamOutlined className="mt-0.5" />
                <span>报名人数：{contest?.user_count ?? 0}</span>
              </div>
            </div>

            <div className="mt-6 flex flex-wrap gap-2">
              {contest ? (
                (() => {
                  const status = contest.registration_status
                  const approved = status === 'APPROVED'
                  const pending = status === 'PENDING'
                  const rejected = status === 'REJECTED'
                  const ended =
                    contest.lifecycle_status === 'ENDED' || contest.lifecycle_status === 'LOCKED'
                  const loggedIn = isLogin()

                  if (contest.can_enter || (approved && contest.lifecycle_status === 'RUNNING')) {
                    return (
                      <Space size={8}>
                        <Button
                          type="primary"
                          icon={<SendOutlined />}
                          loading={joinLoading}
                          className="!bg-white !text-[var(--ant-color-primary)]"
                          onClick={() => void handleEnter(true)}
                        >
                          进入比赛
                        </Button>
                        <Button ghost loading={joinLoading} onClick={() => void handleEnter(false)}>
                          题目列表
                        </Button>
                        <Button ghost onClick={() => setActiveTab('scoreboard')}>
                          查看榜单
                        </Button>
                      </Space>
                    )
                  }

                  if (approved && contest.lifecycle_status === 'SCHEDULED') {
                    return (
                      <Space size={8}>
                        <Button type="primary" disabled className="!bg-white/80 !text-[var(--ant-color-primary)]">
                          已报名 · 等待开赛
                        </Button>
                        <Button loading={joinLoading} onClick={() => void handleLeave()}>
                          取消报名
                        </Button>
                      </Space>
                    )
                  }

                  if (pending) {
                    return (
                      <Space size={8}>
                        <Button type="primary" disabled className="!bg-white/80 !text-[var(--ant-color-primary)]">
                          审核中
                        </Button>
                        <Button loading={joinLoading} onClick={() => void handleLeave()}>
                          取消报名
                        </Button>
                      </Space>
                    )
                  }

                  if (rejected) {
                    return (
                      <Space size={8}>
                        <Button danger ghost disabled>
                          报名已拒绝{contest.registration_remark ? `：${contest.registration_remark}` : ''}
                        </Button>
                        {contest.can_register ? (
                          <Button
                            type="primary"
                            loading={joinLoading}
                            className="!bg-white !text-[var(--ant-color-primary)]"
                            onClick={() => void handleRegister()}
                          >
                            重新报名
                          </Button>
                        ) : null}
                      </Space>
                    )
                  }

                  if (contest.is_private && !approved) {
                    return (
                      <Button type="primary" disabled className="!bg-white/80 !text-[var(--ant-color-primary)]">
                        需管理员添加后参加
                      </Button>
                    )
                  }

                  if (!loggedIn && !ended && !contest.is_private) {
                    return (
                      <Space size={8}>
                        <Button
                          type="primary"
                          icon={<SendOutlined />}
                          className="!h-10 !bg-white !px-5 !text-[var(--ant-color-primary)]"
                          onClick={() =>
                            navigate(`/auth/login?redirect=${encodeURIComponent(`/contests/${id}`)}`)
                          }
                        >
                          {contest.lifecycle_status === 'RUNNING' ? '登录后参赛' : '登录后报名'}
                        </Button>
                        <Button ghost onClick={() => setActiveTab('scoreboard')}>
                          查看榜单
                        </Button>
                      </Space>
                    )
                  }

                  if (contest.can_register) {
                    return (
                      <Space size={8}>
                        <Button
                          type="primary"
                          icon={<SendOutlined />}
                          loading={joinLoading}
                          className="!h-10 !bg-white !px-5 !text-[var(--ant-color-primary)]"
                          onClick={() => void handleRegister()}
                        >
                          {contest.lifecycle_status === 'RUNNING' ? '立即参赛' : '立即报名'}
                        </Button>
                        <Button ghost onClick={() => setActiveTab('scoreboard')}>
                          查看榜单
                        </Button>
                      </Space>
                    )
                  }

                  return (
                    <Button ghost onClick={() => setActiveTab('scoreboard')}>
                      查看榜单
                    </Button>
                  )
                })()
              ) : null}
            </div>
          </div>

          {/* 右侧倒计时卡片 */}
          <div className="rounded-2xl bg-black/25 px-5 py-5 text-white backdrop-blur-sm ring-1 ring-white/15">
            <div className="mb-4 text-center text-sm text-white/85">{remain.label}</div>
            <div className="grid grid-cols-4 gap-2">
              {[
                { value: remain.days, unit: '天' },
                { value: remain.hours, unit: '时' },
                { value: remain.minutes, unit: '分' },
                { value: remain.seconds, unit: '秒' },
              ].map((item) => (
                <div
                  key={item.unit}
                  className="flex flex-col items-center rounded-xl bg-black/25 px-2 py-3 ring-1 ring-white/10"
                >
                  <div className="text-2xl font-semibold tabular-nums md:text-3xl">
                    {item.unit === '天' ? item.value : pad2(item.value)}
                  </div>
                  <div className="mt-1 text-xs text-white/70">{item.unit}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </header>

      {/* 正文：白卡片 + 现有 Tabs */}
      <div className="px-6 py-6">
        <div className="panel min-w-0 overflow-hidden rounded-xl px-4 pb-4 pt-2">
          <Tabs
            activeKey={activeTab}
            onChange={(key) => {
              setActiveTab(key)
              if (key === 'scoreboard' && !scoreboard) void loadScoreboard()
            }}
            items={tabItems}
          />
        </div>
      </div>

      <Modal
        open={joinModalOpen}
        title="报名私有竞赛"
        okText="报名"
        cancelText="取消"
        confirmLoading={joinLoading}
        onOk={() => void doRegister(accessCode.trim() || null)}
        onCancel={() => setJoinModalOpen(false)}
      >
        <p className="mb-2 text-sm text-[var(--ant-color-text-secondary)]">
          本竞赛为私有竞赛，请输入准入码报名：
        </p>
        <Input
          placeholder="准入码"
          value={accessCode}
          onChange={(e) => setAccessCode(e.target.value)}
          onPressEnter={() => void doRegister(accessCode.trim() || null)}
        />
      </Modal>

      <Modal
        open={threadModalOpen}
        title="新建提问"
        okText="提交"
        cancelText="取消"
        confirmLoading={threadSubmitting}
        onOk={() => void handleCreateThread()}
        onCancel={() => setThreadModalOpen(false)}
      >
        <Form form={threadForm} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input placeholder="问题标题" allowClear />
          </Form.Item>
          <Form.Item name="problem_id" hidden>
            <Input />
          </Form.Item>
          <Form.Item label="关联题目">
            <Space.Compact className="w-full">
              <Input readOnly value={threadProblemLabel} placeholder="（可选）选择关联题目" />
              <Button onClick={() => setThreadProblemPickerOpen(true)}>选择</Button>
              <Button
                disabled={!threadProblemId}
                onClick={() => threadForm.setFieldValue('problem_id', undefined)}
              >
                清除
              </Button>
            </Space.Compact>
          </Form.Item>
          <Form.Item name="body" label="内容" rules={[{ required: true, message: '请输入提问内容' }]}>
            <Input.TextArea rows={4} placeholder="详细描述你的问题" />
          </Form.Item>
        </Form>
      </Modal>

      <ProblemSelector
        open={threadProblemPickerOpen}
        onClose={() => setThreadProblemPickerOpen(false)}
        title="选择关联题目"
        mode="single"
        loadPage={loadContestProblemPage}
        onSelect={(problem) => threadForm.setFieldValue('problem_id', problem.id)}
      />
    </div>
  )
}
