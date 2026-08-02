import { useEffect, useState } from 'react'
import { Card, Input, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useSearchParams } from 'react-router-dom'
import { problemPage } from '@/api/problem'
import type { PortalProblemListItem } from '@/api/problem'
import type { PageData } from '@/typing/api'

const formatRate = (rate: number) => `${rate.toFixed(1)}%`

export function ProblemListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 20)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<PageData<PortalProblemListItem> | null>(null)
  const [searchText, setSearchText] = useState(keyword)

  async function load() {
    try {
      const res = await problemPage({ current, size, keyword: keyword || undefined })
      setData(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, current, size])

  function onSearch() {
    setLoading(true)
    setSearchParams(searchText ? { keyword: searchText } : {})
  }

  function onPageChange(nextCurrent: number, nextSize: number) {
    setLoading(true)
    setSearchParams({ keyword, current: String(nextCurrent), size: String(nextSize) })
  }

  const columns: ColumnsType<PortalProblemListItem> = [
    {
      title: '题号',
      dataIndex: 'code',
      width: 110,
      render: (code: string, record) => (
        <Link to={`/problems/${record.id}`} className="font-mono text-sm">
          {code}
        </Link>
      ),
    },
    {
      title: '标题',
      dataIndex: 'name',
      render: (name: string, record) => (
        <Link to={`/problems/${record.id}`} className="text-sm">
          {name}
        </Link>
      ),
    },
    {
      title: '通过率',
      dataIndex: 'ac_rate',
      width: 110,
      align: 'right',
      render: (rate: number) => <Typography.Text type="secondary">{formatRate(rate)}</Typography.Text>,
    },
    {
      title: '通过人数',
      dataIndex: 'user_count',
      width: 100,
      align: 'right',
      render: (count: number) => <Typography.Text type="secondary">{count}</Typography.Text>,
    },
    {
      title: '标签',
      dataIndex: 'type_names',
      width: 220,
      render: (names: string[]) => (
        <div className="flex flex-wrap gap-1">
          {names.map((name) => (
            <Tag key={name} className="m-0">
              {name}
            </Tag>
          ))}
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <Card size="small">
        <div className="flex items-center gap-2">
          <Typography.Title level={4} className="!mb-0">
            题库
          </Typography.Title>
          <div className="flex-1" />
          <Input.Search
            className="w-64"
            placeholder="搜索题号 / 标题"
            allowClear
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onSearch={onSearch}
          />
        </div>
      </Card>

      <Card size="small" className="!p-0">
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={data?.records ?? []}
          pagination={{
            current,
            pageSize: size,
            total: data?.total ?? 0,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 题`,
            onChange: onPageChange,
          }}
        />
      </Card>
    </div>
  )
}
