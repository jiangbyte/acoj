<script setup lang="ts">
import { useSysUserFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import { CalendarHeatmap } from 'vue3-calendar-heatmap'

defineOptions({
  inheritAttrs: false,
})

const endDate = ref(new Date())

const route = useRoute()
const originalId = AesCrypto.decrypt(route.query.userId as string)

const detailData = ref()

const setPageData = ref()
async function loadData() {
  const { sysUserDetailClient } = useSysUserFetch()
  sysUserDetailClient({ id: originalId }).then(({ data }) => {
    detailData.value = data
  })

  // const { proProblemUserRecentSolvedPage } = useProProblemFetch()
  // proProblemUserRecentSolvedPage(pageParam.value).then(({ data }) => {
  //   pageData.value = data
  // })

  // const { proSetRecentSolvedPage } = useProSetFetch()
  // proSetRecentSolvedPage(setPageParam.value).then(({ data }) => {
  //   setPageData.value = data
  // })
  setPageData.value = {
    records: [],
  }
}
loadData()
</script>

<template>
  <!-- 用户背景Banner -->
  <!-- <div class="relative h-64 md:h-80 overflow-hidden">
    <div class="absolute" />
    <img
      :src="detailData?.background"
      class="absolute inset-0 w-full h-full object-cover object-top"
    >
  </div> -->
  <!-- 全屏背景Banner -->
  <div class="absolute  inset-0 -z-10">
    <div class="absolute inset-0 bg-gradient-to-b from-black/25 via-black/10 via-30% to-#f5f6f7 to-75% dark:from-black/60 dark:to-gray-900 z-10" />
    <!-- 背景图片 -->
    <img
      :src="detailData?.background"
      class="absolute inset-0 w-full h-full object-cover object-top z-0"
    >
  </div>
  <!-- 页面内容 -->
  <div class="relative z-20">
    <!-- 用户背景Banner占位区域 -->
    <div class="h-70 md:h-70" />

    <!-- 用户信息区域 -->
    <div class="container mx-auto px-4 -mt-20 relative z-10">
      <div class="flex flex-col md:flex-row gap-6 items-start md:items-end mb-8">
        <!-- 用户头像 -->
        <div class="w-32 h-32 md:w-40 md:h-40 rounded-xl overflow-hidden border-4 border-white dark:border-gray-800 shadow-lg">
          <img :src="detailData?.avatar" class="w-32 h-32 md:w-40 md:h-40 object-cover">
        </div>

        <!-- 用户基本信息 -->
        <div class="flex-grow w-full">
          <div class="flex flex-wrap items-start justify-between gap-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 w-full">
            <div>
              <h1 class="text-2xl md:text-3xl font-bold mb-4">
                {{ detailData?.nickname ? detailData?.nickname : '暂无昵称' }}
              </h1>
              <!-- <p class="text-gray-600 dark:text-gray-300 mb-3">
              算法爱好者 | 后端开发工程师
            </p> -->

              <div class="flex flex-wrap items-center gap-3 mb-4">
                <div class="flex items-center text-sm">
                  <icon-park-outline-every-user class="mr-1.5" />
                  <span>{{ detailData?.groupIdName ? detailData?.groupIdName : '暂无用户组' }}</span>
                </div>
                <div class="flex items-center text-sm">
                  <icon-park-outline-accept-email class="mr-1.5" />
                  <span>{{ detailData?.email ? detailData?.email : '暂无邮箱' }}</span>
                </div>
                <div class="flex items-center text-sm">
                  <icon-park-outline-calendar class="mr-1.5" />
                  <span>注册于 <n-time :time="detailData?.createTime" /></span>
                </div>
              </div>

              <p class="text-gray-600 dark:text-gray-300 w-full">
                {{ detailData?.quote ? detailData?.quote : '暂无座右铭' }}
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
        <!-- <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 text-center">
          <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-1">
            {{ detailData?.tryProblem ? detailData?.tryProblem : 0 }}
          </div>
          <div class="text-sm text-gray-500 dark:text-gray-400">
            总尝试题目
          </div>
        </div> -->
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
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h2 class="text-lg font-bold">
            AC 记录
          </h2>
        </div>
        <div class="overflow-scroll">
          <CalendarHeatmap
            :values="detailData?.acRecord ? detailData?.acRecord : []"
            tooltip-unit="AC"
            :end-date="endDate"
            :range-color="['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39']"
            class="custom-heatmap p-4 min-w-1000px"
          />
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

    <!-- <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
        <h2 class="text-lg font-bold">
          最近解题记录
        </h2>
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

    <div class="grid grid-cols-1 lg:grid-cols-1 gap-8 mb-8">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h2 class="text-lg font-bold">
            最近题集记录
          </h2>
        </div>

        <n-empty
          v-if="setPageData?.records.length === 0"
          class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
          description="无数据"
        >
          <template #icon>
            <n-icon size="40" class="text-gray-300 dark:text-gray-600">
              <icon-park-outline-info />
            </n-icon>
          </template>
          <n-text depth="3" class="text-center max-w-xs">
            无数据
          </n-text>
        </n-empty>

        <div v-if="setPageData?.records.length !== 0" class="divide-y divide-gray-200 dark:divide-gray-700">
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
    </div> -->
    </div>
  </div>
</template>

<style scoped>
.custom-heatmap {
  /* font-size: 9px; */
}

/* 使用深度选择器修改子组件样式 */
:deep(.custom-heatmap .vch__day__label) {
  font-size: 8px;
  font-family: 'Your Font', sans-serif;
}
:deep(.custom-heatmap .vch__month__label) {
  font-size: 8px;
  font-family: 'Your Font', sans-serif;
}

/* 图片悬停效果 */
img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
}
</style>
