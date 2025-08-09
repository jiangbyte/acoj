<script lang="ts" setup>
import { useAuthFetch } from '@/composables'
import { NAvatar } from 'naive-ui'

const { getProfile } = useAuthFetch()

const profile = ref()
async function loadData() {
  const { data } = await getProfile()
  if (data) {
    profile.value = data
  }
}
loadData()
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <div class="w-full">
      <!-- 题集头部信息 -->
      <div class="bg-white content-card shadow-sm overflow-hidden">
        <!-- 封面图区域 -->
        <div class="relative h-48 md:h-64">
          <img
            :src="profile?.background"
            class="w-full h-full object-cover"
          >
          <!-- 遮罩层和信息 -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent flex items-end">
            <div class="p-6 md:p-8 text-white">
              <!-- 标签组 -->

              <div class="flex items-center gap-4">
                <NAvatar
                  :size="45"
                  round
                  :src="profile?.avatar"
                />
                <n-flex vertical>
                  <n-text class="text-lg font-bold color-white">
                    {{ profile?.nickname }}
                  </n-text>
                  <n-text depth="3" class=" color-white">
                    {{ profile?.quote }}
                  </n-text>
                </n-flex>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮区 -->
        <div class="px-6 md:px-8 py-4 border-t border-gray-100 flex flex-wrap items-center justify-between gap-4">
          <div class="flex items-center gap-6 text-sm">
            <div class="flex items-center text-gray-600">
              <n-icon size="16" class="text-primary mr-1">
                <icon-park-outline-like />
              </n-icon>
              <span>{{ 0 }} 题</span>
            </div>

            <div class="flex items-center text-gray-600">
              <n-icon size="16" class="text-primary mr-1">
                <icon-park-outline-comment />
              </n-icon>
              <span>{{ 0 }} 人参与</span>
            </div>

            <div class="flex items-center text-gray-600">
              <n-icon size="16" class="text-warning mr-1">
                <icon-park-outline-star />
              </n-icon>
              <span>{{ 0 }}% 完成率</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <n-grid cols="12 m:24" :x-gap="16" :y-gap="16" responsive="screen">
      <!-- 左侧筛选区域 -->
      <n-gi span="12 m:6">
        <n-flex vertical>
          <n-card class="content-card" size="small" hoverable>
            45646
          </n-card>
          <n-card class="content-card" size="small" hoverable>
            45646
          </n-card>
        </n-flex>
      </n-gi>
      <n-gi span="12 m:18">
        <n-flex vertical>
          <n-card class="content-card" size="small" hoverable>
            45646
          </n-card>
          <n-card class="content-card" size="small" hoverable>
            45646
          </n-card>
        </n-flex>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>

</style>
