<script setup lang="ts">
import { useSysGroupFetch, useTaskReportsFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { NAvatar, NButton, NEllipsis, NSpace, NTag, NText, NTime } from 'naive-ui'
import CodeEditor from '@/components/common/editor/code/CodeEditor.vue'
import { actionIconBtn } from '@/utils'

const route = useRoute()
const reportId = route.params.reportId // Get reportId
const taskId = route.params.taskId // Get taskId

const showModal = ref(false)
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])

const columns: DataTableColumns<any> = [
  // {
  //   title: 'Task ID',
  //   key: 'taskId',
  //   width: 100,
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Similarity(%)',
    key: 'similarity',
    width: 100,
    render: (row) => {
      return h(NTag, { type: 'success' }, () => (row.similarity * 100).toFixed(2))
    },
  },
  {
    title: 'SubmissionUser',
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
              NEllipsis,
              {
                style: {
                  maxWidth: '90px',
                },
              },
              { default: () => row.submitUserName },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Code Length',
    key: 'submitCodeLength',
    width: 50,
  },
  {
    title: 'Submission ID',
    key: 'submitId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Sample User',
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
              NEllipsis,
              {
                style: {
                  maxWidth: '90px',
                },
              },
              { default: () => row.originUserName },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Code Length',
    key: 'originCodeLength',
    width: 50,
  },
  {
    title: 'Submission ID',
    key: 'originId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Sample Recorded At',
    key: 'originTime',
    width: 100,
    render: (row) => {
      return h(NTime, { time: row.originTime })
    },
  },
  {
    title: 'Actions',
    key: 'action',
    width: 72,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        actionIconBtn({ kind: 'view', onClick: () => dataDetail(row) }),
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   disabled: row.canUseSimilarReport !== true,
        //   onClick: () => { reportRef.value.doOpen(row) },
        // }, () => 'Similarity Report'),
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
                Clone Statistics
              </h2>
              <NSpace align="center">
                <NTag v-if="detailData?.reportTypeName">
                  {{ detailData?.reportTypeName ? detailData.reportTypeName : '-' }}
                </NTag>
                <NTag>
                  {{ detailData?.taskType ? 'Manual' : 'Automatic' }}
                </NTag>
                <NText depth="3">
                  Checked At: <NTime :time="detailData?.createTime" /> | Task ID: {{ detailData?.id }}
                </NText>
              </NSpace>
            </div>
            <div class="flex space-x-3">
              <!-- <button class="px-4 py-2 bg-white border border-gray-medium rounded-md text-sm text-gray-dark hover:bg-gray-light transition-colors">
            <i class="fa fa-download mr-1" /> Export Report
          </button>
          <button class="px-4 py-2 bg-primary text-white rounded-md text-sm hover:bg-primary/90 transition-colors">
            <i class="fa fa-refresh mr-1" /> Run Again
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
            <n-card v-if="detailData?.isSet" size="small" title="Problem Set" class="rounded-xl">
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
            <n-card size="small" title="Problem" class="rounded-xl">
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
            <n-card size="small" title="Statistics" class="rounded-xl">
              <div class="flex items-center">
                <n-icon size="30" class="mr-3">
                  <icon-park-outline-chart-pie />
                </n-icon>
                <NSpace class="w-full" justify="space-between">
                  <NSpace :size="1" vertical>
                    <p class="font-medium">
                      Generated <NTag size="small">
                        {{ detailData?.sampleCount ? detailData.sampleCount : 0 }}
                      </NTag>  clone pairs
                    </p>
                    <p class="font-medium">
                      Found <NTag size="small">
                        {{ detailData?.similarityGroupCount ? detailData.similarityGroupCount : 0 }}
                      </NTag>  similarity groups
                    </p>
                  </NSpace>
                </NSpace>
              </div>
            </n-card>
            <n-card size="small" title="Metrics" class="rounded-xl">
              <div class="flex items-center">
                <n-icon size="30" class="mr-3">
                  <icon-park-outline-data />
                </n-icon>
                <NSpace class="w-full" justify="space-between">
                  <NSpace :size="1" vertical>
                    <p class="font-medium">
                      Average Similarity：<NTag size="small">
                        {{ detailData?.avgSimilarity ? (detailData?.avgSimilarity * 100).toFixed(2) : 0 }} %
                      </NTag>
                    </p>
                    <p class="font-medium">
                      Maximum Similarity：<NTag size="small">
                        {{ detailData?.maxSimilarity ? (detailData?.maxSimilarity * 100).toFixed(2) : 0 }} %
                      </NTag>
                    </p>
                  </NSpace>
                </NSpace>
              </div>
            </n-card>
          </div>

          <n-flex align="center" :wrap="false">
            <n-card size="small" title="Similarity Distribution" class="rounded-xl h-300px">
              <SimilarityDistributionChart :value="detailData?.similarityDistribution || []" />
            </n-card>
            <n-card size="small" title="Suspicion Levels" class="rounded-xl h-300px w-200">
              <div class="flex flex-col gap-2">
                <div v-for="(item, index) in detailData?.degreeStatistics" :key="index">
                  <div class="flex items-center justify-between w-full">
                    <NText>
                      {{ item.cloneLevelName }} (≥{{ (item.similarity * 100).toFixed(2) }}%)
                    </NText>
                    <NText class="mr-4">
                      {{ item.count }} pairs
                    </NText>
                  </div>
                  <n-progress
                    type="line"
                    indicator-placement="inside"
                    :percentage="(item.percentage).toFixed(2)"
                  />
                </div>
              </div>
              <template #footer>
                Highly suspicious clones may indicate plagiarism; low similarity may be coincidental.
              </template>
            </n-card>
          </n-flex>

          <n-card size="small" title="Suspected Clone Pairs" class="rounded-xl">
            <template #header-extra>
              <NSpace align="center">
                <n-tree-select
                  v-model:value="formData.groupId"
                  :options="groupOptions"
                  label-field="name"
                  key-field="id"
                  :indent="12"
                  placeholder="Filter by user group"
                  class="w-50"
                />
                <n-input placeholder="SearchSubmissionUserusername" />
                <NButton type="primary" @click="loadData">
                  Search
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
                label: `${(i + 1) * 10} per page`,
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
      title="Suspicion Details"
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
              <NText>
                <NEllipsis style="width: 90px;">
                  {{ similarityDataDetail.submitUserName }}
                </NEllipsis>
              </NText>
            </NSpace>
            <div>
              Submitted At：<NTime :time="Number(similarityDataDetail.submitTime) || Date.now()" />
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
              Code Length：{{ similarityDataDetail?.submitCodeLength || 0 }}
            </NText>
            <n-collapse class="mt-2">
              <n-collapse-item title="Syntax Tokens" name="1">
                <NCode
                  :code="JSON.stringify(similarityDataDetail.submitTokenName, null)"
                  language="json"
                  word-wrap
                  show-line-numbers
                />
              </n-collapse-item>
              <n-collapse-item title="Lexical Tokens" name="2">
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
              <NText>
                <NEllipsis style="width: 90px;">
                  {{ similarityDataDetail.originUserName }}
                </NEllipsis>
              </NText>
            </NSpace>
            <div>
              Submitted At：<NTime :time="Number(similarityDataDetail.originTime) || Date.now()" />
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
              Code Length：{{ similarityDataDetail?.originCodeLength || 0 }}
            </NText>
            <n-collapse class="mt-2">
              <n-collapse-item title="Syntax Tokens" name="1">
                <NCode
                  :code="JSON.stringify(similarityDataDetail.originTokenName, null)"
                  language="json"
                  word-wrap
                  show-line-numbers
                />
              </n-collapse-item>
              <n-collapse-item title="Lexical Tokens" name="2">
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
        Similarity：{{ (similarityDataDetail.similarity * 100) || 0 }} %
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
</style>
