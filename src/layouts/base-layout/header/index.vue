<template>
  <div class="layout-header flex items-center justify-between px-4 h-16 border-b">
    <div class="flex items-center gap-3">
      <MenuUnfoldOutlined v-if="app.collapsed" @click="app.toggleCollapsed()" class="text-lg cursor-pointer" />
      <MenuFoldOutlined v-else @click="app.toggleCollapsed()" class="text-lg cursor-pointer" />
    </div>
    <div class="flex items-center gap-3">
      <FullscreenOutlined class="text-lg cursor-pointer" @click="toggleFullscreen" />
      <UserAvatar />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app'
import { MenuUnfoldOutlined, MenuFoldOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import UserAvatar from '../components/UserAvatar.vue'

const app = useAppStore()

function toggleFullscreen() {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}
</script>
