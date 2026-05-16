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
      <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
        <a-input v-model:value="form.username" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="昵称" name="nickname">
        <a-input v-model:value="form.nickname" placeholder="请输入昵称" />
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value="form.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item label="性别" name="gender">
        <DictSelect v-model="form.gender" type-code="GENDER" placeholder="请选择性别" allow-clear />
      </a-form-item>
      <a-form-item label="座右铭" name="motto">
        <a-input v-model:value="form.motto" placeholder="请输入座右铭" />
      </a-form-item>
      <a-form-item label="GitHub" name="github">
        <a-input v-model:value="form.github" placeholder="请输入 GitHub 地址" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <DictSelect v-model="form.status" type-code="USER_STATUS" option-type="radio" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
defineOptions({ name: 'ClientUserForm' })
import { reactive, ref } from 'vue'
import { fetchClientUserDetail, fetchClientUserCreate, fetchClientUserModify } from '@/api/client-user'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  username: '',
  nickname: '',
  email: '',
  gender: undefined,
  motto: '',
  github: '',
  status: 'ACTIVE',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchClientUserDetail({ id: row.id })
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
    return await fetchClientUserModify({ ...f, id: currentId.value })
  } else {
    return await fetchClientUserCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
