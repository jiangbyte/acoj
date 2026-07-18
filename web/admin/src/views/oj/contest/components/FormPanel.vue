<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojContestApi } from '@/api'
import { createRequiredRule, formatDateTime } from '@/utils'
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
  key: '',
  name: '',
  description: '',
  summary: '',
  start_at: '',
  end_at: '',
  duration_seconds: null as number | null,
  visibility: 'PUBLIC',
  contest_format: 'ICPC',
  format_config: '{}',
  scoreboard_visibility: 'VISIBLE',
  is_rated: false,
  rating_floor: null as number | null,
  rating_ceiling: null as number | null,
  access_code_hash: '',
  allow_virtual: false,
  freeze_at: '',
  unfreeze_at: '',
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

const panelTitle = computed(() => (state.dataId ? '编辑竞赛' : '新增竞赛'))
const rules = computed<FormRules>(() => ({
  key: createRequiredRule('key', 'input'),
  name: createRequiredRule('name', 'input'),
  start_at: createRequiredRule('start_at', 'input'),
  end_at: createRequiredRule('end_at', 'input'),
  visibility: createRequiredRule('visibility', 'change'),
  contest_format: createRequiredRule('contest_format', 'change'),
  scoreboard_visibility: createRequiredRule('scoreboard_visibility', 'change'),
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
    const response = await ojContestApi.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      ...defaultFormData,
      ...data,
      description: data.description ?? '',
      summary: data.summary ?? '',
      start_at: formatDateTime(data.start_at, ''),
      end_at: formatDateTime(data.end_at, ''),
      access_code_hash: data.access_code_hash ?? '',
      freeze_at: formatDateTime(data.freeze_at, ''),
      unfreeze_at: formatDateTime(data.unfreeze_at, ''),
      format_config: JSON.stringify(data.format_config ?? {}, null, 2),
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
      description: nullableString(state.formModel.description),
      summary: nullableString(state.formModel.summary),
      access_code_hash: nullableString(state.formModel.access_code_hash),
      freeze_at: nullableString(state.formModel.freeze_at),
      unfreeze_at: nullableString(state.formModel.unfreeze_at),
      format_config: parseJson(state.formModel.format_config),
      extra: parseJson(state.formModel.extra),
    }
    if (state.dataId) {
      await ojContestApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojContestApi.create(payload)
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
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="170" :disabled="state.loading || state.submitLoading">
          <NTabs v-model:value="state.activeTab" type="line" animated>
            <NTabPane name="basic" tab="基础信息">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="key" path="key"><NInput v-model:value="state.formModel.key" /></NFormItem></NGi>
                <NGi><NFormItem label="name" path="name"><NInput v-model:value="state.formModel.name" /></NFormItem></NGi>
                <NGi><NFormItem label="visibility" path="visibility"><NSelect v-model:value="state.formModel.visibility" :options="['PUBLIC','PRIVATE','ORG_ONLY'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="status" path="status"><NSelect v-model:value="state.formModel.status" :options="['ENABLED','DISABLED'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="summary" path="summary"><NInput v-model:value="state.formModel.summary" /></NFormItem>
              <NFormItem label="description" path="description"><NInput v-model:value="state.formModel.description" type="textarea" :autosize="{ minRows: 8, maxRows: 16 }" /></NFormItem>
            </NTabPane>

            <NTabPane name="schedule" tab="时间访问">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="start_at" path="start_at"><NInput v-model:value="state.formModel.start_at" /></NFormItem></NGi>
                <NGi><NFormItem label="end_at" path="end_at"><NInput v-model:value="state.formModel.end_at" /></NFormItem></NGi>
                <NGi><NFormItem label="duration_seconds" path="duration_seconds"><NInputNumber v-model:value="state.formModel.duration_seconds" class="w-full" clearable /></NFormItem></NGi>
                <NGi><NFormItem label="allow_virtual" path="allow_virtual"><NSwitch v-model:value="state.formModel.allow_virtual" /></NFormItem></NGi>
                <NGi><NFormItem label="access_code_hash" path="access_code_hash"><NInput v-model:value="state.formModel.access_code_hash" /></NFormItem></NGi>
              </NGrid>
            </NTabPane>

            <NTabPane name="scoreboard" tab="赛制榜单">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="contest_format" path="contest_format"><NSelect v-model:value="state.formModel.contest_format" :options="['ICPC','IOI','OI','ACM','CUSTOM'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="scoreboard_visibility" path="scoreboard_visibility"><NSelect v-model:value="state.formModel.scoreboard_visibility" :options="['VISIBLE','AFTER_CONTEST','AFTER_PARTICIPATION','HIDDEN'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
                <NGi><NFormItem label="freeze_at" path="freeze_at"><NInput v-model:value="state.formModel.freeze_at" /></NFormItem></NGi>
                <NGi><NFormItem label="unfreeze_at" path="unfreeze_at"><NInput v-model:value="state.formModel.unfreeze_at" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="format_config" path="format_config"><NInput v-model:value="state.formModel.format_config" type="textarea" :autosize="{ minRows: 8, maxRows: 16 }" /></NFormItem>
            </NTabPane>

            <NTabPane name="rating" tab="评级配置">
              <NGrid :cols="2" :x-gap="16">
                <NGi><NFormItem label="is_rated" path="is_rated"><NSwitch v-model:value="state.formModel.is_rated" /></NFormItem></NGi>
                <NGi />
                <NGi><NFormItem label="rating_floor" path="rating_floor"><NInputNumber v-model:value="state.formModel.rating_floor" class="w-full" clearable /></NFormItem></NGi>
                <NGi><NFormItem label="rating_ceiling" path="rating_ceiling"><NInputNumber v-model:value="state.formModel.rating_ceiling" class="w-full" clearable /></NFormItem></NGi>
              </NGrid>
            </NTabPane>

            <NTabPane name="extra" tab="扩展信息">
              <NFormItem label="extra" path="extra"><NInput v-model:value="state.formModel.extra" type="textarea" :autosize="{ minRows: 10, maxRows: 18 }" /></NFormItem>
            </NTabPane>
          </NTabs>
        </NForm>
      </NScrollbar>
    </NSpin>
  </ProCard>
</template>
