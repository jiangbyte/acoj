<script setup lang="ts">
import {
  createClarificationThread,
  fetchClarifications,
  fetchContestDetail,
  fetchContestProblems,
  fetchContestScoreboard,
  fetchMyClarificationThreads,
  fetchMyContestSubmissions,
  joinContest,
  leaveContest,
  type PortalContestBrief,
  type PortalContestProblemMeta,
} from '~/api/biz/contest'
import { format } from 'date-fns'

const route = useRoute()
const toast = useToast()
const { isLoggedIn } = useAuth()
const id = computed(() => String(route.params.id || ''))

const tab = ref('overview')
const contest = ref<PortalContestBrief | null>(null)
const problems = ref<PortalContestProblemMeta[]>([])
const board = ref<Record<string, unknown> | null>(null)
const mySubs = ref<Array<Record<string, unknown>>>([])
const clarifications = ref<Array<Record<string, unknown>>>([])
const myThreads = ref<Array<Record<string, unknown>>>([])
const accessCode = ref('')
const loading = ref(true)
const askTitle = ref('')
const askBody = ref('')

const tabs = [
  { label: '概览', value: 'overview' },
  { label: '题目', value: 'problems' },
  { label: '榜单', value: 'scoreboard' },
  { label: '答疑', value: 'clarifications' },
  { label: '我的提交', value: 'subs' },
]

function fmt(t?: string | null) {
  if (!t)
    return '-'
  try {
    return format(new Date(t), 'yyyy-MM-dd HH:mm')
  }
  catch {
    return t
  }
}

async function ensureLogin() {
  if (isLoggedIn.value)
    return true
  toast.add({ title: '请先登录', color: 'warning' })
  await navigateTo({ path: '/auth/login', query: { redirect: route.fullPath } })
  return false
}

async function loadBase() {
  if (!id.value) {
    toast.add({ title: '缺少竞赛 id', color: 'error' })
    await navigateTo('/contests')
    return
  }
  loading.value = true
  try {
    contest.value = await fetchContestDetail(id.value)
    problems.value = (await fetchContestProblems(id.value)) ?? []
    clarifications.value = (await fetchClarifications(id.value)) ?? []
  }
  catch {
    await navigateTo('/contests')
  }
  finally {
    loading.value = false
  }
}

async function loadTab() {
  if (!id.value)
    return
  if (tab.value === 'scoreboard') {
    try {
      board.value = await fetchContestScoreboard(id.value)
    }
    catch {
      board.value = null
    }
  }
  if (tab.value === 'subs' && isLoggedIn.value) {
    mySubs.value = (await fetchMyContestSubmissions(id.value)) ?? []
  }
  if (tab.value === 'clarifications' && isLoggedIn.value) {
    myThreads.value = (await fetchMyClarificationThreads(id.value)) ?? []
  }
}

onMounted(() => {
  loadBase().then(() => loadTab())
})
watch(id, () => {
  if (import.meta.client)
    loadBase().then(() => loadTab())
})
watch(tab, () => {
  if (import.meta.client)
    loadTab()
})

async function onJoin() {
  if (!(await ensureLogin()))
    return
  await joinContest(id.value, { access_code: accessCode.value || undefined })
  toast.add({ title: '报名成功', color: 'success' })
  await loadBase()
}

async function onLeave() {
  if (!(await ensureLogin()))
    return
  await leaveContest(id.value)
  toast.add({ title: '已取消报名', color: 'success' })
  await loadBase()
}

async function onAsk() {
  if (!(await ensureLogin()))
    return
  if (!askTitle.value.trim() || !askBody.value.trim()) {
    toast.add({ title: '请填写标题和内容', color: 'warning' })
    return
  }
  await createClarificationThread(id.value, {
    title: askTitle.value,
    body: askBody.value,
  })
  askTitle.value = ''
  askBody.value = ''
  toast.add({ title: '已提交提问', color: 'success' })
  myThreads.value = await fetchMyClarificationThreads(id.value)
}
</script>

