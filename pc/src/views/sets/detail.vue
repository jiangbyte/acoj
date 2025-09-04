<script lang="ts" setup>
import { AesCrypto } from '@/utils'
import MdViewer from '@/components/common/editor/md/Viewer.vue'
import { useProSetFetch, useProSetProgressFetch, useProSetSolvedFetch, useProSetSubmitFetch } from '@/composables'
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NButton, NSpace, NTag, NText, NTime } from 'naive-ui'
import { Icon } from '@iconify/vue'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.set as string)
const setProblemPageData = ref()
const submitPageData = ref()
const proSetSolvedUserData = ref()
const router = useRouter()
const setProblemPageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
  setId: originalId,
})
const submitPageParam = ref({
  current: 1,
  size: 10,
  sortField: null,
  sortOrder: null,
  keyword: '',
  problem: '',
  language: null,
  submitType: null,
  status: null,
})
const proSetSolvedUserDataParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
  setId: originalId,
})

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
  {
    title: '操作',
    key: 'action',
    width: 100,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        onClick: () => {
          router.push({
            name: 'set_submit',
            query: { setId: AesCrypto.encrypt(originalId), problemId: AesCrypto.encrypt(row.id) },
          })
        },
      }, '开始')
    },
  },
]
const submitColumns: DataTableColumns<any> = [
  {
    title: 'ID',
    key: 'id',
    width: 80,
    ellipsis: true,
  },
  {
    title: '题目',
    key: 'problemIdName',
    ellipsis: true,
    width: 120,
  },
  {
    title: '用户',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            h(
              NAvatar,
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.userIdName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '状态',
    key: 'statusName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.statusName)
    },
  },
  {
    title: '编程语言',
    key: 'languageName',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.languageName)
    },
  },
  {
    title: '代码长度',
    key: 'codeLength',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.codeLength)
    },
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.submitTypeName)
    },
  },
  {
    title: '耗时',
    key: 'maxTime',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.maxTime)
    },
  },
  {
    title: '内存',
    key: 'maxMemory',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.maxMemory)
    },
  },
  {
    title: '相似度',
    key: 'similarity',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, row.similarity)
    },
  },
  // {
  //   title: '检测任务',
  //   key: 'taskId',
  //   width: 80,
  // },
  {
    title: '提交时间',
    key: 'createTime',
    width: 120,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 120,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
]
const userColumns: DataTableColumns<any> = [
  {
    title: '用户组',
    key: 'groupIdName',
    width: 150,
  },
  {
    title: '用户',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            h(
              NAvatar,
              {
                size: 'small',
                round: true,
                src: row.avatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.nickname },
            ),
          ],
        },
      )
    },
  },
  {
    title: '签名',
    key: 'quote',
    ellipsis: true,
    width: 200,
  },
  {
    title: '性别',
    key: 'gender',
    width: 100,
    render: (value) => {
      if (value.gender === 1) {
        return '男'
      }
      else if (value.gender === 2) {
        return '女'
      }
      return '未知'
    },
  },
  {
    title: '邮箱',
    key: 'email',
    ellipsis: true,
    width: 150,
  },
]

const processColumns: DataTableColumns<any> = [
  {
    title: '用户',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            h(
              NAvatar,
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.userName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '已解决题目数',
    key: 'solvedCount',
    width: 90,
  },
  {
    title: '总提交数',
    key: 'submitCount',
    width: 90,
  },
  {
    title: '通过率',
    key: 'passRate',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, '0')
    },
  },
]
const progressPageData = ref()
const progressPageParam = ref({
  current: 1,
  size: 10,
  sortField: null,
  sortOrder: null,
  keyword: '',
  problemSetId: originalId,
})

