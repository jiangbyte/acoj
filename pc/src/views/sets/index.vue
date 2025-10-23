<script lang="ts" setup>
import { useDataSetFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables/v1'
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
const difficultyDistribution = ref()

const isLoading = ref(false)
async function loadData() {
  const { dataSetPage } = useDataSetFetch()
  const { sysCategoryOptions } = useSysCategoryFetch()
  const { sysDictOptions } = useSysDictFetch()
  // const { setRankingTop } = useSetRankingFetch()

  loading.value = true
  isLoading.value = true
  const { data } = await dataSetPage(pageParam.value)
  if (data) {
    pageData.value = data
    isLoading.value = false
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
  useDataSetFetch().dataSetLatest().then(({ data }) => {
    setListData.value = data
  })

  useDataSetFetch().dataSetHot().then(({ data }) => {
    setRankingListData.value = data
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
  pageParam.value.difficulty = null
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
      <!-- 左侧主内容 -->
      <n-gi span="1 l:4">
        <!-- 公告内容 -->
        <NSpace vertical :size="24">
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                筛选题集
              </n-h2>
            </template>
            <!-- <template #header-extra>
              <n-flex align="center">
                <n-text class="text-sm">
                  题目总数
                </n-text>
                <n-text class="text-2xl font-bold">
                  {{ problemcount?.total ? problemcount.total : 0 }}
                </n-text>
                <n-text class="text-green-600 dark:text-green-400 text-xs flex items-center">
                  <icon-park-outline-arrow-up class="mr-1" />
                  较上月增长 {{ problemcount?.growthRate * 100 }} %
                </n-text>
              </n-flex>
            </template> -->
            <n-grid
              cols="1 l:3"
              :x-gap="24"
              :y-gap="24"
              responsive="screen"
            >
              <n-gi span="1 l:1">
                <n-input
                  v-model:value="pageParam.keyword"
                  placeholder="搜索题集"
                  clearable
                  @keyup.enter="loadData"
                  @clear="() => { pageParam.keyword = ''; loadData() }"
                />
              </n-gi>
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.categoryId"
                  placeholder="选择分类"
                  clearable
                  :options="categoryOptions"
                  @clear="() => { pageParam.categoryId = null; loadData() }"
                />
              </n-gi>
              <n-gi span="1 l:1">
                <n-select
                  v-model:value="pageParam.difficulty"
                  placeholder="选择难度"
                  clearable
                  :options="difficultyOptions"
                  @clear="() => { pageParam.difficulty = null; loadData() }"
                />
              </n-gi>
            </n-grid>
            <template #footer>
              <n-input-group class="flex justify-end">
                <NButton type="warning" @click="resetHandle">
                  重置筛选
                </NButton>
                <NButton type="primary" @click="loadData">
                  应用筛选
                </NButton>
              </n-input-group>
            </template>
          </n-card>

          <n-card class="rounded-xl" size="small" content-style="padding: 0">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                题集列表
              </n-h2>
            </template>
            <template #header-extra>
              <n-text>
                当前共 <span class="text-blue-600 dark:text-blue-400 font-medium">
                  {{ pageData?.total ? pageData.total : 0 }}
                </span> 条数据
              </n-text>
            </template>
            <SetSkeleton01 v-if="!pageData" />
            <EmptyData v-else-if="pageData?.records.length === 0" />
            <n-list v-if="pageData && pageData?.records.length > 0" hoverable class="rounded-xl p-0">
              <n-spin :show="isLoading">
                <n-list-item
                  v-for="item in pageData?.records" :key="item.id" @click="$router.push({
                    name: 'proset_detail',
                    query: { set: AesCrypto.encrypt(item.id) },
                  })"
                >
                  <n-grid
                    cols="1 l:8"
                    :x-gap="18"
                    responsive="screen"
                  >
                    <n-gi span="1 l:3">
                      <img :src="item?.cover" class="w-full h-42 l:h-42 rounded-xl object-cover">
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
                            <n-flex>
                              <NTag size="small" type="error">
                                {{ item.setTypeName }}
                              </NTag>
                              <NTag size="small" type="info">
                                {{ item.categoryName }}
                              </NTag>
                              <NTag size="small" type="warning">
                                {{ item.difficultyName }}
                              </NTag>
                            </n-flex>
                            <n-text depth="3">
                              <n-ellipsis :line-clamp="2" :tooltip="false">
                                {{ CleanMarkdown(item.description) }}
                              </n-ellipsis>
                            </n-text>
                          </n-space>
                        </template>
                        <template #footer>
                          <n-space :size="0" align="center" justify="space-between">
                            <n-space align="center" :size="0">
                              <n-avatar :src="item?.createUserAvatar" round class="mr-2" />
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
                class="flex items-center justify-center"
                show-size-picker
                :page-count="pageData ? Number(pageData.pages) : 0"
                :page-sizes="[10, 20, 30, 50].map(size => ({
                  label: `${size} 每页`,
                  value: size,
                }))"
                :page-slot="3"
                @update:page="loadData"
                @update:page-size="loadData"
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
                热门题集
              </n-h2>
            </template>
            <ListSkeleton02 v-if="!setRankingListData" />
            <EmptyData v-else-if="setRankingListData.length === 0" />
            <HotSet v-else :list-data="setRankingListData" />
          </n-card>
        </NSpace>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>
</style>
