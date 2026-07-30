<script setup lang="ts">
import type { FormInst, FormRules, SelectOption } from 'naive-ui'
import { ojContestApi, ojContestTagApi } from '@/api'
import MdEditor from '@/components/editor/MdEditor.vue'
import { createRequiredRule, formatDateTime } from '@/utils'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BannedPanel from '../banned-user/index.vue'
import ParticipationPanel from '../participation/index.vue'
import PrivatePanel from '../private-contestant/index.vue'
import ProblemsPanel from '../problem/index.vue'
import StaffPanel from '../staff/index.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const tagOptions = ref<SelectOption[]>([])

const defaultFormData: Record<string, any> = {
  key: '',
  name: '',
  description: '',
  summary: '',
  start_time: null,
  end_time: null,
  time_limit_seconds: 0,
  is_visible: false,
  is_private: false,
  access_code: '',
  is_rated: false,
  rating_floor: 0,
  rating_ceiling: 0,
  rate_all: false,
  scoreboard_visibility: 'visible',
  format_name: 'default',
  format_config: '{}',
  points_precision: 0,
  hide_problem_tags: false,
  hide_problem_authors: false,
  run_pretests_only: false,
  use_clarifications: false,
  tester_see_scoreboard: false,
  tester_see_submissions: false,
  show_short_display: false,
  problem_label_script: '',
  locked_after: null,
  og_image: '',
  logo_override_image: '',
  tag_ids: [],
  extra: '{}',
}

const state = reactive({
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  activeTab: 'basic',
  formModel: normalizeFormData(),
})

const pageTitle = computed(() => state.dataId ? '编辑竞赛' : '新增竞赛')

const rules = computed<FormRules>(() => ({
  key: [createRequiredRule('竞赛标识', 'input')],
  name: [createRequiredRule('竞赛名称', 'input')],
  description: [createRequiredRule('竞赛说明', 'input')],
  start_time: [createRequiredRule('开始时间', 'change')],
  end_time: [createRequiredRule('结束时间', 'change')],
  format_config: [{
    validator: () => isValidJsonValue(state.formModel.format_config),
    message: '请输入合法 JSON 对象',
    trigger: ['input', 'blur'],
  }],
  extra: [{
    validator: () => isValidJsonValue(state.formModel.extra),
    message: '请输入合法 JSON 对象',
    trigger: ['input', 'blur'],
  }],
}))

onMounted(async () => {
  await loadTagOptions()
  const id = route.query.id ? String(route.query.id) : null
  state.dataId = id
  const tab = route.query.tab ? String(route.query.tab) : 'basic'
  state.activeTab = id ? tab : 'basic'
  if (id) {
    await fetchDetail(id)
  }
})

watch(
  () => state.activeTab,
  (tab) => {
    if (!state.dataId) {
      return
    }
    router.replace({
      path: '/biz/contest/contest/edit',
      query: { id: state.dataId, tab },
    })
  },
)

async function loadTagOptions() {
  const response = await ojContestTagApi.list()
  tagOptions.value = (response.data ?? []).map((item: any) => ({
    label: item.name,
    value: item.id,
  }))
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojContestApi.detail({ id })
    state.formModel = normalizeFormData(response.data ?? {})
  } finally {
    state.loading = false
  }
}

