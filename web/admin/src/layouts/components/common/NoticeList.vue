<script setup lang="ts">
export interface NoticeItem {
  id: string
  type: number
  title: string
  icon: string
  tagTitle?: string
  tagType?: 'default' | 'error' | 'primary' | 'info' | 'success' | 'warning'
  description?: string
  date: string
  isRead?: boolean
}

defineProps<{
  list?: NoticeItem[]
  loading?: boolean
  hasMore?: boolean
}>()

const emit = defineEmits<{
  open: [id: string]
  loadMore: []
}>()
</script>

<template>
  <n-scrollbar style="height: 400px">
    <n-empty
      v-if="!loading && !list?.length"
      class="h-full py-80px"
      :description="'No data'"
    />
    <div v-else-if="loading && !list?.length" class="h-full flex items-center justify-center">
      <n-spin size="small" />
    </div>
    <n-list v-else hoverable clickable>
      <n-list-item v-for="item in list" :key="item.id" @click="emit('open', item.id)">
        <n-thing content-indented :class="{ 'opacity-40': item.isRead }">
          <template #header>
            <n-ellipsis :line-clamp="1">
              {{ item.title }}
            </n-ellipsis>
          </template>
          <template #avatar>
            <NovaIcon :icon="item.icon" :size="30" class="c-primary" />
          </template>
          <template v-if="item.tagTitle" #header-extra>
            <n-tag :bordered="false" :type="item.tagType" size="small">
              {{ item.tagTitle }}
            </n-tag>
          </template>
          <template v-if="item.description" #description>
            <n-ellipsis :line-clamp="2">
              {{ item.description }}
            </n-ellipsis>
          </template>
          <template #footer>
            {{ item.date }}
          </template>
        </n-thing>
      </n-list-item>
      <div v-if="hasMore" class="py-12px text-center">
        <n-button text size="small" :loading="loading" @click.stop="emit('loadMore')">
          {{ 'Load more' }}
        </n-button>
      </div>
    </n-list>
  </n-scrollbar>
</template>
