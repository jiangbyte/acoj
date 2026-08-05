<script setup lang="ts">
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import { ojContestApi, ojContestClarificationApi, ojContestRatingApi, ojContestTagApi } from '@/api'
import MdEditor from '@/components/editor/MdEditor.vue'
import { createRequiredRule, createTagColor, dictList, dictTypeColor, dictTypeData, formatDateTime } from '@/utils'
import { NButton, NTag } from 'naive-ui'
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BannedPanel from '../banned-user/index.vue'
import ParticipationPanel from '../participation/index.vue'
import RegistrationPanel from '../registration/index.vue'
import ProblemsPanel from '../problem/index.vue'
import StaffPanel from '../staff/index.vue'
import SubmissionPanel from '../../submission/submission/index.vue'

const PEOPLE_TABS = ['staff', 'registration', 'banned', 'participation'] as const
const threadStatusOptions: SelectOption[] = [
  { label: 'OPEN', value: 'OPEN' },
  { label: 'ANSWERED', value: 'ANSWERED' },
  { label: 'CLOSED', value: 'CLOSED' },
]

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const tagOptions = ref<SelectOption[]>([])

const formatOptions = computed<SelectOption[]>(() => {
  const fromDict = dictList('CONTEST_FORMAT')
  return fromDict.length
    ? fromDict
    : [
        { label: 'default', value: 'default' },
        { label: 'ACM', value: 'acm' },
        { label: 'ICPC', value: 'icpc' },
        { label: 'AtCoder', value: 'atcoder' },
        { label: 'OI', value: 'oi' },
        { label: 'IOI', value: 'ioi' },
      ]
})

const scoreboardVisibilityOptions: SelectOption[] = [
  { label: 'VISIBLE', value: 'VISIBLE' },
  { label: 'AFTER_CONTEST', value: 'AFTER_CONTEST' },
  { label: 'AFTER_PARTICIPATION', value: 'AFTER_PARTICIPATION' },
  { label: 'HIDDEN', value: 'HIDDEN' },
]

const defaultFormData: Record<string, any> = {
  key: '',
  name: '',
  description: '',
  summary: '',
  start_time: null,
  end_time: null,
  time_limit_seconds: 0,
  freeze_seconds: 0,
  is_visible: false,
  is_private: false,
  access_code: '',
  is_rated: false,
  rating_floor: 0,
  rating_ceiling: 0,
  rate_all: false,
  scoreboard_visibility: 'VISIBLE',
  format_name: 'default',
  format_config: '{}',
  points_precision: 0,
  hide_problem_tags: false,
  hide_problem_authors: false,
  run_pretests_only: false,
  use_clarifications: false,
  tester_see_scoreboard: false,
  tester_see_submissions: false,
  locked_after: null,
  register_start: null,
  register_end: null,
  registration_mode: 'AUTO',
  list_visibility: 'PUBLIC',
  tag_ids: [],
  extra: '{}',
  lifecycle_status: null,
}

const state = reactive({
  loading: false,
  submitLoading: false,
  actionLoading: false,
  dataId: null as string | null,
  activeTab: 'basic',
  peopleSubTab: 'staff' as string,
  formModel: normalizeFormData(),
  formatExtras: {
    penalty_minutes: 20,
    cumtime: false,
  },
  rating: {
    loading: false,
    rows: [] as any[],
  },
  scoreboard: {
    loading: false,
    rows: [] as any[],
    virtual: 0,
  },
  clarification: {
    broadcastLoading: false,
    threadLoading: false,
    broadcasts: [] as any[],
    threads: [] as any[],
    createTitle: '',
    createBody: '',
    createLoading: false,
    activeThreadId: null as string | null,
    activeThread: null as any,
    replyBody: '',
    replyLoading: false,
    setAnswered: true,
    statusLoading: false,
    promoteLoading: false,
  },
})

const pageTitle = computed(() => state.dataId ? '编辑竞赛' : '新增竞赛')
const isPenaltyFormat = computed(() => ['acm', 'icpc', 'atcoder'].includes(String(state.formModel.format_name)))
const isCumtimeFormat = computed(() => ['oi', 'ioi'].includes(String(state.formModel.format_name)))
const isLocked = computed(() => state.formModel.lifecycle_status === 'LOCKED')

