import { useEffect, useMemo, useRef, useState } from 'react'
import { Collapse, Empty, Spin, Table, Tag, Typography, theme } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
  DatabaseOutlined,
  LinkOutlined,
  RightOutlined,
} from '@ant-design/icons'
import { Link, useParams } from 'react-router-dom'
import { MonacoEditor } from '@/components/editor/MonacoEditor'
import { SubmissionPerformance } from '@/components/oj/SubmissionPerformance'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { useDict } from '@/hooks/useDict'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { languageLabel, monacoLanguage } from '@/utils/monacoLanguage'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/time'
import { submissionApi } from '@/api'

const formatTime = (value: string | null) => formatDateTime(value)
const formatMemory = (kb: number) => `${(kb / 1024).toFixed(1)} MB`

export function SubmissionDetailPage() {
  const { id = '' } = useParams()
  useDict()
  const { token } = theme.useToken()
  const isLogin = useAuthStore((s) => s.isLogin)
  const resolvedTheme = useAppStore((s) => s.resolvedTheme)

  const [detail, setDetail] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [snapshot, setSnapshot] = useState<any>(null)
  const [watching, setWatching] = useState(false)

  const abortRef = useRef<AbortController | null>(null)

  async function load() {
    try {
      const res = await submissionApi.submissionDetail(id)
      setDetail(res.data)
      if (!submissionApi.isTerminalStatus(res.data.status) && isLogin()) {
        startWatching(id)
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
    return () => {
      abortRef.current?.abort()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function startWatching(submissionId: string) {
    const controller = new AbortController()
    abortRef.current?.abort()
    abortRef.current = controller
    setWatching(true)

    await submissionApi.watchSubmissionEvents(
      submissionId,
      {
        onSnapshot: (snap: any) => setSnapshot(snap),
        onUpdate: (snap: any) => setSnapshot(snap),
        onDone: async (snap: any) => {
          setSnapshot(snap)
          setWatching(false)
          await reloadDetail()
        },
        onTimeout: async (snap: any) => {
          setSnapshot(snap)
          setWatching(false)
          await reloadDetail()
        },
        onError: async () => {
          try {
            const final = await submissionApi.pollSubmissionUntilDone(submissionId, {
              signal: controller.signal,
              maxWaitMs: 120_000,
            })
            setSnapshot(final)
            await reloadDetail()
          } catch {
            // aborted
          } finally {
            if (!controller.signal.aborted) {
              setWatching(false)
            }
          }
        },
      },
      { signal: controller.signal, maxWaitSec: 120 },
    )
    if (!controller.signal.aborted) {
      setWatching(false)
    }
  }

  async function reloadDetail() {
    const res = await submissionApi.submissionDetail(id)
    setDetail(res.data)
  }

  const caseColor = (result?: string | null) => {
    if (!result) return token.colorTextSecondary
    return dictTypeColor('SUBMISSION_RESULT', result) || token.colorTextSecondary
  }

  const casesColumns: ColumnsType<any> = [
    {
      title: '#',
      dataIndex: 'case_no',
      width: 56,
      render: (no: number) => <span className="muted-text">#{no}</span>,
    },
    {
      title: '结果',
      dataIndex: 'result',
      width: 110,
      render: (result: string | null) => (
        <Tag color={caseColor(result) || undefined}>
          {result ? dictTypeData('SUBMISSION_RESULT', result) || result : '-'}
        </Tag>
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
      render: (time: number) => <span>{time} ms</span>,
    },
    {
      title: '内存',
      dataIndex: 'memory_kb',
      width: 90,
      align: 'right',
      render: (kb: number) => <span>{formatMemory(kb)}</span>,
    },
  ]

  const status = snapshot?.status ?? detail?.status
  const result = snapshot?.result ?? detail?.result
  const score = snapshot?.score ?? detail?.score ?? 0
  const timeMs = snapshot?.time_ms ?? detail?.time_ms ?? 0
  const memoryKb = snapshot?.memory_kb ?? detail?.memory_kb ?? 0
  const compileOutput = snapshot?.compile_output ?? detail?.compile_output
  const cases = snapshot?.cases.length ? snapshot.cases : (detail?.cases ?? [])
  const showPerformance = Boolean(
    detail &&
      detail.kind === 'OFFICIAL' &&
      !detail.contest_id &&
      detail.result === 'AC' &&
      detail.status === 'COMPLETED',
  )

  const hasSource = detail?.source !== null && detail?.source !== undefined

  const caseStats = useMemo(() => {
    const total = cases.length
    const ac = cases.filter((c: any) => c.result === 'AC').length
    const fail = Math.max(0, total - ac)
    const rate = total ? Math.round((ac / total) * 100) : 0
    return { total, ac, fail, rate }
  }, [cases])

  if (loading && !detail) {
    return (
      <div className="panel flex items-center justify-center rounded-xl py-16">
        <Spin />
      </div>
    )
  }

  if (!detail) {
    return (
      <div className="panel rounded-xl py-10">
        <Empty description="提交不存在" />
      </div>
    )
  }

  return (
    <div className="page-shell flex w-full flex-col gap-4">
      <div className="panel rounded-xl p-5">
        <div className="flex flex-wrap items-start gap-3">
          <div className="min-w-0 flex-1">
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <VerdictBadge status={status} result={result} />
              {watching ? (
                <span className="muted-text inline-flex items-center gap-2 text-xs">
                  <Spin /> {status || 'JUDGING'}
                </span>
              ) : null}
            </div>
            <h1 className="text-2xl font-semibold">
              {detail.problem_code}. {detail.problem_name}
            </h1>
            <div className="muted-text mt-2 flex flex-wrap gap-x-5 gap-y-1 text-sm">
              <span>语言 {languageLabel(detail.language_key)}</span>
              <span>{formatTime(detail.created_at)}</span>
              <span className="font-mono text-xs">ID {detail.id}</span>
            </div>
          </div>
          <Link
            to={`/problems/${detail.problem_id}`}
            className="inline-flex items-center gap-1 rounded-lg bg-[var(--ant-color-primary)] px-3 py-2 text-sm text-white"
          >
            打开题目 <RightOutlined />
          </Link>
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {[
          {
            label: '得分',
            value: score,
            icon: <CheckCircleOutlined />,
            tone: 'text-[var(--ant-color-success)] bg-[var(--ant-color-success-bg)]',
          },
          {
            label: '耗时',
            value: `${timeMs} ms`,
            icon: <ClockCircleOutlined />,
            tone: 'text-[var(--ant-color-warning)] bg-[var(--ant-color-warning-bg)]',
          },
          {
            label: '内存',
            value: formatMemory(memoryKb),
            icon: <DatabaseOutlined />,
            tone: 'text-[var(--ant-color-info)] bg-[var(--ant-color-info-bg)]',
          },
          {
            label: '用例通过率',
            value: caseStats.total ? `${caseStats.rate}%` : '-',
            icon: <CodeOutlined />,
            tone: 'text-[var(--ant-color-primary)] bg-[var(--ant-color-primary-bg)]',
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

      {showPerformance ? (
        <div className="panel rounded-xl p-4">
          <div className="mb-3 text-sm font-semibold">练习表现</div>
          <SubmissionPerformance submissionId={detail.id} problemId={detail.problem_id} />
        </div>
      ) : null}

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_300px]">
        <div className="min-w-0 flex flex-col gap-4">
          {detail.error ? (
            <div className="panel rounded-xl p-4">
              <div className="mb-2 text-sm font-semibold">错误信息</div>
              <pre className="error-box m-0 whitespace-pre-wrap break-words rounded-lg p-3 text-sm">
                {detail.error}
              </pre>
            </div>
          ) : null}

          {compileOutput ? (
            <div className="panel rounded-xl p-4">
              <Collapse
                items={[
                  {
                    key: 'compile',
                    label: '编译输出',
                    children: (
                      <pre className="m-0 max-h-64 overflow-auto whitespace-pre-wrap break-words text-xs">
                        {compileOutput}
                      </pre>
                    ),
                  },
                ]}
              />
            </div>
          ) : null}

          <div className="panel overflow-hidden rounded-xl">
            <div className="panel-header justify-between text-sm font-medium">
              <span>测试用例（{cases.length}）</span>
              <span className="muted-text font-normal">
                通过 {caseStats.ac} · 未通过 {caseStats.fail}
              </span>
            </div>
            {cases.length ? (
              <>
                <div className="flex flex-wrap gap-1.5 border-b border-[color-mix(in_srgb,var(--ant-color-border)_45%,transparent)] px-4 py-3">
                  {cases.map((c: any) => (
                    <span
                      key={c.case_no}
                      title={`#${c.case_no} ${c.result ? dictTypeData('SUBMISSION_RESULT', c.result) || c.result : '-'}`}
                      className="inline-flex h-7 min-w-7 items-center justify-center rounded-md px-1.5 text-xs font-medium text-white"
                      style={{
                        background: c.result
                          ? caseColor(c.result)
                          : token.colorFillSecondary,
                        color: c.result ? '#fff' : token.colorTextSecondary,
                      }}
                    >
                      {c.case_no}
                    </span>
                  ))}
                </div>
                <Table
                  rowKey="case_no"
                  columns={casesColumns}
                  dataSource={cases as any[]}
                  pagination={false}
                />
              </>
            ) : (
              <div className="py-10">
                <Empty description="暂无用例明细" />
              </div>
            )}
          </div>

          <div className="panel overflow-hidden rounded-xl">
            <div className="panel-header text-sm font-medium">源码</div>
            <div className="p-3">
              {hasSource ? (
                <div className="editor-shell rounded-lg">
                  <MonacoEditor
                    value={detail.source ?? ''}
                    language={monacoLanguage(detail.language_key)}
                    theme={resolvedTheme === 'dark' ? 'vs-dark' : 'vs'}
                    readOnly
                    height={480}
                  />
                </div>
              ) : (
                <Empty description="当前题目不允许查看源码" />
              )}
            </div>
          </div>
        </div>

        <aside className="flex flex-col gap-4">
          <div className="panel sticky top-[80px] rounded-xl p-4">
            <div className="mb-3 text-sm font-semibold">提交摘要</div>
            <div className="space-y-3 text-sm">
              <div>
                <div className="muted-text text-xs">题目</div>
                <Link
                  to={`/problems/${detail.problem_id}`}
                  className="mt-0.5 inline-flex items-center gap-1 text-[var(--ant-color-primary)]"
                >
                  {detail.problem_code}. {detail.problem_name}
                  <LinkOutlined />
                </Link>
              </div>
              <div>
                <div className="muted-text text-xs">判题结果</div>
                <div className="mt-1">
                  <VerdictBadge status={status} result={result} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-3 py-2">
                  <div className="muted-text text-[11px]">得分</div>
                  <div className="font-semibold tabular-nums">{score}</div>
                </div>
                <div className="rounded-xl bg-[var(--ant-color-fill-quaternary)] px-3 py-2">
                  <div className="muted-text text-[11px]">语言</div>
                  <div className="font-semibold">{languageLabel(detail.language_key)}</div>
                </div>
              </div>
            </div>

            <div className="mt-4">
              <div className="mb-1 flex items-center justify-between text-xs">
                <span className="muted-text">用例通过</span>
                <span className="tabular-nums">
                  {caseStats.ac}/{caseStats.total || '-'}
                </span>
              </div>
              <div className="h-1.5 overflow-hidden rounded-full bg-[var(--ant-color-fill-quaternary)]">
                <div
                  className="h-full rounded-full bg-[var(--ant-color-success)]"
                  style={{ width: `${caseStats.rate}%` }}
                />
              </div>
            </div>

            <div className="mt-4 flex flex-col gap-2">
              <Link
                to="/submissions"
                className="flex items-center justify-center rounded-lg bg-[var(--ant-color-primary)] py-2 text-sm text-white"
              >
                返回提交列表
              </Link>
              <Link
                to={`/problems/${detail.problem_id}`}
                className="flex items-center justify-center rounded-lg bg-[var(--ant-color-fill-quaternary)] py-2 text-sm"
              >
                再做一题
              </Link>
            </div>
          </div>

          {showPerformance ? (
            <div className="rounded-xl bg-[var(--ant-color-primary-bg)] px-4 py-4">
              <div className="text-sm font-semibold text-[var(--ant-color-primary)]">练习表现</div>
              <Typography.Paragraph className="!mb-2 !mt-1 text-xs text-[var(--ant-color-primary-text)]">
                上方已展示击败比例、用时/内存分布与相似解法。
              </Typography.Paragraph>
              <Link
                to={`/problems/${detail.problem_id}?tab=passed&submission_id=${detail.id}`}
                className="text-xs text-[var(--ant-color-primary)]"
              >
                在题目页打开「通过」
              </Link>
            </div>
          ) : (
            <div className="rounded-xl bg-[var(--ant-color-primary-bg)] px-4 py-4">
              <div className="text-sm font-semibold text-[var(--ant-color-primary)]">复盘提示</div>
              <Typography.Paragraph className="!mb-0 !mt-1 text-xs text-[var(--ant-color-primary-text)]">
                对照失败用例的耗时与内存；CE 先看编译输出；WA 优先核对边界与输出格式。
              </Typography.Paragraph>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}
