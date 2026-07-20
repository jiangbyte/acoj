<script setup lang="ts">
import { ojProblemApi } from '@/api'
import { dictTypeColor, dictTypeData, formatDateTime } from '@/utils'
import { createTagColor } from '@/utils/color'
import { ProCard } from 'pro-naive-ui'
import {
  NCode, NDataTable, NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent,
  NFlex, NGi, NGrid, NScrollbar, NSpace, NSpin, NStatistic, NTag,
} from 'naive-ui'
import { computed, reactive } from 'vue'

const showModal = reactive({ show: false, loading: false })
const workspace = reactive({
  problem: null as any,
  samples: [] as any[],
  datasets: [] as any[],
  test_cases: [] as any[],
  tags: [] as any[],
  tag_relations: [] as any[],
  assets: [] as any[],
  members: [] as any[],
  objective_answers: [] as any[],
})

const activeDataset = computed(() =>
  workspace.datasets.find((d: any) => d.is_active) ?? workspace.datasets[0],
)
const activeDatasetCases = computed(() =>
  workspace.test_cases.filter((tc: any) => tc.dataset_id === activeDataset.value?.id),
)

const authorMembers = computed(() => workspace.members.filter((m: any) => m.role === 'AUTHOR'))
const curatorMembers = computed(() => workspace.members.filter((m: any) => m.role === 'CURATOR'))
const testerMembers = computed(() => workspace.members.filter((m: any) => m.role === 'TESTER'))
const bannedMembers = computed(() => workspace.members.filter((m: any) => m.role === 'BANNED'))

const p = computed(() => workspace.problem ?? {})

