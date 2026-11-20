<script lang="ts" setup>
import { useSiteStore } from '@/stores'

const siteStore = useSiteStore()
</script>

<template>
  <div
    class="admin-logo"
    @click="$router.push('/')"
  >
    <span class="admin-logo__title">
      {{ siteStore.collapsed ? 'AC' : 'ACOJ' }}
    </span>
  </div>
</template>

<style scoped>
.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  background: #001529;
  cursor: pointer;
  flex-shrink: 0;
}

.admin-logo__title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.04em;
  white-space: nowrap;
  user-select: none;
}
</style>
