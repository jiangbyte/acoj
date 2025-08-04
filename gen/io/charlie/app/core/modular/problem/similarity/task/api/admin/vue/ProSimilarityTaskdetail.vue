<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NImage, NTime } from 'naive-ui'
import { useProSimilarityTaskFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { proSimilarityTaskDefaultData } = useProSimilarityTaskFetch()
const formData = ref<any>({ ...proSimilarityTaskDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSimilarityTaskDefaultData }
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
          <NDescriptionsItem label="用户ID">
            {{ formData.userId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="题目ID">
            {{ formData.problemId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="任务状态">
            {{ formData.status }}
          </NDescriptionsItem>
          <NDescriptionsItem label="比较范围">
            {{ formData.compareRange }}
          </NDescriptionsItem>
          <NDescriptionsItem label="近期天数">
            {{ formData.daysBefore }}
          </NDescriptionsItem>
          <NDescriptionsItem label="比较提交数">
            {{ formData.totalCompared }}
          </NDescriptionsItem>
          <NDescriptionsItem label="最大相似度">
            {{ formData.maxSimilarity }}
          </NDescriptionsItem>
          <NDescriptionsItem label="手动任务">
            {{ formData.isManual }}
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
