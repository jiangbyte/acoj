<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { ojSubmissionApi } from '@/api'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { displayValue, formatDateTime, hasPermission, resolveFileUrl } from '@/utils'
import { monacoLanguageFromExtension } from '@/views/biz/problem/shared/monacoLanguage'
import SubmissionPerformancePanel from './SubmissionPerformancePanel.vue'
import { NAvatar, NFlex, NTag } from 'naive-ui'
import { computed, reactive } from 'vue'
import { useRouter } from 'vue-router'

const emit = defineEmits<{ rejudged: [] }>()
const router = useRouter()

const statusColor: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
  AC: 'success',
  WA: 'error',
  TLE: 'warning',
  MLE: 'warning',
  RE: 'error',
  CE: 'error',
  OLE: 'warning',
  SE: 'error',
  IE: 'error',
  COMPLETED: 'info',
  JUDGING: 'info',
  QUEUED: 'info',
  FAILED: 'error',
}

const state = reactive({
  showModal: false,
  loading: false,
  rejudgeLoading: false,
  detail: {} as any,
})

const monacoLanguage = computed(() => {
  const key = String(state.detail.language_key || '')
  if (key.startsWith('py'))
    return 'python'
  if (key.startsWith('java'))
    return 'java'
  if (key.startsWith('go'))
    return 'go'
  if (key.includes('js') || key.includes('node'))
    return 'javascript'
  if (key.startsWith('rs') || key.includes('rust'))
    return 'rust'
  return monacoLanguageFromExtension('.cpp')
})

const userDisplayName = computed(() => state.detail.user_nickname || '-')

const caseColumns = computed<DataTableColumns<any>>(() => [
  { title: '#', key: 'case_no', width: 50 },
  {
    title: '结果',
    key: 'result',
    width: 90,
    render: row => (
      <NTag size="small" type={statusColor[String(row.result ?? '')] ?? 'default'}>
        {String(row.result ?? '-')}
      </NTag>
    ),
  },
  { title: '分', key: 'score', width: 60 },
  {
    title: '时间',
    key: 'time_ms',
    width: 80,
    render: row => `${row.time_ms ?? 0}ms`,
  },
  {
    title: '内存',
    key: 'memory_kb',
    width: 90,
    render: row => `${row.memory_kb ?? 0}KB`,
  },
])

async function openModal(id: string) {
  state.detail = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojSubmissionApi.detail({ id })
    state.detail = response.data ?? {}
  }
  finally {
    state.loading = false
  }
}

async function handleRejudge() {
  if (!state.detail.id)
    return
  if (state.detail.locked_at) {
    window.$message.warning('该提交已锁定，无法重判')
    return
  }
  state.rejudgeLoading = true
  try {
    const response = await ojSubmissionApi.rejudge({ ids: [state.detail.id] })
    const data = response.data ?? {}
    if (data.failed) {
      window.$message.error(data.errors?.[0] || '重判失败')
      return
    }
    window.$message.info('已入队重判…')
    try {
      await ojSubmissionApi.watchSubmissionEvents(state.detail.id, {
        maxWaitSec: 120,
        onUpdate: () => {},
      })
    }
    catch {
      await ojSubmissionApi.pollSubmissionUntilDone(state.detail.id, {
        maxWaitSec: 120,
        fetchDetail: async (id) => (await ojSubmissionApi.detail({ id })).data ?? {},
        onUpdate: () => {},
      })
    }
    await fetchDetail(state.detail.id)
    window.$message.success('重判完成')
    emit('rejudged')
  }
  finally {
    state.rejudgeLoading = false
  }
}

function goProblem() {
  if (!state.detail.problem_id)
    return
  router.push({ path: '/biz/problem/problem/edit', query: { id: state.detail.problem_id } })
}

