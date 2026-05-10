<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑轮播图' : '新增轮播图'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="标题" name="title" :rules="[{ required: true, message: '请输入标题' }]">
        <a-input v-model:value="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item label="图片" name="image" :rules="[{ required: true, message: '请输入图片地址' }]">
        <a-input v-model:value="form.image" placeholder="请输入图片URL" />
      </a-form-item>
      <a-form-item label="类别" name="category" :rules="[{ required: true, message: '请选择类别' }]">
        <a-select v-model:value="form.category" placeholder="请选择类别">
          <a-select-option value="HOME">首页</a-select-option>
          <a-select-option value="PAGE">页面</a-select-option>
          <a-select-option value="APP">应用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="类型" name="type" :rules="[{ required: true, message: '请选择类型' }]">
        <a-select v-model:value="form.type" placeholder="请选择类型">
          <a-select-option value="IMAGE">图片</a-select-option>
          <a-select-option value="VIDEO">视频</a-select-option>
          <a-select-option value="TEXT">文字</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="展示位置" name="position" :rules="[{ required: true, message: '请选择位置' }]">
        <a-select v-model:value="form.position" placeholder="请选择位置">
          <a-select-option value="TOP">顶部</a-select-option>
          <a-select-option value="CENTER">中间</a-select-option>
          <a-select-option value="BOTTOM">底部</a-select-option>
          <a-select-option value="SIDEBAR">侧栏</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
      </a-form-item>
      <a-form-item label="跳转地址" name="url">
        <a-input v-model:value="form.url" placeholder="可选" />
      </a-form-item>
      <a-form-item label="链接类型" name="link_type">
        <a-select v-model:value="form.link_type" placeholder="默认 URL">
          <a-select-option value="URL">URL</a-select-option>
          <a-select-option value="ROUTE">路由</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="摘要" name="summary">
        <a-textarea v-model:value="form.summary" :rows="2" placeholder="可选" />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" :rows="3" placeholder="可选" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchBannerDetail, fetchBannerCreate, fetchBannerModify } from '@/api/banner'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  title: '',
  image: '',
  category: undefined,
  type: undefined,
  position: undefined,
  url: '',
  link_type: 'URL',
  summary: '',
  description: '',
  sort_code: 0,
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchBannerDetail({ id: row.id })
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
    return await fetchBannerModify({ ...f, id: currentId.value })
  } else {
    return await fetchBannerCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
