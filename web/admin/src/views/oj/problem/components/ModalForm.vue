<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NDynamicTags, NFlex, NForm, NFormItem, NGi, NGrid, NInput, NInputNumber, NScrollbar, NSelect, NSpace, NSpin, NSwitch } from 'naive-ui'
import { ojProblemApi, ojProblemMemberApi, ojProblemTagRelationApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import DictSelect from '@/components/common/DictSelect.vue'
import { ProCard } from 'pro-naive-ui'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
  openDataManager: [id: string]
}>()

const formRef = ref<FormInst | null>(null)
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const dataId = ref<string | null>(null)

const defaultFormData = {
  code: '',
  title: '',
  summary: '',
  description: '',
  input_description: '',
  output_description: '',
  source: '',
  difficulty: 0,
  problem_type: 'PROGRAM',
  judge_mode: 'STANDARD',
  visibility: 'PUBLIC',
  time_limit_ms: 1000,
  memory_limit_kb: 262144,
  stack_limit_kb: null as number | null,
  output_limit_kb: null as number | null,
  points: 100,
  partial: false,
  allow_languages: '',
  spj_language_id: '',
  spj_source: '',
  interactor_language_id: '',
  interactor_source: '',
  remote_provider: '',
  remote_problem_id: '',
  sort: 0,
  status: 'ENABLED',
  extra: '{}',
  accepted_count: 0,
  submit_count: 0,
  ac_rate: 0,
  objective_answer_type: '',
  objective_answer: '{}',
  objective_score_rule: '{}',
  objective_explanation: '',
}

const state = reactive({
  formModel: { ...defaultFormData },
  authorAccounts: [] as string[],
  curatorAccounts: [] as string[],
  testerAccounts: [] as string[],
  bannedAccounts: [] as string[],
  selectedTags: [] as string[],
  datasetCount: 0,
  testCaseCount: 0,
})

const rules = computed<FormRules>(() => ({
  code: createRequiredRule('题号', 'input'),
  title: createRequiredRule('标题', 'input'),
}))

const isEditing = computed(() => !!dataId.value)
const panelTitle = computed(() => (isEditing.value ? '编辑题目: ' + state.formModel.code : '新增题目'))

async function openDrawer(id?: string | null) {
  visible.value = true
  loading.value = true
  dataId.value = id ?? null
  resetForm()
  if (id) {
    await fetchDetail(id)
    await fetchMembers()
    await fetchTags()
    await fetchDatasetSummary()
  }
  loading.value = false
}

function resetForm() {
  state.formModel = { ...defaultFormData }
  state.authorAccounts = []
  state.curatorAccounts = []
  state.testerAccounts = []
  state.bannedAccounts = []
  state.selectedTags = []
  state.datasetCount = 0
  state.testCaseCount = 0
}

async function fetchDetail(id: string) {
  const response = await ojProblemApi.detail({ id })
  const data = response.data ?? {}
  state.formModel = {
    ...defaultFormData,
    ...data,
    allow_languages: (data.allow_languages ?? []).join(','),
    extra: JSON.stringify(data.extra ?? {}, null, 2),
  }
}

async function fetchMembers() {
  const problemId = dataId.value
  if (!problemId) return
  const response = await ojProblemMemberApi.page({ problem_id: problemId, size: 100 })
  const members = response.data?.records ?? []
  state.authorAccounts = members.filter((m: any) => m.role === 'AUTHOR').map((m: any) => m.account_id)
  state.curatorAccounts = members.filter((m: any) => m.role === 'CURATOR').map((m: any) => m.account_id)
  state.testerAccounts = members.filter((m: any) => m.role === 'TESTER').map((m: any) => m.account_id)
  state.bannedAccounts = members.filter((m: any) => m.role === 'BANNED').map((m: any) => m.account_id)
}

async function fetchTags() {
  const problemId = dataId.value
  if (!problemId) return
  const response = await ojProblemApi.workspace({ id: problemId })
  state.selectedTags = (response.data?.tag_relations ?? []).map((tr: any) => tr.tag_id)
}

