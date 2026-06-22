<script setup lang="ts">
import { GlobalOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { localeOptions, type SupportedLocale } from '@/i18n'
import { useAppStore } from '@/stores/app'

defineProps<{
  buttonClass?: string
}>()

const app = useAppStore()
const { t } = useI18n()

const currentLabel = computed(() => t(`locale.${app.locale}`))

const handleLocaleClick: MenuProps['onClick'] = ({ key }) => {
  app.setLocale(key as SupportedLocale)
}
</script>

<template>
  <ATooltip :title="t('locale.switch')">
    <ADropdown placement="bottomRight" :trigger="['click']">
      <button
        :aria-label="t('locale.switch')"
        :class="buttonClass"
        class="inline-flex h-9 min-w-9 cursor-pointer items-center justify-center rounded-2 border-0 bg-transparent px-2 text-16px text-slate-600 transition hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500/35 dark:text-zinc-300 dark:hover:bg-zinc-800"
        type="button"
      >
        <GlobalOutlined />
        <span class="ml-1 hidden text-12px font-600 lg:inline">{{ currentLabel }}</span>
      </button>
      <template #overlay>
        <AMenu :selected-keys="[app.locale]" @click="handleLocaleClick">
          <AMenuItem v-for="option in localeOptions" :key="option.key">
            {{ t(option.labelKey) }}
          </AMenuItem>
        </AMenu>
      </template>
    </ADropdown>
  </ATooltip>
</template>