async function openModal(id: string) {
  showModal.show = true
  showModal.loading = true
  try {
    const response = await ojProblemApi.workspace({ id })
    Object.assign(workspace, response.data)
  } finally {
    showModal.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="showModal.show" :width="1080">
    <NDrawerContent
      :title="workspace.problem ? `${workspace.problem.code} ${workspace.problem.title}` : '题目详情'"
      closable
    >
      <NSpin :show="showModal.loading">
        <NFlex v-if="workspace.problem" vertical :size="16">
          <!-- 概览指标 -->
          <NGrid :cols="4" :x-gap="12" :y-gap="12">
            <NGi><NStatistic label="题号" :value="p.code" /></NGi>
            <NGi><NStatistic label="类型" :value="dictTypeData('OJ_PROBLEM_TYPE', p.problem_type)" /></NGi>
            <NGi><NStatistic label="判题" :value="dictTypeData('OJ_JUDGE_MODE', p.judge_mode)" /></NGi>
            <NGi><NStatistic label="可见性" :value="dictTypeData('OJ_PROBLEM_VISIBILITY', p.visibility)" /></NGi>
            <NGi><NStatistic label="通过率" :value="`${p.ac_rate}%`" /></NGi>
            <NGi><NStatistic label="时间" :value="`${p.time_limit_ms}ms`" /></NGi>
            <NGi><NStatistic label="内存" :value="`${Math.round(p.memory_limit_kb / 1024)}MB`" /></NGi>
            <NGi><NStatistic label="分数" :value="p.points" /></NGi>
          </NGrid>

          <NScrollbar style="max-height: calc(100vh - 280px)" trigger="none">
            <NFlex vertical :size="16">
              <!-- 基础信息 -->
              <ProCard title="基础信息" :show-collapse="true" :collapsed="false">
                <NDescriptions :column="2" bordered size="small">
                  <NDescriptionsItem label="标题">{{ p.title }}</NDescriptionsItem>
                  <NDescriptionsItem label="状态"><NTag v-if="p.status" :color="dictTypeColor('COMMON_STATUS', p.status) ? { color: '#fff', border: dictTypeColor('COMMON_STATUS', p.status) } : undefined" :bordered="false" size="small">{{ p.status }}</NTag><span v-else>-</span></NDescriptionsItem>
                  <NDescriptionsItem label="摘要">{{ p.summary || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="难度">{{ p.difficulty }}</NDescriptionsItem>
                  <NDescriptionsItem label="来源">{{ p.source || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="排序">{{ p.sort }}</NDescriptionsItem>
                  <NDescriptionsItem label="创建时间">{{ formatDateTime(p.created_at) }}</NDescriptionsItem>
                  <NDescriptionsItem label="更新时间">{{ formatDateTime(p.updated_at) }}</NDescriptionsItem>
                </NDescriptions>
              </ProCard>

              <!-- 题面 -->
              <ProCard title="题面内容" :show-collapse="true" :collapsed="true">
                <NFlex vertical :size="12">
                  <div><strong>题目描述</strong><NCode :code="p.description || '-'" language="markdown" word-wrap /></div>
                  <div><strong>输入描述</strong><p>{{ p.input_description || '-' }}</p></div>
                  <div><strong>输出描述</strong><p>{{ p.output_description || '-' }}</p></div>
                </NFlex>
              </ProCard>

              <!-- 协作者 -->
              <ProCard title="协作者" :show-collapse="true" :collapsed="true">
                <NDescriptions :column="2" bordered size="small">
                  <NDescriptionsItem label="作者">{{ authorMembers.map((m: any) => m.account_id).join(', ') || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="维护者">{{ curatorMembers.map((m: any) => m.account_id).join(', ') || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="测试者">{{ testerMembers.map((m: any) => m.account_id).join(', ') || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="封禁">{{ bannedMembers.map((m: any) => m.account_id).join(', ') || '-' }}</NDescriptionsItem>
                </NDescriptions>
              </ProCard>

              <!-- 评测配置 -->
              <ProCard title="评测配置" :show-collapse="true" :collapsed="true">
                <NDescriptions :column="2" bordered size="small">
                  <NDescriptionsItem label="时间上限">{{ p.time_limit_ms }} ms</NDescriptionsItem>
                  <NDescriptionsItem label="内存上限">{{ p.memory_limit_kb }} KB</NDescriptionsItem>
                  <NDescriptionsItem label="栈上限">{{ p.stack_limit_kb ?? '-' }} KB</NDescriptionsItem>
                  <NDescriptionsItem label="输出上限">{{ p.output_limit_kb ?? '-' }} KB</NDescriptionsItem>
                  <NDescriptionsItem label="部分分">{{ p.partial ? '是' : '否' }}</NDescriptionsItem>
                  <NDescriptionsItem label="分数">{{ p.points }}</NDescriptionsItem>
                  <NDescriptionsItem label="允许语言">{{ p.allow_languages?.join(', ') || '-' }}</NDescriptionsItem>
                </NDescriptions>
              </ProCard>

              <!-- 样例 -->
              <ProCard title="样例" :show-collapse="true" :collapsed="false">
                <NDataTable
                  v-if="workspace.samples.length"
                  :columns="[
                    { title: '#', key: 'sort', width: 60 },
                    { title: '输入', key: 'input', ellipsis: { tooltip: true } },
                    { title: '输出', key: 'output', ellipsis: { tooltip: true } },
                    { title: '解析', key: 'explanation', ellipsis: { tooltip: true } },
                  ]"
                  :data="workspace.samples"
                  size="small"
                  :bordered="false"
                />
                <span v-else>无</span>
              </ProCard>

              <!-- 测试数据 -->
              <ProCard title="测试数据" :show-collapse="true" :collapsed="true">
                <NFlex vertical :size="12">
                  <NDataTable title="数据集" :columns="[
                    { title: '名称', key: 'name' },
                    { title: '版本', key: 'version', width: 100 },
                    { title: 'Check', key: 'checker', width: 100 },
                    { title: '激活', key: 'is_active', width: 70 },
                    { title: '数据包', key: 'data_zip_url', ellipsis: { tooltip: true } },
                  ]" :data="workspace.datasets" size="small" :bordered="false" />
                  <template v-if="activeDataset">
                    <NDataTable :title="`测试点 (${activeDataset.name})`" :columns="[
                      { title: '#', key: 'case_no', width: 60 },
                      { title: '类型', key: 'case_type', width: 100 },
                      { title: '输入文件', key: 'input_file', ellipsis: { tooltip: true } },
                      { title: '输出文件', key: 'output_file', ellipsis: { tooltip: true } },
                      { title: '分数', key: 'points', width: 70 },
                      { title: '预测试', key: 'is_pretest', width: 70 },
                      { title: '批次', key: 'batch_no', width: 60 },
                    ]" :data="activeDatasetCases" size="small" :bordered="false" />
                  </template>
                </NFlex>
              </ProCard>

              <!-- 标签 -->
              <ProCard title="标签" :show-collapse="true" :collapsed="true">
                <NFlex v-if="workspace.tags.length" size="small" wrap>
                  <NTag v-for="tag in workspace.tags" :key="tag.id" :bordered="false" size="small">{{ tag.name || tag.code }}</NTag>
                </NFlex>
                <span v-else>无</span>
              </ProCard>

              <!-- 附件 -->
              <ProCard title="附件" :show-collapse="true" :collapsed="true">
                <NDataTable v-if="workspace.assets.length" :columns="[
                  { title: '名称', key: 'name' },
                  { title: '类型', key: 'asset_type', width: 120 },
                  { title: 'URL', key: 'url', ellipsis: { tooltip: true } },
                  { title: '版本', key: 'version', width: 100 },
                ]" :data="workspace.assets" size="small" :bordered="false" />
                <span v-else>无</span>
              </ProCard>

              <!-- 客观题 -->
              <ProCard v-if="workspace.objective_answers.length" title="客观题答案" :show-collapse="true" :collapsed="true">
                <NDataTable :columns="[
                  { title: '类型', key: 'answer_type', width: 130 },
                  { title: '答案', key: 'answer', render: (row: any) => JSON.stringify(row.answer) },
                  { title: '计分规则', key: 'score_rule', render: (row: any) => JSON.stringify(row.score_rule) },
                  { title: '解析', key: 'explanation' },
                ]" :data="workspace.objective_answers" size="small" :bordered="false" />
              </ProCard>

              <!-- SPJ -->
              <ProCard title="SPJ / 交互" :show-collapse="true" :collapsed="true">
                <NDescriptions :column="1" bordered size="small">
                  <NDescriptionsItem label="SPJ 语言">{{ p.spj_language_id || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="SPJ 源码"><NCode :code="p.spj_source || '-'" language="cpp" word-wrap /></NDescriptionsItem>
                  <NDescriptionsItem label="交互器语言">{{ p.interactor_language_id || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="交互器源码"><NCode :code="p.interactor_source || '-'" language="cpp" word-wrap /></NDescriptionsItem>
                  <NDescriptionsItem label="远程提供商">{{ p.remote_provider || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="远程题目 ID">{{ p.remote_problem_id || '-' }}</NDescriptionsItem>
                  <NDescriptionsItem label="扩展配置"><NCode :code="JSON.stringify(p.extra ?? {}, null, 2)" language="json" word-wrap /></NDescriptionsItem>
                </NDescriptions>
              </ProCard>
            </NFlex>
          </NScrollbar>
        </NFlex>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
