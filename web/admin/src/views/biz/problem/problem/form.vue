<script setup lang="ts">
import type { FormInst, FormRules, SelectOption } from 'naive-ui'
import { ojProblemApi, ojProblemGroupApi, ojProblemTestCaseApi, ojProblemTypeApi } from '@/api'
import MdEditor from '@/components/editor/MdEditor.vue'
import { createRequiredRule, formatDateTime } from '@/utils'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TestdataPanel from '../data/index.vue'
import LanguagePanel from '../language/index.vue'
import SolutionPanel from '../solution/index.vue'
import StaffPanel from '../staff/index.vue'
import TestCasePanel from '../test-case/index.vue'
import TrialJudgePanel from '../test-case/components/TrialJudgePanel.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const typeOptions = ref<SelectOption[]>([])
const groupOptions = ref<SelectOption[]>([])
/** 测试数据：配置 | 导入 | 测例 | 试测 */
const testdataSubTab = ref('config')

const submissionSourceVisibilityOptions = [
  { label: '跟随全局', value: 'FOLLOW' },
  { label: '始终可见', value: 'ALWAYS' },
  { label: 'AC 后可见', value: 'SOLVED' },
  { label: '仅自己可见', value: 'ONLY_OWN' },
]

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '就绪', value: 'ready' },
  { label: '已发布', value: 'published' },
]

const defaultFormData: Record<string, any> = {
  code: '',
  name: '',
  description: '',
  summary: '',
  group_id: null,
  time_limit_ms: 1000,
  memory_limit_kb: 262144,
  points: 100,
  partial: false,
  short_circuit: false,
  status: 'draft',
  published_at: null,
  submission_source_visibility: 'FOLLOW',
  type_ids: [],
  extra: '{}',
}

const state = reactive({
  loading: false,
  submitLoading: false,
  statusLoading: false,
  dataId: null as string | null,
  activeTab: 'basic',
  testCaseCount: 0,
  formModel: normalizeFormData(),
})

const pageTitle = computed(() => (state.dataId ? '编辑题目' : '新增题目'))
const isCreate = computed(() => !state.dataId)

const rules = computed<FormRules>(() => ({
  code: [createRequiredRule('题目编码', 'input')],
  name: [createRequiredRule('题目标题', 'input')],
  description: [createRequiredRule('题面正文', 'input')],
  submission_source_visibility: [createRequiredRule('提交源码可见性', 'select')],
}))

onMounted(async () => {
  await Promise.all([loadTypeOptions(), loadGroupOptions()])
  const id = route.query.id ? String(route.query.id) : null
  state.dataId = id
  const tab = route.query.tab ? String(route.query.tab) : 'basic'
  state.activeTab = id ? tab : 'basic'
  if (id) {
    await fetchDetail(id)
    await refreshTestCaseCount()
  }
})

watch(
  () => state.activeTab,
  (tab) => {
    if (!state.dataId) {
      return
    }
    router.replace({
      path: '/biz/problem/problem/edit',
      query: { id: state.dataId, tab },
    })
  },
)

async function loadTypeOptions() {
  const response = await ojProblemTypeApi.list()
  typeOptions.value = (response.data ?? []).map((item: any) => ({
    label: item.name,
    value: item.id,
  }))
}

async function loadGroupOptions() {
  const response = await ojProblemGroupApi.list()
  groupOptions.value = (response.data ?? []).map((item: any) => ({
    label: item.name,
    value: item.id,
  }))
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojProblemApi.detail({ id })
    state.formModel = normalizeFormData(response.data ?? {})
  } finally {
    state.loading = false
  }
}

async function refreshTestCaseCount() {
  if (!state.dataId) {
    state.testCaseCount = 0
    return
  }
  try {
    const res = await ojProblemTestCaseApi.page(state.dataId, { current: 1, size: 1 })
    state.testCaseCount = res.data?.total ?? 0
  } catch {
    state.testCaseCount = 0
  }
}

function normalizeFormData(data: Record<string, any> = {}) {
  return {
    ...defaultFormData,
    ...data,
    group_id: data.group_id ?? null,
    type_ids: Array.isArray(data.type_ids) ? [...data.type_ids] : [],
    published_at: normalizeDateTimeValue(data.published_at),
    extra: stringifyJsonValue(data.extra),
  }
}

function normalizeDateTimeValue(value: unknown) {
  return formatDateTime(value, '') || null
}

