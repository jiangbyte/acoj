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
      <a-form-item
        label="组织名称"
        name="name"
        :rules="[{ required: true, message: '请输入组织名称' }]"
      >
        <a-input v-model:value="form.name" placeholder="请输入组织名称" />
      </a-form-item>
      <a-form-item
        label="组织类别"
        name="category"
        :rules="[{ required: true, message: '请选择组织类别' }]"
      >
        <DictSelect v-model="form.category" type-code="ORG_CATEGORY" placeholder="请选择组织类别" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <DictSelect v-model="form.status" type-code="SYS_STATUS" placeholder="请选择状态" />
        <div class="text-[12px] text-gray-400 leading-tight mt-1">
          禁用后仅不可被选择，不影响已绑定的数据
        </div>
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number
          v-model:value="form.sort_code"
          :min="0"
          :max="9999"
          style="width: 100%"
          placeholder="排序值"
        />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入组织描述" :rows="3" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchOrgDetail, fetchOrgCreate, fetchOrgModify, fetchOrgTree } from '@/api/org'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const treeData = ref<any[]>([])
const treeFieldNames = { children: 'children', label: 'name', value: 'id' }

const initialForm = () => ({
  parent_id: '0',
  code: '',
  name: '',
  category: undefined,
  status: 'ENABLED',
  sort_code: 0,
  description: '',
})

const form = reactive(initialForm())

async function loadTree() {
  const { data } = await fetchOrgTree({})
  treeData.value = [
    {
      id: '0',
      name: '顶级',
      children: null,
    },
    ...data,
  ]
}

async function doOpen(row?: any, parentId?: string) {
  await loadTree()
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchOrgDetail({ id: row.id })
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
    return await fetchOrgModify({ ...f, id: currentId.value })
  } else {
    return await fetchOrgCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
