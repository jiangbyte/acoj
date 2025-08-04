<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NImage, NTime } from 'naive-ui'
import { useProSetSimilarityResultFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { proSetSimilarityResultDefaultData } = useProSetSimilarityResultFetch()
const formData = ref<any>({ ...proSetSimilarityResultDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetSimilarityResultDefaultData }
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
          <NDescriptionsItem label="关联的任务ID">
            {{ formData.taskId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="提交ID">
            {{ formData.originSubmitId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="被比较的提交ID">
            {{ formData.comparedSubmitId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="相似度值">
            {{ formData.similarity }}
          </NDescriptionsItem>
          <NDescriptionsItem label="详细比对结果">
            {{ formData.details }}
          </NDescriptionsItem>
          <NDescriptionsItem label="匹配部分详情">
            {{ formData.matchDetails }}
          </NDescriptionsItem>
          <NDescriptionsItem label="相似度阈值">
            {{ formData.threshold }}
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
