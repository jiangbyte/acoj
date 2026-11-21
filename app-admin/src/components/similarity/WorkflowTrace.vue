<script lang="ts" setup>
import { NDataTable, NDrawer, NDrawerContent, NEmpty } from 'naive-ui'
import { useTaskReportsFetch } from '@/composables/v1'

const show = ref(false)
const loading = ref(false)
const result = ref<any>(null)
const { workflowTraceByProblem, workflowTraceByUser } = useTaskReportsFetch()

async function doOpen(query: { problemId?: string, userId?: string }) {
  show.value = true
  loading.value = true
  result.value = null
  const request = query.problemId
    ? workflowTraceByProblem(query)
    : workflowTraceByUser(query)
  const { data } = await request
  result.value = data
  loading.value = false
}

defineExpose({ doOpen })

const submitColumns = [
  { title: 'Submit', key: 'id', ellipsis: { tooltip: true } },
  { title: 'User', key: 'userId', ellipsis: { tooltip: true } },
  { title: 'Status', key: 'status' },
]
const libraryColumns = [
  { title: 'Library', key: 'id', ellipsis: { tooltip: true } },
  { title: 'Submit', key: 'submitId', ellipsis: { tooltip: true } },
  { title: 'User', key: 'userId', ellipsis: { tooltip: true } },
]
const similarityColumns = [
  { title: 'Submit', key: 'submitId', ellipsis: { tooltip: true } },
  { title: 'Origin', key: 'originId', ellipsis: { tooltip: true } },
  { title: 'Similarity', key: 'similarity' },
]
const conversationColumns = [
  { title: 'Conversation', key: 'conversationId', ellipsis: { tooltip: true } },
  { title: 'Role', key: 'messageRole' },
  { title: 'Content', key: 'messageContent', ellipsis: { tooltip: true } },
]
const toolColumns = [
  { title: 'Tool', key: 'toolName' },
  { title: 'Status', key: 'status' },
  { title: 'Args', key: 'toolArgs', ellipsis: { tooltip: true } },
]
</script>

<template>
  <NDrawer v-model:show="show" width="860" placement="right">
    <NDrawerContent title="Trace" :native-scrollbar="false">
      <NEmpty v-if="!loading && !result" description="No data" />
      <NSpace v-else vertical>
        <NCard size="small" title="Submits">
          <NDataTable size="small" :loading="loading" :columns="submitColumns" :data="result?.submits || []" :pagination="false" />
        </NCard>
        <NCard size="small" title="Libraries">
          <NDataTable size="small" :loading="loading" :columns="libraryColumns" :data="result?.libraries || []" :pagination="false" />
        </NCard>
        <NCard size="small" title="Similarities">
          <NDataTable size="small" :loading="loading" :columns="similarityColumns" :data="result?.similarities || []" :pagination="false" />
        </NCard>
        <NCard size="small" title="Conversations">
          <NDataTable size="small" :loading="loading" :columns="conversationColumns" :data="result?.conversations || []" :pagination="false" />
        </NCard>
        <NCard size="small" title="Tool traces">
          <NDataTable size="small" :loading="loading" :columns="toolColumns" :data="result?.toolTraces || []" :pagination="false" />
        </NCard>
      </NSpace>
    </NDrawerContent>
  </NDrawer>
</template>
