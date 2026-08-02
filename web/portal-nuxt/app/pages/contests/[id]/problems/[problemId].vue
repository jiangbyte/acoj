<script setup lang="ts">
import {
  fetchContestProblemDetail,
  submitContest,
  type PortalContestProblemDetail,
} from '~/api/biz/contest'

const route = useRoute()
const toast = useToast()
const contestId = computed(() => String(route.params.id || ''))
const problemId = computed(() => String(route.params.problemId || ''))

const detail = ref<PortalContestProblemDetail | null>(null)
const loading = ref(true)

async function load() {
  if (!contestId.value || !problemId.value) {
    toast.add({ title: '缺少竞赛或题目参数', color: 'error' })
    await navigateTo('/contests')
    return
  }
  loading.value = true
  try {
    detail.value = await fetchContestProblemDetail(contestId.value, problemId.value)
  }
  catch {
    await navigateTo(`/contests/${contestId.value}`)
  }
  finally {
    loading.value = false
  }
}

onMounted(() => load())
watch([contestId, problemId], () => {
  if (import.meta.client)
    load()
})
</script>

<template>
  <UContainer class="py-8 space-y-4">
    <NuxtLink
      class="text-sm text-primary"
      :to="`/contests/${contestId}`"
    >
      ← 返回竞赛
    </NuxtLink>
    <div v-if="loading" class="text-muted">
      加载中…
    </div>
    <div v-else-if="detail" class="grid gap-8 lg:grid-cols-2">
      <ProblemStatement
        :code="detail.label"
        :title="detail.problem_name"
        :description="detail.description"
        :time-limit-ms="detail.time_limit_ms"
        :memory-limit-kb="detail.memory_limit_kb"
      />
      <div class="space-y-3">
        <h2 class="text-lg font-semibold">
          提交代码
        </h2>
        <SubmitPanel
          :languages="detail.languages"
          :submit-fn="(p) => submitContest(contestId, { problem_id: problemId, ...p })"
        />
      </div>
    </div>
  </UContainer>
</template>
