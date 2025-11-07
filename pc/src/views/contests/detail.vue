<script lang="ts" setup>
import { AesCrypto, Poller } from '@/utils'
import { useDataContestAuthFetch, useDataContestFetch, useDataContestProblemFetch, useDataSubmitFetch } from '@/composables/v1'
import type { DataTableColumns, ImageInst } from 'naive-ui'
import { NAvatar, NButton, NEllipsis, NSpace, NTag, NText, NTime } from 'naive-ui'
import { Icon } from '@iconify/vue'

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
    title: '状态',
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
    title: '编号',
    key: 'problemCode',
    width: 20,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '题目',
    key: 'problemIdName',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 20,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        // disabled: detailData.value.setType === 2 ? detailData.value.timeStatus !== 2 : false,
        onClick: () => {
          router.push({
            name: 'contest_submit',
            query: { contestId: AesCrypto.encrypt(routeContestId), problemId: AesCrypto.encrypt(row.problemId) },
          })
        },
      }, { default: () => '开始' })
    },
  },
]
const submitColumns: DataTableColumns<any> = [
  {
    title: '题目',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
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
    title: '状态',
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
    title: '编程语言',
    key: 'languageName',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.languageName })
    },
  },
  {
    title: '长度(byte)',
    key: 'codeLength',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.codeLength })
    },
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
    width: 90,
    render: (row) => {
      return h(NTag, { size: 'small', type: row.submitType ? 'info' : 'warning' }, { default: () => row.submitTypeName })
    },
  },
  {
    title: '耗时(ms)',
    key: 'maxTime',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxTime })
    },
  },
  {
    title: '内存(Kb)',
    key: 'maxMemory',
    width: 80,
    render: (row) => {
      return h(NTag, { size: 'small' }, { default: () => row.maxMemory })
    },
  },
  {
    title: '提交时间',
    key: 'createTime',
    width: 100,
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 100,
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
    title: '签名',
    key: 'quote',
    ellipsis: {
      tooltip: true,
    },
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

    if (data.isPublic) {
      console.log('公开竞赛')
    }
    else {
      console.log('私密竞赛')
    }
  }

  // 竞赛题目获取
  useDataContestProblemFetch().dataContestProblemList({
    contestId: routeContestId,
  }).then(({ data }) => {
    setProblemPageData.value = data
    console.log(data)
  })

  // 竞赛提交历史获取
  useDataSubmitFetch().dataModuleSubmitPage(submitPageParam.value).then(({ data }) => {
    submitPageData.value = data
  })
  // TODO 竞赛用户获取
  isLoading.value = false
}
loadData()

const imageRef = ref<ImageInst>()
function handleClickImage() {
  imageRef.value?.showPreview()
}

// 倒计时相关
const countdownDuration = ref(0)
const countdownActive = ref(false)
const countdownType = ref<'start' | 'end'>('start')

// 计算倒计时持续时间
function calculateCountdown() {
  const now = Date.now()
  const startTime = Number(detailData.value.startTime)
  const endTime = Number(detailData.value.endTime)

  // 如果当前时间在开始时间之前，显示开始倒计时
  if (now < startTime) {
    countdownDuration.value = startTime - now
    countdownType.value = 'start'
    countdownActive.value = true
  }
  // 如果当前时间在开始时间和结束时间之间，显示结束倒计时
  else if (now >= startTime && now < endTime) {
    countdownDuration.value = endTime - now
    countdownType.value = 'end'
    countdownActive.value = true
  }
  // 如果已经结束，停止倒计时
  else {
    countdownActive.value = false
  }
}

// 监听detailData变化，计算倒计时
watch(() => detailData.value, () => {
  calculateCountdown()
})

// 每秒更新一次倒计时（可选）
const countdownPoller = new Poller(
  {
    interval: 1000, // 1秒轮询一次
    immediate: true,
  },
  {
    onPoll: () => {
      calculateCountdown()
    },
  },
)

// 自定义倒计时显示格式 - 时分秒
function renderCountdown({ hours, minutes, seconds }: { hours: number, minutes: number, seconds: number }) {
  return `${hours.toString().padStart(2, '0')} 时 ${minutes.toString().padStart(2, '0')} 分 ${seconds.toString().padStart(2, '0')} 秒`
}
onMounted(() => {
  if (detailData.value?.setType === 2) {
    countdownPoller.start()
  }
})

onUnmounted(() => {
  countdownPoller.stop()
})

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
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

function handlePasswordSubmit() {
  passwordFormRef.value.validate((errors: any) => {
    if (!errors) {
      passwordFormLoading.value = true

      useDataContestAuthFetch().dataContestAuth(passwordFormValue.value).then(({ success }) => {
        if (success) {
          passwordFormLoading.value = false
          // 刷新页面
          window.location.reload()
        }
        else {
          passwordFormLoading.value = false
        }
      })
    }
  })
}
</script>