const scoreboardColumns = computed<DataTableColumns<any>>(() => [
  { title: '排名', key: 'rank', width: 70 },
  { title: '账户', key: 'account_id', ellipsis: { tooltip: true } },
  { title: '得分', key: 'score', width: 90 },
  { title: '罚时/时间', key: 'cumtime', width: 110 },
  { title: 'Tiebreaker', key: 'tiebreaker', width: 110 },
  {
    title: '取消资格',
    key: 'is_disqualified',
    width: 90,
    render: (row) => (row.is_disqualified ? '是' : '否'),
  },
])

const ratingColumns = computed<DataTableColumns<any>>(() => [
  { title: '排名', key: 'rank', width: 70 },
  { title: '账户', key: 'account_id', ellipsis: { tooltip: true } },
  { title: 'Rating', key: 'rating', width: 90 },
  { title: 'Delta', key: 'delta', width: 90 },
  { title: 'Performance', key: 'performance', width: 110 },
  {
    title: '结算时间',
    key: 'rated_at',
    width: 170,
    render: (row) => formatDateTime(row.rated_at),
  },
])

const broadcastColumns = computed<DataTableColumns<any>>(() => [
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '题目', key: 'problem_id', width: 140, ellipsis: { tooltip: true } },
  {
    title: '发布时间',
    key: 'published_at',
    width: 170,
    render: (row) => formatDateTime(row.published_at),
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row) => h(
      NButton,
      { size: 'tiny', type: 'error', text: true, onClick: () => deleteBroadcast(row.id) },
      { default: () => '删除' },
    ),
  },
])

const threadColumns = computed<DataTableColumns<any>>(() => [
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '账户', key: 'account_id', width: 140, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render: (row) => h(NTag, { size: 'small' }, { default: () => row.status }),
  },
  {
    title: '创建时间',
    key: 'created_at',
    width: 170,
    render: (row) => formatDateTime(row.created_at),
  },
])

const rules = computed<FormRules>(() => ({
  key: [createRequiredRule('竞赛标识', 'input')],
  name: [createRequiredRule('竞赛名称', 'input')],
  description: [createRequiredRule('竞赛说明', 'input')],
  start_time: [createRequiredRule('开始时间', 'change')],
  end_time: [createRequiredRule('结束时间', 'change')],
  format_name: [createRequiredRule('赛制', 'change')],
  scoreboard_visibility: [createRequiredRule('榜单可见性', 'change')],
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
  applyTabFromQuery(tab, route.query.people ? String(route.query.people) : undefined)
  if (id) {
    await fetchDetail(id)
    await loadTabData(state.activeTab)
  }
})

watch(
  () => state.activeTab,
  async (tab) => {
    if (!state.dataId) {
      return
    }
    syncTabQuery()
    await loadTabData(tab)
  },
)

watch(
  () => state.peopleSubTab,
  () => {
    if (state.dataId && state.activeTab === 'people') {
      syncTabQuery()
    }
  },
)

function applyTabFromQuery(tab: string, people?: string) {
  if (!state.dataId) {
    state.activeTab = 'basic'
    return
  }
  if ((PEOPLE_TABS as readonly string[]).includes(tab)) {
    state.activeTab = 'people'
    state.peopleSubTab = tab
    return
  }
  if (tab === 'clarification') {
    state.activeTab = 'clarifications'
    return
  }
  if (tab === 'people') {
    state.activeTab = 'people'
    state.peopleSubTab = people && (PEOPLE_TABS as readonly string[]).includes(people) ? people : 'staff'
    return
  }
  state.activeTab = tab || 'basic'
}

function syncTabQuery() {
  if (!state.dataId) {
    return
  }
  const query: Record<string, string> = { id: state.dataId, tab: state.activeTab }
  if (state.activeTab === 'people') {
    query.people = state.peopleSubTab
  }
  router.replace({ path: '/biz/contest/contest/edit', query })
}

async function loadTabData(tab: string) {
  if (tab === 'access') {
    await loadRatings()
  }
  if (tab === 'scoreboard') {
    await loadScoreboard()
  }
  if (tab === 'clarifications') {
    await loadClarifications()
  }
}

watch(
  () => [state.formatExtras.penalty_minutes, state.formatExtras.cumtime, state.formModel.format_name],
  () => syncFormatExtrasIntoConfig(),
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
    pullFormatExtrasFromConfig()
  } finally {
    state.loading = false
  }
}

