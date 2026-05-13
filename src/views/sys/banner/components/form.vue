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
      <a-form-item
        label="图片"
        name="image"
        :rules="[{ required: true, message: '请输入图片地址' }]"
      >
        <a-input v-model:value="form.image" placeholder="请输入图片URL" />
      </a-form-item>
      <a-form-item
        label="类别"
        name="category"
        :rules="[{ required: true, message: '请选择类别' }]"
      >
        <DictSelect v-model="form.category" type-code="BANNER_CATEGORY" placeholder="请选择类别" />
      </a-form-item>
      <a-form-item label="类型" name="type" :rules="[{ required: true, message: '请选择类型' }]">
        <DictSelect v-model="form.type" type-code="BANNER_TYPE" placeholder="请选择类型" />
      </a-form-item>
      <a-form-item
        label="展示位置"
        name="position"
        :rules="[{ required: true, message: '请选择位置' }]"
      >
        <DictSelect v-model="form.position" type-code="BANNER_POSITION" placeholder="请选择位置" />
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
      <a-form-item label="跳转地址" name="url">
        <a-input v-model:value="form.url" placeholder="可选" />
      </a-form-item>
      <a-form-item label="链接类型" name="link_type">
        <DictSelect v-model="form.link_type" type-code="LINK_TYPE" placeholder="默认 URL" />
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
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  title: '',
  image: '',
  category: null,
  type: null,
  position: null,
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
