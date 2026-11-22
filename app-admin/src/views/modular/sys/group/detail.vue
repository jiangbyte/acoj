<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysGroupFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysGroupDefaultData } = useSysGroupFetch()
const formData = ref<any>({ ...sysGroupDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysGroupDefaultData }
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
        <NDescriptionsItem label="User Group">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Parent User Group">
          {{ formData.parentIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Name">
          {{ formData.name }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Code">
          {{ formData.code }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Description">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sort Order">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Owner">
          {{ formData.adminIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="System Group">
          {{ formData.groupTypeName }}
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
