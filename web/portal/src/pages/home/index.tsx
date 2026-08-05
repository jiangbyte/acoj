import { useEffect, useMemo, useState } from 'react'
import { Empty, Modal, Skeleton, Tag } from 'antd'
import {
  BookOutlined,
  CodeOutlined,
  DatabaseOutlined,
  HistoryOutlined,
  NotificationOutlined,
  RightOutlined,
  TrophyOutlined,
} from '@ant-design/icons'
import { Link } from 'react-router-dom'
import dayjs from 'dayjs'
import { contestPage, type PortalContestBrief } from '@/api/contest'
import {
  announcementApi,
  type PortalAnnouncement,
} from '@/api/message/announcement'
import { problemRecommend, type PortalProblemRecommendItem } from '@/api/problem'
import { submissionPage, type OjSubmissionListItem } from '@/api/submission'
import {
  dailyApi,
  learningPlanApi,
  userStatsApi,
  type DailyToday,
  type LearningPlanItem,
  type UserHeatmap,
  type UserStats,
} from '@/api/study'
import { PromoCarousel } from '@/components/common/PromoCarousel'
import { ContestStatusBadge } from '@/components/oj/ContestStatusBadge'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useBannerSlides } from '@/hooks/useBannerSlides'
import { useAuthStore } from '@/stores/auth'
import { languageLabel } from '@/utils/monacoLanguage'
import { formatDateTime } from '@/utils/time'

const formatRate = (rate: number) => `${Number(rate || 0).toFixed(1)}%`
const formatTime = (value: string | null) => formatDateTime(value)

const planTones = [
  'bg-[var(--ant-color-primary-bg)] text-[var(--ant-color-primary)]',
  'bg-[var(--ant-color-success-bg)] text-[var(--ant-color-success)]',
  'bg-[var(--ant-color-warning-bg)] text-[var(--ant-color-warning)]',
]
const planIcons = [<BookOutlined key="b" />, <CodeOutlined key="c" />, <TrophyOutlined key="t" />]

function announcementSummary(content: string, contentType: string) {
  const raw = content || ''
  const text =
    contentType === 'html' || contentType === 'markdown'
      ? raw.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
      : raw.replace(/\s+/g, ' ').trim()
  if (text.length <= 80) return text
  return `${text.slice(0, 80)}…`
}

const heatColors = [
  'bg-[var(--ant-color-fill-quaternary)]',
  'bg-[color-mix(in_srgb,var(--ant-color-primary)_22%,transparent)]',
  'bg-[color-mix(in_srgb,var(--ant-color-primary)_40%,transparent)]',
  'bg-[color-mix(in_srgb,var(--ant-color-primary)_65%,transparent)]',
  'bg-[var(--ant-color-primary)]',
]

function heatLevel(count: number) {
  if (count <= 0) return 0
  if (count === 1) return 1
  if (count <= 3) return 2
  if (count <= 6) return 3
  return 4
}

