<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysUserFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysUserDefaultData } = useSysUserFetch()
const formData = ref<any>({ ...sysUserDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysUserDefaultData }
}

function doOpen(row: any) {
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
function getGenderText(gender: 0 | 1 | 2) {
  const genderMap = {
    1: 'Male',
    2: 'Female',
    0: 'Unknown',
  }
  return genderMap[gender]
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="User Group">
          {{ formData.groupIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="username">
          {{ formData.username }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Nickname">
          {{ formData.nickname }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Avatar">
          <NImage :src="formData.avatar" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Background Image">
          <NImage :src="formData.background" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Bio">
          {{ formData.quote }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Gender">
          {{ getGenderText(formData.gender) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Email">
          {{ formData.email }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Phone">
          {{ formData.telephone }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Student ID">
          {{ formData.studentNumber }}
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
