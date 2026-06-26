<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores'

const router = useRouter()
const appStore = useAppStore()
const name = import.meta.env.VITE_APP_TITLE
const homePath = import.meta.env.VITE_HOME_PATH

// 侧边栏折叠时只保留图标，隐藏系统名称，避免窄宽度下文字挤压菜单区域。
const hiddenLogoText = computed(() => appStore.collapsed)
</script>

<template>
  <button class="logo-button" type="button" @click="router.push(homePath)">
    <NovaIcon icon="icon-park-outline:code-computer" :size="24" />
    <span v-show="!hiddenLogoText" class="text-ellipsis overflow-hidden whitespace-nowrap">
      {{ name }}
    </span>
  </button>
</template>

<style scoped>
.logo-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 60px;
  gap: 8px;
  padding: 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: inherit;
  cursor: pointer;
  background: transparent;
  border: 0;
}
</style>
