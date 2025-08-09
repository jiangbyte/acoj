<script lang="ts" setup>
import { useSysNoticeFetch } from '@/composables'
import { AesCrypto } from '@/utils'
import MdViewer from '@/components/common/editor/md/Viewer.vue'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.notice as string)
async function loadData() {
  try {
    const { sysNoticeDetail } = useSysNoticeFetch()
    const { data } = await sysNoticeDetail({ id: originalId })

    if (data) {
      detailData.value = data
    }
  }
  catch {
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
            :src="detailData?.cover"
            class="w-full h-full object-cover"
          >
          <!-- 遮罩层和信息 -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent flex items-end">
            <div class="p-6 md:p-8 text-white">
              <!-- 标题 -->
              <h1 class="text-2xl md:text-3xl font-bold mb-2 text-shadow">
                {{ detailData?.title }}
              </h1>
            </div>
          </div>
        </div>
      </div>
    </div>

    <n-card size="small" class="content-card min-h-[calc(100vh-300px)]" hoverable>
      <MdViewer :model-value="detailData?.content" />
    </n-card>
  </div>
</template>

<style scoped>

</style>
