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
  <main class="container mx-auto px-4 py-8">
    <!-- 公告内容 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm mx-auto">
      <!-- 公告封面图 -->
      <div class="rounded-xl overflow-hidden">
        <img :src="detailData?.cover" alt="全国编程大赛通知封面" class="w-full h-70 object-cover">
      </div>

      <!-- 公告标题区 -->
      <div class="mb-4 mt-6 text-center">
        <h1 class="text-3xl md:text-4xl font-bold mb-4">
          {{ detailData?.title }}
        </h1>
        <span class="ml-2 text-sm text-gray-500 dark:text-gray-400">发布于：<n-time :time="detailData?.createTime" /></span>
        <span class="ml-2 text-sm text-gray-500 dark:text-gray-400">发布者：{{ detailData?.createUserName }}</span>
      </div>

      <div class="pl-8 pr-8 pb-4 pt-2">
        <MdViewer :model-value="detailData?.content" />
      </div>
    </div>
  </main>
</template>

<style scoped>
/* 基础样式补充 */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* 图片悬停效果 */
img {
  transition: transform 0.3s ease;
}

/* 评论区样式优化 */
textarea {
  resize: vertical;
}

/* 文章内容样式增强 */
.prose h2 {
  font-size: 1.5rem;
  margin-top: 2rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.prose p {
  margin-bottom: 1rem;
  line-height: 1.8;
}

.prose ul, .prose ol {
  margin-bottom: 1rem;
  padding-left: 1.5rem;
}

.prose li {
  margin-bottom: 0.5rem;
}

.prose table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

.prose th, .prose td {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  text-align: left;
}

.prose th {
  background-color: #f8fafc;
  font-weight: 600;
}

.dark .prose th {
  background-color: #1e293b;
}

.dark .prose th, .dark .prose td {
  border-color: #334155;
}
</style>
