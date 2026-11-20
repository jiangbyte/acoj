<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysLogFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysLogDefaultData, sysLogAdd, sysLogEdit } = useSysLogFetch()
const formData = ref<any>({ ...sysLogDefaultData })
const rules = {
  userId: [
    { required: true, message: 'Enter User ID', trigger: ['input', 'blur'] },
  ],
  operation: [
    { required: true, message: 'Enter Actions', trigger: ['input', 'blur'] },
  ],
  method: [
    { required: true, message: 'Enter Method', trigger: ['input', 'blur'] },
  ],
  params: [
    { required: true, message: 'Enter Parameters', trigger: ['input', 'blur'] },
  ],
  ip: [
    { required: true, message: 'Enter IP', trigger: ['input', 'blur'] },
  ],
  category: [
    { required: true, message: 'Enter ActionsCategory', trigger: ['input', 'blur'] },
  ],
  module: [
    { required: true, message: 'Enter Module Name', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: 'Enter ActionsDescription', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: 'Enter ActionsStatus', trigger: ['input', 'blur'] },
  ],
  message: [
    { required: true, message: 'Enter Action Message', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysLogDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysLogEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysLogAdd(formData.value)
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
        <NFormItem label="User ID" path="userId">
          <NInput v-model:value="formData.userId" placeholder="Enter User ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Actions" path="operation">
          <NInput v-model:value="formData.operation" placeholder="Enter Actions" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Method" path="method">
          <NInput v-model:value="formData.method" placeholder="Enter Method" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Parameters" path="params">
          <NInput v-model:value="formData.params" placeholder="Enter Parameters" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="IP" path="ip">
          <NInput v-model:value="formData.ip" placeholder="Enter IP" />
        </NFormItem>
        <!-- Date selector -->
        <NFormItem label="Action Time" path="operationTime">
          <!-- <NDatePicker type="datetime" v-model:value="formData.operationTime" /> -->
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="ActionsCategory" path="category">
          <NInput v-model:value="formData.category" placeholder="Enter ActionsCategory" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Module Name" path="module">
          <NInput v-model:value="formData.module" placeholder="Enter Module Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="ActionsDescription" path="description">
          <NInput v-model:value="formData.description" placeholder="Enter ActionsDescription" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="ActionsStatus" path="status">
          <NInput v-model:value="formData.status" placeholder="Enter ActionsStatus" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Action Message" path="message">
          <NInput v-model:value="formData.message" placeholder="Enter Action Message" />
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
