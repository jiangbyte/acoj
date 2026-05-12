<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑配置' : '新增配置'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="配置键" name="config_key" :rules="[{ required: true, message: '请输入配置键' }]">
        <a-input v-model:value="form.config_key" placeholder="请输入配置键" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="配置值" name="config_value" :rules="[{ required: true, message: '请输入配置值' }]">
        <a-input v-model:value="form.config_value" placeholder="请输入配置值" />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-input v-model:value="form.remark" placeholder="备注说明" />
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { configApi } from '@/api/config'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  config_key: '',
  config_value: '',
  remark: '',
  sort_code: 0,
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await configApi.detail({ id: row.id })
    if (data) Object.assign(form, data)
  } else {
    isEdit.value = false
    currentId.value = null
    Object.assign(form, initialForm())
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await configApi.modify({ ...f, id: currentId.value })
  } else {
    return await configApi.create({ ...f, category: 'BIZ_DEFINE' })
  }
}

function handleClose() { emit('update:open', false) }

defineExpose({ doOpen })
</script>
