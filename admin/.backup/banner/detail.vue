<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NImage, NTime } from 'naive-ui'
import { useSysBannerFetch } from '@/composables'

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
        <NDescriptionsItem label="标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="副标题">
          {{ formData.subtitle }}
        </NDescriptionsItem>
        <NDescriptionsItem label="横幅">
          <NImage
            v-if="formData.banner"
            :src="formData.banner"
            height="120px"
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="按钮文字">
          {{ formData.buttonText }}
        </NDescriptionsItem>
        <NDescriptionsItem label="按钮链接">
          {{ formData.toUrl }}
        </NDescriptionsItem>
        <NDescriptionsItem label="排序">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="修改时间">
          <NTime :time="Number(formData.updateTime)" />
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
