<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import type { PerformanceBucket, SimilarSubmissionItem, SubmissionPerformanceData } from '@/api/biz/submission/submission'
import { performance, similar } from '@/api/biz/submission/submission'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { formatDateTime, resolveFileUrl } from '@/utils'
import { monacoLanguageFromExtension } from '@/views/biz/problem/shared/monacoLanguage'
import { Chart } from '@antv/g2'
import { NAvatar, NFlex, NTag } from 'naive-ui'
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'

type ChartInstance = InstanceType<typeof Chart>

const props = defineProps<{
  submissionId: string
}>()

const loading = ref(false)
const loadError = ref<string | null>(null)
const performanceData = ref<SubmissionPerformanceData | null>(null)
const similarItems = ref<SimilarSubmissionItem[]>([])
const similarAvailable = ref(false)
const selectedSimilarId = ref<string | null>(null)
const sourceExpanded = ref<string[]>([])

const runtimeChartRef = ref<HTMLDivElement | null>(null)
const memoryChartRef = ref<HTMLDivElement | null>(null)
const charts: ChartInstance[] = []

const showPanel = computed(() => performanceData.value?.available === true)

const panelTitle = computed(() =>
  performanceData.value?.scope === 'contest' ? '竞赛内分布（本场）' : '练习分布',
)

const insufficientSample = computed(() => performanceData.value?.insufficient_sample === true)

const selectedSimilar = computed(() =>
  similarItems.value.find(item => item.id === selectedSimilarId.value) ?? null,
)

const similarColumns = computed<DataTableColumns<SimilarSubmissionItem>>(() => [
  {
    title: '用户',
    key: 'nickname',
    minWidth: 120,
    render: row => (
      <NFlex align="center" size={8}>
        {resolveFileUrl(row.avatar)
          ? (
              <NAvatar
                round
                size={24}
                src={resolveFileUrl(row.avatar)!}
                imgProps={{ referrerPolicy: 'no-referrer' }}
              />
            )
          : (
              <NAvatar round size={24} color="#d9d9d9">
                {(row.nickname?.[0] ?? '?').toUpperCase()}
              </NAvatar>
            )}
        <span>{row.nickname ?? '匿名用户'}</span>
      </NFlex>
    ),
  },
  {
    title: '语言',
    key: 'language_key',
    width: 100,
    render: row => row.language_key,
  },
  {
    title: '用时',
    key: 'time_ms',
    width: 90,
    render: row => `${row.time_ms} ms`,
  },
  {
    title: '内存',
    key: 'memory_kb',
    width: 100,
    render: row => formatMemory(row.memory_kb),
  },
  {
    title: '提交时间',
    key: 'created_at',
    minWidth: 150,
    render: row => formatDateTime(row.created_at),
  },
])

function formatMemory(kb: number) {
  return `${(kb / 1024).toFixed(1)} MB`
}

function formatBeats(pct: number) {
  return `击败 ${pct.toFixed(2)}%`
}

function monacoLanguage(languageKey: string) {
  const key = String(languageKey || '')
  if (key.startsWith('py'))
    return 'python'
  if (key.startsWith('java'))
    return 'java'
  if (key.startsWith('go'))
    return 'go'
  if (key.includes('js') || key.includes('node'))
    return 'javascript'
  if (key.startsWith('rs') || key.includes('rust'))
    return 'rust'
  return monacoLanguageFromExtension('.cpp')
}

function bucketChartData(buckets: PerformanceBucket[], unit: string) {
  return buckets.map((bucket, index) => ({
    index,
    count: bucket.count,
    type: bucket.is_current ? '当前提交' : '其他',
    range: `${Math.round(bucket.start)}–${Math.round(bucket.end)} ${unit}`,
  }))
}

function destroyCharts() {
  while (charts.length) {
    charts.pop()?.destroy()
  }
}

async function renderHistogram(
  container: HTMLDivElement,
  buckets: PerformanceBucket[],
  unit: string,
) {
  const chart = new Chart({ container, autoFit: true, height: 160 })
  const data = bucketChartData(buckets, unit)
  chart.options({
    type: 'interval',
    data,
    encode: { x: 'index', y: 'count', color: 'type' },
    scale: {
      color: {
        domain: ['其他', '当前提交'],
        range: ['#d1d5db', '#2563eb'],
      },
    },
    axis: {
      x: { title: false, label: false, tick: false },
      y: { title: false, grid: true },
    },
    legend: { color: { position: 'top', layout: { justifyContent: 'flex-end' } } },
    tooltip: {
      title: 'range',
      items: [{ channel: 'y', name: '次数' }],
    },
    style: { maxWidth: 24 },
  })
  charts.push(chart)
  await chart.render()
}

async function renderCharts() {
  destroyCharts()
  if (!performanceData.value || insufficientSample.value)
    return

  const tasks: Promise<void>[] = []
  const runtimeBuckets = performanceData.value.runtime_buckets ?? []
  const memoryBuckets = performanceData.value.memory_buckets ?? []

  if (runtimeBuckets.length && runtimeChartRef.value) {
    tasks.push(renderHistogram(runtimeChartRef.value, runtimeBuckets, 'ms'))
  }
  if (memoryBuckets.length && memoryChartRef.value) {
    tasks.push(renderHistogram(memoryChartRef.value, memoryBuckets, 'KB'))
  }
  await Promise.all(tasks)
}

