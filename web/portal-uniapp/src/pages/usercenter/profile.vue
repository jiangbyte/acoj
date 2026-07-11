<template>
  <Layout title="个人资料" back>
    <view>
      <u-card>
        <template #head>
          <view class="profile-head">
            <u-avatar :src="avatarUrl" :text="avatarText"></u-avatar>
            <view class="profile-head__content">
              <text>{{ displayName }}</text>
              <u-button
                text="更换头像"
                plain
                :loading="uploading"
                @click="chooseAvatar"
              ></u-button>
            </view>
          </view>
        </template>
      </u-card>

      <u-card title="基础资料">
        <template #body>
          <u-form :model="form">
            <u-form-item>
              <view class="form-field">
                <text>账号</text>
                <u-input
                  v-model="accountText"
                  disabled
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>姓名</text>
                <u-input
                  v-model="form.name"
                  placeholder="请输入姓名"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>昵称</text>
                <u-input
                  v-model="form.nickname"
                  placeholder="请输入昵称"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>签名</text>
                <u-textarea
                  v-model="form.signature"
                  placeholder="请输入签名"
                  border="surround"
                ></u-textarea>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>简介</text>
                <u-textarea
                  v-model="form.bio"
                  placeholder="请输入简介"
                  border="surround"
                ></u-textarea>
              </view>
            </u-form-item>
          </u-form>
        </template>
        <template #foot>
          <u-button
            text="保存"
            type="primary"
            :loading="saving"
            @click="saveProfile"
          ></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { resolveFileUrl } from '@/utils/format'

const authStore = useAuthStore()
const saving = ref(false)
const uploading = ref(false)
const accountText = ref('')
const form = reactive({
  name: '',
  nickname: '',
  signature: '',
  bio: '',
})

const displayName = computed(
  () => form.nickname || form.name || accountText.value || '门户用户'
)
const avatarUrl = computed(() =>
  resolveFileUrl(
    authStore.userInfo?.avatar || authStore.userInfo?.profile?.avatar
  )
)
const avatarText = computed(() => displayName.value.slice(0, 1))

onShow(async () => {
  if (!authStore.isLogin) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  await loadMe()
})

async function loadMe() {
  const data = await authStore.refreshUserInfo()
  const profile = data.profile ?? {}
  accountText.value = data.account || ''
  form.name = data.name ?? profile.name ?? ''
  form.nickname = data.nickname ?? profile.nickname ?? ''
  form.signature = profile.signature ?? ''
  form.bio = profile.bio ?? ''
}

async function saveProfile() {
  saving.value = true
  try {
    await authApi.updateUserCenterProfile({
      name: form.name || null,
      nickname: form.nickname || null,
      signature: form.signature || null,
      bio: form.bio || null,
    })
    await loadMe()
    uni.showToast({ title: '已保存', icon: 'success' })
  } finally {
    saving.value = false
  }
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const filePath = res.tempFilePaths?.[0]
      if (!filePath) {
        return
      }
      uploading.value = true
      try {
        await authApi.uploadUserCenterAvatar(filePath)
        await loadMe()
        uni.showToast({ title: '头像已更新', icon: 'success' })
      } finally {
        uploading.value = false
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.profile-head__content,
.form-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}
</style>
