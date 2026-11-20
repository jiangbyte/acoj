<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysArticleFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysArticleDefaultData, sysArticleAdd, sysArticleEdit } = useSysArticleFetch()
const formData = ref<any>({ ...sysArticleDefaultData })
const rules = {
  title: [
    { required: true, message: 'Enter Title', trigger: ['input', 'blur'] },
  ],
  subtitle: [
    { required: true, message: 'Enter Subtitle', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: 'Enter Cover', trigger: ['input', 'blur'] },
  ],
  author: [
    { required: true, message: 'Enter Author', trigger: ['input', 'blur'] },
  ],
  summary: [
    { required: true, message: 'Enter Summary', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
  toUrl: [
    { required: true, message: 'Enter Link', trigger: ['input', 'blur'] },
  ],
  parentId: [
    { required: true, message: 'Enter Parent', trigger: ['input', 'blur'] },
  ],
  type: [
    { required: true, message: 'Enter Type', trigger: ['input', 'blur'] },
  ],
  category: [
    { required: true, message: 'Enter Category', trigger: ['input', 'blur'] },
  ],
  content: [
    { required: true, message: 'Enter Content', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysArticleDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysArticleEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysArticleAdd(formData.value)
        if (success) {
          window.$message.success('Created successfully')
        }
      }
      emit('submit', true)
      doClose()
      show.value = false
      loading.value = false
    }
    else {
      //
    }
  })
}

function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Title" path="title">
          <NInput v-model:value="formData.title" placeholder="Enter Title" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Subtitle" path="subtitle">
          <NInput v-model:value="formData.subtitle" placeholder="Enter Subtitle" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Cover" path="cover">
          <NInput v-model:value="formData.cover" placeholder="Enter Cover" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Author" path="author">
          <NInput v-model:value="formData.author" placeholder="Enter Author" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Summary" path="summary">
          <NInput v-model:value="formData.summary" placeholder="Enter Summary" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Sort Order" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="Enter Sort Order" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Link" path="toUrl">
          <NInput v-model:value="formData.toUrl" placeholder="Enter Link" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Parent" path="parentId">
          <NInput v-model:value="formData.parentId" placeholder="Enter Parent" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Type" path="type">
          <NInput v-model:value="formData.type" placeholder="Enter Type" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Category" path="category">
          <NInput v-model:value="formData.category" placeholder="Enter Category" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Content" path="content">
          <NInput v-model:value="formData.content" placeholder="Enter Content" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            Save
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
