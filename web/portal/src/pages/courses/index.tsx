import { useEffect, useState } from 'react'
import { Empty, Input, Spin, Tag } from 'antd'
import { BookOutlined, SearchOutlined } from '@ant-design/icons'
import { Link, useSearchParams } from 'react-router-dom'
import { coursePage } from '@/api/course'
import type { PortalCourseBrief } from '@/api/course'
import { formatDateMinute } from '@/utils/time'

const thumbTones = [
  'from-[var(--ant-color-primary)] to-[var(--ant-color-primary-hover)]',
  'from-[var(--ant-color-info)] to-[var(--ant-color-info-hover)]',
  'from-[var(--ant-color-success)] to-[var(--ant-color-success-hover)]',
  'from-[var(--ant-color-warning)] to-[var(--ant-color-warning-hover)]',
  'from-[var(--ant-color-error)] to-[var(--ant-color-error-active)]',
]

export function CourseListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 12)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<PortalCourseBrief[]>([])
  const [total, setTotal] = useState(0)
  const [searchText, setSearchText] = useState(keyword)

  const totalPages = Math.max(1, Math.ceil(total / size))

  async function loadPublic() {
    setLoading(true)
    try {
      const res = await coursePage({ current, size, keyword: keyword || undefined })
      setData(res.data.records ?? [])
      setTotal(res.data.total ?? 0)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadPublic()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, current, size])

  function onSearch() {
    const params: Record<string, string> = {}
    if (searchText.trim()) params.keyword = searchText.trim()
    setSearchParams(params)
  }

  return (
    <div className="page-shell w-full">
      <header className="panel mb-5 rounded-xl p-6">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <h1 className="text-2xl font-semibold">公开课程</h1>
            <p className="muted-text mt-1 text-sm">浏览全站公开课；交作业与课内小组需登录后参与</p>
          </div>
          <Input
            className="max-w-md"
            allowClear
            size="large"
            prefix={<SearchOutlined />}
            placeholder="搜索课程名称 / 简介"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onPressEnter={onSearch}
            addonAfter={
              <button type="button" className="px-2" onClick={onSearch}>
                搜索
              </button>
            }
          />
        </div>
      </header>

      <Spin spinning={loading}>
        {!data.length && !loading ? (
          <div className="panel rounded-xl p-10">
            <Empty description="暂无公开课程" />
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {data.map((item, index) => (
              <Link
                key={item.id}
                to={`/courses/${item.id}`}
                className="panel group overflow-hidden rounded-xl transition hover:-translate-y-0.5 hover:shadow-md"
              >
                <div
                  className={`flex h-28 items-end bg-gradient-to-br px-4 py-3 text-white ${thumbTones[index % thumbTones.length]}`}
                >
                  <div className="flex items-center gap-2">
                    <BookOutlined />
                    <Tag className="!m-0" color="processing">
                      公开课
                    </Tag>
                  </div>
                </div>
                <div className="space-y-2 p-4">
                  <h2 className="line-clamp-1 text-lg font-semibold group-hover:text-[var(--ant-color-primary)]">
                    {item.name}
                  </h2>
                  <p className="muted-text line-clamp-2 min-h-10 text-sm">{item.summary || '暂无简介'}</p>
                  <div className="muted-text text-xs">{formatDateMinute(item.updated_at)}</div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </Spin>

      {totalPages > 1 ? (
        <div className="mt-5 flex justify-center gap-2">
          <button
            type="button"
            className="panel rounded-lg px-3 py-1 text-sm disabled:opacity-40"
            disabled={current <= 1}
            onClick={() => {
              const next = new URLSearchParams(searchParams)
              next.set('current', String(current - 1))
              setSearchParams(next)
            }}
          >
            上一页
          </button>
          <span className="muted-text self-center text-sm">
            {current} / {totalPages}
          </span>
          <button
            type="button"
            className="panel rounded-lg px-3 py-1 text-sm disabled:opacity-40"
            disabled={current >= totalPages}
            onClick={() => {
              const next = new URLSearchParams(searchParams)
              next.set('current', String(current + 1))
              setSearchParams(next)
            }}
          >
            下一页
          </button>
        </div>
      ) : null}
    </div>
  )
}
