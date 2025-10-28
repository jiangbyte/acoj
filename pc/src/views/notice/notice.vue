<script lang="ts" setup>
import { useSysNoticeFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.notice as string)
const noticeListData = ref()
async function loadData() {
  useSysNoticeFetch().sysNoticeDetail({ id: originalId }).then(({ data }) => {
    detailData.value = data
  })

  useSysNoticeFetch().sysNoticeLatest().then(({ data }) => {
    noticeListData.value = data ?? []
  })
}
loadData()
</script>

<template>
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:6"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <!-- 左侧主内容 -->
      <n-gi span="1 l:4">
        <!-- 公告内容 -->
        <n-card class="rounded-xl" size="small" content-style="padding: 0">
          <!-- 公告封面图 -->
          <n-image
            :src="detailData?.cover"
            width="100%"
            object-fit="cover"
            class="w-full h-62 md:h-60 rounded-t-xl object-cover"
          />

          <!-- 公告标题区 -->
          <n-flex class="p-x-6 pt-4 pb-4" :size="0">
            <n-h1>
              {{ detailData?.title }}
            </n-h1>
            <!-- 元信息卡片 -->
            <n-card class="rounded-xl" size="small">
              <n-flex class="w-full" vertical>
                <n-space align="center" :size="0">
                  <n-text>
                    作者：
                  </n-text>
                  <n-space align="center" :size="0">
                    <n-avatar :src="detailData?.createUserAvatar" round :size="36" class="mr-3 shadow-sm" />
                    <n-text class="flex-1">
                      {{ detailData?.createUserName }}
                    </n-text>
                  </n-space>
                </n-space>
                <n-space align="center" :size="0">
                  <n-text>
                    发布时间：
                  </n-text>
                  <n-flex>
                    <n-time :time="detailData?.createTime" />
                  </n-flex>
                </n-space>
                <n-space align="center" :size="0">
                  <n-text>
                    更新时间：
                  </n-text>
                  <n-flex>
                    <n-time :time="detailData?.updateTime" />
                  </n-flex>
                </n-space>
              </n-flex>
            </n-card>
          </n-flex>

          <!-- 内容区域 -->
          <MdViewer :model-value="detailData?.content" class="rounded-b-xl px-6 pb-2" />
        </n-card>
      </n-gi>

      <!-- 右侧边栏 -->
      <n-gi span="1 l:2">
        <n-card class="rounded-xl" size="small" content-style="padding: 0">
          <template #header>
            <n-h2 class="pb-0 mb-0">
              最新公告
            </n-h2>
          </template>
          <ListSkeleton01 v-if="!noticeListData" />
          <EmptyData v-else-if="noticeListData.length === 0" />
          <LatestNotice02 v-else :list-data="noticeListData" />
        </n-card>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>

</style>
