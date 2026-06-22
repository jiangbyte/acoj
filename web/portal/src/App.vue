<script setup lang="ts">
import { theme } from 'ant-design-vue'
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'

import { antdLocales, setI18nLocale } from '@/i18n'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const app = useAppStore()
const auth = useAuthStore()
const user = useUserStore()
let mediaQuery: MediaQueryList | null = null

const themeConfig = computed(() => ({
  algorithm: app.isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
  components: {
    Button: {
      borderRadius: 8,
    },
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

function handleAuthExpired() {
  auth.clearSession()
  user.clear()
}

watch(() => app.isDark, syncDocumentTheme, { immediate: true })
watch(() => app.locale, setI18nLocale, { immediate: true })

onMounted(() => {
  mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  app.setSystemDark(mediaQuery.matches)
  mediaQuery.addEventListener('change', handleSystemThemeChange)
  window.addEventListener('portal-auth-expired', handleAuthExpired)
})

onBeforeUnmount(() => {
  mediaQuery?.removeEventListener('change', handleSystemThemeChange)
  window.removeEventListener('portal-auth-expired', handleAuthExpired)
})
</script>

<template>
  <AConfigProvider :theme="themeConfig" :locale="antLocale">
    <RouterView />
  </AConfigProvider>
</template>
