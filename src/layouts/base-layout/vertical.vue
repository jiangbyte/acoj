<template>
  <ALayout class="h-screen">
    <Sider />
    <ALayout>
      <Header />
      <Tab />
      <ALayoutContent class="p-4 bg-gray-50 overflow-auto">
        <router-view />
      </ALayoutContent>
    </ALayout>
  </ALayout>
</template>

<script setup lang="ts">
import Sider from './sider/index.vue'
import Header from './header/index.vue'
import Tab from './tab/index.vue'
</script>
