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
    <!-- 公告标题区 -->
    <div class="mb-6 text-center">
      <div class="inline-flex items-center mb-4">
        <span class="inline-block px-2 py-0.5 bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 text-xs font-medium rounded mr-2">置顶</span>
        <span class="inline-block px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-medium rounded">赛事通知</span>
        <span class="ml-2 text-sm text-gray-500 dark:text-gray-400">发布于 <n-time :time="detailData?.createTime" /></span>
      </div>
      <h1 class="text-3xl md:text-4xl font-bold mb-4">
        {{ detailData?.title }}
      </h1>
      <div class="flex items-center justify-center text-sm text-gray-500 dark:text-gray-400 space-x-6">
        <span class="flex items-center">
          <i class="fa fa-eye mr-1" /> 3245 阅读
        </span>
        <span class="flex items-center">
          <i class="fa fa-comment mr-1" /> 128 评论
        </span>
        <span class="flex items-center">
          <i class="fa fa-user mr-1" /> 管理员发布
        </span>
      </div>
    </div>

    <!-- 公告内容 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm mb-6 mx-auto">
      <!-- 公告封面图 -->
      <div class="rounded-xl overflow-hidden shadow-sm">
        <img :src="detailData?.cover" alt="全国编程大赛通知封面" class="w-full h-70 object-cover">
      </div>

      <div class="pl-8 pr-8 pb-8 pt-2">
        <MdViewer :model-value="detailData?.content" />
        <!-- 附件下载 -->
        <div class="mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
          <h3 class="font-semibold mb-4">
            相关附件
          </h3>
          <div class="space-y-3">
            <a href="#" class="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors">
              <i class="fa fa-file-pdf-o text-red-500 text-xl mr-3" />
              <div class="flex-grow">
                <div class="font-medium">2023年全国大学生编程大赛章程.pdf</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">2.4MB · 2023-06-10</div>
              </div>
              <i class="fa fa-download text-gray-400" />
            </a>
            <a href="#" class="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors">
              <i class="fa fa-file-word-o text-blue-500 text-xl mr-3" />
              <div class="flex-grow">
                <div class="font-medium">2023年全国大学生编程大赛报名表.docx</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">1.1MB · 2023-06-10</div>
              </div>
              <i class="fa fa-download text-gray-400" />
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- 上一篇/下一篇 -->
    <div class="flex flex-col md:flex-row justify-between gap-4 mx-auto">
      <a href="#" class="flex items-center p-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <i class="fa fa-angle-left text-gray-400 mr-3" />
        <div>
          <div class="text-sm text-gray-500 dark:text-gray-400">上一篇</div>
          <div class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">2023春季编程挑战赛获奖名单公布</div>
        </div>
      </a>
      <a href="#" class="flex items-center justify-end p-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="text-right">
          <div class="text-sm text-gray-500 dark:text-gray-400">下一篇</div>
          <div class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">平台将于6月12日进行系统维护升级</div>
        </div>
        <i class="fa fa-angle-right text-gray-400 ml-3" />
      </a>
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
