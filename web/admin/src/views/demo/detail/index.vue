<template>
  <n-space vertical size="large">
    <n-page-header :title="t('demo.detailTitle')" :subtitle="t('demo.detailSubtitle')" @back="router.back">
      <template #extra>
        <n-button type="primary" @click="router.push('/demo/list')">
          {{ t('demo.backList') }}
        </n-button>
      </template>
    </n-page-header>

    <n-card :bordered="false">
      <n-descriptions label-placement="left" bordered :column="1">
        <n-descriptions-item :label="t('demo.id')">{{ route.query.id ?? '1001' }}</n-descriptions-item>
        <n-descriptions-item :label="t('demo.title')">
          {{ t('demo.sampleProblemDetail') }}
        </n-descriptions-item>
        <n-descriptions-item :label="t('demo.status')">{{ t('demo.published') }}</n-descriptions-item>
        <n-descriptions-item :label="t('demo.updatedAt')">2026-06-26</n-descriptions-item>
      </n-descriptions>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
</script>
