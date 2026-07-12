<template>
  <view class="login-page">
    <view class="login-header">
      <image
        class="login-header__logo"
        src="/static/logo.png"
        mode="aspectFit"
      ></image>
      <text class="login-header__title">Admin Console</text>
      <text class="login-header__subtitle">Sign in to continue</text>
    </view>

    <u-card class="login-card" :show-head="false">
      <u-tabs
        :list="loginTabs"
        :current="activeIndex"
        @change="changeLoginType"
      ></u-tabs>

      <u-form :model="form">
        <u-form-item :border-bottom="false">
          <view class="form-field">
            <text class="form-field__label">{{ currentLogin.label }}</text>
            <u-input
              v-model="form[activeField]"
              :placeholder="currentLogin.placeholder"
              border="surround"
              clearable
            ></u-input>
          </view>
        </u-form-item>

        <u-form-item :border-bottom="false">
          <view class="form-field">
            <text class="form-field__label">Password</text>
            <u-input
              v-model="form.password"
              type="password"
              placeholder="Enter password"
              border="surround"
            ></u-input>
          </view>
        </u-form-item>

        <u-form-item :border-bottom="false">
          <view class="form-field">
            <text class="form-field__label">Captcha</text>
            <view class="captcha-row">
              <u-input
                v-model="form.captcha_value"
                placeholder="Enter captcha"
                border="surround"
              ></u-input>
              <image
                v-if="captchaImage"
                :src="captchaImage"
                mode="aspectFit"
                class="captcha-image"
                @click="loadCaptcha"
              ></image>
            </view>
          </view>
        </u-form-item>
      </u-form>

      <view class="login-submit">
        <u-button
          text="Sign In"
          type="primary"
          :loading="loading"
          @click="submit"
        ></u-button>
      </view>
    </u-card>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { useRouteStore } from '@/stores/route'

type LoginType = 'ACCOUNT' | 'EMAIL' | 'PHONE'

const authStore = useAuthStore()
const dictStore = useDictStore()
const routeStore = useRouteStore()
const loading = ref(false)
const captchaImage = ref('')
const activeType = ref<LoginType>('ACCOUNT')

const loginTypes: Array<{
  key: LoginType
  name: string
  label: string
  placeholder: string
}> = [
  {
    key: 'ACCOUNT',
    name: 'Account',
    label: 'Account',
    placeholder: 'Enter admin account',
  },
  {
    key: 'EMAIL',
    name: 'Email',
    label: 'Email',
    placeholder: 'Enter login email',
  },
  {
    key: 'PHONE',
    name: 'Phone',
    label: 'Phone',
    placeholder: 'Enter login phone',
  },
]

const form = reactive({
  account: '',
  email: '',
  phone: '',
  password: '',
  captcha_id: '',
  captcha_value: '',
})

const currentLogin = computed(
  () =>
    loginTypes.find((item) => item.key === activeType.value) || loginTypes[0]
)
const activeField = computed(
  () => activeType.value.toLowerCase() as 'account' | 'email' | 'phone'
)
const loginTabs = computed(() =>
  loginTypes.map((item) => ({ name: item.name, key: item.key }))
)
const activeIndex = computed(() =>
  loginTypes.findIndex((item) => item.key === activeType.value)
)

function changeLoginType(event: any) {
  const index = typeof event === 'number' ? event : event.index
  activeType.value = loginTypes[index]?.key || 'ACCOUNT'
}

onLoad(() => {
  loadCaptcha()
})

async function loadCaptcha() {
  const captcha = await authApi.captcha({ format: 'png' })
  form.captcha_id = captcha.captcha_id
  captchaImage.value = `data:${captcha.image_type || 'image/png'};base64,${captcha.image_base64}`
}

async function submit() {
  const identity = form[activeField.value]
  if (!identity || !form.password || !form.captcha_value) {
    uni.showToast({ title: '请完整填写登录信息', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await authStore.login({
      account: identity.trim(),
      password: form.password,
      captcha_id: form.captcha_id,
      captcha_value: form.captcha_value,
      identity_type: activeType.value,
    })
    await Promise.all([dictStore.refreshDict(), routeStore.initRoutes()])
    uni.switchTab({ url: '/pages/dashboard/index' })
  } catch {
    form.captcha_value = ''
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
  background-color: #f8f9fa;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 48rpx;
}

.login-header__logo {
  width: 160rpx;
  height: 160rpx;
}

.login-header__title {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
}

.login-header__subtitle {
  font-size: 26rpx;
  color: #6b7280;
}

.login-card {
  width: 100%;
  max-width: 600rpx;
}

.form-field {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.form-field__label {
  font-size: 26rpx;
  color: #6b7280;
}

.captcha-row {
  width: 100%;
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.captcha-image {
  width: 156rpx;
  height: 70rpx;
}

.login-submit {
  margin-top: 32rpx;
}
</style>
