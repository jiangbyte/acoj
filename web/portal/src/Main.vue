<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'
import type { ProConfigProviderProps } from 'pro-naive-ui'
import { darkTheme, dateEnUS, dateZhCN } from 'naive-ui'
import { ProConfigProvider, enUS as proEnUS, zhCN as proZhCN } from 'pro-naive-ui'
import { computed } from 'vue'
import { useAppStore } from './stores'
import themeConfig from './stores/app/theme.json'
import { naiveI18nOptions } from './utils/i18n'

const appStore = useAppStore()
const theme = themeConfig as GlobalThemeOverrides
const naiveLocale = computed(() => naiveI18nOptions[appStore.lang])
const proConfigProviderProps = computed<ProConfigProviderProps>(() => ({
  abstract: true,
  locale: appStore.lang === 'zhCN' ? proZhCN : proEnUS,
  dateLocale: appStore.lang === 'zhCN' ? dateZhCN : dateEnUS,
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
    :theme="appStore.storeColorMode === 'dark' ? darkTheme : null"
    :theme-overrides="theme"
    :locale="naiveLocale.locale"
    :date-locale="naiveLocale.dateLocale"
  >
    <naive-provider>
      <ProConfigProvider v-bind="proConfigProviderProps">
        <router-view />
        <watermark show text="ACOJ" />
      </ProConfigProvider>
    </naive-provider>
  </n-config-provider>
</template>
