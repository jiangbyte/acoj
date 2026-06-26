<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'
import { darkTheme } from 'naive-ui'
import { useAppStore } from './stores'
import themeConfig from './stores/app/theme.json'

const appStore = useAppStore()
const theme = themeConfig as GlobalThemeOverrides
</script>

<template>
  <n-config-provider
    class="wh-full"
    inline-theme-disabled
    :theme="appStore.storeColorMode === 'dark' ? darkTheme : null"
    :theme-overrides="theme"
  >
    <naive-provider>
      <router-view />
      <watermark show text="ACOJ" />
    </naive-provider>
  </n-config-provider>
</template>
