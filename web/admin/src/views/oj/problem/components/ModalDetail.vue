<script setup lang="tsx">
import type { ProDataTableColumns } from 'pro-naive-ui'
import { ojProblemApi } from '@/api'
import { formatDateTime } from '@/utils'
import { computed, reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  activeTab: 'basic',
  workspace: {
    problem: null as any,
    samples: [] as any[],
    datasets: [] as any[],
    test_cases: [] as any[],
    tags: [] as any[],
    tag_relations: [] as any[],
    assets: [] as any[],
    members: [] as any[],
    objective_answers: [] as any[],
  },
})

const activeDataset = computed(() => state.workspace.datasets.find((item) => item.is_active) ?? state.workspace.datasets[0])
const activeDatasetCases = computed(() =>
  state.workspace.test_cases.filter((item) => item.dataset_id === activeDataset.value?.id),
)

const sampleColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 140, ellipsis: { tooltip: true } },
  { title: 'problem_id', path: 'problem_id', width: 160, ellipsis: { tooltip: true } },
  { title: 'input', path: 'input', width: 260 },
  { title: 'output', path: 'output', width: 220 },
  { title: 'explanation', path: 'explanation', width: 260 },
  { title: 'sort', path: 'sort', width: 80 },
]

const datasetColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 160, ellipsis: { tooltip: true } },
  { title: 'problem_id', path: 'problem_id', width: 160, ellipsis: { tooltip: true } },
  { title: 'name', path: 'name', width: 140 },
  { title: 'version', path: 'version', width: 100 },
  { title: 'is_active', path: 'is_active', width: 100 },
  { title: 'data_zip_url', path: 'data_zip_url', width: 360 },
  { title: 'generator_url', path: 'generator_url', width: 220 },
  { title: 'checker', path: 'checker', width: 120 },
  { title: 'output_prefix', path: 'output_prefix', width: 130 },
  { title: 'output_limit', path: 'output_limit', width: 130 },
  { title: 'unicode_enabled', path: 'unicode_enabled', width: 150 },
]

const testCaseColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150, ellipsis: { tooltip: true } },
  { title: 'dataset_id', path: 'dataset_id', width: 170, ellipsis: { tooltip: true } },
  { title: 'case_no', path: 'case_no', width: 100 },
  { title: 'case_type', path: 'case_type', width: 130 },
  { title: 'input_file', path: 'input_file', width: 200 },
  { title: 'output_file', path: 'output_file', width: 200 },
  { title: 'input_inline', path: 'input_inline', width: 180 },
  { title: 'output_inline', path: 'output_inline', width: 180 },
  { title: 'generator_args', path: 'generator_args', width: 180 },
  { title: 'points', path: 'points', width: 100 },
  { title: 'is_pretest', path: 'is_pretest', width: 110 },
  { title: 'batch_no', path: 'batch_no', width: 100 },
  { title: 'time_limit_ms', path: 'time_limit_ms', width: 130 },
  { title: 'memory_limit_kb', path: 'memory_limit_kb', width: 150 },
  { title: 'checker', path: 'checker', width: 120 },
  { title: 'sort', path: 'sort', width: 90 },
]

const tagColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 160 },
  { title: 'code', path: 'code', width: 120 },
  { title: 'name', path: 'name', width: 130 },
  { title: 'color', path: 'color', width: 120 },
  { title: 'description', path: 'description', width: 220 },
  { title: 'status', path: 'status', width: 110 },
]

const assetColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 160 },
  { title: 'problem_id', path: 'problem_id', width: 160 },
  { title: 'asset_type', path: 'asset_type', width: 130 },
  { title: 'name', path: 'name', width: 180 },
  { title: 'url', path: 'url', width: 240 },
  { title: 'storage_key', path: 'storage_key', width: 320 },
  { title: 'checksum', path: 'checksum', width: 180 },
  { title: 'size', path: 'size', width: 100 },
  { title: 'version', path: 'version', width: 100 },
]

const memberColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'problem_id', path: 'problem_id', width: 160 },
  { title: 'account_type', path: 'account_type', width: 130 },
  { title: 'account_id', path: 'account_id', width: 220 },
  { title: 'role', path: 'role', width: 130 },
]

const objectiveColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 140 },
  { title: 'problem_id', path: 'problem_id', width: 160 },
  { title: 'answer_type', path: 'answer_type', width: 130 },
  { title: 'answer', key: 'answer', width: 260, render: (row) => JSON.stringify(row.answer) },
  { title: 'score_rule', key: 'score_rule', width: 260, render: (row) => JSON.stringify(row.score_rule) },
  { title: 'explanation', path: 'explanation', width: 260 },
]

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.activeTab = 'basic'
  try {
    const response = await ojProblemApi.workspace({ id })
    state.workspace = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="1080">
    <NDrawerContent :title="state.workspace.problem ? `${state.workspace.problem.code} ${state.workspace.problem.title}` : '题目详情'" closable>
      <NSpin :show="state.loading">
        <NFlex v-if="state.workspace.problem" vertical :size="16">
          <NGrid :cols="4" :x-gap="12" :y-gap="12">
            <NGi><NStatistic label="problem_type" :value="state.workspace.problem.problem_type" /></NGi>
            <NGi><NStatistic label="judge_mode" :value="state.workspace.problem.judge_mode" /></NGi>
            <NGi><NStatistic label="ac_rate" :value="`${state.workspace.problem.ac_rate}%`" /></NGi>
            <NGi><NStatistic label="limit" :value="`${state.workspace.problem.time_limit_ms}ms / ${Math.round(state.workspace.problem.memory_limit_kb / 1024)}MB`" /></NGi>
          </NGrid>

          <NTabs v-model:value="state.activeTab" type="line" animated>
            <NTabPane name="basic" tab="基础信息">
              <NDescriptions :column="2" bordered size="small">
                <NDescriptionsItem label="id">{{ state.workspace.problem.id }}</NDescriptionsItem>
                <NDescriptionsItem label="code">{{ state.workspace.problem.code }}</NDescriptionsItem>
                <NDescriptionsItem label="title">{{ state.workspace.problem.title }}</NDescriptionsItem>
                <NDescriptionsItem label="summary">{{ state.workspace.problem.summary || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="source">{{ state.workspace.problem.source || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="difficulty">{{ state.workspace.problem.difficulty }}</NDescriptionsItem>
                <NDescriptionsItem label="problem_type">{{ state.workspace.problem.problem_type }}</NDescriptionsItem>
                <NDescriptionsItem label="judge_mode">{{ state.workspace.problem.judge_mode }}</NDescriptionsItem>
                <NDescriptionsItem label="visibility">{{ state.workspace.problem.visibility }}</NDescriptionsItem>
                <NDescriptionsItem label="time_limit_ms">{{ state.workspace.problem.time_limit_ms }}</NDescriptionsItem>
                <NDescriptionsItem label="memory_limit_kb">{{ state.workspace.problem.memory_limit_kb }}</NDescriptionsItem>
                <NDescriptionsItem label="stack_limit_kb">{{ state.workspace.problem.stack_limit_kb ?? '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="output_limit_kb">{{ state.workspace.problem.output_limit_kb ?? '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="points">{{ state.workspace.problem.points }}</NDescriptionsItem>
                <NDescriptionsItem label="partial">{{ state.workspace.problem.partial }}</NDescriptionsItem>
                <NDescriptionsItem label="accepted_count">{{ state.workspace.problem.accepted_count }}</NDescriptionsItem>
                <NDescriptionsItem label="submit_count">{{ state.workspace.problem.submit_count }}</NDescriptionsItem>
                <NDescriptionsItem label="ac_rate">{{ state.workspace.problem.ac_rate }}</NDescriptionsItem>
                <NDescriptionsItem label="sort">{{ state.workspace.problem.sort }}</NDescriptionsItem>
                <NDescriptionsItem label="status">{{ state.workspace.problem.status }}</NDescriptionsItem>
                <NDescriptionsItem label="created_at">{{ formatDateTime(state.workspace.problem.created_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="updated_at">{{ formatDateTime(state.workspace.problem.updated_at) }}</NDescriptionsItem>
              </NDescriptions>
            </NTabPane>
            <NTabPane name="statement" tab="题面内容">
              <NDescriptions :column="1" bordered size="small">
                <NDescriptionsItem label="description"><NCode :code="state.workspace.problem.description || '-'" language="markdown" word-wrap /></NDescriptionsItem>
                <NDescriptionsItem label="input_description">{{ state.workspace.problem.input_description || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="output_description">{{ state.workspace.problem.output_description || '-' }}</NDescriptionsItem>
              </NDescriptions>
            </NTabPane>
            <NTabPane name="samples" tab="样例"><ProDataTable :columns="sampleColumns" :data="state.workspace.samples" :pagination="false" :scroll-x="1200" /></NTabPane>
            <NTabPane name="datasets" tab="测试数据">
              <NFlex vertical>
                <ProDataTable title="数据集" :columns="datasetColumns" :data="state.workspace.datasets" :pagination="false" :scroll-x="1700" />
                <ProDataTable title="当前数据集测试点" :columns="testCaseColumns" :data="activeDatasetCases" :pagination="false" :scroll-x="2300" />
              </NFlex>
            </NTabPane>
            <NTabPane name="judge" tab="判题配置">
              <NDescriptions :column="1" bordered size="small">
                <NDescriptionsItem label="allow_languages">{{ state.workspace.problem.allow_languages?.join(', ') || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="spj_language_id">{{ state.workspace.problem.spj_language_id || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="spj_source"><NCode :code="state.workspace.problem.spj_source || '-'" language="cpp" word-wrap /></NDescriptionsItem>
                <NDescriptionsItem label="interactor_language_id">{{ state.workspace.problem.interactor_language_id || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="interactor_source"><NCode :code="state.workspace.problem.interactor_source || '-'" language="cpp" word-wrap /></NDescriptionsItem>
                <NDescriptionsItem label="remote_provider">{{ state.workspace.problem.remote_provider || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="remote_problem_id">{{ state.workspace.problem.remote_problem_id || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="extra"><NCode :code="JSON.stringify(state.workspace.problem.extra ?? {}, null, 2)" language="json" word-wrap /></NDescriptionsItem>
              </NDescriptions>
            </NTabPane>
            <NTabPane name="tags" tab="标签"><ProDataTable :columns="tagColumns" :data="state.workspace.tags" :pagination="false" :scroll-x="860" /></NTabPane>
            <NTabPane name="assets" tab="附件资源"><ProDataTable :columns="assetColumns" :data="state.workspace.assets" :pagination="false" :scroll-x="1350" /></NTabPane>
            <NTabPane name="members" tab="协作者"><ProDataTable :columns="memberColumns" :data="state.workspace.members" :pagination="false" :scroll-x="820" /></NTabPane>
            <NTabPane name="objective" tab="客观题答案"><ProDataTable :columns="objectiveColumns" :data="state.workspace.objective_answers" :pagination="false" :scroll-x="1100" /></NTabPane>
          </NTabs>
        </NFlex>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
