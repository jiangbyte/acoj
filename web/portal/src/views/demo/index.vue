<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const stats = computed(() => [
  { label: t('resource.demo.route_count'), value: '1' },
  { label: t('resource.demo.menu_page'), value: t('resource.demo.visible_page') },
  { label: t('resource.demo.cache_page'), value: 'OFF' },
])

const actions = computed(() => [
  {
    icon: 'icon-park-outline:connection-point',
    title: t('resource.demo.route_init'),
    text: t('resource.demo.route_init_content'),
  },
  {
    icon: 'icon-park-outline:tree-diagram',
    title: t('resource.demo.menu_generate'),
    text: t('resource.demo.menu_generate_content'),
  },
])
</script>

<template>
  <section class="px-4 py-8 sm:px-6 lg:px-8">
    <div class="mb-8">
      <div
        class="mb-4 inline-flex items-center gap-2 rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] px-3 py-1 text-sm font-600 text-[var(--primary-color)]"
      >
        <NovaIcon icon="icon-park-outline:experiment-one" />
        {{ t('resource.demo.static_example') }}
      </div>
      <h1 class="text-3xl font-800 sm:text-4xl">{{ t('resource.demo.title') }}</h1>
      <p class="mt-3 max-w-160 text-base leading-7 text-[var(--text-color-2)]">
        {{ t('resource.demo.overview_subtitle') }}
      </p>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <div
        v-for="item in stats"
        :key="item.label"
        class="rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] p-5"
      >
        <div class="text-sm text-[var(--text-color-3)]">{{ item.label }}</div>
        <div class="mt-3 text-2xl font-800">{{ item.value }}</div>
      </div>
    </div>

    <div class="mt-6 grid gap-4 md:grid-cols-2">
      <div
        v-for="item in actions"
        :key="item.title"
        class="rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] p-5"
      >
        <NovaIcon class="text-[var(--primary-color)]" :icon="item.icon" :size="26" />
        <h2 class="mt-4 text-lg font-750">{{ item.title }}</h2>
        <p class="mt-2 text-sm leading-6 text-[var(--text-color-3)]">
          {{ item.text }}
        </p>
      </div>
    </div>
  </section>
</template>
