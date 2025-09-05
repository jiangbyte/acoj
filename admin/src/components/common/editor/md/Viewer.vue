<script setup lang="ts">
import { MdPreview } from 'md-editor-v3'
// https://imzbf.github.io/md-editor-v3/zh-CN/docs
import 'md-editor-v3/lib/style.css'

const model = defineModel<string>()
</script>

<template>
  <MdPreview
    v-model="model"
  />
</template>

<style scoped></style>
