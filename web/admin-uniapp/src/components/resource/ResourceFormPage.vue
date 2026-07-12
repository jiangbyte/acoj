<template>
  <Layout :title="pageTitle" back>
    <view class="resource-form">
      <u-card :show-head="false">
        <template #body>
          <FormFields v-model="form" :fields="config.formFields" :mode="mode" />
        </template>
        <template #foot>
          <u-button
            :text="mode === 'create' ? '创建' : '保存'"
            type="primary"
            :loading="loading"
            @click="submit"
          />
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import FormFields from '@/components/common/FormFields.vue'
import {
  adminResourceApis,
  resourceConfigs,
  type FieldConfig,
  type ResourceKey,
} from '@/config/resource'
import { formatDateTime } from '@/utils/format'
import { encryptPasswords } from '@/utils/security'

const props = defineProps<{
  resourceKey: ResourceKey
}>()

const mode = ref<'create' | 'update'>('create')
const id = ref('')
const detailParams = ref<Record<string, any>>({})
const form = ref<Record<string, any>>({})
const loading = ref(false)
const config = computed(() => resourceConfigs[props.resourceKey])
const api = computed<any>(() => adminResourceApis[props.resourceKey])
const pageTitle = computed(
  () => `${mode.value === 'create' ? '新增' : '编辑'}${config.value.title}`
)

onLoad(async (query: any) => {
  mode.value = query.mode === 'update' ? 'update' : 'create'
  id.value = query.id || ''
  detailParams.value = buildDetailParams(query)
  initDefaults()
  if (mode.value === 'update' && id.value) {
    const detail = await api.value.detail(detailParams.value)
    form.value = { ...form.value, ...normalizeDetail(detail) }
  }
})

function buildDetailParams(query: Record<string, any>) {
  const params: Record<string, any> = { id: query.id || id.value }
  const contextKeys = [
    'account_type',
    'account_id',
    'target_account_type',
    'target_account_id',
  ]
  contextKeys.forEach((key) => {
    if (query[key] !== undefined && query[key] !== null && query[key] !== '') {
      params[key] = query[key]
    }
  })
  return params
}

function initDefaults() {
  const model: Record<string, any> = {}
  config.value.formFields.forEach((field) => {
    if (field.defaultValue !== undefined) {
      model[field.prop] = field.defaultValue
    }
  })
  form.value = model
}

async function submit() {
  if (!api.value.create && mode.value === 'create') {
    uni.showToast({ title: '当前资源不支持新增', icon: 'none' })
    return
  }
  if (!api.value.update && mode.value === 'update') {
    uni.showToast({ title: '当前资源不支持编辑', icon: 'none' })
    return
  }
  const fields = config.value.formFields.filter(isVisibleField)
  const missing = fields.find(
    (field) => field.required && isEmpty(form.value[field.prop])
  )
  if (missing) {
    uni.showToast({ title: `请填写${missing.label}`, icon: 'none' })
    return
  }

  loading.value = true
  try {
    const payload = await buildPayload()
    if (mode.value === 'create') {
      await api.value.create(payload)
    } else {
      await api.value.update({ ...payload, id: id.value })
    }
    uni.showToast({ title: '已保存', icon: 'success' })
    uni.navigateBack()
  } finally {
    loading.value = false
  }
}

function normalizeDetail(data: any) {
  const detail = Array.isArray(data?.records) ? (data.records[0] ?? {}) : data
  return normalizeDateTimeFields(detail ?? {})
}

function normalizeDateTimeFields(detail: Record<string, any>) {
  const normalized = { ...detail }
  config.value.formFields.forEach((field) => {
    if (isDateTimeField(field) && normalized[field.prop] !== undefined) {
      normalized[field.prop] = formatDateTime(normalized[field.prop], '')
    }
  })
  return normalized
}

function isDateTimeField(field: FieldConfig) {
  return field.type === 'datetime' || field.prop.endsWith('_at') || field.prop.endsWith('_time')
}

function isVisibleField(field: FieldConfig) {
  if (mode.value === 'create' && field.updateOnly) {
    return false
  }
  if (mode.value === 'update' && field.createOnly) {
    return false
  }
  return field.type !== 'hidden'
}

async function buildPayload() {
  const payload = { ...form.value }
  Object.keys(payload).forEach((key) => {
    if (payload[key] === '') {
      payload[key] = null
    }
  })
  if (props.resourceKey === 'account' && payload.password) {
    const security = await encryptPasswords({ password: payload.password })
    payload.password = security.values.password
    payload.password_key_id = security.password_key_id
  }
  return payload
}

function isEmpty(value: unknown) {
  return value === undefined || value === null || value === ''
}
</script>

<style lang="scss" scoped>
.resource-form {
  padding-top: var(--space-3);
}
</style>
