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
/* 创建层叠上下文 */
.mdpre {
  /* position: relative; */
  z-index: 1;
}

/* 重置代码标题栏的层级 */
:deep(.md-editor-code-head) {
  z-index: 5 !important;
  /* position: relative; */
}

/* 确保代码块容器不会创建高层级 */
:deep(.md-editor-code) {
  /* position: relative; */
  z-index: 1;
}

/* 预览区域整体层级控制 */
:deep(.md-editor-preview) {
  /* position: relative; */
  z-index: 1;
}
</style>
