<script setup lang="ts">
import { computed } from 'vue'


const stats = computed(() => [
  { label: 'Routes', value: '1' },
  { label: 'Menu Pages', value: 'Visible pages' },
  { label: 'Cached Pages', value: 'OFF' },
])

const actions = computed(() => [
  {
    icon: 'icon-park-outline:connection-point',
    title: 'Route Initialization',
    text: 'Load the static route table',
  },
  {
    icon: 'icon-park-outline:tree-diagram',
    title: 'Menu Generation',
    text: 'Generate sidebar menus from RowRoute',
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
        {{ 'Static example' }}
      </div>
      <h1 class="text-3xl font-800 sm:text-4xl">{{ 'Demo' }}</h1>
      <p class="mt-3 max-w-160 text-base leading-7 text-[var(--text-color-2)]">
        {{ 'Static route, menu, and tab examples' }}
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
