<!--
  Author: Charlie

  OJ 题目新建/编辑页（仅题目信息，测例在独立页维护）。
-->
<script setup lang="ts">
import type { FormInst, FormRules, SelectOption } from 'naive-ui'
import { MdEditor } from '@/components/editor'
import { ojJudgeNodeApi, ojProblemApi, ojTagApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { wireFields } from '@/utils/wire'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface SampleItem {
  input: string
  output: string
  explanation: string
}

interface LanguageLimitItem {
  language: string | null
  time_limit_ms: number | null
  memory_limit_bytes: number | null
  stack_limit_bytes: number | null
  output_limit_bytes: number | null
}

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const listPath = '/oj/problem'

const difficultyOptions: SelectOption[] = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' },
]

const defaultLanguageLimit = (): LanguageLimitItem => ({
  language: 'cpp17',
  time_limit_ms: 1000,
  memory_limit_bytes: 268435456,
  stack_limit_bytes: null,
  output_limit_bytes: null,
})

const defaultFormData: Record<string, any> = {
  problem_key: '',
  title: '',
  statement_md: '',
  input_format: '',
  output_format: '',
  hint: '',
  samples: [] as SampleItem[],
  difficulty: 'EASY',
  judge_mode: 'STANDARD',
  language_limits: [defaultLanguageLimit()] as LanguageLimitItem[],
  status: 'DRAFT',
  source: '',
  extra: '{}',
  case_version: 1,
  tag_ids: [] as string[],
}

const state = reactive({
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: normalizeFormData(),
  clusterLanguages: [] as string[],
  tagOptions: [] as SelectOption[],
})

const pageTitle = computed(() => (state.dataId ? '编辑题目' : '新增题目'))

const languageOptions = computed<SelectOption[]>(() => {
  const selected = (Array.isArray(state.formModel.language_limits)
    ? state.formModel.language_limits
    : []
  )
    .map((item: LanguageLimitItem) => item?.language)
    .filter(Boolean) as string[]
  const merged = new Set<string>([...state.clusterLanguages, ...selected])
  return [...merged]
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b))
    .map((lang) => ({ label: lang, value: lang }))
})

const rules = computed<FormRules>(() => ({
  problem_key: [createRequiredRule('题号', 'input')],
  title: [createRequiredRule('标题', 'input')],
  statement_md: [createRequiredRule('题面', 'input')],
  difficulty: [createRequiredRule('难度', 'change')],
  status: [createRequiredRule('状态', 'change')],
  language_limits: [
    {
      validator: () => validateLanguageLimits(state.formModel.language_limits),
      message: '请完善语言限额（语言唯一，时限≥1，内存≥1MiB）',
      trigger: ['change', 'blur'],
    },
  ],
  samples: [
    {
      validator: () => validateSamples(state.formModel.samples),
      message: '请完善样例的输入与输出',
      trigger: ['change', 'blur'],
    },
  ],
  tag_ids: [
    {
      validator: () => {
        const tags = state.formModel.tag_ids
        return !Array.isArray(tags) || tags.length <= 2
      },
      message: '最多选择 2 个标签',
      trigger: ['change', 'blur'],
    },
  ],
  extra: [
    {
      validator: () => isValidJsonObject(state.formModel.extra),
      message: '请输入合法 JSON 对象',
      trigger: ['input', 'blur'],
    },
  ],
}))

function resolveQueryId() {
  const id = route.query.id
  return typeof id === 'string' && id ? id : null
}

async function initPage() {
  const id = resolveQueryId()
  state.dataId = id
  state.formModel = normalizeFormData()
  await Promise.all([loadClusterLanguages(), loadTagOptions()])
  if (id) {
    await fetchDetail(id)
  }
}

async function loadTagOptions() {
  try {
    const response = await ojTagApi.options()
    const list = Array.isArray(response.data) ? response.data : []
    state.tagOptions = list.map((item: any) => ({
      label: String(item.name || item.id),
      value: String(item.id),
    }))
  } catch {
    state.tagOptions = []
  }
}

