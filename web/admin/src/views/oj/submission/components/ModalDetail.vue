<script setup lang="tsx">
import type { ProDataTableColumns } from 'pro-naive-ui'
import { ojSubmissionApi } from '@/api'
import { formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  detail: {
    submission: null as any,
    cases: [] as any[],
    source: null as any,
  },
})

const caseColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'submission_id', path: 'submission_id', width: 160 },
  { title: 'case_no', path: 'case_no', width: 100 },
  { title: 'status', path: 'status', width: 130 },
  { title: 'result', path: 'result', width: 100 },
  { title: 'time_ms', path: 'time_ms', width: 100 },
  { title: 'memory_kb', path: 'memory_kb', width: 120 },
  { title: 'points', path: 'points', width: 100 },
  { title: 'total', path: 'total', width: 100 },
  { title: 'batch_no', path: 'batch_no', width: 110 },
  { title: 'feedback', path: 'feedback', width: 220, ellipsis: { tooltip: true } },
  { title: 'sort', path: 'sort', width: 90 },
]

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.detail = {
    submission: null,
    cases: [],
    source: null,
  }
  try {
    const response = await ojSubmissionApi.fullDetail({ id })
    state.detail = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="980">
    <NDrawerContent title="提交详情" closable>
      <NSpin :show="state.loading">
        <NFlex vertical :size="16">
          <NDescriptions v-if="state.detail.submission" :column="2" bordered size="small">
            <NDescriptionsItem label="id">{{ state.detail.submission.id }}</NDescriptionsItem>
            <NDescriptionsItem label="problem_id">{{ state.detail.submission.problem_id }}</NDescriptionsItem>
            <NDescriptionsItem label="problem_code">{{ state.detail.submission.problem_code }}</NDescriptionsItem>
            <NDescriptionsItem label="account_type">{{ state.detail.submission.account_type }}</NDescriptionsItem>
            <NDescriptionsItem label="account_id">{{ state.detail.submission.account_id }}</NDescriptionsItem>
            <NDescriptionsItem label="language_id">{{ state.detail.submission.language_id || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="judge_mode">{{ state.detail.submission.judge_mode }}</NDescriptionsItem>
            <NDescriptionsItem label="status">{{ state.detail.submission.status }}</NDescriptionsItem>
            <NDescriptionsItem label="result">{{ state.detail.submission.result || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="score">{{ state.detail.submission.score ?? '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="time_ms">{{ state.detail.submission.time_ms ?? '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="memory_kb">{{ state.detail.submission.memory_kb ?? '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="current_case">{{ state.detail.submission.current_case }}</NDescriptionsItem>
            <NDescriptionsItem label="case_points">{{ state.detail.submission.case_points }}</NDescriptionsItem>
            <NDescriptionsItem label="case_total">{{ state.detail.submission.case_total }}</NDescriptionsItem>
            <NDescriptionsItem label="judge_node_id">{{ state.detail.submission.judge_node_id || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="submitted_at">{{ formatDateTime(state.detail.submission.submitted_at) }}</NDescriptionsItem>
            <NDescriptionsItem label="judged_at">{{ formatDateTime(state.detail.submission.judged_at) }}</NDescriptionsItem>
            <NDescriptionsItem label="rejudged_at">{{ formatDateTime(state.detail.submission.rejudged_at) }}</NDescriptionsItem>
            <NDescriptionsItem label="contest_id">{{ state.detail.submission.contest_id || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="contest_problem_id">{{ state.detail.submission.contest_problem_id || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="participation_id">{{ state.detail.submission.participation_id || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="is_pretest">{{ state.detail.submission.is_pretest }}</NDescriptionsItem>
            <NDescriptionsItem label="is_archived">{{ state.detail.submission.is_archived }}</NDescriptionsItem>
            <NDescriptionsItem label="source_visibility">{{ state.detail.submission.source_visibility || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="created_at">{{ formatDateTime(state.detail.submission.created_at) }}</NDescriptionsItem>
            <NDescriptionsItem label="updated_at">{{ formatDateTime(state.detail.submission.updated_at) }}</NDescriptionsItem>
          </NDescriptions>

          <NDescriptions v-if="state.detail.submission" :column="1" bordered size="small">
            <NDescriptionsItem label="compile_output">
              <NCode :code="state.detail.submission.compile_output || '-'" language="text" word-wrap />
            </NDescriptionsItem>
            <NDescriptionsItem label="extra">
              <NCode :code="JSON.stringify(state.detail.submission.extra ?? {}, null, 2)" language="json" word-wrap />
            </NDescriptionsItem>
          </NDescriptions>

          <ProDataTable title="提交测试点" :columns="caseColumns" :data="state.detail.cases" :pagination="false" :scroll-x="1480" />

          <NDescriptions v-if="state.detail.source" :column="1" bordered size="small">
            <NDescriptionsItem label="source.id">{{ state.detail.source.id }}</NDescriptionsItem>
            <NDescriptionsItem label="source.submission_id">{{ state.detail.source.submission_id }}</NDescriptionsItem>
            <NDescriptionsItem label="source_hash">{{ state.detail.source.source_hash || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="answer_files">
              <NCode :code="JSON.stringify(state.detail.source.answer_files ?? [], null, 2)" language="json" word-wrap />
            </NDescriptionsItem>
            <NDescriptionsItem label="size">{{ state.detail.source.size ?? '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="source">
              <NCode :code="state.detail.source.source || '-'" language="cpp" word-wrap />
            </NDescriptionsItem>
          </NDescriptions>
        </NFlex>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
