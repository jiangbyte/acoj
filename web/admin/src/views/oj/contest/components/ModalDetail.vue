<script setup lang="tsx">
import type { ProDataTableColumns } from 'pro-naive-ui'
import { ojContestApi } from '@/api'
import { formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  activeTab: 'basic',
  workspace: {
    contest: null as any,
    problems: [] as any[],
    members: [] as any[],
    participations: [] as any[],
    problem_results: [] as any[],
    ratings: [] as any[],
  },
})

const problemColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'contest_id', path: 'contest_id', width: 170 },
  { title: 'problem_id', path: 'problem_id', width: 170 },
  { title: 'label', path: 'label', width: 100 },
  { title: 'points', path: 'points', width: 100 },
  { title: 'partial', path: 'partial', width: 100 },
  { title: 'is_pretest', path: 'is_pretest', width: 120 },
  { title: 'max_submissions', path: 'max_submissions', width: 150 },
  { title: 'sort', path: 'sort', width: 90 },
]

const memberColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'contest_id', path: 'contest_id', width: 170 },
  { title: 'account_type', path: 'account_type', width: 130 },
  { title: 'account_id', path: 'account_id', width: 220 },
  { title: 'role', path: 'role', width: 140 },
]

const participationColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'contest_id', path: 'contest_id', width: 170 },
  { title: 'account_type', path: 'account_type', width: 130 },
  { title: 'account_id', path: 'account_id', width: 220 },
  { title: 'participation_type', path: 'participation_type', width: 160 },
  { title: 'started_at', path: 'started_at', width: 180 },
  { title: 'ended_at', path: 'ended_at', width: 180 },
  { title: 'score', path: 'score', width: 100 },
  { title: 'penalty', path: 'penalty', width: 100 },
  { title: 'rank', path: 'rank', width: 100 },
  { title: 'is_disqualified', path: 'is_disqualified', width: 150 },
  { title: 'format_data', key: 'format_data', width: 220, render: (row) => JSON.stringify(row.format_data) },
]

const resultColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'contest_id', path: 'contest_id', width: 170 },
  { title: 'participation_id', path: 'participation_id', width: 190 },
  { title: 'contest_problem_id', path: 'contest_problem_id', width: 190 },
  { title: 'best_submission_id', path: 'best_submission_id', width: 180 },
  { title: 'score', path: 'score', width: 100 },
  { title: 'penalty', path: 'penalty', width: 100 },
  { title: 'attempts', path: 'attempts', width: 100 },
  { title: 'accepted_at', path: 'accepted_at', width: 180 },
  { title: 'is_first_ac', path: 'is_first_ac', width: 120 },
]

