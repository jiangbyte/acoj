<!-- Author: Charlie -->

<script setup lang="ts">
import type { ProConfigProviderProps } from 'pro-naive-ui'
import { darkTheme, dateZhCN, zhCN } from 'naive-ui'
import { ProConfigProvider, zhCN as proZhCN } from 'pro-naive-ui'
import { computed, onMounted } from 'vue'
import { hljs } from './plugins/hljs'
import { useAppStore, useAuthStore, watchAppAppearance } from './stores'

const appStore = useAppStore()
const authStore = useAuthStore()
const chinaTimeZone = 'Asia/Shanghai'

onMounted(() => {
  watchAppAppearance(appStore)
})

const watermarkText = computed(() => {
  const info = authStore.userInfo
  if (!info) {
    return 'HEI'
  }
  return [info.nickname || info.account, info.account].filter(Boolean).join(' ')
})

const proConfigProviderProps = computed<ProConfigProviderProps>(() => ({
  abstract: true,
  locale: proZhCN,
  dateLocale: dateZhCN,
  propOverrides: {
    ProButton: {
      focusable: false,
    },
    ProCard: {
      size: 'small',
    },
    ProSearchForm: {
      showFeedback: false,
      yGap: 12,
      labelWidth: 100,
      suffixFormItemProps: {
        showFeedback: false,
      },
    },
    ProDataTable: {
      size: 'small',
      flexHeight: !appStore.isMobile,
      pagination: {
        pageSlot: appStore.isMobile ? 6 : undefined,
      },
      tableCardProps: {
        size: 'small',
      },
    },
    ProModalForm: {
      preset: 'card',
      labelPlacement: 'left',
      labelWidth: '100',
    },
    ProTime: {
      fieldProps: {
        timeZone: chinaTimeZone,
      },
    },
  },
}))
</script>

<template>
  <n-config-provider
    class="wh-full"
    inline-theme-disabled
    :theme="appStore.naiveTheme === 'dark' ? darkTheme : null"
    :theme-overrides="appStore.themeOverrides"
    :component-options="{ Card: { size: 'small' } }"
    :locale="zhCN"
    :date-locale="dateZhCN"
    :hljs="hljs"
  >
    <naive-provider>
      <ProConfigProvider v-bind="proConfigProviderProps">
        <router-view />
        <watermark
          :show="appStore.showWatermark"
          :text="watermarkText"
        />
      </ProConfigProvider>
    </naive-provider>
  </n-config-provider>
</template>
