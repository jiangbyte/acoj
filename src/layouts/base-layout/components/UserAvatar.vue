<template>
  <ADropdown :trigger="['click']" v-if="!app.isMobile">
    <div class="flex items-center gap-2 cursor-pointer">
      <AAvatar :size="32" :src="auth.userInfo?.avatar || undefined">
        {{ auth.userInfo?.nickname?.[0] || 'U' }}
      </AAvatar>
      <span>{{ auth.userInfo?.nickname || '' }}</span>
    </div>
    <template #overlay>
      <AMenu>
        <AMenuItem v-for="item in userMenuItems" :key="item.key" @click="item.onClick">
          <component :is="item.icon" v-if="item.icon" />
          {{ item.label }}
        </AMenuItem>
      </AMenu>
    </template>
  </ADropdown>
  <!-- 移动端：点击触发用户抽屉 -->
  <div v-else class="flex items-center gap-2 cursor-pointer" @click="$emit('toggleUserDrawer')">
    <AAvatar :size="32" :src="auth.userInfo?.avatar || undefined">
      {{ auth.userInfo?.nickname?.[0] || 'U' }}
    </AAvatar>
    <span>{{ auth.userInfo?.nickname || '' }}</span>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore, useAppStore } from '@/store'
import { useUserMenu } from './useUserMenu'

defineEmits<{ toggleUserDrawer: [] }>()

const auth = useAuthStore()
const app = useAppStore()
const { userMenuItems } = useUserMenu()
</script>