async function loadData() {
  const { proSetDetail, proSetProblemPage, proSetProblemList } = useProSetFetch()
  const { data } = await proSetDetail({ id: originalId })

  if (data) {
    detailData.value = data
  }

  // proSetProblemPage(setProblemPageParam.value).then(({ data }) => {
  //   setProblemPageData.value = data
  //   // processColumns.push({
  //   //   title: '操作',
  //   //   key: 'action',
  //   //   width: 150,
  //   // })
  //   // 获得setProblemPageData.value里面的records的title，以id为key，title为title，增加到processColumns
  //   setProblemPageData.value.records.forEach((item: any) => {
  //     processColumns.push({
  //       title: item.title,
  //       key: item.id,
  //       width: 100,
  //     })
  //   })
  // })

  proSetProblemList(setProblemPageParam.value).then(({ data }) => {
    setProblemPageData.value = data
    if (data) {
      data.forEach((item: any) => {
        processColumns.push({
          title: item.title,
          key: item.id,
          width: 100,
        })
      })
    }
  })

  const { proSetSubmitPage } = useProSetSubmitFetch()
  proSetSubmitPage(submitPageParam.value).then(({ data }) => {
    submitPageData.value = data
  })

  const { proSetSolvedUserPage } = useProSetSolvedFetch()
  proSetSolvedUserPage(proSetSolvedUserDataParam.value).then(({ data }) => {
    proSetSolvedUserData.value = data
  })

  const { proSetProgressDataPage } = useProSetProgressFetch()
  proSetProgressDataPage(progressPageParam.value).then(({ data }) => {
    progressPageData.value = data
  })
}
loadData()
function rowProps(row: any) {
  // return {
  //   style: 'cursor: pointer;',
  //   onClick: () => {
  //     router.push({
  //       name: 'problem_submit',
  //       query: { problem: AesCrypto.encrypt(row.id) },
  //     })
  //   },
  // }
}
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- 题集封面和基本信息 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden mb-8">
      <div class="md:flex items-center">
        <!-- 题集封面图 -->
        <div class="md:w-1/3 lg:w-1/4">
          <img :src="detailData?.cover" class="w-full h-69 object-cover rounded-xl">
        </div>

        <!-- 题集基本信息 -->
        <div class="p-6 md:p-8 md:w-2/3 lg:w-3/4">
          <div class="flex flex-wrap items-start justify-between gap-4 mb-4">
            <div>
              <h1 class="text-2xl md:text-3xl font-bold mb-2">
                {{ detailData?.title }}
              </h1>
              <div class="flex flex-wrap items-center gap-2">
                <span class="inline-block px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-medium rounded"> {{ detailData?.setTypeName }}</span>
                <span class="inline-block px-2 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs font-medium rounded">  {{ detailData?.categoryName }}</span>
                <span class="inline-block px-2 py-0.5 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs font-medium rounded">{{ detailData?.difficultyName }}</span>
              </div>
            </div>

            <div class="flex gap-3">
              <!-- <button class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors flex items-center">
                  <i class="fa fa-bookmark-o mr-2"></i> 收藏
                </button>
                <button class="px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors flex items-center">
                  <i class="fa fa-share-alt mr-2"></i> 分享
                </button> -->
            </div>
          </div>

          <!-- 题集统计信息 -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                题目数量
              </div>
              <div class="text-xl font-bold">
                {{ detailData?.problemCount ? detailData?.problemCount : 0 }} 题
              </div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                总提交次数
              </div>
              <div class="text-xl font-bold">
                {{ detailData?.submitCount ? detailData?.submitCount : 0 }}
              </div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                平均通过率
              </div>
              <div class="text-xl font-bold">
                {{ detailData?.avgPassRate ? detailData?.avgPassRate : 0 }} %
              </div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-lg">
              <div class="text-sm text-gray-500 dark:text-gray-400">
                参与人数
              </div>
              <div class="text-xl font-bold">
                {{ detailData?.participantCount ? detailData?.participantCount : 0 }}
              </div>
            </div>
          </div>

          <!-- 题集作者信息 -->
          <div class="flex items-center">
            <NAvatar :src="detailData?.createUserAvatar" round :size="40" class="mr-3" />
            <div>
              <div class="font-medium">
                {{ detailData?.createUserName }}
              </div>
              <div class="text-sm text-gray-500 dark:text-gray-400">
                发布于 <NTime :time="detailData?.createTime" /> · 最后更新 <NTime :time="detailData?.updateTime" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="bg-white overflow-hidden p-x-6 p-y-4">
        <MdViewer :model-value="detailData?.description" />
      </div>
    </div>

    <!-- 题集内容导航 -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6">
      <n-tabs type="line" animated>
        <n-tab-pane name="problems" tab="题目">
          <!-- 题目筛选和搜索 -->
          <div class="bg-white p-y-2">
            <div class="flex flex-col md:flex-row gap-4 mb-4">
              <div class="flex flex-wrap gap-2">
                <NButton>
                  全部题目
                </NButton>
                <NButton>
                  未完成
                </NButton>
                <NButton>
                  已完成
                </NButton>
              </div>

              <div class="flex-grow relative">
                <n-input placeholder="搜索本题集题目" />
              </div>

              <div class="w-full md:w-auto">
                <n-select class="w-40" />
              </div>
            </div>

            <div class="divide-y divide-gray-100 dark:divide-gray-700">
              <n-data-table
                :columns="columns"
                :data="setProblemPageData"
                :bordered="false"
                :row-key="(row: any) => row.id"
                class="flex-1 h-full"
                :scroll-x="1400"
              />
            </div>
          <!-- <n-pagination
              v-model:page="setProblemPageParam.current"
              v-model:page-size="setProblemPageParam.size"
              show-size-picker
              :page-count="setProblemPageData ? Number(setProblemPageData.pages) : 0"
              :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                label: `${(i + 1) * 10} 每页`,
                value: (i + 1) * 10,
              }))"
              class="flex justify-center items-center pt-6"
              @update:page="loadData"
              @update:page-size="loadData"
            /> -->
          </div>
        </n-tab-pane>
        <n-tab-pane name="progress" tab="进度">
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <n-data-table
              :columns="processColumns"
              :data="progressPageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              class="flex-1 h-full"
              :scroll-x="1400"
            />
          </div>
          <n-pagination
            v-model:page="submitPageParam.current"
            v-model:page-size="submitPageParam.size"
            show-size-picker
            :page-count="submitPageData ? Number(submitPageData.pages) : 0"
            :page-sizes="Array.from({ length: 10 }, (_, i) => ({
              label: `${(i + 1) * 10} 每页`,
              value: (i + 1) * 10,
            }))"
            class="flex justify-center items-center pt-6"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </n-tab-pane>
        <n-tab-pane name="submissions" tab="提交">
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <n-data-table
              :columns="submitColumns"
              :data="submitPageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              class="flex-1 h-full"
              :scroll-x="1400"
            />
          </div>
          <n-pagination
            v-model:page="submitPageParam.current"
            v-model:page-size="submitPageParam.size"
            show-size-picker
            :page-count="submitPageData ? Number(submitPageData.pages) : 0"
            :page-sizes="Array.from({ length: 10 }, (_, i) => ({
              label: `${(i + 1) * 10} 每页`,
              value: (i + 1) * 10,
            }))"
            class="flex justify-center items-center pt-6"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </n-tab-pane>
        <n-tab-pane name="users" tab="用户">
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <n-data-table
              :columns="userColumns"
              :data="proSetSolvedUserData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              class="flex-1 h-full"
              :scroll-x="1400"
            />
          </div>
          <n-pagination
            v-model:page="proSetSolvedUserDataParam.current"
            v-model:page-size="proSetSolvedUserDataParam.size"
            show-size-picker
            :page-count="proSetSolvedUserData ? Number(proSetSolvedUserData.pages) : 0"
            :page-sizes="Array.from({ length: 10 }, (_, i) => ({
              label: `${(i + 1) * 10} 每页`,
              value: (i + 1) * 10,
            }))"
            class="flex justify-center items-center pt-6"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </n-tab-pane>
      </n-tabs>
    </div>
  </main>
</template>

<style scoped>
</style>
