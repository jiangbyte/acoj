<template>
  <AConfigProvider :theme="themeConfig" :locale="zhCN">
    <AppLoading :loading="app.loading">
      <div :class="app.roundedCorners ? 'rounded-style' : ''">
        <router-view :key="app.reloadCounter" />
      </div>
    </AppLoading>
  </AConfigProvider>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { theme } from 'ant-design-vue'
import AppLoading from '@/components/AppLoading.vue'
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

// Init theme on mount
changeColor(app.colorPrimary, app.theme)
toggleGrayMode(app.grayMode)
toggleColorWeak(app.colorWeak)

// Watch theme changes
watch(() => app.colorPrimary, (color) => {
  changeColor(color, app.theme)
})

watch(() => app.theme, (t) => {
  changeColor(app.colorPrimary, t)
})

watch(() => app.grayMode, toggleGrayMode)
watch(() => app.colorWeak, toggleColorWeak)
</script>
