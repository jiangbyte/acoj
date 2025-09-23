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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="横幅">
          <NImage :src="formData.banner" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="按钮文字">
          {{ formData.buttonText }}
        </NDescriptionsItem>
        <NDescriptionsItem label="链接">
          {{ formData.toUrl }}
        </NDescriptionsItem>
        <NDescriptionsItem label="排序">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="子标题">
          {{ formData.subtitle }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建者">
          {{ formData.createUserName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否可见">
          {{ formData.isVisibleName }}
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
