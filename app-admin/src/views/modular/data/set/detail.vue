<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataSetFetch } from '@/composables/v1'
import MDViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataSetDefaultData } = useDataSetFetch()
const formData = ref<any>({ ...dataSetDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataSetDefaultData }
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
        <NDescriptionsItem label="Set Type">
          {{ formData.setTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Title">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Cover">
          <!-- {{ formData.cover }} -->
          <SafeImage :src="formData.cover" width="100" height="100" object-fit="cover"  previewable />
        </NDescriptionsItem>
        <NDescriptionsItem label="Description">
          <!-- {{ formData.description }} -->
          <MDViewer :model-value="formData.description" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Category">
          {{ formData.categoryName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Difficulty">
          {{ formData.difficultyName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Starts At">
          <NTime v-if="Number(formData.setType) === 2" :time="Number(formData.startTime)" />
          <div v-else>
            -
          </div>
        </NDescriptionsItem>
        <NDescriptionsItem label="Ends At">
          <NTime v-if="Number(formData.setType) === 2" :time="Number(formData.endTime)" />
          <div v-else>
            -
          </div>
        </NDescriptionsItem>
        <NDescriptionsItem label="Published">
          {{ formData.isVisibleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Use AI">
          {{ formData.useAiName }}
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
