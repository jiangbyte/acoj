<template>
  <ADrawer
    :open="open"
    placement="right"
    width="260"
    :closable="false"
    :bodyStyle="{ padding: 0, height: '100%' }"
    @update:open="$emit('update:open', $event)"
  >
    <div class="flex flex-col h-full">
      <!-- 用户信息 -->
      <div class="flex flex-col items-center gap-3 py-10 border-b">
        <AAvatar :size="64" :src="auth.userInfo?.avatar || undefined">
          {{ auth.userInfo?.nickname?.[0] || 'U' }}
        </AAvatar>
        <div class="text-base font-medium">{{ auth.userInfo?.nickname || '' }}</div>
        <div class="text-xs text-gray-400">{{ auth.userInfo?.username || '' }}</div>
      </div>

      <!-- 操作按钮 -->
      <div
        v-for="item in userMenuItems"
        :key="item.key"
        class="flex items-center gap-3 px-6 py-4 border-b cursor-pointer hover:bg-gray-50"
        :class="{ 'text-red-500': item.danger }"
        @click="handleClick(item)"
      >
        <component :is="item.icon" v-if="item.icon" />
        <span>{{ item.label }}</span>
      </div>
    </div>
  </ADrawer>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store'
import { useUserMenu } from './useUserMenu'

defineProps<{ open: boolean }>()
const emit = defineEmits<{ 'update:open': [value: boolean] }>()

const auth = useAuthStore()
const { userMenuItems } = useUserMenu()

function handleClick(item: { onClick: () => void }) {
  item.onClick()
  emit('update:open', false)
}
</script>
