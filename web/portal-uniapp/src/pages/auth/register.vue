<template>
  <Layout title="注册" back>
    <view class="auth-page">
      <u-card>
        <template #head>
          <CardHead
            title="Create Portal Account"
            sub-title="Create an account with email login enabled."
          />
        </template>
        <template #body>
          <u-form :model="form">
            <u-form-item>
              <view class="form-field">
                <text>Account</text>
                <u-input
                  v-model="form.account"
                  placeholder="Enter account"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>Nickname</text>
                <u-input
                  v-model="form.nickname"
                  placeholder="Enter nickname"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>Email</text>
                <u-input
                  v-model="form.email"
                  placeholder="Enter email"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>Password</text>
                <u-input
                  v-model="form.password"
                  type="password"
                  placeholder="At least 8 characters"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>Confirm Password</text>
                <u-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="Enter password again"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>Captcha</text>
                <CaptchaField
                  ref="captchaRef"
                  v-model:captcha-id="form.captcha_id"
                  v-model:captcha-value="form.captcha_value"
                />
              </view>
            </u-form-item>
          </u-form>
        </template>
        <template #foot>
          <u-button
            text="Register"
            type="primary"
            :loading="loading"
            @click="submit"
          ></u-button>
          <u-button text="Back to Sign In" plain @click="openLogin"></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import CaptchaField from '@/components/common/CaptchaField.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const captchaRef = ref<InstanceType<typeof CaptchaField> | null>(null)
const loading = ref(false)
const form = reactive({
  account: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  captcha_id: '',
  captcha_value: '',
})

async function submit() {
  if (
    !form.account ||
    !form.nickname ||
    !form.email ||
    !form.password ||
    !form.captcha_value
  ) {
    uni.showToast({ title: '请完整填写注册信息', icon: 'none' })
    return
  }
  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await authStore.register({
      account: form.account.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim(),
      password: form.password,
      captcha_id: form.captcha_id,
      captcha_value: form.captcha_value,
    })
    uni.showToast({ title: '注册成功', icon: 'success' })
    uni.navigateBack()
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}

function openLogin() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.form-field {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
</style>
