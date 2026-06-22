<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { DEFAULT_HOME_PATH } from '@/config/app'
import { useRouteStore } from '@/stores/route'

const { t } = useI18n()
const routeStore = useRouteStore()
const backPath = computed(() => routeStore.firstAvailablePath || DEFAULT_HOME_PATH)
</script>

<template>
  <AResult status="403" title="403" :sub-title="t('system.forbiddenSubTitle')">
    <template #extra>
      <RouterLink :to="backPath">
        <AButton type="primary">{{ t('system.backDashboard') }}</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
