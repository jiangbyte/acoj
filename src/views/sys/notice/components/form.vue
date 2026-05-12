<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑通知' : '新增通知'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="通知标题" name="title" :rules="[{ required: true, message: '请输入通知标题' }]">
        <a-input v-model:value="form.title" placeholder="请输入通知标题" />
      </a-form-item>
      <a-form-item label="通知类别" name="category" :rules="[{ required: true, message: '请选择通知类别' }]">
        <a-select v-model:value="form.category" placeholder="请选择通知类别">
          <a-select-option value="NOTICE">通知</a-select-option>
          <a-select-option value="NEWS">新闻</a-select-option>
          <a-select-option value="MESSAGE">消息</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="通知类型" name="type" :rules="[{ required: true, message: '请输入通知类型' }]">
        <a-input v-model:value="form.type" placeholder="如 SYSTEM、PLATFORM" />
      </a-form-item>
      <a-form-item label="通知级别" name="level">
        <a-select v-model:value="form.level" placeholder="请选择通知级别">
          <a-select-option value="NORMAL">普通</a-select-option>
          <a-select-option value="IMPORTANT">重要</a-select-option>
          <a-select-option value="URGENT">紧急</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="通知摘要" name="summary">
        <a-textarea v-model:value="form.summary" placeholder="通知摘要" :rows="2" />
      </a-form-item>
      <a-form-item label="通知内容" name="content">
        <a-textarea v-model:value="form.content" placeholder="通知内容（支持 HTML）" :rows="5" />
      </a-form-item>
      <a-form-item label="封面图片" name="cover">
        <a-input v-model:value="form.cover" placeholder="封面图片 URL" />
      </a-form-item>
      <a-form-item label="通知位置" name="position">
        <a-input v-model:value="form.position" placeholder="通知位置" />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="是否置顶" name="is_top">
            <a-select v-model:value="form.is_top">
              <a-select-option value="NO">否</a-select-option>
              <a-select-option value="YES">是</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="排序" name="sort_code">
            <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item label="状态" name="status">
        <a-select v-model:value="form.status" placeholder="请选择状态">
          <a-select-option value="ENABLED">启用</a-select-option>
          <a-select-option value="DISABLED">禁用</a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { noticeApi } from '@/api/notice'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  title: '',
  category: undefined,
  type: '',
  level: 'NORMAL',
  summary: '',
  content: '',
  cover: '',
  position: '',
  is_top: 'NO',
  sort_code: 0,
  status: 'ENABLED',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await noticeApi.detail({ id: row.id })
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
    return await noticeApi.modify({ ...f, id: currentId.value })
  } else {
    return await noticeApi.create(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
