import { useEffect, useState } from 'react'
import { Card, Input, Select, Table, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useSearchParams } from 'react-router-dom'
import { submissionPage } from '@/api/submission'
import type { OjSubmissionListItem } from '@/api/submission'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { languageLabel } from '@/utils/monacoLanguage'
import type { PageData } from '@/typing/api'

const resultOptions = [
  { value: 'AC', label: '通过' },
  { value: 'WA', label: '答案错误' },
  { value: 'TLE', label: '超时' },
  { value: 'MLE', label: '超内存' },
  { value: 'RE', label: '运行错误' },
  { value: 'CE', label: '编译错误' },
  { value: 'OLE', label: '输出超限' },
  { value: 'SE', label: '系统错误' },
]

const formatTime = (value: string) => (value ? new Date(value).toLocaleString() : '-')

export function SubmissionListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const problemCode = searchParams.get('problem_code') ?? ''
  const result = searchParams.get('result') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 20)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<PageData<OjSubmissionListItem> | null>(null)
  const [codeText, setCodeText] = useState(problemCode)

  async function load() {
    try {
      const res = await submissionPage({
        current,
        size,
        problem_code: problemCode || undefined,
        result: result || undefined,
      })
      setData(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [problemCode, result, current, size])

  function applyFilter(next: { problem_code?: string; result?: string }) {
    setLoading(true)
    const params: Record<string, string> = {}
    if (next.problem_code) params.problem_code = next.problem_code
    if (next.result) params.result = next.result
    setSearchParams(params)
  }

  const columns: ColumnsType<OjSubmissionListItem> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
      render: (id: string) => (
        <Link to={`/submissions/${id}`} className="font-mono text-xs">
          {id.length > 12 ? id.slice(0, 12) : id}
        </Link>
      ),
    },
    {
      title: '题号',
      dataIndex: 'problem_code',
      width: 110,
      render: (code: string | null, record) => (
        <Link to={`/problems/${record.problem_id}`} className="font-mono text-sm">
          {code ?? '-'}
        </Link>
      ),
    },
    {
      title: '用户',
      dataIndex: 'user_nickname',
      width: 130,
      render: (nickname: string | null) => <span>{nickname || '-'}</span>,
    },
    {
      title: '语言',
      dataIndex: 'language_key',
      width: 110,
      render: (key: string) => <span>{languageLabel(key)}</span>,
    },
    {
      title: '结果',
      dataIndex: 'result',
      width: 120,
      render: (res: string | null, record) => (
        <VerdictBadge status={record.status} result={res} />
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
      width: 90,
      align: 'right',
      render: (time: number) => <Typography.Text type="secondary">{time} ms</Typography.Text>,
    },
    {
      title: '内存',
      dataIndex: 'memory_kb',
      width: 90,
      align: 'right',
      render: (kb: number) => <Typography.Text type="secondary">{(kb / 1024).toFixed(1)} MB</Typography.Text>,
    },
    {
      title: '提交时间',
      dataIndex: 'created_at',
      width: 170,
      render: (createdAt: string) => (
        <Typography.Text type="secondary" className="text-xs">
          {formatTime(createdAt)}
        </Typography.Text>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <Card size="small">
        <div className="flex items-center gap-2">
          <Typography.Title level={4} className="!mb-0">
            提交记录
          </Typography.Title>
          <div className="flex-1" />
          <Input.Search
            className="w-52"
            placeholder="题号"
            allowClear
            value={codeText}
            onChange={(e) => setCodeText(e.target.value)}
            onSearch={(value) => applyFilter({ problem_code: value })}
          />
          <Select
            className="w-36"
            placeholder="结果"
            allowClear
            value={result || undefined}
            options={resultOptions}
            onChange={(value) => applyFilter({ result: value })}
          />
        </div>
      </Card>

      <Card size="small" className="!p-0">
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={data?.records ?? []}
          scroll={{ x: 'max-content' }}
          pagination={{
            current,
            pageSize: size,
            total: data?.total ?? 0,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (nextCurrent, nextSize) => {
              setLoading(true)
              const params: Record<string, string> = {}
              if (problemCode) params.problem_code = problemCode
              if (result) params.result = result
              params.current = String(nextCurrent)
              params.size = String(nextSize)
              setSearchParams(params)
            },
          }}
        />
      </Card>
    </div>
  )
}
