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
    :title="t('error.not_found_title')"
    :description="t('error.not_found_description')"
  >
    <template #footer>
      <n-button type="primary" @click="router.push(homePath)">
        {{ t('error.back_workbench') }}
      </n-button>
    </template>
  </n-result>
</template>
