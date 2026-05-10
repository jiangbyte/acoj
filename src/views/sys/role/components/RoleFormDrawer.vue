<template>
  <AppDrawerForm
    :open="open"
    :title="id ? '编辑角色' : '新增角色'"
    :form="form"
    :onSubmit="handleSubmit"
    @close="closeDrawer"
    @success="emit('success')"
  >
    <template #default="{ form }">
      <a-form-item label="角色编码" name="code" :rules="[{ required: true, message: '请输入角色编码' }]">
        <a-input v-model:value="form.code" />
      </a-form-item>
      <a-form-item label="角色名称" name="name" :rules="[{ required: true, message: '请输入角色名称' }]">
        <a-input v-model:value="form.name" />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { fetchRoleDetail, fetchRoleCreate, fetchRoleModify } from '@/api/role'
import AppDrawerForm from '@/components/AppDrawerForm.vue'

const props = defineProps<{ open: boolean; id: string }>()
const emit = defineEmits(['update:open', 'success'])

const form = reactive({ code: '', name: '', description: '' })

watch(() => props.open, async (v) => {
  if (v && props.id) {
    const { data } = await fetchRoleDetail({ id: props.id })
    if (data) Object.assign(form, data)
  } else {
    Object.assign(form, { code: '', name: '', description: '' })
  }
})

async function handleSubmit(f: any) {
  if (props.id) {
    return await fetchRoleModify({ ...f, id: props.id })
  } else {
    return await fetchRoleCreate(f)
  }
}

function closeDrawer() { emit('update:open', false) }
</script>
