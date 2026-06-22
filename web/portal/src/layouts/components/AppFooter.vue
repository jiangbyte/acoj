<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import { APP_TITLE } from '@/config/app'

const { t } = useI18n()
</script>

<template>
  <footer class="border-t border-slate-200 bg-white py-8 dark:border-zinc-800 dark:bg-zinc-950">
    <div class="portal-container flex flex-col gap-3 text-13px text-slate-500 dark:text-zinc-400 md:flex-row md:items-center md:justify-between">
      <div class="font-600 text-slate-700 dark:text-zinc-200">{{ APP_TITLE }}</div>
      <div>{{ t('layout.footerSlogan') }}</div>
    </div>
  </footer>
</template>
