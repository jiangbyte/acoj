<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import { DEFAULT_HOME_PATH } from '@/config/app'

const route = useRoute()
const { t } = useI18n()
const retryPath = computed(() => String(route.query.redirect || DEFAULT_HOME_PATH))
</script>

<template>
  <AResult status="500" :title="t('system.serverErrorTitle')" :sub-title="t('system.serverErrorSubTitle')">
    <template #extra>
      <RouterLink :to="retryPath">
        <AButton type="primary">{{ t('system.retry') }}</AButton>
      </RouterLink>
      <RouterLink to="/auth/login">
        <AButton>{{ t('system.backLogin') }}</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
