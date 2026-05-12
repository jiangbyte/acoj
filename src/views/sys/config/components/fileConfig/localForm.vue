<template>
  <a-spin :spinning="loading">
    <a-form layout="vertical">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12">
          <a-form-item label="本地存储路径（Windows）" name="SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS">
            <a-input v-model:value="formData.SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS" placeholder="D:/hei-file-upload" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12">
          <a-form-item label="本地存储路径（Unix）" name="SYS_FILE_LOCAL_FOLDER_FOR_UNIX">
            <a-input v-model:value="formData.SYS_FILE_LOCAL_FOLDER_FOR_UNIX" placeholder="/data/hei-file-upload" />
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
    const { data } = await fetchConfigListByCategory({ category: 'FILE_LOCAL' })
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
    const { success } = await fetchConfigEditByCategory({ category: 'FILE_LOCAL', configs })
    if (success) {
      message.success('保存成功')
      Object.keys(formData).forEach(k => { initialData[k] = formData[k] })
    }
  } finally {
    saving.value = false
  }
}

function handleReset() {
  Object.keys(initialData).forEach(k => { formData[k] = initialData[k] })
}

onMounted(loadData)
</script>
