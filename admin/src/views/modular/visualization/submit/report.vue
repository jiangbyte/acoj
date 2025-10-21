<script setup lang="ts">
import { useSysGroupFetch, useTaskReportsFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { NAvatar, NButton, NSpace, NTag, NText, NTime } from 'naive-ui'
import CodeEditor from '@/components/common/editor/code/CodeEditor.vue'

const route = useRoute()
const reportId = route.params.reportId // 获取 reportId
const taskId = route.params.taskId // 获取 taskId

const showModal = ref(false)
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])

const columns: DataTableColumns<any> = [
  // {
  //   title: '任务ID',
  //   key: 'taskId',
  //   width: 100,
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: '相似度(%)',
    key: 'similarity',
    width: 100,
    render: (row) => {
      return h(NTag, { type: 'success' }, () => row.similarity * 100)
    },
  },
  {
    title: '提交用户',
    key: 'submitUser',
    width: 100,
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
                src: row.submitUserAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.submitUserName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '代码长度',
    key: 'submitCodeLength',
    width: 50,
  },
  {
    title: '提交ID',
    key: 'submitId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '样本用户',
    key: 'originUser',
    width: 100,
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
    title: '代码长度',
    key: 'originCodeLength',
    width: 50,
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
    title: '样本记录时间',
    key: 'originTime',
    width: 100,
    render: (row) => {
      return h(NTime, { time: row.originTime })
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 50,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        h(NButton, { type: 'primary', size: 'small', onClick: () => dataDetail(row) }, () => '查看详情'),
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

const similarityDataDetail = ref()
function dataDetail(row: any) {
  similarityDataDetail.value = row
  showModal.value = true
}

const pageParam = ref({
  current: 1,
  size: 20,
  sortField: 'similarity',
  sortOrder: 'DESCEND',
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
  taskId,
})
const pageData = ref({
  total: 0,
  records: [],
  pages: 0,
})

const formData = ref({
  groupId: null,
})
const detailData = ref()
async function loadData() {
  useTaskReportsFetch().taskReportsDetail({ id: reportId }).then(({ data }) => {
    detailData.value = data
  })

  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })

  useTaskSimilarityFetch().taskSimilarityPage(pageParam.value).then(({ data }) => {
    pageData.value = data
  })
}

