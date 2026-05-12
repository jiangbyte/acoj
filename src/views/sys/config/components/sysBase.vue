<template>
  <a-spin :spinning="loading">
    <a-form layout="vertical">
      <a-alert message="系统基础配置" type="info" show-icon class="mb-4" />
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="默认文件引擎" name="SYS_DEFAULT_FILE_ENGINE">
            <a-select v-model:value="formData.SYS_DEFAULT_FILE_ENGINE">
              <a-select-option value="LOCAL">本地存储</a-select-option>
              <a-select-option value="ALIYUN">阿里云OSS</a-select-option>
              <a-select-option value="MINIO">MinIO</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Snowflake 工作节点ID" name="SYS_SNOWFLAKE_WORKER_ID">
            <a-input-number v-model:value="formData.SYS_SNOWFLAKE_WORKER_ID" :min="0" :max="31" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Snowflake 数据中心ID" name="SYS_SNOWFLAKE_DATACENTER_ID">
            <a-input-number v-model:value="formData.SYS_SNOWFLAKE_DATACENTER_ID" :min="0" :max="31" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="默认密码" name="SYS_DEFAULT_PASSWORD">
            <a-input v-model:value="formData.SYS_DEFAULT_PASSWORD" placeholder="新增用户时使用的默认密码" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="用户初始密码" name="SYS_USER_INIT_PASSWORD">
            <a-input v-model:value="formData.SYS_USER_INIT_PASSWORD" placeholder="用户首次登录密码" />
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
    const { data } = await fetchConfigListByCategory({ category: 'SYS_BASE' })
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
    const { success } = await fetchConfigEditByCategory({ category: 'SYS_BASE', configs })
    if (success) {
      message.success('保存成功')
      Object.keys(formData).forEach(k => { initialData[k] = formData[k] })
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
