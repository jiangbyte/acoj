<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'
import { usePreferredDark } from '@vueuse/core'
import { darkTheme, dateZhCN, zhCN } from 'naive-ui'
import { computed } from 'vue'
import { useAppStore } from './stores'
import themeConfig from './stores/app/theme.json'

const appStore = useAppStore()
const prefersDark = usePreferredDark()
const theme = themeConfig as GlobalThemeOverrides
const naiveTheme = computed(() =>
  appStore.storeColorMode === 'dark' || (appStore.storeColorMode === 'auto' && prefersDark.value)
    ? darkTheme
    : null,
)
</script>

<template>
  <n-config-provider
    class="wh-full"
    inline-theme-disabled
    :theme="naiveTheme"
    :theme-overrides="theme"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <naive-provider>
      <router-view />
    </naive-provider>
  </n-config-provider>
</template>
