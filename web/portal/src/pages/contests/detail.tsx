import { useEffect, useState } from 'react'
import {
  Button,
  Card,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Select,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  contestClarifications,
  contestCreateThread,
  contestDetail,
  contestJoin,
  contestLeave,
  contestMySubmissions,
  contestMyThreads,
  contestProblems,
  contestScoreboard,
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
import { useAuthStore } from '@/stores/auth'

const formatTime = (value: string | null) => (value ? new Date(value).toLocaleString() : '-')

export function ContestDetailPage() {
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
  const [threadForm] = Form.useForm()

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
      const [clarRes, threadRes] = await Promise.all([contestClarifications(id), contestMyThreads(id)])
      setClarifications(clarRes.data)
      setThreads(threadRes.data)
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

  async function handleJoin() {
    if (!isLogin()) {
      navigate(`/auth/login?redirect=${encodeURIComponent(`/contests/${id}`)}`)
      return
    }
    if (contest?.is_private) {
      setJoinModalOpen(true)
      return
    }
    await doJoin(null)
  }

  async function doJoin(code: string | null) {
    setJoinLoading(true)
    try {
      await contestJoin(id, { access_code: code, spectate: false })
      message.success('报名成功')
      setJoinModalOpen(false)
      setAccessCode('')
      await refreshDetail()
    } finally {
      setJoinLoading(false)
    }
  }

  async function handleLeave() {
    setJoinLoading(true)
    try {
      await contestLeave(id)
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
          <Link to={`/contests/${id}/problems/${record.problem_id}`} className="text-sm">
            {name}
          </Link>
        ) : (
          <span className="text-sm text-gray-400">比赛开始后可见</span>
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
        <Link to={`/submissions/${subId}`} className="font-mono text-xs">
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
      children: contest?.description ? (
        <Markdown content={contest.description} />
      ) : (
        <Typography.Text type="secondary">{contest?.summary || '暂无简介'}</Typography.Text>
      ),
    },
    {
      key: 'problems',
      label: '题目',
      children: (
        <Table
          rowKey="id"
          size="small"
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
        <Spin />
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
          <div className="space-y-4">
            <div>
              <Typography.Title level={5}>公告</Typography.Title>
              {clarifications.length === 0 ? (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无公告" />
              ) : (
                <List
                  size="small"
                  dataSource={clarifications}
                  renderItem={(item) => (
                    <List.Item>
                      <div className="w-full">
                        <div className="flex items-center justify-between gap-2">
                          <Typography.Text strong>{item.title}</Typography.Text>
                          <Typography.Text type="secondary" className="text-xs">
                            {formatTime(item.published_at)}
                          </Typography.Text>
                        </div>
                        <div className="mt-1 whitespace-pre-wrap text-sm text-gray-600">
                          {item.body}
                        </div>
                      </div>
                    </List.Item>
                  )}
                />
              )}
            </div>

            <div>
              <div className="mb-2 flex items-center justify-between">
                <Typography.Title level={5} className="!mb-0">
                  我的提问
                </Typography.Title>
                <Button
                  size="small"
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
                  size="small"
                  dataSource={threads}
                  renderItem={(thread) => (
                    <List.Item>
                      <div className="w-full">
                        <div className="flex items-center gap-2">
                          <Typography.Text strong>{thread.title}</Typography.Text>
                          {thread.status === 'CLOSED' ? <Tag color="default">已关闭</Tag> : null}
                        </div>
                        <div className="mt-2 space-y-2">
                          {thread.messages.map((msg) => (
                            <div key={msg.id} className="rounded bg-gray-50 px-3 py-2 text-sm">
                              <div className="mb-1 text-xs text-gray-400">
                                {msg.is_staff ? '管理员' : '我'} · {formatTime(msg.created_at)}
                              </div>
                              <div className="whitespace-pre-wrap text-gray-700">{msg.body}</div>
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
      label: '我的提交',
      children: isLogin() ? (
        <Table
          rowKey="submission_id"
          size="small"
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
    <div className="space-y-4">
      <Card>
        <div className="flex flex-wrap items-center gap-3">
          <Typography.Title level={4} className="!mb-0">
            {contest?.name ?? '加载中…'}
          </Typography.Title>
          <ContestStatusBadge status={contest?.lifecycle_status} />
          {contest?.is_rated ? <Tag color="gold">Rated</Tag> : null}
          {contest?.is_private ? <Tag color="orange">私有</Tag> : null}
          <div className="flex-1" />
          {contest ? (
            contest.joined ? (
              <Space size={8}>
                <Tag color="green">已报名</Tag>
                {contest.lifecycle_status === 'SCHEDULED' ? (
                  <Button loading={joinLoading} onClick={() => void handleLeave()}>
                    取消报名
                  </Button>
                ) : null}
              </Space>
            ) : (
              <Button type="primary" loading={joinLoading} onClick={() => void handleJoin()}>
                {contest.lifecycle_status === 'RUNNING' ? '参赛' : '报名'}
              </Button>
            )
          ) : null}
        </div>
        <div className="mt-3 space-y-1 text-sm text-gray-500">
          <div>
            时间：{formatTime(contest?.start_time ?? null)} ~ {formatTime(contest?.end_time ?? null)}
          </div>
          <div>
            赛制：{contest?.format_name ?? '-'} · 报名人数：{contest?.user_count ?? 0}
          </div>
        </div>
      </Card>

      <Card className="min-w-0">
        <Tabs items={tabItems} />
      </Card>

      <Modal
        open={joinModalOpen}
        title="报名私有竞赛"
        okText="报名"
        cancelText="取消"
        confirmLoading={joinLoading}
        onOk={() => void doJoin(accessCode.trim() || null)}
        onCancel={() => setJoinModalOpen(false)}
      >
        <p className="mb-2 text-sm text-gray-500">本竞赛为私有竞赛，请输入准入码报名：</p>
        <Input
          placeholder="准入码"
          value={accessCode}
          onChange={(e) => setAccessCode(e.target.value)}
          onPressEnter={() => void doJoin(accessCode.trim() || null)}
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
          <Form.Item name="problem_id" label="关联题目">
            <Select
              allowClear
              placeholder="（可选）选择关联题目"
              options={problems
                .filter((p) => p.problem_name)
                .map((p) => ({ value: p.problem_id, label: `${p.label}. ${p.problem_name}` }))}
            />
          </Form.Item>
          <Form.Item name="body" label="内容" rules={[{ required: true, message: '请输入提问内容' }]}>
            <Input.TextArea rows={4} placeholder="详细描述你的问题" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
