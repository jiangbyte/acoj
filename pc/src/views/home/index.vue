<script lang="ts" setup>
import { useDataProblemFetch, useDataSetFetch, useSysBannerFetch, useSysNoticeFetch, useUserRankingFetch } from '@/composables/v1'

import { AesCrypto, CleanMarkdown } from '@/utils'

// 列表数据
const bannerListData = ref()
const noticeListData = ref()
const problemListData = ref()
const setListData = ref()
const problemUserRankingListData = ref()
const problemRankingListData = ref()
const setRankingListData = ref()

function openLink(url: string) {
  window.open(url, '_blank')
}

// 获取列表数据
async function loadData() {
  useSysBannerFetch().sysBannerLatest().then(({ data }) => {
    bannerListData.value = data ?? []
  })

  useSysNoticeFetch().sysNoticeLatest().then(({ data }) => {
    noticeListData.value = data ?? []
  })

  // 获取最新题目数据
  useDataProblemFetch().dataProblemLatest().then(({ data }) => {
    problemListData.value = data
  })

  // 获取最新题集数据
  useDataSetFetch().dataSetLatest().then(({ data }) => {
    setListData.value = data
  })

  // 获取Top10排行榜
  useUserRankingFetch().useUserRankingTop().then(({ data }) => {
    problemUserRankingListData.value = data
    console.log(data)
  })

  // 获取Top10排行榜
  useDataProblemFetch().dataProblemHot().then(({ data }) => {
    problemRankingListData.value = data
  })

  // 获取Top10排行榜
  useDataSetFetch().dataSetHot().then(({ data }) => {
    setRankingListData.value = data
    console.log(data)
  })
}

loadData()
</script>

