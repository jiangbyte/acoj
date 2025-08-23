<script lang="ts" setup>
import { useProSetFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables'
import { NButton, NTag } from 'naive-ui'
import { AesCrypto } from '@/utils'

const { proSetPage } = useProSetFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysDictOptions } = useSysDictFetch()

const categoryOptions = ref()
const difficultyOptions = ref()
const setTypeOptions = ref()

const pageParam = ref({
  current: 1,
  size: 10,
  sortField: null,
  sortOrder: null,
  keyword: '',
  categoryId: null,
  difficulty: null,
  setType: null,
})

const pageData = ref()
const loading = ref(true)

async function loadData() {
  loading.value = true
  const { data } = await proSetPage(pageParam.value)
  if (data) {
    pageData.value = data
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  // 获取难度下拉列表数据
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    difficultyOptions.value = difficultyData.map((item: any) => ({
      value: Number(item.value),
      label: item.label,
    }))
  }

  // 获取题集类型下拉列表数据
  const { data: setTypeData } = await sysDictOptions({ dictType: 'PROBLEM_SET_TYPE' })
  if (setTypeData) {
    setTypeOptions.value = setTypeData.map((item: any) => ({
      value: Number(item.value),
      label: item.label,
    }))
  }

  loading.value = false
}

loadData()

function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const router = useRouter()

