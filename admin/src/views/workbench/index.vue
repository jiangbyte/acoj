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
                <NGi>
                  <NButton type="primary" block @click="problemCreateFormRef.doOpen(null, false)">
                    题目创建
                  </NButton>
                </NGi>
                <NGi>
                  <NButton type="success" block @click="setCreateFormRef.doOpen(null, false)">
                    题集创建
                  </NButton>
                </NGi>
                <NGi>
                  <NButton type="info" block @click="categoryCreateFormRef.doOpen(null, false)">
                    分类创建
                  </NButton>
                </NGi>
                <NGi>
                  <NButton type="warning" block @click="tagCreateFormRef.doOpen(null, false)">
                    标签创建
                  </NButton>
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
                <NButton type="primary" ghost block @click="$router.push('/problem/list')">
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
                </NButton>
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
