<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysConversationFetch } from '@/composables/v1'
import MDViewer from '@/components/common/editor/md/MarkdownViewer.vue'

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
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Conversation ID">
          {{ formData.conversationId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Problem">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Problem Set">
          {{ formData.setIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Set Conversation">
          {{ formData.isSetName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="User">
          <!-- {{ formData.userId }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.userAvatar"
            />
            <NText>{{ formData.userIdName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Message Type">
          {{ formData.messageTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Message Role">
          {{ formData.messageRoleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Message Content">
          <!-- {{ formData.messageContent }} -->
          <MDViewer :model-value="formData.messageContent" />
        </NDescriptionsItem>
        <NDescriptionsItem label="User Code">
          <NCode
            :code="formData.userCode"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Code Language">
          {{ formData.languageName }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="NoticeTokens">
          {{ formData.promptTokens }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Completion Tokens">
          {{ formData.completionTokens }}
        </NDescriptionsItem> -->
        <!-- <NDescriptionsItem label="Total Tokens">
          {{ formData.totalTokens }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Response Time">
          <NTime :time="Number(formData.responseTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Streaming Duration">
          {{ formData.streamingDuration }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Status">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Error Message">
          {{ formData.errorMessage }}
        </NDescriptionsItem>
        <NDescriptionsItem label="User Platform">
          {{ formData.userPlatform }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="IP Address">
          {{ formData.ipAddress }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.createUserAvatar"
            />
            <NText>{{ formData.createUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated At">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated By">
          <!-- {{ formData.updateUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.updateUserAvatar"
            />
            <NText>{{ formData.updateUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
      </NDescriptions>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
