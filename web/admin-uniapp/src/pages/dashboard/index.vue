<template>
  <Layout title="首页">
    <view>
      <u-card>
        <template #head>
          <CardHead
            :title="`你好，${displayName}`"
            sub-title="管理端数据总览"
          />
        </template>
        <template #body>
          <u-cell-item
            title="账号"
            :value="authStore.userInfo?.account || '-'"
            :arrow="false"
          ></u-cell-item>
        </template>
      </u-card>

      <u-card title="核心指标">
        <template #body>
          <u-grid :col="2" :border="false">
            <u-grid-item v-for="metric in metrics" :key="metric.key">
              <text>{{ metric.key }}</text>
              <text>{{ metric.value }}</text>
              <text>趋势 {{ metric.trend_value ?? 0 }}</text>
            </u-grid-item>
          </u-grid>
        </template>
      </u-card>
      <u-card title="账号趋势">
        <template #body>
          <u-cell-group :border="false">
            <u-cell-item
              v-for="item in accountTrend"
              :key="`${item.date}-${item.type}`"
              :title="item.date"
              :value="String(item.value)"
              :arrow="false"
            >
              <u-line-progress
                :percent="percent(item.value, maxTrendValue)"
                :show-percent="false"
              ></u-line-progress>
            </u-cell-item>
          </u-cell-group>
        </template>
        <u-empty
          v-if="!accountTrend.length"
          mode="list"
          text="暂无趋势数据"
        ></u-empty>
      </u-card>

      <u-card title="文件类型分布">
        <template #body>
          <u-cell-group :border="false">
            <u-cell-item
              v-for="item in fileTypeShare"
              :key="item.name"
              :title="item.name"
              :value="String(item.value)"
              :arrow="false"
            >
              <u-line-progress
                :percent="percent(item.value, maxFileValue)"
                :show-percent="false"
              ></u-line-progress>
            </u-cell-item>
          </u-cell-group>
        </template>
        <u-empty
          v-if="!fileTypeShare.length"
          mode="list"
          text="暂无文件数据"
        ></u-empty>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import { dashboardApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { useRouteStore } from '@/stores/route'

const authStore = useAuthStore()
const dictStore = useDictStore()
const routeStore = useRouteStore()
const metrics = ref<any[]>([])
const accountTrend = ref<any[]>([])
const fileTypeShare = ref<any[]>([])

const displayName = computed(
  () =>
    authStore.userInfo?.nickname ||
    authStore.userInfo?.name ||
    authStore.userInfo?.account ||
    '管理员'
)

const maxTrendValue = computed(() =>
  Math.max(...accountTrend.value.map((item) => Number(item.value) || 0), 1)
)
const maxFileValue = computed(() =>
  Math.max(...fileTypeShare.value.map((item) => Number(item.value) || 0), 1)
)

onShow(async () => {
  if (!authStore.isLogin) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
    return
  }
  await bootstrap()
})

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
})

async function bootstrap() {
  if (!dictStore.loaded) {
    await dictStore.refreshDict()
  }
  if (!routeStore.modules.length) {
    await routeStore.initRoutes()
  }
  await refresh()
}

async function refresh() {
  const overview = await dashboardApi.overview().catch(() => ({
    metrics: [],
    account_trend: [],
    file_type_share: [],
  }))
  metrics.value = overview.metrics ?? []
  accountTrend.value = overview.account_trend ?? []
  fileTypeShare.value = overview.file_type_share ?? []
}

function percent(value: number | string, max: number) {
  const ratio = Math.max(0, Math.min(100, (Number(value) / max) * 100))
  return Math.round(ratio)
}
</script>
