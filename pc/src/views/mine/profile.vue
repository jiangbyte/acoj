<script setup>

</script>

<template>
  <!-- 用户背景Banner -->
  <div class="relative h-64 md:h-80 overflow-hidden">
    <div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-indigo-700 opacity-90" />
    <img
      src="https://picsum.photos/seed/codepattern/1920/1080"
      alt="用户背景图"
      class="absolute inset-0 w-full h-full object-cover mix-blend-overlay"
    >
    <!-- 编辑Banner按钮 - 仅当前用户可见 -->
    <!-- <button class="absolute right-4 bottom-4 px-3 py-1.5 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white rounded-lg text-sm transition-colors">
      <i class="fa fa-pencil mr-1" /> 编辑封面
    </button> -->
  </div>

  <!-- 用户信息区域 -->
  <div class="container mx-auto px-4 -mt-20 relative z-10">
    <div class="flex flex-col md:flex-row gap-6 items-start md:items-end mb-8">
      <!-- 用户头像 -->
      <div class="w-32 h-32 md:w-40 md:h-40 rounded-xl overflow-hidden border-4 border-white dark:border-gray-800 shadow-lg">
        <img src="https://picsum.photos/seed/user123/400/400" alt="用户 code_warrior 的头像" class="w-full h-full object-cover">
      </div>

      <!-- 用户基本信息 -->
      <div class="flex-grow">
        <div class="flex flex-wrap items-start justify-between gap-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 w-full">
          <div>
            <h1 class="text-2xl md:text-3xl font-bold mb-1">
              code_warrior
            </h1>
            <p class="text-gray-600 dark:text-gray-300 mb-3">
              算法爱好者 | 后端开发工程师
            </p>

            <div class="flex flex-wrap items-center gap-3 mb-4">
              <div class="flex items-center text-sm">
                <i class="fa fa-envelope text-gray-500 mr-1.5" />
                <span>code@example.com</span>
              </div>
              <div class="flex items-center text-sm">
                <i class="fa fa-calendar text-gray-500 mr-1.5" />
                <span>注册于 2021-03-15</span>
              </div>
            </div>

            <p class="text-gray-600 dark:text-gray-300 max-w-2xl">
              热爱编程与算法，享受解决问题的乐趣。主要研究方向包括动态规划、图论和分布式系统。
              欢迎交流学习经验和技术心得！
            </p>
          </div>

          <!-- 操作按钮 -->
          <!-- <div class="flex flex-col gap-2">
            <button class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors">
              <i class="fa fa-user-plus mr-1.5" /> 关注
            </button>
            <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors">
              <i class="fa fa-envelope-o mr-1.5" /> 发消息
            </button>
          </div> -->
        </div>
      </div>
    </div>

    <!-- 统计数据卡片 -->
    <div class="grid grid-cols-3 md:grid-cols-3 gap-4 mb-8">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
        <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-1">
          248
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          已解决题目
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
        <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-1">
          87
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          参与题集
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
        <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
          356
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          粉丝数量
        </div>
      </div>
    </div>

    <!-- 内容导航 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm mb-8">
      <div class="flex overflow-x-auto md:overflow-visible border-b border-gray-200 dark:border-gray-700">
        Tabs
        <!-- <button class="px-6 py-4 font-medium text-blue-600 dark:text-blue-400 border-b-2 border-blue-600 dark:border-blue-400">
          解题记录
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          比赛成绩
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          创建的题集
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          发布的讨论
        </button>
        <button class="px-6 py-4 font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
          学习笔记
        </button> -->
      </div>
    </div>

    <!-- 最近解题记录 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
        <h2 class="text-lg font-bold">
          最近解题记录
        </h2>
        <a href="#" class="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
          查看全部 <i class="fa fa-angle-right ml-1" />
        </a>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-700/50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                题目
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hidden md:table-cell">
                难度
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                提交时间
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hidden sm:table-cell">
                耗时
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                状态
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <!-- 解题记录1 -->
            <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">#1003 最大子数组和</a>
              </td>
              <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200">中等</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                2023-06-15 14:32
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden sm:table-cell">
                4 ms
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">通过</span>
              </td>
            </tr>

            <!-- 解题记录2 -->
            <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">#2001 两数之和</a>
              </td>
              <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">简单</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                2023-06-14 09:15
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden sm:table-cell">
                2 ms
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">通过</span>
              </td>
            </tr>

            <!-- 解题记录3 -->
            <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">#3005 最长回文子串</a>
              </td>
              <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200">困难</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                2023-06-12 16:47
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden sm:table-cell">
                8 ms
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">通过</span>
              </td>
            </tr>

            <!-- 解题记录4 -->
            <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">#4002 二叉树的层序遍历</a>
              </td>
              <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200">中等</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                2023-06-10 11:23
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden sm:table-cell">
                5 ms
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">通过</span>
              </td>
            </tr>

            <!-- 解题记录5 -->
            <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                <a href="#" class="text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">#5001 全排列</a>
              </td>
              <td class="px-6 py-4 whitespace-nowrap hidden md:table-cell">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200">中等</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                2023-06-08 19:05
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 hidden sm:table-cell">
                6 ms
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">通过</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 最近比赛成绩和创建的题集 -->
    <div class="grid grid-cols-1 lg:grid-cols-1 gap-8 mb-8">
      <!-- 创建的题集 -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h2 class="text-lg font-bold">
            最近题集记录
          </h2>
          <a href="#" class="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
            查看全部 <i class="fa fa-angle-right ml-1" />
          </a>
        </div>

        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <!-- 题集1 -->
          <div class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <div class="flex gap-4">
              <div class="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0">
                <img src="https://picsum.photos/seed/dp1/200/200" alt="动态规划入门到精通" class="w-full h-full object-cover">
              </div>
              <div class="flex-grow">
                <h3 class="font-medium mb-1">
                  <a href="#" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors">动态规划入门到精通</a>
                </h3>
                <div class="flex flex-wrap items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mb-2">
                  <span>32 题</span>
                  <span>•</span>
                  <span>4.8 分</span>
                  <span>•</span>
                  <span>2,356 人学习</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2">
                  从基础到高级，系统学习动态规划算法，掌握各种经典问题的解题思路和优化技巧。
                </p>
              </div>
            </div>
          </div>

          <!-- 题集2 -->
          <div class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <div class="flex gap-4">
              <div class="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0">
                <img src="https://picsum.photos/seed/graph/200/200" alt="图论算法专题" class="w-full h-full object-cover">
              </div>
              <div class="flex-grow">
                <h3 class="font-medium mb-1">
                  <a href="#" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors">图论算法专题</a>
                </h3>
                <div class="flex flex-wrap items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mb-2">
                  <span>28 题</span>
                  <span>•</span>
                  <span>4.7 分</span>
                  <span>•</span>
                  <span>1,872 人学习</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2">
                  涵盖图的遍历、最短路径、最小生成树、网络流等经典图论算法，适合有一定基础的学习者。
                </p>
              </div>
            </div>
          </div>

          <!-- 题集3 -->
          <div class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <div class="flex gap-4">
              <div class="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0">
                <img src="https://picsum.photos/seed/interview/200/200" alt="面试高频算法题" class="w-full h-full object-cover">
              </div>
              <div class="flex-grow">
                <h3 class="font-medium mb-1">
                  <a href="#" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors">面试高频算法题</a>
                </h3>
                <div class="flex flex-wrap items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mb-2">
                  <span>100 题</span>
                  <span>•</span>
                  <span>4.9 分</span>
                  <span>•</span>
                  <span>5,621 人学习</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2">
                  收集各大公司面试中高频出现的算法题，包含详细解题思路和优化方法，助你备战面试。
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
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

img:hover {
  transform: scale(1.03);
}

/* 表格行悬停效果 */
tr {
  transition: background-color 0.2s ease;
}

/* 导航栏滚动效果 */
header {
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

header.scrolled {
  background-color: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.dark header.scrolled {
  background-color: #1f2937;
}
</style>
