<template>
  <Layout title="账号安全" back>
    <view>
      <u-card title="修改密码">
        <template #body>
          <u-form :model="passwordForm">
            <u-form-item>
              <view class="form-field">
                <text>当前密码</text>
                <u-input
                  v-model="passwordForm.old_password"
                  type="password"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>新密码</text>
                <u-input
                  v-model="passwordForm.new_password"
                  type="password"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>确认新密码</text>
                <u-input
                  v-model="passwordForm.confirm_password"
                  type="password"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
          </u-form>
        </template>
        <template #foot>
          <u-button
            text="更新密码"
            type="primary"
            :loading="savingPassword"
            @click="savePassword"
          ></u-button>
        </template>
      </u-card>

      <u-card title="手机号">
        <template #body>
          <u-form :model="phoneForm">
            <u-form-item>
              <view class="form-field">
                <text>手机号</text>
                <u-input
                  v-model="phoneForm.phone"
                  placeholder="请输入手机号"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>允许手机号登录</text>
                <u-switch v-model="phoneForm.phone_login_enabled"></u-switch>
              </view>
            </u-form-item>
          </u-form>
        </template>
        <template #foot>
          <u-button
            text="更新手机号"
            type="primary"
            :loading="savingPhone"
            @click="openConfirm('phone')"
          ></u-button>
        </template>
      </u-card>

      <u-card title="邮箱">
        <template #body>
          <u-form :model="emailForm">
            <u-form-item>
              <view class="form-field">
                <text>邮箱</text>
                <u-input
                  v-model="emailForm.email"
                  placeholder="请输入邮箱"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>允许邮箱登录</text>
                <u-switch v-model="emailForm.email_login_enabled"></u-switch>
              </view>
            </u-form-item>
          </u-form>
        </template>
        <template #foot>
          <u-button
            text="更新邮箱"
            type="primary"
            :loading="savingEmail"
            @click="openConfirm('email')"
          ></u-button>
        </template>
      </u-card>

      <u-popup
        :show="confirmVisible"
        mode="center"
        @close="confirmVisible = false"
      >
        <view class="confirm-panel">
          <text>{{ confirmTitle }}</text>
          <u-input
            v-model="confirmPassword"
            type="password"
            placeholder="请输入当前密码"
            border="surround"
          ></u-input>
          <u-button
            text="确认"
            type="primary"
            :loading="confirmLoading"
            @click="confirmBind"
          ></u-button>
          <u-button
            text="取消"
            plain
            @click="confirmVisible = false"
          ></u-button>
        </view>
      </u-popup>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { encryptPasswords } from '@/utils/security'

const authStore = useAuthStore()
const savingPassword = ref(false)
const savingPhone = ref(false)
const savingEmail = ref(false)
const confirmVisible = ref(false)
const confirmPassword = ref('')
const confirmLoading = ref(false)
const confirmType = ref<'phone' | 'email'>('phone')
const confirmTitle = computed(() =>
  confirmType.value === 'phone' ? '确认更新手机号' : '确认更新邮箱'
)

const passwordForm = reactive({
  old_password: '',
  new_password: '',
  confirm_password: '',
})
const phoneForm = reactive({
  phone: '',
  phone_login_enabled: false,
})
const emailForm = reactive({
  email: '',
  email_login_enabled: false,
})

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
  phoneForm.phone = profile.phone ?? ''
  phoneForm.phone_login_enabled = Boolean(profile.phone_login_enabled)
  emailForm.email = profile.email ?? ''
  emailForm.email_login_enabled = Boolean(profile.email_login_enabled)
}

async function savePassword() {
  if (!passwordForm.old_password || !passwordForm.new_password) {
    uni.showToast({ title: '请完整填写密码', icon: 'none' })
    return
  }
  if (passwordForm.new_password !== passwordForm.confirm_password) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  savingPassword.value = true
  try {
    const encrypted = await encryptPasswords({
      old_password: passwordForm.old_password,
      new_password: passwordForm.new_password,
    })
    await authApi.updateUserCenterPassword({
      old_password: encrypted.values.old_password,
      new_password: encrypted.values.new_password,
      password_key_id: encrypted.password_key_id,
    })
    passwordForm.old_password = ''
    passwordForm.new_password = ''
    passwordForm.confirm_password = ''
    uni.showToast({ title: '密码已更新', icon: 'success' })
  } finally {
    savingPassword.value = false
  }
}

function openConfirm(type: 'phone' | 'email') {
  if (
    type === 'email' &&
    emailForm.email &&
    !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.email)
  ) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return
  }
  confirmType.value = type
  confirmPassword.value = ''
  confirmVisible.value = true
}

async function confirmBind() {
  if (!confirmPassword.value) {
    uni.showToast({ title: '请输入当前密码', icon: 'none' })
    return
  }
  confirmLoading.value = true
  savingPhone.value = confirmType.value === 'phone'
  savingEmail.value = confirmType.value === 'email'
  try {
    const encrypted = await encryptPasswords({
      password: confirmPassword.value,
    })
    if (confirmType.value === 'phone') {
      await authApi.updateUserCenterPhone({
        password: encrypted.values.password,
        password_key_id: encrypted.password_key_id,
        phone: phoneForm.phone || null,
        phone_login_enabled: phoneForm.phone_login_enabled,
      })
    } else {
      await authApi.updateUserCenterEmail({
        password: encrypted.values.password,
        password_key_id: encrypted.password_key_id,
        email: emailForm.email || null,
        email_login_enabled: emailForm.email_login_enabled,
      })
    }
    confirmVisible.value = false
    await loadMe()
    uni.showToast({ title: '已更新', icon: 'success' })
  } finally {
    confirmLoading.value = false
    savingPhone.value = false
    savingEmail.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-field,
.confirm-panel {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}

.confirm-panel {
  width: 620rpx;
  max-width: 86vw;
  padding: 24rpx;
  background: #fff;
}
</style>
