<template>
  <Layout title="注册" back>
    <view class="auth-page">
      <u-card>
        <template #head>
          <CardHead
            title="创建门户账号"
            sub-title="创建可使用邮箱登录的账号。"
          />
        </template>
        <template #body>
          <u-form :model="form">
            <u-form-item>
              <view class="form-field">
                <text>账号</text>
                <u-input
                  v-model="form.account"
                  placeholder="请输入账号"
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
                <text>邮箱</text>
                <u-input
                  v-model="form.email"
                  placeholder="请输入邮箱"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>密码</text>
                <u-input
                  v-model="form.password"
                  type="password"
                  placeholder="至少 8 个字符"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>确认密码</text>
                <u-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  border="surround"
                ></u-input>
              </view>
            </u-form-item>
            <u-form-item>
              <view class="form-field">
                <text>验证码</text>
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
            text="注册"
            type="primary"
            :loading="loading"
            @click="submit"
          ></u-button>
          <u-button text="返回登录" plain @click="openLogin"></u-button>
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
