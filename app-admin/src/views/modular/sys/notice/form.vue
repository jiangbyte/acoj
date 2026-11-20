<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysNoticeFetch } from '@/composables/v1'
import MDEditor from '@/components/common/editor/md/MarkdownEditor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysNoticeDefaultData, sysNoticeAdd, sysNoticeEdit } = useSysNoticeFetch()
const formData = ref<any>({ ...sysNoticeDefaultData })
const rules = {
  title: [
    { required: true, message: 'Enter Title', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: 'Enter Cover', trigger: ['input', 'blur'] },
  ],
  url: [
    // { required: true, message: 'Enter Link', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
  content: [
    { required: true, message: 'Enter Content', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysNoticeDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysNoticeEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysNoticeAdd(formData.value)
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
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" :default-width="800" resizable @after-leave="doClose">
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
        <NFormItem label="Cover" path="cover">
          <FileUpload v-model="formData.cover" :is-image="true" />
        </NFormItem>
        <!-- Text input -->
        <!-- <NFormItem label="Link" path="url">
          <NInput v-model:value="formData.url" placeholder="Enter Link" />
        </NFormItem> -->
        <!-- Number input -->
        <NFormItem label="Sort Order" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="Enter Sort Order" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Content" path="content">
          <MDEditor v-model="formData.content" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Published" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
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
