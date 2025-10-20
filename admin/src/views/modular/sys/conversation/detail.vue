<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysConversationFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysConversationDefaultData } = useSysConversationFetch()
const formData = ref<any>({ ...sysConversationDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysConversationDefaultData }
}

function doOpen(row: any) {
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="对话ID">
          {{ formData.conversationId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目ID">
          {{ formData.problemId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题集ID">
          {{ formData.setId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题集对话">
          {{ formData.isSet }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户ID">
          {{ formData.userId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="消息类型">
          {{ formData.messageType }}
        </NDescriptionsItem>
        <NDescriptionsItem label="消息角色">
          {{ formData.messageRole }}
        </NDescriptionsItem>
        <NDescriptionsItem label="消息内容">
          {{ formData.messageContent }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户代码">
          {{ formData.userCode }}
        </NDescriptionsItem>
        <NDescriptionsItem label="代码语言">
          {{ formData.language }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提示Tokens">
          {{ formData.promptTokens }}
        </NDescriptionsItem>
        <NDescriptionsItem label="完成Tokens">
          {{ formData.completionTokens }}
        </NDescriptionsItem>
        <NDescriptionsItem label="总Tokens">
          {{ formData.totalTokens }}
        </NDescriptionsItem>
        <NDescriptionsItem label="响应时间">
          <NTime :time="Number(formData.responseTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="流式传输总耗时">
          {{ formData.streamingDuration }}
        </NDescriptionsItem>
        <NDescriptionsItem label="状态">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="错误信息">
          {{ formData.errorMessage }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户平台">
          {{ formData.userPlatform }}
        </NDescriptionsItem>
        <NDescriptionsItem label="IP地址">
          {{ formData.ipAddress }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建者">
          {{ formData.createUserName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="更新时间">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="更新人">
          {{ formData.updateUserName }}
        </NDescriptionsItem>
      </NDescriptions>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