async function loadClusterLanguages() {
  try {
    const response = await ojJudgeNodeApi.languages()
    const langs = response.data?.languages
    state.clusterLanguages = Array.isArray(langs)
      ? langs.map((item: unknown) => String(item).trim()).filter(Boolean)
      : []
  } catch {
    state.clusterLanguages = []
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const data = (await ojProblemApi.detail({ id })).data ?? {}
    state.formModel = normalizeFormData(data)
    mergeSelectedTagOptions(data.tags)
  } finally {
    state.loading = false
  }
}

function mergeSelectedTagOptions(tags: unknown) {
  if (!Array.isArray(tags)) {
    return
  }
  const existing = new Set(state.tagOptions.map((item) => String(item.value)))
  for (const tag of tags) {
    if (!tag || typeof tag !== 'object') {
      continue
    }
    const row = tag as Record<string, unknown>
    const id = String(row.id || '')
    if (!id || existing.has(id)) {
      continue
    }
    state.tagOptions.push({
      label: String(row.name || id),
      value: id,
    })
    existing.add(id)
  }
}

function emptySample(): SampleItem {
  return { input: '', output: '', explanation: '' }
}

function normalizeSamples(value: unknown): SampleItem[] {
  let list: unknown[] = []
  if (Array.isArray(value)) {
    list = value
  } else if (typeof value === 'string' && value.trim()) {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) {
        list = parsed
      }
    } catch {
      list = []
    }
  }
  return list.map((item) => {
    const row = item && typeof item === 'object' ? (item as Record<string, unknown>) : {}
    return {
      input: row.input == null ? '' : String(row.input),
      output: row.output == null ? '' : String(row.output),
      explanation: row.explanation == null ? '' : String(row.explanation),
    }
  })
}

function normalizeLanguageLimits(value: unknown): LanguageLimitItem[] {
  if (!Array.isArray(value) || !value.length) {
    return [defaultLanguageLimit()]
  }
  return value.map((item) => {
    const row = item && typeof item === 'object' ? (item as Record<string, any>) : {}
    return {
      language: row.language == null || row.language === '' ? null : String(row.language),
      time_limit_ms:
        row.time_limit_ms === null || row.time_limit_ms === undefined || row.time_limit_ms === ''
          ? null
          : Number(row.time_limit_ms),
      memory_limit_bytes:
        row.memory_limit_bytes === null ||
        row.memory_limit_bytes === undefined ||
        row.memory_limit_bytes === ''
          ? null
          : Number(row.memory_limit_bytes),
      stack_limit_bytes:
        row.stack_limit_bytes === null ||
        row.stack_limit_bytes === undefined ||
        row.stack_limit_bytes === ''
          ? null
          : Number(row.stack_limit_bytes),
      output_limit_bytes:
        row.output_limit_bytes === null ||
        row.output_limit_bytes === undefined ||
        row.output_limit_bytes === ''
          ? null
          : Number(row.output_limit_bytes),
    }
  })
}

function validateLanguageLimits(value: unknown): boolean {
  if (!Array.isArray(value) || !value.length) return false
  const seen = new Set<string>()
  for (const item of value as LanguageLimitItem[]) {
    const lang = String(item?.language || '')
      .trim()
      .toLowerCase()
    if (!lang || seen.has(lang)) return false
    seen.add(lang)
    if (typeof item.time_limit_ms !== 'number' || !Number.isFinite(item.time_limit_ms) || item.time_limit_ms < 1) {
      return false
    }
    if (
      typeof item.memory_limit_bytes !== 'number' ||
      !Number.isFinite(item.memory_limit_bytes) ||
      item.memory_limit_bytes < 1024 * 1024
    ) {
      return false
    }
  }
  return true
}

