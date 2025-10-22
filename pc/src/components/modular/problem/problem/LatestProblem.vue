<script lang="ts" setup>
import { AesCrypto } from '@/utils'

defineProps<{
  problemListData: any
}>()
</script>

<template>
  <div class="space-y-4">
    <!-- 题目项 -->
    <div
      v-for="item in problemListData" :key="item.id" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 hover:shadow-md transition-shadow" @click="$router.push({
        name: 'problem_submit',
        query: { problemId: AesCrypto.encrypt(item.id) },
      })"
    >
      <div class="flex flex-col md:flex-row md:items-center justify-between">
        <div>
          <div class="flex items-center mb-2">
            <n-button text class="mr-3">
              <h3 class="text-xl font-semibold">
                <n-ellipsis style="max-width: 260px">
                  {{ item.title }}
                </n-ellipsis>
              </h3>
            </n-button>
          </div>
          <div class="flex mb-3">
            <n-tag v-for="tagName in item.tagNames" :key="tagName" size="small" class="mr-3">
              {{ tagName }}
            </n-tag>
          </div>
          <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
            <n-tag class="mr-3" size="small" type="success">
              {{ item.difficultyName }}
            </n-tag>
            <n-tag class="mr-4" size="small" type="info">
              {{ item.categoryName }}
            </n-tag>
            <span>通过率: {{ item.acceptance }} %</span>
          </div>
        </div>
        <div class="mt-4 md:mt-0 flex items-center text-sm text-gray-500 dark:text-gray-400">
          <span>发布于 <n-time :time="item.createTime" /></span>
        </div>
      </div>
    </div>
  </div>
</template>

<style>

</style>
