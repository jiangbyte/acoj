<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'
import type { ProConfigProviderProps } from 'pro-naive-ui'
import { darkTheme, dateEnUS } from 'naive-ui'
import { ProConfigProvider, enUS as proEnUS } from 'pro-naive-ui'
import { computed } from 'vue'
import { useAppStore } from './stores'
import themeConfig from './stores/app/theme.json'

const appStore = useAppStore()
const theme = themeConfig as GlobalThemeOverrides
const proConfigProviderProps = computed<ProConfigProviderProps>(() => ({
  abstract: true,
  locale: proEnUS,
  dateLocale: dateEnUS,
  propOverrides: {
    ProButton: {
      focusable: false,
    },
    ProDataTable: {
      size: 'small',
      flexHeight: !appStore.isMobile,
      pagination: {
        pageSlot: appStore.isMobile ? 6 : undefined,
      },
    },
    ProModalForm: {
      preset: 'card',
      labelPlacement: 'left',
      labelWidth: '100',
    },
  },
}))
</script>

<template>
  <n-config-provider
    class="wh-full"
    inline-theme-disabled
    :theme="appStore.naiveTheme === 'dark' ? darkTheme : null"
    :theme-overrides="theme"
    :date-locale="dateEnUS"
  >
    <naive-provider>
      <ProConfigProvider v-bind="proConfigProviderProps">
        <router-view />
        <watermark show text="HEI" />
      </ProConfigProvider>
    </naive-provider>
  </n-config-provider>
</template>