async function fetchData(submissionId: string) {
  loading.value = true
  loadError.value = null
  performanceData.value = null
  similarItems.value = []
  similarAvailable.value = false
  selectedSimilarId.value = null
  sourceExpanded.value = []
  destroyCharts()

  try {
    const [performanceRes, similarRes] = await Promise.all([
      performance({ id: submissionId }),
      similar({ id: submissionId, size: 10 }),
    ])
    performanceData.value = performanceRes.data ?? null
    similarAvailable.value = similarRes.data?.available === true
    similarItems.value = similarRes.data?.items ?? []
    if (similarItems.value.length > 0) {
      selectedSimilarId.value = similarItems.value[0].id
    }
    await nextTick()
    await renderCharts()
  }
  catch {
    loadError.value = '加载失败，请稍后重试'
  }
  finally {
    loading.value = false
  }
}

function handleSimilarRowClick(row: SimilarSubmissionItem) {
  selectedSimilarId.value = row.id
  if (!sourceExpanded.value.includes('source')) {
    sourceExpanded.value = ['source']
  }
}

watch(
  () => props.submissionId,
  (submissionId) => {
    if (submissionId)
      void fetchData(submissionId)
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  destroyCharts()
})
</script>

<template>
  <div v-if="loading || showPanel || loadError" class="performance-panel">
    <NSpin :show="loading">
      <NAlert v-if="loadError" type="warning" :show-icon="false">
        {{ loadError }}
      </NAlert>

      <template v-else-if="showPanel && performanceData">
        <div class="text-13px font-medium">
          {{ panelTitle }}
        </div>

        <div class="metric-grid">
          <div>
            <div class="metric-label">
              运行用时
            </div>
            <div class="metric-value">
              {{ performanceData.time_ms ?? 0 }} ms
            </div>
            <NTag
              v-if="!insufficientSample && performanceData.beats_time_pct != null"
              size="small"
              type="success"
              :bordered="false"
            >
              {{ formatBeats(performanceData.beats_time_pct) }}
            </NTag>
          </div>
          <div>
            <div class="metric-label">
              内存消耗
            </div>
            <div class="metric-value">
              {{ formatMemory(performanceData.memory_kb ?? 0) }}
            </div>
            <NTag
              v-if="!insufficientSample && performanceData.beats_memory_pct != null"
              size="small"
              type="success"
              :bordered="false"
            >
              {{ formatBeats(performanceData.beats_memory_pct) }}
            </NTag>
          </div>
        </div>

        <div v-if="insufficientSample" class="insufficient-tip">
          样本不足，暂无分布
        </div>
        <template v-else>
          <div v-if="(performanceData.runtime_buckets?.length ?? 0) > 0" class="chart-block">
            <div class="chart-title">
              运行用时分布
            </div>
            <div ref="runtimeChartRef" class="chart-box" />
          </div>
          <div v-if="(performanceData.memory_buckets?.length ?? 0) > 0" class="chart-block">
            <div class="chart-title">
              内存消耗分布
            </div>
            <div ref="memoryChartRef" class="chart-box" />
          </div>
        </template>

        <div class="similar-section">
          <div class="text-13px font-medium">
            相似解法
          </div>

          <NEmpty
            v-if="!similarAvailable || similarItems.length === 0"
            description="暂无相似解法"
            class="py-16px"
          />
          <template v-else>
            <NDataTable
              size="small"
              :bordered="false"
              :columns="similarColumns"
              :data="similarItems"
              :pagination="false"
              :row-key="(row: SimilarSubmissionItem) => row.id"
              :row-props="(row: SimilarSubmissionItem) => ({
                style: selectedSimilarId === row.id ? 'cursor: pointer; background: var(--n-td-color-hover)' : 'cursor: pointer',
                onClick: () => handleSimilarRowClick(row),
              })"
              :max-height="220"
            />

            <NCollapse
              v-if="selectedSimilar"
              v-model:expanded-names="sourceExpanded"
              class="mt-8px"
            >
              <NCollapseItem title="源码" name="source">
                <MonacoEditor
                  v-if="selectedSimilar.source != null"
                  :value="selectedSimilar.source"
                  :language="monacoLanguage(selectedSimilar.language_key)"
                  height="200px"
                  theme="vs"
                  :options="{ readOnly: true }"
                />
                <NEmpty v-else description="源码不可见" class="py-16px" />
              </NCollapseItem>
            </NCollapse>
          </template>
        </div>
      </template>
    </NSpin>
  </div>
</template>

<style scoped>
.performance-panel {
  margin-top: 4px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 12px;
}

.metric-label {
  color: var(--text-color-3);
  font-size: 12px;
}

.metric-value {
  margin: 4px 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.insufficient-tip {
  margin-top: 12px;
  color: var(--text-color-3);
  font-size: 13px;
}

.chart-block {
  margin-top: 12px;
}

.chart-title {
  color: var(--text-color-3);
  font-size: 12px;
}

.chart-box {
  width: 100%;
  min-width: 0;
  height: 160px;
}

.similar-section {
  margin-top: 16px;
}
</style>
