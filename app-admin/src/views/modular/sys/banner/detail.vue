<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysBannerFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysBannerDefaultData } = useSysBannerFetch()
const formData = ref<any>({ ...sysBannerDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysBannerDefaultData }
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
        <NDescriptionsItem label="Banner">
          <NImage :src="formData.banner" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Title">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Subtitle">
          {{ formData.subtitle }}
        </NDescriptionsItem>
        <NDescriptionsItem label="SubtitleVisible">
          {{ formData.isVisibleSubtitleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Button Text">
          {{ formData.buttonText }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Show Button">
          {{ formData.isVisibleButtonName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Navigation Module">
          {{ formData.jumpModuleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Open in New Window">
          {{ formData.targetBlankName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Navigation Type">
          {{ formData.jumpTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Navigation Target">
          {{ formData.jumpTarget }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sort Order">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Published">
          {{ formData.isVisibleName }}
        </NDescriptionsItem>
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