function handleClick(item: any) {
  router.push({
    name: 'proset_detail',
    query: { set: AesCrypto.encrypt(item.id) },
  })
}
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- 页面标题和统计信息 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">
        题集
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        共收录 <span class="text-blue-600 dark:text-blue-400 font-medium">
          {{ pageData?.total }}
        </span> 个题集，覆盖各类学习路径和知识点体系
      </p>
    </div>

    <!-- 新增：学习路径推荐 -->
    <!-- <section class="mb-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold">
          推荐学习路径
        </h2>
        <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline text-sm font-medium flex items-center">
          查看全部 <i class="fa fa-arrow-right ml-1" />
        </a>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300">
          <div class="p-6">
            <div class="flex items-center mb-4">
              <div class="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center mr-4">
                <i class="fa fa-code text-blue-600 dark:text-blue-300 text-xl" />
              </div>
              <div>
                <h3 class="text-xl font-semibold">
                  算法工程师之路
                </h3>
                <p class="text-gray-500 dark:text-gray-400 text-sm">
                  6个题集 • 6个月学习周期
                </p>
              </div>
            </div>

            <div class="relative pl-6 pb-2 mb-4">
              <div class="absolute left-2 top-0 bottom-0 w-0.5 bg-gray-200 dark:bg-gray-700" />

              <div class="relative mb-4">
                <div class="absolute left-[-26px] top-1 w-5 h-5 rounded-full bg-blue-600 border-4 border-white dark:border-gray-800" />
                <div class="bg-gray-50 dark:bg-gray-700 p-3 rounded-lg">
                  <p class="font-medium">
                    1. 数据结构基础
                  </p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    30题 • 入门级
                  </p>
                </div>
              </div>

              <div class="relative mb-4">
                <div class="absolute left-[-26px] top-1 w-5 h-5 rounded-full bg-blue-600 border-4 border-white dark:border-gray-800" />
                <div class="bg-gray-50 dark:bg-gray-700 p-3 rounded-lg">
                  <p class="font-medium">
                    2. 算法入门
                  </p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    50题 • 进阶级
                  </p>
                </div>
              </div>

              <div class="relative">
                <div class="absolute left-[-26px] top-1 w-5 h-5 rounded-full bg-gray-300 dark:bg-gray-600 border-4 border-white dark:border-gray-800" />
                <div class="bg-gray-50 dark:bg-gray-700 p-3 rounded-lg opacity-70">
                  <p class="font-medium">
                    3. 高级算法应用
                  </p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    70题 • 挑战级
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

   
      </div>
    </section> -->

    <!-- 搜索和筛选区 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 mb-8">
      <!-- 关键字搜索 -->
      <div class="mb-6">
        <n-input
          v-model:value="pageParam.keyword"
          placeholder="搜索题集"
          clearable
          @keyup.enter="loadData"
          @clear="resetHandle"
        />
      </div>

      <!-- 高级筛选区 - 分类、难度 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 分类筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">题集分类</label>
          <n-select
            v-model:value="pageParam.categoryId"
            placeholder="选择分类"
            clearable
            :options="categoryOptions"
            @clear="resetHandle"
          />
        </div>

        <!-- 难度筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">难度范围</label>
          <n-select
            v-model:value="pageParam.difficulty"
            placeholder="选择难度"
            clearable
            :options="difficultyOptions"
            @clear="resetHandle"
          />
        </div>
      </div>

      <!-- 筛选按钮 -->
      <div class="mt-6 flex justify-end space-x-3">
        <NButton type="warning" @click="resetHandle">
          重置筛选
        </NButton>
        <NButton type="primary" @click="loadData">
          应用筛选
        </NButton>
      </div>
    </div>

    <!-- 题集列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- 题集项 1 -->
      <div
        v-for="item in pageData?.records" :key="item.id"
        class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1"
        @click="$router.push({
          name: 'proset_detail',
          query: { set: AesCrypto.encrypt(item.id) },
        })"
      >
        <div class="relative h-48 overflow-hidden">
          <img :src="item.cover" class="w-full h-full object-cover transition-transform duration-500 hover:scale-110">

          <NTag size="small" :bordered="false" type="info" class="absolute top-3 left-3 text-xs px-2 py-1">
            {{ item.setTypeName }}
          </NTag>

          <NTag size="small" :bordered="false" type="warning" class="absolute top-3 right-3  text-xs px-2 py-1">
            {{ item.categoryName }}
          </NTag>
        </div>
        <div class="p-5">
          <NButton text class=" mb-2">
            <h3 class="text-xl font-semibold ">
              {{ item.title }}
            </h3>
          </NButton>

          <p class="text-gray-600 dark:text-gray-300 text-sm mb-4 line-clamp-2">
            {{ item.description }}
          </p>
          <!--          <div class="flex flex-wrap gap-2 mb-4"> -->
          <!--            <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-1 rounded">排序</span> -->
          <!--            <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-1 rounded">查找</span> -->
          <!--            <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-1 rounded">贪心</span> -->
          <!--          </div> -->
          <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
            <span><i class="fa fa-question-circle mr-1" /> 100题</span>
            <span><i class="fa fa-user mr-1" /> 12,540人学习</span>
            <span><i class="fa fa-star mr-1" /> 4.8</span>
          </div>
        </div>
      </div>
    </div>

    <n-flex justify="center" class="mt-6">
      <n-pagination
        v-model:page="pageParam.current"
        v-model:page-size="pageParam.size"
        show-size-picker
        :page-count="pageData ? Number(pageData.pages) : 0"
        :page-sizes="[10, 20, 30, 50].map(size => ({
          label: `${size} 每页`,
          value: size,
        }))"
        @update:page="loadData"
        @update:page-size="loadData"
      />
    </n-flex>

    <!-- 新增：热门学习榜单 -->
    <section>
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold">
          热门学习榜单
        </h2>
        <div class="flex space-x-2">
          <button class="px-3 py-1 bg-blue-600 text-white rounded-lg text-sm font-medium">
            本周
          </button>
          <button class="px-3 py-1 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
            本月
          </button>
          <button class="px-3 py-1 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors">
            全年
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 榜单：最受欢迎 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              最受欢迎
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 排名1 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-yellow-100 dark:bg-yellow-900 flex items-center justify-center text-yellow-600 dark:text-yellow-300 font-bold mr-4">
                  1
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">算法入门必刷100题</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    12,540人学习
                  </p>
                </div>
                <div class="flex items-center text-yellow-500">
                  <i class="fa fa-star mr-1" />
                  <span>4.8</span>
                </div>
              </div>
            </div>

            <!-- 排名2 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-gray-600 dark:text-gray-300 font-bold mr-4">
                  2
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">编程零基础入门</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    15,720人学习
                  </p>
                </div>
                <div class="flex items-center text-yellow-500">
                  <i class="fa fa-star mr-1" />
                  <span>4.6</span>
                </div>
              </div>
            </div>

            <!-- 排名3 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-orange-100 dark:bg-orange-900 flex items-center justify-center text-orange-600 dark:text-orange-300 font-bold mr-4">
                  3
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">大厂面试高频50题</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    8,750人学习
                  </p>
                </div>
                <div class="flex items-center text-yellow-500">
                  <i class="fa fa-star mr-1" />
                  <span>4.9</span>
                </div>
              </div>
            </div>

            <!-- 排名4-5 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-gray-600 dark:text-gray-300 font-bold mr-4">
                  4
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">数据结构实战指南</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    6,320人学习
                  </p>
                </div>
                <div class="flex items-center text-yellow-500">
                  <i class="fa fa-star mr-1" />
                  <span>4.7</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 榜单：难度挑战 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              难度挑战
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 排名1 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-yellow-100 dark:bg-yellow-900 flex items-center justify-center text-yellow-600 dark:text-yellow-300 font-bold mr-4">
                  1
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">ACM竞赛金牌之路</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    大师级 • 120题
                  </p>
                </div>
                <div class="flex items-center text-red-500">
                  <i class="fa fa-trophy mr-1" />
                  <span>1,850</span>
                </div>
              </div>
            </div>

            <!-- 排名2 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-gray-600 dark:text-gray-300 font-bold mr-4">
                  2
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">动态规划高级应用</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    挑战级 • 40题
                  </p>
                </div>
                <div class="flex items-center text-red-500">
                  <i class="fa fa-trophy mr-1" />
                  <span>3,280</span>
                </div>
              </div>
            </div>

            <!-- 排名3 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-orange-100 dark:bg-orange-900 flex items-center justify-center text-orange-600 dark:text-orange-300 font-bold mr-4">
                  3
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">数据结构实战指南</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    进阶级 • 75题
                  </p>
                </div>
                <div class="flex items-center text-red-500">
                  <i class="fa fa-trophy mr-1" />
                  <span>6,320</span>
                </div>
              </div>
            </div>

            <!-- 排名4 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-gray-600 dark:text-gray-300 font-bold mr-4">
                  4
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">大厂面试高频50题</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    进阶级 • 50题
                  </p>
                </div>
                <div class="flex items-center text-red-500">
                  <i class="fa fa-trophy mr-1" />
                  <span>8,750</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 榜单：最新上线 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              最新上线
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 最新1 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center text-green-600 dark:text-green-300 font-bold mr-4">
                  <i class="fa fa-clock-o" />
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">机器学习算法基础</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    3天前 • 60题
                  </p>
                </div>
                <span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs rounded-full">新</span>
              </div>
            </div>

            <!-- 最新2 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center text-green-600 dark:text-green-300 font-bold mr-4">
                  <i class="fa fa-clock-o" />
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">Python高级编程技巧</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    1周前 • 45题
                  </p>
                </div>
                <span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs rounded-full">新</span>
              </div>
            </div>

            <!-- 最新3 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center text-green-600 dark:text-green-300 font-bold mr-4">
                  <i class="fa fa-clock-o" />
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">系统设计基础</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    2周前 • 30题
                  </p>
                </div>
                <span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs rounded-full">新</span>
              </div>
            </div>

            <!-- 最新4 -->
            <div class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center text-green-600 dark:text-green-300 font-bold mr-4">
                  <i class="fa fa-clock-o" />
                </div>
                <div class="flex-1">
                  <h4 class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <a href="#">前端算法与数据结构</a>
                  </h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    3周前 • 50题
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
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

/* 图片加载效果 */
img {
  opacity: 1;
  transition: opacity 0.3s ease-in-out;
}

img.loading {
  opacity: 0.5;
}
</style>
