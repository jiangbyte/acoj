import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { Empty, Input, Spin, Tag } from 'antd'
import {
  CalendarOutlined,
  ClockCircleOutlined,
  SearchOutlined,
  TeamOutlined,
  TrophyOutlined,
} from '@ant-design/icons'
import { Link, useSearchParams } from 'react-router-dom'
import dayjs from 'dayjs'
import { contestApi } from '@/api'
import { PromoCarousel } from '@/components/common/PromoCarousel'
import { ContestStatusBadge } from '@/components/oj/ContestStatusBadge'
import { useBannerSlides } from '@/hooks/useBannerSlides'
import { useDict } from '@/hooks/useDict'
import { useAuthStore } from '@/stores/auth'
import { dictList, dictTypeData } from '@/utils/dict'
import { formatDateMinute } from '@/utils/time'

const formatTime = (value: string | null) => formatDateMinute(value)

const thumbTones = [
  'from-[var(--ant-color-error)] to-[var(--ant-color-error-active)]',
  'from-[var(--ant-color-primary)] to-[var(--ant-color-primary-hover)]',
  'from-[var(--ant-color-info)] to-[var(--ant-color-info-hover)]',
  'from-[var(--ant-color-success)] to-[var(--ant-color-success-hover)]',
  'from-[var(--ant-color-warning)] to-[var(--ant-color-warning-hover)]',
]

function countdownText(start: string, end: string, status: string) {
  const now = dayjs()
  const startAt = dayjs(start)
  const endAt = dayjs(end)
  if (status === 'RUNNING') {
    const diff = endAt.diff(now, 'second')
    if (diff <= 0) return '即将结束'
    return `距结束 ${formatDuration(diff)}`
  }
  if (status === 'SCHEDULED') {
    const diff = startAt.diff(now, 'second')
    if (diff <= 0) return '即将开始'
    return `距比赛 ${formatDuration(diff)}`
  }
  return '比赛已结束'
}

function formatDuration(totalSeconds: number) {
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  const hh = String(hours).padStart(2, '0')
  const mm = String(minutes).padStart(2, '0')
  const ss = String(seconds).padStart(2, '0')
  if (days > 0) return `${days}天 ${hh}:${mm}:${ss}`
  return `${hh}:${mm}:${ss}`
}

