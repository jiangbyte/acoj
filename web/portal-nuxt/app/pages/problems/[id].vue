<script setup lang="ts">
import {
  fetchProblemDetail,
  fetchProblemLanguages,
  submitProblem,
  type PortalProblemDetail,
  type PortalProblemLanguage,
} from '~/api/biz/problem'

const route = useRoute()
const toast = useToast()
const id = computed(() => String(route.params.id || ''))

const problem = ref<PortalProblemDetail | null>(null)
const languages = ref<PortalProblemLanguage[]>([])
const loading = ref(true)

async function load() {
  if (!id.value) {
    toast.add({ title: '缺少题目 id', color: 'error' })
    await navigateTo('/problems')
    return
  }
  loading.value = true
  try {
    const [detail, langs] = await Promise.all([
      fetchProblemDetail(id.value),
      fetchProblemLanguages(id.value),
    ])
    problem.value = detail
    languages.value = langs ?? []
  }
  catch {
    await navigateTo('/problems')
  }
  finally {
    loading.value = false
  }
}

onMounted(() => load())
watch(id, () => {
  if (import.meta.client)
    load()
})
</script>

<template>
  <UContainer class="py-8">
    <div v-if="loading" class="text-muted">
      加载中…
    </div>
    <div v-else-if="problem" class="grid gap-8 lg:grid-cols-2">
      <ProblemStatement
        :code="problem.code"
        :title="problem.name"
        :description="problem.description"
        :time-limit-ms="problem.time_limit_ms"
        :memory-limit-kb="problem.memory_limit_kb"
      />
      <div class="space-y-3">
        <h2 class="text-lg font-semibold">
          提交代码
        </h2>
        <SubmitPanel
          :languages="languages"
          :submit-fn="(p) => submitProblem(id, p)"
        />
      </div>
    </div>
  </UContainer>
</template>
