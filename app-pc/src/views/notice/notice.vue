<script lang="ts" setup>
import { useSysNoticeFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const route = useRoute()
const detailData = ref<any>()
const detailLoaded = ref(false)
const originalId = AesCrypto.decrypt(route.query.notice as string)
const noticeListData = ref()

/**
  * Implementation details.
 */
async function loadData() {
  // Implementation note.
  const { success, data } = await useSysNoticeFetch().sysNoticeDetail({ id: originalId })
  detailLoaded.value = true
  detailData.value = success && data ? data : null

  // Implementation note.
  useSysNoticeFetch().sysNoticeLatest().then(({ data: list }) => {
    noticeListData.value = list ?? []
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
      <!-- Section -->
      <n-gi span="1 l:4">
        <n-card class="rounded-xl" size="small" content-style="padding: 0">
          <template v-if="!detailLoaded">
            <ListSkeleton01 />
          </template>
          <EmptyData v-else-if="!detailData" />
          <template v-else>
            <!-- Section -->
            <SafeImage
              v-if="detailData.cover"
              :src="detailData.cover"
              width="100%"
              object-fit="cover"
              class="w-full h-62 md:h-60 rounded-t-xl"
              previewable
            />

            <!-- Section -->
            <n-flex class="p-x-6 pt-4 pb-4" :size="0">
              <n-h1>
                {{ detailData.title }}
              </n-h1>
              <!-- Section -->
              <n-card class="rounded-xl" size="small">
                <n-flex class="w-full" vertical>
                  <n-space align="center" :size="0">
                    <n-text>
                      Author:
                    </n-text>
                    <n-space align="center" :size="0">
                      <SafeAvatar :src="detailData.createUserAvatar" round :size="36" class="mr-3 shadow-sm" />
                      <n-text class="flex-1">
                        {{ detailData.createUserName }}
                      </n-text>
                    </n-space>
                  </n-space>
                  <n-space align="center" :size="0">
                    <n-text>
                      Published:
                    </n-text>
                    <n-flex>
                      <n-time v-if="detailData.createTime" :time="detailData.createTime" />
                    </n-flex>
                  </n-space>
                  <n-space align="center" :size="0">
                    <n-text>
                      Updated:
                    </n-text>
                    <n-flex>
                      <n-time v-if="detailData.updateTime" :time="detailData.updateTime" />
                    </n-flex>
                  </n-space>
                </n-flex>
              </n-card>
            </n-flex>

            <!-- Section -->
            <MdViewer :model-value="detailData.content" class="rounded-b-xl px-6 pb-2" />
          </template>
        </n-card>
      </n-gi>

      <!-- Section -->
      <n-gi span="1 l:2">
        <n-card class="rounded-xl" size="small" content-style="padding: 0">
          <template #header>
            <n-h2 class="pb-0 mb-0">
              Latest Notices
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
