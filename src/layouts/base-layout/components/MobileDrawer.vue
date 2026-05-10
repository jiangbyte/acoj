<template>
  <ADrawer
    :open="open"
    placement="left"
    width="220"
    :closable="false"
    :bodyStyle="{ padding: 0, height: '100%' }"
    @update:open="$emit('update:open', $event)"
  >
    <div class="flex flex-col h-full">
      <Logo :collapsed="false" />
      <AMenu
        mode="inline"
        :selectedKeys="[route.path]"
        :openKeys="openKeys"
        :items="menuItems"
        @click="handleClick"
        @openChange="handleOpenChange"
        class="flex-1 overflow-auto"
      />
    </div>
  </ADrawer>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import Logo from './Logo.vue'
import { useMenu } from '../sider/useMenu'

defineProps<{ open: boolean }>()
const emit = defineEmits<{ 'update:open': [value: boolean] }>()

const route = useRoute()
const { openKeys, menuItems, handleMenuClick, handleOpenChange } = useMenu()

function handleClick(key: { key: string }) {
  handleMenuClick(key)
  emit('update:open', false)
}
</script>
