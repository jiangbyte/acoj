<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { ProCard } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'

const props = defineProps<{
  id?: string | null
}>()

const emit = defineEmits<{
  saved: []
  cancel: []
}>()

const formRef = ref<FormInst | null>(null)
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
  accepted_count: 0,
  submit_count: 0,
  ac_rate: 0,
  sort: 0,
  status: 'ENABLED',
  extra: '{}',
}

const state = reactive({
  loading: false,
  submitLoading: false,
  activeTab: 'basic',
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const panelTitle = computed(() => (state.dataId ? '编辑题目' : '新增题目'))
const rules = computed<FormRules>(() => ({
  code: createRequiredRule('code', 'input'),
  title: createRequiredRule('title', 'input'),
  problem_type: createRequiredRule('problem_type', 'change'),
  judge_mode: createRequiredRule('judge_mode', 'change'),
  visibility: createRequiredRule('visibility', 'change'),
  status: createRequiredRule('status', 'change'),
}))

onMounted(initForm)
watch(() => props.id, initForm)

async function initForm() {
  state.dataId = props.id ?? null
  state.activeTab = 'basic'
  state.formModel = { ...defaultFormData }
  if (state.dataId) {
    await fetchDetail(state.dataId)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojProblemApi.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      ...defaultFormData,
      ...data,
      summary: data.summary ?? '',
      description: data.description ?? '',
      input_description: data.input_description ?? '',
      output_description: data.output_description ?? '',
      source: data.source ?? '',
      allow_languages: (data.allow_languages ?? []).join(','),
      spj_language_id: data.spj_language_id ?? '',
      spj_source: data.spj_source ?? '',
      interactor_language_id: data.interactor_language_id ?? '',
      interactor_source: data.interactor_source ?? '',
      remote_provider: data.remote_provider ?? '',
      remote_problem_id: data.remote_problem_id ?? '',
      extra: JSON.stringify(data.extra ?? {}, null, 2),
    }
  } finally {
    state.loading = false
  }
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = {
      ...state.formModel,
      summary: nullableString(state.formModel.summary),
      description: nullableString(state.formModel.description),
      input_description: nullableString(state.formModel.input_description),
      output_description: nullableString(state.formModel.output_description),
      source: nullableString(state.formModel.source),
      allow_languages: state.formModel.allow_languages.split(',').map((item) => item.trim()).filter(Boolean),
      spj_language_id: nullableString(state.formModel.spj_language_id),
      spj_source: nullableString(state.formModel.spj_source),
      interactor_language_id: nullableString(state.formModel.interactor_language_id),
      interactor_source: nullableString(state.formModel.interactor_source),
      remote_provider: nullableString(state.formModel.remote_provider),
      remote_problem_id: nullableString(state.formModel.remote_problem_id),
      extra: parseJson(state.formModel.extra),
    }
    if (state.dataId) {
      await ojProblemApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojProblemApi.create(payload)
      window.$message.success('创建成功')
    }
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function nullableString(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function parseJson(value: string) {
  try {
    return JSON.parse(value || '{}')
  } catch {
    return {}
  }
}
</script>

<template>
  <ProCard class="min-h-0 flex-1" content-class="h-full min-h-0" :show-collapse="false">
    <template #header>
      <NFlex align="center" justify="space-between" :wrap="false">
        <NFlex align="center" :size="8">
          <NButton text title="返回" aria-label="返回" @click="emit('cancel')">
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:back" /></NIcon>
            </template>
          </NButton>
          <span>{{ panelTitle }}</span>
        </NFlex>
        <NFlex>
          <NButton @click="emit('cancel')">取消</NButton>
          <NButton type="primary" :loading="state.submitLoading" @click="submitForm">保存</NButton>
        </NFlex>
      </NFlex>
    </template>

    <NSpin :show="state.loading">
      <NScrollbar class="h-[calc(100vh-230px)] pr-16px">
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="150" :disabled="state.loading || state.submitLoading">
          <NTabs v-model:value="state.activeTab" type="line" animated>
            <NTabPane name="basic" tab="基础信息">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="code" path="code"><NInput v-model:value="state.formModel.code" /></NFormItem></NGi>
                <NGi><NFormItem label="title" path="title"><NInput v-model:value="state.formModel.title" /></NFormItem></NGi>
                <NGi><NFormItem label="difficulty" path="difficulty"><NInputNumber v-model:value="state.formModel.difficulty" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="source" path="source"><NInput v-model:value="state.formModel.source" /></NFormItem></NGi>
                <NGi><NFormItem label="problem_type" path="problem_type"><NSelect v-model:value="state.formModel.problem_type" :options="['PROGRAM','OUTPUT_ONLY','FUNCTION','INTERACTIVE','OBJECTIVE'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="judge_mode" path="judge_mode"><NSelect v-model:value="state.formModel.judge_mode" :options="['STANDARD','SPECIAL_JUDGE','INTERACTIVE','OUTPUT_ONLY','FUNCTION','OBJECTIVE','REMOTE'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="visibility" path="visibility"><NSelect v-model:value="state.formModel.visibility" :options="['PUBLIC','PRIVATE','CONTEST_ONLY','ORG_ONLY'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="status" path="status"><NSelect v-model:value="state.formModel.status" :options="['ENABLED','DISABLED'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="sort" path="sort"><NInputNumber v-model:value="state.formModel.sort" class="w-full" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="summary" path="summary"><NInput v-model:value="state.formModel.summary" /></NFormItem>
            </NTabPane>

            <NTabPane name="statement" tab="题面内容">
              <NFormItem label="description" path="description"><NInput v-model:value="state.formModel.description" type="textarea" :autosize="{ minRows: 10, maxRows: 18 }" /></NFormItem>
              <NFormItem label="input_description" path="input_description"><NInput v-model:value="state.formModel.input_description" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" /></NFormItem>
              <NFormItem label="output_description" path="output_description"><NInput v-model:value="state.formModel.output_description" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" /></NFormItem>
            </NTabPane>

            <NTabPane name="judge" tab="评测配置">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="time_limit_ms" path="time_limit_ms"><NInputNumber v-model:value="state.formModel.time_limit_ms" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="memory_limit_kb" path="memory_limit_kb"><NInputNumber v-model:value="state.formModel.memory_limit_kb" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="stack_limit_kb" path="stack_limit_kb"><NInputNumber v-model:value="state.formModel.stack_limit_kb" class="w-full" clearable /></NFormItem></NGi>
                <NGi><NFormItem label="output_limit_kb" path="output_limit_kb"><NInputNumber v-model:value="state.formModel.output_limit_kb" class="w-full" clearable /></NFormItem></NGi>
                <NGi><NFormItem label="points" path="points"><NInputNumber v-model:value="state.formModel.points" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="partial" path="partial"><NSwitch v-model:value="state.formModel.partial" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="allow_languages" path="allow_languages"><NInput v-model:value="state.formModel.allow_languages" placeholder="cpp17,python3" /></NFormItem>
            </NTabPane>

            <NTabPane name="advanced" tab="特殊判题">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="spj_language_id" path="spj_language_id"><NInput v-model:value="state.formModel.spj_language_id" /></NFormItem></NGi>
                <NGi><NFormItem label="interactor_language_id" path="interactor_language_id"><NInput v-model:value="state.formModel.interactor_language_id" /></NFormItem></NGi>
                <NGi><NFormItem label="remote_provider" path="remote_provider"><NInput v-model:value="state.formModel.remote_provider" /></NFormItem></NGi>
                <NGi><NFormItem label="remote_problem_id" path="remote_problem_id"><NInput v-model:value="state.formModel.remote_problem_id" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="spj_source" path="spj_source"><NInput v-model:value="state.formModel.spj_source" type="textarea" :autosize="{ minRows: 8, maxRows: 16 }" /></NFormItem>
              <NFormItem label="interactor_source" path="interactor_source"><NInput v-model:value="state.formModel.interactor_source" type="textarea" :autosize="{ minRows: 8, maxRows: 16 }" /></NFormItem>
            </NTabPane>

            <NTabPane name="stats" tab="统计扩展">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="accepted_count" path="accepted_count"><NInputNumber v-model:value="state.formModel.accepted_count" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="submit_count" path="submit_count"><NInputNumber v-model:value="state.formModel.submit_count" class="w-full" /></NFormItem></NGi>
                <NGi><NFormItem label="ac_rate" path="ac_rate"><NInputNumber v-model:value="state.formModel.ac_rate" class="w-full" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="extra" path="extra"><NInput v-model:value="state.formModel.extra" type="textarea" :autosize="{ minRows: 8, maxRows: 16 }" /></NFormItem>
            </NTabPane>
          </NTabs>
        </NForm>
      </NScrollbar>
    </NSpin>
  </ProCard>
</template>