<template>
  <main class="container mx-auto px-4 py-6">
    <!-- 轮播图 -->
    <section class="mb-12 rounded-2xl overflow-hidden shadow-lg">
      <div class="my-section">
        <n-carousel
          autoplay
          show-arrow
          draggable
        >
          <div
            v-for="item in bannerListData"
            :key="item.id"
            class="carousel-item"
          >
            <img
              class="carousel-img"
              :src="item.banner"
            >
            <div class="carousel-caption">
              <div class="caption-title">
                {{ item.title }}
              </div>
              <div class="caption-subtitle">
                {{ item.subtitle }}
              </div>
              <n-button
                v-if="item.buttonText"
                type="primary"
                round
                @click="openLink(item.toUrl || '#')"
              >
                {{ item.buttonText || '了解更多' }}
                <template #icon>
                  <icon-park-outline-hand-right />
                </template>
              </n-button>
            </div>
          </div>
        </n-carousel>
      </div>
    </section>

    <!-- 主要内容区：两列布局 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 左侧主内容 -->
      <div class="lg:col-span-2 space-y-10">
        <!-- 全站公告 -->
        <section>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold">
              全站公告
            </h2>
            <!-- <n-button text @click="$router.push({ path: '/notices' })">
              查看全部
            </n-button> -->
          </div>

          <n-empty
            v-if="!noticeListData || noticeListData.length === 0"
            class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <n-text depth="3" class="text-center max-w-xs">
              暂无数据
            </n-text>
          </n-empty>
          <div v-else class="space-y-4">
            <div
              v-for="item in noticeListData" :key="item.id"
              class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow " @click="$router.push({
                name: 'notice_detail',
                query: { notice: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="md:flex">
                <div class="md:w-1/3 h-32 md:h-auto relative">
                  <img :src="item.cover" :alt="item.title" class="absolute inset-0 w-full h-full object-cover">
                </div>
                <div class="md:w-2/3 p-x-6 pt-6 pb-1">
                  <div class="flex items-center text-sm text-gray-500 dark:text-gray-400 mb-2">
                    <!-- <n-tag size="small" :bordered="false" type="info" class="mr-3">
                      重要
                    </n-tag> -->
                    <n-time :time="item.createTime" />
                    <span class="mx-2">•</span>
                    <n-text>
                      {{ item.createUserName }}
                    </n-text>
                  </div>
                  <n-button text class="mb-3">
                    <h3 class="text-xl font-semibold ">
                      {{ item.title }}
                    </h3>
                  </n-button>
                  <p class="text-gray-600 dark:text-gray-300 mb-4 line-clamp-2">
                    {{ CleanMarkdown(item.content) }}
                  </p>
                  <!-- <n-button text>
                    阅读详情
                  </n-button> -->
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 最新题目 -->
        <section>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold">
              最新题目
            </h2>
            <n-button text @click="$router.push({ path: '/problems' })">
              查看全部
            </n-button>
          </div>

          <n-empty
            v-if="!problemListData || problemListData.length === 0"
            class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <n-text depth="3" class="text-center max-w-xs">
              暂无数据
            </n-text>
          </n-empty>
          <div v-else class="space-y-4">
            <!-- 题目项 -->
            <div
              v-for="item in problemListData" :key="item.id" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 hover:shadow-md transition-shadow" @click="$router.push({
                name: 'problem_submit',
                query: { problemId: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="flex flex-col md:flex-row md:items-center justify-between">
                <div>
                  <div class="flex items-center mb-2">
                    <n-button text class="mr-3">
                      <h3 class="text-xl font-semibold">
                        {{ item.title }}
                      </h3>
                    </n-button>

                    <n-tag class="mr-3" size="small" type="success">
                      {{ item.difficultyName }}
                    </n-tag>
                  </div>
                  <div class="flex mb-3">
                    <n-tag v-for="tagName in item.tagNames" :key="tagName" size="small" class="mr-3">
                      {{ tagName }}
                    </n-tag>
                  </div>
                  <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
                    <n-tag class="mr-4" size="small" type="info">
                      {{ item.categoryName }}
                    </n-tag>
                    <span>通过率: {{ item.acceptance }} %</span>
                  </div>
                </div>
                <div class="mt-4 md:mt-0 flex items-center text-sm text-gray-500 dark:text-gray-400">
                  <span>发布于 <n-time :time="item.createTime" /></span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 最新题集 -->
        <section>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold">
              最新题集
            </h2>
            <n-button text @click="$router.push({ path: '/sets' })">
              查看全部
            </n-button>
          </div>

          <n-empty
            v-if="!setListData || setListData.length === 0"
            class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <n-text depth="3" class="text-center max-w-xs">
              暂无数据
            </n-text>
          </n-empty>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 题集项 -->
            <div
              v-for="item in setListData" :key="item.id" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow" @click="$router.push({
                name: 'proset_detail',
                query: { set: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="h-40 relative">
                <img :src="item.cover" class="absolute inset-0 w-full h-full object-cover">
              </div>
              <div class="p-5">
                <div class="flex items-center mb-2">
                  <n-tag size="small" type="info" class="mr-3">
                    {{ item.setTypeName }}
                  </n-tag>
                  <span class="text-sm text-gray-500 dark:text-gray-400">共 {{ item.problemIds.length ? item.problemIds.length : 0 }} 道题</span>
                </div>
                <n-button text class="mb-2">
                  <h3 class="text-xl font-semibold">
                    {{ item.title }}
                  </h3>
                </n-button>
                <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
                  {{ CleanMarkdown(item.description) }}
                </p>
                <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
                  <span>分类: {{ item.categoryName }}</span>
                  <span>难度: {{ item.difficultyName }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 右侧边栏 -->
      <div class="space-y-8">
        <!-- 用户排行榜 -->
        <section class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-x-5 pt-5 border-b border-gray-100 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              用户排行榜
            </h2>
          </div>
          <n-empty
            v-if="!problemUserRankingListData || problemUserRankingListData.length === 0"
            class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <n-text depth="3" class="text-center max-w-xs">
              暂无数据
            </n-text>
          </n-empty>
          <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 排行榜项 -->
            <div
              v-for="item in problemUserRankingListData" :key="item.rank" class="p-4 flex items-center hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
                name: 'user',
                query: { userId: AesCrypto.encrypt(item.id) },
              })"
            >
              <RankIcon :rank="item.rank" />
              <n-avatar :src="item.avatar" round :size="40" class="mr-3" />
              <div class="flex-1">
                <div class="font-medium">
                  {{ item.nickname }}
                </div>
                <div class="text-xs text-gray-500 dark:text-gray-400">
                  解题: {{ item.score }} 道
                </div>
              </div>
            </div>
          </div>
          <n-button text class="text-center w-full p-4" @click="$router.push({ path: '/ranking' })">
            查看完整排行榜
          </n-button>
        </section>

        <!-- 题目排行榜 -->
        <section class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-x-5 pt-5 border-b border-gray-100 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              热门题目
            </h2>
          </div>
          <n-empty
            v-if="!problemRankingListData || problemRankingListData.length === 0"
            class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <n-text depth="3" class="text-center max-w-xs">
              暂无数据
            </n-text>
          </n-empty>
          <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 题目排行项 -->
            <div
              v-for="item in problemRankingListData" :key="item.rank" class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
                name: 'problem_submit',
                query: { problemId: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="flex items-start">
                <RankIcon :rank="item.rank" />
                <div class="flex-1">
                  <n-button text class="mb-2">
                    <h3 class="font-medium">
                      {{ item.title }}
                    </h3>
                  </n-button>
                  <div class="flex items-center text-xs text-gray-500 dark:text-gray-400">
                    <n-text depth="3">
                      通过率: {{ item.acceptance }} %
                    </n-text>
                    <span class="mx-1">•</span>
                    <n-text depth="3">
                      参与: {{ item.participantUserCount }} 人
                    </n-text>
                    <!-- <span class="mx-1">•</span>
                    <n-text depth="3">
                      提交: {{ item.submitUserCount }}
                    </n-text> -->
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 题集排行榜 -->
        <section class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-x-5 pt-5 border-b border-gray-100 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              热门题集
            </h2>
          </div>
          <n-empty
            v-if="!setRankingListData || setRankingListData.length === 0"
            class="flex flex-col items-center justify-center py-18 bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden"
            description="暂无结果"
          >
            <template #icon>
              <n-icon size="40" class="text-gray-300 dark:text-gray-600">
                <icon-park-outline-info />
              </n-icon>
            </template>
            <n-text depth="3" class="text-center max-w-xs">
              暂无数据
            </n-text>
          </n-empty>
          <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 题集排行项 -->
            <div
              v-for="item in setRankingListData" :key="item.rank" class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
                name: 'proset_detail',
                query: { set: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="flex items-start">
                <RankIcon :rank="item.rank" />
                <div class="flex-1">
                  <n-button text class="mb-2">
                    <h3 class="font-medium">
                      {{ item.title }}
                    </h3>
                  </n-button>
                  <div class="flex items-center text-xs text-gray-500 dark:text-gray-400">
                    <n-text depth="3">
                      通过率: {{ item.avgAcceptance }} %
                    </n-text>
                    <span class="mx-1">•</span>
                    <n-text depth="3">
                      参与: {{ item.participantUserCount }} 人
                    </n-text>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* 基础样式补充，Unocss未覆盖的部分 */
/* 确保平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 链接样式统一 */
a {
  text-decoration: none;
}

/* 图片加载过渡 */
img {
  transition: opacity 0.3s ease-in-out;
}

img.loading {
  opacity: 0.5;
}

img.loaded {
  opacity: 1;
}

/* img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
} */

.my-section {
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
}

.carousel-item {
  position: relative;
  height: 320px;
}

@media (max-width: 768px) {
  .carousel-item {
    height: 250px;
  }

  .carousel-caption {
    padding: 40px 20px;
  }
}

@media (max-width: 480px) {
  .carousel-item {
    height: 250px;
  }

  .carousel-caption {
    padding: 30px 15px;
  }
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 10px 40px 40px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
  z-index: 1;
}

.caption-title {
  font-size: 1.5rem;
  margin: 0;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.caption-subtitle {
  font-size: 1.0rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 4px 0 8px;
  max-width: 600px;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
