<script setup lang="ts">
</script>

<template>
  <n-config-provider
    class="wh-full"
    inline-theme-disabled
  >
    <naive-provider>
      <router-view />
      444
      <watermark :show="true" />
    </naive-provider>
  </n-config-provider>
</template>
