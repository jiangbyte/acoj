<script setup lang="ts">
import { useSiteStore } from '@/stores'

const siteStore = useSiteStore()
</script>

<template>
  <n-button
    text
    @click="siteStore.toggleCollapse"
  >
    <template #icon>
      <icon-park-outline-menu-fold-one v-if="siteStore.collapsed" />
      <icon-park-outline-menu-unfold-one v-else />
    </template>
  </n-button>
</template>

<style>

</style>
