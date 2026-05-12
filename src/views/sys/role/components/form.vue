<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑角色' : '新增角色'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="角色编码" name="code" :rules="[{ required: true, message: '请输入角色编码' }]">
        <a-input v-model:value="form.code" placeholder="请输入角色编码" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="角色名称" name="name" :rules="[{ required: true, message: '请输入角色名称' }]">
        <a-input v-model:value="form.name" placeholder="请输入角色名称" />
      </a-form-item>
      <a-form-item label="角色类别" name="category" :rules="[{ required: true, message: '请选择角色类别' }]">
        <a-select v-model:value="form.category" placeholder="请选择角色类别">
          <a-select-option value="ADMIN">管理</a-select-option>
          <a-select-option value="NORMAL">普通</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
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
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="角色描述" :rows="3" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { roleApi } from '@/api/role'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  code: '',
  name: '',
  category: undefined,
  sort_code: 0,
  status: 'ENABLED',
  description: '',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await roleApi.detail({ id: row.id })
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
    return await roleApi.modify({ ...f, id: currentId.value })
  } else {
    return await roleApi.create(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
