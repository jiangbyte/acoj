<template>
  <a-spin :spinning="loading">
    <a-form layout="vertical">
      <a-alert message="安全配置" type="info" show-icon class="mb-4" />
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="最大登录失败次数" name="SYS_MAX_LOGIN_RETRIES">
            <a-input-number
              v-model:value="formData.SYS_MAX_LOGIN_RETRIES"
              :min="0"
              :max="99"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="登录锁定时间（分钟）" name="SYS_LOGIN_LOCK_MINUTES">
            <a-input-number
              v-model:value="formData.SYS_LOGIN_LOCK_MINUTES"
              :min="0"
              :max="999"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="JWT Token 过期时间（秒）" name="SYS_JWT_TOKEN_EXPIRE">
            <a-input-number
              v-model:value="formData.SYS_JWT_TOKEN_EXPIRE"
              :min="0"
              :max="864000"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-space class="mt-4">
        <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </a-form>
  </a-spin>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { fetchConfigListByCategory, fetchConfigEditByCategory } from '@/api/config'

const loading = ref(false)
const saving = ref(false)
const initialData: Record<string, any> = {}
const formData = reactive<Record<string, any>>({})

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchConfigListByCategory({ category: 'SYS_SECURITY' })
    if (data) {
      Object.keys(formData).forEach(k => delete formData[k])
      data.forEach((item: any) => {
        formData[item.config_key] = item.config_value
        initialData[item.config_key] = item.config_value
      })
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const configs = Object.entries(formData).map(([key, value]) => ({
      config_key: key,
      config_value: String(value ?? ''),
    }))
    const { success } = await fetchConfigEditByCategory({ category: 'SYS_SECURITY', configs })
    if (success) {
      message.success('保存成功')
      Object.keys(formData).forEach(k => {
        initialData[k] = formData[k]
      })
    }
  } finally {
    saving.value = false
  }
}

function handleReset() {
  Object.keys(initialData).forEach(k => {
    formData[k] = initialData[k]
  })
}

onMounted(loadData)
</script>
