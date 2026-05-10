<template>
  <ALayoutSider
    v-model:collapsed="app.collapsed"
    :width="220"
    :theme="siderTheme"
    collapsible
    :trigger="null"
    :style="{ overflow: 'auto' }"
  >
    <Logo :collapsed="app.collapsed" />
    <AMenu
      mode="inline"
      :selectedKeys="[route.path]"
      :openKeys="openKeys"
      :theme="menuTheme"
      :items="menuItems"
      @click="handleMenuClick"
      @openChange="handleOpenChange"
    />
  </ALayoutSider>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store'
import Logo from '../components/Logo.vue'
import { useMenu } from './useMenu'

const route = useRoute()
const app = useAppStore()

const { openKeys, menuItems, handleMenuClick, handleOpenChange } = useMenu()

const siderTheme = computed(() => app.theme === 'light' ? 'light' : 'dark')
const menuTheme = computed(() => siderTheme.value)
</script>
