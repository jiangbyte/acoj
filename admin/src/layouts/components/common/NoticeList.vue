<!-- Author: Charlie -->

<script setup lang="ts">
import MessageListItem from '@/components/sys/MessageListItem.vue'

export interface BannerItem {
  avatar?: string | null
  id: string
  type?: number
  title: string
  icon: string
  tagTitle?: string
  /** 字典色，优先于 tagType */
  tagColor?: { color: string; textColor?: string }
  tagType?: 'default' | 'error' | 'primary' | 'info' | 'success' | 'warning'
  severityLabel?: string
  description?: string
  date: string
  isRead?: boolean
}

defineProps<{
  list?: BannerItem[]
  loading?: boolean
  hasMore?: boolean
}>()

const emit = defineEmits<{
  open: [id: string]
  loadMore: []
}>()
</script>

<template>
  <NScrollbar style="height: 360px">
    <NEmpty
      v-if="!loading && !list?.length"
      description="暂无消息"
      size="small"
      style="padding: 64px 0"
    />
    <NSpace
      v-else-if="loading && !list?.length"
      justify="center"
      style="padding: 120px 0"
    >
      <NSpin size="small" />
    </NSpace>
    <NList
      v-else
      hoverable
      clickable
      class="notice-list"
    >
      <NListItem
        v-for="item in list"
        :key="item.id"
        @click="emit('open', item.id)"
      >
        <MessageListItem
          :title="item.title"
          :time="item.date"
          :excerpt="item.description"
          :icon="item.icon"
          :is-read="item.isRead"
          :kind-label="item.tagTitle"
          :kind-type="item.tagType || 'default'"
          :severity-label="item.severityLabel"
        />
      </NListItem>
      <NSpace
        v-if="hasMore"
        justify="center"
        style="padding: 8px 0 12px"
      >
        <NButton
          text
          size="small"
          :loading="loading"
          @click.stop="emit('loadMore')"
        >
          加载更多
        </NButton>
      </NSpace>
    </NList>
  </NScrollbar>
</template>

<style scoped>
.notice-list :deep(.n-list-item) {
  padding: 10px 12px;
}

.notice-list :deep(.n-list-item:not(:last-child)) {
  border-bottom: 1px solid color-mix(in srgb, #000 6%, transparent);
}

html.dark .notice-list :deep(.n-list-item:not(:last-child)) {
  border-bottom-color: color-mix(in srgb, #fff 8%, transparent);
}
</style>
