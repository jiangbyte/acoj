<script lang="ts" setup>
import { AesCrypto } from '@/utils'
import MdViewer from '@/components/common/editor/md/Viewer.vue'
import { useProSetFetch } from '@/composables'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.set as string)
async function loadData() {
  try {
    const { proSetDetail } = useProSetFetch()
    const { data } = await proSetDetail({ id: originalId })

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
    <!-- 题集封面和基本信息 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
      <div class="md:flex">
        <!-- 题集封面图 -->
        <div class="md:w-1/3 lg:w-1/4">
          <img :src="detailData?.cover" class="w-full h-full object-cover">
        </div>

        <!-- 题集基本信息 -->
        <div class="p-6 md:p-8 md:w-2/3 lg:w-3/4">
          <div class="flex flex-wrap items-start justify-between gap-4 mb-4">
            <div>
              <h1 class="text-2xl md:text-3xl font-bold mb-2">
                {{ detailData?.title }}
              </h1>
              <div class="flex flex-wrap items-center gap-2">
                <span class="inline-block px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-medium rounded"> {{ detailData?.setTypeName }}</span>
                <span class="inline-block px-2 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs font-medium rounded">  {{ detailData?.categoryName }}</span>
                <span class="inline-block px-2 py-0.5 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs font-medium rounded">{{ detailData?.difficultyName }}</span>
              </div>
            </div>

            <div class="flex gap-3">
              <!-- <button class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors flex items-center">
                  <i class="fa fa-bookmark-o mr-2"></i> 收藏
                </button>
                <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors flex items-center">
                  <i class="fa fa-share-alt mr-2"></i> 分享
                </button> -->
            </div>
          </div>

          <!-- 题集统计信息 -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                题目数量
              </div>
              <div class="text-xl font-bold">
                32 题
              </div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                总提交次数
              </div>
              <div class="text-xl font-bold">
                15,842
              </div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                平均通过率
              </div>
              <div class="text-xl font-bold">
                42.8%
              </div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                学习人数
              </div>
              <div class="text-xl font-bold">
                2,356
              </div>
            </div>
          </div>

          <!-- 题集作者信息 -->
          <!-- <div class="flex items-center mb-6">
              <div class="w-10 h-10 rounded-full overflow-hidden mr-3">
                <img src="https://picsum.photos/seed/author1/100/100" alt="题集作者 algorithm_master 头像" class="w-full h-full object-cover">
              </div>
              <div>
                <div class="font-medium">作者: algorithm_master</div>
                <div class="text-sm text-gray-500 dark:text-gray-400">发布于 2023-03-15 · 最后更新 2023-05-20</div>
              </div>
            </div> -->
        </div>
      </div>
    </div>
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8 p-x-6">
      <MdViewer :model-value="detailData?.description" />
    </div>
    <!-- 题集内容导航 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm mb-8">
      <div class="flex overflow-x-auto md:overflow-visible border-b border-gray-200 dark:border-gray-700">
        <button class="px-6 py-4 font-medium text-blue-600 dark:text-blue-400 border-b-2 border-blue-600 dark:border-blue-400">
          题目列表
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          学习进度
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          讨论区
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          学习笔记
        </button>
      </div>
    </div>

    <!-- 题目筛选和搜索 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 mb-8">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex flex-wrap gap-2">
          <button class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium transition-colors">
            全部题目
          </button>
          <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
            未完成
          </button>
          <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
            已完成
          </button>
        </div>

        <div class="flex-grow relative">
          <input
            type="text"
            placeholder="搜索题目..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          >
          <i class="fa fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        </div>

        <div class="w-full md:w-auto">
          <select class="w-full md:w-auto px-4 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all appearance-none pr-10 relative">
            <option value="default">
              默认排序
            </option>
            <option value="difficulty-asc">
              难度从低到高
            </option>
            <option value="difficulty-desc">
              难度从高到低
            </option>
            <option value="acceptance-asc">
              通过率从低到高
            </option>
            <option value="acceptance-desc">
              通过率从高到低
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-700/50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-16">
              题号
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              题目名称
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hidden md:table-cell">
              难度
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hidden lg:table-cell">
              通过率
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hidden md:table-cell">
              提交次数
            </th>
            <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              状态
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <!-- 题目1 -->
          <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
              #1001
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">斐波那契数列</a>
                <span class="ml-2 inline-block px-1.5 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs rounded">入门</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                基础动态规划思想
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">简单</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden lg:table-cell">
              89.2%
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden md:table-cell">
              3,452
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <span class="text-green-600 dark:text-green-400 flex items-center justify-end">
                <i class="fa fa-check-circle mr-1" /> 已完成
              </span>
            </td>
          </tr>

          <!-- 题目2 -->
          <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
              #1002
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">爬楼梯</a>
                <span class="ml-2 inline-block px-1.5 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs rounded">入门</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                基础动态规划应用
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">简单</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden lg:table-cell">
              78.5%
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden md:table-cell">
              2,987
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <span class="text-green-600 dark:text-green-400 flex items-center justify-end">
                <i class="fa fa-check-circle mr-1" /> 已完成
              </span>
            </td>
          </tr>

          <!-- 题目3 -->
          <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
              #1003
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">最大子数组和</a>
                <span class="ml-2 inline-block px-1.5 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs rounded">经典</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Kadane算法
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200">中等</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden lg:table-cell">
              62.3%
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden md:table-cell">
              4,125
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
                开始做题
              </a>
            </td>
          </tr>

          <!-- 题目4 -->
          <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
              #1004
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">最长递增子序列</a>
                <span class="ml-2 inline-block px-1.5 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs rounded">经典</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                序列类动态规划
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200">中等</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden lg:table-cell">
              51.7%
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden md:table-cell">
              3,876
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
                开始做题
              </a>
            </td>
          </tr>

          <!-- 题目5 -->
          <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
              #1005
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">编辑距离</a>
                <span class="ml-2 inline-block px-1.5 py-0.5 bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 text-xs rounded">挑战</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                二维动态规划
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
              <span class="px-2 py-1 text-xs font-medium rounded-full bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200">困难</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden lg:table-cell">
              32.8%
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden md:table-cell">
              2,154
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
                开始做题
              </a>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="px-6 py-4 bg-gray-50 dark:bg-gray-700/50 border-t border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            显示 1 至 5，共 32 题
          </div>
          <div class="flex items-center space-x-1">
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed" disabled>
              <i class="fa fa-angle-left" />
            </button>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg bg-blue-600 text-white">
              1
            </button>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
              2
            </button>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
              3
            </button>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
              4
            </button>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
              5
            </button>
            <button class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
              <i class="fa fa-angle-right" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 相关推荐题集 -->
    <div class="mb-8">
      <h2 class="text-xl font-bold mb-4">
        相关推荐题集
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- 推荐题集1 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
          <div class="h-36 overflow-hidden">
            <img src="https://picsum.photos/seed/rec1/600/400" alt="算法入门与进阶" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>
          <div class="p-4">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-medium rounded mr-2">算法</span>
              <span class="text-xs text-gray-500 dark:text-gray-400">28题</span>
            </div>
            <h3 class="font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
              <a href="#">算法入门与进阶</a>
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2 mb-3">
              从基础算法到高级算法，循序渐进地掌握算法设计与分析能力。
            </p>
            <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
              <span>by data_structure</span>
              <span>4.8 分</span>
            </div>
          </div>
        </div>

        <!-- 推荐题集2 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
          <div class="h-36 overflow-hidden">
            <img src="https://picsum.photos/seed/rec2/600/400" alt="数据结构实战" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>
          <div class="p-4">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs font-medium rounded mr-2">数据结构</span>
              <span class="text-xs text-gray-500 dark:text-gray-400">35题</span>
            </div>
            <h3 class="font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
              <a href="#">数据结构实战</a>
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2 mb-3">
              专注于数据结构的实现与应用，提升编程基础能力。
            </p>
            <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
              <span>by structure_master</span>
              <span>4.7 分</span>
            </div>
          </div>
        </div>

        <!-- 推荐题集3 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
          <div class="h-36 overflow-hidden">
            <img src="https://picsum.photos/seed/rec3/600/400" alt="编程面试真题" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>
          <div class="p-4">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs font-medium rounded mr-2">面试</span>
              <span class="text-xs text-gray-500 dark:text-gray-400">50题</span>
            </div>
            <h3 class="font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
              <a href="#">编程面试真题</a>
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2 mb-3">
              收集各大公司面试高频算法题，助你轻松备战面试。
            </p>
            <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
              <span>by interview_pro</span>
              <span>4.9 分</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
</style>
