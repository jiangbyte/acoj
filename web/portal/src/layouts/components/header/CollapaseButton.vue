<script setup lang="ts">
import { useAppStore } from '@/stores'

const appStore = useAppStore()
</script>

<template>
  <n-tooltip placement="bottom" trigger="hover">
    <template #trigger>
      <CommonWrapper @click="appStore.toggleCollapse()">
        <NovaIcon
          :icon="
            appStore.collapsed ? 'icon-park-outline:menu-unfold' : 'icon-park-outline:menu-fold'
          "
        />
      </CommonWrapper>
    </template>
    {{ $t('app.toggleSider') }}
  </n-tooltip>
</template>