function normalizeSubmitDateTimeValue(value: unknown) {
  const text = formatDateTime(value, '')
  if (!text) {
    return null
  }
  const date = new Date(`${text.replace(' ', 'T')}+08:00`)
  return Number.isNaN(date.getTime()) ? null : date.toISOString().replace(/\.\d{3}Z$/, 'Z')
}

function normalizeSubmitData(data: Record<string, any>) {
  return {
    ...data,
    group_id: data.group_id || null,
    published_at: normalizeSubmitDateTimeValue(data.published_at),
    extra: parseJsonValue(data.extra),
  }
}

function parseJsonValue(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) {
    return {}
  }
  const parsed = JSON.parse(text)
  if (Array.isArray(parsed) || typeof parsed !== 'object' || parsed === null) {
    throw new Error('JSON value must be an object')
  }
  return parsed
}

function isValidJsonValue(value: unknown) {
  try {
    parseJsonValue(value)
    return true
  } catch {
    return false
  }
}

function stringifyJsonValue(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return '{}'
  }
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

function goBack() {
  router.push('/biz/problem/problem')
}

async function submitForm() {
  try {
    await formRef.value?.validate()
  } catch {
    state.activeTab = 'basic'
    return
  }
  state.submitLoading = true
  try {
    const payload = normalizeSubmitData(state.formModel)
    if (state.dataId) {
      await ojProblemApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
      await fetchDetail(state.dataId)
    } else {
      const response = await ojProblemApi.create(payload)
      const newId = response.data
      window.$message.success('创建成功')
      if (newId) {
        state.dataId = String(newId)
        await router.replace({
          path: '/biz/problem/problem/edit',
          query: { id: state.dataId, tab: 'basic' },
        })
      } else {
        goBack()
      }
    }
  } finally {
    state.submitLoading = false
  }
}

async function changeStatus(next: string) {
  if (!state.dataId) {
    return
  }
  if ((next === 'ready' || next === 'published') && state.testCaseCount < 1) {
    window.$message.warning('至少需要 1 个测例才能设为就绪或发布')
    return
  }
  state.statusLoading = true
  try {
    await ojProblemApi.setStatus({ id: state.dataId, status: next })
    window.$message.success('状态已更新')
    await fetchDetail(state.dataId)
  } finally {
    state.statusLoading = false
  }
}

</script>

