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
    { required: true, message: '请输入对话ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: '请输入题目ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: '请输入题集ID', trigger: ['input', 'blur'] },
  ],
  userId: [
    { required: true, message: '请输入用户ID', trigger: ['input', 'blur'] },
  ],
  messageType: [
    { required: true, message: '请输入消息类型', trigger: ['input', 'blur'] },
  ],
  messageRole: [
    { required: true, message: '请输入消息角色', trigger: ['input', 'blur'] },
  ],
  messageContent: [
    { required: true, message: '请输入消息内容', trigger: ['input', 'blur'] },
  ],
  userCode: [
    { required: true, message: '请输入用户代码', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: '请输入代码语言', trigger: ['input', 'blur'] },
  ],
  promptTokens: [
    { required: true, message: '请输入提示Tokens', type: 'number', trigger: ['input', 'blur'] },
  ],
  completionTokens: [
    { required: true, message: '请输入完成Tokens', type: 'number', trigger: ['input', 'blur'] },
  ],
  totalTokens: [
    { required: true, message: '请输入总Tokens', type: 'number', trigger: ['input', 'blur'] },
  ],
  streamingDuration: [
    { required: true, message: '请输入流式传输总耗时', type: 'number', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: '请输入状态', trigger: ['input', 'blur'] },
  ],
  errorMessage: [
    { required: true, message: '请输入错误信息', trigger: ['input', 'blur'] },
  ],
  userPlatform: [
    { required: true, message: '请输入用户平台', trigger: ['input', 'blur'] },
  ],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: ['input', 'blur'] },
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
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysConversationAdd(formData.value)
        if (success) {
          window.$message.success('新增成功')
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
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="对话ID" path="conversationId">
          <NInput v-model:value="formData.conversationId" placeholder="请输入对话ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="题目ID" path="problemId">
          <NInput v-model:value="formData.problemId" placeholder="请输入题目ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="题集ID" path="setId">
          <NInput v-model:value="formData.setId" placeholder="请输入题集ID" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="题集对话" path="isSet">
          <NRadioGroup v-model:value="formData.isSet">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用户ID" path="userId">
          <NInput v-model:value="formData.userId" placeholder="请输入用户ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="消息类型" path="messageType">
          <NInput v-model:value="formData.messageType" placeholder="请输入消息类型" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="消息角色" path="messageRole">
          <NInput v-model:value="formData.messageRole" placeholder="请输入消息角色" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="消息内容" path="messageContent">
          <NInput v-model:value="formData.messageContent" placeholder="请输入消息内容" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用户代码" path="userCode">
          <NInput v-model:value="formData.userCode" placeholder="请输入用户代码" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="代码语言" path="language">
          <NInput v-model:value="formData.language" placeholder="请输入代码语言" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="提示Tokens" path="promptTokens">
          <NInputNumber v-model:value="formData.promptTokens" :min="0" :max="100" placeholder="请输入提示Tokens" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="完成Tokens" path="completionTokens">
          <NInputNumber v-model:value="formData.completionTokens" :min="0" :max="100" placeholder="请输入完成Tokens" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="总Tokens" path="totalTokens">
          <NInputNumber v-model:value="formData.totalTokens" :min="0" :max="100" placeholder="请输入总Tokens" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="响应时间" path="responseTime">
          <NDatePicker v-model:value="formData.responseTime" type="datetime" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="流式传输总耗时" path="streamingDuration">
          <NInputNumber v-model:value="formData.streamingDuration" :min="0" :max="100" placeholder="请输入流式传输总耗时" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="状态" path="status">
          <NInput v-model:value="formData.status" placeholder="请输入状态" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="错误信息" path="errorMessage">
          <NInput v-model:value="formData.errorMessage" placeholder="请输入错误信息" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用户平台" path="userPlatform">
          <NInput v-model:value="formData.userPlatform" placeholder="请输入用户平台" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="IP地址" path="ipAddress">
          <NInput v-model:value="formData.ipAddress" placeholder="请输入IP地址" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            保存
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
