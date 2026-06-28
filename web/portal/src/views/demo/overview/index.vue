<template>
  <n-space vertical size="large">
    <n-page-header :title="t('demo.overviewTitle')" :subtitle="t('demo.overviewSubtitle')" />

    <n-grid cols="1 s:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item v-for="item in stats" :key="item.label">
        <n-card :title="item.title" :bordered="false">
          <n-statistic :label="item.label" :value="item.value" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card :title="t('demo.recentActions')" :bordered="false">
      <n-timeline>
        <n-timeline-item
          type="success"
          :title="t('demo.routeInit')"
          :content="t('demo.routeInitContent')"
        />
        <n-timeline-item
          type="info"
          :title="t('demo.menuGenerate')"
          :content="t('demo.menuGenerateContent')"
        />
        <n-timeline-item
          type="warning"
          :title="t('demo.cacheRoute')"
          :content="t('demo.cacheRouteContent')"
        />
      </n-timeline>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const stats = computed(() => [
  { title: t('demo.routeCount'), label: t('demo.staticExample'), value: 6 },
  { title: t('demo.menuPage'), label: t('demo.visiblePage'), value: 4 },
  { title: t('demo.hiddenPage'), label: t('demo.detailPage'), value: 1 },
  { title: t('demo.cachePage'), label: 'is_cache', value: 1 },
])
</script>
