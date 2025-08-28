<script lang="ts" setup>
import { useProSetFetch, useSetRankingFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables'
import { NButton, NTag } from 'naive-ui'
import { AesCrypto, CleanMarkdown } from '@/utils'

const categoryOptions = ref()
const difficultyOptions = ref()
const setTypeOptions = ref()
const setRankingListData = ref()

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
const setListData = ref()

async function loadData() {
  const { proSetPage, proSetLatest10 } = useProSetFetch()
  const { sysCategoryOptions } = useSysCategoryFetch()
  const { sysDictOptions } = useSysDictFetch()
  const { setRankingTop } = useSetRankingFetch()

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

  // 获取最新题集数据
  proSetLatest10().then(({ data }) => {
    setListData.value = data
  })

  // 获取Top10排行榜
  setRankingTop().then(({ data }) => {
    setRankingListData.value = data
  })
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

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8">
        <!-- 题集列表 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-6">
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
                {{ CleanMarkdown(item.description) }}
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

        <n-pagination
          v-model:page="pageParam.current"
          v-model:page-size="pageParam.size"
          class="mt-6 flex items-center justify-center"
          show-size-picker
          :page-count="pageData ? Number(pageData.pages) : 0"
          :page-sizes="[10, 20, 30, 50].map(size => ({
            label: `${size} 每页`,
            value: size,
          }))"
          @update:page="loadData"
          @update:page-size="loadData"
        />
      </div>
      <div class="space-y-8">
        <!-- 榜单：最受欢迎 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              热门题集
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 排名1 -->
            <div
              v-for="item in setRankingListData" :key="item.ranking" class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
                name: 'proset_detail',
                query: { set: AesCrypto.encrypt(item.setId) },
              })"
            >
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-yellow-100 dark:bg-yellow-900 flex items-center justify-center text-yellow-600 dark:text-yellow-300 font-bold mr-4">
                  {{ item.ranking }}
                </div>
                <div class="flex-1">
                  <NButton text class="mb-2">
                    <h4 class="font-medium">
                      {{ item.title }}
                    </h4>
                  </NButton>
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
            <div
              v-for="item in setListData" :key="item.id" class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"@click="$router.push({
                name: 'proset_detail',
                query: { set: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center text-green-600 dark:text-green-300 font-bold mr-4">
                  <icon-park-outline-time />
                </div>
                <div class="flex-1">
                  <NButton text class="mb-2">
                    <h4 class="font-medium">
                      {{ item.title }}
                    </h4>
                  </NButton>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    3天前 • 60题
                  </p>
                </div>
                <span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs rounded-full">新</span>
              </div>
            </div>
          </div>
        </div>
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

img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
}
</style>
