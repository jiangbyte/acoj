<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <AResult status="404" title="404" :sub-title="t('system.notFoundSubTitle')">
    <template #extra>
      <RouterLink to="/dashboard">
        <AButton type="primary">{{ t('system.backDashboard') }}</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
