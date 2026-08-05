import { useEffect, useMemo, useState } from 'react'
import { Button, Empty, Grid, Skeleton, Splitter, Table, Tag, Typography, message } from 'antd'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CopyOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  HistoryOutlined,
  PercentageOutlined,
  StarFilled,
  StarOutlined,
  TrophyOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { CustomTabs } from '@/components/common/CustomTabs'
import { Markdown } from '@/components/common/Markdown'
import { AiChatPanel } from '@/components/oj/AiChatPanel'
import { SolveContextProvider } from '@/components/oj/SolveContext'
import { SolveProblemNav } from '@/components/oj/SolveProblemNav'
import { SolveSidebar } from '@/components/oj/SolveSidebar'
import { SubmissionPerformance } from '@/components/oj/SubmissionPerformance'
import { SubmitPanel } from '@/components/oj/SubmitPanel'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useDict } from '@/hooks/useDict'
import { useAuthStore } from '@/stores/auth'
import { dictTypeData } from '@/utils/dict'
import { formatDateTime } from '@/utils/time'
import { courseApi, problemApi, problemListApi, submissionApi } from '@/api'

const formatMemory = (kb: number) => `${Math.round(kb / 1024)} MB`
const formatRate = (rate: number) => `${Number(rate || 0).toFixed(1)}%`