function Chip({
  active,
  children,
  onClick,
}: {
  active: boolean
  children: ReactNode
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-md px-3 py-1 text-sm transition-colors ${
        active
          ? 'bg-[var(--ant-color-primary)] text-white'
          : 'text-[var(--ant-color-text-secondary)] hover:bg-[var(--ant-color-fill-secondary)] hover:text-[var(--ant-color-text)]'
      }`}
    >
      {children}
    </button>
  )
}

export function ContestListPage() {
  const dictTree = useDict()
  const isLogin = useAuthStore((s) => s.isLogin)
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 10)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [searchText, setSearchText] = useState(keyword)
  const [formatFilter, setFormatFilter] = useState('all')
  const [typeFilter, setTypeFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')

  const formatFilters = useMemo(
    () => [{ key: 'all', label: '全部' }, ...dictList('CONTEST_FORMAT').map((d: any) => ({ key: String(d.value), label: d.label }))],
    [dictTree],
  )
  const typeFilters = useMemo(
    () => [{ key: 'all', label: '全部' }, ...dictList('CONTEST_TYPE').map((d: any) => ({ key: String(d.value), label: d.label }))],
    [dictTree],
  )
  const statusFilters = useMemo(
    () => [
      { key: 'all', label: '全部' },
      ...dictList('CONTEST_LIFECYCLE_STATUS')
        .filter((d: any) => d.value !== 'LOCKED')
        .map((d: any) => ({ key: String(d.value), label: d.label })),
    ],
    [dictTree],
  )

  const [myContests, setMyContests] = useState<any[]>([])

  async function load() {
    try {
      const res = await contestApi.contestPage({ current, size, keyword: keyword || undefined })
      setData(res.data.records)
      setTotal(res.data.total)
    } finally {
      setLoading(false)
    }
  }

  async function loadMine() {
    try {
      const res = await contestApi.contestMine({ current: 1, size: 20 })
      setMyContests(res.data.records ?? [])
    } catch {
      setMyContests([])
    }
  }

  useEffect(() => {
    void load()
    if (isLogin()) {
      void loadMine()
    } else {
      setMyContests([])
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, current, size, isLogin()])

  function onSearch() {
    setLoading(true)
    const params: Record<string, string> = {}
    if (searchText.trim()) params.keyword = searchText.trim()
    setSearchParams(params)
  }

  const filtered = useMemo(() => {
    return data.filter((item) => {
      if (formatFilter !== 'all' && item.format_name !== formatFilter) return false
      if (statusFilter !== 'all' && item.lifecycle_status !== statusFilter) return false
      if (typeFilter === 'RATED' && !item.is_rated) return false
      if (typeFilter === 'UNRATED' && item.is_rated) return false
      if (typeFilter === 'PRIVATE' && !item.is_private) return false
      return true
    })
  }, [data, formatFilter, statusFilter, typeFilter])

  const summary = useMemo(() => {
    const scheduled = data.filter((c) => c.lifecycle_status === 'SCHEDULED').length
    const running = data.filter((c) => c.lifecycle_status === 'RUNNING').length
    const ended = data.filter((c) => c.lifecycle_status === 'ENDED').length
    return { scheduled, running, ended }
  }, [data])

  const calendarItems = useMemo(() => {
    return [...myContests]
      .filter((c) => c.start_time)
      .sort((a, b) => dayjs(a.start_time).valueOf() - dayjs(b.start_time).valueOf())
      .slice(0, 8)
  }, [myContests])

  const totalPages = Math.max(1, Math.ceil(total / size))
  const { slides: heroSlides } = useBannerSlides({
    position: 'CONTESTS_TOP',
    type: 'CAROUSEL',
  })

  return (
    <div className="page-shell grid w-full gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
      {/* 左栏：轮播 + 列表（与首页同构） */}
      <div className="min-w-0 flex flex-col gap-5">
        <PromoCarousel slides={heroSlides} height={220} />

        <section className="flex flex-col gap-4">
          <Input
            allowClear
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onPressEnter={() => onSearch()}
            placeholder="请输入竞赛名称 / 关键字搜索"
            prefix={<SearchOutlined className="muted-text" />}
            suffix={
              <button
                type="button"
                onClick={() => onSearch()}
                className="rounded-md bg-[var(--ant-color-primary)] px-3 py-1 text-sm text-white"
              >
                搜索
              </button>
            }
            className="h-11"
          />

          <div className="grid gap-3 sm:grid-cols-3">
            {[
              {
                title: '官方系列赛',
                desc: summary.scheduled
                  ? `下一场可报名 · ${summary.scheduled} 场待开始`
                  : '暂无待开始赛事',
                badge: '可以报名',
              },
              {
                title: '进行中的比赛',
                desc: summary.running
                  ? `${summary.running} 场正在进行`
                  : '当前没有进行中的比赛',
                badge: summary.running ? '进行中' : '暂无',
              },
              {
                title: '练习与复现',
                desc: `${total} 场竞赛可浏览，结束后可回顾榜单`,
                badge: '开放浏览',
              },
            ].map((card) => (
              <div key={card.title} className="panel rounded-xl p-4">
                <div className="flex items-start justify-between gap-2">
                  <div className="text-base font-semibold">{card.title}</div>
                  <Tag color="cyan" className="m-0">
                    {card.badge}
                  </Tag>
                </div>
                <div className="muted-text mt-2 text-sm">{card.desc}</div>
              </div>
            ))}
          </div>
        </section>

        <section className="panel space-y-3 rounded-xl px-4 py-3">
          <div className="flex flex-wrap items-center gap-2">
            <span className="w-12 shrink-0 text-sm text-[var(--ant-color-text-secondary)]">赛制</span>
            {formatFilters.map((item) => (
              <Chip
                key={item.key}
                active={formatFilter === item.key}
                onClick={() => setFormatFilter(item.key)}
              >
                {item.label}
              </Chip>
            ))}
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="w-12 shrink-0 text-sm text-[var(--ant-color-text-secondary)]">类型</span>
            {typeFilters.map((item) => (
              <Chip
                key={item.key}
                active={typeFilter === item.key}
                onClick={() => setTypeFilter(item.key)}
              >
                {item.label}
              </Chip>
            ))}
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="w-12 shrink-0 text-sm text-[var(--ant-color-text-secondary)]">状态</span>
            {statusFilters.map((item) => (
              <Chip
                key={item.key}
                active={statusFilter === item.key}
                onClick={() => setStatusFilter(item.key)}
              >
                {item.label}
              </Chip>
            ))}
          </div>
        </section>

        <section className="min-w-0">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold">等你来战</h2>
            <span className="muted-text text-sm">共 {total} 场</span>
          </div>

          <Spin spinning={loading}>
            {filtered.length ? (
              <div className="space-y-4">
                {filtered.map((contest, index) => {
                  const tone = thumbTones[index % thumbTones.length]
                  const cta =
                    contest.lifecycle_status === 'RUNNING'
                      ? '进入'
                      : contest.lifecycle_status === 'SCHEDULED'
                        ? contest.joined
                          ? '已报名'
                          : '报名'
                        : '查看'
                  return (
                    <article
                      key={contest.id}
                      className="panel flex flex-col gap-4 rounded-xl p-4 md:flex-row md:items-stretch"
                    >
                      <div
                        className={`flex h-28 w-full shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${tone} text-3xl text-white md:h-auto md:w-28`}
                      >
                        <TrophyOutlined />
                      </div>

                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <Link
                            to={`/contests/${contest.id}`}
                            className="text-lg font-semibold hover:text-[var(--ant-color-primary)]"
                          >
                            {contest.name}
                          </Link>
                          <ContestStatusBadge status={contest.lifecycle_status} />
                          {contest.is_rated ? (
                            <Tag color="gold">{dictTypeData('CONTEST_TYPE', 'RATED') || '计分'}</Tag>
                          ) : null}
                          {contest.is_private ? (
                            <Tag color="orange">{dictTypeData('CONTEST_TYPE', 'PRIVATE') || '私有'}</Tag>
                          ) : null}
                          <Tag>{dictTypeData('CONTEST_FORMAT', contest.format_name) || contest.format_name || '-'}</Tag>
                        </div>
                        <div className="muted-text mt-2 line-clamp-2 text-sm">
                          {contest.summary || '暂无简介'}
                        </div>
                        <div className="muted-text mt-3 space-y-1 text-sm">
                          <div className="flex items-center gap-2">
                            <CalendarOutlined />
                            比赛时间：{formatTime(contest.start_time)} ~ {formatTime(contest.end_time)}
                          </div>
                          <div className="flex items-center gap-2">
                            <TeamOutlined />
                            报名人数：{contest.user_count}
                          </div>
                          <div className="flex items-center gap-2">
                            <ClockCircleOutlined />
                            {countdownText(
                              contest.start_time,
                              contest.end_time,
                              contest.lifecycle_status,
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="flex shrink-0 flex-col items-stretch justify-center gap-2 md:w-36">
                        <Link
                          to={`/contests/${contest.id}`}
                          className="rounded-lg bg-[var(--ant-color-primary)] px-4 py-2.5 text-center text-sm font-medium text-white hover:bg-[var(--ant-color-primary-hover)]"
                        >
                          {cta}
                        </Link>
                        <div className="text-center text-xs text-[var(--ant-color-text-secondary)]">
                          {countdownText(
                            contest.start_time,
                            contest.end_time,
                            contest.lifecycle_status,
                          )}
                        </div>
                      </div>
                    </article>
                  )
                })}
              </div>
            ) : (
              <div className="panel rounded-xl py-16">
                <Empty description="暂无竞赛" />
              </div>
            )}
          </Spin>

          {total > 0 ? (
            <div className="mt-4 flex items-center justify-between">
              <span className="muted-text text-sm">
                第 {current} / {totalPages} 页
              </span>
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={current <= 1}
                  onClick={() => {
                    setLoading(true)
                    setSearchParams({
                      ...(keyword ? { keyword } : {}),
                      current: String(current - 1),
                      size: String(size),
                    })
                  }}
                  className="rounded-lg px-3 py-1.5 text-sm ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
                >
                  上一页
                </button>
                <button
                  type="button"
                  disabled={current >= totalPages}
                  onClick={() => {
                    setLoading(true)
                    setSearchParams({
                      ...(keyword ? { keyword } : {}),
                      current: String(current + 1),
                      size: String(size),
                    })
                  }}
                  className="rounded-lg px-3 py-1.5 text-sm ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
                >
                  下一页
                </button>
              </div>
            </div>
          ) : null}
        </section>
      </div>

      {/* 右栏：轮播右侧为我的比赛 */}
      <aside className="flex flex-col gap-4">
        <div
          className="panel flex flex-col overflow-hidden rounded-xl"
          style={{ height: 220 }}
        >
          <div className="flex shrink-0 items-center justify-between border-b border-[var(--ant-color-border)] px-4 py-2.5">
            <h3 className="text-base font-semibold">我的比赛</h3>
            <span className="muted-text text-xs">{myContests.length} 场</span>
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto">
            {myContests.length ? (
              <div>
                {myContests.map((contest) => (
                  <Link
                    key={contest.id}
                    to={`/contests/${contest.id}`}
                    className="flex items-center gap-2 border-b border-[var(--ant-color-border)] px-4 py-2.5 last:border-b-0 hover:bg-[var(--ant-color-fill-secondary)]"
                  >
                    <TrophyOutlined className="shrink-0 text-[var(--ant-color-warning)]" />
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-sm font-medium">{contest.name}</div>
                      <div className="muted-text mt-0.5 truncate text-xs">
                        {countdownText(
                          contest.start_time,
                          contest.end_time,
                          contest.lifecycle_status,
                        )}
                      </div>
                    </div>
                    <ContestStatusBadge status={contest.lifecycle_status} />
                  </Link>
                ))}
              </div>
            ) : (
              <div className="flex h-full flex-col items-center justify-center gap-2 px-4">
                {isLogin() ? (
                  <Empty
                    description="暂无已报名比赛"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  />
                ) : (
                  <>
                    <Empty
                      description="登录后查看已报名比赛"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                    <Link
                      to="/auth/login"
                      className="text-sm text-[var(--ant-color-primary)]"
                    >
                      去登录
                    </Link>
                  </>
                )}
              </div>
            )}
          </div>
        </div>

        <div className="panel rounded-xl p-4">
          <div className="mb-3 flex items-center gap-2 text-base font-semibold">
            <CalendarOutlined />
            比赛日历
          </div>
          {calendarItems.length ? (
            <div className="space-y-3">
              {calendarItems.map((contest) => (
                <Link
                  key={contest.id}
                  to={`/contests/${contest.id}`}
                  className="block rounded-lg border border-[var(--ant-color-border)] px-3 py-2 hover:bg-[var(--ant-color-fill-secondary)]"
                >
                  <div className="muted-text text-xs">
                    {formatTime(contest.start_time)}
                  </div>
                  <div className="mt-1 truncate text-sm font-medium">{contest.name}</div>
                  <div className="mt-1">
                    <ContestStatusBadge status={contest.lifecycle_status} />
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <Empty
              description={isLogin() ? '暂无已报名赛程' : '登录并报名后可查看赛程'}
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          )}
        </div>

        <div className="rounded-xl bg-[var(--ant-color-primary-bg)] px-4 py-4">
          <div className="text-sm font-semibold text-[var(--ant-color-primary)]">新手入门清单</div>
          <div className="mt-1 text-xs text-[var(--ant-color-primary-text)]">
            先完成一场练习赛，熟悉报名、提交与榜单流程。
          </div>
          <Link
            to="/problems"
            className="mt-3 inline-flex rounded-lg bg-[var(--ant-color-primary)] px-3 py-1.5 text-sm text-white"
          >
            开始挑战
          </Link>
        </div>
      </aside>
    </div>
  )
}
