<template>
  <ALayoutHeader class="layout-header flex items-center justify-between !px-4">
    <div class="flex items-center gap-3">
      <!-- 移动端：汉堡菜单按钮 -->
      <template v-if="app.isMobile">
        <MenuOutlined class="text-lg cursor-pointer" @click="$emit('toggleMobileMenu')" />
      </template>
      <!-- 桌面端：侧边栏折叠按钮 -->
      <template v-else>
        <MenuUnfoldOutlined
          v-if="app.collapsed"
          @click="app.toggleCollapsed()"
          class="text-lg cursor-pointer"
        />
        <MenuFoldOutlined v-else @click="app.toggleCollapsed()" class="text-lg cursor-pointer" />
      </template>
    </div>
    <div class="flex items-center gap-4">
      <template v-if="!app.isMobile">
        <FullscreenOutlined class="text-lg cursor-pointer" @click="toggleFullscreen" />
        <ATooltip title="切换主题">
          <BulbOutlined
            v-if="app.theme === 'realDark'"
            class="text-lg cursor-pointer"
            @click="cycleTheme"
          />
          <HighlightOutlined
            v-else-if="app.theme === 'dark'"
            class="text-lg cursor-pointer"
            @click="cycleTheme"
          />
          <BulbOutlined v-else class="text-lg cursor-pointer" @click="cycleTheme" />
        </ATooltip>
        <SettingOutlined class="text-lg cursor-pointer" @click="app.showSettings = true" />
      </template>
      <UserAvatar @toggleUserDrawer="$emit('toggleUserDrawer')" />
    </div>
  </ALayoutHeader>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store'
import type { ThemeMode } from '@/utils'
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  MenuOutlined,
  FullscreenOutlined,
  SettingOutlined,
  BulbOutlined,
  HighlightOutlined,
} from '@ant-design/icons-vue'
import UserAvatar from '../components/UserAvatar.vue'

defineEmits<{ toggleMobileMenu: []; toggleUserDrawer: [] }>()

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
