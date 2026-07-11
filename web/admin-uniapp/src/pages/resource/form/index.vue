<template>
  <Layout :title="pageTitle" back>
    <view>
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
          ></u-button>
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
import { encryptPasswords } from '@/utils/security'

const resource = ref<ResourceKey>('account')
const mode = ref<'create' | 'update'>('create')
const id = ref('')
const form = ref<Record<string, any>>({})
const loading = ref(false)
const config = computed(() => resourceConfigs[resource.value])
const api = computed<any>(() => adminResourceApis[resource.value])
const pageTitle = computed(
  () => `${mode.value === 'create' ? '新增' : '编辑'}${config.value.title}`
)

onLoad(async (query: any) => {
  resource.value = query.resource || 'account'
  mode.value = query.mode === 'update' ? 'update' : 'create'
  id.value = query.id || ''
  initDefaults()
  if (mode.value === 'update' && id.value) {
    const detail = await api.value.detail({ id: id.value })
    form.value = { ...form.value, ...detail }
  }
})

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
  const fields = config.value.formFields.filter((field) =>
    isVisibleField(field)
  )
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
  if (resource.value === 'account' && payload.password) {
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
