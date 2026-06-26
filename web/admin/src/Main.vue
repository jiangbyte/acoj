<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'
import { darkTheme } from 'naive-ui'
import { computed } from 'vue'
import { useAppStore } from './stores'
import themeConfig from './stores/app/theme.json'
import { naiveI18nOptions } from './utils/i18n'

const appStore = useAppStore()
const theme = themeConfig as GlobalThemeOverrides
const naiveLocale = computed(() => naiveI18nOptions[appStore.lang])
</script>

<template>
  <n-config-provider
    class="wh-full"
    inline-theme-disabled
    :theme="appStore.storeColorMode === 'dark' ? darkTheme : null"
    :theme-overrides="theme"
    :locale="naiveLocale.locale"
    :date-locale="naiveLocale.dateLocale"
  >
    <naive-provider>
      <router-view />
      <watermark show text="ACOJ" />
    </naive-provider>
  </n-config-provider>
</template>
