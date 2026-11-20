<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysConversationFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysConversationDefaultData, sysConversationAdd, sysConversationEdit } = useSysConversationFetch()
const formData = ref<any>({ ...sysConversationDefaultData })
const rules = {
  conversationId: [
    { required: true, message: 'Enter Conversation ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: 'Enter Problem ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: 'Enter Set ID', trigger: ['input', 'blur'] },
  ],
  userId: [
    { required: true, message: 'Enter User ID', trigger: ['input', 'blur'] },
  ],
  messageType: [
    { required: true, message: 'Enter Message Type', trigger: ['input', 'blur'] },
  ],
  messageRole: [
    { required: true, message: 'Enter Message Role', trigger: ['input', 'blur'] },
  ],
  messageContent: [
    { required: true, message: 'Enter Message Content', trigger: ['input', 'blur'] },
  ],
  userCode: [
    { required: true, message: 'Enter User Code', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: 'Enter Code Language', trigger: ['input', 'blur'] },
  ],
  promptTokens: [
    { required: true, message: 'Enter NoticeTokens', type: 'number', trigger: ['input', 'blur'] },
  ],
  completionTokens: [
    { required: true, message: 'Enter Completion Tokens', type: 'number', trigger: ['input', 'blur'] },
  ],
  totalTokens: [
    { required: true, message: 'Enter Total Tokens', type: 'number', trigger: ['input', 'blur'] },
  ],
  streamingDuration: [
    { required: true, message: 'Enter Streaming Duration', type: 'number', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: 'Enter Status', trigger: ['input', 'blur'] },
  ],
  errorMessage: [
    { required: true, message: 'Enter Error Message', trigger: ['input', 'blur'] },
  ],
  userPlatform: [
    { required: true, message: 'Enter User Platform', trigger: ['input', 'blur'] },
  ],
  ipAddress: [
    { required: true, message: 'Enter IP Address', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysConversationDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysConversationEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysConversationAdd(formData.value)
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
        <NFormItem label="Conversation ID" path="conversationId">
          <NInput v-model:value="formData.conversationId" placeholder="Enter Conversation ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Problem ID" path="problemId">
          <NInput v-model:value="formData.problemId" placeholder="Enter Problem ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Set ID" path="setId">
          <NInput v-model:value="formData.setId" placeholder="Enter Set ID" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Set Conversation" path="isSet">
          <NRadioGroup v-model:value="formData.isSet">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="User ID" path="userId">
          <NInput v-model:value="formData.userId" placeholder="Enter User ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Message Type" path="messageType">
          <NInput v-model:value="formData.messageType" placeholder="Enter Message Type" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Message Role" path="messageRole">
          <NInput v-model:value="formData.messageRole" placeholder="Enter Message Role" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Message Content" path="messageContent">
          <NInput v-model:value="formData.messageContent" placeholder="Enter Message Content" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="User Code" path="userCode">
          <NInput v-model:value="formData.userCode" placeholder="Enter User Code" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Code Language" path="language">
          <NInput v-model:value="formData.language" placeholder="Enter Code Language" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="NoticeTokens" path="promptTokens">
          <NInputNumber v-model:value="formData.promptTokens" :min="0" :max="100" placeholder="Enter NoticeTokens" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Completion Tokens" path="completionTokens">
          <NInputNumber v-model:value="formData.completionTokens" :min="0" :max="100" placeholder="Enter Completion Tokens" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Total Tokens" path="totalTokens">
          <NInputNumber v-model:value="formData.totalTokens" :min="0" :max="100" placeholder="Enter Total Tokens" />
        </NFormItem>
        <!-- Date selector -->
        <NFormItem label="Response Time" path="responseTime">
          <NDatePicker v-model:value="formData.responseTime" type="datetime" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Streaming Duration" path="streamingDuration">
          <NInputNumber v-model:value="formData.streamingDuration" :min="0" :max="100" placeholder="Enter Streaming Duration" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Status" path="status">
          <NInput v-model:value="formData.status" placeholder="Enter Status" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Error Message" path="errorMessage">
          <NInput v-model:value="formData.errorMessage" placeholder="Enter Error Message" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="User Platform" path="userPlatform">
          <NInput v-model:value="formData.userPlatform" placeholder="Enter User Platform" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="IP Address" path="ipAddress">
          <NInput v-model:value="formData.ipAddress" placeholder="Enter IP Address" />
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
