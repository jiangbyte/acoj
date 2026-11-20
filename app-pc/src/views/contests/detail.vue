<script lang="ts" setup>
import { AesCrypto } from '@/utils'
import { useDataContestAuthFetch, useDataContestFetch, useDataContestProblemFetch, useDataSubmitFetch } from '@/composables/v1'
import type { DataTableColumns, ImageInst } from 'naive-ui'
import { NAvatar, NButton, NEllipsis, NSpace, NTag, NText, NTime } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'

const route = useRoute()
const detailData = ref()
const routeContestId = AesCrypto.decrypt(route.query.contestId as string)
const setProblemPageData = ref()
const submitPageData = ref()
const proSetSolvedUserData = ref()
const router = useRouter()
const submitPageParam = ref({
  current: 1,
  size: 10,
  sortField: 'id',
  sortOrder: 'DESCEND',
  keyword: '',
  problem: '',
  moduleId: routeContestId,
  moduleType: 'CONTEST',
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
  setId: routeContestId,
})

const columns: DataTableColumns<any> = [
  {
    title: 'Status',
    align: 'center',
    key: 'currentUserSolved',
    width: 20,
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
    title: 'ID',
    key: 'problemCode',
    width: 20,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Problem',
    key: 'problemIdName',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Actions',
    key: 'action',
    fixed: 'right',
    width: 20,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        // Implementation note.
        disabled: !detailData.value.isRegister || detailData.value.status !== 'RUNNING',
        onClick: () => {
          router.push({
            name: 'contest_submit',
            query: { contestId: AesCrypto.encrypt(routeContestId), problemId: AesCrypto.encrypt(row.problemId) },
          })
        },
      }, { default: () => 'Start' })
    },
  },
]
const submitColumns: DataTableColumns<any> = [
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
const isLoading = ref(true)
async function loadData() {
  const { dataContestDetail } = useDataContestFetch()
  const { data } = await dataContestDetail({ id: routeContestId })

  if (data) {
    detailData.value = data
    console.log(data)

    if (data.isPublic) {
      console.log('Public Contest')
    }
    else {
      console.log('Private Contest')
    }
  }

  // Implementation note.
  useDataContestProblemFetch().dataContestProblemList({
    contestId: routeContestId,
  }).then(({ data }) => {
    setProblemPageData.value = data
    console.log(data)
  })

  // Implementation note.
  useDataSubmitFetch().dataModuleSubmitPage(submitPageParam.value).then(({ data }) => {
    submitPageData.value = data
  })
  // TODO: Review this implementation.
  isLoading.value = false
}
loadData()

const imageRef = ref<ImageInst>()
function handleClickImage() {
  imageRef.value?.showPreview()
}

// Implementation note.
const countdownDuration = ref(0)
const countdownActive = ref(false)
const countdownType = ref<'register_start' | 'register_end' | 'contest_start' | 'contest_end'>('contest_start')

// Implementation note.
function calculateCountdown() {
  const now = Date.now()
  const registerStartTime = Number(detailData.value.registerStartTime)
  const registerEndTime = Number(detailData.value.registerEndTime)
  const contestStartTime = Number(detailData.value.contestStartTime)
  const contestEndTime = Number(detailData.value.contestEndTime)

  // Implementation note.
  switch (detailData.value.status) {
    case 'NOT_STARTED':
      // Implementation note.
      countdownDuration.value = registerStartTime - now
      countdownType.value = 'register_start'
      countdownActive.value = true
      break
    case 'REGISTERING':
      // Implementation note.
      countdownDuration.value = registerEndTime - now
      countdownType.value = 'register_end'
      countdownActive.value = true
      break
    case 'REGISTER_END':
      // Implementation note.
      countdownDuration.value = contestStartTime - now
      countdownType.value = 'contest_start'
      countdownActive.value = true
      break
    case 'RUNNING':
    case 'FROZEN':
      // Implementation note.
      countdownDuration.value = contestEndTime - now
      countdownType.value = 'contest_end'
      countdownActive.value = true
      break
    case 'FINISHED':
    default:
      // Implementation note.
      countdownActive.value = false
      break
  }
}

// Implementation note.
function getCountdownText() {
  if (!countdownActive.value)
    return ''
  switch (countdownType.value) {
    case 'register_start':
      return `Registration opens in:`
    case 'register_end':
      return `Registration closes in:`
    case 'contest_start':
      return `Contest starts in:`
    case 'contest_end':
      return `Contest ends in:`
    default:
      return ''
  }
}

// Implementation note.
watch(() => detailData.value, () => {
  calculateCountdown()
})

// Implementation note.
function renderCountdown({ hours, minutes, seconds }: { hours: number, minutes: number, seconds: number }) {
  return `${hours.toString().padStart(2, '0')}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s`
}

function onfinishTime() {
  window.location.reload()
}

const passwordFormRef = ref()
const passwordFormLoading = ref(false)
const passwordFormValue = ref({
  contestId: routeContestId,
  password: '',
})

const rules = {
  password: [
    { required: true, message: 'Enter your password', trigger: 'blur' },
  ],
}

function handlePasswordSubmit() {
  passwordFormRef.value.validate((errors: any) => {
    if (!errors) {
      passwordFormLoading.value = true

      useDataContestAuthFetch().dataContestAuth(passwordFormValue.value).then(({ success }) => {
        if (success) {
          passwordFormLoading.value = false
          // Implementation note.
          window.location.reload()
        }
        else {
          passwordFormLoading.value = false
        }
      })
    }
  })
}

function signUp() {
  const siup = {
    contestId: routeContestId,
  }
  useDataContestFetch().dataContestSignUp(siup).then(({ success }) => {
    if (success) {
      window.location.reload()
    }
  })
}

function cancelSignUp() {
  const siup = {
    contestId: routeContestId,
  }
  useDataContestFetch().dataContestCancelSignUp(siup).then(({ success }) => {
    if (success) {
      window.location.reload()
    }
  })
}
</script>

<template>
  <!-- Section -->
  <template v-if="isLoading">
    <main class="container mx-auto px-2 py-6">
      <n-flex class="min-h-[calc(100vh-10rem-12rem)]" align="center" justify="center" vertical>
        <n-spin size="large" />
        <NText>Loading contest...</NText>
      </n-flex>
    </main>
  </template>
  <template v-else-if="detailData?.isAuth">
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
                <img
                  :src="detailData?.cover" class="w-full
                <!-- Section -->
                  rounded-t-xl rounded-tr-xl rounded-tl-xl
                  rounded-br-none rounded-bl-none
                  <!-- Section -->
                  m:rounded-l-xl m:rounded-bl-xl m:rounded-tl-xl
                  m:rounded-r-none m:rounded-tr-none m:rounded-br-none
                  l:rounded-l-xl l:rounded-bl-xl l:rounded-tl-xl
                  l:rounded-r-none l:rounded-tr-none l:rounded-br-none
                  object-cover h-50 m:absolute m:inset-0 m:h-full l:absolute l:inset-0 l:h-full"
                  @click="handleClickImage"
                >
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
                    <template #header-extra>
                      <n-flex align="center">
                        <NButton v-if="detailData?.status === 'REGISTERING' && !detailData?.isRegister" type="primary" @click="signUp">
                          Register
                        </NButton>
                        <NButton v-if="detailData?.status === 'REGISTERING' && detailData?.isRegister" type="warning" @click="cancelSignUp">
                          Withdraw
                        </NButton>
                      </n-flex>
                    </template>
                    <template #description>
                      <NSpace vertical>
                        <n-flex>
                          <NTag v-if="detailData?.statusName" size="small" type="error">
                            {{ detailData?.statusName }}
                          </NTag>
                          <NTag size="small" type="error">
                            {{ detailData?.isPublic ? 'Public Contest' : 'Private Contest' }}
                          </NTag>
                          <NTag v-if="detailData?.category" size="small" type="success">
                            {{ detailData?.category }}
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
                          <NAvatar :src="detailData?.createUserAvatar" round class="mr-2" />
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
                        v-if="detailData?.status === 'RUNNING'"
                        :columns="columns"
                        :data="setProblemPageData"
                        :bordered="false"
                        :row-key="(row: any) => row.id"
                        class="flex-1 h-full"
                        :scroll-x="800"
                      />
                      <NText v-else>
                        Problems will be available when the contest starts
                      </NText>
                    </div>
                  </div>
                </n-tab-pane>
                <n-tab-pane name="submissions" tab="Submit">
                  <div class="divide-y divide-gray-100 dark:divide-gray-700">
                    <n-data-table
                      v-if="detailData?.status === 'RUNNING'"
                      :columns="submitColumns"
                      :data="submitPageData?.records"
                      :bordered="false"
                      :row-key="(row: any) => row.userId"
                      class="flex-1 h-full"
                      :scroll-x="1200"
                    />
                    <NText v-else>
                      Submissions will be available when the contest starts
                    </NText>
                  </div>
                  <n-pagination
                    v-if="detailData?.status === 'RUNNING'"
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
                  Contest Details
                </n-h2>
              </template>
              <div class="mb-4 flex flex-col gap-3">
                <!-- Section -->
                <NSpace v-if="countdownActive" align="center" :size="0">
                  <NText depth="3">
                    {{ getCountdownText() }}
                  </NText>
                  <NCountdown
                    :key="`${countdownType}-${countdownDuration}`"
                    :duration="countdownDuration"
                    :active="countdownActive"
                    :render="renderCountdown"
                    @finish="onfinishTime()"
                  />
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    Registration Opens:
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.registerStartTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    Registration Closes:
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.registerEndTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    Contest Starts:
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.contestStartTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    Contest Ends:
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.contestEndTime) || 0" />
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
              </div>
              <n-alert show-icon type="warning">
                Refresh the page when the contest starts. Submissions are locked when it ends.
              </n-alert>
            </n-card>
          </NSpace>
        </n-gi>
      </n-grid>
    </main>
  </template>
  <template v-else>
    <main class="container mx-auto px-2 py-6">
      <n-flex class="min-h-[calc(100vh-10rem-12rem)]" align="center" justify="center" vertical>
        <n-card class="w-full max-w-md" title="Private Contest">
          <n-form ref="passwordFormRef" :model="passwordFormValue" :rules="rules">
            <n-form-item path="password" label="Access Password">
              <n-input
                v-model:value="passwordFormValue.password"
                type="password"
                placeholder="Enter the access password"
                @keydown.enter="handlePasswordSubmit"
              />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-flex justify="end">
              <NButton type="primary" :loading="passwordFormLoading" @click="handlePasswordSubmit">
                Enter Contest
              </NButton>
            </n-flex>
          </template>
        </n-card>
      </n-flex>
    </main>
  </template>
</template>

<style scoped>
</style>