function normalizeFormData(data: Record<string, any> = {}): Record<string, any> {
  return {
    ...defaultFormData,
    ...data,
    ...wireFields(
      data,
      {
        case_version: 'int',
      },
      defaultFormData,
    ),
    difficulty: data.difficulty || defaultFormData.difficulty,
    status: data.status || defaultFormData.status,
    language_limits: Object.keys(data).length
      ? normalizeLanguageLimits(data.language_limits)
      : [defaultLanguageLimit()],
    samples: Object.keys(data).length ? normalizeSamples(data.samples ?? []) : [],
    extra: stringifyJsonObject(data.extra),
    tag_ids: Array.isArray(data.tag_ids)
      ? data.tag_ids.map((item: unknown) => String(item)).slice(0, 2)
      : [],
  }
}

function onTagIdsUpdate(value: Array<string | number> | null) {
  const next = Array.isArray(value) ? value.map(String) : []
  state.formModel.tag_ids = next.slice(0, 2)
  if (next.length > 2) {
    window.$message.warning('最多选择 2 个标签')
  }
}

function addLanguageLimit() {
  if (!Array.isArray(state.formModel.language_limits)) {
    state.formModel.language_limits = []
  }
  state.formModel.language_limits.push({
    language: null,
    time_limit_ms: 1000,
    memory_limit_bytes: 268435456,
    stack_limit_bytes: null,
    output_limit_bytes: null,
  })
}

function removeLanguageLimit(index: number) {
  if (!Array.isArray(state.formModel.language_limits)) return
  if (state.formModel.language_limits.length <= 1) {
    window.$message.warning('至少保留一种语言限额')
    return
  }
  state.formModel.language_limits.splice(index, 1)
}

function normalizeSubmitData(data: Record<string, any>): Record<string, any> {
  const limits = (Array.isArray(data.language_limits) ? data.language_limits : []).map(
    (item: LanguageLimitItem) => ({
      language: String(item?.language ?? '').trim(),
      time_limit_ms: item.time_limit_ms,
      memory_limit_bytes: item.memory_limit_bytes,
      stack_limit_bytes: item.stack_limit_bytes,
      output_limit_bytes: item.output_limit_bytes,
    }),
  )
  return {
    problem_key: data.problem_key,
    title: data.title,
    statement_md: data.statement_md,
    input_format: data.input_format || null,
    output_format: data.output_format || null,
    hint: data.hint || null,
    samples: (Array.isArray(data.samples) ? data.samples : []).map((item: SampleItem) => {
      const row: Record<string, string> = {
        input: String(item?.input ?? ''),
        output: String(item?.output ?? ''),
      }
      const explanation = String(item?.explanation ?? '').trim()
      if (explanation) {
        row.explanation = explanation
      }
      return row
    }),
    difficulty: data.difficulty,
    judge_mode: data.judge_mode || 'STANDARD',
    language_limits: limits,
    status: data.status,
    source: data.source || null,
    extra: parseJsonObject(data.extra),
    tag_ids: Array.isArray(data.tag_ids)
      ? data.tag_ids.map((item: unknown) => String(item)).filter(Boolean).slice(0, 2)
      : [],
  }
}

function parseJsonObject(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) return {}
  const parsed = JSON.parse(text)
  if (Array.isArray(parsed) || typeof parsed !== 'object' || parsed === null) {
    throw new Error('JSON value must be an object')
  }
  return parsed
}

function isValidJsonObject(value: unknown) {
  try {
    parseJsonObject(value)
    return true
  } catch {
    return false
  }
}

function validateSamples(value: unknown) {
  if (!Array.isArray(value)) {
    return false
  }
  return value.every((item) => {
    if (!item || typeof item !== 'object') {
      return false
    }
    const row = item as SampleItem
    return String(row.input ?? '').length > 0 && String(row.output ?? '').length > 0
  })
}

