import { useEffect, useMemo, useState } from 'react'
import { Input, Select, Table, Typography, theme } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
  FireOutlined,
  RightOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { Link, useSearchParams } from 'react-router-dom'
import { submissionApi } from '@/api'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useDict } from '@/hooks/useDict'
import { useAuthStore } from '@/stores/auth'
import { dictList } from '@/utils/dict'
import { languageLabel } from '@/utils/monacoLanguage'
import type { PageData } from '@/typing/api'
import { formatDateTime } from '@/utils/time'

const formatTime = (value: string) => formatDateTime(value)

export function SubmissionListPage() {
  useDict()
  const { token } = theme.useToken()
  const isLogin = useAuthStore((s) => s.isLogin)
  const resultOptions = dictList('SUBMISSION_RESULT')
  const quickFilters = [{ value: '', label: '全部' }, ...resultOptions.slice(0, 5)]
  const [searchParams, setSearchParams] = useSearchParams()
  const problemCode = searchParams.get('problem_code') ?? ''
  const result = searchParams.get('result') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 20)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<PageData<any> | null>(null)
  const [codeText, setCodeText] = useState(problemCode)
  const [myStats, setMyStats] = useState<any>(null)
  const [myStatsLoading, setMyStatsLoading] = useState(false)

  async function load() {
    try {
      const res = await submissionApi.submissionPage({
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

  async function loadMyStats() {
    if (!isLogin()) {
      setMyStats(null)
      return
    }
    setMyStatsLoading(true)
    try {
      const res = await submissionApi.mySubmissionStats()
      setMyStats(res.data)
    } catch {
      setMyStats(null)
    } finally {
      setMyStatsLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [problemCode, result, current, size])

  useEffect(() => {
    void loadMyStats()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLogin()])

  function applyFilter(next: { problem_code?: string; result?: string; current?: number; size?: number }) {
    setLoading(true)
    const params: Record<string, string> = {}
    const nextCode = next.problem_code ?? problemCode
    const nextResult = next.result ?? result
    const nextCurrent = next.current ?? current
    const nextSize = next.size ?? size
    if (nextCode) params.problem_code = nextCode
    if (nextResult) params.result = nextResult
    if (nextCurrent > 1) params.current = String(nextCurrent)
    if (nextSize !== 20) params.size = String(nextSize)
    setSearchParams(params)
  }

  const records = data?.records ?? []
  const total = data?.total ?? 0

  const stats = useMemo(() => {
    const ac = records.filter((r) => r.result === 'AC' || r.result === 'ACCEPTED').length
    const judging = records.filter((r) =>
      ['QUEUED', 'JUDGING', 'PENDING'].includes(r.status || ''),
    ).length
    const fail = Math.max(0, records.length - ac - judging)
    return {
      total,
      pageAc: ac,
      pageFail: fail,
      pageJudging: judging,
      avgTime:
        records.length > 0
          ? Math.round(records.reduce((sum, r) => sum + (r.time_ms || 0), 0) / records.length)
          : 0,
    }
  }, [records, total])

  const recentAc = useMemo(
    () => records.filter((r) => r.result === 'AC' || r.result === 'ACCEPTED').slice(0, 5),
    [records],
  )

  const langBuckets = useMemo(() => {
    const map = new Map<string, number>()
    for (const row of records) {
      const key = languageLabel(row.language_key)
      map.set(key, (map.get(key) ?? 0) + 1)
    }
    return [...map.entries()]
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
  }, [records])

  const columns: ColumnsType<any> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
      render: (id: string) => (
        <Link
          to={`/submissions/${id}`}
          className="font-mono text-xs"
          style={{ color: token.colorPrimary }}
        >
          {id.length > 12 ? id.slice(0, 12) : id}
        </Link>
      ),
    },
    {
      title: '题号',
      dataIndex: 'problem_code',
      width: 110,
      render: (code: string | null, record) => (
        <Link
          to={`/problems/${record.problem_id}`}
          className="font-mono text-sm"
          style={{ color: token.colorPrimary }}
        >
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
      render: (kb: number) => (
        <Typography.Text type="secondary">{(kb / 1024).toFixed(1)} MB</Typography.Text>
      ),
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

  const myStatItems = [
    {
      label: '我的提交',
      value: myStats?.submission_total ?? 0,
      tone: 'text-[var(--ant-color-primary)]',
    },
    {
      label: '通过次数',
      value: myStats?.ac_total ?? 0,
      tone: 'text-[var(--ant-color-success)]',
    },
    {
      label: '通过率',
      value: `${Number(myStats?.ac_rate ?? 0).toFixed(1)}%`,
      tone: 'text-[var(--ant-color-warning)]',
    },
    {
      label: '已解题数',
      value: myStats?.solved_problem_total ?? 0,
      tone: 'text-[var(--ant-color-info)]',
    },
  ]

  return (
    <div className="page-shell flex w-full flex-col gap-4">
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {[
          {
            label: '总提交',
            value: stats.total,
            icon: <ThunderboltOutlined />,
            tone: 'text-[var(--ant-color-primary)] bg-[var(--ant-color-primary-bg)]',
          },
          {
            label: '本页通过',
            value: stats.pageAc,
            icon: <CheckCircleOutlined />,
            tone: 'text-[var(--ant-color-success)] bg-[var(--ant-color-success-bg)]',
          },
          {
            label: '本页未通过',
            value: stats.pageFail,
            icon: <FireOutlined />,
            tone: 'text-[var(--ant-color-error)] bg-[var(--ant-color-error-bg)]',
          },
          {
            label: '本页均耗时',
            value: `${stats.avgTime}ms`,
            icon: <ClockCircleOutlined />,
            tone: 'text-[var(--ant-color-warning)] bg-[var(--ant-color-warning-bg)]',
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
            <div className="flex flex-wrap items-start gap-3">
              <div className="min-w-0 flex-1">
                <Typography.Title level={4} className="!mb-0">
                  提交记录
                </Typography.Title>
                <Typography.Text type="secondary" className="text-sm">
                  查看全站提交状态并按题号或结果筛选
                </Typography.Text>
              </div>
              <Input.Search
                className="w-52"
                placeholder="题号"
                allowClear
                value={codeText}
                onChange={(e) => setCodeText(e.target.value)}
                onSearch={(value) => applyFilter({ problem_code: value, current: 1 })}
              />
              <Select
                className="w-36"
                placeholder="结果"
                allowClear
                value={result || undefined}
                options={resultOptions}
                onChange={(value) => applyFilter({ result: value || '', current: 1 })}
              />
            </div>

            <div className="mt-3 flex flex-wrap gap-2">
              {quickFilters.map((item) => (
                <button
                  key={item.label}
                  type="button"
                  onClick={() => applyFilter({ result: item.value, current: 1 })}
                  className={`chip ${
                    result === item.value
                      ? 'bg-[var(--ant-color-primary)] text-white'
                      : 'bg-[var(--ant-color-fill-quaternary)] text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]'
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </div>
          </div>

          <div className="panel overflow-hidden rounded-xl">
            <Table
              rowKey="id"
              loading={loading}
              columns={columns}
              dataSource={records}
              scroll={{ x: 'max-content' }}
              pagination={{
                current,
                pageSize: size,
                total,
                showSizeChanger: true,
                showTotal: (count) => `共 ${count} 条`,
                onChange: (nextCurrent, nextSize) => {
                  applyFilter({ current: nextCurrent, size: nextSize })
                },
              }}
            />
          </div>
        </div>

        <aside className="flex flex-col gap-4">
          <div className="panel sticky top-[80px] z-10 rounded-xl p-4">
            <div className="mb-3 flex items-center justify-between">
              <div className="flex items-center gap-2 text-sm font-semibold">
                <UserOutlined />
                我的提交统计
              </div>
              {isLogin() ? (
                <Link
                  to="/profile"
                  className="muted-text text-xs hover:text-[var(--ant-color-primary)]"
                >
                  个人主页 <RightOutlined />
                </Link>
              ) : null}
            </div>
            {!isLogin() ? (
              <div className="py-4 text-center">
                <div className="muted-text mb-3 text-sm">登录后查看个人提交汇总</div>
                <Link
                  to="/auth/login"
                  className="inline-flex rounded-lg bg-[var(--ant-color-primary)] px-3 py-1.5 text-sm text-white"
                >
                  去登录
                </Link>
              </div>
            ) : myStatsLoading && !myStats ? (
              <div className="muted-text py-6 text-center text-sm">加载中…</div>
            ) : (
              <>
                <div className="grid grid-cols-2 gap-2">
                  {myStatItems.map((item) => (
                    <div
                      key={item.label}
                      className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-3 py-2"
                    >
                      <div className={`text-base font-semibold tabular-nums ${item.tone}`}>
                        {item.value}
                      </div>
                      <div className="muted-text text-[11px]">{item.label}</div>
                    </div>
                  ))}
                </div>
                <div className="muted-text mt-3 flex flex-wrap gap-x-3 gap-y-1 text-[11px]">
                  <span>未通过 {myStats?.fail_total ?? 0}</span>
                  <span>判题中 {myStats?.judging_total ?? 0}</span>
                </div>
              </>
            )}
          </div>

          <div className="panel rounded-xl p-4">
            <div className="mb-3 flex items-center justify-between">
              <div className="text-sm font-semibold">本页通过</div>
              <Link to="/problems" className="muted-text text-xs hover:text-[var(--ant-color-primary)]">
                去做题 <RightOutlined />
              </Link>
            </div>
            {recentAc.length ? (
              <div className="space-y-2">
                {recentAc.map((row) => (
                  <Link
                    key={row.id}
                    to={`/submissions/${row.id}`}
                    className="list-row flex items-center gap-2 rounded-lg px-2 py-2"
                  >
                    <CheckCircleOutlined className="text-[var(--ant-color-success)]" />
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-sm font-medium">
                        {row.problem_code ?? '题目'} · {row.user_nickname || '匿名'}
                      </div>
                      <div className="muted-text truncate text-[11px]">
                        {languageLabel(row.language_key)} · {row.time_ms} ms
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="muted-text py-6 text-center text-sm">本页暂无通过记录</div>
            )}
          </div>

          <div className="panel rounded-xl p-4">
            <div className="mb-3 flex items-center gap-2 text-sm font-semibold">
              <CodeOutlined />
              本页语言分布
            </div>
            {langBuckets.length ? (
              <div className="space-y-2">
                {langBuckets.map(([lang, count]) => {
                  const max = langBuckets[0]?.[1] || 1
                  const width = Math.max(12, Math.round((count / max) * 100))
                  return (
                    <div key={lang}>
                      <div className="mb-1 flex items-center justify-between text-xs">
                        <span>{lang}</span>
                        <span className="muted-text tabular-nums">{count}</span>
                      </div>
                      <div className="h-1.5 overflow-hidden rounded-full bg-[var(--ant-color-fill-quaternary)]">
                        <div
                          className="h-full rounded-full bg-[var(--ant-color-primary)]"
                          style={{ width: `${width}%` }}
                        />
                      </div>
                    </div>
                  )
                })}
              </div>
            ) : (
              <div className="muted-text py-4 text-center text-sm">暂无数据</div>
            )}
            <div className="muted-text mt-3 text-[11px]">统计仅基于当前页，后续可接全站聚合</div>
          </div>

          <div className="rounded-xl bg-[var(--ant-color-primary-bg)] px-4 py-4">
            <div className="text-sm font-semibold text-[var(--ant-color-primary)]">复盘建议（占位）</div>
            <div className="mt-1 text-xs text-[var(--ant-color-primary-text)]">
              连续 WA 时可对比样例输出；TLE 优先检查复杂度与死循环。
            </div>
            <Link
              to="/rank"
              className="mt-3 inline-flex rounded-lg bg-[var(--ant-color-primary)] px-3 py-1.5 text-sm text-white"
            >
              看看排行榜
            </Link>
          </div>
        </aside>
      </div>
    </div>
  )
}
