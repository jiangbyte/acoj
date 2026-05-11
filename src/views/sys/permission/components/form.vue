<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑权限' : '新增权限'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="权限编码" name="code" :rules="[{ required: true, message: '请输入权限编码' }]">
        <a-input v-model:value="form.code" placeholder="如 sys:user:page" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="权限名称" name="name" :rules="[{ required: true, message: '请输入权限名称' }]">
        <a-input v-model:value="form.name" placeholder="请输入权限名称" />
      </a-form-item>
      <a-form-item label="所属模块" name="module">
        <a-input v-model:value="form.module" placeholder="如 sys:user" />
      </a-form-item>
      <a-form-item label="权限分类" name="category" :rules="[{ required: true, message: '请选择权限分类' }]">
        <a-select v-model:value="form.category" placeholder="请选择权限分类">
          <a-select-option value="BACKEND">后端权限</a-select-option>
          <a-select-option value="FRONTEND">前端权限</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-select v-model:value="form.status" placeholder="请选择状态">
          <a-select-option value="ENABLED">启用</a-select-option>
          <a-select-option value="DISABLED">禁用</a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
defineOptions({ name: 'PermissionForm' })
import { reactive, ref } from 'vue'
import { fetchPermissionDetail, fetchPermissionCreate, fetchPermissionModify } from '@/api/permission'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  code: '',
  name: '',
  module: '',
  category: 'BACKEND',
  sort_code: 0,
  status: 'ENABLED',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchPermissionDetail({ id: row.id })
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
    return await fetchPermissionModify({ ...f, id: currentId.value })
  } else {
    return await fetchPermissionCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
