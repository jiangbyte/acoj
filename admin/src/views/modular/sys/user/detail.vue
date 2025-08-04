<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysUserFetch } from '@/composables'

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
    1: '男',
    2: '女',
    0: '未知',
  }
  return genderMap[gender]
}
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户组">
          {{ formData.groupIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户名">
          {{ formData.username }}
        </NDescriptionsItem>
        <NDescriptionsItem label="昵称">
          {{ formData.nickname }}
        </NDescriptionsItem>
        <NDescriptionsItem label="头像">
          <NImage :src="formData.avatar" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="背景图片">
          <NImage :src="formData.background" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="签名">
          {{ formData.quote }}
        </NDescriptionsItem>
        <NDescriptionsItem label="性别">
          {{ getGenderText(formData.gender) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="邮箱">
          {{ formData.email }}
        </NDescriptionsItem>
        <NDescriptionsItem label="电话">
          {{ formData.telephone }}
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
