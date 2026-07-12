<template>
  <Layout title="我的">
    <view>
      <u-card v-if="!authStore.isLogin">
        <template #head>
          <CardHead
            title="访客"
            sub-title="登录后可查看个人资料、空间和消息。"
          />
        </template>
        <template #foot>
          <u-button text="登录" type="primary" @click="openLogin"></u-button>
          <u-button text="注册" plain @click="openRegister"></u-button>
        </template>
      </u-card>

      <u-card v-else>
        <template #head>
          <view class="profile-head">
            <u-avatar :src="avatarUrl" :text="avatarText"></u-avatar>
            <view class="profile-head__content">
              <text>{{ displayName }}</text>
              <text>{{ authStore.userInfo?.account || '-' }}</text>
            </view>
          </view>
        </template>
        <template #body>
          <view class="summary-grid">
            <view>
              <text>{{ names(authStore.userInfo?.roleIdNames) }}</text>
              <text>角色</text>
            </view>
            <view>
              <text>{{ names(authStore.userInfo?.deptIdNames) }}</text>
              <text>部门</text>
            </view>
            <view>
              <text>{{ names(authStore.userInfo?.groupIdNames) }}</text>
              <text>用户组</text>
            </view>
          </view>
        </template>
      </u-card>

      <u-card :show-head="false">
        <template #body>
          <u-grid :col="2" :border="false">
            <u-grid-item
              v-for="item in entries"
              :key="item.text"
              @click="openEntry(item)"
            >
              <u-icon :name="item.icon"></u-icon>
              <text>{{ item.text }}</text>
            </u-grid-item>
          </u-grid>
        </template>
      </u-card>

      <u-card v-if="authStore.isLogin" title="账号">
        <template #body>
          <u-button text="刷新权限与菜单" plain @click="refreshAuth"></u-button>
          <u-button text="退出登录" plain @click="authStore.logout"></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouteStore } from '@/stores/route'
import { refreshDict } from '@/utils/dict'
import { resolveFileUrl } from '@/utils/format'

const authStore = useAuthStore()
const routeStore = useRouteStore()
const displayName = computed(
  () =>
    authStore.userInfo?.nickname ||
    authStore.userInfo?.name ||
    authStore.userInfo?.account ||
    '门户用户'
)
const avatarUrl = computed(() =>
  resolveFileUrl(
    authStore.userInfo?.avatar || authStore.userInfo?.profile?.avatar
  )
)
const avatarText = computed(() => displayName.value.slice(0, 1))
const entries = computed(() => [
  {
    text: '个人资料',
    icon: 'account',
    url: '/pages/usercenter/profile',
    auth: true,
  },
  {
    text: '账号安全',
    icon: 'lock',
    url: '/pages/usercenter/security',
    auth: true,
  },
  { text: '我的空间', icon: 'home', url: '/pages/space/index', auth: false },
  {
    text: '消息中心',
    icon: 'bell',
    url: '/pages/messages/index',
    tab: true,
    auth: true,
  },
])

onShow(() => {
  if (authStore.isLogin) {
    authStore.refreshUserInfo().catch(() => undefined)
  }
})

function names(items?: any[]) {
  return (
    items
      ?.map((item) => item.name)
      .filter(Boolean)
      .join('、') || '-'
  )
}

async function refreshAuth() {
  await authStore.refreshUserInfo()
  await Promise.all([refreshDict(), routeStore.initRoutes()])
  uni.showToast({ title: '已刷新', icon: 'success' })
}

function openLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function openRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function openEntry(item: { url: string; tab?: boolean; auth?: boolean }) {
  if (item.auth && !authStore.isLogin) {
    openLogin()
    return
  }
  if (item.tab) {
    uni.switchTab({ url: item.url })
    return
  }
  uni.navigateTo({ url: item.url })
}
</script>

<style lang="scss" scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.profile-head__content {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 6rpx;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
}

.summary-grid > view {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6rpx;
}
</style>
