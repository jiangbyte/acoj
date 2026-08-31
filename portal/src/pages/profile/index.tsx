/** Author: Charlie */

import { useEffect, useMemo, useState } from 'react'
import { Avatar, Empty, Flex, Progress, Spin, Tag, Typography } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi, ojUserApi, type OjUserHomepage } from '@/api'
import { useAuthStore } from '@/stores/auth'
import {
  difficultyColor,
  difficultyLabel,
  formatRelativeTime,
  ojLanguageLabel,
} from '@/utils'

type HeatmapCell = {
  date: string
  count: number
  level: 0 | 1 | 2 | 3 | 4
}

function heatmapLevel(count: number): HeatmapCell['level'] {
  if (count <= 0) return 0
  if (count === 1) return 1
  if (count <= 3) return 2
  if (count <= 6) return 3
  return 4
}

function buildHeatmapWeeks(days: { date: string; count?: number }[] | undefined): (HeatmapCell | null)[][] {
  const map = new Map<string, number>()
  for (const day of days || []) {
    if (!day?.date) continue
    map.set(day.date, Number(day.count || 0))
  }
  const today = new Date()
  const chinaToday = new Date(today.getTime() + 8 * 60 * 60 * 1000)
  const end = Date.UTC(
    chinaToday.getUTCFullYear(),
    chinaToday.getUTCMonth(),
    chinaToday.getUTCDate(),
  )
  const cells: HeatmapCell[] = []
  for (let i = 364; i >= 0; i -= 1) {
    const ts = end - i * 86400000
    const d = new Date(ts)
    const date = [
      d.getUTCFullYear(),
      String(d.getUTCMonth() + 1).padStart(2, '0'),
      String(d.getUTCDate()).padStart(2, '0'),
    ].join('-')
    const count = map.get(date) || 0
    cells.push({ date, count, level: heatmapLevel(count) })
  }

  const firstWeekday = new Date(end - 364 * 86400000).getUTCDay()
  const padded: (HeatmapCell | null)[] = [
    ...Array.from({ length: firstWeekday }, () => null),
    ...cells,
  ]
  while (padded.length % 7 !== 0) {
    padded.push(null)
  }
  const weeks: (HeatmapCell | null)[][] = []
  for (let i = 0; i < padded.length; i += 7) {
    weeks.push(padded.slice(i, i + 7))
  }
  return weeks
}

const heatmapCellClass = (level: HeatmapCell['level'] | undefined) => {
  if (level == null) return 'rounded-[2px] bg-transparent'
  if (level === 0) {
    return 'rounded-[2px] bg-[color-mix(in_srgb,var(--ant-color-border)_40%,transparent)]'
  }
  if (level === 1) {
    return 'rounded-[2px] bg-[color-mix(in_srgb,var(--ant-color-primary)_28%,transparent)]'
  }
  if (level === 2) {
    return 'rounded-[2px] bg-[color-mix(in_srgb,var(--ant-color-primary)_48%,transparent)]'
  }
  if (level === 3) {
    return 'rounded-[2px] bg-[color-mix(in_srgb,var(--ant-color-primary)_68%,transparent)]'
  }
  return 'rounded-[2px] bg-[var(--ant-color-primary)]'
}

function SolveRing({
  accepted,
  total,
  attempting,
}: {
  accepted: number
  total: number
  attempting: number
}) {
  const percent = total > 0 ? Math.min(100, Math.round((accepted / total) * 100)) : 0
  return (
    <div className="relative flex h-36 w-36 items-center justify-center">
      <Progress
        type="circle"
        percent={percent}
        size={144}
        strokeWidth={8}
        showInfo={false}
        strokeColor="var(--ant-color-primary)"
        trailColor="color-mix(in srgb, var(--ant-color-border) 55%, transparent)"
      />
      <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
        <div className="text-2xl font-semibold leading-none">
          {accepted}
          <span className="text-sm font-normal text-[var(--ant-color-text-secondary)]">
            /{total}
          </span>
        </div>
        <div className="mt-1 text-xs text-[var(--ant-color-text-secondary)]">已解答</div>
        <div className="mt-1 text-xs text-[var(--ant-color-text-secondary)]">
          尝试中 {attempting}
        </div>
      </div>
    </div>
  )
}

function DifficultyRow({
  label,
  color,
  solved,
  total,
}: {
  label: string
  color: string
  solved: number
  total: number
}) {
  const percent = total > 0 ? Math.min(100, (solved / total) * 100) : 0
  return (
    <div className="min-w-0 flex-1">
      <div className="mb-1 flex items-baseline justify-between gap-2 text-sm">
        <span style={{ color }}>{label}</span>
        <span className="text-[var(--ant-color-text-secondary)]">
          {solved}/{total}
        </span>
      </div>
      <div className="h-1.5 overflow-hidden rounded-full bg-[color-mix(in_srgb,var(--ant-color-border)_45%,transparent)]">
        <div className="h-full rounded-full" style={{ width: `${percent}%`, background: color }} />
      </div>
    </div>
  )
}

