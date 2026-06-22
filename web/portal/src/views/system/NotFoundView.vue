<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <main class="mx-auto min-h-[calc(100vh-129px)] max-w-1180px px-4 py-14 sm:px-6 lg:px-8">
    <AResult status="404" title="404" :sub-title="t('system.notFoundSubtitle')">
      <template #extra>
        <RouterLink to="/">
          <AButton type="primary">{{ t('system.backHome') }}</AButton>
        </RouterLink>
      </template>
    </AResult>
  </main>
</template>
