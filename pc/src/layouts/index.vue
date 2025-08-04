<script setup lang="ts">
</script>

<template>
  <div>
    <Header />
    <router-view v-slot="{ Component, route }" class="flex-1 mt-72px p-4 min-h-[calc(100vh-10rem)]">
      <component
        :is="Component"
        :key="route.fullPath"
      />
    </router-view>
    <Footer />
  </div>
</template>

<style scoped>

</style>
