<template>
  <n-space vertical size="large">
    <n-page-header :title="t('resource.demo.overview')" :subtitle="t('resource.demo.overview_subtitle')" />

    <n-grid cols="1 s:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item v-for="item in stats" :key="item.label">
        <n-card :title="item.title" :bordered="false">
          <n-statistic :label="item.label" :value="item.value" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card :title="t('resource.demo.recent_actions')" :bordered="false">
      <n-timeline>
        <n-timeline-item
          type="success"
          :title="t('resource.demo.route_init')"
          :content="t('resource.demo.route_init_content')"
        />
        <n-timeline-item
          type="info"
          :title="t('resource.demo.menu_generate')"
          :content="t('resource.demo.menu_generate_content')"
        />
        <n-timeline-item
          type="warning"
          :title="t('resource.demo.cache_route')"
          :content="t('resource.demo.cache_route_content')"
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
  { title: t('resource.demo.route_count'), label: t('resource.demo.static_example'), value: 6 },
  { title: t('resource.demo.menu_page'), label: t('resource.demo.visible_page'), value: 4 },
  { title: t('resource.demo.hidden_page'), label: t('resource.demo.detail_page'), value: 1 },
  { title: t('resource.demo.cache_page'), label: 'is_cache', value: 1 },
])
</script>
