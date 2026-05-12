<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑用户' : '新增用户'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="账号" name="account" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="form.account" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item label="昵称" name="nickname">
        <a-input v-model:value="form.nickname" placeholder="请输入昵称" />
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value="form.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio value="ACTIVE">启用</a-radio>
          <a-radio value="INACTIVE">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserForm' })
import { reactive, ref } from 'vue'
import { userApi } from '@/api/user'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  account: '',
  nickname: '',
  email: '',
  phone: '',
  status: 'ACTIVE',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await userApi.detail({ id: row.id })
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
    return await userApi.modify({ ...f, id: currentId.value })
  } else {
    return await userApi.create(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
