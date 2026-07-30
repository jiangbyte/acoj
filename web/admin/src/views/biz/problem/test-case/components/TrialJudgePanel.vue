<script setup lang="tsx">
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { ojProblemApi, ojProblemLanguageApi } from '@/api'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { NTag } from 'naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'

const props = defineProps<{
  problemId: string
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
  Compiling: 'info',
}

const extensionToMonaco: Record<string, string> = {
  '.cpp': 'cpp',
  '.c': 'c',
  '.py': 'python',
  '.java': 'java',
  '.go': 'go',
  '.js': 'javascript',
  '.rs': 'rust',
}

const languageOptions = ref<SelectOption[]>([])
const extByKey = ref<Record<string, string>>({})

const state = reactive({
  submitLoading: false,
  language_key: 'cpp17',
  source: '',
  wait_timeout_sec: 60,
  result: null as any,
})

const monacoLanguage = computed(() => {
  const ext = extByKey.value[state.language_key] || '.cpp'
  return extensionToMonaco[ext] || 'cpp'
})

onMounted(() => {
  void loadLanguages()
})

async function loadLanguages() {
  try {
    const [optRes, pageRes] = await Promise.all([
      ojProblemLanguageApi.options(),
      ojProblemLanguageApi.page(props.problemId, { current: 1, size: 100 }),
    ])
    const all = optRes.data ?? []
    extByKey.value = Object.fromEntries(all.map((item: any) => [item.key, item.extension]))

    const problemKeys = new Set(
      (pageRes.data?.records ?? []).map((row: any) => String(row.language_key)),
    )
    const preferred = problemKeys.size
      ? all.filter((item: any) => problemKeys.has(item.key))
      : all.filter((item: any) => ['cpp17', 'python3', 'c11', 'java17', 'go'].includes(item.key))

    const source = preferred.length ? preferred : all
    languageOptions.value = source.map((item: any) => ({
      label: `${item.label} (${item.key})`,
      value: item.key,
    }))
    if (!source.some((item: any) => item.key === state.language_key) && source[0]) {
      state.language_key = source[0].key
    }
  }
  catch {
    languageOptions.value = [
      { label: 'C++17 (cpp17)', value: 'cpp17' },
      { label: 'Python 3 (python3)', value: 'python3' },
      { label: 'C11 (c11)', value: 'c11' },
    ]
  }
}

const overallStatus = computed(() => String(state.result?.status ?? state.result?.result ?? ''))
const overallType = computed(() => statusColor[overallStatus.value] ?? 'default')

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

async function submitTrial() {
  if (!props.problemId) {
    window.$message.error('缺少题目 ID')
    return
  }
  if (!state.source.trim()) {
    window.$message.warning('请输入源代码')
    return
  }
  state.submitLoading = true
  state.result = null
  try {
    const response = await ojProblemApi.trialJudge(props.problemId, {
      language_key: state.language_key,
      source: state.source,
      wait_timeout_sec: state.wait_timeout_sec,
    })
    state.result = response.data ?? {}
    window.$message.success('试判完成')
  }
  catch (error: any) {
    state.result = { error: error?.message ?? '试判失败', status: 'SE' }
  }
  finally {
    state.submitLoading = false
  }
}

function resetSource() {
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
            height="520px"
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
                :max-height="280"
              />
            </template>
          </NSpace>
        </NCard>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.trial-judge-panel {
  min-height: 560px;
}
.trial-code-card :deep(.monaco-editor) {
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  overflow: hidden;
}
</style>
