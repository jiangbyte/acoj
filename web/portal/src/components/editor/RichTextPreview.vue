<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    value?: string | null
  }>(),
  {
    value: '',
  },
)

const html = computed(() => props.value ?? '')
</script>

<template>
  <div class="rich-text-preview" v-html="html" />
</template>

<style scoped>
.rich-text-preview {
  width: 100%;
  min-width: 0;
  line-height: 1.7;
  color: var(--text-color-1);
  word-break: break-word;
}

.rich-text-preview :deep(p) {
  margin: 0 0 8px;
}

.rich-text-preview :deep(img),
.rich-text-preview :deep(video) {
  max-width: 100%;
}

.rich-text-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
}

.rich-text-preview :deep(th),
.rich-text-preview :deep(td) {
  border: 1px solid var(--border-color);
  padding: 6px 8px;
}
</style>
