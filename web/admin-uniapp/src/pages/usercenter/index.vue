<template>
  <Layout title="我的">
    <view>
      <u-card>
        <template #head>
          <CardHead
            :title="displayName"
            :sub-title="authStore.userInfo?.account || ''"
          />
        </template>
        <template #body>
          <u-cell-group :border="false">
            <u-cell-item
              title="角色"
              :value="names(authStore.userInfo?.roleIdNames)"
              :arrow="false"
            ></u-cell-item>
            <u-cell-item
              title="部门"
              :value="names(authStore.userInfo?.deptIdNames)"
              :arrow="false"
            ></u-cell-item>
            <u-cell-item
              title="用户组"
              :value="names(authStore.userInfo?.groupIdNames)"
              :arrow="false"
            ></u-cell-item>
          </u-cell-group>
        </template>
      </u-card>

      <u-card :show-head="false">
        <template #body>
          <u-cell-group :border="false">
            <u-cell-item
              title="刷新权限与菜单"
              icon="reload"
              @click="refreshAuth"
            ></u-cell-item>
            <u-cell-item
              title="个人资料"
              icon="account"
              @click="openProfile"
            ></u-cell-item>
            <u-cell-item
              title="退出登录"
              icon="close-circle"
              @click="authStore.logout"
            ></u-cell-item>
          </u-cell-group>
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

const authStore = useAuthStore()
const routeStore = useRouteStore()
const displayName = computed(
  () =>
    authStore.userInfo?.nickname ||
    authStore.userInfo?.name ||
    authStore.userInfo?.account ||
    '管理员'
)

onShow(() => {
  if (!authStore.isLogin) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
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

function openProfile() {
  uni.navigateTo({ url: '/pages/usercenter/profile/index' })
}
</script>
