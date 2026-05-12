<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑组织' : '新增组织'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="上级组织" name="parent_id">
        <a-tree-select
          v-model:value="form.parent_id"
          :tree-data="treeData"
          :field-names="treeFieldNames"
          placeholder="请选择上级组织"
          allow-clear
          tree-default-expand-all
        />
      </a-form-item>
      <a-form-item label="组织编码" name="code">
        <a-input v-model:value="form.code" placeholder="留空自动生成" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="组织名称" name="name" :rules="[{ required: true, message: '请输入组织名称' }]">
        <a-input v-model:value="form.name" placeholder="请输入组织名称" />
      </a-form-item>
      <a-form-item label="组织类别" name="category" :rules="[{ required: true, message: '请选择组织类别' }]">
        <a-select v-model:value="form.category" placeholder="请选择组织类别" allow-clear>
          <a-select-option value="COMPANY">公司</a-select-option>
          <a-select-option value="DEPT">部门</a-select-option>
          <a-select-option value="UNIT">单位</a-select-option>
          <a-select-option value="GROUP">集团</a-select-option>
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
        <a-textarea v-model:value="form.description" placeholder="请输入组织描述" :rows="3" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { orgApi } from '@/api/org'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const treeData = ref<any[]>([])
const treeFieldNames = { children: 'children', label: 'name', value: 'id' }

const initialForm = () => ({
  parent_id: undefined as string | undefined,
  code: '',
  name: '',
  category: undefined,
  status: 'ENABLED',
  sort_code: 0,
  description: '',
})

const form = reactive(initialForm())

async function loadTree() {
  const { data } = await orgApi.tree({})
  treeData.value = [
    { id: '0', label: '顶级', children: data || [] },
  ]
}

async function doOpen(row?: any, parentId?: string) {
  await loadTree()
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await orgApi.detail({ id: row.id })
    if (data) Object.assign(form, data)
  } else {
    isEdit.value = false
    currentId.value = null
    Object.assign(form, initialForm())
    if (parentId) form.parent_id = parentId
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await orgApi.modify({ ...f, id: currentId.value })
  } else {
    return await orgApi.create(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
