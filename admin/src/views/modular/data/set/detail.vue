<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataSetFetch } from '@/composables/v1'

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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题集类型">
          {{ formData.setType }}
        </NDescriptionsItem>
        <NDescriptionsItem label="标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="封面">
          {{ formData.cover }}
        </NDescriptionsItem>
        <NDescriptionsItem label="描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分类">
          {{ formData.categoryId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="难度">
          {{ formData.difficulty }}
        </NDescriptionsItem>
        <NDescriptionsItem label="开始时间">
          <NTime :time="Number(formData.startTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="结束时间">
          <NTime :time="Number(formData.endTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="额外的信息">
          {{ formData.exJson }}
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
        <NDescriptionsItem label="是否可见">
          {{ formData.isVisible }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否使用AI">
          {{ formData.useAi }}
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
