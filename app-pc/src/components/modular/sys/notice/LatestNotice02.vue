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
        name: 'notice_detail',
        query: { notice: AesCrypto.encrypt(item.id) },
      })"
    >
      <n-grid
        cols="1 l:7"
        :x-gap="18"
        responsive="screen"
      >
        <n-gi span="1 l:2">
          <!-- <n-image
            :src="item?.cover"
            width="100%"
            object-fit="cover"
            class="h-25 rounded-xl"
            preview-disabled
          /> -->
          <img :src="item?.cover" class="w-full h-35 m:h-26 l:h-28 rounded-xl object-cover">
        </n-gi>
        <n-gi span="1 l:5" class="flex items-center w-full">
          <n-thing class="w-full">
            <template #header>
              <n-ellipsis :line-clamp="1">
                {{ item.title }}
              </n-ellipsis>
            </template>
            <template #description>
              <n-text depth="3">
                <n-ellipsis :line-clamp="1" :tooltip="false">
                  {{ CleanMarkdown(item.content) }}
                </n-ellipsis>
              </n-text>
            </template>
            <template #footer>
              <n-space :size="0" align="center" justify="space-between">
                <n-space align="center" :size="0">
                  <n-avatar :src="item?.createUserAvatar" round class="mr-2" />
                  <n-text class="flex-1">
                    <n-ellipsis style="width: 90px">
                      {{ item?.createUserName }}
                    </n-ellipsis>
                  </n-text>
                </n-space>
                <n-time :time="item.createTime" type="relative" />
              </n-space>
            </template>
          </n-thing>
        </n-gi>
      </n-grid>
    </n-list-item>
  </n-list>
</template>

<style>

</style>
