<script setup lang="ts">
import { useTaskReportsFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, DataTableSortState } from 'naive-ui'
import { NAvatar, NEllipsis, NSpace, NTag, NText, NTime } from 'naive-ui'
import CodeCompare from '@/components/similarity/CodeCompare.vue'
import { actionIconBtn } from '@/utils'

const route = useRoute()
const reportId = route.params.reportId // Get reportId
const taskId = route.params.taskId // Get taskId

const showModal = ref(false)

const columns = computed<DataTableColumns<any>>(() => [
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
    width: 120,
    sorter: true,
    sortOrder: pageParam.value.sortField === 'similarity'
      ? (pageParam.value.sortOrder === 'ASCEND' ? 'ascend' : 'descend')
      : false,
    render: (row) => {
      return h(NTag, { type: 'success' }, () => (Number(row.similarity) * 100).toFixed(4))
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
])

function onSort(sorter: DataTableSortState | DataTableSortState[] | null) {
  const state = Array.isArray(sorter) ? sorter[0] : sorter
  pageParam.value.current = 1
  if (!state || state.order === false || state.columnKey !== 'similarity') {
    pageParam.value.sortField = 'similarity'
    pageParam.value.sortOrder = 'DESCEND'
  }
  else {
    pageParam.value.sortField = 'similarity'
    pageParam.value.sortOrder = state.order === 'ascend' ? 'ASCEND' : 'DESCEND'
  }
  loadPairs()
}

const SUSPICION_LABELS: Record<string, string> = {
  HIGHLY_SUSPECTED: 'Highly suspicious',
  MEDIUM_SUSPECTED: 'Moderately suspicious',
  LOW_SUSPECTED: 'Slightly suspicious',
  NOT_REACHED: 'Below threshold',
  BELOW_THRESHOLD: 'Below threshold',
}

function suspicionLabel(item: { cloneLevel?: string }) {
  return (item?.cloneLevel && SUSPICION_LABELS[item.cloneLevel]) || item?.cloneLevel || ''
}

const similarityDataDetail = ref()
async function dataDetail(row: any) {
  similarityDataDetail.value = row
  showModal.value = true
  const { data } = await useTaskSimilarityFetch().taskSimilarityDetail({ id: row.id })
  if (data)
    similarityDataDetail.value = { ...row, ...data }
}

const pageParam = ref({
  current: 1,
  size: 20,
  sortField: 'similarity',
  sortOrder: 'DESCEND',
  taskId,
})
const similarityMin = ref<number | null>(null)
const similarityMax = ref<number | null>(null)
const pageData = ref({
  total: 0,
  records: [],
  pages: 0,
})

const detailData = ref()
const pairsLoading = ref(false)

function pairQuery() {
  const query: Record<string, unknown> = {
    current: pageParam.value.current,
    size: pageParam.value.size,
    sortField: pageParam.value.sortField,
    sortOrder: pageParam.value.sortOrder,
    taskId: pageParam.value.taskId,
  }
  if (similarityMin.value != null)
    query.minSimilarity = similarityMin.value / 100
  if (similarityMax.value != null)
    query.maxSimilarity = similarityMax.value / 100
  return query
}

function applySimilarityRange() {
  if (similarityMin.value != null && similarityMax.value != null && similarityMin.value > similarityMax.value) {
    window.$message?.warning('Min similarity cannot be greater than max')
    return
  }
  pageParam.value.current = 1
  loadPairs()
}

function loadPairs() {
  pairsLoading.value = true
  useTaskSimilarityFetch().taskSimilarityPage(pairQuery()).then(({ data }) => {
    pageData.value = data
  }).finally(() => {
    pairsLoading.value = false
  })
}

async function loadData() {
  useTaskReportsFetch().taskReportsDetail({ id: reportId }).then(({ data }) => {
    detailData.value = data
  })
  loadPairs()
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
                      Average similarity: <NTag size="small">
                        {{ detailData?.avgSimilarity ? (detailData?.avgSimilarity * 100).toFixed(2) : 0 }} %
                      </NTag>
                    </p>
                    <p class="font-medium">
                      Maximum similarity: <NTag size="small">
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
                      {{ suspicionLabel(item) }} (≥{{ (item.similarity * 100).toFixed(2) }}%)
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
              <NSpace align="center" :wrap="false">
                <n-input-number
                  v-model:value="similarityMin"
                  :min="0"
                  :max="100"
                  :show-button="false"
                  placeholder="Min %"
                  class="w-24"
                  clearable
                />
                <NText depth="3">
                  -
                </NText>
                <n-input-number
                  v-model:value="similarityMax"
                  :min="0"
                  :max="100"
                  :show-button="false"
                  placeholder="Max %"
                  class="w-24"
                  clearable
                />
                <n-button size="small" type="primary" @click="applySimilarityRange">
                  Filter
                </n-button>
              </NSpace>
            </template>
            <div class="h-420px">
              <n-data-table
                remote
                :columns="columns"
                :data="pageData?.records"
                :loading="pairsLoading"
                :bordered="false"
                :scroll-x="1400"
                flex-height
                :row-key="(row: any) => row.id"
                class="h-full"
                @update:sorter="onSort"
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
              @update:page="loadPairs"
              @update:page-size="loadPairs"
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
      class="w-[min(1100px,96vw)]"
      :content-style="{ maxHeight: 'calc(100vh - 180px)', overflow: 'auto' }"
    >
      <n-grid x-gap="12" :cols="2" class="mb-3">
        <n-gi>
          <NSpace align="center" size="small" justify="space-between">
            <NSpace align="center">
              <NAvatar round :src="similarityDataDetail.submitUserAvatar" />
              <NEllipsis style="max-width: 140px;">
                {{ similarityDataDetail.submitUserName }}
              </NEllipsis>
            </NSpace>
            <NText depth="3">
              {{ similarityDataDetail?.submitCodeLength || 0 }} chars ·
              <NTime :time="Number(similarityDataDetail.submitTime) || Date.now()" />
            </NText>
          </NSpace>
        </n-gi>
        <n-gi>
          <NSpace align="center" size="small" justify="space-between">
            <NSpace align="center">
              <NAvatar round :src="similarityDataDetail.originUserAvatar" />
              <NEllipsis style="max-width: 140px;">
                {{ similarityDataDetail.originUserName }}
              </NEllipsis>
            </NSpace>
            <NText depth="3">
              {{ similarityDataDetail?.originCodeLength || 0 }} chars ·
              <NTime :time="Number(similarityDataDetail.originTime) || Date.now()" />
            </NText>
          </NSpace>
        </n-gi>
      </n-grid>
      <CodeCompare
        :left-code="similarityDataDetail?.submitCode"
        :right-code="similarityDataDetail?.originCode"
        :left-tokens="similarityDataDetail?.submitTokenTexts"
        :right-tokens="similarityDataDetail?.originTokenTexts"
        :tiles="similarityDataDetail?.matchTiles"
        :language="similarityDataDetail?.language"
        height="360px"
      />
      <n-collapse class="mt-3">
        <n-collapse-item title="Tokens" name="tokens">
          <n-grid x-gap="12" :cols="2">
            <n-gi>
              <NText depth="3" class="mb-1 block">
                Syntax
              </NText>
              <NCode
                :code="JSON.stringify(similarityDataDetail.submitTokenName, null, 2)"
                language="json"
                word-wrap
              />
              <NText depth="3" class="mb-1 mt-3 block">
                Lexical
              </NText>
              <NCode
                :code="JSON.stringify(similarityDataDetail.submitTokenTexts, null, 2)"
                language="json"
                word-wrap
              />
            </n-gi>
            <n-gi>
              <NText depth="3" class="mb-1 block">
                Syntax
              </NText>
              <NCode
                :code="JSON.stringify(similarityDataDetail.originTokenName, null, 2)"
                language="json"
                word-wrap
              />
              <NText depth="3" class="mb-1 mt-3 block">
                Lexical
              </NText>
              <NCode
                :code="JSON.stringify(similarityDataDetail.originTokenTexts, null, 2)"
                language="json"
                word-wrap
              />
            </n-gi>
          </n-grid>
        </n-collapse-item>
      </n-collapse>
      <template #action>
        Similarity: {{ ((Number(similarityDataDetail?.similarity) || 0) * 100).toFixed(2) }}%
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
</style>
