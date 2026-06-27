<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/stores'

const appStore = useAppStore()
const { t } = useI18n()

const options = computed<DropdownOption[]>(() => [
  {
    label: t('app.langZh'),
    key: 'zhCN',
  },
  {
    label: t('app.langEn'),
    key: 'enUS',
  },
])

const currentLabel = computed(() => (appStore.lang === 'zhCN' ? t('app.langZh') : t('app.langEn')))

function handleSelect(key: string | number) {
  appStore.setAppLang(key as App.Lang)
}
</script>

<template>
  <n-dropdown :options="options" trigger="click" @select="handleSelect">
    <n-tooltip placement="bottom" trigger="hover">
      <template #trigger>
        <CommonWrapper>
          <NovaIcon icon="icon-park-outline:translate" />
        </CommonWrapper>
      </template>
      {{ t('app.language') }}: {{ currentLabel }}
    </n-tooltip>
  </n-dropdown>
</template>
