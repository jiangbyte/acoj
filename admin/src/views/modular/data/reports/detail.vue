<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useTaskReportsFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { taskReportsDefaultData } = useTaskReportsFetch()
const formData = ref<any>({ ...taskReportsDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskReportsDefaultData }
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
        <NDescriptionsItem label="报告类型">
          {{ formData.reportType }}
        </NDescriptionsItem>
        <NDescriptionsItem label="任务ID">
          {{ formData.taskId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题集ID">
          {{ formData.setId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否是题集提交">
          {{ formData.isSet }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目ID">
          {{ formData.problemId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="样例数量">
          {{ formData.sampleCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似组数量">
          {{ formData.similarityGroupCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="平均相似度">
          {{ formData.avgSimilarity }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最大相似度">
          {{ formData.maxSimilarity }}
        </NDescriptionsItem>
        <NDescriptionsItem label="检测阈值">
          {{ formData.threshold }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似度分布">
          {{ formData.similarityDistribution }}
        </NDescriptionsItem>
        <NDescriptionsItem label="程度统计">
          {{ formData.degreeStatistics }}
        </NDescriptionsItem>
        <NDescriptionsItem label="检测模式">
          {{ formData.checkMode }}
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
