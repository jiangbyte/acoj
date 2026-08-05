import { useEffect, useMemo, useState } from 'react'
import { Avatar, Empty, Progress, Spin, Tabs, Tag } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { Link, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import { resolveFileUrl } from '@/utils/file'
import { formatDateTime } from '@/utils/time'
import { useDict } from '@/hooks/useDict'
import { dictTypeData } from '@/utils/dict'
import { authApi, problemListApi, userStatsApi } from '@/api'

function DiffBar({ label, solved, total, color }: { label: string; solved: number; total: number; color: string }) {
  const pct = total ? Math.round((solved / total) * 100) : 0
  return (
    <div className="mb-2">
      <div className="mb-1 flex justify-between text-sm">
        <span style={{ color }}>{label}</span>
        <span className="tabular-nums text-[var(--ant-color-text-secondary)]">
          {solved}/{total}
        </span>
      </div>
      <Progress percent={pct} showInfo={false} strokeColor={color} size="small" />
    </div>
  )
}

export function ProfilePage() {
  useDict()
  const [params] = useSearchParams()
  const userInfo = useAuthStore((s) => s.userInfo)
  const isLogin = useAuthStore((s) => s.isLogin)
  const accountId = params.get('account_id') || userInfo?.accountId || ''
  const isSelf = Boolean(userInfo?.accountId && accountId === userInfo.accountId)

  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState<any>(null)
  const [stats, setStats] = useState<any>(null)
  const [heatmap, setHeatmap] = useState<any>(null)
  const [recent, setRecent] = useState<any[]>([])
  const [lists, setLists] = useState<any[]>([])

  const year = new Date().getFullYear()

  useEffect(() => {
    if (!accountId) {
      setLoading(false)
      setProfile(null)
      return
    }
    void (async () => {
      setLoading(true)
      try {
        const [spaceRes, s, h, r] = await Promise.all([
          authApi.getPublicSpace(accountId),
          userStatsApi.stats(accountId),
          userStatsApi.heatmap(year, accountId),
          userStatsApi.recentSolved({ current: 1, size: 20, account_id: accountId }),
        ])
        setProfile(spaceRes.data)
        setStats(s.data)
        setHeatmap(h.data)
        setRecent(r.data?.records ?? [])
        if (isSelf && isLogin()) {
          const mine = await problemListApi.mine()
          setLists(mine.data ?? [])
        } else {
          setLists([])
        }
      } catch {
        setProfile(null)
        setStats(null)
        setHeatmap(null)
        setRecent([])
        setLists([])
      } finally {
        setLoading(false)
      }
    })()
  }, [accountId, isSelf, year])

  const easy = stats?.by_difficulty.find((d: any) => d.difficulty === 'Easy')
  const medium = stats?.by_difficulty.find((d: any) => d.difficulty === 'Medium')
  const hard = stats?.by_difficulty.find((d: any) => d.difficulty === 'Hard')
  const solvedPct = stats?.problem_total
    ? Math.round((stats.solved_total / stats.problem_total) * 100)
    : 0

  const heatMap = useMemo(() => {
    const map = new Map<string, number>(
      (heatmap?.days ?? []).map((d: any) => [d.day_date.slice(0, 10), Number(d.count) || 0]),
    )
    return map
  }, [heatmap])

  const heatCells = useMemo(() => {
    const cells: { date: string; count: number }[] = []
    const start = new Date(year, 0, 1)
    const end = new Date(year, 11, 31)
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      cells.push({ date: key, count: heatMap.get(key) ?? 0 })
    }
    return cells
  }, [heatMap, year])

  const displayName =
    profile?.nickname ||
    profile?.name ||
    (isSelf ? userInfo?.nickname || userInfo?.name || userInfo?.account : null) ||
    '未命名选手'
  const avatarSrc = resolveFileUrl(profile?.avatar || (isSelf ? userInfo?.avatar : null))
  const signature = profile?.signature?.trim() || ''

  if (!accountId) {
    return (
      <div className="page-shell py-20">
        <Empty description="请先登录查看个人主页" />
      </div>
    )
  }

  return (
    <div className="page-shell">
      <Spin spinning={loading}>
        <div className="grid gap-4 lg:grid-cols-[280px_minmax(0,1fr)]">
          <aside className="panel rounded-xl p-5">
            <div className="flex flex-col items-center text-center">
              <Avatar size={88} src={avatarSrc || undefined} icon={<UserOutlined />} />
              <div className="mt-3 text-lg font-semibold">{displayName}</div>
              {signature ? <div className="muted-text mt-1 text-sm">{signature}</div> : null}
              {isSelf ? (
                <Link to="/usercenter" className="mt-4 text-sm text-[var(--ant-color-primary)]">
                  编辑个人资料 / 账号设置
                </Link>
              ) : null}
            </div>
            <div className="mt-6 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="muted-text">连续练习</span>
                <span className="font-medium">{stats?.streak ?? 0} 天</span>
              </div>
              <div className="flex justify-between">
                <span className="muted-text">总提交</span>
                <span className="font-medium">{stats?.submission_total ?? 0}</span>
              </div>
              <div className="flex justify-between">
                <span className="muted-text">通过率</span>
                <span className="font-medium">{Number(stats?.ac_rate ?? 0).toFixed(1)}%</span>
              </div>
            </div>
          </aside>

          <div className="min-w-0 space-y-4">
            <div className="grid gap-4 md:grid-cols-[1fr_220px]">
              <div className="panel flex flex-wrap items-center gap-6 rounded-xl p-5">
                <Progress
                  type="circle"
                  percent={solvedPct}
                  size={120}
                  format={() => (
                    <div className="text-center">
                      <div className="text-xl font-semibold">{stats?.solved_total ?? 0}</div>
                      <div className="text-xs text-[var(--ant-color-text-secondary)]">
                        /{stats?.problem_total ?? 0}
                      </div>
                    </div>
                  )}
                />
                <div className="min-w-[180px] flex-1">
                  <DiffBar
                    label="简单"
                    solved={easy?.solved ?? 0}
                    total={easy?.total ?? 0}
                    color="var(--ant-color-diff-easy)"
                  />
                  <DiffBar
                    label="中等"
                    solved={medium?.solved ?? 0}
                    total={medium?.total ?? 0}
                    color="var(--ant-color-diff-medium)"
                  />
                  <DiffBar
                    label="困难"
                    solved={hard?.solved ?? 0}
                    total={hard?.total ?? 0}
                    color="var(--ant-color-diff-hard)"
                  />
                </div>
              </div>
              <div className="panel rounded-xl p-5">
                <div className="text-sm text-[var(--ant-color-text-secondary)]">已通过题目</div>
                <div className="mt-1 text-3xl font-semibold text-[var(--ant-color-primary)]">
                  {stats?.solved_total ?? 0} 题
                </div>
                <div className="muted-text mt-3 text-xs">连续练习 {stats?.streak ?? 0} 天</div>
              </div>
            </div>

            <div className="panel rounded-xl p-5">
              <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                <div className="font-medium">{year} 年提交活跃度</div>
                <div className="muted-text text-xs">
                  共 {heatmap?.total_submissions ?? 0} 次 · 活跃 {heatmap?.active_days ?? 0} 天
                </div>
              </div>
              <div className="flex flex-wrap gap-[2px]">
                {heatCells.map((c) => {
                  const level =
                    c.count === 0 ? 0 : c.count < 2 ? 1 : c.count < 4 ? 2 : c.count < 8 ? 3 : 4
                  const bg =
                    [
                      'var(--ant-color-fill-quaternary)',
                      'color-mix(in srgb, var(--ant-color-primary) 25%, transparent)',
                      'color-mix(in srgb, var(--ant-color-primary) 45%, transparent)',
                      'color-mix(in srgb, var(--ant-color-primary) 70%, transparent)',
                      'var(--ant-color-primary)',
                    ][level]
                  return (
                    <div
                      key={c.date}
                      title={`${c.date}: ${c.count}`}
                      className="h-[10px] w-[10px] rounded-[2px]"
                      style={{ background: bg }}
                    />
                  )
                })}
              </div>
            </div>

            <div className="panel rounded-xl p-4">
              <Tabs
                items={[
                  {
                    key: 'recent',
                    label: '最近通过',
                    children: recent.length ? (
                      <div className="divide-y divide-[var(--ant-color-border)]">
                        {recent.map((item) => (
                          <Link
                            key={`${item.problem_id}-${item.solved_at}`}
                            to={`/problems/${item.problem_id}`}
                            className="flex items-center justify-between gap-3 py-3 hover:bg-[var(--ant-color-fill-secondary)]"
                          >
                            <div className="min-w-0">
                              <div className="truncate font-medium">
                                {item.problem_code}. {item.problem_name}
                              </div>
                              <Tag className="mt-1">
                                {dictTypeData('PROBLEM_DIFFICULTY', item.difficulty) || item.difficulty}
                              </Tag>
                            </div>
                            <span className="muted-text shrink-0 text-xs">
                              {formatDateTime(item.solved_at)}
                            </span>
                          </Link>
                        ))}
                      </div>
                    ) : (
                      <Empty description="暂无通过记录" />
                    ),
                  },
                  {
                    key: 'lists',
                    label: '题单',
                    children: isSelf ? (
                      lists.length ? (
                        <div className="grid gap-2 sm:grid-cols-2">
                          {lists.map((l) => (
                            <Link
                              key={l.id}
                              to={`/lists/detail?id=${l.id}`}
                              className="rounded-lg px-3 py-3 ring-1 ring-[var(--ant-color-border)] hover:bg-[var(--ant-color-fill-secondary)]"
                            >
                              <div className="font-medium">{l.title}</div>
                              <div className="muted-text text-xs">{l.problem_count} 题</div>
                            </Link>
                          ))}
                        </div>
                      ) : (
                        <Empty description="暂无题单" />
                      )
                    ) : (
                      <Empty description="仅本人可见题单" />
                    ),
                  },
                ]}
              />
            </div>
          </div>
        </div>
      </Spin>
    </div>
  )
}
