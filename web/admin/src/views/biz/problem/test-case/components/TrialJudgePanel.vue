<script setup lang="tsx">
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { ojProblemApi, ojProblemLanguageApi, ojSubmissionApi } from '@/api'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { monacoLanguageFromExtension } from '../../shared/monacoLanguage'
import { NTag } from 'naive-ui'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

const props = defineProps<{
  problemId: string
  /** When set, only these oj_problem_test_case ids are judged. */
  caseIds?: string[]
  /** Display hint, e.g. case_no for single-case trial. */
  caseLabel?: string | number | null
}>()

const statusColor: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
  AC: 'success',
  Accepted: 'success',
  WA: 'error',
  WrongAnswer: 'error',
  TLE: 'warning',
  TimeLimitExceeded: 'warning',
  MLE: 'warning',
  MemoryLimitExceeded: 'warning',
  RE: 'error',
  RuntimeError: 'error',
  CE: 'error',
  CompileError: 'error',
  OLE: 'warning',
  SE: 'error',
  SystemError: 'error',
  Pending: 'info',
  Judging: 'info',
  JUDGING: 'info',
  QUEUED: 'info',
  COMPLETED: 'info',
  FAILED: 'error',
  Compiling: 'info',
}

const languageOptions = ref<SelectOption[]>([])
const extByKey = ref<Record<string, string>>({})
const abortRef = ref<AbortController | null>(null)

const state = reactive({
  submitLoading: false,
  language_key: 'cpp17',
  source: '',
  wait_timeout_sec: 60,
  result: null as any,
})

const monacoLanguage = computed(() =>
  monacoLanguageFromExtension(extByKey.value[state.language_key]),
)

onMounted(() => {
  void loadLanguages()
})

async function loadLanguages() {
  try {
    const [optRes, pageRes] = await Promise.all([
      ojProblemLanguageApi.options(),
      ojProblemLanguageApi.page(props.problemId, { current: 1, size: 100 }),
    ])
    // 唯一来源：GET /language/options（worker 镜像显式启用）；优先展示题目已绑定语言
    const workerLangs = optRes.data ?? []
    extByKey.value = Object.fromEntries(workerLangs.map((item: any) => [item.key, item.extension]))

    const enabledProblemKeys = new Set(
      (pageRes.data?.records ?? [])
        .filter((row: any) => row.status !== 'DISABLED')
        .map((row: any) => String(row.language_key)),
    )
    const source = enabledProblemKeys.size
      ? workerLangs.filter((item: any) => enabledProblemKeys.has(item.key))
      : workerLangs
    const finalSource = source.length ? source : workerLangs

    languageOptions.value = finalSource.map((item: any) => ({
      label: `${item.label} (${item.key})`,
      value: item.key,
    }))
    if (!finalSource.some((item: any) => item.key === state.language_key) && finalSource[0]) {
      state.language_key = finalSource[0].key
    }
  }
  catch {
    languageOptions.value = []
  }
}

const overallStatus = computed(() => String(state.result?.status ?? state.result?.result ?? ''))
const overallType = computed(() => statusColor[overallStatus.value] ?? 'default')
const scopeLabel = computed(() => {
  if (props.caseLabel != null && props.caseLabel !== '') {
    return `测例 #${props.caseLabel}`
  }
  if (props.caseIds?.length === 1) {
    return '单条测例'
  }
  if (props.caseIds?.length) {
    return `${props.caseIds.length} 条测例`
  }
  return '全部测例'
})

const caseRows = computed(() => {
  const cases = state.result?.cases
  return Array.isArray(cases) ? cases : []
})

function caseTagType(row: any) {
  const status = String(row?.status ?? row?.result ?? '')
  return statusColor[status] ?? 'default'
}

const caseColumns = computed<DataTableColumns<any>>(() => [
  {
    title: '#',
    key: 'case_no',
    width: 50,
    render: row => row.case_no ?? row.id ?? '-',
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row => (
      <NTag size="small" type={caseTagType(row)}>
        {String(row.status ?? row.result ?? '-')}
      </NTag>
    ),
  },
  {
    title: '分',
    key: 'score',
    width: 60,
    render: row => row.score ?? row.points ?? '-',
  },
  {
    title: '时间',
    key: 'time_ms',
    width: 80,
    render: row => `${row.time_ms ?? row.time ?? 0}ms`,
  },
  {
    title: '内存',
    key: 'memory_kb',
    width: 90,
    render: row => `${row.memory_kb ?? row.memory ?? 0}KB`,
  },
])

onBeforeUnmount(() => {
  abortRef.value?.abort()
})

