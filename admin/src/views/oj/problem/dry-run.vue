<!--
  Author: Charlie

  OJ 题目测例试跑（独立页，自测例列表进入）。
-->
<script setup lang="tsx">
import type { FormInst, FormRules, PaginationProps, SelectOption } from 'naive-ui'
import type { ProDataTableColumns } from 'pro-naive-ui'
import { MonacoEditor } from '@/components/editor'
import { ojProblemApi, ojProblemSolutionApi } from '@/api'
import { createRequiredRule, displayValue, hasPermission, mapOjLanguageToMonaco } from '@/utils'
import { Icon } from '@iconify/vue/offline'
import { NButton, NFlex, NIcon, NTag } from 'naive-ui'
import { ProCard, ProDataTable } from 'pro-naive-ui'
import { useElementSize } from '@vueuse/core'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const editorAreaRef = ref<HTMLElement | null>(null)
const { height: editorAreaHeight } = useElementSize(editorAreaRef)

const editorHeight = computed(() => {
  const height = Math.floor(editorAreaHeight.value || 0)
  return height > 0 ? height : 360
})

const limitModeOptions: SelectOption[] = [
  { label: '题目限额（发布验收）', value: 'PROBLEM' },
  { label: '宽松限额（摸底耗时）', value: 'RELAXED' },
]

const state = reactive({
  problem: {} as any,
  loading: false,
  submitLoading: false,
  applyLoading: false,
  solutions: [] as any[],
  formModel: {
    language: '',
    source: '',
    limit_mode: 'PROBLEM',
    stop_on_first_error: false,
  },
  result: null as any,
  resultPage: 1,
  resultPageSize: 20,
})

const problemId = computed(() => {
  const id = route.query.id
  return typeof id === 'string' ? id : ''
})

const caseKey = computed(() => {
  const key = route.query.case_key
  return typeof key === 'string' && key ? key : null
})

const tableTitle = computed(() => '测例结果')

const sourceCardTitle = computed(() => {
  const key = state.problem.problem_key
  const prefix = key ? `源码 · ${displayValue(key)}` : '源码'
  return caseKey.value ? `${prefix} · ${caseKey.value}` : `${prefix} · 全部测例`
})

const canUpdate = computed(() => hasPermission('oj:problem:update'))

const languageOptions = computed<SelectOption[]>(() => {
  const langs = Array.isArray(state.problem.allowed_languages)
    ? state.problem.allowed_languages
    : []
  return langs.map((lang: string) => ({ label: lang, value: lang }))
})

const monacoLanguage = computed(() => mapOjLanguageToMonaco(state.formModel.language))

const caseResultRows = computed(() => state.result?.case_results ?? [])

const resultPagination = computed<PaginationProps>(() => ({
  page: state.resultPage,
  pageSize: state.resultPageSize,
  itemCount: caseResultRows.value.length,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  prefix: ({ itemCount }) => `${itemCount} 条`,
  onUpdatePage: (value) => {
    state.resultPage = value
  },
  onUpdatePageSize: (value) => {
    state.resultPageSize = value
    state.resultPage = 1
  },
}))

const paginatedCaseResults = computed(() => {
  const start = (state.resultPage - 1) * state.resultPageSize
  return caseResultRows.value.slice(start, start + state.resultPageSize)
})

const rules = computed<FormRules>(() => ({
  language: [createRequiredRule('语言', 'change')],
  source: [createRequiredRule('源码', 'input')],
  limit_mode: [createRequiredRule('限额模式', 'change')],
}))

const caseResultColumns = computed<ProDataTableColumns<any>>(() => [
  { title: '测例', path: 'case_key', width: 100 },
  {
    title: '结果',
    path: 'status',
    width: 80,
    render: (row) => (
      <NTag
        size="small"
        bordered={false}
        type={row.status === 'AC' ? 'success' : 'warning'}
      >
        {displayValue(row.status)}
      </NTag>
    ),
  },
  {
    title: '耗时(ms)',
    path: 'time_ms',
    width: 90,
    align: 'right',
    render: (row) => displayValue(row.time_ms),
  },
  {
    title: '内存',
    path: 'memory_bytes',
    width: 110,
    align: 'right',
    render: (row) => displayValue(row.memory_bytes),
  },
  {
    title: '说明',
    path: 'message',
    ellipsis: { tooltip: true },
    render: (row) => displayValue(row.message),
  },
])

watch(
  () => state.result,
  () => {
    state.resultPage = 1
  },
)

async function fetchProblem() {
  if (!problemId.value) return
  const response = await ojProblemApi.detail({ id: problemId.value })
  state.problem = response.data ?? {}
}