<template>
  <UContainer class="py-8 space-y-6">
    <div v-if="loading" class="text-muted">
      加载中…
    </div>
    <template v-else-if="contest">
      <div class="space-y-3">
        <div class="flex flex-wrap items-center gap-2">
          <h1 class="text-2xl font-semibold">
            {{ contest.name }}
          </h1>
          <ContestStatusBadge :status="contest.lifecycle_status" />
          <UBadge v-if="contest.is_rated" color="primary" variant="subtle">
            Rated
          </UBadge>
        </div>
        <p class="text-sm text-muted">
          {{ contest.key }} · {{ contest.format_name }} · {{ fmt(contest.start_time) }} — {{ fmt(contest.end_time) }}
        </p>
        <div class="flex flex-wrap items-center gap-2">
          <template v-if="!contest.joined">
            <UInput
              v-if="contest.is_private"
              v-model="accessCode"
              placeholder="准入码"
              class="w-40"
            />
            <UButton color="primary" @click="onJoin">
              报名参赛
            </UButton>
          </template>
          <template v-else>
            <UBadge color="success" variant="subtle">
              已报名
            </UBadge>
            <UButton
              v-if="contest.lifecycle_status === 'SCHEDULED'"
              color="neutral"
              variant="outline"
              @click="onLeave"
            >
              取消报名
            </UButton>
          </template>
        </div>
      </div>

      <UTabs v-model="tab" :items="tabs" class="w-full">
        <template #content="{ item }">
          <div v-if="item.value === 'overview'" class="prose prose-neutral dark:prose-invert max-w-none pt-4">
            <Comark :markdown="contest.description || contest.summary || '暂无说明'" />
          </div>

          <div v-else-if="item.value === 'problems'" class="space-y-2 pt-4">
            <NuxtLink
              v-for="p in problems"
              :key="p.id"
              class="flex items-center justify-between rounded-lg border border-default px-4 py-3 hover:bg-elevated"
              :to="`/contests/${id}/problems/${p.problem_id}`"
            >
              <div>
                <span class="font-mono font-semibold mr-2">{{ p.label }}</span>
                <span>{{ p.problem_name || p.problem_code || '题目' }}</span>
              </div>
              <span class="text-sm text-muted">{{ p.points }} pts</span>
            </NuxtLink>
            <p v-if="!problems.length" class="text-sm text-muted">
              暂无题目（未开始且未报名时不可见）
            </p>
          </div>

          <div v-else-if="item.value === 'scoreboard'" class="pt-4">
            <ScoreboardTable :board="board" />
          </div>

          <div v-else-if="item.value === 'clarifications'" class="space-y-6 pt-4">
            <div class="space-y-3">
              <h3 class="font-semibold">
                公告答疑
              </h3>
              <div
                v-for="c in clarifications"
                :key="String(c.id)"
                class="rounded-lg border border-default p-4 space-y-1"
              >
                <div class="font-medium">
                  {{ c.title }}
                </div>
                <div class="text-sm whitespace-pre-wrap">
                  {{ c.body }}
                </div>
              </div>
              <p v-if="!clarifications.length" class="text-sm text-muted">
                暂无公告
              </p>
            </div>
            <div v-if="contest.use_clarifications" class="space-y-3">
              <h3 class="font-semibold">
                我的提问
              </h3>
              <UInput v-model="askTitle" placeholder="标题" />
              <UTextarea v-model="askBody" placeholder="内容" :rows="4" />
              <UButton @click="onAsk">
                提问
              </UButton>
              <div
                v-for="t in myThreads"
                :key="String(t.id)"
                class="rounded-lg border border-default p-4"
              >
                <div class="font-medium">
                  {{ t.title }}
                </div>
                <div class="text-xs text-muted mt-1">
                  {{ t.status }}
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="item.value === 'subs'" class="space-y-2 pt-4">
            <div
              v-for="s in mySubs"
              :key="String(s.submission_id)"
              class="flex items-center justify-between rounded-lg border border-default px-4 py-3"
            >
              <div class="space-y-1">
                <NuxtLink
                  class="font-mono text-primary"
                  :to="`/submissions/${s.submission_id}`"
                >
                  {{ s.submission_id }}
                </NuxtLink>
                <div class="text-sm text-muted">
                  {{ s.language_key }} · {{ s.created_at }}
                </div>
              </div>
              <VerdictBadge :result="String(s.result || '')" :status="String(s.status || '')" />
            </div>
            <p v-if="!mySubs.length" class="text-sm text-muted">
              {{ isLoggedIn ? '暂无提交' : '登录后查看' }}
            </p>
          </div>
        </template>
      </UTabs>
    </template>
  </UContainer>
</template>
