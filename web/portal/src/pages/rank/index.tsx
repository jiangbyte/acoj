import { useEffect, useMemo, useState } from 'react'
import { Avatar, Empty, Pagination, Skeleton } from 'antd'
import {
  FireOutlined,
  RiseOutlined,
  TrophyOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { Link } from 'react-router-dom'
import {
  rankApi,
  type RankBoard,
  type RankMe,
  type RankSummary,
  type RatingRankItem,
  type SolvedRankItem,
} from '@/api/rank'
import { useAuthStore } from '@/stores/auth'
import { resolveFileUrl } from '@/utils/file'

type RankRow = {
  rank: number
  account_id: string
  nickname: string | null
  avatar: string | null
  score: number
  contests?: number
  delta?: number
}

const tabs: { key: RankBoard; label: string }[] = [
  { key: 'solved', label: '练习榜' },
  { key: 'rating', label: '竞赛 Rating' },
]

function formatScore(score: number) {
  return score.toLocaleString('zh-CN')
}

function displayName(nickname: string | null | undefined, accountId?: string) {
  return nickname?.trim() || accountId || '未命名选手'
}

function RankBadge({ rank }: { rank: number }) {
  if (rank === 1) {
    return (
      <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-[var(--ant-color-rank-gold)] text-sm font-semibold text-white shadow-sm">
        1
      </span>
    )
  }
  if (rank === 2) {
    return (
      <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-[var(--ant-color-rank-silver)] text-sm font-semibold text-white shadow-sm">
        2
      </span>
    )
  }
  if (rank === 3) {
    return (
      <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-[var(--ant-color-rank-bronze)] text-sm font-semibold text-white shadow-sm">
        3
      </span>
    )
  }
  return (
    <span className="inline-flex h-8 w-8 items-center justify-center text-sm font-medium text-[var(--ant-color-text-secondary)]">
      {rank}
    </span>
  )
}

function DeltaText({ delta = 0 }: { delta?: number }) {
  if (!delta) return <span className="muted-text text-xs">-</span>
  if (delta > 0) {
    return <span className="text-xs text-[var(--ant-color-success)]">↑{delta}</span>
  }
  return <span className="text-xs text-[var(--ant-color-error)]">↓{Math.abs(delta)}</span>
}

function toRows(board: RankBoard, records: SolvedRankItem[] | RatingRankItem[]): RankRow[] {
  if (board === 'solved') {
    return (records as SolvedRankItem[]).map((r) => ({
      rank: r.rank,
      account_id: r.account_id,
      nickname: r.nickname,
      avatar: r.avatar,
      score: r.solved,
    }))
  }
  return (records as RatingRankItem[]).map((r) => ({
    rank: r.rank,
    account_id: r.account_id,
    nickname: r.nickname,
    avatar: r.avatar,
    score: r.rating,
    contests: r.contests,
    delta: r.delta,
  }))
}

export function RankPage() {
  const isLogin = useAuthStore((s) => s.isLogin)
  const userInfo = useAuthStore((s) => s.userInfo)
  const loggedIn = isLogin()
  const [board, setBoard] = useState<RankBoard>('solved')
  const [current, setCurrent] = useState(1)
  const [size, setSize] = useState(20)
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState<RankRow[]>([])
  const [total, setTotal] = useState(0)
  const [summary, setSummary] = useState<RankSummary | null>(null)
  const [me, setMe] = useState<RankMe | null>(null)

  useEffect(() => {
    setCurrent(1)
  }, [board])

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      try {
        const listReq =
          board === 'solved'
            ? rankApi.solved({ current, size })
            : rankApi.rating({ current, size })
        const meReq = loggedIn ? rankApi.me(board) : Promise.resolve(null)
        const [listRes, summaryRes, meRes] = await Promise.all([
          listReq,
          rankApi.summary(board),
          meReq,
        ])
        if (!mounted) return
        setRows(toRows(board, listRes.data.records || []))
        setTotal(listRes.data.total || 0)
        setSummary(summaryRes.data)
        setMe(meRes?.data ?? null)
      } catch {
        if (!mounted) return
        setRows([])
        setTotal(0)
        setSummary(null)
        setMe(null)
      } finally {
        if (mounted) setLoading(false)
      }
    }

    void load()
    return () => {
      mounted = false
    }
  }, [board, current, size, loggedIn])

  const podium = useMemo(() => {
    if (current !== 1) return []
    return rows.filter((r) => r.rank <= 3).sort((a, b) => a.rank - b.rank)
  }, [rows, current])

  const list = useMemo(() => {
    if (current === 1) return rows.filter((r) => r.rank > 3)
    return rows
  }, [rows, current])

  const rising = useMemo(() => {
    if (board !== 'rating') return []
    return [...rows]
      .filter((r) => (r.delta ?? 0) > 0)
      .sort((a, b) => (b.delta ?? 0) - (a.delta ?? 0))
      .slice(0, 6)
  }, [board, rows])

  const scoreLabel = board === 'solved' ? '通关题数' : 'Rating'
  const subtitle =
    board === 'solved'
      ? '按正式提交通过的去重题数排名（含竞赛 AC，不含试提交）'
      : '按竞赛 Rating 结算后的当前积分排名'

  return (
    <div className="page-shell flex w-full flex-col gap-4">
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {[
          {
            label: '上榜选手',
            value: formatScore(summary?.total_users ?? 0),
            icon: <UserOutlined />,
            tone: 'text-[var(--ant-color-primary)] bg-[var(--ant-color-primary-bg)]',
          },
          {
            label: board === 'solved' ? '榜首通关' : '榜首 Rating',
            value: formatScore(summary?.top_score ?? 0),
            icon: <TrophyOutlined />,
            tone: 'text-[var(--ant-color-warning)] bg-[var(--ant-color-warning-bg)]',
          },
          {
            label: board === 'solved' ? '平均通关' : '平均 Rating',
            value: formatScore(summary?.avg_score ?? 0),
            icon: <FireOutlined />,
            tone: 'text-[var(--ant-color-error)] bg-[var(--ant-color-error-bg)]',
          },
          {
            label: board === 'rating' ? '最大涨幅' : '我的名次',
            value:
              board === 'rating'
                ? `+${summary?.max_delta ?? 0}`
                : me?.rank
                  ? `#${me.rank}`
                  : loggedIn
                    ? '未上榜'
                    : '-',
            icon: <RiseOutlined />,
            tone: 'text-[var(--ant-color-success)] bg-[var(--ant-color-success-bg)]',
          },
        ].map((card) => (
          <div key={card.label} className="panel flex items-center gap-3 rounded-xl px-4 py-3">
            <div className={`flex h-10 w-10 items-center justify-center rounded-xl text-lg ${card.tone}`}>
              {card.icon}
            </div>
            <div>
              <div className="text-lg font-semibold tabular-nums">{card.value}</div>
              <div className="muted-text text-xs">{card.label}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_300px]">
        <div className="min-w-0 flex flex-col gap-4">
          <div className="panel rounded-xl px-4 py-4">
            <div className="flex flex-wrap items-end justify-between gap-3">
              <div>
                <h1 className="text-2xl font-semibold">排名</h1>
                <p className="muted-text mt-1 text-sm">{subtitle}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                {tabs.map((tab) => (
                  <button
                    key={tab.key}
                    type="button"
                    onClick={() => setBoard(tab.key)}
                    className={`chip ${
                      board === tab.key
                        ? 'bg-[var(--ant-color-primary)] text-white'
                        : 'bg-[var(--ant-color-fill-quaternary)] text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]'
                    }`}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {loading ? (
            <div className="panel rounded-xl p-4">
              <Skeleton active paragraph={{ rows: 8 }} />
            </div>
          ) : (
            <>
              {podium.length > 0 ? (
                <div className="panel grid gap-3 rounded-xl p-4 md:grid-cols-3">
                  {[podium[1], podium[0], podium[2]].map((user, index) => {
                    if (!user) return null
                    const heights = ['md:mt-6', '', 'md:mt-10']
                    const medalTone =
                      user.rank === 1
                        ? 'from-[var(--ant-color-rank-gold)] to-[var(--ant-color-warning)]'
                        : user.rank === 2
                          ? 'from-[var(--ant-color-rank-silver)] to-[var(--ant-color-text-quaternary)]'
                          : 'from-[var(--ant-color-rank-bronze)] to-[var(--ant-color-warning-active)]'
                    return (
                      <Link
                        key={user.account_id}
                        to={`/profile?account_id=${user.account_id}`}
                        className={`rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-5 text-center hover:opacity-90 ${heights[index]}`}
                      >
                        <div
                          className={`mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br ${medalTone} text-lg font-bold text-white`}
                        >
                          {user.rank}
                        </div>
                        <div className="truncate font-semibold">{displayName(user.nickname, user.account_id)}</div>
                        <div className="muted-text mt-1 text-xs">
                          {board === 'rating' ? `${user.contests ?? 0} 场` : scoreLabel}
                        </div>
                        <div className="mt-2 text-xl font-semibold tabular-nums">{formatScore(user.score)}</div>
                        {board === 'rating' ? (
                          <div className="mt-2">
                            <DeltaText delta={user.delta} />
                          </div>
                        ) : null}
                      </Link>
                    )
                  })}
                </div>
              ) : null}

              <div className="panel overflow-hidden rounded-xl">
                <div
                  className={`grid items-center gap-2 border-b border-[color-mix(in_srgb,var(--ant-color-border)_45%,transparent)] px-4 py-3 text-sm text-[var(--ant-color-text-secondary)] ${
                    board === 'rating'
                      ? 'grid-cols-[64px_minmax(0,1.4fr)_88px_88px_64px]'
                      : 'grid-cols-[64px_minmax(0,1.4fr)_120px]'
                  }`}
                >
                  <div>排名</div>
                  <div>选手</div>
                  {board === 'rating' ? (
                    <>
                      <div className="text-right">参赛</div>
                      <div className="text-right">Rating</div>
                      <div className="text-right">变动</div>
                    </>
                  ) : (
                    <div className="text-right">通关</div>
                  )}
                </div>
                {list.length === 0 && podium.length === 0 ? (
                  <div className="px-4 py-12">
                    <Empty description={board === 'solved' ? '暂无通关记录' : '暂无 Rating 数据'} />
                  </div>
                ) : (
                  <div>
                    {list.map((user, index) => (
                      <div
                        key={user.account_id}
                        className={`list-row grid items-center gap-2 px-4 py-3.5 ${
                          board === 'rating'
                            ? 'grid-cols-[64px_minmax(0,1.4fr)_88px_88px_64px]'
                            : 'grid-cols-[64px_minmax(0,1.4fr)_120px]'
                        } ${
                          index % 2 === 0
                            ? 'bg-[var(--ant-color-bg-container)]'
                            : 'bg-[var(--ant-color-fill-alter)]'
                        }`}
                      >
                        <div>
                          <RankBadge rank={user.rank} />
                        </div>
                        <Link
                          to={`/profile?account_id=${user.account_id}`}
                          className="flex min-w-0 items-center gap-3 hover:text-[var(--ant-color-primary)]"
                        >
                          <Avatar
                            size={36}
                            src={resolveFileUrl(user.avatar) || undefined}
                            icon={<UserOutlined />}
                          />
                          <span className="truncate font-medium">
                            {displayName(user.nickname, user.account_id)}
                          </span>
                        </Link>
                        {board === 'rating' ? (
                          <>
                            <div className="text-right tabular-nums">{user.contests ?? 0}</div>
                            <div className="text-right font-semibold tabular-nums">
                              {formatScore(user.score)}
                            </div>
                            <div className="text-right">
                              <DeltaText delta={user.delta} />
                            </div>
                          </>
                        ) : (
                          <div className="text-right font-semibold tabular-nums">
                            {formatScore(user.score)}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
                {total > 0 ? (
                  <div className="flex justify-end border-t border-[color-mix(in_srgb,var(--ant-color-border)_45%,transparent)] px-4 py-3">
                    <Pagination
                      current={current}
                      pageSize={size}
                      total={total}
                      showSizeChanger
                      showTotal={(count) => `共 ${count} 人`}
                      onChange={(nextCurrent, nextSize) => {
                        setCurrent(nextCurrent)
                        setSize(nextSize)
                      }}
                    />
                  </div>
                ) : null}
              </div>
            </>
          )}
        </div>

        <aside className="flex flex-col gap-4">
          <div className="panel sticky top-[80px] rounded-xl p-4">
            <div className="text-sm font-semibold">我的排名</div>
            <div className="mt-3 rounded-xl bg-[var(--ant-color-fill-quaternary)] p-3">
              {loggedIn ? (
                <>
                  <div className="flex items-center gap-3">
                    <Avatar
                      size={44}
                      src={resolveFileUrl(me?.avatar || userInfo?.avatar) || undefined}
                      icon={<UserOutlined />}
                    />
                    <div className="min-w-0">
                      <div className="truncate font-medium">
                        {displayName(me?.nickname, userInfo?.accountId)}
                      </div>
                      <div className="muted-text text-xs">
                        {me?.rank ? `当前 ${tabs.find((t) => t.key === board)?.label}` : '尚未进入本榜'}
                      </div>
                    </div>
                  </div>
                  <div className="mt-3 grid grid-cols-3 gap-2 text-center">
                    <div>
                      <div className="text-lg font-semibold tabular-nums">{me?.rank ? `#${me.rank}` : '-'}</div>
                      <div className="muted-text text-[11px]">名次</div>
                    </div>
                    <div>
                      <div className="text-lg font-semibold tabular-nums">{formatScore(me?.score ?? 0)}</div>
                      <div className="muted-text text-[11px]">{scoreLabel}</div>
                    </div>
                    <div>
                      <div className="text-lg font-semibold tabular-nums">
                        {board === 'rating' ? <DeltaText delta={me?.delta} /> : me?.score ? '已上榜' : '-'}
                      </div>
                      <div className="muted-text text-[11px]">{board === 'rating' ? '变动' : '状态'}</div>
                    </div>
                  </div>
                </>
              ) : (
                <div className="flex items-center gap-3">
                  <div className="flex h-11 w-11 items-center justify-center rounded-full bg-[var(--ant-color-primary-bg)] text-[var(--ant-color-primary)]">
                    <UserOutlined />
                  </div>
                  <div>
                    <div className="font-medium">未登录</div>
                    <div className="muted-text text-xs">登录后查看个人名次</div>
                  </div>
                </div>
              )}
            </div>
            {loggedIn ? (
              <Link
                to="/profile"
                className="mt-3 flex items-center justify-center rounded-lg bg-[var(--ant-color-primary)] py-2 text-sm text-white"
              >
                查看个人主页
              </Link>
            ) : (
              <Link
                to="/auth/login"
                className="mt-3 flex items-center justify-center rounded-lg bg-[var(--ant-color-primary)] py-2 text-sm text-white"
              >
                登录查看我的排名
              </Link>
            )}
          </div>

          {board === 'rating' && rising.length > 0 ? (
            <div className="panel rounded-xl p-4">
              <div className="mb-3 flex items-center gap-2 text-sm font-semibold">
                <RiseOutlined className="text-[var(--ant-color-success)]" />
                本页上涨选手
              </div>
              <div className="space-y-2">
                {rising.map((user, index) => (
                  <Link
                    key={user.account_id}
                    to={`/profile?account_id=${user.account_id}`}
                    className="flex items-center gap-2 rounded-lg px-1 py-1.5 hover:bg-[var(--ant-color-fill-quaternary)]"
                  >
                    <span className="w-5 text-sm tabular-nums text-[var(--ant-color-text-secondary)]">
                      {index + 1}
                    </span>
                    <div className="min-w-0 flex-1 truncate text-sm font-medium">
                      {displayName(user.nickname, user.account_id)}
                    </div>
                    <DeltaText delta={user.delta} />
                  </Link>
                ))}
              </div>
            </div>
          ) : null}

          <div className="rounded-xl bg-[var(--ant-color-primary-bg)] px-4 py-4">
            <div className="text-sm font-semibold text-[var(--ant-color-primary)]">说明</div>
            <div className="mt-1 text-xs text-[var(--ant-color-primary-active)]">
              {board === 'solved'
                ? '练习榜统计正式提交通过的去重题目数。完成题库、题单、学习计划或竞赛中的题目均可累计。'
                : 'Rating 来自竞赛结算回写。参加带 Rating 的比赛并结算后才会进入本榜。'}
            </div>
            <Link
              to={board === 'solved' ? '/problems' : '/contests'}
              className="mt-3 inline-flex rounded-lg bg-[var(--ant-color-primary)] px-3 py-1.5 text-sm text-white"
            >
              {board === 'solved' ? '去做题' : '参加比赛'}
            </Link>
          </div>
        </aside>
      </div>
    </div>
  )
}
