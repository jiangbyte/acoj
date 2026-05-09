<template>
  <ALayoutHeader class="layout-header flex items-center justify-between !px-4">
    <div class="flex items-center gap-3">
      <MenuUnfoldOutlined v-if="app.collapsed" @click="app.toggleCollapsed()" class="text-lg cursor-pointer" />
      <MenuFoldOutlined v-else @click="app.toggleCollapsed()" class="text-lg cursor-pointer" />
    </div>
    <div class="flex items-center gap-4">
      <FullscreenOutlined class="text-lg cursor-pointer" @click="toggleFullscreen" />
      <ATooltip title="切换主题">
        <BulbOutlined v-if="app.theme === 'realDark'" class="text-lg cursor-pointer" @click="cycleTheme" />
        <HighlightOutlined v-else-if="app.theme === 'dark'" class="text-lg cursor-pointer" @click="cycleTheme" />
        <BulbOutlined v-else class="text-lg cursor-pointer" @click="cycleTheme" />
      </ATooltip>
      <SettingOutlined class="text-lg cursor-pointer" @click="app.showSettings = true" />
      <UserAvatar />
    </div>
  </ALayoutHeader>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app'
import type { ThemeMode } from '@/utils/themeUtil'
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  FullscreenOutlined,
  SettingOutlined,
  BulbOutlined,
  HighlightOutlined,
} from '@ant-design/icons-vue'
import UserAvatar from '../components/UserAvatar.vue'

const app = useAppStore()

const themeCycle: ThemeMode[] = ['light', 'dark', 'realDark']

function cycleTheme() {
  const idx = themeCycle.indexOf(app.theme)
  app.setTheme(themeCycle[(idx + 1) % themeCycle.length])
}

function toggleFullscreen() {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}
</script>
