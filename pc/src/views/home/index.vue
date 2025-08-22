<script lang="ts" setup>
import {useSysBannerFetch, useSysNoticeFetch, useProProblemFetch, useProSetFetch, useProblemUserRankingFetch, useProblemRankingFetch, useSetRankingFetch} from '@/composables'

import {AesCrypto} from '@/utils'

// 列表数据
const bannerListData = ref()
const noticeListData = ref()
const problemListData = ref()
const setListData = ref()
const problemUserRankingListData = ref()
const problemRankingListData = ref()
const setRankingListData = ref()
const noticeLoading = ref(true)

const router = useRouter()

function goNotice(id: string) {
  router.push({
    name: 'notice_detail',
    query: {notice: AesCrypto.encrypt(id)},
  })
}

function openLink(url: string) {
  window.open(url, '_blank')
}

function goProblem(id: string) {
  router.push({
    name: 'problem_submit',
    query: {problem: AesCrypto.encrypt(id)},
  })
}

function goSet(id: string) {
  router.push({
    name: 'proset_detail',
    query: {set: AesCrypto.encrypt(id)},
  })
}

// 获取列表数据
async function loadData() {
  const {sysBannerLatest10} = useSysBannerFetch()
  const {sysNoticeLatest10} = useSysNoticeFetch()
  const {proProblemLatest10} = useProProblemFetch()
  const {proSetLatest10} = useProSetFetch()
  const {problemUserRankingTop} = useProblemUserRankingFetch()
  const {problemRankingTop} = useProblemRankingFetch()
  const {setRankingTop} = useSetRankingFetch()

  // 获取横幅数据
  sysBannerLatest10().then(({data}) => {
    bannerListData.value = data
    if (data) {
      bannerListData.value = data
    } else {
      bannerListData.value = []
    }
  })

  // 获取最新公告数据
  noticeLoading.value = true
  sysNoticeLatest10().then(({data}) => {
    noticeListData.value = data
    noticeListData.value = noticeListData.value.sort((a: any, b: any) => (a.sort || 0) - (b.sort || 0))
    noticeLoading.value = false
  })

  // 获取最新题目数据
  proProblemLatest10().then(({data}) => {
    problemListData.value = data
  })

  // 获取最新题集数据
  proSetLatest10().then(({data}) => {
    setListData.value = data
  })

  // 获取Top10排行榜
  problemUserRankingTop().then(({data}) => {
    problemUserRankingListData.value = data
  })

  // 获取Top10排行榜
  problemRankingTop().then(({data}) => {
    problemRankingListData.value = data
  })

  // 获取Top10排行榜
  setRankingTop().then(({data}) => {
    setRankingListData.value = data
  })
}

