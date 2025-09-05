<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysGroupFetch } from '@/composables'

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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="用户组">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="父级用户组">
          {{ formData.parentId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="名称">
          {{ formData.name }}
        </NDescriptionsItem>
        <NDescriptionsItem label="编码">
          {{ formData.code }}
        </NDescriptionsItem>
        <NDescriptionsItem label="描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="排序">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="负责人">
          {{ formData.adminIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="系统组">
          {{ formData.groupTypeName }}
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