const ratingColumns: ProDataTableColumns<any> = [
  { title: 'id', path: 'id', width: 150 },
  { title: 'contest_id', path: 'contest_id', width: 170 },
  { title: 'participation_id', path: 'participation_id', width: 190 },
  { title: 'account_type', path: 'account_type', width: 130 },
  { title: 'account_id', path: 'account_id', width: 220 },
  { title: 'rank', path: 'rank', width: 100 },
  { title: 'old_rating', path: 'old_rating', width: 120 },
  { title: 'new_rating', path: 'new_rating', width: 120 },
  { title: 'performance', path: 'performance', width: 130 },
  { title: 'rated_at', path: 'rated_at', width: 180 },
]

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.activeTab = 'basic'
  try {
    const response = await ojContestApi.workspace({ id })
    state.workspace = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="1080">
    <NDrawerContent :title="state.workspace.contest ? `${state.workspace.contest.key} ${state.workspace.contest.name}` : '竞赛详情'" closable>
      <NSpin :show="state.loading">
        <NFlex v-if="state.workspace.contest" vertical :size="16">
          <NGrid :cols="4" :x-gap="12" :y-gap="12">
            <NGi><NStatistic label="contest_format" :value="state.workspace.contest.contest_format" /></NGi>
            <NGi><NStatistic label="visibility" :value="state.workspace.contest.visibility" /></NGi>
            <NGi><NStatistic label="scoreboard_visibility" :value="state.workspace.contest.scoreboard_visibility" /></NGi>
            <NGi><NStatistic label="status" :value="state.workspace.contest.status" /></NGi>
          </NGrid>

          <NTabs v-model:value="state.activeTab" type="line" animated>
            <NTabPane name="basic" tab="基础信息">
              <NDescriptions :column="2" bordered size="small">
                <NDescriptionsItem label="id">{{ state.workspace.contest.id }}</NDescriptionsItem>
                <NDescriptionsItem label="key">{{ state.workspace.contest.key }}</NDescriptionsItem>
                <NDescriptionsItem label="name">{{ state.workspace.contest.name }}</NDescriptionsItem>
                <NDescriptionsItem label="summary">{{ state.workspace.contest.summary || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="description">{{ state.workspace.contest.description || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="start_at">{{ formatDateTime(state.workspace.contest.start_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="end_at">{{ formatDateTime(state.workspace.contest.end_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="duration_seconds">{{ state.workspace.contest.duration_seconds ?? '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="visibility">{{ state.workspace.contest.visibility }}</NDescriptionsItem>
                <NDescriptionsItem label="contest_format">{{ state.workspace.contest.contest_format }}</NDescriptionsItem>
                <NDescriptionsItem label="scoreboard_visibility">{{ state.workspace.contest.scoreboard_visibility }}</NDescriptionsItem>
                <NDescriptionsItem label="is_rated">{{ state.workspace.contest.is_rated }}</NDescriptionsItem>
                <NDescriptionsItem label="rating_floor">{{ state.workspace.contest.rating_floor ?? '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="rating_ceiling">{{ state.workspace.contest.rating_ceiling ?? '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="access_code_hash">{{ state.workspace.contest.access_code_hash || '-' }}</NDescriptionsItem>
                <NDescriptionsItem label="allow_virtual">{{ state.workspace.contest.allow_virtual }}</NDescriptionsItem>
                <NDescriptionsItem label="freeze_at">{{ formatDateTime(state.workspace.contest.freeze_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="unfreeze_at">{{ formatDateTime(state.workspace.contest.unfreeze_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="status">{{ state.workspace.contest.status }}</NDescriptionsItem>
                <NDescriptionsItem label="created_at">{{ formatDateTime(state.workspace.contest.created_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="updated_at">{{ formatDateTime(state.workspace.contest.updated_at) }}</NDescriptionsItem>
              </NDescriptions>
            </NTabPane>
            <NTabPane name="config" tab="赛制配置">
              <NDescriptions :column="1" bordered size="small">
                <NDescriptionsItem label="format_config"><NCode :code="JSON.stringify(state.workspace.contest.format_config ?? {}, null, 2)" language="json" word-wrap /></NDescriptionsItem>
                <NDescriptionsItem label="extra"><NCode :code="JSON.stringify(state.workspace.contest.extra ?? {}, null, 2)" language="json" word-wrap /></NDescriptionsItem>
              </NDescriptions>
            </NTabPane>
            <NTabPane name="problems" tab="题目编排"><ProDataTable :columns="problemColumns" :data="state.workspace.problems" :pagination="false" :scroll-x="1200" /></NTabPane>
            <NTabPane name="members" tab="成员"><ProDataTable :columns="memberColumns" :data="state.workspace.members" :pagination="false" :scroll-x="850" /></NTabPane>
            <NTabPane name="participations" tab="参赛记录"><ProDataTable :columns="participationColumns" :data="state.workspace.participations" :pagination="false" :scroll-x="1700" /></NTabPane>
            <NTabPane name="results" tab="题目结果"><ProDataTable :columns="resultColumns" :data="state.workspace.problem_results" :pagination="false" :scroll-x="1500" /></NTabPane>
            <NTabPane name="ratings" tab="评级"><ProDataTable :columns="ratingColumns" :data="state.workspace.ratings" :pagination="false" :scroll-x="1450" /></NTabPane>
          </NTabs>
        </NFlex>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
