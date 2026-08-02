<script setup lang="ts">
import { fetchRatingRank, type PortalRatingRankItem } from '~/api/biz/rank'

const current = ref(1)
const size = ref(50)
const total = ref(0)
const loading = ref(false)
const records = ref<PortalRatingRankItem[]>([])

async function load() {
  loading.value = true
  try {
    const page = await fetchRatingRank({ current: current.value, size: size.value })
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

const columns = [
  { accessorKey: 'rank', header: '#' },
  { accessorKey: 'nickname', header: '用户' },
  { accessorKey: 'rating', header: 'Rating' },
]
</script>

<template>
  <UContainer class="py-8 space-y-6">
    <div>
      <h1 class="text-2xl font-semibold">
        Rating 排名
      </h1>
      <p class="text-muted text-sm mt-1">
        按门户用户当前 Rating 排序
      </p>
    </div>

    <UTable :data="records" :columns="columns" :loading="loading" class="w-full">
      <template #nickname-cell="{ row }">
        <div class="flex items-center gap-2">
          <UAvatar
            :src="row.original.avatar || undefined"
            :alt="row.original.nickname || ''"
            size="xs"
            icon="i-lucide-user"
          />
          <span>{{ row.original.nickname || row.original.account_id }}</span>
        </div>
      </template>
    </UTable>

    <div class="flex justify-end">
      <UPagination v-model:page="current" :items-per-page="size" :total="total" />
    </div>
  </UContainer>
</template>