function normalizeFormData(data: Record<string, any> = {}) {
  const visibility = String(data.scoreboard_visibility || 'VISIBLE').toUpperCase()
  return {
    ...defaultFormData,
    ...data,
    tag_ids: Array.isArray(data.tag_ids) ? [...data.tag_ids] : [],
    start_time: normalizeDateTimeValue(data.start_time),
    end_time: normalizeDateTimeValue(data.end_time),
    locked_after: normalizeDateTimeValue(data.locked_after),
    register_start: normalizeDateTimeValue(data.register_start),
    register_end: normalizeDateTimeValue(data.register_end),
    registration_mode: data.registration_mode || 'AUTO',
    list_visibility: data.list_visibility || 'PUBLIC',
    freeze_seconds: data.freeze_seconds ?? 0,
    scoreboard_visibility: visibility,
    format_name: data.format_name || 'default',
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
  syncFormatExtrasIntoConfig()
  return {
    ...data,
    start_time: normalizeSubmitDateTimeValue(data.start_time),
    end_time: normalizeSubmitDateTimeValue(data.end_time),
    locked_after: normalizeSubmitDateTimeValue(data.locked_after),
    register_start: normalizeSubmitDateTimeValue(data.register_start),
    register_end: normalizeSubmitDateTimeValue(data.register_end),
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

function pullFormatExtrasFromConfig() {
  try {
    const config = parseJsonValue(state.formModel.format_config)
    state.formatExtras.penalty_minutes = Number(config.penalty_minutes ?? 20)
    state.formatExtras.cumtime = Boolean(config.cumtime)
  } catch {
    state.formatExtras.penalty_minutes = 20
    state.formatExtras.cumtime = false
  }
}

function syncFormatExtrasIntoConfig() {
  let config: Record<string, any> = {}
  try {
    config = parseJsonValue(state.formModel.format_config)
  } catch {
    config = {}
  }
  if (isPenaltyFormat.value) {
    config.penalty_minutes = state.formatExtras.penalty_minutes
  }
  if (isCumtimeFormat.value) {
    config.cumtime = state.formatExtras.cumtime
  }
  state.formModel.format_config = stringifyJsonValue(config)
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
    } else if ([
      'start_time', 'end_time', 'time_limit_seconds', 'freeze_seconds',
      'format_name', 'format_config', 'points_precision',
    ].includes(firstPath)) {
      state.activeTab = 'schedule'
    } else {
      state.activeTab = 'access'
    }
    return
  }
  state.submitLoading = true
  try {
    const payload = normalizeSubmitData(state.formModel)
    delete payload.lifecycle_status
    delete payload.user_count
    delete payload.tag_names
    delete payload.created_at
    delete payload.created_by
    delete payload.updated_at
    delete payload.updated_by
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
        await fetchDetail(state.dataId)
      } else {
        goBack()
      }
    }
  } finally {
    state.submitLoading = false
  }
}

async function handleLock() {
  if (!state.dataId) {
    return
  }
  state.actionLoading = true
  try {
    await ojContestApi.lock({ contest_id: state.dataId })
    window.$message.success('已锁定')
    await fetchDetail(state.dataId)
  } finally {
    state.actionLoading = false
  }
}

async function handleUnlock() {
  if (!state.dataId) {
    return
  }
  state.actionLoading = true
  try {
    await ojContestApi.unlock({ contest_id: state.dataId })
    window.$message.success('已解锁')
    await fetchDetail(state.dataId)
  } finally {
    state.actionLoading = false
  }
}

function handleClone() {
  if (!state.dataId) {
    return
  }
  window.$dialog.warning({
    title: '克隆竞赛',
    content: '将复制当前竞赛配置与题目，是否继续？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      state.actionLoading = true
      try {
        const response = await ojContestApi.clone({ contest_id: state.dataId!, copy_staff: false })
        const newId = response.data
        window.$message.success('克隆成功')
        if (newId) {
          await router.push({ path: '/biz/contest/contest/edit', query: { id: String(newId), tab: 'basic' } })
        }
      } finally {
        state.actionLoading = false
      }
    },
  })
}

async function handleRescore() {
  if (!state.dataId) {
    return
  }
  state.actionLoading = true
  try {
    const response = await ojContestApi.rescore({ contest_id: state.dataId })
    window.$message.success(`重算完成：${response.data?.participations ?? 0} 条参赛`)
    if (state.activeTab === 'scoreboard') {
      await loadScoreboard()
    }
  } finally {
    state.actionLoading = false
  }
}

