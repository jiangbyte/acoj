<script setup lang="ts">
import { useProProblemFetch, useProSetFetch, useSysUserFetch } from '@/composables'
import { AesCrypto, CleanMarkdown } from '@/utils'
import type { DataTableColumns } from 'naive-ui'
import { NSpace, NTag } from 'naive-ui'

const route = useRoute()
const originalId = AesCrypto.decrypt(route.query.userId as string)

const detailData = ref()

const columns: DataTableColumns<any> = [
  {
    title: '题目',
    key: 'title',
    width: 250,
  },
  {
    title: '分类',
    key: 'categoryName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.categoryName)
    },
  },
  {
    title: '标签',
    key: 'tagNames',
    width: 250,
    render: (row) => {
      return h(NSpace, { align: 'center' }, row.tagNames?.map((tag: any) => h(NTag, { key: tag, size: 'small', bordered: false }, tag)) || null)
    },
  },
  {
    title: '难度',
    key: 'difficultyName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.difficultyName)
    },
  },
  {
    title: '通过率',
    key: 'acceptance',
    width: 120,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.acceptance)
    },
  },
  {
    title: '解决',
    key: 'solved',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.solved)
    },
  },
]

const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
  userId: originalId,
})
const setPageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
  userId: originalId,
})
const pageData = ref()
const setPageData = ref()
async function loadData() {
  const { getUserDetail } = useSysUserFetch()
  getUserDetail({ id: originalId }).then(({ data }) => {
    detailData.value = data
  })

  const { proProblemUserRecentSolvedPage } = useProProblemFetch()
  proProblemUserRecentSolvedPage(pageParam.value).then(({ data }) => {
    pageData.value = data
  })

  const { proSetRecentSolvedPage } = useProSetFetch()
  proSetRecentSolvedPage(setPageParam.value).then(({ data }) => {
    setPageData.value = data
  })
}
loadData()
const router = useRouter()

function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      router.push({
        name: 'problem_submit',
        query: { problem: AesCrypto.encrypt(row.id) },
      })
    },
  }
}
</script>

<template>
  <!-- 用户背景Banner -->
  <div class="relative h-64 md:h-80 overflow-hidden">
    <div class="absolute" />
    <img
      :src="detailData?.background"
      class="absolute inset-0 w-full h-full object-cover object-top "
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
        <img :src="detailData?.avatar" class="w-full h-full object-cover">
      </div>

      <!-- 用户基本信息 -->
      <div class="flex-grow">
        <div class="flex flex-wrap items-start justify-between gap-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 w-full">
          <div>
            <h1 class="text-2xl md:text-3xl font-bold mb-4">
              {{ detailData?.nickname }}
            </h1>
            <!-- <p class="text-gray-600 dark:text-gray-300 mb-3">
              算法爱好者 | 后端开发工程师
            </p> -->

            <div class="flex flex-wrap items-center gap-3 mb-4">
              <div class="flex items-center text-sm">
                <i class="fa fa-envelope text-gray-500 mr-1.5" />
                <span>{{ detailData?.email }}</span>
              </div>
              <div class="flex items-center text-sm">
                <i class="fa fa-calendar text-gray-500 mr-1.5" />
                <span>注册于 <n-time :time="detailData?.createTime" /></span>
              </div>
            </div>

            <p class="text-gray-600 dark:text-gray-300 w-full">
              {{ detailData?.quote }}
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
          {{ detailData?.solvedProblem ? detailData?.solvedProblem : 0 }}
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          已解决题目
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
        <div class="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-1">
          {{ detailData?.participatedSet ? detailData?.participatedSet : 0 }}
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          参与题集
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
        <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
          {{ detailData?.activeScore ? detailData?.activeScore : 0 }}
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400">
          活跃指数
        </div>
      </div>
    </div>

    <!-- 内容导航 -->
    <!-- <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm mb-8">
      <div class="flex overflow-x-auto md:overflow-visible border-b border-gray-200 dark:border-gray-700">
        Tabs -->
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
    <!-- </div>
    </div> -->

    <!-- 最近解题记录 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
        <h2 class="text-lg font-bold">
          最近解题记录
        </h2>
        <!-- <a href="#" class="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
          查看全部 <i class="fa fa-angle-right ml-1" />
        </a> -->
      </div>

      <div class="overflow-x-auto">
        <div class="divide-y divide-gray-100 dark:divide-gray-700">
          <n-data-table
            :columns="columns"
            :data="pageData?.records"
            :bordered="false"
            :row-key="(row: any) => row.userId"
            class="flex-1 h-full"
            :row-props="rowProps"
          />
        </div>
        <n-pagination
          v-model:page="pageParam.current"
          v-model:page-size="pageParam.size"
          show-size-picker
          :page-count="pageData ? Number(pageData.pages) : 0"
          :page-sizes="Array.from({ length: 10 }, (_, i) => ({
            label: `${(i + 1) * 10} 每页`,
            value: (i + 1) * 10,
          }))"
          class="flex justify-center items-center p-6"
          @update:page="loadData"
          @update:page-size="loadData"
        />
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
          <!-- <a href="#" class="text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors">
            查看全部 <i class="fa fa-angle-right ml-1" />
          </a> -->
        </div>

        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <!-- 题集1 -->
          <div
            v-for="item in setPageData?.records" :key="item.id" class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors" @click="$router.push({
              name: 'proset_detail',
              query: { set: AesCrypto.encrypt(item.id) },
            })"
          >
            <div class="flex gap-4">
              <div class="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0">
                <img :src="item.cover" class="w-full h-full object-cover">
              </div>
              <div class="flex-grow">
                <NButton text class=" mb-2">
                  <h3 class="text-xl font-semibold ">
                    {{ item.title }}
                  </h3>
                </NButton>
                <div class="flex flex-wrap items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mb-2">
                  <span>32 题</span>
                  <span>•</span>
                  <span>4.8 分</span>
                  <span>•</span>
                  <span>2,356 人学习</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-1">
                  {{ CleanMarkdown(item.description) }}
                </p>
              </div>
            </div>
          </div>

          <n-pagination
            v-model:page="setPageParam.current"
            v-model:page-size="setPageParam.size"
            class="mt-6 mb-6 flex items-center justify-center"
            show-size-picker
            :page-count="setPageData ? Number(setPageData.pages) : 0"
            :page-sizes="[10, 20, 30, 50].map(size => ({
              label: `${size} 每页`,
              value: size,
            }))"
            @update:page="loadData"
            @update:page-size="loadData"
          />
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
