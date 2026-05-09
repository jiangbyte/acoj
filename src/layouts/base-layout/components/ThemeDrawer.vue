<template>
  <a-drawer :open="open" title="主题设置" width="360" @close="emit('update:open', false)">
    <div class="mb-4">
      <div class="font-medium mb-2">布局模式</div>
      <a-radio-group v-model:value="app.layoutMode" button-style="solid">
        <a-radio-button value="vertical">侧边栏</a-radio-button>
        <a-radio-button value="horizontal">顶部</a-radio-button>
        <a-radio-button value="mixed">混合</a-radio-button>
      </a-radio-group>
    </div>
    <div class="mb-4">
      <div class="font-medium mb-2">主题色</div>
      <div class="flex gap-2">
        <div
          v-for="color in colors" :key="color"
          class="w-8 h-8 rounded cursor-pointer border-2"
          :class="app.colorPrimary === color ? 'border-primary' : 'border-transparent'"
          :style="{ backgroundColor: color }"
          @click="app.colorPrimary = color"
        />
      </div>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])
const app = useAppStore()
const colors = ['#1677ff', '#52c41a', '#faad14', '#ff4d4f', '#722ed1', '#13c2c2']
</script>
