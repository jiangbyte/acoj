<script setup lang="ts">
import { fetchProblemPage, type PortalProblemListItem } from '~/api/biz/problem'

const keyword = ref('')
const current = ref(1)
const size = ref(20)
const total = ref(0)
const loading = ref(false)
const records = ref<PortalProblemListItem[]>([])

async function load() {
  loading.value = true
  try {
    const page = await fetchProblemPage({
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

const columns = [
  { accessorKey: 'code', header: '题号' },
  { accessorKey: 'name', header: '标题' },
  { accessorKey: 'ac_rate', header: '通过率' },
  { accessorKey: 'user_count', header: '通过人数' },
]
</script>

<template>
  <UContainer class="py-8 space-y-6">
    <div class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold">
          题库
        </h1>
        <p class="text-muted text-sm mt-1">
          公开练习题，登录后即可提交
        </p>
      </div>
      <div class="flex gap-2">
        <UInput
          v-model="keyword"
          icon="i-lucide-search"
          placeholder="搜索题号 / 标题"
          class="w-64"
          @keyup.enter="onSearch"
        />
        <UButton color="primary" @click="onSearch">
          搜索
        </UButton>
      </div>
    </div>

    <UTable
      :data="records"
      :columns="columns"
      :loading="loading"
      class="w-full"
    >
      <template #code-cell="{ row }">
        <NuxtLink
          class="font-mono text-primary"
          :to="`/problems/${row.original.id}`"
        >
          {{ row.original.code }}
        </NuxtLink>
      </template>
      <template #name-cell="{ row }">
        <NuxtLink :to="`/problems/${row.original.id}`">
          {{ row.original.name }}
        </NuxtLink>
      </template>
      <template #ac_rate-cell="{ row }">
        {{ ((row.original.ac_rate || 0) * 100).toFixed(1) }}%
      </template>
    </UTable>

    <div class="flex justify-end">
      <UPagination
        v-model:page="current"
        :items-per-page="size"
        :total="total"
      />
    </div>
  </UContainer>
</template>
