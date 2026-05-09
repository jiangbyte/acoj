<template>
  <AppDrawerForm
    :open="open"
    :title="id ? '编辑用户' : '新增用户'"
    :form="form"
    :onSubmit="handleSubmit"
    @close="closeDrawer"
    @success="emit('success')"
  >
    <template #default="{ form }">
      <a-form-item label="账号" name="account" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="form.account" />
      </a-form-item>
      <a-form-item label="昵称" name="nickname">
        <a-input v-model:value="form.nickname" />
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value="form.email" />
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
import { reactive, watch } from 'vue'
import { fetchUserDetail, fetchUserCreate, fetchUserModify } from '@/service/api/user'
import AppDrawerForm from '@/components/AppDrawerForm.vue'

const props = defineProps<{ open: boolean; id: string }>()
const emit = defineEmits(['update:open', 'success'])

const form = reactive({
  account: '', nickname: '', email: '', phone: '', status: 'ACTIVE',
})

watch(() => props.open, async (v) => {
  if (v && props.id) {
    const { data } = await fetchUserDetail({ id: props.id })
    if (data) Object.assign(form, data)
  } else {
    Object.assign(form, { account: '', nickname: '', email: '', phone: '', status: 'ACTIVE' })
  }
})

async function handleSubmit(f: any) {
  if (props.id) {
    return await fetchUserModify({ ...f, id: props.id })
  } else {
    return await fetchUserCreate(f)
  }
}

function closeDrawer() { emit('update:open', false) }
</script>