export function HomePage() {
  const token = useAuthStore((s) => s.token)
  const userInfo = useAuthStore((s) => s.userInfo)
  const isLogin = useAuthStore((s) => s.isLogin)

  const [loading, setLoading] = useState(true)
  const [problems, setProblems] = useState<PortalProblemRecommendItem[]>([])
  const [contests, setContests] = useState<PortalContestBrief[]>([])
  const [submissions, setSubmissions] = useState<OjSubmissionListItem[]>([])
  const [studyPlans, setStudyPlans] = useState<LearningPlanItem[]>([])
  const [dailyToday, setDailyToday] = useState<DailyToday | null>(null)
  const [userStats, setUserStats] = useState<UserStats | null>(null)
  const [userHeatmap, setUserHeatmap] = useState<UserHeatmap | null>(null)
  const [announcements, setAnnouncements] = useState<PortalAnnouncement[]>([])
  const [announceLoading, setAnnounceLoading] = useState(true)
  const [activeAnnouncement, setActiveAnnouncement] = useState<PortalAnnouncement | null>(null)
  const { slides: homeBanners } = useBannerSlides({
    position: 'HOME_TOP',
    type: 'CAROUSEL',
  })

  useEffect(() => {
    let mounted = true

    async function loadAnnouncements() {
      setAnnounceLoading(true)
      try {
        const res = await announcementApi.list({ current: 1, size: 5 })
        if (!mounted) return
        setAnnouncements(res.data.records ?? [])
      } catch {
        if (!mounted) return
        setAnnouncements([])
      } finally {
        if (mounted) setAnnounceLoading(false)
      }
    }

    void loadAnnouncements()
    return () => {
      mounted = false
    }
  }, [token])

  async function openAnnouncement(item: PortalAnnouncement) {
    setActiveAnnouncement(item)
    if (!isLogin()) return
    try {
      if (!item.is_read) {
        await announcementApi.read([item.id])
        setAnnouncements((prev) =>
          prev.map((row) => (row.id === item.id ? { ...row, is_read: true } : row)),
        )
        setActiveAnnouncement((curr) => (curr?.id === item.id ? { ...curr, is_read: true } : curr))
      }
    } catch {
      // 标已读失败不影响阅读
    }
  }

  useEffect(() => {
    let mounted = true

    async function load() {
      try {
        const year = new Date().getFullYear()
        const [problemRes, contestRes, submissionRes, planRes, dailyRes, statsRes, heatRes] =
          await Promise.allSettled([
            problemRecommend({ size: 8 }),
            contestPage({ current: 1, size: 4 }),
            submissionPage({ current: 1, size: 6 }),
            learningPlanApi.page({ current: 1, size: 3 }),
            dailyApi.today(),
            isLogin() ? userStatsApi.stats() : Promise.reject(new Error('skip')),
            isLogin() ? userStatsApi.heatmap(year) : Promise.reject(new Error('skip')),
          ])

        if (!mounted) return

        if (problemRes.status === 'fulfilled') {
          setProblems(problemRes.value.data.records ?? [])
        }
        if (contestRes.status === 'fulfilled') {
          setContests(contestRes.value.data.records)
        }
        if (submissionRes.status === 'fulfilled') {
          setSubmissions(submissionRes.value.data.records)
        }
        if (planRes.status === 'fulfilled') {
          setStudyPlans(planRes.value.data?.records ?? [])
        }
        if (dailyRes.status === 'fulfilled') {
          setDailyToday(dailyRes.value.data)
        }
        if (statsRes.status === 'fulfilled') {
          setUserStats(statsRes.value.data)
        }
        if (heatRes.status === 'fulfilled') {
          setUserHeatmap(heatRes.value.data)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void load()
    return () => {
      mounted = false
    }
  }, [token])

  const displayName = userInfo?.nickname || userInfo?.account || '同学'
  const heatmap = useMemo(() => {
    const cells: number[] = Array.from({ length: 91 }, () => 0)
    if (!userHeatmap?.days?.length) return cells
    const end = dayjs().startOf('day')
    const start = end.subtract(90, 'day')
    const map = new Map(userHeatmap.days.map((d) => [d.day_date.slice(0, 10), d.count]))
    for (let i = 0; i < 91; i++) {
      const key = start.add(i, 'day').format('YYYY-MM-DD')
      cells[i] = heatLevel(map.get(key) ?? 0)
    }
    return cells
  }, [userHeatmap])
  const dailyProblem = dailyToday?.problem

  return (
    <div className="page-shell grid w-full gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
      <div className="min-w-0 flex flex-col gap-5">
        <PromoCarousel slides={homeBanners} />

        <section>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-semibold">练习路径</h2>
            <Link to="/plans" className="text-sm text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]">
              全部路径 <RightOutlined />
            </Link>
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            {(studyPlans.length
              ? studyPlans
              : [
                  {
                    id: '',
                    title: '练习路径大厅',
                    subtitle: '按知识点循序练习',
                  } as LearningPlanItem,
                ]
            ).map((plan, i) => (
              <Link
                key={plan.id || `plan-ph-${i}`}
                to={plan.id ? `/plans/detail?id=${plan.id}` : '/plans'}
                className="panel flex items-center gap-3 rounded-xl p-4 transition-all hover:-translate-y-0.5 hover:shadow-[0_6px_16px_rgba(22,119,255,0.1)]"
              >
                <div
                  className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-lg ${planTones[i % planTones.length]}`}
                >
                  {planIcons[i % planIcons.length]}
                </div>
                <div className="min-w-0">
                  <div className="font-medium">{plan.title}</div>
                  <div className="muted-text mt-1 line-clamp-2 text-sm">
                    {plan.subtitle || `${plan.problem_count ?? 0} 题 · 按章节练习`}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </section>

        <section className="panel overflow-hidden">
          <div className="flex items-center gap-2 border-b border-[var(--ant-color-border)] px-4 py-3">
            <NotificationOutlined className="text-[var(--ant-color-text-secondary)]" />
            <h2 className="text-base font-semibold">课程公告</h2>
          </div>
          <Skeleton active loading={announceLoading} paragraph={{ rows: 3 }}>
            {announcements.length ? (
              <div>
                {announcements.map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => void openAnnouncement(item)}
                    className="flex w-full flex-col border-b border-[var(--ant-color-border)] px-4 py-3.5 text-left last:border-b-0 transition-colors hover:bg-[var(--ant-color-fill-quaternary)]"
                  >
                    <div className="flex items-center gap-2">
                      <div className="min-w-0 flex-1 truncate text-[15px] font-medium">{item.title}</div>
                      {item.is_pinned ? (
                        <Tag color="warning" className="m-0 shrink-0">
                          置顶
                        </Tag>
                      ) : null}
                      {isLogin() && !item.is_read ? (
                        <span className="h-2 w-2 shrink-0 rounded-full bg-[var(--ant-color-primary)]" />
                      ) : null}
                    </div>
                    <div className="muted-text mt-1 line-clamp-2 text-sm">
                      {announcementSummary(item.content, item.content_type) || '点击查看详情'}
                    </div>
                    {item.publish_at ? (
                      <div className="muted-text mt-1 text-xs">{formatTime(item.publish_at)}</div>
                    ) : null}
                  </button>
                ))}
              </div>
            ) : !announceLoading ? (
              <div className="py-10">
                <Empty description="暂无公告" />
              </div>
            ) : null}
          </Skeleton>
        </section>

        <section className="panel overflow-hidden">
          <div className="flex items-center justify-between border-b border-[var(--ant-color-border)] px-4 py-3">
            <h2 className="text-base font-semibold">题目推荐</h2>
            <Link to="/problems" className="text-sm text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]">
              题库 <RightOutlined />
            </Link>
          </div>
          <Skeleton active loading={loading} paragraph={{ rows: 6 }}>
            {problems.length ? (
              <div>
                {problems.slice(0, 6).map((problem) => (
                  <Link
                    key={problem.id}
                    to={`/problems/${problem.id}`}
                    className="flex gap-3 border-b border-[var(--ant-color-border)] px-4 py-3 last:border-b-0 transition-colors hover:bg-[var(--ant-color-fill-quaternary)]"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <div className="min-w-0 flex-1 truncate text-[15px] font-medium">
                          {problem.code}. {problem.name}
                        </div>
                        {problem.reason ? (
                          <Tag className="m-0 shrink-0">{problem.reason}</Tag>
                        ) : null}
                      </div>
                      <div className="muted-text mt-1 truncate text-sm">
                        {problem.difficulty} · 通过率 {formatRate(problem.ac_rate)}
                        {problem.type_names?.length
                          ? ` · ${problem.type_names.slice(0, 2).join(' / ')}`
                          : ''}
                      </div>
                    </div>
                    <RightOutlined className="muted-text shrink-0 self-center" />
                  </Link>
                ))}
              </div>
            ) : !loading ? (
              <div className="py-10">
                <Empty description="暂无题目" />
              </div>
            ) : null}
          </Skeleton>
        </section>

        <section className="panel">
          <div className="flex items-center justify-between border-b border-[var(--ant-color-border)] px-4 py-3">
            <h2 className="text-base font-semibold">近期竞赛</h2>
            <Link to="/contests" className="text-sm text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]">
              更多 <RightOutlined />
            </Link>
          </div>
          <Skeleton active loading={loading} paragraph={{ rows: 3 }}>
            {contests.length ? (
              <div>
                {contests.map((contest) => (
                  <Link
                    key={contest.id}
                    to={`/contests/${contest.id}`}
                    className="flex items-center gap-3 border-b border-[var(--ant-color-border)] px-4 py-3 last:border-b-0 hover:bg-[var(--ant-color-fill-quaternary)]"
                  >
                    <TrophyOutlined className="text-[var(--ant-color-warning)]" />
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-sm font-medium">{contest.name}</div>
                      <div className="muted-text mt-0.5 text-xs">{formatTime(contest.start_time)}</div>
                    </div>
                    <ContestStatusBadge status={contest.lifecycle_status} />
                  </Link>
                ))}
              </div>
            ) : (
              <div className="py-8">
                <Empty description="暂无竞赛" />
              </div>
            )}
          </Skeleton>
        </section>
      </div>

      <aside className="flex flex-col gap-4">
        <div className="panel sticky top-[80px] p-4">
          <div className="mb-3">
            <div className="text-sm font-medium">
              {token ? `${displayName}，开始今天的练习` : '欢迎使用校园 OJ'}
            </div>
            <div className="muted-text mt-1 text-xs">完成今日练习，巩固课堂知识点</div>
          </div>

          <div className="rounded-lg bg-[var(--ant-color-fill-quaternary)] p-3">
            <div className="mb-1 flex items-center gap-1 text-xs text-[var(--ant-color-text-secondary)]">
              今日练习
              {dailyToday?.checked_in ? (
                <span className="ml-auto text-[var(--ant-color-diff-easy)]">已完成</span>
              ) : (
                <span className="ml-auto text-[var(--ant-color-warning)]">未完成</span>
              )}
            </div>
            {dailyProblem ? (
              <Link to={`/problems/${dailyProblem.problem_id}`} className="block">
                <div className="text-sm font-medium">
                  {dailyProblem.problem_code}. {dailyProblem.problem_name}
                </div>
                <div className="muted-text mt-1 text-xs">
                  通过率 {formatRate(dailyProblem.ac_rate)}
                  {dailyToday?.streak ? ` · 连续练习 ${dailyToday.streak} 天` : ''}
                </div>
              </Link>
            ) : (
              <Link to="/problems" className="block text-sm font-medium">
                今日暂未布置，去题库选一题
              </Link>
            )}
          </div>

          <div className="mt-3 grid grid-cols-3 gap-2 text-center">
            <div>
              <div className="text-lg font-semibold tabular-nums">{userStats?.solved_total ?? 0}</div>
              <div className="muted-text text-[11px]">已通过</div>
            </div>
            <div>
              <div className="text-lg font-semibold tabular-nums">{dailyToday?.month_done ?? 0}</div>
              <div className="muted-text text-[11px]">本月练习</div>
            </div>
            <div>
              <div className="text-lg font-semibold tabular-nums">
                {dailyToday?.streak ?? userStats?.streak ?? 0}
              </div>
              <div className="muted-text text-[11px]">连续天数</div>
            </div>
          </div>

          <div className="mt-4">
            <div className="mb-2 text-xs font-medium">提交活跃度</div>
            <div className="grid grid-flow-col grid-rows-7 gap-[3px]">
              {heatmap.map((level, i) => (
                <div
                  key={i}
                  className={`h-2.5 w-2.5 rounded-[2px] ${heatColors[level] ?? heatColors[0]}`}
                  title={`活跃等级 ${level}`}
                />
              ))}
            </div>
            <div className="muted-text mt-2 flex justify-between text-[11px]">
              <span>较少</span>
              <span>较多</span>
            </div>
          </div>

          <Link
            to="/profile"
            className="mt-4 flex items-center justify-center rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2.5 text-sm font-medium transition-colors hover:bg-[var(--ant-color-fill-secondary)]"
          >
            我的练习统计
          </Link>
        </div>

        <div className="panel p-4">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold">最近提交</h3>
            <Link to="/submissions" className="text-xs text-[var(--ant-color-text-secondary)]">
              全部 <RightOutlined />
            </Link>
          </div>
          <Skeleton active loading={loading} paragraph={{ rows: 4 }}>
            {submissions.length ? (
              <div className="space-y-2.5">
                {submissions.slice(0, 5).map((submission) => (
                  <Link key={submission.id} to={`/submissions/${submission.id}`} className="flex items-center gap-2">
                    <VerdictBadge status={submission.status} result={submission.result} />
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-xs">
                        {submission.problem_code || '-'} {submission.problem_name || ''}
                      </div>
                      <div className="muted-text truncate text-[11px]">
                        {languageLabel(submission.language_key)}
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <Empty description="暂无提交" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Skeleton>
        </div>

        <div className="panel p-4">
          <div className="mb-3 flex items-center gap-2 text-sm font-semibold">
            <DatabaseOutlined />
            快捷入口
          </div>
          <div className="grid grid-cols-2 gap-2">
            <Link
              to="/problems"
              className="rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2 text-center text-sm hover:bg-[var(--ant-color-fill-secondary)]"
            >
              <CodeOutlined className="mr-1" />
              题库
            </Link>
            <Link
              to="/contests"
              className="rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2 text-center text-sm hover:bg-[var(--ant-color-fill-secondary)]"
            >
              <TrophyOutlined className="mr-1" />
              竞赛
            </Link>
            <Link
              to="/submissions"
              className="rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2 text-center text-sm hover:bg-[var(--ant-color-fill-secondary)]"
            >
              <HistoryOutlined className="mr-1" />
              提交
            </Link>
            <Link
              to="/plans"
              className="rounded-lg bg-[var(--ant-color-fill-quaternary)] px-3 py-2 text-center text-sm hover:bg-[var(--ant-color-fill-secondary)]"
            >
              <BookOutlined className="mr-1" />
              路径
            </Link>
          </div>
        </div>
      </aside>

      <Modal
        open={Boolean(activeAnnouncement)}
        title={activeAnnouncement?.title}
        footer={null}
        onCancel={() => setActiveAnnouncement(null)}
        width={640}
        destroyOnHidden
      >
        {activeAnnouncement ? (
          <div className="space-y-3">
            <div className="muted-text flex flex-wrap items-center gap-2 text-xs">
              {activeAnnouncement.is_pinned ? <Tag color="warning">置顶</Tag> : null}
              {activeAnnouncement.publish_at ? (
                <span>发布于 {formatTime(activeAnnouncement.publish_at)}</span>
              ) : null}
            </div>
            {activeAnnouncement.content_type === 'html' ? (
              <div
                className="prose max-w-none text-sm leading-6"
                dangerouslySetInnerHTML={{ __html: activeAnnouncement.content }}
              />
            ) : (
              <div className="whitespace-pre-wrap text-sm leading-6 text-[var(--ant-color-text)]">
                {activeAnnouncement.content}
              </div>
            )}
          </div>
        ) : null}
      </Modal>
    </div>
  )
}