export function ProfilePage() {
  const [params] = useSearchParams()
  const userInfo = useAuthStore((s) => s.userInfo)
  const accountId = params.get('account_id') || userInfo?.accountId || ''
  const isSelf = Boolean(userInfo?.accountId && accountId === userInfo.accountId)

  const [loading, setLoading] = useState(Boolean(accountId))
  const [profile, setProfile] = useState<any>(null)
  const [homepage, setHomepage] = useState<OjUserHomepage | null>(null)
  const [activeAccountId, setActiveAccountId] = useState(accountId)

  if (accountId !== activeAccountId) {
    setActiveAccountId(accountId)
    setLoading(Boolean(accountId))
    setProfile(null)
    setHomepage(null)
  }

  useEffect(() => {
    if (!accountId) return
    let cancelled = false
    void (async () => {
      try {
        const spaceRes = await authApi.getPublicSpace(accountId)
        if (cancelled) return
        setProfile(spaceRes.data)
      } catch {
        if (!cancelled) setProfile(null)
      }

      try {
        const statsRes = await ojUserApi.homepage(accountId)
        if (cancelled) return
        setHomepage(statsRes.data || null)
      } catch {
        if (!cancelled) setHomepage(null)
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [accountId])

  const nickname = String(
    profile?.nickname ?? (isSelf ? userInfo?.nickname : null) ?? '',
  ).trim()
  const displayName = nickname || '未命名用户'
  const accountLabel = String(
    profile?.account ?? (isSelf ? userInfo?.account : null) ?? '',
  ).trim()
  const avatarSrc = profile?.avatar || (isSelf ? userInfo?.avatar : undefined) || undefined
  const signature = String(profile?.signature || '').trim()

  const solved = homepage?.solved
  const easy = solved?.easy
  const medium = solved?.medium
  const hard = solved?.hard
  const publishedTotal =
    Number(easy?.total || 0) + Number(medium?.total || 0) + Number(hard?.total || 0)
  const accepted = Number(solved?.accepted || 0)
  const attempting = Number(solved?.attempting || 0)
  const languages = homepage?.languages || []
  const heatmap = homepage?.heatmap
  const recentAccepted = homepage?.recent_accepted || []
  const heatmapWeeks = useMemo(() => buildHeatmapWeeks(heatmap?.days), [heatmap?.days])

  if (!accountId) {
    return (
      <div className="flex min-h-[360px] items-center justify-center px-4 py-12">
        <Empty description="请先登录或指定用户查看用户主页" />
      </div>
    )
  }

  return (
    <div className="min-h-[calc(100vh-64px-72px)] w-full bg-[var(--ant-color-bg-layout)] text-[var(--ant-color-text)]">
      <Spin spinning={loading}>
        {profile || isSelf ? (
          <div className="flex w-full flex-col gap-4 px-4 py-5 md:flex-row md:items-start md:gap-5 md:px-6 md:py-6">
            <aside className="w-full shrink-0 space-y-4 md:w-72 lg:w-80">
              <section className="border border-[color-mix(in_srgb,var(--ant-color-border)_65%,transparent)] bg-[var(--ant-color-bg-container)] px-5 py-5">
                <Flex align="flex-start" gap={14}>
                  <Avatar
                    size={72}
                    src={avatarSrc || undefined}
                    icon={<UserOutlined />}
                    className="shrink-0"
                  />
                  <div className="min-w-0 pt-1">
                    <h1 className="m-0 truncate text-xl font-semibold leading-tight">
                      {displayName}
                    </h1>
                    {accountLabel ? (
                      <p className="mt-1 truncate text-sm text-[var(--ant-color-text-secondary)]">
                        @{accountLabel}
                      </p>
                    ) : null}
                  </div>
                </Flex>

                {signature ? (
                  <p className="mt-4 whitespace-pre-wrap text-sm leading-relaxed text-[var(--ant-color-text-secondary)]">
                    {signature}
                  </p>
                ) : (
                  <p className="mt-4 text-sm text-[var(--ant-color-text-secondary)]">暂未填写签名</p>
                )}

                {isSelf ? (
                  <Link
                    to="/usercenter"
                    className="mt-4 inline-flex w-full items-center justify-center border border-[var(--ant-color-border)] px-3 py-2 text-sm transition-colors hover:border-[var(--ant-color-primary)] hover:text-[var(--ant-color-primary)]"
                  >
                    编辑资料
                  </Link>
                ) : null}
              </section>

              <section className="border border-[color-mix(in_srgb,var(--ant-color-border)_65%,transparent)] bg-[var(--ant-color-bg-container)] px-5 py-4">
                <h2 className="mb-3 mt-0 text-sm font-semibold">语言</h2>
                {languages.length ? (
                  <ul className="m-0 list-none space-y-2.5 p-0">
                    {languages.map((item) => (
                      <li
                        key={item.language}
                        className="flex items-center justify-between gap-3 text-sm"
                      >
                        <span>{ojLanguageLabel(item.language)}</span>
                        <span className="text-[var(--ant-color-text-secondary)]">
                          {Number(item.solved_count || 0)} 题
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="m-0 text-sm text-[var(--ant-color-text-secondary)]">暂无通过记录</p>
                )}
              </section>
            </aside>

            <div className="min-w-0 flex-1 space-y-4">
              <section className="border border-[color-mix(in_srgb,var(--ant-color-border)_65%,transparent)] bg-[var(--ant-color-bg-container)] px-5 py-5">
                <Flex align="center" gap={28} wrap="wrap">
                  <SolveRing
                    accepted={accepted}
                    total={publishedTotal}
                    attempting={attempting}
                  />
                  <div className="flex min-w-[220px] flex-1 flex-col gap-4">
                    <DifficultyRow
                      label="简单"
                      color="var(--ant-color-success)"
                      solved={Number(easy?.solved || 0)}
                      total={Number(easy?.total || 0)}
                    />
                    <DifficultyRow
                      label="中等"
                      color="var(--ant-color-warning)"
                      solved={Number(medium?.solved || 0)}
                      total={Number(medium?.total || 0)}
                    />
                    <DifficultyRow
                      label="困难"
                      color="var(--ant-color-error)"
                      solved={Number(hard?.solved || 0)}
                      total={Number(hard?.total || 0)}
                    />
                  </div>
                </Flex>
              </section>

              <section className="border border-[color-mix(in_srgb,var(--ant-color-border)_65%,transparent)] bg-[var(--ant-color-bg-container)] px-5 py-4">
                <div className="mb-3 flex flex-wrap items-baseline gap-x-5 gap-y-1">
                  <Typography.Text>
                    过去一年共提交{' '}
                    <span className="font-semibold">{Number(heatmap?.total_submissions || 0)}</span>{' '}
                    次
                  </Typography.Text>
                  <span className="text-sm text-[var(--ant-color-text-secondary)]">
                    累计提交天数{' '}
                    <span className="text-[var(--ant-color-text)]">
                      {Number(heatmap?.active_days || 0)}
                    </span>
                  </span>
                  <span className="text-sm text-[var(--ant-color-text-secondary)]">
                    连续提交天数{' '}
                    <span className="text-[var(--ant-color-text)]">
                      {Number(heatmap?.current_streak || 0)}
                    </span>
                  </span>
                  <span className="text-sm text-[var(--ant-color-text-secondary)]">
                    最长连续{' '}
                    <span className="text-[var(--ant-color-text)]">
                      {Number(heatmap?.max_streak || 0)}
                    </span>
                  </span>
                </div>

                <div className="flex w-full gap-[3px]">
                  {heatmapWeeks.map((week, weekIndex) => (
                    <div key={weekIndex} className="flex min-w-0 flex-1 flex-col gap-[3px]">
                      {week.map((cell, dayIndex) => (
                        <div
                          key={cell?.date ?? `pad-${weekIndex}-${dayIndex}`}
                          title={cell ? `${cell.date}: ${cell.count} 次提交` : undefined}
                          className={`aspect-square w-full ${heatmapCellClass(cell?.level)}`}
                        />
                      ))}
                    </div>
                  ))}
                </div>
              </section>

              <section className="border border-[color-mix(in_srgb,var(--ant-color-border)_65%,transparent)] bg-[var(--ant-color-bg-container)] px-5 py-4">
                <h2 className="mb-3 mt-0 text-base font-semibold">最近通过</h2>
                {recentAccepted.length ? (
                  <ul className="m-0 list-none divide-y divide-[color-mix(in_srgb,var(--ant-color-border)_55%,transparent)] p-0">
                    {recentAccepted.map((item) => (
                      <li key={item.problem_id} className="flex items-center justify-between gap-3 py-3">
                        <div className="min-w-0">
                          <Link
                            to={`/problems/${item.problem_id}`}
                            className="truncate text-[var(--ant-color-text)] hover:text-[var(--ant-color-primary)]"
                          >
                            {item.problem_key ? `${item.problem_key}. ` : ''}
                            {item.title || '未命名题目'}
                          </Link>
                          <div className="mt-1">
                            <Tag color={difficultyColor(item.difficulty)} className="m-0">
                              {difficultyLabel(item.difficulty)}
                            </Tag>
                          </div>
                        </div>
                        <span className="shrink-0 text-sm text-[var(--ant-color-text-secondary)]">
                          {formatRelativeTime(item.accepted_at)}
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无通过记录" />
                )}
              </section>
            </div>
          </div>
        ) : (
          <div className="flex min-h-[360px] items-center justify-center px-4 py-12">
            <Empty description="用户不存在或资料未公开" />
          </div>
        )}
      </Spin>
    </div>
  )
}
