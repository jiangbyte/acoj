<script setup lang="ts">
import { Icon } from '@iconify/vue'

interface Props {
  icon?: string
  color?: string
  size?: number
  depth?: 1 | 2 | 3 | 4 | 5
}

const { size = 18 } = defineProps<Props>()
</script>

<template>
  <n-icon v-if="icon" :size="size" :depth="depth" :color="color">
    <Icon :icon="icon" />
  </n-icon>
</template>
