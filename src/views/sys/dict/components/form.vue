<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑字典' : '新增字典'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="上级字典" name="parent_id">
        <a-tree-select
          v-model:value="form.parent_id"
          :tree-data="treeData"
          :field-names="treeFieldNames"
          placeholder="请选择上级字典"
          allow-clear
          tree-default-expand-all
        />
      </a-form-item>
      <a-form-item label="字典编码" name="code">
        <a-input v-model:value="form.code" placeholder="留空自动生成" :disabled="isEdit" />
      </a-form-item>
      <a-form-item
        label="字典标签"
        name="label"
        :rules="[{ required: true, message: '请输入字典标签' }]"
      >
        <a-input v-model:value="form.label" placeholder="请输入字典标签" />
      </a-form-item>
      <a-form-item label="字典值" name="value">
        <a-input v-model:value="form.value" placeholder="请输入字典值" />
      </a-form-item>
      <a-form-item label="颜色" name="color">
        <a-select v-model:value="form.color" placeholder="请选择颜色" allow-clear>
          <a-select-option v-for="c in colorOptions" :key="c" :value="c">
            <a-tag :color="c">{{ c }}</a-tag>
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="分类" name="category">
        <DictSelect v-model="form.category" type-code="DICT_CATEGORY" placeholder="请选择分类" />
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
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchDictDetail, fetchDictCreate, fetchDictModify, fetchDictTree } from '@/api/dict'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const colorOptions = [
  'default',
  'pink',
  'red',
  'orange',
  'green',
  'cyan',
  'blue',
  'purple',
  'gold',
  'geekblue',
  'volcano',
  'magenta',
  'processing',
  'success',
  'error',
  'warning',
]

const treeData = ref<any[]>([])
const treeFieldNames = { children: 'children', label: 'label', value: 'id' }

const initialForm = () => ({
  parent_id: undefined as string | undefined,
  code: '',
  label: '',
  value: '',
  color: undefined,
  category: undefined,
  status: 'ENABLED',
  sort_code: 0,
})

const form = reactive(initialForm())

async function loadTree() {
  const { data } = await fetchDictTree({})
  treeData.value = [{ id: '0', label: '顶级' }, ...(data || [])]
}

async function doOpen(row?: any, parentId?: string) {
  await loadTree()
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchDictDetail({ id: row.id })
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
    return await fetchDictModify({ ...f, id: currentId.value })
  } else {
    return await fetchDictCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
