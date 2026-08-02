import { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Empty, Table, Tabs, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useParams } from 'react-router-dom'
import { problemDetail, problemLanguages, problemSubmit } from '@/api/problem'
import type { PortalProblemDetail, PortalProblemLanguage } from '@/api/problem'
import { submissionPage } from '@/api/submission'
import type { OjSubmissionListItem } from '@/api/submission'
import { Markdown } from '@/components/common/Markdown'
import { SubmitPanel } from '@/components/oj/SubmitPanel'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useAuthStore } from '@/stores/auth'

const formatMemory = (kb: number) => `${Math.round(kb / 1024)} MB`
const formatRate = (rate: number) => `${rate.toFixed(1)}%`

export function ProblemDetailPage() {
  const { id = '' } = useParams()
  const userInfo = useAuthStore((s) => s.userInfo)
  const isLogin = useAuthStore((s) => s.isLogin)

  const [detail, setDetail] = useState<PortalProblemDetail | null>(null)
  const [languages, setLanguages] = useState<PortalProblemLanguage[]>([])
  const [loading, setLoading] = useState(true)
  const [mySubmissions, setMySubmissions] = useState<OjSubmissionListItem[]>([])
  const [submissionsLoading, setSubmissionsLoading] = useState(true)

  async function load() {
    try {
      const [detailRes, langRes] = await Promise.all([problemDetail(id), problemLanguages(id)])
      setDetail(detailRes.data)
      setLanguages(langRes.data)
    } finally {
      setLoading(false)
    }
  }

  async function loadMySubmissions() {
    try {
      const res = await submissionPage({
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
    if (isLogin()) {
      void loadMySubmissions()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, userInfo?.accountId])

  async function handleSubmit(payload: { language_key: string; source: string }) {
    const res = await problemSubmit(id, payload)
    return res.data
  }

  const submissionsColumns: ColumnsType<OjSubmissionListItem> = [
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
          {createdAt ? new Date(createdAt).toLocaleString() : '-'}
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
              {detail.code}. {detail.name}
            </Typography.Title>
            {detail.partial ? <Tag color="blue">部分分</Tag> : null}
            {detail.type_names.map((name) => (
              <Tag key={name}>{name}</Tag>
            ))}
          </div>
          <Descriptions
            size="small"
            column={{ xs: 1, sm: 2, lg: 4 }}
            className="mb-4"
            items={[
              { key: 'time', label: '时间限制', children: `${detail.time_limit_ms} ms` },
              { key: 'memory', label: '内存限制', children: formatMemory(detail.memory_limit_kb) },
              { key: 'points', label: '分值', children: detail.points },
              { key: 'rate', label: '通过率', children: formatRate(detail.ac_rate) },
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
      label: '提交记录',
      children: isLogin() ? (
        <Table
          rowKey="id"
          size="small"
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

  return (
    <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_520px]">
      <Card className="min-w-0" loading={loading}>
        <Tabs items={tabItems} />
      </Card>
      <Card className="min-w-0 lg:sticky lg:top-2" styles={{ body: { paddingTop: 16 } }}>
        <SubmitPanel
          languages={languages}
          defaultLanguage={languages[0]?.language_key}
          onSubmit={handleSubmit}
        />
      </Card>
    </div>
  )
}
