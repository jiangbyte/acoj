<script setup lang="ts">
import { theme } from 'ant-design-vue'
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'

import { useAppStore } from '@/stores/app'
import { antdLocales, setI18nLocale } from '@/i18n'

const app = useAppStore()
let mediaQuery: MediaQueryList | null = null

const themeConfig = computed(() => ({
  algorithm: app.isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
  components: {
    Menu: {
      colorItemBg: 'inherit',
      colorSubItemBg: 'inherit',
      menuSubMenuBg: 'inherit',
    },
  },
}))
const antLocale = computed(() => antdLocales[app.locale])

function syncDocumentTheme() {
  document.documentElement.classList.toggle('dark', app.isDark)
  document.documentElement.style.colorScheme = app.isDark ? 'dark' : 'light'
}

function handleSystemThemeChange(event: MediaQueryListEvent) {
  app.setSystemDark(event.matches)
}

watch(() => app.isDark, syncDocumentTheme, { immediate: true })
watch(() => app.locale, setI18nLocale, { immediate: true })

onMounted(() => {
  mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  app.setSystemDark(mediaQuery.matches)
  mediaQuery.addEventListener('change', handleSystemThemeChange)
})

onBeforeUnmount(() => {
  mediaQuery?.removeEventListener('change', handleSystemThemeChange)
})
</script>

<template>
  <AConfigProvider :theme="themeConfig" :locale="antLocale">
    <RouterView />
  </AConfigProvider>
</template>
