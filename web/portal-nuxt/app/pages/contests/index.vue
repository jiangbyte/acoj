<script setup lang="ts">
import { fetchContestPage, type PortalContestBrief } from '~/api/biz/contest'
import { format } from 'date-fns'

const keyword = ref('')
const current = ref(1)
const size = ref(20)
const total = ref(0)
const loading = ref(false)
const records = ref<PortalContestBrief[]>([])

async function load() {
  loading.value = true
  try {
    const page = await fetchContestPage({
      current: current.value,
      size: size.value,
      keyword: keyword.value || undefined,
    })
    records.value = page?.records ?? []
    total.value = page?.total ?? 0
  }
  catch {
    records.value = []
    total.value = 0
  }
  finally {
    loading.value = false
  }
}

onMounted(() => load())
watch([current, size], () => {
  if (import.meta.client)
    load()
})

function onSearch() {
  current.value = 1
  load()
}

function fmt(t: string) {
  try {
    return format(new Date(t), 'yyyy-MM-dd HH:mm')
  }
  catch {
    return t
  }
}
</script>

<template>
  <UContainer class="py-8 space-y-6">
    <div class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold">
          竞赛
        </h1>
        <p class="text-muted text-sm mt-1">
          公开竞赛列表
        </p>
      </div>
      <div class="flex gap-2">
        <UInput
          v-model="keyword"
          icon="i-lucide-search"
          placeholder="搜索竞赛"
          class="w-64"
          @keyup.enter="onSearch"
        />
        <UButton @click="onSearch">
          搜索
        </UButton>
      </div>
    </div>

    <div v-if="loading" class="text-muted text-sm">
      加载中…
    </div>
    <div v-else class="space-y-3">
      <NuxtLink
        v-for="c in records"
        :key="c.id"
        :to="`/contests/${c.id}`"
        class="block rounded-xl border border-default px-5 py-4 transition hover:bg-elevated"
      >
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="space-y-1">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-lg">{{ c.name }}</span>
              <ContestStatusBadge :status="c.lifecycle_status" />
              <UBadge v-if="c.is_rated" color="primary" variant="subtle">
                Rated
              </UBadge>
            </div>
            <p class="text-sm text-muted">
              {{ c.key }} · {{ c.format_name }} · {{ c.user_count }} 人报名
            </p>
            <p class="text-sm text-muted">
              {{ fmt(c.start_time) }} — {{ fmt(c.end_time) }}
            </p>
          </div>
          <UIcon name="i-lucide-chevron-right" class="size-5 text-muted" />
        </div>
      </NuxtLink>
      <p v-if="!records.length" class="text-muted text-sm">
        暂无竞赛
      </p>
    </div>

    <div class="flex justify-end">
      <UPagination v-model:page="current" :items-per-page="size" :total="total" />
    </div>
  </UContainer>
</template>
