<template>
  <Layout title="首页">
    <view>
      <u-card>
        <template #head>
          <CardHead
            title="Enterprise Portal"
            sub-title="One entry for teams, workflows, and services."
          />
        </template>
        <template #body>
          <u-swiper
            v-if="banners.length"
            :list="bannerImages"
            @click="openBanner"
          ></u-swiper>
          <view class="status-line">
            <text>Portal Access</text>
            <text>{{ authStore.isLogin ? displayName : 'Guest' }}</text>
          </view>
        </template>
        <template #foot>
          <u-button
            :text="authStore.isLogin ? 'Open User Center' : 'Sign In'"
            type="primary"
            @click="openPrimary"
          ></u-button>
          <u-button
            v-if="!authStore.isLogin"
            text="Register"
            plain
            @click="openRegister"
          ></u-button>
        </template>
      </u-card>

      <u-card title="服务入口">
        <template #body>
          <u-grid :col="3" :border="false">
            <u-grid-item
              v-for="item in entries"
              :key="item.key"
              @click="openEntry(item)"
            >
              <u-icon :name="item.icon"></u-icon>
              <text>{{ item.name }}</text>
            </u-grid-item>
          </u-grid>
        </template>
      </u-card>

      <u-card title="核心能力">
        <template #body>
          <view class="feature-list">
            <view v-for="item in features" :key="item.title">
              <text>{{ item.title }}</text>
              <text>{{ item.description }}</text>
            </view>
          </view>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import { bannerApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { useRouteStore, type ResourceItem } from '@/stores/route'

const authStore = useAuthStore()
const dictStore = useDictStore()
const routeStore = useRouteStore()
const banners = ref<any[]>([])

const displayName = computed(
  () =>
    authStore.userInfo?.nickname ||
    authStore.userInfo?.name ||
    authStore.userInfo?.account ||
    'Portal User'
)
const bannerImages = computed(() =>
  banners.value
    .map((item) => item.image || item.image_url || item.url)
    .filter(Boolean)
)
const entries = computed(() => {
  const configured = routeStore.headerResources.map((item) => ({
    key: item.id,
    name: item.name,
    icon: iconName(item.icon || item.code),
    resource: item,
  }))
  return configured.length
    ? configured
    : [
        {
          key: 'messages',
          name: '消息',
          icon: 'bell',
          path: '/pages/messages/index',
        },
        {
          key: 'usercenter',
          name: '我的',
          icon: 'setting',
          path: '/pages/usercenter/index',
        },
      ]
})
const features = [
  {
    title: 'Workflow Access',
    description: 'Business workflows and personal work entry points.',
  },
  {
    title: 'Team Collaboration',
    description: 'Messages, notifications, and todos in one place.',
  },
  {
    title: 'Public Space',
    description: 'Profile, intro, and public information display.',
  },
]

onShow(bootstrap)

onPullDownRefresh(async () => {
  await bootstrap()
  uni.stopPullDownRefresh()
})

async function bootstrap() {
  await Promise.all([
    loadBanners(),
    routeStore.initRoutes().catch(() => undefined),
    dictStore.refreshDict().catch(() => undefined),
  ])
}

async function loadBanners() {
  banners.value = await bannerApi
    .list({ position: 'TOP', category: 'HOME' })
    .catch(() => [])
}

function openPrimary() {
  if (authStore.isLogin) {
    uni.switchTab({ url: '/pages/usercenter/index' })
  } else {
    uni.navigateTo({ url: '/pages/auth/login' })
  }
}

function openRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function openBanner(index: number) {
  const banner = banners.value[index]
  if (banner?.id) {
    bannerApi.interaction({ id: banner.id }).catch(() => undefined)
  }
}

function openEntry(item: { path?: string; resource?: ResourceItem }) {
  if (item.resource) {
    routeStore.openResource(item.resource)
    return
  }
  if (item.path) {
    if (
      item.path.includes('/pages/home/') ||
      item.path.includes('/pages/messages/') ||
      item.path.includes('/pages/usercenter/')
    ) {
      uni.switchTab({ url: item.path })
      return
    }
    uni.navigateTo({ url: item.path })
  }
}

function iconName(value?: string | null) {
  const text = String(value || '').toLowerCase()
  if (text.includes('message')) return 'bell'
  if (text.includes('space') || text.includes('user')) return 'account'
  if (text.includes('home')) return 'home'
  return 'grid'
}
</script>

<style lang="scss" scoped>
.status-line,
.feature-list > view {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6rpx;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}
</style>
