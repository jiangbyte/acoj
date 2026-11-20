<script setup lang="ts">
import { MdPreview } from 'md-editor-v3'
// https://imzbf.github.io/md-editor-v3/zh-CN/docs
import 'md-editor-v3/lib/style.css'

const model = defineModel<string>()
</script>

<template>
  <MdPreview
    v-model="model"
    class="mdpre"
  />
</template>

<style scoped>
/* Component styles. */
.mdpre {
  /* position: relative; */
  z-index: 1;
}

/* Component styles. */
:deep(.md-editor-code-head) {
  z-index: 5 !important;
  /* position: relative; */
}

/* Component styles. */
:deep(.md-editor-code) {
  /* position: relative; */
  z-index: 1;
}

/* Component styles. */
:deep(.md-editor-preview) {
  /* position: relative; */
  z-index: 1;
}
</style>