async function submitTrial() {
  if (!props.problemId) {
    window.$message.error('缺少题目 ID')
    return
  }
  if (!state.source.trim()) {
    window.$message.warning('请输入源代码')
    return
  }
  abortRef.value?.abort()
  const controller = new AbortController()
  abortRef.value = controller
  state.submitLoading = true
  state.result = null
  try {
    const response = await ojProblemApi.trialJudge(props.problemId, {
      language_key: state.language_key,
      source: state.source,
      wait_timeout_sec: state.wait_timeout_sec,
      wait: false,
      ...(props.caseIds?.length ? { case_ids: props.caseIds } : {}),
    })
    const initial = response.data ?? {}
    state.result = initial
    const submissionId = initial.submission_id
    if (!submissionId) {
      window.$message.error('未返回 submission_id')
      return
    }
    window.$message.info('已入队，等待判题…')

    let finalSnap = null as any
    try {
      finalSnap = await ojSubmissionApi.watchSubmissionEvents(submissionId, {
        maxWaitSec: state.wait_timeout_sec,
        signal: controller.signal,
        onUpdate: (snap) => {
          state.result = snap
        },
      })
    }
    catch {
      finalSnap = await ojSubmissionApi.pollSubmissionUntilDone(submissionId, {
        maxWaitSec: state.wait_timeout_sec,
        signal: controller.signal,
        fetchDetail: async (id) => {
          const res = await ojSubmissionApi.detail({ id })
          return res.data ?? {}
        },
        onUpdate: (snap) => {
          state.result = snap
        },
      })
    }

    const status = String(finalSnap?.status ?? state.result?.status ?? '')
    if (status === 'COMPLETED') {
      window.$message.success('试判完成')
    }
    else if (status === 'FAILED') {
      window.$message.error(finalSnap?.error || '试判失败')
    }
    else {
      window.$message.warning('试判超时，请稍后在提交列表查看')
    }
  }
  catch (error: any) {
    if (error?.name === 'AbortError')
      return
    state.result = { error: error?.message ?? '试判失败', status: 'SE' }
  }
  finally {
    state.submitLoading = false
  }
}

function resetSource() {
  abortRef.value?.abort()
  state.source = ''
  state.result = null
}
</script>

<template>
  <div class="trial-judge-panel">
    <NGrid cols="1 l:24" responsive="screen" :x-gap="16" :y-gap="12">
      <NGi :span="14">
        <NCard size="small" :bordered="false" class="trial-code-card">
          <template #header>
            <NFlex align="center" justify="space-between">
              <span class="font-medium">源代码</span>
              <NFlex :size="8" align="center">
                <NSelect
                  v-model:value="state.language_key"
                  :options="languageOptions"
                  filterable
                  class="w-180px"
                  size="small"
                />
                <NInputNumber
                  v-model:value="state.wait_timeout_sec"
                  :min="5"
                  :max="300"
                  size="small"
                  class="w-110px"
                >
                  <template #suffix>
                    秒
                  </template>
                </NInputNumber>
                <NButton size="small" quaternary @click="resetSource">
                  清空
                </NButton>
                <NButton type="primary" size="small" :loading="state.submitLoading" @click="submitTrial">
                  运行试测
                </NButton>
              </NFlex>
            </NFlex>
          </template>
          <MonacoEditor
            v-model:value="state.source"
            :language="monacoLanguage"
            height="320px"
            theme="vs"
          />
        </NCard>
      </NGi>

      <NGi :span="10">
        <NCard size="small" :bordered="false" class="trial-result-card" title="试测结果">
          <NEmpty v-if="!state.result" description="提交代码后显示判题结果" class="py-40px" />
          <NSpace v-else vertical :size="12">
            <NAlert v-if="state.result.error" type="error" :title="String(state.result.error)" :bordered="false" />
            <NDescriptions v-else label-placement="left" :column="1" size="small" bordered>
              <NDescriptionsItem label="范围">
                {{ scopeLabel }}
              </NDescriptionsItem>
              <NDescriptionsItem label="状态">
                <NTag :type="overallType" size="small">
                  {{ overallStatus || '-' }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem label="得分">
                {{ state.result.score ?? 0 }}
              </NDescriptionsItem>
              <NDescriptionsItem label="耗时">
                {{ state.result.time_ms ?? 0 }} ms
              </NDescriptionsItem>
              <NDescriptionsItem label="内存">
                {{ state.result.memory_kb ?? 0 }} KB
              </NDescriptionsItem>
              <NDescriptionsItem v-if="state.result.submission_id" label="试测 ID">
                <span class="font-mono text-12px">{{ state.result.submission_id }}</span>
              </NDescriptionsItem>
            </NDescriptions>

            <template v-if="state.result.compile_output || state.result.compile_error">
              <div class="text-13px font-medium">
                编译信息
              </div>
              <NInput
                :value="state.result.compile_output || ''"
                type="textarea"
                readonly
                :rows="6"
                class="font-mono"
              />
            </template>

            <template v-if="caseRows.length">
              <div class="text-13px font-medium">
                测例明细
              </div>
              <NDataTable
                size="small"
                :bordered="false"
                :single-line="false"
                :columns="caseColumns"
                :data="caseRows"
                :pagination="false"
                :max-height="180"
              />
            </template>
          </NSpace>
        </NCard>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.trial-result-card {
  max-height: 400px;
  overflow: auto;
}
.trial-code-card :deep(.monaco-editor) {
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  overflow: hidden;
}
</style>
