<script lang="ts" setup>
import { AesCrypto, CleanMarkdown } from '@/utils'

defineProps<{
  noticeListData: any
}>()
</script>

<template>
  <div class="space-y-4">
    <div
      v-for="item in noticeListData" :key="item.id"
      class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow " @click="$router.push({
        name: 'notice_detail',
        query: { notice: AesCrypto.encrypt(item.id) },
      })"
    >
      <div class="md:flex">
        <div class="md:w-1/3 h-32 md:h-auto relative">
          <img :src="item.cover" :alt="item.title" class="absolute inset-0 w-full h-full object-cover">
        </div>
        <div class="md:w-2/3 p-x-6 pt-6 pb-1">
          <div class="flex items-center text-sm text-gray-500 dark:text-gray-400 mb-2">
            <n-time :time="item.createTime" />
            <span class="mx-2">•</span>
            <n-text>
              {{ item.createUserName }}
            </n-text>
          </div>
          <n-button text class="mb-3">
            <h3 class="text-xl font-semibold ">
              <n-ellipsis style="max-width: 230px">
                {{ item.title }}
              </n-ellipsis>
            </h3>
          </n-button>
          <p class="text-gray-600 dark:text-gray-300 mb-4 line-clamp-2">
            {{ CleanMarkdown(item.content) }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notice-cover {
  border-radius: 6px;
  overflow: hidden;
}
</style>
