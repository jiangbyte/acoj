<script lang="ts" setup>
import { useDataContestFetch, useDataSetFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables/v1'
import { NButton, NTag } from 'naive-ui'
import { AesCrypto, CleanMarkdown } from '@/utils'

const categoryOptions = ref()
const setTypeOptions = ref()
const setRankingListData = ref()

const pageParam = ref({
  current: 1,
  size: 10,
  sortField: 'id',
  sortOrder: 'DESCEND',
  keyword: '',
  categoryId: null,
  setType: null,
})

const pageData = ref()
const loading = ref(true)
const setListData = ref()
const difficultyDistribution = ref()

const isLoading = ref(false)
async function loadData() {
  const { dataContestPage, dataContestHot } = useDataContestFetch()
  const { sysCategoryOptions } = useSysCategoryFetch()
  const { sysDictOptions } = useSysDictFetch()
  // const { setRankingTop } = useSetRankingFetch()

  loading.value = true
  isLoading.value = true
  const { data } = await dataContestPage(pageParam.value)
  if (data) {
    pageData.value = data
    isLoading.value = false
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  // Implementation note.
  const { data: setTypeData } = await sysDictOptions({ dictType: 'PROBLEM_SET_TYPE' })
  if (setTypeData) {
    setTypeOptions.value = setTypeData.map((item: any) => ({
      value: Number(item.value),
      label: item.label,
    }))
  }

  loading.value = false

  // Implementation note.
  useDataSetFetch().dataSetLatest().then(({ data }) => {
    setListData.value = data
  })

  dataContestHot().then(({ data }) => {
    setRankingListData.value = data
    console.log(data)
  })

  useDataSetFetch().dataSetDifficultyDistribution().then(({ data }) => {
    if (data) {
      difficultyDistribution.value = data
    }
  })
}

loadData()

function resetHandle() {
  pageParam.value.keyword = ''
  pageParam.value.categoryId = null
  pageParam.value.setType = null
  loadData()
}
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
      <n-gi span="1 l:4">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Filter Contests
              </n-h2>
            </template>
            <!-- <template #header-extra>
              <n-flex align="center">
                <n-text class="text-sm">
                  Total Problems
                </n-text>
                <n-text class="text-2xl font-bold">
                  {{ problemcount?.total ? problemcount.total : 0 }}
                </n-text>
                <n-text class="text-green-600 dark:text-green-400 text-xs flex items-center">
                  <icon-park-outline-arrow-up class="mr-1" />
                  Up from last month: {{ problemcount?.growthRate * 100 }} %
                </n-text>
              </n-flex>
            </template> -->
            <n-grid
              cols="1 l:3"
              :x-gap="24"
              :y-gap="24"
              responsive="screen"
            >
              <n-gi span="2 l:2">
                <n-input
                  v-model:value="pageParam.keyword"
                  placeholder="Search contests"
                  clearable
                  @keyup.enter="loadData"
                  @clear="() => { pageParam.keyword = ''; loadData() }"
                />
              </n-gi>
              <!-- <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.categoryId"
                  placeholder="Select a category"
                  clearable
                  :options="categoryOptions"
                  @clear="() => { pageParam.categoryId = null; loadData() }"
                />
              </n-gi> -->
            </n-grid>
            <template #footer>
              <n-input-group class="flex justify-end">
                <NButton type="warning" @click="resetHandle">
                  Reset
                </NButton>
                <NButton type="primary" @click="loadData">
                  Apply Filters
                </NButton>
              </n-input-group>
            </template>
          </n-card>

          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Contest List
              </n-h2>
            </template>
            <template #header-extra>
              <n-text>
                Total: <span class="text-blue-600 dark:text-blue-400 font-medium">
                  {{ pageData?.total ? pageData.total : 0 }}
                </span> items
              </n-text>
            </template>
            <SetSkeleton01 v-if="!pageData" />
            <EmptyData v-else-if="pageData?.records.length === 0" />
            <n-list v-if="pageData && pageData?.records.length > 0" hoverable class="rounded-xl p-0">
              <n-spin :show="isLoading">
                <n-list-item
                  v-for="item in pageData?.records" :key="item.id" @click="$router.push({
                    name: 'contest_detail',
                    query: { contestId: AesCrypto.encrypt(item.id) },
                  })"
                >
                  <n-grid
                    cols="1 l:8"
                    :x-gap="18"
                    responsive="screen"
                  >
                    <n-gi span="1 l:3">
                      <SafeImage :src="item?.cover" class="w-full h-52 l:h-52 rounded-xl" object-fit="cover" />
                    </n-gi>
                    <n-gi span="1 l:5" class="flex items-center w-full">
                      <n-thing class="w-full">
                        <template #header>
                          <n-flex align="center" :wrap="false">
                            <n-h3 class="pb-0 mb-0">
                              <n-ellipsis :line-clamp="1">
                                {{ item.title }}
                              </n-ellipsis>
                            </n-h3>
                          </n-flex>
                        </template>
                        <template #description>
                          <n-space vertical>
                            <n-flex vertical>
                              <n-flex align="center">
                                <NTag size="small" type="error">
                                  {{ item.isPublic ? 'Public Contest' : 'Private Contest' }}
                                </NTag>
                                <NTag v-if="item.statusName" size="small" type="error">
                                  {{ item.statusName }}
                                </NTag>
                              </n-flex>
                              <n-flex align="center">
                                <NTag v-if="item.categoryName" size="small" type="success">
                                  {{ item.categoryName }}
                                </NTag>
                              </n-flex>
                              <n-flex vertical :size="0">
                                <n-flex>
                                  <n-text>
                                    Contest Period <NTime :time="Number(item.contestStartTime)" /> - <NTime :time="Number(item.contestEndTime)" />
                                  </n-text>
                                </n-flex>
                              </n-flex>
                            </n-flex>
                            <n-text depth="3">
                              <n-ellipsis :line-clamp="1" :tooltip="false">
                                {{ CleanMarkdown(item.description) }}
                              </n-ellipsis>
                            </n-text>
                          </n-space>
                        </template>
                        <template #footer>
                          <n-space :size="0" align="center" justify="space-between">
                            <n-space align="center" :size="0">
                              <SafeAvatar :src="item?.createUserAvatar" round class="mr-2" />
                              <n-text class="flex-1">
                                {{ item?.createUserName }}
                              </n-text>
                            </n-space>
                            <n-text>
                              <n-time :time="item.createTime" />
                            </n-text>
                          </n-space>
                        </template>
                      </n-thing>
                    </n-gi>
                  </n-grid>
                </n-list-item>
              </n-spin>
            </n-list>
            <template #footer>
              <n-pagination
                v-model:page="pageParam.current"
                v-model:page-size="pageParam.size"
                class="flex items-center justify-center mt-2"
                show-size-picker
                :page-count="pageData ? Number(pageData.pages) : 0"
                :page-sizes="[10, 20, 30, 50].map(size => ({
                  label: `${size} per page`,
                  value: size,
                }))"
                :page-slot="3"
                @update:page="loadData"
                @update:page-size="() => {
                  pageParam.current = 1
                  loadData()
                }"
              />
            </template>
          </n-card>
        </NSpace>
      </n-gi>
      <n-gi span="1 l:2">
        <NSpace
          vertical
          :size="24"
        >
          <n-card size="small" class="rounded-xl" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Popular Contests
              </n-h2>
            </template>
            <ListSkeleton02 v-if="!setRankingListData" />
            <EmptyData v-else-if="setRankingListData.length === 0" />
            <HotContest v-else :list-data="setRankingListData" />
          </n-card>
        </NSpace>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>
</style>
