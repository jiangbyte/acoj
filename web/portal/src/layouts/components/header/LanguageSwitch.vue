<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/stores'

const appStore = useAppStore()
const { t } = useI18n()

const options = computed<DropdownOption[]>(() => [
  {
    label: t('app.lang_zh'),
    key: 'zhCN',
  },
  {
    label: t('app.lang_en'),
    key: 'enUS',
  },
])

const currentLabel = computed(() => (appStore.lang === 'zhCN' ? t('app.lang_zh') : t('app.lang_en')))

function handleSelect(key: string | number) {
  appStore.setAppLang(key as 'zhCN' | 'enUS')
}
</script>

<template>
  <n-dropdown :options="options" trigger="click" :to="false" @select="handleSelect">
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