async function loadSolutions() {
  if (!problemId.value) return
  state.loading = true
  try {
    const response = await ojProblemSolutionApi.page({
      problem_id: problemId.value,
      status: 'ENABLED',
      current: 1,
      size: 100,
    })
    const data = response.data ?? {}
    state.solutions = data.records ?? []
    if (!state.formModel.language) {
      const def = state.solutions.find((s) => s.is_default) || state.solutions[0]
      if (def?.language) {
        state.formModel.language = def.language
      }
    }
    fillSourceFromStored()
  } finally {
    state.loading = false
  }
}

function fillSourceFromStored() {
  const match = state.solutions.find(
    (s) => s.language === state.formModel.language && s.status === 'ENABLED',
  )
  if (match?.source) {
    state.formModel.source = match.source
  }
}

function onLanguageChange() {
  fillSourceFromStored()
}

function resetForm() {
  state.result = null
  state.resultPage = 1
  const defaultLang = languageOptions.value[0]?.value
  state.formModel = {
    language: defaultLang ? String(defaultLang) : '',
    source: '',
    limit_mode: 'PROBLEM',
    stop_on_first_error: false,
  }
}

async function initPage() {
  if (!problemId.value) {
    router.replace('/oj/problem')
    return
  }
  resetForm()
  state.problem = {}
  await fetchProblem()
  const defaultLang = languageOptions.value[0]?.value
  if (defaultLang) {
    state.formModel.language = String(defaultLang)
  }
  await loadSolutions()
}

async function submitDryRun() {
  await formRef.value?.validate()
  state.submitLoading = true
  state.result = null
  try {
    const stored = state.solutions.find(
      (s) => s.language === state.formModel.language && s.status === 'ENABLED',
    )
    const payload: Record<string, any> = {
      problem_id: problemId.value,
      language: state.formModel.language,
      limit_mode: state.formModel.limit_mode,
      stop_on_first_error: state.formModel.stop_on_first_error,
    }
    if (!stored || state.formModel.source !== stored.source) {
      payload.source = state.formModel.source
    }
    if (caseKey.value) {
      payload.case_key = caseKey.value
    }
    const response = await ojProblemApi.dryRun(payload)
    state.result = response.data ?? null
    window.$message.success(`试跑完成：${displayValue(state.result?.overall_status)}`)
  } finally {
    state.submitLoading = false
  }
}

async function applySuggestedLimits() {
  if (!state.result?.suggested_time_ms || !state.result?.suggested_memory_bytes) {
    return
  }
  state.applyLoading = true
  try {
    await ojProblemApi.applyLimits({
      problem_id: problemId.value,
      time_limit_ms: state.result.suggested_time_ms,
      memory_limit_bytes: state.result.suggested_memory_bytes,
    })
    window.$message.success('已写回题目限额')
    await fetchProblem()
  } finally {
    state.applyLoading = false
  }
}

function goBack() {
  if (!problemId.value) {
    router.push('/oj/problem')
    return
  }
  router.push({ path: '/oj/problem/cases', query: { id: problemId.value } })
}

function goDryRuns() {
  if (!problemId.value) return
  router.push({ path: '/oj/problem/dry-runs', query: { id: problemId.value } })
}

onMounted(() => {
  void initPage()
})

watch([problemId, caseKey], () => {
  void initPage()
})
</script>

