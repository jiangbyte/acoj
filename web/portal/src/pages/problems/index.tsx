import { useEffect, useState, type ReactNode } from 'react'
import { Badge, Calendar, Empty, Input, Spin } from 'antd'
import {
  AppstoreOutlined,
  BarChartOutlined,
  BookOutlined,
  CheckCircleFilled,
  CodeOutlined,
  DatabaseOutlined,
  FilterOutlined,
  FolderOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import dayjs, { type Dayjs } from 'dayjs'
import {
  problemGroups,
  problemPage,
  problemTypes,
  type PortalProblemGroupItem,
  type PortalProblemPageData,
  type PortalProblemTypeItem,
  type ProblemDifficulty,
} from '@/api/problem'
import { dailyApi, learningPlanApi, type DailyCalendar, type DailyToday, type LearningPlanItem } from '@/api/study'
import { ProblemBankSidebar } from '@/components/oj/ProblemBankSidebar'
import { useDict } from '@/hooks/useDict'
import { dictTypeData } from '@/utils/dict'

const formatRate = (rate: number) => `${Number(rate || 0).toFixed(1)}%`

const groupIcons: Record<string, ReactNode> = {
  ALGO: <CodeOutlined />,
  DATABASE: <DatabaseOutlined />,
  SHELL: <FolderOutlined />,
  JAVASCRIPT: <CodeOutlined />,
}

/** 缺省难度：优先 API，否则按通过率启发式兜底 */
function normalizeDifficulty(value: string | null | undefined, acRate: number): ProblemDifficulty {
  if (value === 'Easy' || value === 'Medium' || value === 'Hard') return value
  if (Number.isNaN(acRate)) return 'Medium'
  if (acRate >= 50) return 'Easy'
  if (acRate >= 25) return 'Medium'
  return 'Hard'
}

function difficultyClass(level: ProblemDifficulty) {
  if (level === 'Easy') return 'text-[var(--ant-color-diff-easy)]'
  if (level === 'Medium') return 'text-[var(--ant-color-diff-medium)]'
  return 'text-[var(--ant-color-diff-hard)]'
}

function difficultyLabel(level: ProblemDifficulty) {
  return (
    dictTypeData('PROBLEM_DIFFICULTY', level) ||
    (level === 'Easy' ? '简单' : level === 'Medium' ? '中等' : '困难')
  )
}

export function ProblemListPage() {
  useDict()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const groupId = searchParams.get('group_id') ?? ''
  const typeId = searchParams.get('type_id') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 20)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<PortalProblemPageData | null>(null)
  const [groups, setGroups] = useState<PortalProblemGroupItem[]>([])
  const [types, setTypes] = useState<PortalProblemTypeItem[]>([])
  const [searchText, setSearchText] = useState(keyword)
  const [topicsExpanded, setTopicsExpanded] = useState(false)
  const [featuredPlans, setFeaturedPlans] = useState<LearningPlanItem[]>([])
  const [dailyToday, setDailyToday] = useState<DailyToday | null>(null)
  const [dailyCal, setDailyCal] = useState<DailyCalendar | null>(null)
  const [calMonth, setCalMonth] = useState(() => dayjs())

  async function loadMeta() {
    const [groupRes, typeRes, planRes] = await Promise.allSettled([
      problemGroups(),
      problemTypes(),
      learningPlanApi.page({ current: 1, size: 4, category: 'FEATURED' }),
    ])
    if (groupRes.status === 'fulfilled') setGroups(groupRes.value.data)
    if (typeRes.status === 'fulfilled') setTypes(typeRes.value.data)
    if (planRes.status === 'fulfilled') setFeaturedPlans(planRes.value.data?.records ?? [])
  }

  async function loadDaily(month = calMonth) {
    const [todayRes, calRes] = await Promise.allSettled([
      dailyApi.today(),
      dailyApi.calendar(month.year(), month.month() + 1),
    ])
    if (todayRes.status === 'fulfilled') setDailyToday(todayRes.value.data)
    if (calRes.status === 'fulfilled') setDailyCal(calRes.value.data)
  }

  async function load() {
    try {
      const res = await problemPage({
        current,
        size,
        keyword: keyword || undefined,
        group_id: groupId || undefined,
        type_id: typeId || undefined,
      })
      setData(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadMeta()
    void loadDaily()
  }, [])

  useEffect(() => {
    void loadDaily(calMonth)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [calMonth.year(), calMonth.month()])

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, groupId, typeId, current, size])

  useEffect(() => {
    setSearchText(keyword)
  }, [keyword])

  function buildParams(overrides: Record<string, string | undefined> = {}) {
    const next: Record<string, string> = {}
    const merged = {
      keyword,
      group_id: groupId,
      type_id: typeId,
      current: String(current),
      size: String(size),
      ...overrides,
    }
    if (merged.keyword?.trim()) next.keyword = merged.keyword.trim()
    if (merged.group_id) next.group_id = merged.group_id
    if (merged.type_id) next.type_id = merged.type_id
    if (merged.current && merged.current !== '1') next.current = merged.current
    if (merged.size && merged.size !== '20') next.size = merged.size
    return next
  }

  function onSearch(value?: string) {
    const next = (value ?? searchText).trim()
    setLoading(true)
    setSearchParams(buildParams({ keyword: next, current: '1' }))
  }

  function onGroupChange(nextGroupId: string) {
    setLoading(true)
    setSearchParams(buildParams({ group_id: nextGroupId, current: '1' }))
  }

  function onTypeChange(nextTypeId: string) {
    setLoading(true)
    setSearchParams(
      buildParams({
        type_id: nextTypeId === typeId ? '' : nextTypeId,
        current: '1',
      }),
    )
  }

  function onPageChange(nextCurrent: number) {
    setLoading(true)
    setSearchParams(buildParams({ current: String(nextCurrent) }))
  }

  const records = data?.records ?? []
  const total = data?.total ?? 0
  const totalPages = Math.max(1, Math.ceil(total / size))
  const visibleTypes = topicsExpanded ? types : types.slice(0, 10)
  const solvedCount = data?.solved_count ?? 0

  return (
    <div className="page-shell flex w-full gap-4">
      <ProblemBankSidebar active="problems" />

      {/* 中间主内容 */}
      <main className="min-w-0 flex-1">
        <div className="mb-4 grid grid-cols-2 gap-3 xl:grid-cols-4">
          {(featuredPlans.length
            ? featuredPlans
            : [
                { id: '', title: '练习路径', subtitle: '按知识点循序练习', cover_url: null },
              ]
          ).map((plan) => (
            <button
              key={plan.id || `ph-${plan.title}`}
              type="button"
              onClick={() => navigate(plan.id ? `/plans/detail?id=${plan.id}` : '/plans')}
              className="panel rounded-xl border-l-4 border-l-[var(--ant-color-primary)] px-4 py-3.5 text-left transition-all hover:-translate-y-0.5 hover:shadow-[0_6px_16px_rgba(22,119,255,0.1)]"
            >
              <div className="text-sm font-semibold text-[var(--ant-color-text)]">{plan.title}</div>
              <div className="muted-text mt-1 line-clamp-2 text-xs">
                {plan.subtitle || '开始做题'}
              </div>
            </button>
          ))}
        </div>

        <div className="panel mb-4 rounded-xl px-4 py-3">
          <div className="flex flex-wrap items-center gap-x-3 gap-y-2">
            {visibleTypes.map((topic) => (
              <button
                key={topic.id}
                type="button"
                onClick={() => onTypeChange(topic.id)}
                className={`inline-flex items-center gap-1.5 text-sm transition-colors ${
                  typeId === topic.id
                    ? 'font-medium text-[var(--ant-color-text)]'
                    : 'text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]'
                }`}
              >
                {topic.name}
                <span className="rounded-full bg-[var(--ant-color-fill-quaternary)] px-1.5 py-0.5 text-xs tabular-nums">
                  {topic.problem_count}
                </span>
              </button>
            ))}
            {types.length > 10 ? (
              <button
                type="button"
                className="text-sm text-[var(--ant-color-primary)]"
                onClick={() => setTopicsExpanded((v) => !v)}
              >
                {topicsExpanded ? '收起' : '展开'}
              </button>
            ) : null}
          </div>
        </div>

        <div className="mb-3 flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => onGroupChange('')}
            className={`inline-flex items-center gap-1.5 rounded-md px-3.5 py-1.5 text-sm transition-colors ${
              !groupId
                ? 'bg-[var(--ant-color-primary)] text-white'
                : 'bg-[var(--ant-color-bg-container)] text-[var(--ant-color-text-secondary)] ring-1 ring-[var(--ant-color-border)] hover:text-[var(--ant-color-text)]'
            }`}
          >
            <AppstoreOutlined />
            全部题目
          </button>
          {groups.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => onGroupChange(item.id)}
              className={`inline-flex items-center gap-1.5 rounded-md px-3.5 py-1.5 text-sm transition-colors ${
                groupId === item.id
                  ? 'bg-[var(--ant-color-primary)] text-white'
                  : 'bg-[var(--ant-color-bg-container)] text-[var(--ant-color-text-secondary)] ring-1 ring-[var(--ant-color-border)] hover:text-[var(--ant-color-text)]'
              }`}
            >
              {groupIcons[item.code] ?? <FolderOutlined />}
              {item.name}
              <span className="tabular-nums opacity-70">{item.problem_count}</span>
            </button>
          ))}
        </div>

        <div className="panel overflow-hidden rounded-xl">
          <div className="flex flex-wrap items-center gap-3 border-b border-[var(--ant-color-border)] px-4 py-3">
            <Input
              allowClear
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={() => onSearch()}
              prefix={<SearchOutlined className="muted-text" />}
              placeholder="搜索题目"
              className="max-w-md flex-1"
              suffix={
                <button type="button" onClick={() => onSearch()} className="muted-text">
                  <FilterOutlined />
                </button>
              }
            />
            <div className="ml-auto flex items-center gap-3 text-sm text-[var(--ant-color-text-secondary)]">
              <span>
                <span className="font-medium text-[var(--ant-color-text)]">{solvedCount}</span>/{total}{' '}
                已通过
              </span>
            </div>
          </div>

          <Spin spinning={loading}>
            {records.length ? (
              <div>
                {records.map((problem, index) => {
                  const level = normalizeDifficulty(problem.difficulty, problem.ac_rate)
                  const zebra = index % 2 === 1
                  return (
                    <Link
                      key={problem.id}
                      to={`/problems/${problem.id}`}
                      className={`grid grid-cols-[36px_minmax(0,1fr)_88px_64px_36px] items-center gap-2 px-4 py-3.5 transition-colors hover:bg-[var(--ant-color-fill-secondary)] ${
                        zebra ? 'bg-[var(--ant-color-fill-alter)]' : 'bg-[var(--ant-color-bg-container)]'
                      }`}
                    >
                      <span className="flex justify-center text-[var(--ant-color-text-secondary)]">
                        {problem.solved ? (
                          <CheckCircleFilled className="text-[var(--ant-color-diff-easy)]" />
                        ) : (
                          <span className="inline-block h-3.5 w-3.5 rounded-full border border-[var(--ant-color-border-secondary)]" />
                        )}
                      </span>
                      <div className="min-w-0">
                        <div className="truncate text-[15px] font-medium">
                          {problem.code}. {problem.name}
                        </div>
                        {(problem.type_names?.length || problem.group_name) && (
                          <div className="muted-text mt-0.5 truncate text-xs">
                            {[problem.group_name, ...(problem.type_names ?? []).slice(0, 2)]
                              .filter(Boolean)
                              .join(' · ')}
                          </div>
                        )}
                      </div>
                      <span className="text-right text-sm tabular-nums text-[var(--ant-color-text-secondary)]">
                        {formatRate(problem.ac_rate)}
                      </span>
                      <span className={`text-right text-sm ${difficultyClass(level)}`}>
                        {difficultyLabel(level)}
                      </span>
                      <span className="flex justify-center text-[var(--ant-color-text-secondary)]">
                        <BarChartOutlined />
                      </span>
                    </Link>
                  )
                })}
              </div>
            ) : (
              <div className="py-16">
                <Empty description="暂无题目" />
              </div>
            )}
          </Spin>

          <div className="flex items-center justify-between border-t border-[var(--ant-color-border)] px-4 py-3 text-sm">
            <span className="muted-text">共 {total} 题</span>
            <div className="flex items-center gap-2">
              <button
                type="button"
                disabled={current <= 1}
                onClick={() => onPageChange(current - 1)}
                className="rounded-lg px-3 py-1.5 ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
              >
                上一页
              </button>
              <span className="tabular-nums text-[var(--ant-color-text-secondary)]">
                {current} / {totalPages}
              </span>
              <button
                type="button"
                disabled={current >= totalPages}
                onClick={() => onPageChange(current + 1)}
                className="rounded-lg px-3 py-1.5 ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* 右侧小组件 */}
      <aside className="hidden w-[280px] shrink-0 xl:block">
        <div className="sticky top-[80px] flex flex-col gap-4">
          <div className="panel rounded-xl p-3">
            <div className="mb-2 flex items-center justify-between">
              <span className="text-sm font-medium">今日练习</span>
              <BookOutlined className="text-[var(--ant-color-primary)]" />
            </div>
            {dailyToday?.problem ? (
              <Link
                to={`/problems/${dailyToday.problem.problem_id}`}
                className="mb-2 block rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2 hover:bg-[var(--ant-color-fill-secondary)]"
              >
                <div className="text-xs text-[var(--ant-color-text-secondary)]">今日题目</div>
                <div className="truncate text-sm font-medium">
                  {dailyToday.problem.problem_code}. {dailyToday.problem.problem_name}
                </div>
                <div className="mt-1 text-xs">
                  {dailyToday.checked_in ? (
                    <span className="text-[var(--ant-color-diff-easy)]">已完成</span>
                  ) : (
                    <span className="text-[var(--ant-color-warning)]">未完成</span>
                  )}
                  <span className="muted-text ml-2">连续 {dailyToday.streak} 天</span>
                </div>
              </Link>
            ) : (
              <div className="muted-text mb-2 px-1 text-xs">今日暂未布置题目</div>
            )}
            <Calendar
              fullscreen={false}
              value={calMonth}
              onPanelChange={(v) => setCalMonth(v)}
              onSelect={(v) => setCalMonth(v)}
              className="problem-calendar"
              fullCellRender={(date: Dayjs) => {
                const key = date.format('YYYY-MM-DD')
                const day = dailyCal?.days.find((d) => d.day_date.slice(0, 10) === key)
                return (
                  <div className="ant-picker-cell-inner">
                    <Badge
                      dot={Boolean(day?.has_problem)}
                      color={day?.checked_in ? 'green' : day?.has_problem ? 'orange' : undefined}
                    >
                      {date.date()}
                    </Badge>
                  </div>
                )
              }}
            />
            <div className="mt-2 rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2">
              <div className="mb-1 flex justify-between text-xs">
                <span className="muted-text">本月进度</span>
                <span>
                  {dailyToday?.month_done ?? dailyCal?.month_done ?? 0} /{' '}
                  {dailyToday?.month_total ?? dailyCal?.month_total ?? 0}
                </span>
              </div>
              <div className="h-1.5 overflow-hidden rounded-full bg-[var(--ant-color-border)]">
                <div
                  className="h-full rounded-full bg-[var(--ant-color-diff-easy)]"
                  style={{
                    width: `${
                      (dailyToday?.month_total || dailyCal?.month_total || 0)
                        ? Math.round(
                            ((dailyToday?.month_done ?? dailyCal?.month_done ?? 0) /
                              (dailyToday?.month_total ?? dailyCal?.month_total ?? 1)) *
                              100,
                          )
                        : 0
                    }%`,
                  }}
                />
              </div>
            </div>
          </div>
        </div>
      </aside>

      <style>{`
        .problem-calendar .ant-picker-calendar-header {
          padding: 0 0 8px;
        }
        .problem-calendar .ant-picker-panel {
          border-top: none;
        }
        .problem-calendar .ant-picker-content th,
        .problem-calendar .ant-picker-content td {
          font-size: 12px;
        }
      `}</style>
    </div>
  )
}
