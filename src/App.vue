<template>
  <AConfigProvider :theme="themeConfig" :locale="zhCN">
    <!-- Standalone loading overlay (not wrapping content) -->
    <div v-show="app.loading" class="fixed inset-0 z-9999 flex flex-col items-center justify-center" style="background: var(--container-bg, #fff)">
      <a-spin size="large" :spinning="true" />
      <div class="mt-4 text-sm" style="color: var(--text-secondary, #00000073)">加载中...</div>
    </div>
    <!-- App content -->
    <div v-show="!app.loading" :class="app.roundedCorners ? 'rounded-style' : ''">
      <router-view :key="app.reloadCounter" />
    </div>
  </AConfigProvider>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { theme } from 'ant-design-vue'
import { useAppStore } from '@/store'
import { changeColor, toggleGrayMode, toggleColorWeak } from '@/utils'

const app = useAppStore()

const { darkAlgorithm, defaultAlgorithm } = theme

const themeConfig = computed(() => ({
  algorithm: app.theme === 'realDark' ? darkAlgorithm : defaultAlgorithm,
  token: {
    colorPrimary: app.colorPrimary,
    borderRadius: app.roundedCorners ? 6 : 2,
  },
}))

changeColor(app.colorPrimary, app.theme)
toggleGrayMode(app.grayMode)
toggleColorWeak(app.colorWeak)

watch(() => app.colorPrimary, color => changeColor(color, app.theme))
watch(() => app.theme, t => changeColor(app.colorPrimary, t))
watch(() => app.grayMode, toggleGrayMode)
watch(() => app.colorWeak, toggleColorWeak)
</script>
