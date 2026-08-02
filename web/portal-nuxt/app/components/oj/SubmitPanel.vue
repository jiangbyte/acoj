<script setup lang="ts">
import {
  fetchSubmissionDetail,
} from '~/api/biz/submission'
import {
  pollSubmissionUntilDone,
  watchSubmissionEvents,
  type SubmissionWatchSnapshot,
} from '~/api/biz/submissionWatch'
import { monacoLanguageFromExtension } from '~/utils/monacoLanguage'

const props = defineProps<{
  languages: Array<{ language_key: string, label?: string | null, extension?: string | null }>
  submitFn: (payload: { language_key: string, source: string }) => Promise<{ submission_id: string }>
}>()

const emit = defineEmits<{
  submitted: [submissionId: string]
}>()

const router = useRouter()
const route = useRoute()
const toast = useToast()
const { isLoggedIn } = useAuth()
const runtimeConfig = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('token')
const apiBaseUrl = computed(() => String(runtimeConfig.public.apiBaseUrl || ''))

const languageKey = ref('')
const source = ref('')
const submitting = ref(false)
const snap = ref<SubmissionWatchSnapshot | null>(null)
let abort: AbortController | null = null

const languageItems = computed(() =>
  props.languages.map(l => ({
    label: l.label || l.language_key,
    value: l.language_key,
  })),
)

const monacoLang = computed(() => {
  const found = props.languages.find(l => l.language_key === languageKey.value)
  return monacoLanguageFromExtension(found?.extension)
})

watch(
  () => props.languages,
  (list) => {
    if (!list.length)
      return
    if (!list.some(l => l.language_key === languageKey.value))
      languageKey.value = list[0]!.language_key
  },
  { immediate: true },
)

async function ensureLogin() {
  if (isLoggedIn.value)
    return true
  toast.add({ title: '请先登录', color: 'warning' })
  await router.push({ path: '/auth/login', query: { redirect: route.fullPath } })
  return false
}

async function onSubmit() {
  if (!(await ensureLogin()))
    return
  if (!languageKey.value || !source.value.trim()) {
    toast.add({ title: '请选择语言并填写代码', color: 'warning' })
    return
  }
  submitting.value = true
  snap.value = null
  abort?.abort()
  abort = new AbortController()
  try {
    const res = await props.submitFn({
      language_key: languageKey.value,
      source: source.value,
    })
    const submissionId = res.submission_id
    emit('submitted', submissionId)
    toast.add({ title: '已提交', description: submissionId, color: 'success' })
    try {
      await watchSubmissionEvents(submissionId, {
        apiBaseUrl: apiBaseUrl.value,
        token: tokenCookie.value,
        signal: abort.signal,
        onUpdate: (s) => {
          snap.value = s
        },
      })
    }
    catch {
      await pollSubmissionUntilDone(submissionId, {
        signal: abort.signal,
        fetchDetail: id => fetchSubmissionDetail(id),
        onUpdate: (s) => {
          snap.value = s
        },
      })
    }
  }
  catch {
    // http interceptor toasts
  }
  finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => abort?.abort())
</script>

<template>
  <div class="space-y-3">
    <div class="flex flex-wrap items-center gap-3">
      <USelect
        v-model="languageKey"
        :items="languageItems"
        value-key="value"
        class="w-48"
        placeholder="选择语言"
      />
      <UButton
        color="primary"
        icon="i-lucide-send"
        :loading="submitting"
        :disabled="!languages.length"
        @click="onSubmit"
      >
        提交
      </UButton>
      <VerdictBadge v-if="snap" :result="snap.result" :status="snap.status" />
      <NuxtLink
        v-if="snap?.submission_id"
        class="text-sm text-primary"
        :to="`/submissions/${snap.submission_id}`"
      >
        查看详情
      </NuxtLink>
    </div>
    <CodeEditor v-model="source" :language="monacoLang" :height="420" />
    <div v-if="snap?.compile_output" class="rounded-lg bg-muted p-3 text-xs font-mono whitespace-pre-wrap">
      {{ snap.compile_output }}
    </div>
  </div>
</template>