loadData()
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-alert title="全站通知" type="warning" closable>
      <template #icon>
        <n-icon>
          <icon-park-outline-volume-notice/>
        </n-icon>
      </template>
      <n-flex vertical :size="0">
        <div>
          近期系统升级中，请稍后访问。
        </div>
        <n-text :depth="3">
          发布于
        </n-text>
        <!--        <n-time />-->
      </n-flex>
    </n-alert>

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
                <icon-park-outline-hand-right/>
              </template>
            </n-button>
          </div>
        </div>
      </n-carousel>
    </div>

    <n-grid
        cols="12 m:24"
        responsive="screen"
        :x-gap="16"
        :y-gap="16"
    >
      <n-gi span="12" class="flex flex-col gap-4">
        <n-list hoverable>
          <template #header>
            <n-flex align="center" :size="8">
              <icon-park-outline-book/>
              <div>
                最新公告
              </div>
            </n-flex>
          </template>
          <n-list-item
              v-for="item in noticeListData"
              :key="item.id"
              @click="goNotice(item.id)"
          >
            <n-flex align="center" :wrap="false">
              <n-flex align="center">
                <n-image
                    width="48"
                    height="48"
                    object-fit="cover"
                    :src="item.cover"
                    fallback-src="#"
                    class="rounded-lg"
                    preview-disabled
                />
              </n-flex>
              <n-flex align="center">
                <div class="flex flex-col">
                  <h4>
                    <n-ellipsis line-clamp="1">
                      {{ item.title }}
                    </n-ellipsis>
                  </h4>
                  <!--                  <n-ellipsis line-clamp="1" :tooltip="false">-->
                  <!--                    {{ item.content }}-->
                  <!--                  </n-ellipsis>-->
                  <n-space align="center" justify="space-between" class="mt-2">
                    <n-tag size="small" type="info" round>
                      最新
                    </n-tag>
                    <n-space align="center">
                      <n-icon class="mt-1.2">
                        <icon-park-outline-time/>
                      </n-icon>
                      <n-time :time="item.createTime"/>
                    </n-space>
                  </n-space>
                </div>
              </n-flex>
            </n-flex>
          </n-list-item>
        </n-list>
      </n-gi>

      <n-gi span="12" class="flex flex-col gap-4">
        <n-tabs type="line" animated class="bg-white">
          <n-tab-pane name="1" tab="用户排行榜">
            <n-list hoverable>
              <n-list-item
                  v-for="item in problemUserRankingListData"
                  :key="item.id"
              >
                <n-flex align="center" justify="space-between">
                  <n-flex vertical>
                    <n-flex align="center">
                      <n-tag type="info" size="small">
                        {{ item.ranking }}
                      </n-tag>
                      <n-space align="center" size="small">
                        <n-avatar round :src="item.avatar"/>
                        <n-text>
                          {{ item.nickname }}
                        </n-text>
                      </n-space>
                    </n-flex>
                  </n-flex>

                  <n-flex align="center">
                    <n-tag type="info" size="small" class="flex items-center gap-4">
                      已解决 {{ item.solvedCount }}
                    </n-tag>
                  </n-flex>
                </n-flex>

              </n-list-item>
            </n-list>
          </n-tab-pane>
          <n-tab-pane name="2" tab="题目排行榜">
            <n-list hoverable>
              <n-list-item
                  v-for="item in problemRankingListData"
                  :key="item.id"
              >
                <n-flex align="center" justify="space-between">
                  <n-flex vertical>
                    <n-flex align="center">
                      <n-tag type="info" size="small">
                        {{ item.ranking }}
                      </n-tag>
                      <n-ellipsis line-clamp="1" class="text-4">
                        {{ item.title }}
                      </n-ellipsis>
                    </n-flex>
                  </n-flex>

                  <n-flex align="center">
                    <n-tag type="info" size="small" class="flex items-center gap-4">
                      参与人数 {{ item.participantCount }}
                    </n-tag>
                  </n-flex>
                </n-flex>

              </n-list-item>
            </n-list>
          </n-tab-pane>
          <n-tab-pane name="3" tab="题集排行榜">
            <n-list hoverable>
              <n-list-item
                  v-for="item in setRankingListData"
                  :key="item.id"
              >
                <n-flex align="center" justify="space-between">
                  <n-flex vertical>
                    <n-flex align="center">
                      <n-tag type="info" size="small">
                        {{ item.ranking }}
                      </n-tag>
                      <n-ellipsis line-clamp="1" class="text-4">
                        {{ item.title }}
                      </n-ellipsis>
                    </n-flex>
                  </n-flex>

                  <n-flex align="center">
                    <n-tag type="info" size="small" class="flex items-center gap-4">
                      参与人数 {{ item.participantCount }}
                    </n-tag>
                  </n-flex>
                </n-flex>

              </n-list-item>
            </n-list>
          </n-tab-pane>
        </n-tabs>

      </n-gi>

      <n-gi span="12" class="flex flex-col gap-4">
        <n-list hoverable>
          <template #header>
            <n-flex align="center" :size="8">
              <icon-park-outline-book/>
              <div>
                最新题目
              </div>
            </n-flex>
          </template>
          <n-list-item
              v-for="item in problemListData"
              :key="item.id"
              @click="goProblem(item.id)"
          >
            <n-flex align="center" justify="space-between">
              <n-flex vertical class="w-full">
                <n-flex align="center" justify="space-between" class="w-full">
                  <n-flex align="center">
                    <n-tag type="info" size="small">
                      {{ item.categoryName }}
                    </n-tag>
                    <n-ellipsis line-clamp="1" class="text-4">
                      {{ item.title }}
                    </n-ellipsis>
                  </n-flex>

                  <n-tag type="info" size="small">
                    {{ item.difficultyName }}
                  </n-tag>
                </n-flex>
                <n-ellipsis line-clamp="1" :tooltip="false">
                  {{ item.description }}
                </n-ellipsis>
                <n-flex align="center">
                  <n-flex align="center" :size="4" v-if="item.currentUserSolved">
                    <icon-park-outline-check-one/>
                    <div>
                      已解决
                    </div>
                  </n-flex>
                  <n-flex align="center" :size="4" v-if="!item.currentUserSolved">
                    <icon-park-outline-close-one/>
                    <div>
                      未解决
                    </div>
                  </n-flex>
                  <n-flex align="center" :size="4">
                    <icon-park-outline-speed-one/>
                    <div>
                      通过率 {{ item.passRate ? item.passRate : 0 }}
                    </div>
                  </n-flex>
                  <n-flex align="center" :size="4">
                    <icon-park-outline-arrow-circle-up/>
                    <div>
                      参与人数 {{ item.participantCount ? item.participantCount : 0 }}
                    </div>
                  </n-flex>
                </n-flex>
              </n-flex>
            </n-flex>

          </n-list-item>
        </n-list>
      </n-gi>
      <n-gi span="12" class="flex flex-col gap-4">
        <n-list hoverable>
          <template #header>
            <n-flex align="center" :size="8">
              <icon-park-outline-bookshelf/>
              <div>
                最新题集
              </div>
            </n-flex>
          </template>
          <n-list-item
              v-for="item in setListData"
              :key="item.id"
              @click="goSet(item.id)"
          >
            <n-flex align="center" justify="space-between">
              <n-flex vertical class="w-full">
                  <n-flex align="center" justify="space-between" class="w-full">
                    <n-flex align="center">
                      <n-tag type="info" size="small">
                        {{ item.setTypeName }}
                      </n-tag>
                      <n-ellipsis line-clamp="1" class="text-4">
                        {{ item.title }}
                      </n-ellipsis>
                    </n-flex>
                    <n-tag type="info" size="small">
                      {{ item.categoryName }}
                    </n-tag>
                  </n-flex>
                  <n-ellipsis line-clamp="1" :tooltip="false">
                    {{ item.description }}
                  </n-ellipsis>
                <n-flex align="center" justify="space-between">
                  <n-flex align="center" :size="4">
                    <icon-park-outline-user/>
                    {{ item.createUserName }}
                  </n-flex>
                  <n-flex align="center" :size="4">
                    <icon-park-outline-time/>
                    <n-time :time="item.createTime"/>
                  </n-flex>
                </n-flex>
              </n-flex>


            </n-flex>

          </n-list-item>
        </n-list>
      </n-gi>
    </n-grid>
  </div>
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
  height: 280px;
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
:deep(.n-tabs) {
  border-radius: 0.5rem;
}

:deep(.v-x-scroll) {
  padding-left: 1rem;
  padding-right: 1rem;
}

:deep(.n-list) {
  border-radius: 0.5rem;
}

:deep(.n-list__header) {
  border-top-left-radius: 0.5rem;
  border-top-right-radius: 0.5rem;
  font-size: 1rem;
}
</style>
