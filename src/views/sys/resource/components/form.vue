<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑资源' : (parentId ? '新增子级资源' : '新增资源')"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="资源名称" name="name" :rules="[{ required: true, message: '请输入资源名称' }]">
        <a-input v-model:value="form.name" placeholder="请输入资源名称" />
      </a-form-item>
      <a-form-item label="资源编码" name="code" :rules="[{ required: true, message: '请输入资源编码' }]">
        <a-input v-model:value="form.code" placeholder="请输入资源编码" :disabled="isEdit" />
      </a-form-item>
      <a-form-item label="资源分类" name="category" :rules="[{ required: true, message: '请选择资源分类' }]">
        <a-select v-model:value="form.category" placeholder="请选择资源分类">
          <a-select-option value="BACKEND_MENU">后台菜单</a-select-option>
          <a-select-option value="FRONTEND_MENU">前台菜单</a-select-option>
          <a-select-option value="BACKEND_BUTTON">后台按钮</a-select-option>
          <a-select-option value="FRONTEND_BUTTON">前台按钮</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="资源类型" name="type" :rules="[{ required: true, message: '请选择资源类型' }]">
        <a-select v-model:value="form.type" placeholder="请选择资源类型">
          <a-select-option value="DIRECTORY">目录</a-select-option>
          <a-select-option value="MENU">菜单</a-select-option>
          <a-select-option value="BUTTON">按钮</a-select-option>
          <a-select-option value="INTERNAL_LINK">内链</a-select-option>
          <a-select-option value="EXTERNAL_LINK">外链</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="父资源" name="parent_id">
        <a-tree-select
          v-model:value="form.parent_id"
          :tree-data="resourceTreeData"
          :field-names="{ children: 'children', label: 'name', value: 'id' }"
          placeholder="无（根级）"
          allow-clear
          :disabled="!!parentId"
        />
      </a-form-item>
      <a-form-item label="路由路径" name="route_path">
        <a-input v-model:value="form.route_path" placeholder="如 /sys/user" />
      </a-form-item>
      <a-form-item label="组件路径" name="component_path">
        <a-input v-model:value="form.component_path" placeholder="如 sys/user/index" />
      </a-form-item>
      <a-form-item label="重定向" name="redirect_path">
        <a-input v-model:value="form.redirect_path" placeholder="重定向路径" />
      </a-form-item>
      <a-form-item label="图标" name="icon">
        <a-input v-model:value="form.icon" placeholder="图标名称" />
      </a-form-item>
      <a-form-item label="颜色" name="color">
        <a-input v-model:value="form.color" placeholder="资源颜色" />
      </a-form-item>
      <a-form-item label="外链地址" name="external_url">
        <a-input v-model:value="form.external_url" placeholder="https://" />
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
        <a-textarea v-model:value="form.description" placeholder="资源描述" :rows="3" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchResourceDetail, fetchResourceCreate, fetchResourceModify, fetchResourceTree } from '@/api/resource'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)
const parentId = ref<string | undefined>(undefined)
const resourceTreeData = ref<any[]>([])

const initialForm = () => ({
  code: '',
  name: '',
  category: undefined,
  type: undefined,
  parent_id: undefined as string | undefined,
  route_path: '',
  component_path: '',
  redirect_path: '',
  icon: '',
  color: '',
  external_url: '',
  sort_code: 0,
  status: 'ENABLED',
  description: '',
})

const form = reactive(initialForm())

async function loadTree() {
  try {
    const { data } = await fetchResourceTree()
    resourceTreeData.value = data || []
  } catch { /* ignore */ }
}

async function doOpen(row?: any, pId?: string) {
  await loadTree()
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    parentId.value = undefined
    const { data } = await fetchResourceDetail({ id: row.id })
    if (data) {
      Object.assign(form, data)
    }
  } else {
    isEdit.value = false
    currentId.value = null
    parentId.value = pId
    Object.assign(form, initialForm())
    if (pId) form.parent_id = pId
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await fetchResourceModify({ ...f, id: currentId.value })
  } else {
    return await fetchResourceCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
