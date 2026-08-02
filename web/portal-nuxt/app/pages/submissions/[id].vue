<script setup lang="ts">
import { fetchSubmissionDetail, type PortalSubmissionDetail } from '~/api/biz/submission'
import { monacoLanguageFromExtension } from '~/utils/monacoLanguage'

const route = useRoute()
const toast = useToast()
const id = computed(() => String(route.params.id || ''))
const detail = ref<PortalSubmissionDetail | null>(null)
const loading = ref(true)

async function load() {
  if (!id.value) {
    toast.add({ title: '缺少提交 id', color: 'error' })
    await navigateTo('/submissions')
    return
  }
  loading.value = true
  try {
    detail.value = await fetchSubmissionDetail(id.value)
  }
  catch {
    await navigateTo('/submissions')
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

const monacoLang = computed(() => monacoLanguageFromExtension(null, 'cpp'))
</script>

<template>
  <UContainer class="py-8 space-y-6">
    <div v-if="loading" class="text-muted">
      加载中…
    </div>
    <template v-else-if="detail">
      <div class="space-y-2">
        <div class="flex flex-wrap items-center gap-3">
          <h1 class="text-xl font-semibold font-mono">
            {{ detail.id }}
          </h1>
          <VerdictBadge :result="detail.result" :status="detail.status" />
        </div>
        <p class="text-sm text-muted">
          {{ detail.problem_code }} {{ detail.problem_name }} · {{ detail.language_key }} ·
          {{ detail.user_nickname || detail.user_id }} ·
          {{ detail.time_ms }} ms / {{ detail.memory_kb }} KB
        </p>
      </div>

      <div v-if="detail.compile_output" class="rounded-lg bg-muted p-3 text-xs font-mono whitespace-pre-wrap">
        {{ detail.compile_output }}
      </div>

      <div v-if="detail.cases?.length" class="space-y-2">
        <h2 class="font-semibold">
          测试点
        </h2>
        <div
          v-for="c in detail.cases"
          :key="c.case_no"
          class="flex items-center justify-between rounded-lg border border-default px-3 py-2 text-sm"
        >
          <span>#{{ c.case_no }}</span>
          <VerdictBadge :result="c.result" />
          <span class="text-muted">{{ c.time_ms }} ms / {{ c.memory_kb }} KB</span>
        </div>
      </div>

      <div v-if="detail.source != null" class="space-y-2">
        <h2 class="font-semibold">
          源码
        </h2>
        <CodeEditor :model-value="detail.source" :language="monacoLang" :height="420" read-only />
      </div>
      <p v-else class="text-sm text-muted">
        源码不可见（受题目源码可见性限制）
      </p>
    </template>
  </UContainer>
</template>
