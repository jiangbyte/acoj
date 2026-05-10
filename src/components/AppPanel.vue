<template>
  <div class="app-panel" :style="{ padding: padding + 'px' }">
    <div v-if="title || $slots.extra" class="app-panel__header">
      <h3 v-if="title" class="app-panel__title">{{ title }}</h3>
      <div v-if="$slots.extra" class="app-panel__extra">
        <slot name="extra" />
      </div>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  padding?: number
}>()
</script>

<style scoped>
.app-panel {
  background: var(--container-bg, #fff);
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.app-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.app-panel__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--header-text, #000000d9);
}

.app-panel__extra {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
