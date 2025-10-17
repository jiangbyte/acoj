<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NButton, NSpace, NTag, NText, NTime } from 'naive-ui'

const route = useRoute()
const reportId = route.params.reportId // 获取 reportId
const taskId = route.params.taskId // 获取 taskId

const columns: DataTableColumns<any> = [
  {
    title: '任务ID',
    key: 'taskId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '用户',
    key: 'originUser',
    width: 120,
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
                src: row.originUserAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.originUserName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '提交ID',
    key: 'originId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '相似度',
    key: 'similarity',
    width: 100,
    render: (row) => {
      return h(NTag, { }, () => row.similarity * 100)
    },
  },
  {
    title: '提交时间',
    key: 'submitTime',
    width: 100,
    render: (row) => {
      return h(NTime, { time: row.submitTime })
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 50,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        h(NButton, { type: 'primary', size: 'small', onClick: () => {} }, () => '查看详情'),
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   disabled: row.canUseSimilarReport !== true,
        //   onClick: () => { reportRef.value.doOpen(row) },
        // }, () => '相似报告'),
      ])
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
  taskId,
})
const pageData = ref({
  total: 0,
  records: [],
})

const detailData = ref()
const totalSimilarGroups = ref(0)
async function loadData() {
  // const { proSimilarityReportsDetail } = useProSimilarityReportsFetch()
  // proSimilarityReportsDetail({ id: reportId }).then(({ data }) => {
  //   detailData.value = data
  //   if (detailData.value?.similarityDistribution && Array.isArray(detailData.value.similarityDistribution)) {
  //     totalSimilarGroups.value = detailData.value.similarityDistribution.reduce((sum: any, item: { value: any }) => {
  //       const value = typeof item === 'object' ? (item.value || 0) : (item || 0)
  //       return sum + value
  //     }, 0)
  //   }
  //   else {
  //     totalSimilarGroups.value = 0
  //   }
  // })

  // const { proSimilarityDetailPage } = useProSimilarityDetailFetch()
  // proSimilarityDetailPage(pageParam.value).then(({ data }) => {
  //   pageData.value = data
  //   console.log(data)
  // })
}

loadData()
</script>

