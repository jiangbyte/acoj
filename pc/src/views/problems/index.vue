<script setup lang="ts">
import { useDataProblemFetch, useSysCategoryFetch, useSysDictFetch, useSysTagFetch } from '@/composables/v1'
import type { DataTableColumns } from 'naive-ui'
import { Icon } from '@iconify/vue'
import { NSpace, NTag } from 'naive-ui'
import { AesCrypto } from '@/utils'

const columns: DataTableColumns<any> = [
  {
    title: '状态',
    key: 'currentUserSolved',
    width: 60,
    render: (row: any) => {
      return row.currentUserSolved
        ? h(Icon, {
            icon: 'icon-park-outline:check-one',
            style: {
              fontSize: '20px',
              color: '#52c41a',
              verticalAlign: 'middle',
            },
          })
        : null
    },
  },
  {
    title: '题目',
    key: 'title',
    width: 250,
  },
  {
    title: '分类',
    key: 'categoryName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.categoryName)
    },
  },
  {
    title: '标签',
    key: 'tagNames',
    width: 250,
    render: (row) => {
      return h(NSpace, { align: 'center' }, row.tagNames?.map((tag: any) => h(NTag, { key: tag, size: 'small', bordered: false }, tag)) || null)
    },
  },
  {
    title: '难度',
    key: 'difficultyName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.difficultyName)
    },
  },
  {
    title: '通过率',
    key: 'acceptance',
    width: 120,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.acceptance)
    },
  },
  {
    title: '解决',
    key: 'solved',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.solved)
    },
  },
]

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

const categoryOptions = ref()
const difficultyOptions = ref()
const tagOptions = ref()

const pageData = ref()
const difficultyDistribution = ref()
const problemcount = ref()
const problemRankingListData = ref()

async function loadData() {
  const { dataProblemPage, dataProblemCount } = useDataProblemFetch()
  const { sysCategoryOptions } = useSysCategoryFetch()
  const { sysDictOptions } = useSysDictFetch()
  const { sysTagOptions } = useSysTagFetch()
  // 获取Top10排行榜
  useDataProblemFetch().dataProblemHot().then(({ data }) => {
    problemRankingListData.value = data
  })

  dataProblemPage(pageParam.value).then(({ data }) => {
    if (data) {
      pageData.value = data
    }
  })

  dataProblemCount().then(({ data }) => {
    if (data) {
      problemcount.value = data
    }
  })

  sysCategoryOptions({}).then(({ data }) => {
    if (data) {
      categoryOptions.value = data
    }
  })

  // 获取下拉列表数据
  sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' }).then(({ data }) => {
    if (data) {
      // 将其中的value 转换为 number，其余保留
      difficultyOptions.value = data.map((item: any) => {
        return {
          value: Number(item.value),
          label: item.label,
        }
      })
    }
  })

  sysTagOptions({}).then(({ data }) => {
    if (data) {
      tagOptions.value = data
    }
  })
}
loadData()

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

