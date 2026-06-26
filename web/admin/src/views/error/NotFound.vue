<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
const homePath = import.meta.env.VITE_HOME_PATH
</script>

<template>
  <n-result
    status="404"
    :title="t('error.notFoundTitle')"
    :description="t('error.notFoundDescription')"
  >
    <template #footer>
      <n-button type="primary" @click="router.push(homePath)">
        {{ t('error.backWorkbench') }}
      </n-button>
    </template>
  </n-result>
</template>
