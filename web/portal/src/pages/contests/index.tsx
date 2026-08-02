import { useEffect, useState } from 'react'
import { Card, Empty, Input, Pagination, Spin, Tag, Typography } from 'antd'
import { Link, useSearchParams } from 'react-router-dom'
import { contestPage } from '@/api/contest'
import type { PortalContestBrief } from '@/api/contest'
import { ContestStatusBadge } from '@/components/oj/ContestStatusBadge'

const formatTime = (value: string | null) => (value ? new Date(value).toLocaleString() : '-')

export function ContestListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 12)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<PortalContestBrief[]>([])
  const [total, setTotal] = useState(0)
  const [searchText, setSearchText] = useState(keyword)

  async function load() {
    try {
      const res = await contestPage({ current, size, keyword: keyword || undefined })
      setData(res.data.records)
      setTotal(res.data.total)
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

  return (
    <div className="space-y-4">
      <Card size="small">
        <div className="flex items-center gap-2">
          <Typography.Title level={4} className="!mb-0">
            竞赛
          </Typography.Title>
          <div className="flex-1" />
          <Input.Search
            className="w-64"
            placeholder="搜索竞赛名称 / 关键字"
            allowClear
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onSearch={onSearch}
          />
        </div>
      </Card>

      <Spin spinning={loading}>
        {data.length === 0 ? (
          <Card>
            <Empty description="暂无竞赛" />
          </Card>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {data.map((contest) => (
              <Link key={contest.id} to={`/contests/${contest.id}`} className="block no-underline">
                <Card hoverable className="h-full">
                  <div className="flex items-center justify-between gap-2">
                    <Typography.Text strong className="truncate">
                      {contest.name}
                    </Typography.Text>
                    <ContestStatusBadge status={contest.lifecycle_status} />
                  </div>
                  <div className="mt-2 text-sm text-gray-500">{contest.summary || '暂无简介'}</div>
                  <div className="mt-4 space-y-1 text-xs text-gray-500">
                    <div>
                      时间：{formatTime(contest.start_time)} ~ {formatTime(contest.end_time)}
                    </div>
                    <div>
                      赛制：{contest.format_name}
                      {contest.is_rated ? <Tag className="ml-2">Rated</Tag> : null}
                      {contest.is_private ? <Tag color="orange" className="ml-1">私有</Tag> : null}
                    </div>
                    <div>报名人数：{contest.user_count}</div>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </Spin>

      {total > 0 ? (
        <div className="flex justify-end">
          <Pagination
            current={current}
            pageSize={size}
            total={total}
            showSizeChanger
            showTotal={(t) => `共 ${t} 场`}
            onChange={(nextCurrent, nextSize) => {
              setLoading(true)
              setSearchParams({
                keyword,
                current: String(nextCurrent),
                size: String(nextSize),
              })
            }}
          />
        </div>
      ) : null}
    </div>
  )
}
