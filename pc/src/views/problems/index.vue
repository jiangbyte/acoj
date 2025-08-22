<script lang="ts" setup>
import { useSysCategoryFetch, useProProblemFetch, useSysTagFetch, useSysDictFetch } from '@/composables'
import type { DataTableColumns } from 'naive-ui'
import { NIcon, NSpace, NTag, NText } from 'naive-ui'
import {
  CheckmarkDone,
} from '@vicons/ionicons5'
import { AesCrypto } from '@/utils'

const { proProblemPage } = useProProblemFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysDictOptions } = useSysDictFetch()
const { sysTagOptions } = useSysTagFetch()

const categoryOptions = ref()
const difficultyOptions = ref()
const tagOptions = ref()

const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
})

const pageData = ref()

async function loadData() {
  const { data } = await proProblemPage(pageParam.value)
  if (data) {
    pageData.value = data
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  // 获取下拉列表数据
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    // 将其中的value 转换为 number，其余保留
    difficultyOptions.value = difficultyData.map((item: any) => {
      return {
        value: Number(item.value),
        label: item.label,
      }
    })
  }

  const { data: tagData } = await sysTagOptions({})
  if (tagData) {
    tagOptions.value = tagData
  }
}
loadData()

const columns: DataTableColumns<any> = [
  {
    title: '状态',
    key: 'currentUserSolved',
    width: 60,
    render: (row: any) => {
      return row.currentUserSolved
        ? h(NIcon, { component: CheckmarkDone, color: '#18A058', size: 20 })
        : null
    },
  },
  {
    title: '分类',
    key: 'categoryName',
    render: (row) => {
      return h(NTag, { size: 'small' }, row.categoryName)
    },
  },
  {
    title: '题目',
    key: 'title',
    width: 300,
  },
  {
    title: '标签',
    key: 'tagNames',
    width: 250,
    render: (row) => {
      return h(NSpace, { align: 'center' }, row.tagNames?.map((tag: any) => h(NTag, { key: tag, size: 'small' }, tag)) || null)
    },
  },
  {
    title: '难度',
    key: 'difficultyName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small' }, row.difficultyName)
    },
  },
  {
    title: '通过率',
    key: 'acceptance',
    width: 120,
  },
  {
    title: '解决',
    key: 'solved',
    width: 100,
    render: (row: any) => {
      return h(NText, { depth: 3 }, row.solved)
    },
  },
]
const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: '创建时间', value: 'createTime' },
    { label: '更新时间', value: 'updateTime' },
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
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-grid cols="12 m:24" :x-gap="16" :y-gap="16" responsive="screen">
      <!-- 左侧筛选区域 -->
      <n-gi span="12 m:6">
        <NSpace vertical class=" sticky top-22">
          <n-card title="筛选条件" class="content-card" size="small">
            <NSpace vertical>
              <n-input
                v-model:value="pageParam.keyword"
                placeholder="搜索题目"
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
                v-model:value="pageParam.tagId"
                placeholder="选择标签"
                clearable
                :options="tagOptions"
                @clear="resetHandle"
              />

              <n-button
                type="primary"
                block
                @click="loadData"
              >
                搜索
              </n-button>

              <n-button
                secondary
                block
                @click="resetHandle"
              >
                重置
              </n-button>
            </NSpace>
          </n-card>
        </NSpace>
      </n-gi>

      <!-- 右侧题目列表区域 -->
      <n-gi span="12 m:18">
        <n-card
          hoverable
          class="content-card"
          size="small"
        >
          <template #header>
            <n-flex
              align="center"
              justify="space-between"
            >
              <div>
                题目列表
              </div>
              <n-flex
                align="center"
                :size="12"
              >
                <NText depth="3">
                  共 {{ pageData?.total }} 题
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

          <!-- 题目表格 -->
          <n-data-table
            :columns="columns"
            :data="pageData?.records"
            :bordered="false"
            :row-key="(row: any) => row.id"
            :row-props="rowProps"
            class="flex-1 h-full"
          />

          <!-- 分页 -->
          <template #footer>
            <n-flex justify="center">
              <n-pagination
                v-model:page="pageParam.current"
                v-model:page-size="pageParam.size"
                show-size-picker
                :page-count="pageData ? Number(pageData.pages) : 0"
                :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                  label: `${(i + 1) * 10} 每页`,
                  value: (i + 1) * 10,
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
</style>
