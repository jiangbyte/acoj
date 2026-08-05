<script setup lang="ts">
import { ojContestApi } from '@/api'
import { MdPreview } from '@/components/editor'
import { createTagColor, dictTypeColor, dictTypeData, displayValue, formatDateTime, hasPermission } from '@/utils'
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const scoreboardLabel: Record<string, string> = {
  VISIBLE: '始终可见',
  AFTER_CONTEST: '赛后可见',
  AFTER_PARTICIPATION: '个人结束后可见',
  HIDDEN: '隐藏',
}

const state = reactive({
  loading: false,
  activeTab: 'basic',
  detail: {} as any,
})

const contestId = computed(() => String(route.query.id ?? ''))

const contestTypeValue = computed(() => {
  if (state.detail.is_private) return 'PRIVATE'
  return state.detail.is_rated ? 'RATED' : 'UNRATED'
})

onMounted(() => {
  if (!contestId.value) {
    window.$message.error('缺少竞赛 ID')
    goBack()
    return
  }
  const tab = route.query.tab ? String(route.query.tab) : 'basic'
  state.activeTab = tab
  void fetchDetail(contestId.value)
})

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojContestApi.detail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

function goBack() {
  router.push('/biz/contest/contest')
}

function goEdit() {
  if (!contestId.value) return
  router.push({ path: '/biz/contest/contest/edit', query: { id: contestId.value, tab: 'basic' } })
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical :size="12">
    <NFlex align="center" justify="space-between" class="shrink-0 px-2px">
      <NFlex align="center" :size="12">
        <NButton quaternary @click="goBack">
          返回
        </NButton>
        <span class="text-16px font-medium">竞赛详情</span>
        <NTag
          v-if="state.detail.lifecycle_status"
          size="small"
          :color="createTagColor(dictTypeColor('CONTEST_LIFECYCLE_STATUS', state.detail.lifecycle_status))"
          :bordered="false"
        >
          {{ dictTypeData('CONTEST_LIFECYCLE_STATUS', state.detail.lifecycle_status) || state.detail.lifecycle_status }}
        </NTag>
        <NTag
          size="small"
          :color="createTagColor(dictTypeColor('CONTEST_TYPE', contestTypeValue))"
          :bordered="false"
        >
          {{ dictTypeData('CONTEST_TYPE', contestTypeValue) || contestTypeValue }}
        </NTag>
        <span v-if="state.detail.key" class="text-gray-500">
          {{ state.detail.key }} · {{ state.detail.name }}
        </span>
      </NFlex>
      <NSpace>
        <NButton
          v-if="hasPermission('biz:contest:contest:update')"
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
                <NDescriptionsItem label="标识">
                  {{ displayValue(state.detail.key) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="名称">
                  {{ displayValue(state.detail.name) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="标签" :span="2">
                  {{
                    Array.isArray(state.detail.tag_names) && state.detail.tag_names.length
                      ? state.detail.tag_names.join('、')
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

          <NTabPane name="statement" tab="说明" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <MdPreview :value="state.detail.description || ''" :preview="true" />
            </NScrollbar>
          </NTabPane>

          <NTabPane name="schedule" tab="赛程与赛制" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="开始时间">
                  {{ formatDateTime(state.detail.start_time) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="结束时间">
                  {{ formatDateTime(state.detail.end_time) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="个人时长">
                  {{ state.detail.time_limit_seconds ? `${state.detail.time_limit_seconds} 秒` : '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="封榜">
                  {{ state.detail.freeze_seconds ? `${state.detail.freeze_seconds} 秒` : '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="赛制">
                  {{ dictTypeData('CONTEST_FORMAT', state.detail.format_name) || displayValue(state.detail.format_name) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="榜单可见性">
                  {{
                    scoreboardLabel[state.detail.scoreboard_visibility]
                      || displayValue(state.detail.scoreboard_visibility)
                  }}
                </NDescriptionsItem>
                <NDescriptionsItem label="分数精度">
                  {{ displayValue(state.detail.points_precision) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="锁定时间">
                  {{ formatDateTime(state.detail.locked_after) }}
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>

          <NTabPane name="access" tab="访问与 Rating" display-directive="show">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="公开可见">
                  {{ state.detail.is_visible ? '是' : '否' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="私有竞赛">
                  {{ state.detail.is_private ? '是' : '否' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="计入 Rating">
                  {{ state.detail.is_rated ? '是' : '否' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="无提交也计 Rating">
                  {{ state.detail.rate_all ? '是' : '否' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="Rating 下限">
                  {{ displayValue(state.detail.rating_floor) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="Rating 上限">
                  {{ displayValue(state.detail.rating_ceiling) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="使用答疑">
                  {{ state.detail.use_clarifications ? '是' : '否' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="正式参赛人数">
                  {{ displayValue(state.detail.user_count) }}
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>
        </NTabs>
      </ProCard>
    </NSpin>
  </NFlex>
</template>
