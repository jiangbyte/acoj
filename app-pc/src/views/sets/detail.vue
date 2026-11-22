<script lang="ts" setup>
import { AesCrypto, renderSafeAvatar } from '@/utils'
import { useDataSetFetch, useDataSubmitFetch } from '@/composables/v1'
import type { DataTableColumns, ImageInst } from 'naive-ui'
import { NButton, NEllipsis, NSpace, NTag, NText, NTime } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'

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
  id: originalId,
})
const submitPageParam = ref({
  current: 1,
  size: 10,
  sortField: 'id',
  sortOrder: 'DESCEND',
  keyword: '',
  problem: '',
  moduleId: originalId,
  moduleType: 'SET',
  language: null,
  submitType: null,
  status: null,
  isAuth: false,
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
    title: 'Status',
    align: 'center',
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
    title: 'Problem',
    key: 'title',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Category',
    key: 'categoryName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', type: 'success' }, { default: () => row.categoryName })
    },
  },
  {
    title: 'Tags',
    key: 'tagNames',
    width: 180,
    render: (row) => {
      return h(NSpace, { align: 'center' }, { default: () => row.tagNames?.map((tag: any) => h(NTag, { key: tag, size: 'small', type: 'info' }, { default: () => tag })) || [] })
    },
  },
  {
    title: 'Difficulty',
    key: 'difficultyName',
    align: 'center',
    width: 60,
    render: (row) => {
      return h(NTag, { size: 'small', type: 'error' }, { default: () => row.difficultyName })
    },
  },
  {
    title: 'Acceptance Rate',
    key: 'acceptance',
    width: 100,
    align: 'center',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.acceptance })
    },
  },
  {
    title: 'Solved',
    key: 'solved',
    width: 100,
    align: 'center',
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.solved })
    },
  },
  {
    title: 'Actions',
    key: 'action',
    fixed: 'right',
    width: 60,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        disabled: detailData.value.setType === 2 ? detailData.value.timeStatus !== 2 : false,
        onClick: () => {
          router.push({
            name: 'set_submit',
            query: { setId: AesCrypto.encrypt(originalId), problemId: AesCrypto.encrypt(row.id) },
          })
        },
      }, { default: () => 'Start' })
    },
  },
]
const submitColumns: DataTableColumns<any> = [
  // {
  //   title: 'ID',
  //   key: 'id',
  //   width: 80,
  //   ellipsis: true,
  // },
  {
    title: 'Problem',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
    width: 120,
  },
  {
    title: 'User',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            renderSafeAvatar(
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
            ),
            h(
              NEllipsis,
              {
                style: {
                  maxWidth: '90px',
                },
              },
              { default: () => row.userIdName },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Status',
    key: 'statusName',
    width: 100,
    render: (row) => {
      return h(NTag, { size: 'small', type:
      row.status === 'COMPILATION_ERROR'
      || row.status === 'RUNTIME_ERROR'
      || row.status === 'TIME_LIMIT_EXCEEDED'
      || row.status === 'MEMORY_LIMIT_EXCEEDED'
      || row.status === 'WRONG_ANSWER'
      || row.status === 'SYSTEM_ERROR'
      || row.status === 'MEMORY_LIMIT_EXCEEDED'
        ? 'error'
        : 'success' }, { default: () => row.statusName })
    },
  },
  {
    title: 'Programming Language',
    key: 'languageName',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: 'Size (bytes)',
    key: 'codeLength',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.codeLength })
    },
  },
  {
    title: 'Run Type',
    key: 'submitTypeName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', type: row.submitType ? 'info' : 'warning' }, { default: () => row.submitTypeName })
    },
  },
  {
    title: 'Time (ms)',
    key: 'maxTime',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxTime })
    },
  },
  {
    title: 'Memory (KB)',
    key: 'maxMemory',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxMemory })
    },
  },
  // {
  //   title: 'Similarity(%)',
  //   key: 'similarity',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, { size: 'small' }, { default: () => row.similarity * 100 })
  //   },
  // },
  // {
  //   title: 'Flag',
  //   key: 'similarityCategoryName',
  //   ellipsis: true,
  //   width: 80,
  //   render: (row) => {
  //     return row.similarityCategoryName ? row.similarityCategoryName : 'Not Triggered'
  //   },
  // },
  // {
  //   title: 'Detection Task',
  //   key: 'taskId',
  //   width: 80,
  // },
  {
    title: 'Submitted',
    key: 'createTime',
    width: 100,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
  {
    title: 'Updated',
    key: 'updateTime',
    width: 100,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
]
const userColumns: DataTableColumns<any> = [
  {
    title: 'User Group',
    key: 'groupIdName',
    width: 150,
  },
  {
    title: 'User',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            renderSafeAvatar(
              {
                size: 'small',
                round: true,
                src: row.avatar,
              },
            ),
            h(
              NEllipsis,
              {
                style: {
                  maxWidth: '90px',
                },
              },
              { default: () => row.nickname },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Bio',
    key: 'quote',
    ellipsis: {
      tooltip: true,
    },
    width: 200,
  },
  {
    title: 'Gender',
    key: 'gender',
    width: 100,
    render: (value) => {
      if (value.gender === 1) {
        return 'Male'
      }
      else if (value.gender === 2) {
        return 'Female'
      }
      return 'Unknown'
    },
  },
  {
    title: 'Email',
    key: 'email',
    ellipsis: {
      tooltip: true,
    },
    width: 150,
  },
]

const processColumns: DataTableColumns<any> = [
  {
    title: 'User',
    key: 'user',
    width: 150,
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            renderSafeAvatar(
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
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
    title: 'Problems Solved',
    key: 'solvedCount',
    width: 90,
  },
  {
    title: 'Total Submissions',
    key: 'submitCount',
    width: 90,
  },
  {
    title: 'Acceptance Rate',
    key: 'passRate',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', bordered: false }, { default: () => row.passRate })
    },
  },
]

async function loadData() {
  const { dataSetDetail } = useDataSetFetch()
  const { data } = await dataSetDetail({ id: originalId })

  if (data) {
    detailData.value = data
  }

  useDataSetFetch().dataSetProblem(setProblemPageParam.value).then(({ data }) => {
    console.log(data)
    setProblemPageData.value = data
    setProblemPageData.value.forEach((item: any) => {
      processColumns.push({
        title: item.title,
        key: item.id,
        width: 100,
      })
    })
  })

  useDataSubmitFetch().dataModuleSubmitPage(submitPageParam.value).then(({ data }) => {
    submitPageData.value = data
  })
  useDataSetFetch().dataSetUserPage(proSetSolvedUserDataParam.value).then(({ data }) => {
    proSetSolvedUserData.value = data
    console.log('user', data)
  })
}
loadData()

const imageRef = ref<ImageInst>()
function handleClickImage() {
  imageRef.value?.showPreview()
}

// Implementation note.
const countdownDuration = ref(0)
const countdownActive = ref(false)
const countdownType = ref<'start' | 'end'>('start')

/**
  * Implementation details.
 */
function calculateCountdown() {
  // Implementation note.
  if (detailData.value?.setType !== 2) {
    countdownActive.value = false
    return
  }

  const now = Date.now()
  const startTime = Number(detailData.value.startTime)
  const endTime = Number(detailData.value.endTime)

  // Implementation note.
  if (now < startTime) {
    countdownDuration.value = startTime - now
    countdownType.value = 'start'
    countdownActive.value = true
  }
  else if (now >= startTime && now < endTime) {
    countdownDuration.value = endTime - now
    countdownType.value = 'end'
    countdownActive.value = true
  }
  else {
    countdownActive.value = false
  }
}

// Implementation note.
watch(() => detailData.value, (newVal) => {
  if (newVal?.setType === 2) {
    calculateCountdown()
  }
})

// Implementation note.
function renderCountdown({ hours, minutes, seconds }: { hours: number, minutes: number, seconds: number }) {
  return `${hours.toString().padStart(2, '0')}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s`
}

function onfinishTime() {
  window.location.reload()
}
</script>

<template>
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:7"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <n-gi span="1 l:7">
        <n-card size="small" content-style="padding: 0" class="rounded-xl">
          <n-grid
            cols="1 l:8"
            responsive="screen"
            class="relative"
          >
            <n-gi span="1 l:3" class="relative">
              <SafeImage
                :src="detailData?.cover"
                class="w-full rounded-t-xl m:rounded-l-xl m:rounded-r-none l:rounded-l-xl l:rounded-r-none h-50 m:absolute m:inset-0 m:h-full l:absolute l:inset-0 l:h-full"
                object-fit="cover"
                @click="handleClickImage"
              />
              <n-image
                ref="imageRef"
                :src="detailData?.cover"
                class="opacity-0 w-0 h-0"
              />
            </n-gi>
            <n-gi span="1 l:5">
              <n-card size="small" :bordered="false" class="l:h-full m:h-full rounded-xl">
                <n-thing class="w-full">
                  <template #header>
                    <n-flex align="center" :wrap="false">
                      <n-h2 class="pb-0 mb-0">
                        <!-- <n-ellipsis :line-clamp="1"> -->
                        {{ detailData?.title }}
                        <!-- </n-ellipsis> -->
                      </n-h2>
                    </n-flex>
                  </template>
                  <template #description>
                    <NSpace vertical>
                      <n-flex>
                        <!-- Section -->
                        <NTag v-if="detailData?.setType === 2" size="small" type="error">
                          {{ detailData?.timeStatusName || 'Unknown Status' }}
                        </NTag>
                        <NTag size="small" type="success">
                          {{ detailData?.setTypeName }}
                        </NTag>
                        <NTag size="small" type="info">
                          {{ detailData?.categoryName }}
                        </NTag>
                        <NTag size="small" type="warning">
                          {{ detailData?.difficultyName }}
                        </NTag>
                      </n-flex>
                      <NText>
                        <MarkdownViewer :model-value="detailData?.description || ''" />
                      </NText>
                    </NSpace>
                  </template>
                  <template #footer>
                    <NSpace :size="0" align="center" justify="space-between">
                      <NSpace align="center" :size="0">
                        <SafeAvatar :src="detailData?.createUserAvatar" round class="mr-2" />
                        <NText class="flex-1">
                          {{ detailData?.createUserName }}
                        </NText>
                      </NSpace>
                      <NText>
                        <NTime :time="detailData?.createTime" />
                      </NText>
                    </NSpace>
                  </template>
                </n-thing>
              </n-card>
            </n-gi>
          </n-grid>
        </n-card>
      </n-gi>
      <!-- Section -->

      <!-- Section -->
      <n-gi span="1 l:5">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card class="rounded-xl" size="small">
            <n-tabs type="line" animated>
              <n-tab-pane name="problems" tab="Problem">
                <!-- Section -->
                <div class="bg-white p-y-2">
                  <div class="divide-y divide-gray-100 dark:divide-gray-700">
                    <n-data-table
                      :columns="columns"
                      :data="setProblemPageData"
                      :bordered="false"
                      :row-key="(row: any) => row.id"
                      class="flex-1 h-full"
                      :scroll-x="800"
                    />
                  </div>
                </div>
              </n-tab-pane>
              <n-tab-pane name="submissions" tab="Submit">
                <div class="divide-y divide-gray-100 dark:divide-gray-700">
                  <n-data-table
                    :columns="submitColumns"
                    :data="submitPageData?.records"
                    :bordered="false"
                    :row-key="(row: any) => row.userId"
                    class="flex-1 h-full"
                    :scroll-x="1200"
                  />
                </div>
                <n-pagination
                  v-model:page="submitPageParam.current"
                  v-model:page-size="submitPageParam.size"
                  show-size-picker
                  :page-count="submitPageData ? Number(submitPageData.pages) : 0"
                  :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                    label: `${(i + 1) * 10} per page`,
                    value: (i + 1) * 10,
                  }))"
                  :page-slot="3"
                  class="flex justify-center items-center pt-6"
                  @update:page="loadData"
                  @update:page-size="loadData"
                />
              </n-tab-pane>
              <n-tab-pane name="users" tab="User">
                <div class="divide-y divide-gray-100 dark:divide-gray-700">
                  <n-data-table
                    :columns="userColumns"
                    :data="proSetSolvedUserData?.records"
                    :bordered="false"
                    :row-key="(row: any) => row.userId"
                    class="flex-1 h-full"
                    :scroll-x="800"
                  />
                </div>
                <n-pagination
                  v-model:page="proSetSolvedUserDataParam.current"
                  v-model:page-size="proSetSolvedUserDataParam.size"
                  show-size-picker
                  :page-count="proSetSolvedUserData ? Number(proSetSolvedUserData.pages) : 0"
                  :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                    label: `${(i + 1) * 10} per page`,
                    value: (i + 1) * 10,
                  }))"
                  :page-slot="3"
                  class="flex justify-center items-center pt-6"
                  @update:page="loadData"
                  @update:page-size="loadData"
                />
              </n-tab-pane>
            </n-tabs>
          </n-card>
        </NSpace>
      </n-gi>

      <n-gi span="1 l:2">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card class="rounded-xl" size="small">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Problem Set Details
              </n-h2>
            </template>
            <div class="mb-4 flex flex-col gap-3">
              <!-- Section -->
              <NSpace v-if="detailData?.setType === 2 && countdownActive" align="center" :size="0">
                <NText depth="3">
                  {{ countdownType === 'start' ? 'Starts in:' : 'Ends in:' }}
                </NText>
                <NCountdown
                  :key="`${countdownType}-${countdownDuration}`"
                  :duration="countdownDuration"
                  :active="countdownActive"
                  :render="renderCountdown"
                  @finish="onfinishTime()"
                />
              </NSpace>
              <NSpace v-if="detailData?.setType === 2" align="center" :size="0">
                <NText depth="3">
                  Starts:
                </NText>
                <NText>
                  <NTime :time="Number(detailData?.startTime) || 0" />
                </NText>
              </NSpace>
              <NSpace v-if="detailData?.setType === 2" align="center" :size="0">
                <NText depth="3">
                  Ends:
                </NText>
                <NText>
                  <NTime :time="Number(detailData?.endTime) || 0" />
                </NText>
              </NSpace>
              <NSpace align="center" :size="0">
                <NText depth="3">
                  Problems:
                </NText>
                <NText>
                  {{ setProblemPageData?.length ? setProblemPageData.length : 0 }}
                </NText>
              </NSpace>
              <NSpace align="center" :size="0">
                <NText depth="3">
                  AI Assistance:
                </NText>
                <NText>
                  {{ detailData?.useAiName }}
                </NText>
              </NSpace>
              <NSpace align="center" :size="0">
                <NText depth="3">
                  Total Submissions:
                </NText>
                <NText>
                  {{ detailData?.submitCount ? detailData?.submitCount : 0 }}
                </NText>
              </NSpace>
              <NSpace align="center" :size="0">
                <NText depth="3">
                  Average Acceptance:
                </NText>
                <NText>
                  {{ detailData?.avgAcceptance ? detailData?.avgAcceptance : 0 }}
                </NText>
              </NSpace>
              <NSpace align="center" :size="0">
                <NText depth="3">
                  Participants:
                </NText>
                <NText>
                  {{ detailData?.participantUserCount ? detailData?.participantUserCount : 0 }}
                </NText>
              </NSpace>
            </div>
            <n-alert v-if="detailData?.setType === 2" show-icon type="warning">
              Refresh the page when the timed set starts. Submissions are locked when it ends.
            </n-alert>
          </n-card>
        </NSpace>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>
</style>
