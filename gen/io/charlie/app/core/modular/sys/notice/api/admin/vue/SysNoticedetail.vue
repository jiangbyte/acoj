<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NImage, NTime } from 'naive-ui'
import { useSysNoticeFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysNoticeDefaultData } = useSysNoticeFetch()
const formData = ref<any>({ ...sysNoticeDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysNoticeDefaultData }
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
          <NDescriptionsItem label="标题">
            {{ formData.title }}
          </NDescriptionsItem>
          <NDescriptionsItem label="封面">
            {{ formData.cover }}
          </NDescriptionsItem>
          <NDescriptionsItem label="链接">
            {{ formData.url }}
          </NDescriptionsItem>
          <NDescriptionsItem label="排序">
            {{ formData.sort }}
          </NDescriptionsItem>
          <NDescriptionsItem label="内容">
            {{ formData.content }}
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
