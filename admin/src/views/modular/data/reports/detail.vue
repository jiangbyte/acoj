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
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="报告类型">
          {{ formData.reportTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="任务ID">
          {{ formData.taskId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交模块">
          {{ formData.moduleTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="样例数量">
          {{ formData.sampleCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似组数量">
          {{ formData.similarityGroupCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="平均相似度">
          {{ formData.avgSimilarity * 100 }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最大相似度">
          {{ formData.maxSimilarity * 100 }}
        </NDescriptionsItem>
        <NDescriptionsItem label="检测阈值">
          {{ formData.threshold }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似度分布">
          <!-- {{ formData.similarityDistribution }} -->
          <NCode
            :code="JSON.stringify(formData.similarityDistribution, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="程度统计">
          <!-- {{ formData.degreeStatistics }} -->
          <NCode
            :code="JSON.stringify(formData.degreeStatistics, null, 2)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="检测模式">
          {{ formData.checkModeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建者">
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
        <NDescriptionsItem label="更新时间">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="更新人">
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
            关闭
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