loadData()
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <n-card size="small" class="flex-1">
      <NScrollbar class="h-[calc(100vh-9.5rem)]">
        <NSpace vertical :size="20">
          <div class="flex justify-between items-center">
            <div>
              <h2 class="text-[clamp(1.25rem,3vw,1.75rem)] font-bold mb-2">
                代码克隆检测统计
              </h2>
              <NSpace align="center">
                <NTag>
                  {{ detailData?.reportTypeName ? detailData.reportTypeName : '-' }}
                </NTag>
                <NTag>
                  {{ detailData?.taskType ? '人工' : '自动' }}
                </NTag>
                <NText depth="3">
                  检测时间: <NTime :time="detailData?.createTime" /> | 任务ID: {{ detailData?.id }}
                </NText>
              </NSpace>
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

          <div
            :class="[
              detailData?.isSet
                ? 'grid grid-cols-4 md:grid-cols-4 gap-4'
                : 'grid grid-cols-3 md:grid-cols-3 gap-4',
            ]"
          >
            <n-card v-if="detailData?.isSet" size="small" title="题集" class="rounded-xl">
              <div class="flex items-center">
                <n-icon size="30" class="mr-3">
                  <icon-park-outline-book />
                </n-icon>
                <NSpace vertical :size="2">
                  <p class="font-medium">
                    {{ detailData?.setIdName ? detailData.setIdName : '-' }}
                  </p>
                  <NSpace align="center">
                    <div>
                      ID
                      <NTag size="small">
                        {{ detailData?.setId ? detailData.setId : 0 }}
                      </NTag>
                    </div>
                  </NSpace>
                </NSpace>
              </div>
            </n-card>
            <n-card size="small" title="题目" class="rounded-xl">
              <div class="flex items-center">
                <n-icon size="30" class="mr-3">
                  <icon-park-outline-book />
                </n-icon>
                <NSpace vertical :size="2">
                  <p class="font-medium">
                    {{ detailData?.problemIdName ? detailData.problemIdName : '-' }}
                  </p>
                  <NSpace align="center">
                    <div>
                      ID
                      <NTag size="small">
                        {{ detailData?.problemId ? detailData.problemId : 0 }}
                      </NTag>
                    </div>
                  </NSpace>
                </NSpace>
              </div>
            </n-card>
            <n-card size="small" title="统计" class="rounded-xl">
              <div class="flex items-center">
                <n-icon size="30" class="mr-3">
                  <icon-park-outline-chart-pie />
                </n-icon>
                <NSpace class="w-full" justify="space-between">
                  <NSpace :size="1" vertical>
                    <p class="font-medium">
                      共检测 <NTag size="small">
                        {{ detailData?.sampleCount ? detailData.sampleCount : 0 }}
                      </NTag> 份有效代码
                    </p>
                    <p class="font-medium">
                      发现 <NTag size="small">
                        {{ detailData?.similarityGroupCount ? detailData.similarityGroupCount : 0 }}
                      </NTag> 个相似分布
                    </p>
                  </NSpace>
                </NSpace>
              </div>
            </n-card>
            <n-card size="small" title="指标" class="rounded-xl">
              <div class="flex items-center">
                <n-icon size="30" class="mr-3">
                  <icon-park-outline-data />
                </n-icon>
                <NSpace class="w-full" justify="space-between">
                  <NSpace :size="1" vertical>
                    <p class="font-medium">
                      平均相似度：<NTag size="small">
                        {{ detailData?.avgSimilarity ? detailData?.avgSimilarity * 100 : 0 }} %
                      </NTag>
                    </p>
                    <p class="font-medium">
                      最高相似度：<NTag size="small">
                        {{ detailData?.maxSimilarity ? detailData?.maxSimilarity * 100 : 0 }} %
                      </NTag>
                    </p>
                  </NSpace>
                </NSpace>
              </div>
            </n-card>
          </div>

          <n-flex align="center" :wrap="false">
            <n-card size="small" title="相似度分布统计" class="rounded-xl h-300px">
              <SimilarityDistributionChart :value="detailData?.similarityDistribution || []" />
            </n-card>
            <n-card size="small" title="可疑程度统计" class="rounded-xl h-300px w-200">
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
              <template #footer>
                高度疑似克隆可能涉及抄袭行为；轻度疑似克隆可能是思路巧合。
              </template>
            </n-card>
          </n-flex>

          <n-card size="small" title="疑似克隆对列表" class="rounded-xl">
            <template #header-extra>
              <NSpace align="center">
                <n-tree-select
                  v-model:value="formData.groupId"
                  :options="groupOptions"
                  label-field="name"
                  key-field="id"
                  :indent="12"
                  placeholder="筛选用户组"
                  class="w-50"
                />
                <n-input placeholder="搜索提交用户用户名" />
                <NButton type="primary" @click="loadData">
                  搜索
                </NButton>
              </NSpace>
            </template>
            <div class="divide-y divide-gray-100 dark:divide-gray-700">
              <n-data-table
                :columns="columns"
                :data="pageData?.records"
                :bordered="false"
                :scroll-x="1400"
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
        </NSpace>
      </NScrollbar>
    </n-card>

    <n-modal
      v-model:show="showModal"
      preset="card"
      title="疑似详情"
      size="small"
      class="w-1200px"
    >
      <n-grid x-gap="12" :cols="2">
        <n-gi>
          <NSpace align="center" size="small" class="mb-2" justify="space-between">
            <NSpace align="center">
              <NAvatar
                :round="true"
                :src="similarityDataDetail.submitUserAvatar"
              />
              <NText>{{ similarityDataDetail.submitUserName }}</NText>
            </NSpace>
            <div>
              提交时间：<NTime :time="Number(similarityDataDetail.submitTime) || Date.now()" />
            </div>
          </NSpace>
          <NScrollbar class="h-[calc(600px-8rem)]">
            <CodeEditor
              :model-value="similarityDataDetail?.submitCode"
              :language="similarityDataDetail?.language"
              width="100%"
              height="300px"
              class="mb-2"
              :options="{
                readOnly: true,
              }"
            />
            <NText>
              代码长度：{{ similarityDataDetail?.submitCodeLength || 0 }}
            </NText>
            <n-collapse class="mt-2">
              <n-collapse-item title="语法Token" name="1">
                <NCode
                  :code="JSON.stringify(similarityDataDetail.submitTokenName, null)"
                  language="json"
                  word-wrap
                  show-line-numbers
                />
              </n-collapse-item>
              <n-collapse-item title="词法Token" name="2">
                <NCode
                  :code="JSON.stringify(similarityDataDetail.submitTokenTexts, null)"
                  language="json"
                  word-wrap
                  show-line-numbers
                />
              </n-collapse-item>
            </n-collapse>
          </NScrollbar>
        </n-gi>
        <n-gi>
          <NSpace align="center" size="small" class="mb-2" justify="space-between">
            <NSpace align="center">
              <NAvatar
                :round="true"
                :src="similarityDataDetail.originUserAvatar"
              />
              <NText>{{ similarityDataDetail.originUserName }}</NText>
            </NSpace>
            <div>
              提交时间：<NTime :time="Number(similarityDataDetail.originTime) || Date.now()" />
            </div>
          </NSpace>
          <NScrollbar class="h-[calc(600px-8rem)]">
            <CodeEditor
              :model-value="similarityDataDetail?.originCode"
              :language="similarityDataDetail?.language"
              width="100%"
              height="300px"
              class="mb-2"
              :options="{
                readOnly: true,
              }"
            />
            <NText>
              代码长度：{{ similarityDataDetail?.originCodeLength || 0 }}
            </NText>
            <n-collapse class="mt-2">
              <n-collapse-item title="语法Token" name="1">
                <NCode
                  :code="JSON.stringify(similarityDataDetail.originTokenName, null)"
                  language="json"
                  word-wrap
                  show-line-numbers
                />
              </n-collapse-item>
              <n-collapse-item title="词法Token" name="2">
                <NCode
                  :code="JSON.stringify(similarityDataDetail.originTokenTexts, null)"
                  language="json"
                  word-wrap
                  show-line-numbers
                />
              </n-collapse-item>
            </n-collapse>
          </NScrollbar>
        </n-gi>
        <n-gi :span="2">
          <div class="light-green" />
        </n-gi>
      </n-grid>
      <template #action>
        相似度：{{ (similarityDataDetail.similarity * 100) || 0 }} %
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
</style>
