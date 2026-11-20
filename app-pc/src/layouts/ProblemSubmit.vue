<script lang="ts" setup>

</script>

<template>
  <div>
    <Header class-name="w-full" />
    <router-view v-slot="{ Component, route }" class="flex-1">
      <component
        :is="Component"
        :key="route.fullPath"
      />
    </router-view>
  </div>
</template>

<style scoped></style>
