<script setup>
// 注意：按照要求，这里不实现交互功能
// 所有数据都直接在模板中硬编码，方便后续修改
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- 页面标题和描述 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">
        公告中心
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        查看平台最新通知、赛事公告和系统更新
      </p>
    </div>

    <!-- 公告分类筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 mb-8">
      <div class="flex flex-wrap items-center gap-2">
        <button class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium transition-colors">
          全部公告
        </button>
        <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
          赛事通知
        </button>
        <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
          系统更新
        </button>
        <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
          活动公告
        </button>
        <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
          重要通知
        </button>
      </div>
    </div>

    <!-- 置顶公告 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8 border-l-4 border-red-500">
      <div class="md:flex">
        <!-- 公告封面图 -->
        <div class="md:w-1/3 h-48 md:h-auto overflow-hidden">
          <img src="https://picsum.photos/seed/announcement1/800/500" alt="全国编程大赛通知封面" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
        </div>

        <!-- 公告内容 -->
        <div class="p-6 md:w-2/3">
          <div class="flex items-center mb-3">
            <span class="inline-block px-2 py-0.5 bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 text-xs font-medium rounded mr-2">置顶</span>
            <span class="inline-block px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-medium rounded">赛事通知</span>
            <span class="ml-auto text-sm text-gray-500 dark:text-gray-400">2023-06-10</span>
          </div>

          <h2 class="text-xl font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
            <a href="#">2023年全国大学生编程大赛报名启动</a>
          </h2>

          <p class="text-gray-600 dark:text-gray-300 mb-4 line-clamp-2 md:line-clamp-3">
            2023年全国大学生编程大赛将于8月15日正式开赛，本次大赛由教育部高等学校计算机类专业教学指导委员会主办，CodeMaster平台承办。大赛分为初赛、复赛和决赛三个阶段，设置多个奖项，优胜者将有机会代表国家参加国际赛事。报名时间截止至7月30日，欢迎各高校学生踊跃参与。
          </p>

          <div class="flex items-center justify-between">
            <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
              <span class="flex items-center mr-4">
                <i class="fa fa-eye mr-1" /> 3245
              </span>
              <span class="flex items-center">
                <i class="fa fa-comment mr-1" /> 128
              </span>
            </div>

            <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 text-sm font-medium flex items-center">
              阅读全文 <i class="fa fa-angle-right ml-1" />
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
      <!-- 公告项 1 -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
        <div class="md:flex flex-col h-full">
          <!-- 公告封面图 -->
          <div class="h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/announcement2/800/500" alt="系统维护通知封面" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>

          <!-- 公告内容 -->
          <div class="p-5 flex-grow">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-orange-100 dark:bg-orange-900 text-orange-800 dark:text-orange-200 text-xs font-medium rounded">系统更新</span>
              <span class="ml-auto text-sm text-gray-500 dark:text-gray-400">2023-06-05</span>
            </div>

            <h3 class="text-lg font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors line-clamp-1">
              <a href="#">平台将于6月12日进行系统维护升级</a>
            </h3>

            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
              为提供更好的服务体验，平台将于6月12日00:00-06:00进行系统维护升级，期间可能无法正常访问。本次升级将优化评测系统性能，新增多种编程语言支持。
            </p>
          </div>

          <div class="px-5 pb-5 pt-0 flex items-center justify-between text-sm">
            <div class="flex items-center text-gray-500 dark:text-gray-400">
              <span class="flex items-center mr-4">
                <i class="fa fa-eye mr-1" /> 1876
              </span>
              <span class="flex items-center">
                <i class="fa fa-comment mr-1" /> 56
              </span>
            </div>

            <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium">
              详情
            </a>
          </div>
        </div>
      </div>

      <!-- 公告项 2 -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
        <div class="md:flex flex-col h-full">
          <!-- 公告封面图 -->
          <div class="h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/announcement3/800/500" alt="新手训练营活动封面" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>

          <!-- 公告内容 -->
          <div class="p-5 flex-grow">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs font-medium rounded">活动公告</span>
              <span class="ml-auto text-sm text-gray-500 dark:text-gray-400">2023-06-01</span>
            </div>

            <h3 class="text-lg font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors line-clamp-1">
              <a href="#">"编程新手训练营"活动开始报名</a>
            </h3>

            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
              为帮助编程初学者快速入门，平台将于6月15日至7月15日举办"编程新手训练营"活动，活动期间将提供每日一题、在线讲座和答疑服务，完成挑战可获得专属证书和奖品。
            </p>
          </div>

          <div class="px-5 pb-5 pt-0 flex items-center justify-between text-sm">
            <div class="flex items-center text-gray-500 dark:text-gray-400">
              <span class="flex items-center mr-4">
                <i class="fa fa-eye mr-1" /> 2154
              </span>
              <span class="flex items-center">
                <i class="fa fa-comment mr-1" /> 89
              </span>
            </div>

            <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium">
              详情
            </a>
          </div>
        </div>
      </div>

      <!-- 公告项 3 -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
        <div class="md:flex flex-col h-full">
          <!-- 公告封面图 -->
          <div class="h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/announcement4/800/500" alt="新增题库通知封面" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>

          <!-- 公告内容 -->
          <div class="p-5 flex-grow">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs font-medium rounded">重要通知</span>
              <span class="ml-auto text-sm text-gray-500 dark:text-gray-400">2023-05-28</span>
            </div>

            <h3 class="text-lg font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors line-clamp-1">
              <a href="#">新增100道算法与数据结构题目</a>
            </h3>

            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
              平台今日新增100道高质量算法与数据结构题目，涵盖树、图、动态规划等多个知识点，难度从易到难分布，适合不同水平的用户练习。
            </p>
          </div>

          <div class="px-5 pb-5 pt-0 flex items-center justify-between text-sm">
            <div class="flex items-center text-gray-500 dark:text-gray-400">
              <span class="flex items-center mr-4">
                <i class="fa fa-eye mr-1" /> 1567
              </span>
              <span class="flex items-center">
                <i class="fa fa-comment mr-1" /> 42
              </span>
            </div>

            <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium">
              详情
            </a>
          </div>
        </div>
      </div>

      <!-- 公告项 4 -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
        <div class="md:flex flex-col h-full">
          <!-- 公告封面图 -->
          <div class="h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/announcement5/800/500" alt="春季编程挑战赛结果封面" class="w-full h-full object-cover hover:scale-105 transition-transform duration-500">
          </div>

          <!-- 公告内容 -->
          <div class="p-5 flex-grow">
            <div class="flex items-center mb-2">
              <span class="inline-block px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-medium rounded">赛事通知</span>
              <span class="ml-auto text-sm text-gray-500 dark:text-gray-400">2023-05-20</span>
            </div>

            <h3 class="text-lg font-bold mb-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors line-clamp-1">
              <a href="#">2023春季编程挑战赛获奖名单公布</a>
            </h3>

            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
              2023春季编程挑战赛已圆满结束，本次比赛共有来自全国300余所高校的2500余名选手参赛。经过激烈角逐，共产生一等奖5名，二等奖15名，三等奖30名，优秀奖50名。
            </p>
          </div>

          <div class="px-5 pb-5 pt-0 flex items-center justify-between text-sm">
            <div class="flex items-center text-gray-500 dark:text-gray-400">
              <span class="flex items-center mr-4">
                <i class="fa fa-eye mr-1" /> 2890
              </span>
              <span class="flex items-center">
                <i class="fa fa-comment mr-1" /> 156
              </span>
            </div>

            <a href="#" class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium">
              详情
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center">
      <div class="flex items-center space-x-1">
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed" disabled>
          <i class="fa fa-angle-left" />
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg bg-blue-600 text-white">
          1
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          2
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          3
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          4
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          5
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          <i class="fa fa-angle-right" />
        </button>
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

/* 图片悬停放大效果 */
img {
  transition: transform 0.3s ease;
}

/* 解决select下拉箭头在部分浏览器不显示的问题 */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* 卡片悬停效果 */
.bg-white {
  transition: box-shadow 0.3s ease;
}
</style>
