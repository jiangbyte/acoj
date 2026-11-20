<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataLibraryFetch } from '@/composables/v1'
import { FormatModuleType } from '@/utils'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataLibraryDefaultData } = useDataLibraryFetch()
const formData = ref<any>({ ...dataLibraryDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataLibraryDefaultData }
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
            <NAvatar
              size="small"
              :round="true"
              :src="formData.userAvatar"
            />
            <NText>{{ formData.userIdName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission Module">
          {{ FormatModuleType(formData.moduleType) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Problem">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission ID">
          {{ formData.submitId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submitted At">
          <NTime :time="Number(formData.submitTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Language">
          {{ formData.languageName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Source Code">
          <!-- {{ formData.code }} -->
          <NCode
            :code="formData.code"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Source Length">
          {{ formData.codeLength }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Check Activity">
          {{ formData.accessCount }}
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
