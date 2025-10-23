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
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:6"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <!-- 左侧主内容 -->
      <n-gi span="1 l:6">
        <BannerSkeleton01 v-if="!bannerListData" />
        <EmptyData v-else-if="!bannerListData.length" />
        <!-- 轮播图 -->
        <LatestBanner v-else :banner-list-data="bannerListData" />
      </n-gi>
      <!-- 左侧主内容 -->
      <n-gi span="1 l:4">
        <n-space vertical :size="24">
          <!-- 公告内容 -->
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                最新公告
              </n-h2>
            </template>
            <ListSkeleton01 v-if="!noticeListData" />
            <EmptyData v-else-if="noticeListData.length === 0" />
            <LatestNotice02 v-else :list-data="noticeListData" />
          </n-card>
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                最新题目
              </n-h2>
            </template>
            <template #header-extra>
              <n-button text @click="$router.push({ path: '/problems' })">
                查看全部
              </n-button>
            </template>

            <ProblemSkeleton01 v-if="!problemListData" />
            <EmptyData v-else-if="problemListData.length === 0" />
            <LatestProblem v-else :list-data="problemListData" />
          </n-card>
          <!-- <n-card class="rounded-xl" size="small" content-style="padding: 0;">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                最新题集
              </n-h2>
            </template>
            <template #header-extra>
              <n-button text @click="$router.push({ path: '/sets' })">
                查看全部
              </n-button>
            </template>

            <SetSkeleton01 v-if="!setListData" />
            <EmptyData v-else-if="setListData.length === 0" />
            <LatestSet v-else :set-list-data="setListData" />
          </n-card> -->
        </n-space>
      </n-gi>

      <!-- 右侧边栏 -->
      <n-gi span="1 l:2">
        <n-space vertical :size="24">
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                用户排行榜
              </n-h2>
            </template>
            <template #header-extra>
              <n-button text @click="$router.push({ path: '/ranking' })">
                查看全部
              </n-button>
            </template>
            <ListSkeleton01 v-if="!problemUserRankingListData" />
            <EmptyData v-else-if="problemUserRankingListData.length === 0" />
            <UserSolvedRanking v-else :list-data="problemUserRankingListData" />
          </n-card>
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                热门题目
              </n-h2>
            </template>
            <ListSkeleton02 v-if="!problemRankingListData" />
            <EmptyData v-else-if="problemRankingListData.length === 0" />
            <HotProblem v-else :list-data="problemRankingListData" />
          </n-card>
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                热门题集
              </n-h2>
            </template>
            <ListSkeleton02 v-if="!setRankingListData" />
            <EmptyData v-else-if="setRankingListData.length === 0" />
            <HotSet v-else :list-data="setRankingListData" />
          </n-card>
        </n-space>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>
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
