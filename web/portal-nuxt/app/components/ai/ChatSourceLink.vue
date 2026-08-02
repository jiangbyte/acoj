<script setup lang="ts">
import type { Source } from '~/utils/ai/tool'

defineProps<{
  source: Source
}>()
</script>

<template>
  <a
    :href="source.url"
    target="_blank"
    rel="noopener noreferrer"
    class="flex items-center gap-2 rounded-lg border border-default px-3 py-2 text-sm hover:bg-accented/50 transition-colors"
  >
    <img
      v-if="source.url"
      :src="`https://www.google.com/s2/favicons?domain=${encodeURIComponent(source.url)}&sz=16`"
      alt=""
      class="size-4 rounded"
      loading="lazy"
    />
    <span class="truncate text-muted">{{ source.title || source.url }}</span>
  </a>
</template>
