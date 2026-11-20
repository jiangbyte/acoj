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
        name: 'problem_submit',
        query: { problemId: AesCrypto.encrypt(item.id) },
      })"
    >
      <template #prefix>
        <RankIcon :rank="item.rank" />
      </template>
      <n-thing>
        <template #header>
          <n-ellipsis :line-clamp="1">
            {{ item.title }}
          </n-ellipsis>
        </template>
        <!-- <template #header-extra>
          <n-tag size="small" type="info">
            {{ item.difficultyName }}
          </n-tag>
        </template> -->
        <template #description>
          <n-space align="center">
            <n-space align="center" :size="0">
              <n-text depth="3">
                参与人数：{{ item.participantUserCount }}
              </n-text>
            </n-space>
            <n-space align="center" :size="0">
              <n-text depth="3">
                通过率：{{ item.acceptance }} %
              </n-text>
            </n-space>
          </n-space>
        </template>
      </n-thing>
    </n-list-item>
  </n-list>
  <!-- <div class="divide-y divide-gray-100 dark:divide-gray-700">
    <div
      v-for="item in listData" :key="item.rank" class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
        name: 'problem_submit',
        query: { problemId: AesCrypto.encrypt(item.id) },
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
              通过率: {{ item.acceptance }} %
            </n-text>
            <span class="mx-1">•</span>
            <n-text depth="3">
              参与: {{ item.participantUserCount }} 人
            </n-text>
          </div>
        </div>
      </div>
    </div>
  </div> -->
</template>

<style>

</style>
