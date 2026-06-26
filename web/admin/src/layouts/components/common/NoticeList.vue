<script setup lang="ts">
export interface NoticeItem {
  id: number
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
}>()

const emit = defineEmits<{
  read: [id: number]
}>()
</script>

<template>
  <n-scrollbar style="height: 400px">
    <n-list hoverable clickable>
      <n-list-item v-for="item in list" :key="item.id" @click="emit('read', item.id)">
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
    </n-list>
  </n-scrollbar>
</template>
