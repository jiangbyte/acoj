<script setup lang="ts">
// 通知列表项的展示模型：当前仅用于前端占位数据，后续接入接口时应保持字段语义一致。
export interface NoticeItem {
  id: number
  // 通知分组类型：0 通知、1 消息、2 待办，由上层 Notices 组件负责分组。
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

// 列表只负责展示和抛出已读事件，具体如何更新已读状态交给父组件维护，避免组件内复制状态。
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
