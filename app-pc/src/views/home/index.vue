<script lang="ts" setup>
import { useDataProblemFetch, useDataSetFetch, useSysBannerFetch, useSysNoticeFetch, useUserRankingFetch } from '@/composables/v1'

// Implementation note.
const bannerListData = ref()
const noticeListData = ref()
const problemListData = ref()
const problemUserRankingListData = ref()
const problemRankingListData = ref()
const setRankingListData = ref()

// Implementation note.
async function loadData() {
  useSysBannerFetch().sysBannerLatest().then(({ data }) => {
    bannerListData.value = data ?? []
  })

  useSysNoticeFetch().sysNoticeLists().then(({ data }) => {
    noticeListData.value = data ?? []
  })

  // Implementation note.
  useDataProblemFetch().dataProblemLatest().then(({ data }) => {
    problemListData.value = data
  })

  // Implementation note.
  useUserRankingFetch().useUserRankingTop().then(({ data }) => {
    problemUserRankingListData.value = data
    console.log(data)
  })

  // Implementation note.
  useDataProblemFetch().dataProblemHot().then(({ data }) => {
    problemRankingListData.value = data
  })

  // Implementation note.
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
      <!-- Section -->
      <n-gi span="1 l:6">
        <BannerSkeleton01 v-if="!bannerListData" />
        <EmptyData v-else-if="!bannerListData.length" />
        <LatestBanner v-else :banner-list-data="bannerListData" />
      </n-gi>
      <!-- Section -->
      <n-gi span="1 l:4">
        <n-space vertical :size="24">
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Announcements
              </n-h2>
            </template>
            <ListSkeleton01 v-if="!noticeListData" />
            <EmptyData v-else-if="noticeListData.length === 0" />
            <LatestNotice02 v-else :list-data="noticeListData" />
          </n-card>
          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Latest Problems
              </n-h2>
            </template>
            <template #header-extra>
              <n-button text @click="$router.push({ path: '/problems' })">
                View All
              </n-button>
            </template>

            <ProblemSkeleton01 v-if="!problemListData" />
            <EmptyData v-else-if="problemListData.length === 0" />
            <LatestProblem v-else :list-data="problemListData" />
          </n-card>
        </n-space>
      </n-gi>

      <!-- Section -->
      <n-gi span="1 l:2">
        <n-space vertical :size="24">
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                User Rankings
              </n-h2>
            </template>
            <template #header-extra>
              <n-button text @click="$router.push({ path: '/ranking' })">
                View All
              </n-button>
            </template>
            <ListSkeleton01 v-if="!problemUserRankingListData" />
            <EmptyData v-else-if="problemUserRankingListData.length === 0" />
            <UserSolvedRanking v-else :list-data="problemUserRankingListData" />
          </n-card>
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Popular Problems
              </n-h2>
            </template>
            <ListSkeleton02 v-if="!problemRankingListData" />
            <EmptyData v-else-if="problemRankingListData.length === 0" />
            <HotProblem v-else :list-data="problemRankingListData" />
          </n-card>
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Popular Problem Sets
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

/* Component styles. */
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
