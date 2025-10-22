<script lang="ts" setup>
import { useDataProblemFetch, useDataSetFetch, useSysBannerFetch, useSysNoticeFetch, useUserRankingFetch } from '@/composables/v1'

// 列表数据
const bannerListData = ref()
const noticeListData = ref()
const problemListData = ref()
const setListData = ref()
const problemUserRankingListData = ref()
const problemRankingListData = ref()
const setRankingListData = ref()

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
    <BannerSkeleton01 v-if="!bannerListData" />
    <EmptyData v-else-if="!bannerListData.length" />
    <!-- 轮播图 -->
    <LatestBanner v-else :banner-list-data="bannerListData" />

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
            <n-button text>
              查看全部
            </n-button>
          </div>
          <NoticeSkeleton01 v-if="!noticeListData" />
          <EmptyData v-else-if="noticeListData.length === 0" />
          <LatestNotice v-else :notice-list-data="noticeListData" />
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

          <ProblemSkeleton01 v-if="!problemListData" />
          <EmptyData v-else-if="problemListData.length === 0" />
          <LatestProblem v-else :problem-list-data="problemListData" />
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

          <SetSkeleton01 v-if="!setListData" />
          <EmptyData v-else-if="setListData.length === 0" />
          <LatestSet v-else :set-list-data="setListData" />
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
          <ListSkeleton01 v-if="!problemUserRankingListData" />
          <EmptyData v-else-if="problemUserRankingListData.length === 0" />
          <UserSolvedRanking v-else :list-data="problemUserRankingListData" />
        </section>

        <!-- 题目排行榜 -->
        <section class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-x-5 pt-5 border-b border-gray-100 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              热门题目
            </h2>
          </div>

          <ListSkeleton02 v-if="!problemRankingListData" />
          <EmptyData v-else-if="problemRankingListData.length === 0" />
          <HotProblem v-else :list-data="problemRankingListData" />
        </section>

        <!-- 题集排行榜 -->
        <section class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-x-5 pt-5 border-b border-gray-100 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              热门题集
            </h2>
          </div>

          <ListSkeleton02 v-if="!setRankingListData" />
          <EmptyData v-else-if="setRankingListData.length === 0" />
          <HotSet v-else :list-data="setRankingListData" />
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
