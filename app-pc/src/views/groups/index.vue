<script setup>
// 注意：按照要求，这里不实现交互功能
// 所有数据都直接在模板中硬编码，方便后续修改
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- 页面标题和操作区 -->
    <div class="mb-8 flex flex-col md:flex-row md:items-center md:justify-between">
      <div>
        <h1 class="text-3xl font-bold mb-2">
          编程学习群组
        </h1>
        <p class="text-gray-600 dark:text-gray-400">
          加入 <span class="text-blue-600 dark:text-blue-400 font-medium">246</span> 个活跃群组，与志同道合的开发者共同成长
        </p>
      </div>
      <button class="mt-4 md:mt-0 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors flex items-center space-x-2">
        <i class="fa fa-plus" />
        <span>创建群组</span>
      </button>
    </div>

    <!-- 搜索和筛选区 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 mb-8">
      <!-- 关键字搜索 -->
      <div class="mb-6">
        <div class="relative">
          <input
            type="text"
            placeholder="搜索群组名称、描述或主题..."
            class="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          >
          <i class="fa fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        </div>
      </div>

      <!-- 高级筛选区 - 分类、活跃度 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 群组分类 -->
        <div>
          <label class="block text-sm font-medium mb-2">群组分类</label>
          <div class="relative">
            <select class="w-full appearance-none pl-4 pr-10 py-2.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
              <option value="">
                全部分类
              </option>
              <option value="study">
                学习交流
              </option>
              <option value="competition">
                竞赛团队
              </option>
              <option value="language">
                编程语言
              </option>
              <option value="job">
                求职就业
              </option>
              <option value="project">
                项目协作
              </option>
              <option value="beginner">
                新手互助
              </option>
            </select>
            <i class="fa fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
          </div>
        </div>

        <!-- 活跃度筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">活跃度</label>
          <div class="relative">
            <select class="w-full appearance-none pl-4 pr-10 py-2.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
              <option value="">
                全部活跃度
              </option>
              <option value="very-high">
                非常活跃
              </option>
              <option value="high">
                活跃
              </option>
              <option value="medium">
                一般
              </option>
              <option value="low">
                较少
              </option>
            </select>
            <i class="fa fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
          </div>
        </div>
      </div>

      <!-- 热门标签快速筛选 -->
      <div class="mt-6">
        <label class="block text-sm font-medium mb-2">热门主题</label>
        <div class="flex flex-wrap gap-2">
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># C++</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Python</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># 算法竞赛</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># 前端开发</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># 后端开发</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># 机器学习</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># 面试经验</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># 更多</span>
        </div>
      </div>

      <!-- 筛选按钮 -->
      <div class="mt-6 flex justify-end space-x-3">
        <button class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          重置筛选
        </button>
        <button class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          应用筛选
        </button>
      </div>
    </div>

    <!-- 推荐群组 -->
    <div class="mb-10">
      <h2 class="text-2xl font-bold mb-6">
        推荐群组
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 推荐群组1 - 大型群组 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300">
          <div class="relative h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/programming/800/400" alt="编程爱好者交流群封面" class="w-full h-full object-cover">
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
            <div class="absolute bottom-4 left-4 right-4 flex items-end justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-16 h-16 rounded-full border-4 border-white dark:border-gray-800 overflow-hidden">
                  <img src="https://picsum.photos/seed/coding/200/200" alt="编程爱好者交流群头像" class="w-full h-full object-cover">
                </div>
                <div>
                  <h3 class="text-xl font-bold text-white">
                    编程爱好者交流群
                  </h3>
                  <div class="flex items-center text-gray-200 text-sm">
                    <span class="bg-blue-600/90 text-white text-xs px-2 py-0.5 rounded mr-2">学习交流</span>
                    <span><i class="fa fa-users mr-1" /> 12,540 成员</span>
                  </div>
                </div>
              </div>
              <button class="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm transition-colors">
                加入
              </button>
            </div>
          </div>
          <div class="p-4">
            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3">
              面向所有编程爱好者的交流社区，分享学习经验、解决技术难题、探讨行业动态。
            </p>
            <div class="flex justify-between items-center">
              <div class="flex flex-wrap gap-2">
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">多语言</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">技术分享</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">新手友好</span>
              </div>
              <span class="text-sm text-gray-500 dark:text-gray-400"><i class="fa fa-comment mr-1" /> 今日发言: 248</span>
            </div>
          </div>
        </div>

        <!-- 推荐群组2 - 竞赛团队 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300">
          <div class="relative h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/acm/800/400" alt="ACM竞赛集训队封面" class="w-full h-full object-cover">
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
            <div class="absolute bottom-4 left-4 right-4 flex items-end justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-16 h-16 rounded-full border-4 border-white dark:border-gray-800 overflow-hidden">
                  <img src="https://picsum.photos/seed/compete/200/200" alt="ACM竞赛集训队头像" class="w-full h-full object-cover">
                </div>
                <div>
                  <h3 class="text-xl font-bold text-white">
                    ACM竞赛集训队
                  </h3>
                  <div class="flex items-center text-gray-200 text-sm">
                    <span class="bg-red-600/90 text-white text-xs px-2 py-0.5 rounded mr-2">竞赛团队</span>
                    <span><i class="fa fa-users mr-1" /> 328 成员</span>
                  </div>
                </div>
              </div>
              <button class="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm transition-colors">
                申请加入
              </button>
            </div>
          </div>
          <div class="p-4">
            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3">
              专注于ACM/ICPC等程序设计竞赛的训练团队，定期组织模拟赛和题解分享。
            </p>
            <div class="flex justify-between items-center">
              <div class="flex flex-wrap gap-2">
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">算法</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">竞赛</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">C++</span>
              </div>
              <span class="text-sm text-gray-500 dark:text-gray-400"><i class="fa fa-comment mr-1" /> 今日发言: 156</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 活跃群组列表 -->
    <div>
      <h2 class="text-2xl font-bold mb-6">
        活跃群组
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- 活跃群组1 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/python/200/200" alt="Python开发者社区头像" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Python开发者社区</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  加入
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs px-2 py-0.5 rounded mr-2">编程语言</span>
                <span><i class="fa fa-users mr-1" /> 5,721 成员</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                分享Python开发经验、库使用技巧、项目实践和就业信息的专业社区。
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Python</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">数据分析</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">活跃</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 活跃群组2 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/frontend/200/200" alt="前端开发技术圈头像" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">前端开发技术圈</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  加入
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs px-2 py-0.5 rounded mr-2">学习交流</span>
                <span><i class="fa fa-users mr-1" /> 8,342 成员</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                讨论HTML、CSS、JavaScript及各种前端框架的技术社区，分享实践经验。
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">JavaScript</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">React</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">非常活跃</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 活跃群组3 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/interview/200/200" alt="程序员面试经验分享头像" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">程序员面试经验分享</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  加入
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-orange-100 dark:bg-orange-900 text-orange-800 dark:text-orange-200 text-xs px-2 py-0.5 rounded mr-2">求职就业</span>
                <span><i class="fa fa-users mr-1" /> 6,945 成员</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                分享大厂面试经验、算法题解、简历技巧和职场发展的实用信息。
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">面试</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">算法</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">非常活跃</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 活跃群组4 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/beginner/200/200" alt="编程新手互助营头像" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">编程新手互助营</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  加入
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-indigo-100 dark:bg-indigo-900 text-indigo-800 dark:text-indigo-200 text-xs px-2 py-0.5 rounded mr-2">新手互助</span>
                <span><i class="fa fa-users mr-1" /> 4,217 成员</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                为编程初学者提供帮助的友好社区，解答入门疑问，分享学习资源。
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">入门</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">基础</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">活跃</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 活跃群组5 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/ml/200/200" alt="机器学习与AI研究组头像" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">机器学习与AI研究组</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  加入
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-teal-100 dark:bg-teal-900 text-teal-800 dark:text-teal-200 text-xs px-2 py-0.5 rounded mr-2">学习交流</span>
                <span><i class="fa fa-users mr-1" /> 3,872 成员</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                讨论机器学习算法、深度学习框架、AI应用案例和研究前沿的专业社区。
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">AI</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Python</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">活跃</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 活跃群组6 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/project/200/200" alt="开源项目协作组头像" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">开源项目协作组</a>
                </h3>
                <button class="px-3 py-1 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 rounded-lg text-xs transition-colors">
                  申请加入
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 text-xs px-2 py-0.5 rounded mr-2">项目协作</span>
                <span><i class="fa fa-users mr-1" /> 1,254 成员</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                组织开源项目开发，交流协作经验，提升实战能力的开发者社区。
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">开源</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">协作</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">一般</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="mt-10 flex justify-center">
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

/* 解决select下拉箭头在部分浏览器不显示的问题 */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* 群组卡片悬停效果优化 */
.group-card:hover {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}
</style>