<template>
  <NFlex class="h-full min-h-0" vertical :size="12">
    <NFlex align="center" justify="space-between" class="shrink-0 px-2px">
      <NFlex align="center" :size="12">
        <NButton quaternary @click="goBack">
          返回
        </NButton>
        <span class="text-16px font-medium">{{ pageTitle }}</span>
        <NTag v-if="state.dataId" size="small" :type="state.formModel.status === 'published' ? 'success' : state.formModel.status === 'ready' ? 'info' : 'default'">
          {{ statusOptions.find(item => item.value === state.formModel.status)?.label || state.formModel.status }}
        </NTag>
      </NFlex>
      <NSpace>
        <NButton @click="goBack">
          取消
        </NButton>
        <NButton v-if="!isCreate || state.activeTab === 'basic'" type="primary" :loading="state.submitLoading" @click="submitForm">
          保存
        </NButton>
      </NSpace>
    </NFlex>

    <NSpin :show="state.loading" class="min-h-0 flex-1">
      <ProCard class="h-full" content-class="h-full flex flex-col min-h-0" :segmented="{ content: true }">
        <NTabs v-model:value="state.activeTab" type="line" class="h-full min-h-0 flex flex-col">
          <NTabPane name="basic" tab="基本信息" display-directive="show">
            <NForm
              ref="formRef"
              :model="state.formModel"
              :rules="rules"
              label-placement="left"
              label-width="140"
              :disabled="state.loading || state.submitLoading"
            >
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="题目编码" path="code">
                    <NInput v-model:value="state.formModel.code" />
                  </NFormItemGi>
                  <NFormItemGi label="题目标题" path="name">
                    <NInput v-model:value="state.formModel.name" />
                  </NFormItemGi>
                  <NFormItemGi label="分组" path="group_id">
                    <NSelect v-model:value="state.formModel.group_id" filterable clearable :options="groupOptions" />
                  </NFormItemGi>
                  <NFormItemGi label="题目类型" path="type_ids">
                    <NSelect v-model:value="state.formModel.type_ids" multiple filterable clearable :options="typeOptions" />
                  </NFormItemGi>
                  <NFormItemGi label="时间限制（毫秒）" path="time_limit_ms">
                    <NInputNumber v-model:value="state.formModel.time_limit_ms" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="内存限制（KB）" path="memory_limit_kb">
                    <NInputNumber v-model:value="state.formModel.memory_limit_kb" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="题目分值" path="points">
                    <NInputNumber v-model:value="state.formModel.points" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="提交源码可见性" path="submission_source_visibility">
                    <NSelect v-model:value="state.formModel.submission_source_visibility" :options="submissionSourceVisibilityOptions" />
                  </NFormItemGi>
                  <NFormItemGi label="是否允许部分分" path="partial">
                    <NSwitch v-model:value="state.formModel.partial" />
                  </NFormItemGi>
                  <NFormItemGi label="摘要" path="summary" :span="2">
                    <NInput v-model:value="state.formModel.summary" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
                  </NFormItemGi>
                  <NFormItemGi label="题面正文" path="description" :span="2">
                    <MdEditor v-model:value="state.formModel.description" :height="420" />
                  </NFormItemGi>
                </NGrid>
              </NScrollbar>
            </NForm>
          </NTabPane>

          <NTabPane v-if="state.dataId" name="testdata" tab="测试数据" display-directive="if">
            <NTabs v-model:value="testdataSubTab" type="segment" size="small">
              <NTabPane name="config" tab="配置" display-directive="if">
                <NScrollbar class="max-h-[calc(100vh-280px)] pr-8px">
                  <TestdataPanel :problem-id="state.dataId" embedded mode="judge" />
                </NScrollbar>
              </NTabPane>
              <NTabPane name="import" tab="导入" display-directive="if">
                <NScrollbar class="max-h-[calc(100vh-280px)] pr-8px">
                  <TestdataPanel :problem-id="state.dataId" embedded mode="import" />
                </NScrollbar>
              </NTabPane>
              <NTabPane name="cases" tab="测例" display-directive="if">
                <NScrollbar class="max-h-[calc(100vh-280px)] pr-8px">
                  <TestCasePanel :problem-id="state.dataId" embedded />
                </NScrollbar>
              </NTabPane>
              <NTabPane name="trial" tab="试测" display-directive="if">
                <NScrollbar class="max-h-[calc(100vh-280px)] pr-8px">
                  <NAlert type="info" class="mb-12px" :bordered="false">
                    对当前题目「测试用例」表中全部测例试跑（file / inline 均可），经 MQ 发给 worker。
                  </NAlert>
                  <TrialJudgePanel :problem-id="state.dataId" />
                </NScrollbar>
              </NTabPane>
            </NTabs>
          </NTabPane>

          <NTabPane v-if="state.dataId" name="language" tab="语言" display-directive="if">
            <LanguagePanel :problem-id="state.dataId" embedded />
          </NTabPane>

          <NTabPane v-if="state.dataId" name="solution" tab="题解" display-directive="if">
            <SolutionPanel :problem-id="state.dataId" embedded />
          </NTabPane>

          <NTabPane v-if="state.dataId" name="staff" tab="人员" display-directive="if">
            <StaffPanel :problem-id="state.dataId" embedded />
          </NTabPane>

          <NTabPane v-if="state.dataId" name="publish" tab="发布" display-directive="if">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NSpace vertical :size="16">
                <NAlert type="info" title="发布清单">
                  当前测例数：{{ state.testCaseCount }}（就绪/发布至少需要 1 个）
                </NAlert>
                <NDescriptions label-placement="left" :column="1" bordered>
                  <NDescriptionsItem label="当前状态">
                    {{ statusOptions.find(item => item.value === state.formModel.status)?.label || state.formModel.status }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="发布时间">
                    {{ state.formModel.published_at || '-' }}
                  </NDescriptionsItem>
                </NDescriptions>
                <NSpace>
                  <NButton :loading="state.statusLoading" :disabled="state.formModel.status === 'draft'" @click="changeStatus('draft')">
                    撤回为草稿
                  </NButton>
                  <NButton type="info" :loading="state.statusLoading" :disabled="state.formModel.status === 'ready'" @click="changeStatus('ready')">
                    设为就绪
                  </NButton>
                  <NButton type="primary" :loading="state.statusLoading" :disabled="state.formModel.status === 'published'" @click="changeStatus('published')">
                    发布
                  </NButton>
                  <NButton quaternary @click="refreshTestCaseCount">
                    刷新清单
                  </NButton>
                </NSpace>
              </NSpace>
            </NScrollbar>
          </NTabPane>
        </NTabs>
      </ProCard>
    </NSpin>

  </NFlex>
</template>
