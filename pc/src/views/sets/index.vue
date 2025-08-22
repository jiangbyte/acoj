<script lang="ts" setup>
import { useSysCategoryFetch, useProSetFetch, useSysDictFetch } from '@/composables'
import { NButton, NEllipsis, NIcon, NTag, NText, NTime } from 'naive-ui'
import { AesCrypto } from '@/utils'

const { proSetPage } = useProSetFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysDictOptions } = useSysDictFetch()

const categoryOptions = ref()
const difficultyOptions = ref()
const setTypeOptions = ref()

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

async function loadData() {
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
}

loadData()

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: '创建时间', value: 'createTime' },
    { label: '更新时间', value: 'updateTime' },
    { label: '开始时间', value: 'startTime' },
    { label: '结束时间', value: 'endTime' },
  ]
})

const sortOrderOptions = computed(() => [
  { label: '升序', value: 'ASCEND' },
  { label: '降序', value: 'DESCEND' },
])

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
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-grid cols="12 m:24" :x-gap="16" :y-gap="16" responsive="screen">
      <!-- 左侧筛选区域 -->
      <n-gi span="12 m:6">
        <n-space vertical class=" sticky top-22">
          <n-card title="筛选条件" class="content-card" size="small">
            <n-space vertical>
              <n-input
                v-model:value="pageParam.keyword"
                placeholder="搜索题集"
                clearable
                @keyup.enter="loadData"
                @clear="resetHandle"
              />

              <n-select
                v-model:value="pageParam.categoryId"
                placeholder="选择分类"
                clearable
                :options="categoryOptions"
                @clear="resetHandle"
              />

              <n-select
                v-model:value="pageParam.difficulty"
                placeholder="选择难度"
                clearable
                :options="difficultyOptions"
                @clear="resetHandle"
              />

              <n-select
                v-model:value="pageParam.setType"
                placeholder="选择题集类型"
                clearable
                :options="setTypeOptions"
                @clear="resetHandle"
              />

              <NButton
                type="primary"
                block
                @click="loadData"
              >
                搜索
              </NButton>

              <NButton
                secondary
                block
                @click="resetHandle"
              >
                重置
              </NButton>
            </n-space>
          </n-card>
        </n-space>
      </n-gi>

      <!-- 右侧题集列表区域 -->
      <n-gi span="12 m:18">
        <n-card hoverable class="content-card" size="small">
          <template #header>
            <n-flex align="center" justify="space-between">
              <div>
                题集列表
              </div>
              <n-flex align="center" :size="12">
                <NText depth="3">
                  共 {{ pageData?.total }} 个题集
                </NText>
                <NPopselect
                  v-model:value="pageParam.sortField"
                  :options="columnSortFieldOptions"
                  @update:value="loadData"
                >
                  <NButton text>
                    <template #icon>
                      <IconParkOutlineSortOne />
                    </template>
                  </NButton>
                </NPopselect>
                <NPopselect
                  v-model:value="pageParam.sortOrder"
                  :options="sortOrderOptions"
                  @update:value="loadData"
                >
                  <NButton text>
                    <template #icon>
                      <IconParkOutlineSort />
                    </template>
                  </NButton>
                </NPopselect>
              </n-flex>
            </n-flex>
          </template>

          <!-- 题集列表 -->
          <n-list v-if="!loading" hoverable>
            <n-list-item
              v-for="item in pageData?.records"
              :key="item.id"
              class="cursor-pointer hover:bg-gray-50 dark:hover:bg-dark-700 transition-colors"
              @click="handleClick(item)"
            >
              <n-flex align="center">
                <n-flex align="center">
                  <n-image
                    width="110"
                    height="110"
                    object-fit="cover"
                    :src="item.cover"
                    fallback-src="#"
                    class="rounded-lg"
                    preview-disabled
                  />
                </n-flex>
                <n-flex align="center" class="flex-1">
                  <div class="flex flex-col h-full w-full">
                    <n-flex align="center" :size="8">
                      <NTag size="small">
                        {{ item.setTypeName }}
                      </NTag>
                      <h4 class="text-lg font-medium mt-2">
                        <NEllipsis line-clamp="1">
                          {{ item.title }}
                        </NEllipsis>
                      </h4>
                      <NTag v-if="item.startTime && item.endTime" size="small" type="warning">
                        {{ item.isRunning ? '进行中' : '已结束' }}
                      </NTag>
                    </n-flex>
                    <NEllipsis line-clamp="2" :tooltip="false" class="text-gray-500 dark:text-gray-400 mb-1">
                      {{ item.description }}
                    </NEllipsis>

                    <n-flex align="center" justify="space-between" class="w-full">
                      <n-space align="center">
                        <NTag size="small">
                          {{ item.difficultyName }}
                        </NTag>
                        <NTag size="small" type="info">
                          {{ item.categoryName }}
                        </NTag>
                      </n-space>

                      <n-space align="center">
                        <NIcon size="14" class="mt-1">
                          <IconParkOutlineTime />
                        </NIcon>
                        <NText depth="3">
                          <NTime :time="item.createTime" type="relative" />
                        </NText>
                      </n-space>
                    </n-flex>
                  </div>
                </n-flex>
              </n-flex>
            </n-list-item>
          </n-list>

          <n-skeleton v-else :repeat="5" class="mb-4" />

          <!-- 分页 -->
          <template #footer>
            <n-flex justify="center">
              <n-pagination
                v-model:page="pageParam.current"
                v-model:page-size="pageParam.size"
                show-size-picker
                :page-count="pageData ? Number(pageData.pages) : 0"
                :page-sizes="[10, 20, 30, 50].map(size => ({
                  label: `${size} 每页`,
                  value: size,
                }))"
                @update:page="loadData"
                @update:page-size="loadData"
              />
            </n-flex>
          </template>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>
/* :deep(div.n-progress-icon.n-progress-icon--as-text) {
color: red;
} */

/* :deep(.n-split-pane) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
} */
</style>
