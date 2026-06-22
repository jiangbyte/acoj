<script setup lang="ts">
import AppFooter from './components/AppFooter.vue'
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <ALayout class="min-h-screen bg-slate-50 text-slate-900 dark:bg-zinc-950 dark:text-zinc-100">
    <AppHeader />
    <ALayoutContent class="min-w-0">
      <RouterView />
    </ALayoutContent>
    <AppFooter />
  </ALayout>
</template>