function normalizeFormData(data: Record<string, any> = {}) {
  return {
    ...defaultFormData,
    ...data,
    tag_ids: Array.isArray(data.tag_ids) ? [...data.tag_ids] : [],
    start_time: normalizeDateTimeValue(data.start_time),
    end_time: normalizeDateTimeValue(data.end_time),
    locked_after: normalizeDateTimeValue(data.locked_after),
    format_config: stringifyJsonValue(data.format_config),
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
    start_time: normalizeSubmitDateTimeValue(data.start_time),
    end_time: normalizeSubmitDateTimeValue(data.end_time),
    locked_after: normalizeSubmitDateTimeValue(data.locked_after),
    format_config: parseJsonValue(data.format_config),
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
  router.push('/biz/contest/contest')
}

async function submitForm() {
  try {
    await formRef.value?.validate()
  } catch (errors: any) {
    const firstPath = Object.keys(errors ?? {})[0]
    if (['key', 'name', 'description', 'summary', 'tag_ids'].includes(firstPath)) {
      state.activeTab = 'basic'
    } else if (['start_time', 'end_time', 'time_limit_seconds', 'format_name', 'format_config'].includes(firstPath)) {
      state.activeTab = 'schedule'
    } else if ([
      'is_visible', 'is_private', 'access_code', 'scoreboard_visibility',
      'is_rated', 'rate_all', 'rating_floor', 'rating_ceiling',
    ].includes(firstPath)) {
      state.activeTab = 'visibility'
    } else {
      state.activeTab = 'extra'
    }
    return
  }
  state.submitLoading = true
  try {
    const payload = normalizeSubmitData(state.formModel)
    if (state.dataId) {
      await ojContestApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
      await fetchDetail(state.dataId)
    } else {
      const response = await ojContestApi.create(payload)
      const newId = response.data
      window.$message.success('创建成功')
      if (newId) {
        state.dataId = String(newId)
        await router.replace({
          path: '/biz/contest/contest/edit',
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
</script>

<template>
  <NFlex class="h-full min-h-0" vertical :size="12">
    <NFlex align="center" justify="space-between" class="shrink-0 px-2px">
      <NFlex align="center" :size="12">
        <NButton quaternary @click="goBack">返回</NButton>
        <span class="text-16px font-medium">{{ pageTitle }}</span>
      </NFlex>
      <NSpace>
        <NButton @click="goBack">取消</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">保存</NButton>
      </NSpace>
    </NFlex>

    <NSpin :show="state.loading" class="min-h-0 flex-1">
      <ProCard class="h-full" content-class="h-full flex flex-col min-h-0" :segmented="{ content: true }">
        <NForm
          ref="formRef"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="140"
          :disabled="state.loading || state.submitLoading"
          class="h-full min-h-0 flex flex-col"
        >
          <NTabs v-model:value="state.activeTab" type="line" class="h-full min-h-0 flex flex-col">
            <NTabPane name="basic" tab="基本信息" display-directive="show">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="竞赛标识" path="key">
                    <NInput v-model:value="state.formModel.key" />
                  </NFormItemGi>
                  <NFormItemGi label="竞赛名称" path="name">
                    <NInput v-model:value="state.formModel.name" />
                  </NFormItemGi>
                  <NFormItemGi label="竞赛标签" path="tag_ids" :span="2">
                    <NSelect v-model:value="state.formModel.tag_ids" multiple filterable clearable :options="tagOptions" />
                  </NFormItemGi>
                  <NFormItemGi label="摘要" path="summary" :span="2">
                    <NInput v-model:value="state.formModel.summary" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
                  </NFormItemGi>
                  <NFormItemGi label="竞赛说明" path="description" :span="2">
                    <MdEditor v-model:value="state.formModel.description" :height="360" />
                  </NFormItemGi>
                </NGrid>
              </NScrollbar>
            </NTabPane>

            <NTabPane name="schedule" tab="时间与赛制" display-directive="show">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="开始时间" path="start_time">
                    <NDatePicker v-model:formatted-value="state.formModel.start_time" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="结束时间" path="end_time">
                    <NDatePicker v-model:formatted-value="state.formModel.end_time" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="个人参赛时长（秒）" path="time_limit_seconds">
                    <NInputNumber v-model:value="state.formModel.time_limit_seconds" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="赛制" path="format_name">
                    <NInput v-model:value="state.formModel.format_name" />
                  </NFormItemGi>
                  <NFormItemGi label="赛制配置" path="format_config" :span="2">
                    <NInput v-model:value="state.formModel.format_config" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
                  </NFormItemGi>
                </NGrid>
              </NScrollbar>
            </NTabPane>

            <NTabPane name="visibility" tab="可见性与 Rating" display-directive="show">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="是否公开可见" path="is_visible">
                    <NSwitch v-model:value="state.formModel.is_visible" />
                  </NFormItemGi>
                  <NFormItemGi label="是否仅限指定选手" path="is_private">
                    <NSwitch v-model:value="state.formModel.is_private" />
                  </NFormItemGi>
                  <NFormItemGi label="参赛准入码" path="access_code">
                    <NInput v-model:value="state.formModel.access_code" />
                  </NFormItemGi>
                  <NFormItemGi label="榜单可见性" path="scoreboard_visibility">
                    <NInput v-model:value="state.formModel.scoreboard_visibility" />
                  </NFormItemGi>
                  <NFormItemGi label="是否计入 Rating" path="is_rated">
                    <NSwitch v-model:value="state.formModel.is_rated" />
                  </NFormItemGi>
                  <NFormItemGi label="无提交也计 Rating" path="rate_all">
                    <NSwitch v-model:value="state.formModel.rate_all" />
                  </NFormItemGi>
                  <NFormItemGi label="Rating 下限" path="rating_floor">
                    <NInputNumber v-model:value="state.formModel.rating_floor" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="Rating 上限" path="rating_ceiling">
                    <NInputNumber v-model:value="state.formModel.rating_ceiling" class="w-full" />
                  </NFormItemGi>
                </NGrid>
              </NScrollbar>
            </NTabPane>

            <NTabPane name="extra" tab="其他设置" display-directive="show">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="分数小数精度" path="points_precision">
                    <NInputNumber v-model:value="state.formModel.points_precision" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="重判锁定时间" path="locked_after">
                    <NDatePicker v-model:formatted-value="state.formModel.locked_after" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="赛中隐藏题目标签" path="hide_problem_tags">
                    <NSwitch v-model:value="state.formModel.hide_problem_tags" />
                  </NFormItemGi>
                  <NFormItemGi label="赛中隐藏命题人" path="hide_problem_authors">
                    <NSwitch v-model:value="state.formModel.hide_problem_authors" />
                  </NFormItemGi>
                  <NFormItemGi label="赛中仅跑 pretest" path="run_pretests_only">
                    <NSwitch v-model:value="state.formModel.run_pretests_only" />
                  </NFormItemGi>
                  <NFormItemGi label="使用答疑" path="use_clarifications">
                    <NSwitch v-model:value="state.formModel.use_clarifications" />
                  </NFormItemGi>
                  <NFormItemGi label="扩展信息" path="extra" :span="2">
                    <NInput v-model:value="state.formModel.extra" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
                  </NFormItemGi>
                </NGrid>
              </NScrollbar>
            </NTabPane>

            <NTabPane v-if="state.dataId" name="problems" tab="题目集" display-directive="if">
              <ProblemsPanel :contest-id="state.dataId" embedded />
            </NTabPane>
            <NTabPane v-if="state.dataId" name="staff" tab="人员" display-directive="if">
              <StaffPanel :contest-id="state.dataId" embedded />
            </NTabPane>
            <NTabPane v-if="state.dataId" name="private" tab="私有选手" display-directive="if">
              <PrivatePanel :contest-id="state.dataId" embedded />
            </NTabPane>
            <NTabPane v-if="state.dataId" name="banned" tab="禁赛" display-directive="if">
              <BannedPanel :contest-id="state.dataId" embedded />
            </NTabPane>
            <NTabPane v-if="state.dataId" name="participation" tab="参赛" display-directive="if">
              <ParticipationPanel :contest-id="state.dataId" embedded />
            </NTabPane>
          </NTabs>
        </NForm>
      </ProCard>
    </NSpin>
  </NFlex>
</template>
