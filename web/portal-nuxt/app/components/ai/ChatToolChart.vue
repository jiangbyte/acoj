<script setup lang="ts">
defineProps<{
  title: string
  labels: string[]
  values: number[]
}>()
</script>

<template>
  <div class="rounded-lg border border-default p-4 bg-elevated/50">
    <p class="text-sm font-semibold mb-3">{{ title }}</p>
    <div class="flex items-end gap-2 h-32">
      <div
        v-for="(value, index) in values"
        :key="index"
        class="flex-1 flex flex-col items-center gap-1"
      >
        <span class="text-xs text-muted">{{ value }}</span>
        <div
          class="w-full rounded-t bg-primary/70 transition-all duration-500"
          :style="{ height: `${(value / Math.max(...values)) * 100}%` }"
        />
        <span class="text-xs text-muted truncate w-full text-center">{{ labels[index] }}</span>
      </div>
    </div>
  </div>
</template>