function stringifyJsonObject(value: unknown) {
  if (value === undefined || value === null || value === '') return '{}'
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

function addSample() {
  if (!Array.isArray(state.formModel.samples)) {
    state.formModel.samples = []
  }
  state.formModel.samples.push(emptySample())
}

function removeSample(index: number) {
  if (!Array.isArray(state.formModel.samples)) {
    return
  }
  state.formModel.samples.splice(index, 1)
}

function moveSample(index: number, delta: number) {
  const list = state.formModel.samples
  if (!Array.isArray(list)) {
    return
  }
  const target = index + delta
  if (target < 0 || target >= list.length) {
    return
  }
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
}

function goBack() {
  router.push(listPath)
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = normalizeSubmitData(state.formModel)
    if (state.dataId) {
      await ojProblemApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
      goBack()
    } else {
      await ojProblemApi.create(payload)
      window.$message.success('创建成功')
      goBack()
    }
  } finally {
    state.submitLoading = false
  }
}

onMounted(initPage)
watch(
  () => route.query.id,
  () => {
    void initPage()
  },
)
</script>

<template>
  <div class="h-full min-h-0">
    <NCard
      class="h-full min-h-0 overflow-auto"
      :title="pageTitle"
      :bordered="false"
    >
      <template #header-extra>
        <NSpace>
          <NButton @click="goBack">
            返回
          </NButton>
          <NButton
            type="primary"
            :loading="state.submitLoading"
            @click="submitForm"
          >
            保存
          </NButton>
        </NSpace>
      </template>
      <NSpin :show="state.loading">
        <NForm
          ref="formRef"
          class="problem-page-form"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="96"
          :disabled="state.loading || state.submitLoading"
        >
          <section class="form-section">
            <div class="form-section-title">
              基本信息
            </div>
            <NGrid
              cols="1 s:2 m:4"
              responsive="screen"
              :x-gap="16"
              :y-gap="0"
            >
              <NGi>
                <NFormItem
                  label="题号"
                  path="problem_key"
                >
                  <NInput
                    v-model:value="state.formModel.problem_key"
                    placeholder="如 P1001"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="难度"
                  path="difficulty"
                >
                  <NSelect
                    v-model:value="state.formModel.difficulty"
                    :options="difficultyOptions"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="状态"
                  path="status"
                >
                  <DictSelect
                    v-model="state.formModel.status"
                    dict-code="OJ_PROBLEM_STATUS"
                    :clearable="false"
                  />
                </NFormItem>
              </NGi>
              <NGi v-if="state.dataId">
                <NFormItem label="测例版本">
                  <NInputNumber
                    :value="state.formModel.case_version"
                    class="w-full"
                    disabled
                  />
                </NFormItem>
              </NGi>
              <NGi span="1 s:2 m:2">
                <NFormItem
                  label="标题"
                  path="title"
                >
                  <NInput
                    v-model:value="state.formModel.title"
                    placeholder="题目标题"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="来源"
                  path="source"
                >
                  <NInput
                    v-model:value="state.formModel.source"
                    placeholder="可选"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="标签"
                  path="tag_ids"
                >
                  <NSelect
                    :value="state.formModel.tag_ids"
                    :options="state.tagOptions"
                    multiple
                    filterable
                    clearable
                    max-tag-count="responsive"
                    placeholder="最多 2 个"
                    @update:value="onTagIdsUpdate"
                  />
                </NFormItem>
              </NGi>
            </NGrid>
          </section>

          <section class="form-section">
            <div class="form-section-head">
              <div class="form-section-title form-section-title--inline">
                语言限额
              </div>
              <NButton
                size="small"
                secondary
                @click="addLanguageLimit"
              >
                添加语言
              </NButton>
            </div>
            <NFormItem
              path="language_limits"
              :show-label="false"
            >
              <div class="samples-editor">
                <div
                  v-for="(item, index) in state.formModel.language_limits"
                  :key="index"
                  class="sample-editor-item"
                >
                  <div class="sample-editor-header">
                    <span class="sample-editor-title">语言 {{ index + 1 }}</span>
                    <NButton
                      size="tiny"
                      quaternary
                      type="error"
                      @click="removeLanguageLimit(index)"
                    >
                      删除
                    </NButton>
                  </div>
                  <NGrid
                    cols="1 s:2 m:5"
                    responsive="screen"
                    :x-gap="12"
                    :y-gap="0"
                  >
                    <NGi>
                      <NFormItem
                        label="语言"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NSelect
                          v-model:value="item.language"
                          :options="languageOptions"
                          filterable
                          tag
                          clearable
                          placeholder="语言 key"
                        />
                      </NFormItem>
                    </NGi>
                    <NGi>
                      <NFormItem
                        label="时限(ms)"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInputNumber
                          v-model:value="item.time_limit_ms"
                          class="w-full"
                          :min="1"
                        />
                      </NFormItem>
                    </NGi>
                    <NGi>
                      <NFormItem
                        label="内存(B)"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInputNumber
                          v-model:value="item.memory_limit_bytes"
                          class="w-full"
                          :min="1048576"
                        />
                      </NFormItem>
                    </NGi>
                    <NGi>
                      <NFormItem
                        label="栈限额"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInputNumber
                          v-model:value="item.stack_limit_bytes"
                          class="w-full"
                          clearable
                          placeholder="默认"
                        />
                      </NFormItem>
                    </NGi>
                    <NGi>
                      <NFormItem
                        label="输出限额"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInputNumber
                          v-model:value="item.output_limit_bytes"
                          class="w-full"
                          clearable
                          placeholder="默认"
                        />
                      </NFormItem>
                    </NGi>
                  </NGrid>
                </div>
              </div>
            </NFormItem>
          </section>

          <section class="form-section">
            <div class="form-section-title">
              题面
            </div>
            <NFormItem
              class="form-statement-item"
              path="statement_md"
              :show-label="false"
            >
              <MdEditor
                v-model:value="state.formModel.statement_md"
                height="420px"
                placeholder="Markdown 题面"
              />
            </NFormItem>
          </section>

          <section class="form-section">
            <div class="form-section-title">
              输入 / 输出说明
            </div>
            <NGrid
              cols="1 m:2"
              responsive="screen"
              :x-gap="16"
              :y-gap="0"
            >
              <NGi>
                <NFormItem
                  label="输入格式"
                  path="input_format"
                  label-placement="top"
                >
                  <NInput
                    v-model:value="state.formModel.input_format"
                    type="textarea"
                    :autosize="{ minRows: 3, maxRows: 8 }"
                    placeholder="描述输入格式"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem
                  label="输出格式"
                  path="output_format"
                  label-placement="top"
                >
                  <NInput
                    v-model:value="state.formModel.output_format"
                    type="textarea"
                    :autosize="{ minRows: 3, maxRows: 8 }"
                    placeholder="描述输出格式"
                  />
                </NFormItem>
              </NGi>
              <NGi span="1 m:2">
                <NFormItem
                  label="提示"
                  path="hint"
                  label-placement="top"
                >
                  <NInput
                    v-model:value="state.formModel.hint"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 5 }"
                    placeholder="可选提示"
                  />
                </NFormItem>
              </NGi>
            </NGrid>
          </section>

          <section class="form-section">
            <div class="form-section-head">
              <div class="form-section-title form-section-title--inline">
                样例
              </div>
              <NButton
                size="small"
                secondary
                @click="addSample"
              >
                添加样例
              </NButton>
            </div>
            <NFormItem
              path="samples"
              :show-label="false"
            >
              <div class="samples-editor">
                <div
                  v-if="!state.formModel.samples?.length"
                  class="samples-empty"
                >
                  暂无样例。题面展示用的公开样例可在此维护；正式测例请保存后到「测例」页配置。
                </div>
                <div
                  v-for="(sample, index) in state.formModel.samples"
                  :key="index"
                  class="sample-editor-item"
                >
                  <div class="sample-editor-header">
                    <span class="sample-editor-title">样例 {{ index + 1 }}</span>
                    <NSpace :size="4">
                      <NButton
                        size="tiny"
                        quaternary
                        :disabled="index === 0"
                        @click="moveSample(index, -1)"
                      >
                        上移
                      </NButton>
                      <NButton
                        size="tiny"
                        quaternary
                        :disabled="index >= state.formModel.samples.length - 1"
                        @click="moveSample(index, 1)"
                      >
                        下移
                      </NButton>
                      <NButton
                        size="tiny"
                        quaternary
                        type="error"
                        @click="removeSample(index)"
                      >
                        删除
                      </NButton>
                    </NSpace>
                  </div>
                  <NGrid
                    cols="1 m:2"
                    responsive="screen"
                    :x-gap="12"
                    :y-gap="0"
                  >
                    <NGi>
                      <NFormItem
                        label="输入"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInput
                          v-model:value="sample.input"
                          type="textarea"
                          :autosize="{ minRows: 4, maxRows: 10 }"
                          placeholder="样例输入"
                        />
                      </NFormItem>
                    </NGi>
                    <NGi>
                      <NFormItem
                        label="输出"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInput
                          v-model:value="sample.output"
                          type="textarea"
                          :autosize="{ minRows: 4, maxRows: 10 }"
                          placeholder="样例输出"
                        />
                      </NFormItem>
                    </NGi>
                    <NGi span="1 m:2">
                      <NFormItem
                        label="说明"
                        label-placement="top"
                        :show-feedback="false"
                      >
                        <NInput
                          v-model:value="sample.explanation"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 4 }"
                          placeholder="可选说明"
                        />
                      </NFormItem>
                    </NGi>
                  </NGrid>
                </div>
              </div>
            </NFormItem>
          </section>

          <section class="form-section form-section--last">
            <div class="form-section-title">
              扩展（JSON）
            </div>
            <NFormItem
              path="extra"
              :show-label="false"
            >
              <NInput
                v-model:value="state.formModel.extra"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 6 }"
                placeholder="{}"
              />
            </NFormItem>
          </section>

          <NAlert
            v-if="!state.dataId"
            type="info"
            class="form-tip"
            :bordered="false"
          >
            新建题目请先保存，再从题目列表操作列进入「测例 / 参考答案」页维护。
          </NAlert>
        </NForm>
      </NSpin>
    </NCard>
  </div>
