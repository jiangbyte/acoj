<script lang="ts" setup>
import { AesCrypto } from '@/utils'

defineProps<{
  listData: any
}>()
</script>

<template>
  <div class="divide-y divide-gray-100 dark:divide-gray-700">
    <!-- 最新1 -->
    <div
      v-for="item in listData" :key="item.id" class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
        name: 'user',
        query: { userId: AesCrypto.encrypt(item.id) },
      })"
    >
      <div class="flex items-center">
        <NAvatar :src="item.avatar" round :size="40" class="mr-3" />
        <div class="flex-1">
          <div class="font-medium">
            {{ item.nickname }}
          </div>
          <div class="text-xs text-gray-500 dark:text-gray-400">
            活跃指数: {{ Number(item.activeScore).toFixed(2) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>

</style>