export function ProblemDetailPage() {
  useDict()
  const { id = '' } = useParams()
  const userInfo = useAuthStore((s) => s.userInfo)
  const isLogin = useAuthStore((s) => s.isLogin)
  const screens = Grid.useBreakpoint()
  const isDesktop = screens.lg ?? false
  const [searchParams, setSearchParams] = useSearchParams()

  const [detail, setDetail] = useState<any>(null)
  const [languages, setLanguages] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [mySubmissions, setMySubmissions] = useState<any[]>([])
  const [submissionsLoading, setSubmissionsLoading] = useState(true)
  const [aiChatOpen, setAiChatOpen] = useState(false)
  const [favorited, setFavorited] = useState(false)
  const [favoriteLoading, setFavoriteLoading] = useState(false)
  const [latestAcSubmissionId, setLatestAcSubmissionId] = useState<string | null>(null)
  const [latestAcLoading, setLatestAcLoading] = useState(false)

  const queryTab = searchParams.get('tab')
  const querySubmissionId = searchParams.get('submission_id')
  const showPassedTab = latestAcSubmissionId != null

  const passedSubmissionId = useMemo(() => {
    if (!latestAcSubmissionId) return null
    if (queryTab === 'passed' && querySubmissionId) {
      return querySubmissionId
    }
    return latestAcSubmissionId
  }, [queryTab, querySubmissionId, latestAcSubmissionId])

  const [activeTab, setActiveTab] = useState(() => {
    if (queryTab === 'passed') return 'passed'
    if (queryTab === 'submissions') return 'submissions'
    return 'statement'
  })


  async function loadLatestAc() {
    setLatestAcLoading(true)
    try {
      const res = await submissionApi.myLatestPracticeAc(id)
      setLatestAcSubmissionId(res.data.submission_id)
    } catch {
      setLatestAcSubmissionId(null)
    } finally {
      setLatestAcLoading(false)
    }
  }

  function handleTabChange(key: string) {
    setActiveTab(key)
    if (key === 'passed') {
      const next = new URLSearchParams(searchParams)
      next.set('tab', 'passed')
      if (passedSubmissionId) {
        next.set('submission_id', passedSubmissionId)
      } else {
        next.delete('submission_id')
      }
      setSearchParams(next, { replace: true })
    } else if (key === 'submissions') {
      const next = new URLSearchParams(searchParams)
      next.set('tab', 'submissions')
      next.delete('submission_id')
      setSearchParams(next, { replace: true })
    } else {
      const next = new URLSearchParams(searchParams)
      next.delete('tab')
      next.delete('submission_id')
      setSearchParams(next, { replace: true })
    }
  }

  async function loadFavoriteState() {
    if (!isLogin() || !id) {
      setFavorited(false)
      return
    }
    try {
      const res = await problemListApi.favoriteStatus(id)
      setFavorited(Boolean(res.data?.favorited))
    } catch {
      setFavorited(false)
    }
  }

  async function load() {
    try {
      const [detailRes, langRes] = await Promise.all([problemApi.problemDetail(id), problemApi.problemLanguages(id)])
      setDetail(detailRes.data)
      setLanguages(langRes.data)
    } finally {
      setLoading(false)
    }
  }

  async function loadMySubmissions() {
    try {
      const res = await submissionApi.submissionPage({
        current: 1,
        size: 20,
        problem_id: id,
        user_id: userInfo?.accountId,
      })
      setMySubmissions(res.data.records)
    } finally {
      setSubmissionsLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    void loadFavoriteState()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, userInfo?.accountId])

  useEffect(() => {
    if (isLogin()) {
      void loadMySubmissions()
      void loadLatestAc()
    } else {
      setLatestAcSubmissionId(null)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, userInfo?.accountId])

  useEffect(() => {
    if (queryTab === 'passed' && showPassedTab && !latestAcLoading) {
      setActiveTab('passed')
    } else if (queryTab === 'submissions') {
      setActiveTab('submissions')
    } else if (activeTab === 'passed' && !showPassedTab && !latestAcLoading) {
      setActiveTab('statement')
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryTab, showPassedTab, latestAcLoading])

  async function handleSubmit(payload: { language_key: string; source: string }) {
    const res = await problemApi.problemSubmit(id, payload)
    const snapshot = res.data
    const taskId = searchParams.get('taskId')
    if (taskId && snapshot?.submission_id) {
      try {
        await courseApi.courseTaskRecordSubmission({
          task_id: taskId,
          problem_id: id,
          submission_id: snapshot.submission_id,
        })
      } catch {
        // 任务关联失败不阻断提交结果展示
      }
    }
    return snapshot
  }

  async function toggleFavorite() {
    if (!isLogin()) {
      message.info('请先登录后再收藏')
      return
    }
    setFavoriteLoading(true)
    try {
      if (favorited) {
        await problemListApi.removeFavorite(id)
        setFavorited(false)
        message.success('已取消收藏')
      } else {
        await problemListApi.addFavorite(id)
        setFavorited(true)
        message.success('已加入收藏')
      }
    } finally {
      setFavoriteLoading(false)
    }
  }

  async function copyStatementMarkdown(content: string) {
    await navigator.clipboard.writeText(content)
    message.success('已复制题面 Markdown')
  }

  const submissionsColumns: ColumnsType<any> = [
    {
      title: '提交',
      dataIndex: 'id',
      width: 90,
      render: (subId: string) => (
        <Link to={`/submissions/${subId}`} className="font-mono text-xs">
          {subId.length > 12 ? subId.slice(0, 12) : subId}
        </Link>
      ),
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
      title: '耗时',
      dataIndex: 'time_ms',
      width: 80,
      align: 'right',
      render: (time: number) => <Typography.Text type="secondary">{time} ms</Typography.Text>,
    },
    {
      title: '提交时间',
      dataIndex: 'created_at',
      render: (createdAt: string) => (
        <Typography.Text type="secondary" className="text-xs">
          {formatDateTime(createdAt)}
        </Typography.Text>
      ),
    },
  ]

  const tabItems = [
    {
      key: 'statement',
      label: '描述',
      icon: <FileTextOutlined />,
      children: loading ? (
        <Skeleton active paragraph={{ rows: 8 }} />
      ) : detail ? (
        <div>
          <div className="mb-2">
            <div className="flex flex-wrap items-center gap-2">
              <Typography.Title level={4} className="!mb-0">
                {detail.code}. {detail.name}
              </Typography.Title>
              <Button
                type="text"
                icon={<CopyOutlined />}
                aria-label="复制题面 Markdown"
                onClick={() => void copyStatementMarkdown(detail.description)}
              />
              <Button
                type="text"
                loading={favoriteLoading}
                icon={favorited ? <StarFilled className="text-[var(--ant-color-warning)]" /> : <StarOutlined />}
                aria-label={favorited ? '取消收藏' : '收藏'}
                onClick={() => void toggleFavorite()}
              >
                {favorited ? '已收藏' : '收藏'}
              </Button>
            </div>
          </div>
          <div className="mb-4 flex flex-wrap items-center gap-2">
            {detail.solved ? <Tag color="success">已通过</Tag> : null}
            {detail.difficulty ? (
              <Tag>
                {dictTypeData('PROBLEM_DIFFICULTY', detail.difficulty) || detail.difficulty}
              </Tag>
            ) : null}
            {detail.partial ? <Tag color="blue">部分分</Tag> : null}
            {detail.type_names.map((name: any) => (
              <Tag key={name}>{name}</Tag>
            ))}
          </div>
          <div className="muted-text mb-4 flex flex-wrap items-center gap-x-5 gap-y-2 text-sm">
            <span className="inline-flex items-center gap-1.5">
              <ClockCircleOutlined />
              时间限制 {detail.time_limit_ms} ms
            </span>
            <span className="inline-flex items-center gap-1.5">
              <DatabaseOutlined />
              内存限制 {formatMemory(detail.memory_limit_kb)}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <TrophyOutlined />
              分值 {detail.points}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <PercentageOutlined />
              通过率 {formatRate(detail.ac_rate)}（{detail.user_count} 人）
            </span>
          </div>
          <Markdown content={detail.description} />
        </div>
      ) : null,
    },
    ...(showPassedTab && passedSubmissionId
      ? [
          {
            key: 'passed',
            label: '通过',
            icon: <CheckCircleOutlined />,
            children: (
              <SubmissionPerformance
                submissionId={passedSubmissionId}
                problemId={id}
                showBackLink
                onBackToSubmissions={() => handleTabChange('submissions')}
              />
            ),
          },
        ]
      : []),
    {
      key: 'submissions',
      label: '提交记录',
      icon: <HistoryOutlined />,
      children: isLogin() ? (
        <Table
          rowKey="id"

          loading={submissionsLoading}
          columns={submissionsColumns}
          dataSource={mySubmissions}
          pagination={false}
        />
      ) : (
        <Empty description={<span>登录后可查看本题目提交记录</span>}>
          <Link to="/auth/login">
            <Button type="primary">去登录</Button>
          </Link>
        </Empty>
      ),
    },
  ]

  const statementPane = (
    <div className={`panel flex min-w-0 flex-col rounded-md ${isDesktop ? 'h-full' : ''}`}>
      <CustomTabs
        items={tabItems}
        activeKey={activeTab}
        onChange={handleTabChange}
        contentClassName="p-4"
        fillHeight={isDesktop}
      />
    </div>
  )

  const solvePane = (
    <div className={`min-w-0 ${isDesktop ? 'h-full overflow-hidden' : ''}`}>
      <SubmitPanel
        languages={languages}
        defaultLanguage={languages[0]?.language_key}
        onSubmit={handleSubmit}
        fillHeight={isDesktop}
        mobileStacked={!isDesktop}
        aiChatOpen={aiChatOpen}
        onToggleAiChat={() => setAiChatOpen((open) => !open)}
      />
    </div>
  )

  const aiPane = (
    <div className="h-full min-w-0">
      <AiChatPanel onClose={() => setAiChatOpen(false)} />
    </div>
  )

  if (!isDesktop) {
    return (
      <SolveContextProvider problemId={id}>
        <div className="workspace h-full overflow-y-auto p-3">
          <SolveProblemNav />
          <div className="mt-2 flex flex-col gap-3">
            {statementPane}
            {solvePane}
          </div>
        </div>
      </SolveContextProvider>
    )
  }

  return (
    <SolveContextProvider problemId={id}>
      <div className="workspace flex h-full">
        <SolveSidebar />
        <div className="min-w-0 flex-1">
          <Splitter style={{ height: '100%' }}>
            <Splitter.Panel
              defaultSize={aiChatOpen ? '42%' : '55%'}
              min={320}
              className="min-w-0"
              collapsible={{ start: true, end: true, showCollapsibleIcon: 'auto' }}
            >
              {statementPane}
            </Splitter.Panel>
            <Splitter.Panel
              defaultSize={aiChatOpen ? '34%' : undefined}
              min={360}
              className="min-w-0"
              collapsible={{ start: true, end: true, showCollapsibleIcon: 'auto' }}
            >
              {solvePane}
            </Splitter.Panel>
            {aiChatOpen ? (
              <Splitter.Panel
                defaultSize="24%"
                min={280}
                className="min-w-0"
                collapsible={{ start: true, end: true, showCollapsibleIcon: 'auto' }}
              >
                {aiPane}
              </Splitter.Panel>
            ) : null}
          </Splitter>
        </div>
      </div>
    </SolveContextProvider>
  )
}