async function handleRate() {
  if (!state.dataId) {
    return
  }
  state.actionLoading = true
  try {
    const response = await ojContestApi.rate({ contest_id: state.dataId })
    window.$message.success(`Rating 结算完成：${response.data?.rated ?? 0} 人`)
    await loadRatings()
  } finally {
    state.actionLoading = false
  }
}

async function loadRatings() {
  if (!state.dataId) {
    return
  }
  state.rating.loading = true
  try {
    const response = await ojContestRatingApi.list(state.dataId)
    state.rating.rows = Array.isArray(response.data) ? response.data : (response.data?.records ?? [])
  } finally {
    state.rating.loading = false
  }
}

async function loadScoreboard() {
  if (!state.dataId) {
    return
  }
  state.scoreboard.loading = true
  try {
    const response = await ojContestApi.scoreboard({
      contest_id: state.dataId,
      virtual: state.scoreboard.virtual,
    })
    state.scoreboard.rows = response.data?.rows ?? []
  } finally {
    state.scoreboard.loading = false
  }
}

async function loadClarifications() {
  if (!state.dataId) {
    return
  }
  state.clarification.broadcastLoading = true
  state.clarification.threadLoading = true
  try {
    const [broadcastRes, threadRes] = await Promise.all([
      ojContestClarificationApi.page(state.dataId, { current: 1, size: 50 }),
      ojContestClarificationApi.threadPage(state.dataId, { current: 1, size: 50 }),
    ])
    state.clarification.broadcasts = broadcastRes.data?.records ?? []
    state.clarification.threads = threadRes.data?.records ?? []
    if (state.clarification.activeThreadId) {
      const found = state.clarification.threads.find((item: any) => item.id === state.clarification.activeThreadId)
      if (found) {
        state.clarification.activeThread = found
      }
    }
  } finally {
    state.clarification.broadcastLoading = false
    state.clarification.threadLoading = false
  }
}

async function createBroadcast() {
  if (!state.dataId) {
    return
  }
  if (!state.clarification.createTitle.trim() || !state.clarification.createBody.trim()) {
    window.$message.warning('请填写广播标题与正文')
    return
  }
  state.clarification.createLoading = true
  try {
    await ojContestClarificationApi.create(state.dataId, {
      title: state.clarification.createTitle.trim(),
      body: state.clarification.createBody.trim(),
    })
    state.clarification.createTitle = ''
    state.clarification.createBody = ''
    window.$message.success('广播已发布')
    await loadClarifications()
  } finally {
    state.clarification.createLoading = false
  }
}

async function deleteBroadcast(id: string) {
  if (!state.dataId) {
    return
  }
  window.$dialog.warning({
    title: '删除广播',
    content: '确认删除该广播？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojContestClarificationApi.remove(state.dataId!, { ids: [id] })
      window.$message.success('已删除')
      await loadClarifications()
    },
  })
}

function openThread(row: any) {
  state.clarification.activeThreadId = row.id
  state.clarification.activeThread = row
  state.clarification.replyBody = ''
}

async function replyThread() {
  if (!state.dataId || !state.clarification.activeThreadId) {
    return
  }
  if (!state.clarification.replyBody.trim()) {
    window.$message.warning('请填写回复内容')
    return
  }
  state.clarification.replyLoading = true
  try {
    const response = await ojContestClarificationApi.threadReply(state.dataId, {
      thread_id: state.clarification.activeThreadId,
      body: state.clarification.replyBody.trim(),
      set_answered: state.clarification.setAnswered,
    })
    window.$message.success('回复成功')
    state.clarification.activeThread = response.data ?? state.clarification.activeThread
    state.clarification.replyBody = ''
    await loadClarifications()
  } finally {
    state.clarification.replyLoading = false
  }
}

async function setThreadStatus(status: string) {
  if (!state.dataId || !state.clarification.activeThreadId) {
    return
  }
  state.clarification.statusLoading = true
  try {
    await ojContestClarificationApi.threadStatus(state.dataId, {
      thread_id: state.clarification.activeThreadId,
      status,
    })
    window.$message.success('状态已更新')
    if (state.clarification.activeThread) {
      state.clarification.activeThread.status = status
    }
    await loadClarifications()
  } finally {
    state.clarification.statusLoading = false
  }
}

