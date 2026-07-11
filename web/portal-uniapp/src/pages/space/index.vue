<template>
  <Layout title="空间">
    <view>
      <u-card v-if="!accountId">
        <template #head>
          <CardHead
            title="公共空间"
            sub-title="登录后可查看并维护自己的门户空间。"
          />
        </template>
        <template #foot>
          <u-button text="登录" type="primary" @click="openLogin"></u-button>
          <u-button text="注册" plain @click="openRegister"></u-button>
        </template>
      </u-card>

      <template v-else>
        <u-card>
          <template #head>
            <view class="space-head">
              <u-avatar :src="avatarUrl" :text="avatarText"></u-avatar>
              <view class="space-head__content">
                <text>{{ displayName }}</text>
                <text>{{ handleText }}</text>
              </view>
            </view>
          </template>
          <template #body>
            <view class="space-profile">
              <text>{{ signatureText }}</text>
              <text>{{ bioText }}</text>
            </view>
          </template>
        </u-card>

        <u-card :show-head="false">
          <template #body>
            <u-tabs
              :list="tabs"
              :current="activeTab"
              @change="changeTab"
            ></u-tabs>
          </template>
        </u-card>

        <u-card v-if="activeTab === 0" title="主页">
          <template #body>
            <u-grid :col="3" :border="false">
              <u-grid-item>
                <text>0</text>
                <text>内容</text>
              </u-grid-item>
              <u-grid-item>
                <text>0</text>
                <text>关注</text>
              </u-grid-item>
              <u-grid-item>
                <text>0</text>
                <text>粉丝</text>
              </u-grid-item>
            </u-grid>
          </template>
        </u-card>

        <u-card v-if="activeTab === 1" title="简介">
          <template #body>
            <view class="space-profile">
              <text>签名</text>
              <text>{{ signatureText }}</text>
              <text>简介</text>
              <text>{{ bioText }}</text>
              <text>等级</text>
              <text>{{ profile.level || '-' }}</text>
            </view>
          </template>
        </u-card>

        <u-card v-if="activeTab === 2" title="动态">
          <template #body>
            <u-empty mode="list" text="暂无公开动态"></u-empty>
          </template>
        </u-card>
      </template>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import { spaceApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { displayValue, resolveFileUrl } from '@/utils/format'

const authStore = useAuthStore()
const queryAccountId = ref('')
const profile = ref<any>({})
const activeTab = ref(0)
const loading = ref(false)
const tabs = [{ name: '主页' }, { name: '简介' }, { name: '动态' }]

const accountId = computed(
  () => queryAccountId.value || authStore.userInfo?.accountId || ''
)
const displayName = computed(
  () =>
    profile.value.nickname ||
    profile.value.name ||
    authStore.userInfo?.account ||
    '门户用户'
)
const avatarUrl = computed(() => resolveFileUrl(profile.value.avatar))
const avatarText = computed(() => displayName.value.slice(0, 1))
const handleText = computed(() =>
  profile.value.account_id ? `ID ${profile.value.account_id}` : '-'
)
const signatureText = computed(() => displayValue(profile.value.signature))
const bioText = computed(() => displayValue(profile.value.bio))

onLoad((query: any) => {
  queryAccountId.value = String(query?.accountId || '')
})

onShow(loadSpace)

onPullDownRefresh(async () => {
  await loadSpace()
  uni.stopPullDownRefresh()
})

async function loadSpace() {
  if (!accountId.value || loading.value) {
    return
  }
  loading.value = true
  try {
    profile.value = await spaceApi.detail(accountId.value)
  } finally {
    loading.value = false
  }
}

function changeTab(event: number | { index?: number; name?: string }) {
  if (typeof event === 'number') {
    activeTab.value = event
    return
  }
  if (typeof event.index === 'number') {
    activeTab.value = event.index
    return
  }
  const index = tabs.findIndex((item) => item.name === event.name)
  activeTab.value = index >= 0 ? index : activeTab.value
}

function openLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function openRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}
</script>

<style lang="scss" scoped>
.space-head {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.space-head__content,
.space-profile {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}
</style>