function resetHandle() {
  pageParam.value.keyword = ''
  pageParam.value.tagId = null
  pageParam.value.categoryId = null
  pageParam.value.difficulty = null
  loadData()
}
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- 页面标题和统计信息 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">
        题库
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        共收录 <span class="text-blue-600 dark:text-blue-400 font-medium">
          {{ pageData?.total }}
        </span> 道题目，覆盖各种难度和知识点
      </p>
    </div>

    <!-- 数据统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-700 transform transition-all hover:shadow-md hover:-translate-y-1">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">
              总题目数
            </p>
            <h3 class="text-2xl font-bold mt-1">
              {{ problemcount?.total }}
            </h3>
            <p class="text-green-600 dark:text-green-400 text-xs mt-2 flex items-center">
              <icon-park-outline-arrow-up class="mr-1" />
              较上月增长 {{ problemcount?.growthRate }} %
            </p>
          </div>
          <div class="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center text-blue-600 dark:text-blue-400">
            <icon-park-outline-book />
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-700 transform transition-all hover:shadow-md hover:-translate-y-1">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">
              已解决题目
            </p>
            <h3 class="text-2xl font-bold mt-1">
              {{ problemcount?.solved ? problemcount.solved : 0 }} / {{ problemcount?.total }}
            </h3>
            <p class="text-gray-500 dark:text-gray-400 text-xs mt-2 flex items-center">
              <icon-park-outline-user class="mr-1" />
              登录后可跟踪进度
            </p>
          </div>
          <div class="w-10 h-10 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center text-green-600 dark:text-green-400">
            <icon-park-outline-check />
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-700 transform transition-all hover:shadow-md hover:-translate-y-1">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">
              平均通过率
            </p>
            <h3 class="text-2xl font-bold mt-1">
              {{ problemcount?.avgPassRate ? problemcount?.avgPassRate : 0 }} %
            </h3>
            <p class="text-gray-500 dark:text-gray-400 text-xs mt-2">
              所有题目的平均提交通过率
            </p>
          </div>
          <div class="w-10 h-10 rounded-full bg-yellow-100 dark:bg-yellow-900/30 flex items-center justify-center text-yellow-600 dark:text-yellow-400">
            <icon-park-outline-chart-line />
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-700 transform transition-all hover:shadow-md hover:-translate-y-1">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-gray-500 dark:text-gray-400 text-sm">
              今日新增
            </p>
            <h3 class="text-2xl font-bold mt-1">
              {{ problemcount?.monthAdd ? problemcount?.monthAdd : 0 }}
            </h3>
            <p class="text-blue-600 dark:text-blue-400 text-xs mt-2 flex items-center">
              <icon-park-outline-calendar class="mr-1" />
              最新更新于  <n-time :time="problemcount?.lastAddTime" type="relative" />
            </p>
          </div>
          <div class="w-10 h-10 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center text-purple-600 dark:text-purple-400">
            <icon-park-outline-add />
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 mb-8">
      <!-- 关键字搜索 -->
      <div class="mb-6">
        <n-input
          v-model:value="pageParam.keyword"
          placeholder="搜索题目"
          clearable
          @keyup.enter="loadData"
          @clear="resetHandle"
        />
      </div>

      <!-- 高级筛选区 - 分类、标签、难度 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- 分类筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">题目分类</label>
          <n-select
            v-model:value="pageParam.categoryId"
            placeholder="选择分类"
            clearable
            :options="categoryOptions"
            @clear="resetHandle"
          />
        </div>

        <!-- 标签筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">标签</label>
          <n-select
            v-model:value="pageParam.tagId"
            placeholder="选择标签"
            clearable
            :options="tagOptions"
            @clear="resetHandle"
          />
        </div>

        <!-- 难度筛选 -->
        <div>
          <label class="block text-sm font-medium mb-2">难度</label>
          <n-select
            v-model:value="pageParam.difficulty"
            placeholder="选择难度"
            clearable
            :options="difficultyOptions"
            @clear="resetHandle"
          />
        </div>
      </div>

      <!-- 热门标签快速筛选 -->
      <!--      <div class="mt-6"> -->
      <!--        <label class="block text-sm font-medium mb-2">热门标签</label> -->
      <!--        <n-tag :bordered="false" class="mr-2" round v-for="i in 6" :key="i">456</n-tag> -->
      <!--      </div> -->

      <!-- 筛选按钮 -->
      <div class="mt-6 flex justify-end space-x-3">
        <n-button type="warning" @click="resetHandle">
          重置筛选
        </n-button>
        <n-button type="primary" @click="loadData">
          应用筛选
        </n-button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8">
        <!-- 题库 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
            <div class="p-5 border-b border-gray-100 dark:border-gray-700">
              <h3 class="font-semibold text-lg">
                题库
              </h3>
            </div>
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
      </div>
      <div class="space-y-8">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-700">
          <h3 class="font-bold text-lg mb-4">
            难度分布
          </h3>
          <div class="space-y-4">
            <div v-for="item in difficultyDistribution" :key="item.difficulty">
              <div class="flex justify-between mb-1">
                <span class="text-sm">{{ item.difficultyName }}</span>
                <span class="text-sm font-medium">{{ item.count }} 题 ({{ item.percentage }}%)</span>
              </div>
              <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5">
                <div class="bg-green-600 h-2.5 rounded-full" :style="{ width: `${item.percentage}%` }" />
              </div>
            </div>
          </div>
        </div>

        <!-- 榜单：最受欢迎 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="p-5 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-semibold text-lg">
              热门题目
            </h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <!-- 排名1 -->
            <div
              v-for="item in problemRankingListData" :key="item.rank" class="p-5 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="$router.push({
                name: 'problem_submit',
                query: { problem: AesCrypto.encrypt(item.id) },
              })"
            >
              <div class="flex items-center">
                <div class="w-8 h-8 rounded-full bg-gray-100 dark:bg-yellow-900 flex items-center justify-center font-bold mr-4">
                  {{ item.rank }}
                </div>
                <div class="flex-1">
                  <n-button text class="mb-2">
                    <h4 class="font-medium">
                      {{ item.title }}
                    </h4>
                  </n-button>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    {{ item.participantCount }}人解题
                  </p>
                </div>
                <!-- <div class="flex items-center text-yellow-500">
                  <span>{{ item.acceptance ? item.acceptance : 0 }}</span>
                </div> -->
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* 基础样式补充 */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* 表格行悬停效果优化 */
tr:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

/* 解决select下拉箭头在部分浏览器不显示的问题 */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}
</style>
