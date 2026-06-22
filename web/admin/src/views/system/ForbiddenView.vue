<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <AResult status="403" title="403" :sub-title="t('system.forbiddenSubTitle')">
    <template #extra>
      <RouterLink to="/dashboard">
        <AButton type="primary">{{ t('system.backDashboard') }}</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
