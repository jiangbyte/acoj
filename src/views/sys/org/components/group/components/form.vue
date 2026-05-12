<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑用户组' : '新增用户组'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="上级用户组" name="parent_id">
        <a-tree-select
          v-model:value="form.parent_id"
          :tree-data="allGroupTreeData"
          :field-names="groupTreeFieldNames"
          placeholder="请选择上级用户组（先选所属组织）"
          allow-clear
          tree-default-expand-all
          :disabled="!form.org_id"
        />
      </a-form-item>
      <a-form-item label="用户组编码" name="code">
        <a-input v-model:value="form.code" placeholder="留空自动生成" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="用户组名称" name="name" :rules="[{ required: true, message: '请输入用户组名称' }]">
        <a-input v-model:value="form.name" placeholder="请输入用户组名称" />
      </a-form-item>
      <a-form-item label="用户组类别" name="category" :rules="[{ required: true, message: '请选择用户组类别' }]">
        <a-select v-model:value="form.category" placeholder="请选择用户组类别" allow-clear>
          <a-select-option value="ROLE">角色组</a-select-option>
          <a-select-option value="DEPT">部门组</a-select-option>
          <a-select-option value="PROJECT">项目组</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-select v-model:value="form.status" placeholder="请选择状态">
          <a-select-option value="ENABLED">启用</a-select-option>
          <a-select-option value="DISABLED">禁用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入用户组描述" :rows="3" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { fetchGroupDetail, fetchGroupCreate, fetchGroupModify, fetchGroupTree } from '@/api/group'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const allGroupTreeData = ref<any[]>([])
const groupTreeFieldNames = { children: 'children', label: 'name', value: 'id' }

// When org_id changes, filter the group tree to show only groups under that org

async function loadGroupTreeByOrg(orgId?: string) {
  const params: any = {}
  if (orgId) params.org_id = orgId
  const { data } = await fetchGroupTree(params)
  allGroupTreeData.value = [
    { id: '0', label: '顶级', children: data || [] },
  ]
}

const editLoading = ref(false)

const initialForm = () => ({
  parent_id: undefined as string | undefined,
  org_id: undefined as string | undefined,
  code: '',
  name: '',
  category: undefined,
  status: 'ENABLED',
  sort_code: 0,
  description: '',
})

const form = reactive(initialForm())

// When org changes, reload group tree and clear parent selection
watch(() => form.org_id, (newOrg) => {
  if (!editLoading.value) {
    form.parent_id = undefined
  }
  if (newOrg) {
    loadGroupTreeByOrg(newOrg)
  } else {
    allGroupTreeData.value = []
  }
})

async function doOpen(row?: any, parentId?: string, orgId?: string) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    editLoading.value = true
    const { data } = await fetchGroupDetail({ id: row.id })
    if (data) {
      Object.assign(form, data)
      // Load groups under the edit record's org
      if (data.org_id) {
        await loadGroupTreeByOrg(data.org_id)
      }
    }
    editLoading.value = false
  } else {
    isEdit.value = false
    currentId.value = null
    Object.assign(form, initialForm())
    if (orgId) {
      form.org_id = orgId
      await loadGroupTreeByOrg(orgId)
      if (parentId) form.parent_id = parentId
    }
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await fetchGroupModify({ ...f, id: currentId.value })
  } else {
    return await fetchGroupCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
