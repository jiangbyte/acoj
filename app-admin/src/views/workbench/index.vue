<script setup lang="ts">
import { useSysLogFetch, useSysUserFetch, useTodayTotalFetch } from '../../composables/v1'
import ProblemCreateForm from '../modular/data/problem/form.vue'
import SetCreateForm from '../modular/data/set/form.vue'
import CategoryCreateForm from '../modular/sys/category/form.vue'
import TagCreateForm from '../modular/sys/tag/form.vue'

const { sysUserDefaultData } = useSysUserFetch()
const userInfo = ref(sysUserDefaultData)

const problemCreateFormRef = ref()
const setCreateFormRef = ref()
const categoryCreateFormRef = ref()
const tagCreateFormRef = ref()

const recentlogs = ref()
const todayTotal = ref()
function loadData() {
  useSysLogFetch().sysLogRecent().then(({ data }) => {
    recentlogs.value = data
    console.log(data)
  })
  useSysUserFetch().getProfile().then(({ data }) => {
    userInfo.value = data
    console.log(data)

    if (!data.loginTime) {
      userInfo.value.loginTime = Date.now()
    }
  })
  useTodayTotalFetch().getTodayTotal().then(({ data }) => {
    todayTotal.value = data
  })
}
loadData()

// Get the greeting
function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 6)
    return 'Good evening'
  if (hour < 12)
    return 'Good morning'
  if (hour < 14)
    return 'Good afternoon'
  if (hour < 18)
    return 'Good afternoon'
  return 'Good evening'
}

// Reactive current-time value
const currentTime = ref()

// Update-time function
function updateTime() {
  currentTime.value = Date.now()
}

// Timer
let timer: any = null

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

// Define all available buttons
const allButtons = [
  {
    permission: '/data/problem/add',
    text: 'ProblemCreate',
    type: 'primary' as const,
    action: () => problemCreateFormRef.value.doOpen(null, false),
  },
  {
    permission: '/data/set/add',
    text: 'Problem SetCreate',
    type: 'success' as const,
    action: () => setCreateFormRef.value.doOpen(null, false),
  },
  {
    permission: '/sys/category/add',
    text: 'CategoryCreate',
    type: 'info' as const,
    action: () => categoryCreateFormRef.value.doOpen(null, false),
  },
  {
    permission: '/sys/tag/add',
    text: 'TagCreate',
    type: 'warning' as const,
    action: () => tagCreateFormRef.value.doOpen(null, false),
  },
]

// Compute visible buttons
const visibleButtons = computed(() => {
  return allButtons.filter(button => checkPermission(button.permission))
})

// Define all available shortcuts
const allQuickActions = [
  {
    path: '/problem/list',
    text: 'Problem Management',
    type: 'primary' as const,
    permission: '/data/problem/add', // Match the actual permission configuration
  },
  {
    path: '/set/list',
    text: 'Problem Set Management',
    type: 'success' as const,
    permission: '/data/set/add',
  },
  {
    path: '/system/user',
    text: 'User Management',
    type: 'info' as const,
    permission: '/sys/user/add',
  },
  {
    path: '/system/log',
    text: 'System Logs',
    type: 'warning' as const,
    permission: '/sys/log/add',
  },
]

// Compute permitted shortcuts
const quickActions = computed(() => {
  return allQuickActions.filter(action => checkPermission(action.permission))
})

function checkPermission(permission: string) {
  try {
    return (userInfo.value?.permissions as string[] | undefined)?.includes(permission) ?? false
  }
  catch {
    return false
  }
}
</script>

