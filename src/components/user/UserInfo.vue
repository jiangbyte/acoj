<template>
  <span v-if="name" class="text-sm text-[var(--header-text,#000000d9)]">{{ name }}</span>
  <span v-else class="text-sm text-[var(--text-secondary,#00000073)]">-</span>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserInfo' })

defineProps<{
  name?: string | null
}>()
</script>
