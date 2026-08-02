<script setup lang="ts">
defineProps<{
  title: string
  isOwner?: boolean
}>()

const emit = defineEmits<{
  'update:title': [title: string]
  rename: []
  delete: []
}>()
</script>

<template>
  <div class="flex items-center gap-2 min-w-0">
    <UDropdownMenu
      :items="[
        [
          { label: '重命名', icon: 'i-lucide-pencil', onSelect: () => emit('rename') },
          {
            label: '删除',
            icon: 'i-lucide-trash-2',
            color: 'error' as const,
            onSelect: () => emit('delete'),
          },
        ],
      ]"
      :content="{ align: 'start' }"
    >
      <button class="flex items-center gap-1.5 min-w-0 cursor-pointer group">
        <span class="text-sm font-medium truncate">{{ title || '新对话' }}</span>
        <UIcon
          name="i-lucide-chevron-down"
          class="size-3.5 text-muted opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
        />
      </button>
    </UDropdownMenu>
  </div>
</template>
