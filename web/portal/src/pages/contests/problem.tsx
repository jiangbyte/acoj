import { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Empty, Table, Tabs, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useParams } from 'react-router-dom'
import { contestMySubmissions, contestProblemDetail, contestSubmit } from '@/api/contest'
import type { PortalContestProblemDetail, PortalContestSubmission } from '@/api/contest'
import { Markdown } from '@/components/common/Markdown'
import { SubmitPanel } from '@/components/oj/SubmitPanel'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useAuthStore } from '@/stores/auth'

const formatMemory = (kb: number) => `${Math.round(kb / 1024)} MB`
const formatTime = (value: string | null) => (value ? new Date(value).toLocaleString() : '-')

export function ContestProblemPage() {
  const { id = '', problemId = '' } = useParams()
  const isLogin = useAuthStore((s) => s.isLogin)
  const userInfo = useAuthStore((s) => s.userInfo)

  const [detail, setDetail] = useState<PortalContestProblemDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [mySubmissions, setMySubmissions] = useState<PortalContestSubmission[]>([])
  const [submissionsLoading, setSubmissionsLoading] = useState(true)

  async function load() {
    try {
      const res = await contestProblemDetail(id, problemId)
      setDetail(res.data)
    } finally {
      setLoading(false)
    }
  }

  async function loadMySubmissions() {
    try {
      const res = await contestMySubmissions(id)
      setMySubmissions(res.data.filter((s) => s.problem_id === problemId))
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
    const res = await contestSubmit(id, { problem_id: problemId, ...payload })
    return res.data
  }

  const submissionsColumns: ColumnsType<PortalContestSubmission> = [
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
      children: detail ? (
        <div>
          <div className="mb-4 flex flex-wrap items-baseline gap-3">
            <Typography.Title level={4} className="!mb-0">
              {detail.label}. {detail.problem_name}
            </Typography.Title>
            {detail.partial ? <Tag color="blue">部分分</Tag> : null}
          </div>
          <Descriptions
            size="small"
            column={{ xs: 1, sm: 2, lg: 4 }}
            className="mb-4"
            items={[
              { key: 'time', label: '时间限制', children: `${detail.time_limit_ms} ms` },
              { key: 'memory', label: '内存限制', children: formatMemory(detail.memory_limit_kb) },
              { key: 'points', label: '分值', children: detail.points },
              {
                key: 'max',
                label: '最大提交次数',
                children: detail.max_submissions ? `${detail.max_submissions} 次` : '不限',
              },
            ]}
          />
          <Markdown content={detail.description} />
        </div>
      ) : (
        <Typography.Text type="secondary">加载中…</Typography.Text>
      ),
    },
    {
      key: 'submissions',
      label: '我的本场提交',
      children: isLogin() ? (
        <Table
          rowKey="submission_id"
          size="small"
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

  return (
    <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_520px]">
      <Card
        className="min-w-0"
        loading={loading}
        title={
          <Link to={`/contests/${id}`} className="text-sm text-gray-500">
            ← 返回竞赛
          </Link>
        }
      >
        <Tabs items={tabItems} />
      </Card>
      <Card className="min-w-0 lg:sticky lg:top-2" styles={{ body: { paddingTop: 16 } }}>
        <SubmitPanel
          languages={detail?.languages ?? []}
          defaultLanguage={detail?.languages[0]?.language_key}
          onSubmit={handleSubmit}
        />
      </Card>
    </div>
  )
}