async function promoteThread() {
  if (!state.dataId || !state.clarification.activeThreadId) {
    return
  }
  state.clarification.promoteLoading = true
  try {
    await ojContestClarificationApi.threadPromote(state.dataId, {
      thread_id: state.clarification.activeThreadId,
    })
    window.$message.success('已转为公开广播')
    await loadClarifications()
  } finally {
    state.clarification.promoteLoading = false
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
        <NTag
          v-if="state.dataId && state.formModel.lifecycle_status"
          size="small"
          :color="createTagColor(dictTypeColor('CONTEST_LIFECYCLE_STATUS', state.formModel.lifecycle_status))"
          :bordered="false"
        >
          {{ dictTypeData('CONTEST_LIFECYCLE_STATUS', state.formModel.lifecycle_status) || state.formModel.lifecycle_status }}
        </NTag>
      </NFlex>
      <NSpace>
        <template v-if="state.dataId">
          <NButton v-if="!isLocked" :loading="state.actionLoading" @click="handleLock">
            锁定
          </NButton>
          <NButton v-else :loading="state.actionLoading" @click="handleUnlock">
            解锁
          </NButton>
          <NButton :loading="state.actionLoading" @click="handleClone">
            克隆
          </NButton>
          <NButton :loading="state.actionLoading" @click="handleRescore">
            重算
          </NButton>
          <NButton :loading="state.actionLoading" @click="handleRate">
            Rating
          </NButton>
        </template>
        <NButton @click="goBack">
          取消
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          保存
        </NButton>
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

            <NTabPane name="schedule" tab="赛程与赛制" display-directive="show">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="开始时间" path="start_time">
                    <NDatePicker v-model:formatted-value="state.formModel.start_time" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="结束时间" path="end_time">
                    <NDatePicker v-model:formatted-value="state.formModel.end_time" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="报名开始" path="register_start">
                    <NDatePicker v-model:formatted-value="state.formModel.register_start" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="报名截止" path="register_end">
                    <NDatePicker v-model:formatted-value="state.formModel.register_end" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="个人参赛时长（秒）" path="time_limit_seconds">
                    <NInputNumber v-model:value="state.formModel.time_limit_seconds" class="w-full" />
                  </NFormItemGi>
                  <NFormItemGi label="封榜秒数" path="freeze_seconds">
                    <NInputNumber v-model:value="state.formModel.freeze_seconds" class="w-full" :min="0" />
                  </NFormItemGi>
                  <NFormItemGi label="赛制" path="format_name">
                    <NSelect v-model:value="state.formModel.format_name" :options="formatOptions" />
                  </NFormItemGi>
                  <NFormItemGi label="分数小数精度" path="points_precision">
                    <NInputNumber v-model:value="state.formModel.points_precision" class="w-full" :min="0" />
                  </NFormItemGi>
                  <NFormItemGi v-if="isPenaltyFormat" label="罚时（分钟）" :span="2">
                    <NInputNumber v-model:value="state.formatExtras.penalty_minutes" class="w-full" :min="0" />
                  </NFormItemGi>
                  <NFormItemGi v-if="isCumtimeFormat" label="累计时间决胜" :span="2">
                    <NSwitch v-model:value="state.formatExtras.cumtime" />
                  </NFormItemGi>
                  <NFormItemGi label="赛制配置（高级）" path="format_config" :span="2">
                    <NInput v-model:value="state.formModel.format_config" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
                  </NFormItemGi>
                </NGrid>
              </NScrollbar>
            </NTabPane>

            <NTabPane name="access" tab="访问与 Rating" display-directive="show">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="是否公开可见" path="is_visible">
                    <NSwitch v-model:value="state.formModel.is_visible" />
                  </NFormItemGi>
                  <NFormItemGi label="是否私有赛（禁止自助报名）" path="is_private">
                    <NSwitch v-model:value="state.formModel.is_private" />
                  </NFormItemGi>
                  <NFormItemGi label="列表可见性" path="list_visibility">
                    <NSelect
                      v-model:value="state.formModel.list_visibility"
                      :options="[
                        { label: '公开列表', value: 'PUBLIC' },
                        { label: '仅邀请可见', value: 'INVITE_ONLY' },
                      ]"
                    />
                  </NFormItemGi>
                  <NFormItemGi label="报名审核" path="registration_mode">
                    <NSelect
                      v-model:value="state.formModel.registration_mode"
                      :disabled="state.formModel.is_private"
                      :options="[
                        { label: '自动通过', value: 'AUTO' },
                        { label: '需审核', value: 'REVIEW' },
                      ]"
                    />
                  </NFormItemGi>
                  <NFormItemGi label="参赛准入码" path="access_code">
                    <NInput v-model:value="state.formModel.access_code" placeholder="公开赛可选" />
                  </NFormItemGi>
                  <NFormItemGi label="榜单可见性" path="scoreboard_visibility">
                    <NSelect v-model:value="state.formModel.scoreboard_visibility" :options="scoreboardVisibilityOptions" />
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
                  <NFormItemGi label="测试员可见榜单" path="tester_see_scoreboard">
                    <NSwitch v-model:value="state.formModel.tester_see_scoreboard" />
                  </NFormItemGi>
                  <NFormItemGi label="测试员可见提交" path="tester_see_submissions">
                    <NSwitch v-model:value="state.formModel.tester_see_submissions" />
                  </NFormItemGi>
                  <NFormItemGi label="重判锁定时间" path="locked_after">
                    <NDatePicker v-model:formatted-value="state.formModel.locked_after" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
                  </NFormItemGi>
                  <NFormItemGi label="扩展信息" path="extra" :span="2">
                    <NInput v-model:value="state.formModel.extra" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
                  </NFormItemGi>
                </NGrid>

                <template v-if="state.dataId">
                  <NDivider title-placement="left">
                    Rating 结算记录
                  </NDivider>
                  <NFlex justify="space-between" align="center" class="mb-12px">
                    <span class="opacity-70">共 {{ state.rating.rows.length }} 条</span>
                    <NSpace>
                      <NButton :loading="state.rating.loading" @click="loadRatings">
                        刷新
                      </NButton>
                      <NButton type="primary" :loading="state.actionLoading" @click="handleRate">
                        结算 Rating
                      </NButton>
                    </NSpace>
                  </NFlex>
                  <NDataTable
                    :columns="ratingColumns"
                    :data="state.rating.rows"
                    :loading="state.rating.loading"
                    size="small"
                    :bordered="false"
                  />
                </template>
              </NScrollbar>
            </NTabPane>

            <NTabPane v-if="state.dataId" name="problems" tab="组题" display-directive="if">
              <ProblemsPanel :contest-id="state.dataId" embedded />
            </NTabPane>

            <NTabPane v-if="state.dataId" name="people" tab="人员" display-directive="if">
              <NTabs v-model:value="state.peopleSubTab" type="segment" size="small" animated>
                <NTabPane name="staff" tab="工作人员" display-directive="if">
                  <StaffPanel :contest-id="state.dataId" embedded />
                </NTabPane>
                <NTabPane name="registration" tab="报名人员" display-directive="if">
                  <RegistrationPanel :contest-id="state.dataId" />
                </NTabPane>
                <NTabPane name="banned" tab="禁赛" display-directive="if">
                  <BannedPanel :contest-id="state.dataId" embedded />
                </NTabPane>
                <NTabPane name="participation" tab="参赛" display-directive="if">
                  <ParticipationPanel :contest-id="state.dataId" embedded />
                </NTabPane>
              </NTabs>
            </NTabPane>

            <NTabPane v-if="state.dataId" name="scoreboard" tab="排行榜" display-directive="if">
              <NFlex vertical :size="12" class="pr-8px">
                <NFlex justify="space-between" align="center">
                  <NSelect
                    v-model:value="state.scoreboard.virtual"
                    style="width: 180px"
                    :options="[
                      { label: '正式赛 (LIVE)', value: 0 },
                      { label: '观赛 (SPECTATE)', value: -1 },
                    ]"
                    @update:value="loadScoreboard"
                  />
                  <NSpace>
                    <NButton :loading="state.scoreboard.loading" @click="loadScoreboard">
                      刷新
                    </NButton>
                    <NButton type="primary" :loading="state.actionLoading" @click="handleRescore">
                      重算
                    </NButton>
                  </NSpace>
                </NFlex>
                <NDataTable
                  :columns="scoreboardColumns"
                  :data="state.scoreboard.rows"
                  :loading="state.scoreboard.loading"
                  size="small"
                  :bordered="false"
                />
              </NFlex>
            </NTabPane>

            <NTabPane v-if="state.dataId" name="clarifications" tab="答疑" display-directive="if">
              <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
                <NFlex vertical :size="16">
                  <div>
                    <div class="mb-8px text-14px font-medium">
                      发布广播
                    </div>
                    <NGrid :cols="1" :x-gap="12">
                      <NFormItemGi label="标题" :show-feedback="false">
                        <NInput v-model:value="state.clarification.createTitle" placeholder="广播标题" />
                      </NFormItemGi>
                      <NFormItemGi label="正文" :show-feedback="false">
                        <NInput
                          v-model:value="state.clarification.createBody"
                          type="textarea"
                          :autosize="{ minRows: 3, maxRows: 6 }"
                          placeholder="广播正文"
                        />
                      </NFormItemGi>
                    </NGrid>
                    <NButton type="primary" :loading="state.clarification.createLoading" @click="createBroadcast">
                      发布广播
                    </NButton>
                  </div>

                  <div>
                    <div class="mb-8px text-14px font-medium">
                      广播列表
                    </div>
                    <NDataTable
                      :columns="broadcastColumns"
                      :data="state.clarification.broadcasts"
                      :loading="state.clarification.broadcastLoading"
                      size="small"
                      :bordered="false"
                    />
                  </div>

                  <div>
                    <div class="mb-8px text-14px font-medium">
                      提问线程
                    </div>
                    <NDataTable
                      :columns="threadColumns"
                      :data="state.clarification.threads"
                      :loading="state.clarification.threadLoading"
                      size="small"
                      :bordered="false"
                      :row-props="(row) => ({
                        style: 'cursor: pointer',
                        onClick: () => openThread(row),
                      })"
                    />
                  </div>

                  <div v-if="state.clarification.activeThread">
                    <NFlex align="center" justify="space-between" class="mb-8px">
                      <div class="text-14px font-medium">
                        线程 · {{ state.clarification.activeThread.title }}
                        <NTag size="small" class="ml-8px">
                          {{ state.clarification.activeThread.status }}
                        </NTag>
                      </div>
                      <NSpace>
                        <NSelect
                          :value="state.clarification.activeThread.status"
                          :options="threadStatusOptions"
                          style="width: 140px"
                          size="small"
                          :loading="state.clarification.statusLoading"
                          @update:value="setThreadStatus"
                        />
                        <NButton
                          size="small"
                          :loading="state.clarification.promoteLoading"
                          @click="promoteThread"
                        >
                          转公开广播
                        </NButton>
                      </NSpace>
                    </NFlex>
                    <NFlex vertical :size="8" class="mb-12px">
                      <NCard
                        v-for="msg in (state.clarification.activeThread.messages || [])"
                        :key="msg.id"
                        size="small"
                        :title="msg.is_staff ? '工作人员' : '选手'"
                      >
                        <div class="whitespace-pre-wrap">
                          {{ msg.body }}
                        </div>
                        <template #footer>
                          <span class="text-12px opacity-60">{{ formatDateTime(msg.created_at) }}</span>
                        </template>
                      </NCard>
                      <NEmpty
                        v-if="!(state.clarification.activeThread.messages || []).length"
                        description="暂无消息"
                        size="small"
                      />
                    </NFlex>
                    <NInput
                      v-model:value="state.clarification.replyBody"
                      type="textarea"
                      :autosize="{ minRows: 3, maxRows: 6 }"
                      placeholder="工作人员回复"
                      class="mb-8px"
                    />
                    <NFlex align="center" :size="12">
                      <NSwitch v-model:value="state.clarification.setAnswered">
                        <template #checked>
                          标记已回复
                        </template>
                        <template #unchecked>
                          保持状态
                        </template>
                      </NSwitch>
                      <NButton type="primary" :loading="state.clarification.replyLoading" @click="replyThread">
                        回复
                      </NButton>
                    </NFlex>
                  </div>
                </NFlex>
              </NScrollbar>
            </NTabPane>

            <NTabPane v-if="state.dataId" name="submissions" tab="提交" display-directive="if">
              <SubmissionPanel :contest-id="state.dataId" embedded />
            </NTabPane>
          </NTabs>
        </NForm>
      </ProCard>
    </NSpin>
  </NFlex>
</template>
