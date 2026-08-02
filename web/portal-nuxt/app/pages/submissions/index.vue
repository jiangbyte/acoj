<script setup lang="ts">
import { fetchSubmissionPage, type PortalSubmissionListItem } from '~/api/biz/submission'

const current = ref(1)
const size = ref(20)
const total = ref(0)
const loading = ref(false)
const records = ref<PortalSubmissionListItem[]>([])
const problemCode = ref('')
const result = ref('')

async function load() {
  loading.value = true
  try {
    const page = await fetchSubmissionPage({
      current: current.value,
      size: size.value,
      problem_code: problemCode.value || undefined,
      result: result.value || undefined,
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

const columns = [
  { accessorKey: 'id', header: 'ID' },
  { accessorKey: 'problem_code', header: '题号' },
  { accessorKey: 'user_nickname', header: '用户' },
  { accessorKey: 'language_key', header: '语言' },
  { accessorKey: 'result', header: '结果' },
  { accessorKey: 'time_ms', header: '耗时' },
  { accessorKey: 'memory_kb', header: '内存' },
  { accessorKey: 'created_at', header: '时间' },
]
</script>

<template>
  <UContainer class="py-8 space-y-6">
    <div class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold">
          提交记录
        </h1>
        <p class="text-muted text-sm mt-1">
          公开 Status 板
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <UInput v-model="problemCode" placeholder="题号" class="w-32" @keyup.enter="onSearch" />
        <UInput v-model="result" placeholder="结果如 AC" class="w-28" @keyup.enter="onSearch" />
        <UButton @click="onSearch">
          筛选
        </UButton>
      </div>
    </div>

    <UTable :data="records" :columns="columns" :loading="loading" class="w-full">
      <template #id-cell="{ row }">
        <NuxtLink
          class="font-mono text-primary text-xs"
          :to="`/submissions/${row.original.id}`"
        >
          {{ row.original.id }}
        </NuxtLink>
      </template>
      <template #result-cell="{ row }">
        <VerdictBadge :result="row.original.result" :status="row.original.status" />
      </template>
    </UTable>

    <div class="flex justify-end">
      <UPagination v-model:page="current" :items-per-page="size" :total="total" />
    </div>
  </UContainer>
</template>