async function fetchDatasetSummary() {
  const problemId = dataId.value
  if (!problemId) return
  const response = await ojProblemApi.workspace({ id: problemId })
  state.datasetCount = response.data?.datasets?.length ?? 0
  state.testCaseCount = response.data?.test_cases?.length ?? 0
}

async function saveMembers() {
  const problemId = dataId.value
  if (!problemId) return
  const roles = [
    { role: 'AUTHOR', accounts: state.authorAccounts },
    { role: 'CURATOR', accounts: state.curatorAccounts },
    { role: 'TESTER', accounts: state.testerAccounts },
    { role: 'BANNED', accounts: state.bannedAccounts },
  ]
  const resp = await ojProblemMemberApi.page({ problem_id: problemId, size: 200 })
  const current: any[] = resp.data?.records ?? []
  const toKeep = new Set<string>()
  const toAdd: Array<{ account_id: string; role: string }> = []
  for (const { role, accounts } of roles) {
    for (const accountId of accounts) {
      toKeep.add(`${role}:${accountId}`)
      if (!current.find((m) => m.account_id === accountId && m.role === role)) {
        toAdd.push({ account_id: accountId, role })
      }
    }
  }
  const toRemove = current.filter((m) => !toKeep.has(`${m.role}:${m.account_id}`))
  for (const m of toRemove) await ojProblemMemberApi.remove({ ids: [m.id] })
  for (const a of toAdd) {
    await ojProblemMemberApi.create({ problem_id: problemId, account_id: a.account_id, account_type: 'ADMIN', role: a.role })
  }
}

async function saveTags() {
  const problemId = dataId.value
  if (!problemId) return
  const response = await ojProblemApi.workspace({ id: problemId })
  const current: any[] = response.data?.tag_relations ?? []
  const currentIds = current.map((tr) => tr.tag_id)
  const newIds = state.selectedTags
  const toRemove = current.filter((tr) => !newIds.includes(tr.tag_id))
  const toAdd = newIds.filter((id) => !currentIds.includes(id))
  for (const tr of toRemove) {
    await ojProblemTagRelationApi.remove({ ids: [tr.id] })
  }
  for (const tagId of toAdd) {
    await ojProblemTagRelationApi.create({ problem_id: problemId, tag_id: tagId })
  }
}

async function submitForm() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = { ...state.formModel }
    payload.allow_languages = state.formModel.allow_languages.split(',').map((s) => s.trim()).filter(Boolean)
    payload.extra = parseJson(state.formModel.extra)
    payload.summary = toNullableString(payload.summary)
    payload.description = toNullableString(payload.description)
    payload.input_description = toNullableString(payload.input_description)
    payload.output_description = toNullableString(payload.output_description)
    payload.source = toNullableString(payload.source)
    payload.spj_language_id = toNullableString(payload.spj_language_id)
    payload.spj_source = toNullableString(payload.spj_source)
    payload.interactor_language_id = toNullableString(payload.interactor_language_id)
    payload.interactor_source = toNullableString(payload.interactor_source)
    payload.remote_provider = toNullableString(payload.remote_provider)
    payload.remote_problem_id = toNullableString(payload.remote_problem_id)

    if (isEditing.value) {
      await ojProblemApi.update({ ...payload, id: dataId.value })
      await saveMembers()
      await saveTags()
      window.$message.success('更新成功')
    } else {
      const createResp = await ojProblemApi.create(payload)
      const newId = createResp.data?.id
      if (newId) {
        dataId.value = newId
        await saveMembers()
        await saveTags()
      }
      window.$message.success('创建成功')
    }
    emit('saved')
    visible.value = false
  } finally {
    submitting.value = false
  }
}

function parseJson(value: string) {
  try { return JSON.parse(value || '{}') } catch { return {} }
}

function closeDrawer() { visible.value = false }

function handleOpenDataManager() {
  const id = dataId.value
  if (id) emit('openDataManager', id)
}

defineExpose({ openDrawer })
</script>

