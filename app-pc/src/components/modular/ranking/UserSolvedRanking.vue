<script lang="ts" setup>
import { AesCrypto } from '@/utils'

defineProps<{
  listData: any
}>()
</script>

<template>
  <n-list hoverable class="rounded-xl p-0">
    <n-list-item
      v-for="item in listData" :key="item.id" @click="$router.push({
        name: 'user',
        query: { userId: AesCrypto.encrypt(item.id) },
      })"
    >
      <template #prefix>
        <RankIcon :rank="item.rank" />
      </template>
      <n-thing>
        <n-space align="center">
          <n-avatar :src="item?.avatar" round :size="36" class="mr-2" />
          <n-space :size="0" vertical>
            <n-text class="flex-1">
              <n-ellipsis style="width: 90px">
                {{ item?.nickname }}
              </n-ellipsis>
            </n-text>
            <n-text depth="3">
              解题 {{ item.solvedCount }}
            </n-text>
          </n-space>
        </n-space>
      </n-thing>
    </n-list-item>
  </n-list>
  <!-- <div class="divide-y divide-gray-100 dark:divide-gray-700">
    <div
      v-for="item in listData" :key="item.rank" class="p-4 flex items-center hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
        name: 'user',
        query: { userId: AesCrypto.encrypt(item.id) },
      })"
    >
      <RankIcon :rank="item.rank" />
      <n-avatar :src="item.avatar" round :size="40" class="mr-3" />
      <div class="flex-1">
        <div class="font-medium mb-0.5">
          {{ item.nickname }}
        </div>
        <div class="flex items-center text-xs text-gray-500 dark:text-gray-400">
          <n-text depth="3">
            通过率: {{ item.acceptanceRate }} %
          </n-text>
          <span class="mx-1">•</span>
          <n-text depth="3">
            解题: {{ item.solvedCount }} 道
          </n-text>
        </div>
      </div>
    </div>
  </div>
  <n-button text class="text-center w-full p-4" @click="$router.push({ path: '/ranking' })">
    查看完整排行榜
  </n-button> -->
</template>

<style>

</style>