</template>

<style scoped>
.problem-page-form {
  width: 100%;
}

.form-section {
  margin-bottom: 22px;
}

.form-section--last {
  margin-bottom: 12px;
}

.form-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--n-divider-color, rgba(0, 0, 0, 0.08));
}

.form-section-title {
  margin: 0 0 12px;
  padding-bottom: 8px;
  color: var(--n-text-color-2, #666);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.02em;
  border-bottom: 1px solid var(--n-divider-color, rgba(0, 0, 0, 0.08));
}

.form-section-title--inline {
  margin: 0;
  padding: 0;
  border: none;
}

.form-statement-item {
  margin-bottom: 0;
}

.form-statement-item :deep(.n-form-item-blank) {
  width: 100%;
}

.form-statement-item :deep(.editor-wrapper) {
  width: 100%;
}

.samples-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.samples-empty {
  padding: 10px 0 2px;
  color: var(--n-text-color-3, #999);
  font-size: 13px;
  line-height: 1.5;
}

.sample-editor-item {
  padding: 10px 12px 4px;
  background: var(--n-color-embedded, rgba(0, 0, 0, 0.03));
  border-radius: 8px;
}

.sample-editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.sample-editor-title {
  color: var(--n-text-color-1, #333);
  font-size: 13px;
  font-weight: 600;
}

.form-tip {
  margin-top: 4px;
}

.w-full {
  width: 100%;
}
</style>
