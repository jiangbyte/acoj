<script setup lang="ts">
import type { Source } from '~/utils/ai/tool'

defineProps<{
  sources: Source[]
}>()
</script>

<template>
  <div class="flex flex-col gap-2 px-3 py-2">
    <p class="text-xs font-medium text-muted">来源</p>
    <div class="flex flex-col gap-1.5">
      <AiChatSourceLink v-for="(source, index) in sources" :key="index" :source="source" />
    </div>
  </div>
</template>
