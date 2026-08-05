import { useEffect, useState } from 'react'
import { Empty, Spin, Typography } from 'antd'
import { Link } from 'react-router-dom'
import { submissionApi } from '@/api'
import { MonacoEditor } from '@/components/editor/MonacoEditor'
import { languageLabel, monacoLanguage } from '@/utils/monacoLanguage'

const formatMemory = (kb: number) => `${(kb / 1024).toFixed(1)} MB`
const formatBeats = (pct: number) => `击败 ${pct.toFixed(2)}%`

type Props = {
  submissionId: string
  problemId?: string
  showBackLink?: boolean
  onBackToSubmissions?: () => void
}

function Histogram({
  title,
  buckets,
  unit,
}: {
  title: string
  buckets: any[]
  unit: string
}) {
  const maxCount = Math.max(...buckets.map((b) => b.count), 1)

  return (
    <div className="mt-4">
      <Typography.Text type="secondary" className="text-xs">
        {title}
      </Typography.Text>
      <div className="mt-2 flex h-28 items-end gap-px rounded bg-gray-50 p-2">
        {buckets.map((bucket, index) => {
          const heightPct = bucket.count > 0 ? (bucket.count / maxCount) * 100 : 2
          return (
            <div
              key={index}
              className="group relative min-w-0 flex-1"
              title={`${bucket.start.toFixed(0)}–${bucket.end.toFixed(0)} ${unit}：${bucket.count} 次`}
            >
              <div
                className={`w-full rounded-t transition-colors ${
                  bucket.is_current ? 'bg-blue-500' : 'bg-gray-300 hover:bg-gray-400'
                }`}
                style={{ height: `${heightPct}%`, minHeight: bucket.count > 0 ? 4 : 2 }}
              />
            </div>
          )
        })}
      </div>
    </div>
  )
}

function MetricBlock({
  label,
  value,
  beatsPct,
}: {
  label: string
  value: string
  beatsPct?: number | null
}) {
  return (
    <div>
      <Typography.Text type="secondary" className="text-xs">
        {label}
      </Typography.Text>
      <div className="text-3xl font-semibold tabular-nums text-gray-900">{value}</div>
      {beatsPct != null ? (
        <Typography.Text type="success" className="text-sm">
          {formatBeats(beatsPct)}
        </Typography.Text>
      ) : null}
    </div>
  )
}

function SimilarListItem({
  item,
  selected,
  onSelect,
}: {
  item: any
  selected: boolean
  onSelect: () => void
}) {
  return (
    <button
      type="button"
      className={`w-full cursor-pointer rounded border px-3 py-2 text-left text-sm transition-colors ${
        selected
          ? 'border-blue-400 bg-blue-50'
          : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
      }`}
      onClick={onSelect}
    >
      <div className="flex items-center justify-between gap-2">
        <span className="truncate font-medium">{item.nickname ?? '匿名用户'}</span>
        <span className="shrink-0 text-xs text-gray-500">{languageLabel(item.language_key)}</span>
      </div>
      <div className="mt-1 flex gap-3 text-xs text-gray-500">
        <span>{item.time_ms} ms</span>
        <span>{formatMemory(item.memory_kb)}</span>
      </div>
    </button>
  )
}

export function SubmissionPerformance({
  submissionId,
  problemId,
  showBackLink = false,
  onBackToSubmissions,
}: Props) {
  const [performance, setPerformance] = useState<any>(null)
  const [similarItems, setSimilarItems] = useState<any[]>([])
  const [similarAvailable, setSimilarAvailable] = useState(true)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [selectedSimilar, setSelectedSimilar] = useState<any>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setLoadError(null)
    setPerformance(null)
    setSimilarItems([])
    setSelectedSimilar(null)

    async function load() {
      try {
        const [perfRes, similarRes] = await Promise.all([
          submissionApi.submissionPerformance(submissionId),
          submissionApi.submissionSimilar(submissionId),
        ])
        if (cancelled) return
        setPerformance(perfRes.data)
        setSimilarAvailable(similarRes.data.available)
        setSimilarItems(similarRes.data.items)
        if (similarRes.data.items.length > 0) {
          setSelectedSimilar(similarRes.data.items[0])
        }
      } catch {
        if (!cancelled) {
          setLoadError('加载失败，请稍后重试')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void load()
    return () => {
      cancelled = true
    }
  }, [submissionId])

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spin />
      </div>
    )
  }

  if (loadError) {
    return <Empty description={loadError} className="py-8" />
  }

  if (!performance?.available) {
    return (
      <Empty
        description={performance?.reason ?? '该提交暂无练习分布数据'}
        className="py-8"
      />
    )
  }

  const insufficient = performance.insufficient_sample
  const hasRuntimeBuckets = (performance.runtime_buckets?.length ?? 0) > 0
  const hasMemoryBuckets = (performance.memory_buckets?.length ?? 0) > 0

  return (
    <div className="flex min-h-0 flex-col gap-4 lg:flex-row">
      <div className="min-w-0 flex-1">
        {showBackLink ? (
          <div className="mb-4">
            {onBackToSubmissions ? (
              <button
                type="button"
                className="cursor-pointer border-none bg-transparent p-0 text-sm text-blue-600 hover:text-blue-700"
                onClick={onBackToSubmissions}
              >
                返回全部提交记录
              </button>
            ) : problemId ? (
              <Link to={`/problems/${problemId}?tab=submissions`} className="text-sm text-blue-600">
                返回全部提交记录
              </Link>
            ) : null}
          </div>
        ) : null}

        <div className="grid grid-cols-2 gap-6">
          <MetricBlock
            label="运行用时"
            value={`${performance.time_ms ?? 0} ms`}
            beatsPct={insufficient ? null : performance.beats_time_pct}
          />
          <MetricBlock
            label="内存消耗"
            value={formatMemory(performance.memory_kb ?? 0)}
            beatsPct={insufficient ? null : performance.beats_memory_pct}
          />
        </div>

        {insufficient ? (
          <Typography.Text type="secondary" className="mt-4 block text-sm">
            样本不足，暂无分布
          </Typography.Text>
        ) : (
          <>
            {hasRuntimeBuckets && performance.runtime_buckets ? (
              <Histogram title="运行用时分布" buckets={performance.runtime_buckets} unit="ms" />
            ) : null}
            {hasMemoryBuckets && performance.memory_buckets ? (
              <Histogram title="内存消耗分布" buckets={performance.memory_buckets} unit="KB" />
            ) : null}
          </>
        )}
      </div>

      <div className="flex min-h-0 min-w-0 flex-1 flex-col border-t border-gray-200 pt-4 lg:border-t-0 lg:border-l lg:pl-4 lg:pt-0">
        <Typography.Text strong className="mb-3 block text-sm">
          相似解法
        </Typography.Text>

        {!similarAvailable || similarItems.length === 0 ? (
          <Empty description="暂无相似解法" className="py-6" />
        ) : (
          <>
            <div className="mb-3 max-h-48 space-y-2 overflow-y-auto">
              {similarItems.map((item) => (
                <SimilarListItem
                  key={item.id}
                  item={item}
                  selected={selectedSimilar?.id === item.id}
                  onSelect={() => setSelectedSimilar(item)}
                />
              ))}
            </div>

            {selectedSimilar ? (
              selectedSimilar.source != null ? (
                <MonacoEditor
                  value={selectedSimilar.source}
                  language={monacoLanguage(selectedSimilar.language_key)}
                  readOnly
                  height={320}
                />
              ) : (
                <Empty description="源码不可见" className="py-8" />
              )
            ) : null}
          </>
        )}
      </div>
    </div>
  )
}
