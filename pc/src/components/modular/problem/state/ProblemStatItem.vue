<script lang="ts" setup>
import type { Component } from 'vue'

interface Props {
  icon: Component
  label: string
  value: string
}

defineProps<Props>()
</script>

<template>
  <NFlex align="center" :size="8">
    <NIcon :component="icon" size="18" color="#176DF2" />
    <NText depth="3">
      {{ label }}: {{ value }}
    </NText>
  </NFlex>
</template>

<style scoped>
</style>