defineExpose({ openModal })
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    title="提交详情"
    class="w-[min(960px,96vw)]"
    :segmented="{ content: true, footer: true }"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NSpace vertical :size="14">
          <NDescriptions label-placement="left" bordered :column="2" size="small">
            <NDescriptionsItem label="ID">
              <span class="font-mono text-12px">{{ displayValue(state.detail.id) }}</span>
            </NDescriptionsItem>
            <NDescriptionsItem label="类型">
              {{ displayValue(state.detail.kind) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="用户">
              <NFlex align="center" :size="8">
                <NAvatar
                  v-if="resolveFileUrl(state.detail.user_avatar)"
                  round
                  :size="28"
                  :src="resolveFileUrl(state.detail.user_avatar)!"
                  :img-props="{ referrerPolicy: 'no-referrer' }"
                />
                <NAvatar v-else round :size="28" color="#d9d9d9">
                  {{ userDisplayName?.[0]?.toUpperCase() }}
                </NAvatar>
                <span>{{ displayValue(userDisplayName) }}</span>
              </NFlex>
            </NDescriptionsItem>
            <NDescriptionsItem label="题目">
              {{ displayValue(state.detail.problem_code) }} · {{ displayValue(state.detail.problem_name) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="竞赛">
              {{ displayValue(state.detail.contest_name || state.detail.contest_key || '-') }}
            </NDescriptionsItem>
            <NDescriptionsItem label="语言">
              {{ displayValue(state.detail.language_key) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="状态">
              <NTag size="small" :type="statusColor[String(state.detail.status ?? '')] ?? 'default'">
                {{ displayValue(state.detail.status) }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="结果">
              <NTag size="small" :type="statusColor[String(state.detail.result ?? '')] ?? 'default'">
                {{ displayValue(state.detail.result || '-') }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="得分">
              {{ displayValue(state.detail.score) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="耗时 / 内存">
              {{ state.detail.time_ms ?? 0 }} ms / {{ state.detail.memory_kb ?? 0 }} KB
            </NDescriptionsItem>
            <NDescriptionsItem label="提交时间" :span="2">
              {{ formatDateTime(state.detail.created_at) }}
            </NDescriptionsItem>
            <NDescriptionsItem v-if="state.detail.error" label="错误" :span="2">
              {{ displayValue(state.detail.error) }}
            </NDescriptionsItem>
          </NDescriptions>

          <div class="text-13px font-medium">
            源代码
          </div>
          <MonacoEditor
            :value="state.detail.source || ''"
            :language="monacoLanguage"
            height="200px"
            theme="vs"
            :options="{ readOnly: true }"
          />

          <template v-if="state.detail.compile_output">
            <div class="text-13px font-medium">
              编译输出
            </div>
            <NInput :value="state.detail.compile_output" type="textarea" readonly :rows="3" class="font-mono" />
          </template>

          <template v-if="(state.detail.cases || []).length">
            <div class="text-13px font-medium">
              测例结果
            </div>
            <NDataTable
              size="small"
              :bordered="false"
              :columns="caseColumns"
              :data="state.detail.cases || []"
              :pagination="false"
              :max-height="180"
            />
          </template>

          <SubmissionPerformancePanel
            v-if="state.detail.result === 'AC' && state.detail.id"
            :key="state.detail.id"
            :submission-id="state.detail.id"
          />
        </NSpace>
      </NSpin>
    </NScrollbar>
    <template #footer>
      <NFlex justify="space-between">
        <NButton quaternary :disabled="!state.detail.problem_id" @click="goProblem">
          打开题目
        </NButton>
        <NFlex :size="8">
          <NButton @click="state.showModal = false">
            关闭
          </NButton>
          <NButton
            v-if="hasPermission('biz:submission:submission:rejudge')"
            type="primary"
            :loading="state.rejudgeLoading"
            :disabled="!!state.detail.locked_at"
            @click="handleRejudge"
          >
            重判
          </NButton>
        </NFlex>
      </NFlex>
    </template>
  </NModal>
</template>