<template>
  <NDrawer v-model:show="visible" :width="1280" mask-closable placement="right">
    <NDrawerContent :title="panelTitle" closable @close="closeDrawer">
      <template #footer>
        <NSpace justify="end">
          <NButton @click="closeDrawer">取消</NButton>
          <NButton type="primary" :loading="submitting" @click="submitForm">保存</NButton>
        </NSpace>
      </template>

      <NSpin :show="loading">
        <NScrollbar class="h-full" trigger="none">
          <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="140">
            <!-- fieldset 1: 基础信息 -->
            <ProCard title="基础信息" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NGrid :cols="2" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="题号 (code)" path="code"><NInput v-model:value="state.formModel.code" /></NFormItem></NGi>
                <NGi><NFormItem label="标题 (title)" path="title"><NInput v-model:value="state.formModel.title" /></NFormItem></NGi>
                <NGi><NFormItem label="可见性"><DictSelect v-model:value="state.formModel.visibility" dict-code="OJ_PROBLEM_VISIBILITY" /></NFormItem></NGi>
                <NGi><NFormItem label="状态"><DictSelect v-model:value="state.formModel.status" dict-code="COMMON_STATUS" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="题目来源 (source)"><NInput v-model:value="state.formModel.source" /></NFormItem>
              <NFormItem label="难度 (difficulty)"><NInputNumber v-model:value="state.formModel.difficulty" class="w-full" :min="0" :max="10" /></NFormItem>
              <!-- 题面：DMOJ 的 description 在基础 fieldset -->
              <NFormItem label="题目描述 (description)"><NInput v-model:value="state.formModel.description" type="textarea" :autosize="{ minRows: 8, maxRows: 16 }" /></NFormItem>
              <NFormItem label="输入描述"><NInput v-model:value="state.formModel.input_description" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" /></NFormItem>
              <NFormItem label="输出描述"><NInput v-model:value="state.formModel.output_description" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" /></NFormItem>
            </ProCard>

            <!-- fieldset 2: 协作者 -->
            <ProCard title="协作者" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NFormItem label="作者 (authors)"><NDynamicTags v-model:value="state.authorAccounts" :max="10" /></NFormItem>
              <NFormItem label="维护者 (curators)"><NDynamicTags v-model:value="state.curatorAccounts" :max="10" /></NFormItem>
              <NFormItem label="测试者 (testers)"><NDynamicTags v-model:value="state.testerAccounts" :max="10" /></NFormItem>
              <NFormItem label="封禁 (banned)"><NDynamicTags v-model:value="state.bannedAccounts" :max="10" /></NFormItem>
            </ProCard>

            <!-- fieldset 3: Social Media -->
            <ProCard title="Social Media" :show-collapse="true" :collapsed="true" class="mb-16px">
              <NFormItem label="摘要 (summary)"><NInput v-model:value="state.formModel.summary" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" /></NFormItem>
            </ProCard>

            <!-- fieldset 4: 分类 -->
            <ProCard title="分类" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NGrid :cols="2" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="题目类型"><DictSelect v-model:value="state.formModel.problem_type" dict-code="OJ_PROBLEM_TYPE" /></NFormItem></NGi>
                <NGi><NFormItem label="判题方式"><DictSelect v-model:value="state.formModel.judge_mode" dict-code="OJ_JUDGE_MODE" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="标签 (tags)"><NSelect v-model:value="state.selectedTags" multiple filterable tag :options="[]" placeholder="选择或输入标签" /></NFormItem>
            </ProCard>

            <!-- fieldset 5: 计分 -->
            <ProCard title="计分" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NGrid :cols="3" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="分数 (points)"><NInputNumber v-model:value="state.formModel.points" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="部分分 (partial)"><NSwitch v-model:value="state.formModel.partial" /></NFormItem></NGi>
              </NGrid>
            </ProCard>

            <!-- fieldset 6: 评测限制 -->
            <ProCard title="评测限制" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NGrid :cols="4" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="时间上限 (ms)"><NInputNumber v-model:value="state.formModel.time_limit_ms" class="w-full" :min="1" /></NFormItem></NGi>
                <NGi><NFormItem label="内存上限 (KB)"><NInputNumber v-model:value="state.formModel.memory_limit_kb" class="w-full" :min="1" /></NFormItem></NGi>
                <NGi><NFormItem label="栈上限 (KB)"><NInputNumber v-model:value="state.formModel.stack_limit_kb" class="w-full" clearable /></NFormItem></NGi>
                <NGi><NFormItem label="输出上限 (KB)"><NInputNumber v-model:value="state.formModel.output_limit_kb" class="w-full" clearable /></NFormItem></NGi>
              </NGrid>
            </ProCard>

            <!-- fieldset 7: 语言 -->
            <ProCard title="语言" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NFormItem label="允许语言"><NInput v-model:value="state.formModel.allow_languages" placeholder="cpp17,python3" /></NFormItem>
            </ProCard>

            <!-- fieldset 8: 数据管理 -->
            <ProCard title="数据管理" :show-collapse="true" :collapsed="false" class="mb-16px">
              <NFlex align="center" justify="space-between">
                <span>数据集 {{ state.datasetCount }} 个，测试点 {{ state.testCaseCount }} 个</span>
                <NButton size="small" :disabled="!isEditing" @click="handleOpenDataManager">打开数据管理</NButton>
              </NFlex>
            </ProCard>

            <!-- fieldset 9: SPJ/交互 -->
            <ProCard title="SPJ / 交互" :show-collapse="true" :collapsed="true" class="mb-16px">
              <NGrid :cols="2" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="SPJ 语言"><NInput v-model:value="state.formModel.spj_language_id" /></NFormItem></NGi>
                <NGi><NFormItem label="交互器语言"><NInput v-model:value="state.formModel.interactor_language_id" /></NFormItem></NGi>
                <NGi><NFormItem label="远程提供商"><NInput v-model:value="state.formModel.remote_provider" /></NFormItem></NGi>
                <NGi><NFormItem label="远程题目 ID"><NInput v-model:value="state.formModel.remote_problem_id" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="SPJ 源码"><NInput v-model:value="state.formModel.spj_source" type="textarea" :autosize="{ minRows: 6, maxRows: 14 }" /></NFormItem>
              <NFormItem label="交互器源码"><NInput v-model:value="state.formModel.interactor_source" type="textarea" :autosize="{ minRows: 6, maxRows: 14 }" /></NFormItem>
            </ProCard>

            <!-- fieldset 10: 元数据 -->
            <ProCard title="元数据" :show-collapse="true" :collapsed="true" class="mb-16px">
              <NGrid :cols="3" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="通过数"><NInputNumber v-model:value="state.formModel.accepted_count" class="w-full" disabled /></NFormItem></NGi>
                <NGi><NFormItem label="提交数"><NInputNumber v-model:value="state.formModel.submit_count" class="w-full" disabled /></NFormItem></NGi>
                <NGi><NFormItem label="通过率"><NInputNumber v-model:value="state.formModel.ac_rate" class="w-full" disabled /></NFormItem></NGi>
                <NGi><NFormItem label="排序 (sort)"><NInputNumber v-model:value="state.formModel.sort" class="w-full" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="扩展配置 (extra)"><NInput v-model:value="state.formModel.extra" type="textarea" :autosize="{ minRows: 6, maxRows: 14 }" /></NFormItem>
            </ProCard>

            <!-- fieldset 11: 客观题答案 -->
            <ProCard v-if="state.formModel.problem_type === 'OBJECTIVE'" title="客观题答案" :show-collapse="true" :collapsed="true" class="mb-16px">
              <NFormItem label="答案类型"><DictSelect v-model:value="state.formModel.objective_answer_type" dict-code="OJ_OBJECTIVE_ANSWER_TYPE" /></NFormItem>
              <NFormItem label="答案 (JSON)"><NInput v-model:value="state.formModel.objective_answer" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" /></NFormItem>
              <NFormItem label="计分规则 (JSON)"><NInput v-model:value="state.formModel.objective_score_rule" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" /></NFormItem>
              <NFormItem label="解析"><NInput v-model:value="state.formModel.objective_explanation" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" /></NFormItem>
            </ProCard>
          </NForm>
        </NScrollbar>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
