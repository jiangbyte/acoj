<script lang="ts" setup>
import { AesCrypto, CleanMarkdown } from '@/utils'

defineProps<{
  setListData: any
}>()
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
    <!-- Section -->
    <div
      v-for="item in setListData" :key="item.id" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow" @click="$router.push({
        name: 'proset_detail',
        query: { set: AesCrypto.encrypt(item.id) },
      })"
    >
      <div class="h-40 relative">
        <img :src="item.cover" class="absolute inset-0 w-full h-full object-cover">
      </div>
      <div class="p-5">
        <div class="flex items-center mb-2">
          <n-tag size="small" type="info" class="mr-3">
            {{ item.setTypeName }}
          </n-tag>
          <span class="text-sm text-gray-500 dark:text-gray-400">{{ item.problemIds.length ? item.problemIds.length : 0 }} problems</span>
        </div>
        <n-button text class="mb-2">
          <h3 class="text-xl font-semibold">
            <n-ellipsis style="max-width: 260px">
              {{ item.title }}
            </n-ellipsis>
          </h3>
        </n-button>
        <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
          {{ CleanMarkdown(item.description) }}
        </p>
        <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
          <span>Category: {{ item.categoryName }}</span>
          <span>Difficulty: {{ item.difficultyName }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style>

</style>
