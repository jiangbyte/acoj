<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useTaskReportsFetch } from '@/composables/v1'
import { FormatModuleType } from '@/utils'
import WorkflowTrace from '@/components/similarity/WorkflowTrace.vue'

const emit = defineEmits(['close'])
const traceRef = ref()
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
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Report Type">
          {{ formData.reportTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Task ID">
          {{ formData.taskId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission Module">
          {{ FormatModuleType(formData.moduleType) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Problem">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample Count">
          {{ formData.sampleCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Similar Group Count">
          {{ formData.similarityGroupCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Average Similarity">
          {{ Number.isFinite(Number(formData.avgSimilarity)) ? (Number(formData.avgSimilarity) * 100).toFixed(2) : '0.00' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Maximum Similarity">
          {{ Number.isFinite(Number(formData.maxSimilarity)) ? (Number(formData.maxSimilarity) * 100).toFixed(2) : '0.00' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Check Threshold">
          {{ formData.threshold }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Min Match Length">
          {{ formData.minMatchLength }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Similarity Distribution">
          <!-- {{ formData.similarityDistribution }} -->
          <NCode
            :code="JSON.stringify(formData.similarityDistribution, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Level Statistics">
          <!-- {{ formData.degreeStatistics }} -->
          <NCode
            :code="JSON.stringify(formData.degreeStatistics, null, 2)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Check Mode">
          {{ formData.checkModeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
              size="small"
              :round="true"
              :src="formData.createUserAvatar"
            />
            <NText>{{ formData.createUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated At">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated By">
          <!-- {{ formData.updateUserName }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
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
          <NButton
            type="primary"
            :disabled="!formData.problemId"
            @click="traceRef.doOpen({ problemId: formData.problemId })"
          >
            Trace
          </NButton>
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
  <WorkflowTrace ref="traceRef" />
</template>

<style scoped>

</style>
