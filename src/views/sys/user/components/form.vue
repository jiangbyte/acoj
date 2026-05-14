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
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="账号" name="account" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="form.account" placeholder="请输入账号" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="昵称" name="nickname">
            <a-input v-model:value="form.nickname" placeholder="请输入昵称" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="邮箱" name="email">
            <a-input v-model:value="form.email" placeholder="请输入邮箱" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="手机" name="phone">
            <a-input v-model:value="form.phone" placeholder="请输入手机号" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-divider class="my-3" />

      <a-row :gutter="16">
        <a-col :span="8">
          <a-form-item label="组织" name="org_id">
            <a-tree-select
              v-model:value="form.org_id"
              :tree-data="orgTree"
              placeholder="请选择组织"
              allow-clear
              tree-default-expand-all
              @change="handleOrgChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label="用户组" name="group_id">
            <a-tree-select
              v-model:value="form.group_id"
              :tree-data="groupTree"
              placeholder="请先选择组织"
              allow-clear
              :disabled="!form.org_id"
              @change="handleGroupChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label="职位" name="position_id">
            <a-select
              v-model:value="form.position_id"
              :options="positionOptions"
              placeholder="请先选择用户组"
              allow-clear
              :disabled="!form.group_id"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-divider class="my-3" />

      <a-form-item label="状态" name="status">
        <DictSelect v-model="form.status" type-code="USER_STATUS" option-type="radio" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserForm' })
import { reactive, ref } from 'vue'
import { fetchUserDetail, fetchUserCreate, fetchUserModify } from '@/api/user'
import { fetchOrgTree } from '@/api/org'
import { fetchGroupTree } from '@/api/group'
import { fetchPositionPage } from '@/api/position'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  account: '',
  nickname: '',
  email: '',
  phone: '',
  org_id: undefined,
  group_id: undefined,
  position_id: undefined,
  status: 'ACTIVE',
})

const form = reactive(initialForm())

const orgTree = ref<any[]>([])
const groupTree = ref<any[]>([])
const positionOptions = ref<{ label: string; value: string }[]>([])

function toTreeData(nodes: any[]): any[] {
  return (nodes || []).map((n: any) => ({
    title: n.name,
    value: n.id,
    key: n.id,
    children: toTreeData(n.children),
  }))
}

async function handleOrgChange() {
  form.group_id = undefined
  form.position_id = undefined
  groupTree.value = []
  positionOptions.value = []
  if (form.org_id) {
    const { data } = await fetchGroupTree({ org_id: form.org_id })
    groupTree.value = toTreeData(data || [])
  }
}

async function handleGroupChange() {
  form.position_id = undefined
  positionOptions.value = []
  if (form.group_id) {
    const { data } = await fetchPositionPage({ group_id: form.group_id, size: 9999 })
    positionOptions.value = (data?.records || []).map((r: any) => ({
      label: r.name,
      value: r.id,
    }))
  }
}

async function doOpen(row?: any) {
  // Load org tree
  const { data: orgData } = await fetchOrgTree({})
  orgTree.value = toTreeData(orgData || [])

  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchUserDetail({ id: row.id })
    if (data) {
      Object.assign(form, data)

      // Cascade: load groups for the user's org
      if (data.org_id) {
        const { data: groups } = await fetchGroupTree({ org_id: data.org_id })
        groupTree.value = toTreeData(groups || [])
      }

      // Cascade: load positions for the user's group
      if (data.group_id) {
        const { data: positions } = await fetchPositionPage({ group_id: data.group_id, size: 9999 })
        positionOptions.value = (positions?.records || []).map((r: any) => ({
          label: r.name,
          value: r.id,
        }))
      }
    }
  } else {
    isEdit.value = false
    currentId.value = null
    Object.assign(form, initialForm())
    groupTree.value = []
    positionOptions.value = []
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  // Clean up undefined cascade fields
  const payload = { ...f }
  if (!payload.org_id) {
    payload.org_id = null
    payload.group_id = null
    payload.position_id = null
  } else if (!payload.group_id) {
    payload.group_id = null
    payload.position_id = null
  } else if (!payload.position_id) {
    payload.position_id = null
  }

  if (currentId.value) {
    return await fetchUserModify({ ...payload, id: currentId.value })
  } else {
    return await fetchUserCreate(payload)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
