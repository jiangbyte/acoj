<script setup lang="ts">
defineProps<{
  title?: string
  code?: string
  description: string
  timeLimitMs?: number
  memoryLimitKb?: number
}>()
</script>

<template>
  <div class="space-y-4">
    <div v-if="title || code" class="space-y-1">
      <h1 class="text-2xl font-semibold tracking-tight">
        <span v-if="code" class="text-muted font-mono mr-2">{{ code }}</span>
        {{ title }}
      </h1>
      <p v-if="timeLimitMs != null || memoryLimitKb != null" class="text-sm text-muted">
        <span v-if="timeLimitMs != null">时间限制 {{ timeLimitMs }} ms</span>
        <span v-if="timeLimitMs != null && memoryLimitKb != null"> · </span>
        <span v-if="memoryLimitKb != null">内存限制 {{ memoryLimitKb }} KB</span>
      </p>
    </div>
    <div class="prose prose-neutral dark:prose-invert max-w-none">
      <Comark :markdown="description || ''" />
    </div>
  </div>
</template>