<template>
  <!-- 单根节点承接布局下发的 h-full；自身纵向滚动，避免 flex-1 把内容压死无法滚动 -->
  <div class="workbench-page flex flex-col w-full h-full min-h-0 overflow-y-auto">
    <div class="flex flex-col gap-3 w-full pb-2">
      <!-- Greeting area -->
      <NCard size="small">
        <NSpace vertical>
          <NH1 style="margin: 0; color: var(--primary-color); font-size: 28px;">
            {{ getGreeting() }}, {{ userInfo?.nickname }}! 👋
          </NH1>
          <NSpace vertical>
            <NText depth="2" style="font-size: 16px;">
              Welcome back to the OJ admin console
            </NText>
            <NText v-if="userInfo?.groupIdName" depth="3" style="font-size: 14px;">
              <n-tag size="small">
                {{ userInfo?.groupIdName }}
              </n-tag>
            </NText>
            <NText v-if="userInfo?.roleNames" depth="3" style="font-size: 14px;">
              <n-space>
                <n-tag v-for="(item, index) in userInfo?.roleNames" :key="index" size="small">
                  {{ item }}
                </n-tag>
              </n-space>
            </NText>
            <NText depth="3" style="font-size: 14px;">
              Current Time: <n-time :time="currentTime" /> | Last Sign In: <n-time :time="Number(userInfo.loginTime)" />
            </NText>
          </NSpace>
        </NSpace>
      </NCard>

      <!-- Main content area：按内容撑开，由外层 overflow-y-auto 滚动 -->
      <NCard size="small">
        <NGrid :x-gap="16" :y-gap="16">
          <NGi :span="16">
            <NSpace vertical :size="12">
              <!-- Quick Actions -->
              <NCard title="Quick Actions" size="small">
                <NGrid :cols="6" :x-gap="8" :y-gap="8">
                  <NGi v-for="button in visibleButtons" :key="button.permission">
                    <NButton :type="button.type" block @click="button.action">
                      {{ button.text }}
                    </NButton>
                  </NGi>
                  <NGi v-if="visibleButtons.length === 0">
                    No quick actions
                  </NGi>
                </NGrid>
              </NCard>

              <!-- Recent Activity -->
              <NCard title="Recent Activity" size="small">
                <NList v-if="recentlogs" hoverable size="small">
                  <NListItem v-for="item in recentlogs" :key="item?.id">
                    <div class="flex items-center gap-2 justify-between">
                      <n-space align="center">
                        <n-space align="center" size="small">
                          <n-avatar size="small" round :src="item?.userAvatar" />
                          <n-text>{{ item?.userIdName }}</n-text>
                        </n-space>
                        <n-text>{{ item?.operation || 'No action' }}</n-text>
                      </n-space>
                      <n-space align="center" size="small">
                        <NTag
                          :bordered="false"
                          size="small"
                          type="info"
                        >
                          {{ item?.category || 'Unknown' }}
                        </NTag>
                        <n-time :time="Number(item.operationTime)" />
                      </n-space>
                    </div>
                  </NListItem>
                </NList>
                <n-empty v-else description="No data" />
              </NCard>
            </NSpace>
          </NGi>

          <NGi :span="8">
            <NSpace vertical :size="12">
              <!-- Shortcuts -->
              <NCard title="Shortcuts" size="small">
                <NSpace vertical :size="10">
                  <NButton
                    v-for="item in quickActions"
                    :key="item.path"
                    :type="item.type"
                    ghost
                    block
                    @click="$router.push(item.path)"
                  >
                    {{ item.text }}
                  </NButton>
                  <div v-if="quickActions.length === 0" class="text-center text-gray-400">
                    No features available
                  </div>
                </NSpace>
              </NCard>

              <!-- Today -->
              <NCard title="Today" size="small">
                <NSpace vertical :size="12">
                  <NCard content-style="padding: 16px;" size="small">
                    <NSpace vertical>
                      <NSpace justify="space-between" align="center">
                        <NText depth="2">
                          New Submissions
                        </NText>
                        <NText style="font-size: 18px; color: var(--info-color);">
                          {{ todayTotal?.todaySubmitCount ? todayTotal.todaySubmitCount : 0 }}
                        </NText>
                      </NSpace>
                      <NSpace justify="space-between" align="center">
                        <NText depth="2">
                          New Users
                        </NText>
                        <NText style="font-size: 18px; color: var(--primary-color);">
                          {{ todayTotal?.todayNewUserCount ? todayTotal.todayNewUserCount : 0 }}
                        </NText>
                      </NSpace>
                    </NSpace>
                  </NCard>
                </NSpace>
              </NCard>
            </NSpace>
          </NGi>
        </NGrid>
      </NCard>
    </div>

    <ProblemCreateForm ref="problemCreateFormRef" />
    <SetCreateForm ref="setCreateFormRef" />
    <CategoryCreateForm ref="categoryCreateFormRef" />
    <TagCreateForm ref="tagCreateFormRef" />
  </div>
</template>
