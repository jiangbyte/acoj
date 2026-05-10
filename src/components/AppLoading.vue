<template>
  <div class="app-loading">
    <a-spin :spinning="loading" size="large" tip="加载中...">
      <slot />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
defineProps<{ loading: boolean }>()
</script>

<style scoped>
.app-loading {
  width: 100%;
  min-height: 100vh;
}
.app-loading :deep(.ant-spin-container) {
  min-height: 100vh;
}
</style>
