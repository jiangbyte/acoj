<script setup lang="ts">
import { fetchContestPage, type PortalContestBrief } from '~/api/biz/contest'
import { fetchProblemPage, type PortalProblemListItem } from '~/api/biz/problem'
import { format } from 'date-fns'

const contests = ref<PortalContestBrief[]>([])
const problems = ref<PortalProblemListItem[]>([])

onMounted(async () => {
  try {
    const [c, p] = await Promise.all([
      fetchContestPage({ current: 1, size: 5 }),
      fetchProblemPage({ current: 1, size: 8 }),
    ])
    contests.value = c?.records ?? []
    problems.value = p?.records ?? []
  }
  catch {
    // offline / api down — keep empty
  }
})

function fmt(t: string) {
  try {
    return format(new Date(t), 'MM-dd HH:mm')
  }
  catch {
    return t
  }
}
</script>

<template>
  <div>
    <UPageHero
      title="ACOJ"
      description="在线评测门户：刷题、竞赛、提交与 Rating 一站完成。"
      :links="[
        { label: '进入题库', to: '/problems', size: 'lg' },
        { label: '查看竞赛', to: '/contests', color: 'neutral', variant: 'outline', size: 'lg' },
      ]"
    />

    <UContainer class="pb-16 space-y-12">
      <section class="space-y-4">
        <div class="flex items-center justify-between">
          <h2 class="text-xl font-semibold">
            近期竞赛
          </h2>
          <UButton to="/contests" variant="ghost" trailing-icon="i-lucide-arrow-right">
            全部
          </UButton>
        </div>
        <div class="grid gap-3 md:grid-cols-2">
          <NuxtLink
            v-for="c in contests"
            :key="c.id"
            :to="`/contests/${c.id}`"
            class="rounded-xl border border-default p-4 hover:bg-elevated transition"
          >
            <div class="flex items-center gap-2 mb-1">
              <span class="font-medium">{{ c.name }}</span>
              <ContestStatusBadge :status="c.lifecycle_status" />
            </div>
            <p class="text-sm text-muted">
              {{ fmt(c.start_time) }} — {{ fmt(c.end_time) }}
            </p>
          </NuxtLink>
          <p v-if="!contests.length" class="text-sm text-muted">
            暂无公开竞赛
          </p>
        </div>
      </section>

      <section class="space-y-4">
        <div class="flex items-center justify-between">
          <h2 class="text-xl font-semibold">
            题库精选
          </h2>
          <UButton to="/problems" variant="ghost" trailing-icon="i-lucide-arrow-right">
            全部
          </UButton>
        </div>
        <div class="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
          <NuxtLink
            v-for="p in problems"
            :key="p.id"
            :to="`/problems/${p.id}`"
            class="rounded-lg border border-default px-3 py-3 hover:bg-elevated transition"
          >
            <div class="font-mono text-sm text-primary">
              {{ p.code }}
            </div>
            <div class="truncate text-sm mt-1">
              {{ p.name }}
            </div>
          </NuxtLink>
          <p v-if="!problems.length" class="text-sm text-muted">
            暂无公开题目
          </p>
        </div>
      </section>
    </UContainer>
  </div>
</template>
