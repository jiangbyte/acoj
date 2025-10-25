<script setup lang="ts">
import { useSysLogFetch, useSysUserFetch, useTodayTotalFetch } from '../../composables/v1'
import ProblemCreateForm from '../modular/data/problem/form.vue'
import SetCreateForm from '../modular/data/set/form.vue'
import CategoryCreateForm from '../modular/sys/category/form.vue'
import TagCreateForm from '../modular/sys/tag/form.vue'

defineOptions({
  inheritAttrs: false,
})

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

// 获取问候语
function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 6)
    return '深夜好'
  if (hour < 12)
    return '上午好'
  if (hour < 14)
    return '中午好'
  if (hour < 18)
    return '下午好'
  return '晚上好'
}

// 当前时间响应式变量
const currentTime = ref()

// 更新时间函数
function updateTime() {
  currentTime.value = Date.now()
}

// 定时器
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

// 定义所有可能的按钮
const allButtons = [
  {
    permission: '/data/problem/add',
    text: '题目创建',
    type: 'primary' as const,
    action: () => problemCreateFormRef.value.doOpen(null, false),
  },
  {
    permission: '/data/set/add',
    text: '题集创建',
    type: 'success' as const,
    action: () => setCreateFormRef.value.doOpen(null, false),
  },
  {
    permission: '/sys/category/add',
    text: '分类创建',
    type: 'info' as const,
    action: () => categoryCreateFormRef.value.doOpen(null, false),
  },
  {
    permission: '/sys/tag/add',
    text: '标签创建',
    type: 'warning' as const,
    action: () => tagCreateFormRef.value.doOpen(null, false),
  },
]

// 计算可见的按钮
const visibleButtons = computed(() => {
  return allButtons.filter(button => checkPermission(button.permission))
})

// 定义所有可能的快捷功能
const allQuickActions = [
  {
    path: '/problem/list',
    text: '题目管理',
    type: 'primary' as const,
    permission: '/data/problem/add', // 根据实际权限配置
  },
  {
    path: '/system/user',
    text: '用户管理',
    type: 'info' as const,
    permission: '/sys/user/add',
  },
  {
    path: '/set/list',
    text: '题集管理',
    type: 'success' as const,
    permission: '/data/set/add',
  },
  {
    path: '/system/log',
    text: '系统日志',
    type: 'warning' as const,
    permission: '/sys/log/add',
  },
]

// 计算有权限的快捷功能
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
  <div class="flex flex-col h-full w-full">
    <!-- 欢迎语区域 -->
    <NCard size="small">
      <NSpace vertical>
        <NH1 style="margin: 0; color: var(--primary-color); font-size: 28px;">
          {{ getGreeting() }}，{{ userInfo?.nickname }}！👋
        </NH1>
        <NSpace vertical>
          <NText depth="2" style="font-size: 16px;">
            欢迎回到 OJ 管理控制台
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
            当前时间：<n-time :time="currentTime" /> | 上次登录时间：<n-time :time="Number(userInfo.loginTime)" />
          </NText>
        </NSpace>
      </NSpace>
    </NCard>

    <!-- 主要内容区域 -->
    <NCard size="small" class="flex-1">
      <NGrid :x-gap="16" :y-gap="16">
        <NGi :span="16">
          <NSpace vertical :size="12">
            <!-- 快速操作 -->
            <NCard title="快速操作" size="small">
              <NGrid :cols="6" :x-gap="8" :y-gap="8">
                <NGi v-for="button in visibleButtons" :key="button.permission">
                  <NButton :type="button.type" block @click="button.action">
                    {{ button.text }}
                  </NButton>
                </NGi>
                <NGi v-if="visibleButtons.length === 0">
                  暂无快速操作
                </NGi>
              </NGrid>
            </NCard>

            <!-- 最近活动 -->
            <NCard title="最近活动" size="small">
              <NList v-if="recentlogs" hoverable size="small">
                <NListItem v-for="item in recentlogs" :key="item?.id">
                  <div class="flex items-center gap-2 justify-between">
                    <n-space align="center">
                      <n-space align="center" size="small">
                        <n-avatar size="small" round :src="item?.userAvatar" />
                        <n-text>{{ item?.userIdName }}</n-text>
                      </n-space>
                      <n-text>{{ item?.operation || '无操作' }}</n-text>
                    </n-space>
                    <n-space align="center" size="small">
                      <NTag
                        :bordered="false"
                        size="small"
                        type="info"
                      >
                        {{ item?.category || '未知' }}
                      </NTag>
                      <n-time :time="Number(item.operationTime)" />
                    </n-space>
                  </div>
                </NListItem>
              </NList>
              <n-empty v-else description="暂无数据" />
            </NCard>
          </NSpace>
        </NGi>

        <NGi :span="8">
          <NSpace vertical :size="12">
            <!-- 快捷功能 -->
            <NCard title="快捷功能" size="small">
              <NSpace vertical :size="10">
                <!-- <NButton type="primary" ghost block @click="$router.push('/problem/list')">
                  题目管理
                </NButton>
                <NButton type="info" ghost block @click="$router.push('/system/user')">
                  用户管理
                </NButton>
                <NButton type="success" ghost block @click="$router.push('/set/list')">
                  题集管理
                </NButton>
                <NButton type="warning" ghost block @click="$router.push('/system/log')">
                  系统日志
                </NButton> -->
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
                  暂无可用功能
                </div>
              </NSpace>
            </NCard>

            <!-- 今日统计 -->
            <NCard title="今日统计" size="small">
              <NSpace vertical :size="12">
                <NCard content-style="padding: 16px;" size="small">
                  <NSpace vertical>
                    <NSpace justify="space-between" align="center">
                      <NText depth="2">
                        新提交
                      </NText>
                      <NText style="font-size: 18px; color: var(--info-color);">
                        {{ todayTotal?.todaySubmitCount ? todayTotal.todaySubmitCount : 0 }}
                      </NText>
                    </NSpace>
                    <NSpace justify="space-between" align="center">
                      <NText depth="2">
                        新用户
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
</template>

<style scoped>
/* 可以添加一些自定义样式 */
</style>
