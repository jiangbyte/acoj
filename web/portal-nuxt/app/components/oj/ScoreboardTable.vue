<script setup lang="ts">
const props = defineProps<{
  board: Record<string, unknown> | null
}>()

const rows = computed(() => {
  const data = props.board
  if (!data)
    return [] as Array<Record<string, unknown>>
  const list = data.rows
  return Array.isArray(list) ? (list as Array<Record<string, unknown>>) : []
})

const columns = [
  { accessorKey: 'rank', header: '#' },
  { accessorKey: 'account_id', header: '选手' },
  { accessorKey: 'score', header: '得分' },
  { accessorKey: 'cumtime', header: '罚时' },
  { accessorKey: 'is_disqualified', header: 'DQ' },
]
</script>

<template>
  <div v-if="!board" class="text-muted text-sm">
    暂无榜单数据
  </div>
  <div v-else class="space-y-2">
    <p v-if="board.is_frozen" class="text-sm text-warning">
      榜单已封榜
    </p>
    <UTable :data="rows" :columns="columns" class="w-full">
      <template #is_disqualified-cell="{ row }">
        {{ row.original.is_disqualified ? '是' : '-' }}
      </template>
    </UTable>
  </div>
</template>
