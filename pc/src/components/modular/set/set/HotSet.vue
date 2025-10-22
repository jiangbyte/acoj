<script lang="ts" setup>
import { AesCrypto } from '@/utils'

defineProps<{
  listData: any
}>()
</script>

<template>
  <div class="divide-y divide-gray-100 dark:divide-gray-700">
    <!-- 题集排行项 -->
    <div
      v-for="item in listData" :key="item.rank" class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
        name: 'proset_detail',
        query: { set: AesCrypto.encrypt(item.id) },
      })"
    >
      <div class="flex items-start">
        <RankIcon :rank="item.rank" />
        <div class="flex-1">
          <n-button text class="mb-2">
            <h3 class="font-medium">
              <n-ellipsis style="max-width: 260px">
                {{ item.title }}
              </n-ellipsis>
            </h3>
          </n-button>
          <div class="flex items-center text-xs text-gray-500 dark:text-gray-400">
            <n-text depth="3">
              通过率: {{ item.avgAcceptance }} %
            </n-text>
            <span class="mx-1">•</span>
            <n-text depth="3">
              参与: {{ item.participantUserCount }} 人
            </n-text>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>

</style>
