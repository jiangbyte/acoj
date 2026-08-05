import { useEffect, useRef, useState } from 'react'
import { Card, Collapse, Descriptions, Empty, Spin, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useParams } from 'react-router-dom'
import { submissionDetail } from '@/api/submission'
import type { OjSubmissionCase, OjSubmissionDetail } from '@/api/submission'
import type { SubmissionSnapshot } from '@/api/problem'
import { isTerminalStatus, pollSubmissionUntilDone, watchSubmissionEvents } from '@/api/submissionWatch'
import { MonacoEditor } from '@/components/editor/MonacoEditor'
import { SubmissionPerformance } from '@/components/oj/SubmissionPerformance'
import { VerdictBadge } from '@/components/oj/VerdictBadge'
import { languageLabel, monacoLanguage } from '@/utils/monacoLanguage'
import { useAuthStore } from '@/stores/auth'

const formatTime = (value: string | null) => (value ? new Date(value).toLocaleString() : '-')
const formatMemory = (kb: number) => `${(kb / 1024).toFixed(1)} MB`

const caseColor = (result?: string | null) => {
  if (!result) return 'default'
  if (result === 'AC') return 'success'
  if (result === 'WA') return 'error'
  if (result === 'TLE' || result === 'MLE') return 'warning'
  return 'error'
}

export function SubmissionDetailPage() {
  const { id = '' } = useParams()
  const isLogin = useAuthStore((s) => s.isLogin)

  const [detail, setDetail] = useState<OjSubmissionDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [snapshot, setSnapshot] = useState<SubmissionSnapshot | null>(null)
  const [watching, setWatching] = useState(false)

  const abortRef = useRef<AbortController | null>(null)

  async function load() {
    try {
      const res = await submissionDetail(id)
      setDetail(res.data)
      if (!isTerminalStatus(res.data.status) && isLogin()) {
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

    await watchSubmissionEvents(
      submissionId,
      {
        onSnapshot: (snap) => setSnapshot(snap),
        onUpdate: (snap) => setSnapshot(snap),
        onDone: async (snap) => {
          setSnapshot(snap)
          setWatching(false)
          await reloadDetail()
        },
        onTimeout: async (snap) => {
          setSnapshot(snap)
          setWatching(false)
          await reloadDetail()
        },
        onError: async () => {
          try {
            const final = await pollSubmissionUntilDone(submissionId, {
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
    const res = await submissionDetail(id)
    setDetail(res.data)
  }

  const casesColumns: ColumnsType<OjSubmissionCase> = [
    {
      title: '#',
      dataIndex: 'case_no',
      width: 56,
      render: (no: number) => <span className="text-gray-500">#{no}</span>,
    },
    {
      title: '结果',
      dataIndex: 'result',
      width: 110,
      render: (result: string | null) => (
        <Tag color={caseColor(result)}>{result || '等待'}</Tag>
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

  if (loading && !detail) {
    return (
      <Card>
        <Spin />
      </Card>
    )
  }

  if (!detail) {
    return (
      <Card>
        <Empty description="提交不存在" />
      </Card>
    )
  }

  const status = snapshot?.status ?? detail.status
  const result = snapshot?.result ?? detail.result
  const score = snapshot?.score ?? detail.score
  const timeMs = snapshot?.time_ms ?? detail.time_ms
  const memoryKb = snapshot?.memory_kb ?? detail.memory_kb
  const compileOutput = snapshot?.compile_output ?? detail.compile_output
  const cases = snapshot?.cases.length ? snapshot.cases : detail.cases
  const hasSource = detail.source !== null && detail.source !== undefined
  const showPerformance =
    detail.kind === 'OFFICIAL' &&
    !detail.contest_id &&
    detail.result === 'AC' &&
    detail.status === 'COMPLETED'

  return (
    <div className="space-y-4">
      <Card>
        <div className="flex flex-wrap items-center gap-3">
          <VerdictBadge status={status} result={result} />
          {watching ? <Spin size="small" /> : null}
          <Typography.Text type="secondary">
            提交 ID：
            <span className="font-mono">{detail.id}</span>
          </Typography.Text>
        </div>
        <Descriptions
          className="mt-4"
          size="small"
          column={{ xs: 1, sm: 2, lg: 4 }}
          items={[
            {
              key: 'problem',
              label: '题目',
              children: (
                <Link to={`/problems/${detail.problem_id}`}>
                  {detail.problem_code}. {detail.problem_name}
                </Link>
              ),
            },
            { key: 'language', label: '语言', children: languageLabel(detail.language_key) },
            { key: 'score', label: '得分', children: score },
            { key: 'time', label: '耗时', children: `${timeMs} ms` },
            { key: 'memory', label: '内存', children: formatMemory(memoryKb) },
            {
              key: 'created_at',
              label: '提交时间',
              children: formatTime(detail.created_at),
            },
          ]}
        />
      </Card>

      {showPerformance ? (
        <Card size="small" title="练习表现">
          <SubmissionPerformance submissionId={detail.id} problemId={detail.problem_id} />
        </Card>
      ) : null}

      {detail.error ? (
        <Card size="small" title="错误信息">
          <pre className="m-0 whitespace-pre-wrap break-words text-sm text-red-600">{detail.error}</pre>
        </Card>
      ) : null}

      {compileOutput ? (
        <Card size="small">
          <Collapse
            size="small"
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
        </Card>
      ) : null}

      <Card size="small" title={`测试用例（${cases.length}）`}>
        <Table
          rowKey="case_no"
          size="small"
          columns={casesColumns}
          dataSource={cases as OjSubmissionCase[]}
          pagination={false}
        />
      </Card>

      <Card size="small" title="源码">
        {hasSource ? (
          <MonacoEditor
            value={detail.source ?? ''}
            language={monacoLanguage(detail.language_key)}
            readOnly
            height={480}
          />
        ) : (
          <Empty description="当前题目不允许查看源码" />
        )}
      </Card>
    </div>
  )
}
