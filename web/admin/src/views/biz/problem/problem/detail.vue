<script setup lang="ts">
import { ojProblemApi } from '@/api'
import { MdPreview } from '@/components/editor'
import { displayValue, formatDateTime, hasPermission } from '@/utils'
import { dictTypeData } from '@/utils/dict'
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const statusLabel: Record<string, string> = {
  draft: '草稿',
  ready: '就绪',
  published: '已发布',
}

const visibilityLabel: Record<string, string> = {
  FOLLOW: '跟随全局',
  ALWAYS: '始终可见',
  SOLVED: 'AC 后可见',
  ONLY_OWN: '仅自己可见',
}

const state = reactive({
  loading: false,
  activeTab: 'basic',
  detail: {} as any,
})

const problemId = computed(() => String(route.query.id ?? ''))

const statusType = computed(() => {
  const status = state.detail.status
  if (status === 'published') {
    return 'success'
  }
  if (status === 'ready') {
    return 'info'
  }
  return 'default'
})

onMounted(() => {
  if (!problemId.value) {
    window.$message.error('缺少题目 ID')
    goBack()
    return
  }
  const tab = route.query.tab ? String(route.query.tab) : 'basic'
  state.activeTab = tab
  void fetchDetail(problemId.value)
})

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojProblemApi.detail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

function goBack() {
  router.push('/biz/problem/problem')
}

function goEdit() {
  if (!problemId.value) {
    return
  }
  router.push({ path: '/biz/problem/problem/edit', query: { id: problemId.value, tab: 'basic' } })
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical :size="12">
    <NFlex align="center" justify="space-between" class="shrink-0 px-2px">
      <NFlex align="center" :size="12">
        <NButton quaternary @click="goBack">
          返回
        </NButton>
        <span class="text-16px font-medium">题目详情</span>
        <NTag v-if="state.detail.status" size="small" :type="statusType">
          {{ statusLabel[state.detail.status] || state.detail.status }}
        </NTag>
        <span v-if="state.detail.code" class="text-gray-500">
          {{ state.detail.code }} · {{ state.detail.name }}
        </span>
      </NFlex>
      <NSpace>
        <NButton
          v-if="hasPermission('biz:problem:problem:update')"
          type="primary"
          @click="goEdit"
        >
          编辑
        </NButton>
      </NSpace>
    </NFlex>

    <NSpin :show="state.loading" class="min-h-0 flex-1">
      <ProCard class="h-full" content-class="h-full flex flex-col min-h-0" :segmented="{ content: true }">
        <NTabs v-model:value="state.activeTab" type="line" class="h-full min-h-0 flex flex-col">
          <NTabPane name="basic" tab="基本信息" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="编码">
                  {{ displayValue(state.detail.code) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="标题">
                  {{ displayValue(state.detail.name) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="分组">
                  {{ displayValue(state.detail.group_name || state.detail.group_id) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="类型">
                  {{
                    Array.isArray(state.detail.type_names) && state.detail.type_names.length
                      ? state.detail.type_names.join('、')
                      : '-'
                  }}
                </NDescriptionsItem>
                <NDescriptionsItem label="摘要" :span="2">
                  <div class="whitespace-pre-wrap">
                    {{ displayValue(state.detail.summary) }}
                  </div>
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>

          <NTabPane name="statement" tab="题面" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <MdPreview :value="state.detail.description || ''" :preview="true" />
            </NScrollbar>
          </NTabPane>

          <NTabPane name="limits" tab="限制与计分" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="时间限制">
                  {{ displayValue(state.detail.time_limit_ms) }} ms
                </NDescriptionsItem>
                <NDescriptionsItem label="内存限制">
                  {{ displayValue(state.detail.memory_limit_kb) }} KB
                </NDescriptionsItem>
                <NDescriptionsItem label="题目分值">
                  {{ displayValue(state.detail.points) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="难度">
                  {{ dictTypeData('PROBLEM_DIFFICULTY', state.detail.difficulty) || displayValue(state.detail.difficulty) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="允许部分分">
                  {{ state.detail.partial ? '是' : '否' }}
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>

          <NTabPane name="publish" tab="发布与可见性" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="状态">
                  {{ statusLabel[state.detail.status] || displayValue(state.detail.status) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="公开题库">
                  {{ state.detail.is_public ? '是' : '否（竞赛专用）' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="发布时间">
                  {{ formatDateTime(state.detail.published_at) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="源码可见性" :span="2">
                  {{
                    visibilityLabel[state.detail.submission_source_visibility]
                      || displayValue(state.detail.submission_source_visibility)
                  }}
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>

          <NTabPane name="stats" tab="统计" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="通过人数">
                  {{ displayValue(state.detail.user_count) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="通过率">
                  {{ displayValue(state.detail.ac_rate) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="创建时间">
                  {{ formatDateTime(state.detail.created_at) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="更新时间">
                  {{ formatDateTime(state.detail.updated_at) }}
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>
        </NTabs>
      </ProCard>
    </NSpin>
  </NFlex>
</template>
