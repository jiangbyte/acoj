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
      <a-form-item
        label="通知标题"
        name="title"
        :rules="[{ required: true, message: '请输入通知标题' }]"
      >
        <a-input v-model:value="form.title" placeholder="请输入通知标题" />
      </a-form-item>
      <a-form-item
        label="通知类别"
        name="category"
        :rules="[{ required: true, message: '请选择通知类别' }]"
      >
        <DictSelect
          v-model="form.category"
          type-code="NOTICE_CATEGORY"
          placeholder="请选择通知类别"
        />
      </a-form-item>
      <a-form-item
        label="通知类型"
        name="type"
        :rules="[{ required: true, message: '请选择通知类型' }]"
      >
        <DictSelect v-model="form.type" type-code="NOTICE_TYPE" placeholder="请选择通知类型" />
      </a-form-item>
      <a-form-item label="通知级别" name="level">
        <DictSelect v-model="form.level" type-code="NOTICE_LEVEL" placeholder="请选择通知级别" />
      </a-form-item>
      <a-form-item label="通知摘要" name="summary">
        <a-textarea v-model:value="form.summary" placeholder="通知摘要" :rows="2" />
      </a-form-item>
      <a-form-item label="通知内容" name="content">
        <a-textarea v-model:value="form.content" placeholder="通知内容（支持 HTML 代码）" :rows="5" />
      </a-form-item>
      <a-form-item label="封面图片" name="cover">
        <a-input v-model:value="form.cover" placeholder="封面图片 URL" />
      </a-form-item>
      <a-form-item label="通知位置" name="position">
        <DictSelect
          v-model="form.position"
          type-code="NOTICE_POSITION"
          placeholder="请选择通知位置"
        />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="是否置顶" name="is_top">
            <DictSelect v-model="form.is_top" type-code="SYS_YES_NO" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="排序" name="sort_code">
            <a-input-number
              v-model:value="form.sort_code"
              :min="0"
              :max="9999"
              style="width: 100%"
              placeholder="排序值"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item label="状态" name="status">
        <DictSelect v-model="form.status" type-code="SYS_STATUS" placeholder="请选择状态" />
        <div class="text-[12px] text-gray-400 leading-tight mt-1">
          禁用后仅不可被选择，不影响已绑定的数据
        </div>
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchNoticeDetail, fetchNoticeCreate, fetchNoticeModify } from '@/api/notice'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  title: '',
  category: null,
  type: null,
  level: 'NORMAL',
  summary: '',
  content: '',
  cover: '',
  position: null,
  is_top: 'NO',
  sort_code: 0,
  status: 'ENABLED',
})

const form = reactive(initialForm())

async function doOpen(row?: any) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchNoticeDetail({ id: row.id })
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
    return await fetchNoticeModify({ ...f, id: currentId.value })
  } else {
    return await fetchNoticeCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
