<template>
  <ADrawer
    :open="open"
    placement="left"
    width="220"
    :closable="false"
    :body-style="{ padding: 0, height: '100%' }"
    @update:open="$emit('update:open', $event)"
  >
    <div class="flex flex-col h-full">
      <Logo :collapsed="false" />
      <AMenu
        mode="inline"
        :selected-keys="[route.path]"
        :open-keys="openKeys"
        :items="menuItems"
        class="flex-1 overflow-auto"
        @click="handleClick"
        @open-change="handleOpenChange"
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