<template>
  <NFlex
    class="h-full min-h-0 overflow-hidden"
    vertical
  >
    <NForm
      ref="formRef"
      class="h-full min-h-0 flex flex-1 flex-col overflow-hidden"
      :model="state.formModel"
      :rules="rules"
      label-placement="left"
      label-width="108"
      :disabled="state.loading || state.submitLoading"
    >
      <ProCard
        class="shrink-0"
        :show-collapse="false"
      >
        <NSpin :show="state.loading">
          <NGrid
            :cols="3"
            :x-gap="24"
            :y-gap="4"
          >
            <NGi>
              <NFormItem
                label="语言"
                path="language"
              >
                <NSelect
                  v-model:value="state.formModel.language"
                  :options="languageOptions"
                  filterable
                  @update:value="onLanguageChange"
                />
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem
                label="限额模式"
                path="limit_mode"
              >
                <NSelect
                  v-model:value="state.formModel.limit_mode"
                  :options="limitModeOptions"
                />
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem
                label="遇错即停"
                path="stop_on_first_error"
              >
                <NSwitch v-model:value="state.formModel.stop_on_first_error" />
              </NFormItem>
            </NGi>
          </NGrid>
        </NSpin>
      </ProCard>

      <NFlex
        class="dry-run-main min-h-0 flex-1 overflow-hidden"
        :size="12"
        :wrap="false"
      >
        <ProCard
          class="dry-run-source min-h-0 min-w-0 flex-1"
          :title="sourceCardTitle"
          :show-collapse="false"
        >
          <template #header-extra>
            <NFlex>
              <NButton
                v-if="canUpdate && state.result?.suggested_time_ms"
                text
                title="写回建议限额"
                aria-label="写回建议限额"
                :loading="state.applyLoading"
                @click="applySuggestedLimits"
              >
                <template #icon>
                  <NIcon>
                    <Icon icon="icon-park-outline:edit" />
                  </NIcon>
                </template>
              </NButton>
              <NButton
                v-if="canUpdate"
                type="warning"
                text
                title="执行试跑"
                aria-label="执行试跑"
                :loading="state.submitLoading"
                @click="submitDryRun"
              >
                <template #icon>
                  <NIcon>
                    <Icon icon="icon-park-outline:play" />
                  </NIcon>
                </template>
              </NButton>
            </NFlex>
          </template>
          <div
            ref="editorAreaRef"
            class="dry-run-source__editor-area"
          >
            <NFormItem
              class="dry-run-source__editor"
              path="source"
              :show-label="false"
              :show-feedback="false"
              :label-width="0"
            >
              <MonacoEditor
                v-model:value="state.formModel.source"
                :language="monacoLanguage"
                :height="editorHeight"
              />
            </NFormItem>
          </div>
        </ProCard>

        <NFlex
          class="dry-run-result min-h-0 min-w-0 flex-1"
          vertical
          :size="12"
        >
          <ProCard
            v-if="state.result"
            class="shrink-0"
            :show-collapse="false"
          >
            <NFlex
              vertical
              :size="4"
            >
              <NText>
                整单结果：
                <NTag
                  size="small"
                  :type="state.result.overall_status === 'AC' ? 'success' : 'warning'"
                  :bordered="false"
                >
                  {{ displayValue(state.result.overall_status) }}
                </NTag>
                · 限额模式 {{ displayValue(state.result.limit_mode) }}
                · 源码来源 {{ displayValue(state.result.source_from) }}
              </NText>
              <NText depth="3">
                峰值 {{ displayValue(state.result.max_time_ms) }} ms /
                {{ displayValue(state.result.max_memory_bytes) }} bytes
                · 建议 {{ displayValue(state.result.suggested_time_ms) }} ms /
                {{ displayValue(state.result.suggested_memory_bytes) }} bytes
                · 本次限额 {{ displayValue(state.result.applied_time_ms) }} ms /
                {{ displayValue(state.result.applied_memory_bytes) }} bytes
              </NText>
              <NText
                v-if="state.result.error_message"
                type="error"
              >
                {{ state.result.error_message }}
              </NText>
            </NFlex>
          </ProCard>

          <ProDataTable
            class="min-h-0 flex-1"
            flex-height
            row-key="case_key"
            :scroll-x="640"
            :title="tableTitle"
            :columns="caseResultColumns"
            :data="paginatedCaseResults"
            :loading="state.submitLoading"
            :pagination="caseResultRows.length > 0 ? resultPagination : false"
          >
            <template #toolbar>
              <NFlex>
                <NButton
                  text
                  title="返回"
                  aria-label="返回"
                  @click="goBack"
                >
                  <template #icon>
                    <NIcon>
                      <Icon icon="icon-park-outline:back" />
                    </NIcon>
                  </template>
                </NButton>
                <NButton
                  text
                  title="试跑历史"
                  aria-label="试跑历史"
                  @click="goDryRuns"
                >
                  <template #icon>
                    <NIcon>
                      <Icon icon="icon-park-outline:history" />
                    </NIcon>
                  </template>
                </NButton>
              </NFlex>
            </template>
          </ProDataTable>
        </NFlex>
      </NFlex>
    </NForm>
  </NFlex>
</template>

<style scoped>
.dry-run-main {
  align-items: stretch;
}

.dry-run-result {
  min-height: 0;
  overflow: hidden;
}

.dry-run-source {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.dry-run-source :deep(.n-card) {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
}

.dry-run-source :deep(.n-card__content) {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
  overflow: hidden;
  padding-bottom: 12px;
}

.dry-run-source :deep([class*='pro-collapse-transition']) {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  height: 100%;
}

.dry-run-source__editor-area {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  width: 100%;
}

.dry-run-source__editor {
  flex: 1;
  width: 100%;
  min-width: 0;
  min-height: 0;
  margin-bottom: 0;
}

.dry-run-source__editor :deep(.n-form-item) {
  display: flex;
  flex: 1;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  min-height: 0;
}

.dry-run-source__editor :deep(.n-form-item-blank) {
  flex: 1;
  width: 100%;
  min-width: 0;
  min-height: 0;
}

.dry-run-source__editor :deep(.monaco-editor) {
  width: 100% !important;
}
</style>
