<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysLogFetch } from '@/composables/v1'
import { FormatJsonString } from '@/utils'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysLogDefaultData } = useSysLogFetch()
const formData = ref<any>({ ...sysLogDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysLogDefaultData }
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
        <NDescriptionsItem label="User">
          <!-- {{ formData.userId }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
              size="small"
              :round="true"
              :src="formData.userAvatar"
            />
            <NText>{{ formData.userIdName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Actions">
          {{ formData.operation }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Method">
          {{ formData.method }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Parameters">
          <!-- {{ formData.params }} -->
          <NCode
            :code="FormatJsonString(formData.params)"
            language="json"
            word-wrap
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="IP">
          {{ formData.ip }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Action Time">
          <NTime :time="Number(formData.operationTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="ActionsCategory">
          {{ formData.category }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Module Name">
          {{ formData.module }}
        </NDescriptionsItem>
        <NDescriptionsItem label="ActionsDescription">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="ActionsStatus">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Action Message">
          {{ formData.message }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
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
            <SafeAvatar
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