<template>
  <div class="flex flex-col h-full w-full overflow-scroll">
    <n-card size="small" class="flex-1">
      <NSpace vertical :size="20">
        <div class="flex justify-between items-center">
          <div>
            <h2 class="text-[clamp(1.25rem,3vw,1.75rem)] font-bold mb-2">
              代码克隆检测报告
            </h2>
            <NText depth="3">
              检测时间: <NTime :time="detailData?.createTime" /> | 任务ID: {{ detailData?.id }}
            </NText>
          </div>
          <div class="flex space-x-3">
          <!-- <button class="px-4 py-2 bg-white border border-gray-medium rounded-md text-sm text-gray-dark hover:bg-gray-light transition-colors">
            <i class="fa fa-download mr-1" /> 导出报告
          </button>
          <button class="px-4 py-2 bg-primary text-white rounded-md text-sm hover:bg-primary/90 transition-colors">
            <i class="fa fa-refresh mr-1" /> 重新检测
          </button> -->
          </div>
        </div>

        <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
          <n-card size="small" title="检测题目" class="rounded-xl">
            <div class="flex items-center">
              <n-icon size="30" class="mr-3">
                <icon-park-outline-book />
              </n-icon>
              <NSpace vertical :size="2">
                <p class="font-medium">
                  题目标题
                </p>
                <NSpace align="center">
                  <div>
                    解决
                    <NTag size="small">
                      80
                    </NTag>
                  </div>
                </NSpace>
              </NSpace>
            </div>
          </n-card>
          <n-card size="small" title="检测统计" class="rounded-xl">
            <div class="flex items-center">
              <n-icon size="30" class="mr-3">
                <icon-park-outline-chart-pie />
              </n-icon>
              <NSpace class="w-full" justify="space-between">
                <NSpace :size="1" vertical>
                  <p class="font-medium">
                    共检测 {{ detailData?.sampleCount }} 份有效代码
                  </p>
                  <p class="font-medium">
                    发现 {{ totalSimilarGroups }} 组疑似克隆
                  </p>
                </NSpace>
                <!-- <NSpace :size="1" vertical>
                  <p class="font-medium">
                    发现 8 组疑似克隆
                  </p>
                  <p class="font-medium">
                    克隆率：19.05%
                  </p>
                </NSpace> -->
              </NSpace>
            </div>
          </n-card>
          <n-card size="small" title="检测指标" class="rounded-xl">
            <div class="flex items-center">
              <n-icon size="30" class="mr-3">
                <icon-park-outline-data />
              </n-icon>
              <NSpace class="w-full" justify="space-between">
                <NSpace :size="1" vertical>
                  <p class="font-medium">
                    平均相似度：{{ detailData?.avgSimilarity * 100 }}%
                  </p>
                  <p class="font-medium">
                    最高相似度：{{ detailData?.maxSimilarity * 100 }}%
                  </p>
                </NSpace>
                <!-- <NSpace :size="1" vertical>
                  <p class="font-medium">
                    检测算法：CCFinder
                  </p>
                  <p class="font-medium">
                    检测模式：深度检测
                  </p>
                </NSpace> -->
              </NSpace>
            </div>
          </n-card>
        </div>

        <n-card size="small" title="相似度分布统计" class="rounded-xl">
          <div class="grid grid-cols-3 gap-8">
            <div class="col-span-2">
              <SimilarityDistributionChart :value="detailData?.similarityDistribution" />
            </div>
            <div class="flex flex-col w-full gap-3">
              <n-card size="small" title="程度指标" class="rounded-xl">
                <div class="flex flex-col gap-2">
                  <div v-for="(item, index) in detailData?.degreeStatistics" :key="index">
                    <div class="flex items-center justify-between w-full">
                      <NText>
                        {{ item.cloneLevelName }} (≥{{ item.similarity * 100 }}%)
                      </NText>
                      <NText class="mr-4">
                        {{ item.count }}组
                      </NText>
                    </div>
                    <n-progress
                      type="line"
                      indicator-placement="inside"
                      :percentage="item.percentage"
                    />
                  </div>
                </div>
              </n-card>
              <n-alert type="info">
                高度疑似克隆可能涉及抄袭行为；轻度疑似克隆可能是思路巧合。
              </n-alert>
            </div>
          </div>
        </n-card>

        <n-card size="small" title="疑似克隆对列表" class="rounded-xl">
          <template #header-extra>
            <NSpace align="center">
              <n-select placeholder="筛选样本用户组" class="w-50" />
              <n-input placeholder="输入用户名" />
              <NButton type="primary">
                搜索
              </NButton>
            </NSpace>
          </template>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <n-data-table
              :columns="columns"
              :data="pageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              class="flex-1 h-full"
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
            class="flex justify-center items-center p-x-6 pt-6 pb-2"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </n-card>

        <!-- <n-card size="small" title="疑似克隆对详情" class="rounded-xl">
          <div class="grid grid-cols-4 gap-8">
            <div class="col-span-2">
              <n-card class="rounded-xl" size="small">
                <NSpace vertical>
                  <NSpace align="center" justify="space-between">
                    <NSpace align="center">
                      <NAvatar round src="https://avatar.csdn.net/8/D/E/3_xiaofeng_xiao.jpg" />
                      <div class="flex flex-col gap-2">
                        <div>
                          用户名
                        </div>
                        <div class="flex items-center gap-2">
                          <div>
                            创建时间
                          </div>
                          <div>
                            最后登录时间
                          </div>
                        </div>
                      </div>
                    </NSpace>
                  </NSpace>
                  <CodeEditor
                    width="100%"
                    height="300px"
                  />
                </NSpace>
              </n-card>
            </div>
            <div class="col-span-2">
              <n-card class="rounded-xl" size="small">
                <NSpace vertical>
                  <NSpace align="center" justify="space-between">
                    <NSpace align="center">
                      <NAvatar round src="https://avatar.csdn.net/8/D/E/3_xiaofeng_xiao.jpg" />
                      <div class="flex flex-col gap-2">
                        <div>
                          用户名
                        </div>
                        <div class="flex items-center gap-2">
                          <div>
                            创建时间
                          </div>
                          <div>
                            最后登录时间
                          </div>
                        </div>
                      </div>
                    </NSpace>
                  </NSpace>
                  <CodeEditor
                    width="100%"
                    height="300px"
                  />
                </NSpace>
              </n-card>
            </div>
          </div>
        </n-card> -->
      </NSpace>
    </n-card>
  </div>
</template>

<style scoped>
</style>
