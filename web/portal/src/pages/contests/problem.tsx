import { useEffect, useState } from 'react'
import { Button, Empty, Grid, Skeleton, Splitter, Table, Tag, Typography, message } from 'antd'
import {
  ClockCircleOutlined,
  CopyOutlined,
  DatabaseOutlined,
  FieldNumberOutlined,
  FileTextOutlined,
  HistoryOutlined,
  TrophyOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { Link, useParams } from 'react-router-dom'
import { contestApi } from '@/api'
import { CustomTabs } from '@/components/common/CustomTabs'
import { Markdown } from '@/components/common/Markdown'
import { AiChatPanel } from '@/components/oj/AiChatPanel'
import { SolveContextProvider } from '@/components/oj/SolveContext'
import { SolveProblemNav } from '@/components/oj/SolveProblemNav'
import { SolveSidebar } from '@/components/oj/SolveSidebar'
import { SubmitPanel } from '@/components/oj/SubmitPanel'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useAuthStore } from '@/stores/auth'

import { formatDateTime } from '@/utils/time'

const formatMemory = (kb: number) => `${Math.round(kb / 1024)} MB`
const formatTime = (value: string | null) => formatDateTime(value)

export function ContestProblemPage() {
  const { id = '', problemId = '' } = useParams()
  const isLogin = useAuthStore((s) => s.isLogin)
  const userInfo = useAuthStore((s) => s.userInfo)
  const screens = Grid.useBreakpoint()
  const isDesktop = screens.lg ?? false

  const [detail, setDetail] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [mySubmissions, setMySubmissions] = useState<any[]>([])
  const [submissionsLoading, setSubmissionsLoading] = useState(true)
  const [aiChatOpen, setAiChatOpen] = useState(false)

  async function load() {
    try {
      const res = await contestApi.contestProblemDetail(id, problemId)
      setDetail(res.data)
    } finally {
      setLoading(false)
    }
  }

  async function loadMySubmissions() {
    try {
      const res = await contestApi.contestMySubmissions(id)
      setMySubmissions(res.data.filter((s: any) => s.problem_id === problemId))
    } finally {
      setSubmissionsLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, problemId])

  useEffect(() => {
    if (isLogin()) {
      void loadMySubmissions()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, userInfo?.accountId])

  async function handleSubmit(payload: { language_key: string; source: string }) {
    const res = await contestApi.contestSubmit(id, { problem_id: problemId, ...payload })
    return res.data
  }

  async function copyStatementMarkdown(content: string) {
    await navigator.clipboard.writeText(content)
    message.success('已复制题面 Markdown')
  }

  const submissionsColumns: ColumnsType<any> = [
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
                {detail.label}. {detail.problem_name}
              </Typography.Title>
              <Button
                type="text"

                icon={<CopyOutlined />}
                aria-label="复制题面 Markdown"
                onClick={() => void copyStatementMarkdown(detail.description)}
              />
            </div>
          </div>
          <div className="mb-4 flex flex-wrap items-center gap-2">
            {detail.partial ? <Tag color="blue">部分分</Tag> : null}
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
              <FieldNumberOutlined />
              最大提交次数 {detail.max_submissions ? `${detail.max_submissions} 次` : '不限'}
            </span>
          </div>
          <Markdown content={detail.description} />
        </div>
      ) : null,
    },
    {
      key: 'submissions',
      label: '我的本场提交',
      icon: <HistoryOutlined />,
      children: isLogin() ? (
        <Table
          rowKey="submission_id"

          loading={submissionsLoading}
          columns={submissionsColumns}
          dataSource={mySubmissions}
          pagination={false}
        />
      ) : (
        <Empty description={<span>登录后可查看本场提交</span>}>
          <Link to="/auth/login">
            <Button type="primary">去登录</Button>
          </Link>
        </Empty>
      ),
    },
  ]

  const statementPane = (
    <div className={`panel flex min-w-0 flex-col rounded-md ${isDesktop ? 'h-full' : ''}`}>
      <CustomTabs items={tabItems} contentClassName="p-4" fillHeight={isDesktop} />
    </div>
  )

  const solvePane = (
    <div className={`min-w-0 ${isDesktop ? 'h-full overflow-hidden' : ''}`}>
      <SubmitPanel
        languages={detail?.languages ?? []}
        defaultLanguage={detail?.languages[0]?.language_key}
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
      <SolveContextProvider problemId={problemId}>
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
    <SolveContextProvider problemId={problemId}>
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