<template>
  <!-- 加载状态 -->
  <template v-if="isLoading">
    <main class="container mx-auto px-2 py-6">
      <n-flex class="min-h-[calc(100vh-10rem-12rem)]" align="center" justify="center" vertical>
        <n-spin size="large" />
        <NText>正在加载竞赛数据...</NText>
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
                <!-- 手机：只有上部圆角 -->
                  rounded-t-xl rounded-tr-xl rounded-tl-xl
                  rounded-br-none rounded-bl-none
                  <!-- 电脑：只有左侧圆角 -->
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
                        <NButton type="primary">
                          报名
                        </NButton>
                      </n-flex>
                    </template>
                    <template #description>
                      <NSpace vertical>
                        <n-flex>
                          <NTag size="small" type="error">
                            {{ detailData?.isPublic ? '公开竞赛' : '私密竞赛' }}
                          </NTag>
                          <NTag size="small" type="warning">
                            {{ detailData?.contestTypeName }}
                          </NTag>
                          <NTag size="small" type="success">
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
        <!-- 左侧主内容 -->

        <!-- 左侧主内容 -->
        <n-gi span="1 l:5">
          <!-- 公告内容 -->
          <NSpace vertical :size="24">
            <n-card class="rounded-xl" size="small">
              <n-tabs type="line" animated>
                <n-tab-pane name="problems" tab="题目">
                  <!-- 题目筛选和搜索 -->
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
                <n-tab-pane name="submissions" tab="提交">
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
                      label: `${(i + 1) * 10} 每页`,
                      value: (i + 1) * 10,
                    }))"
                    :page-slot="3"
                    class="flex justify-center items-center pt-6"
                    @update:page="loadData"
                    @update:page-size="loadData"
                  />
                </n-tab-pane>
                <!-- <n-tab-pane name="users" tab="用户">
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
                      label: `${(i + 1) * 10} 每页`,
                      value: (i + 1) * 10,
                    }))"
                    :page-slot="3"
                    class="flex justify-center items-center pt-6"
                    @update:page="loadData"
                    @update:page-size="loadData"
                  />
                </n-tab-pane> -->
              </n-tabs>
            </n-card>
          </NSpace>
        </n-gi>

        <n-gi span="1 l:2">
          <!-- 公告内容 -->
          <NSpace vertical :size="24">
            <n-card class="rounded-xl" size="small">
              <template #header>
                <n-h2 class="pb-0 mb-0">
                  竞赛信息与统计
                </n-h2>
              </template>
              <NSpace vertical class="mb-4">
                <!-- 倒计时显示 -->
                <NSpace v-if="countdownActive" align="center" :size="0">
                  <NText depth="3">
                    {{ countdownType === 'start' ? '开始倒计时：' : '结束倒计时：' }}
                  </NText>
                  <NCountdown
                    :duration="countdownDuration"
                    :active="countdownActive"
                    :render="renderCountdown"
                    @finish="onfinishTime()"
                  />
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    报名开始时间：
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.registerStartTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    报名结束时间：
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.registerEndTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    竞赛开始时间：
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.contestStartTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    竞赛结束时间：
                  </NText>
                  <NText>
                    <NTime :time="Number(detailData?.contestEndTime) || 0" />
                  </NText>
                </NSpace>
                <NSpace align="center" :size="0">
                  <NText depth="3">
                    题目数量：
                  </NText>
                  <NText>
                    {{ setProblemPageData?.length ? setProblemPageData.length : 0 }}
                  </NText>
                </NSpace>
                <!-- <NSpace align="center" :size="0">
                  <NText depth="3">
                    参与人数：
                  </NText>
                  <NText>
                    {{ detailData?.participantUserCount ? detailData?.participantUserCount : 0 }}
                  </NText>
                </NSpace> -->
                <n-alert show-icon type="warning">
                  竞赛请注意时间及时刷新页面：开始时间到达时请刷新页面，结束时间到达将锁定竞赛，无法提交。
                </n-alert>
              </NSpace>
            </n-card>
          </NSpace>
        </n-gi>
      </n-grid>
    </main>
  </template>
  <template v-else>
    <main class="container mx-auto px-2 py-6">
      <n-flex class="min-h-[calc(100vh-10rem-12rem)]" align="center" justify="center" vertical>
        <n-card class="w-full max-w-md" title="私密竞赛">
          <n-form ref="passwordFormRef" :model="passwordFormValue" :rules="rules">
            <n-form-item path="password" label="访问密码">
              <n-input
                v-model:value="passwordFormValue.password"
                type="password"
                placeholder="请输入访问密码"
                @keydown.enter="handlePasswordSubmit"
              />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-flex justify="end">
              <NButton type="primary" :loading="passwordFormLoading" @click="handlePasswordSubmit">
                进入竞赛
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
