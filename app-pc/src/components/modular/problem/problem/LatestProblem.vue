<script lang="ts" setup>
import { AesCrypto, CleanMarkdown } from '@/utils'

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
      <n-thing>
        <template #header>
          <n-ellipsis :line-clamp="1">
            {{ item.title }}
          </n-ellipsis>
        </template>
        <!-- <template #header-extra>
          <n-space align="center">
            <n-text depth="3">
              通过率
            </n-text>
            <n-tag size="small" type="info">
              {{ item.acceptance }} %
            </n-tag>
          </n-space>
        </template> -->
        <template #description>
          <n-text depth="3">
            <n-ellipsis :line-clamp="1" :tooltip="false">
              {{ CleanMarkdown(item.description) }}
            </n-ellipsis>
          </n-text>
        </template>
        <template #footer>
          <n-space :size="0" align="center" justify="space-between">
            <n-space align="center">
              <n-tag size="small" type="info">
                {{ item.categoryName }}
              </n-tag>
              <n-tag size="small" type="info">
                {{ item.difficultyName }}
              </n-tag>
            </n-space>
            <n-space align="center" :size="3">
              <n-time :time="item.createTime" type="relative" />
            </n-space>
          </n-space>
        </template>
      </n-thing>
    </n-list-item>
  </n-list>

  <!-- <div class="space-y-4">
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
  </div> -->
</template>

<style>

</style>
