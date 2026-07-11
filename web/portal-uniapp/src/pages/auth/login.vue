<template>
  <Layout title="登录" back>
    <view class="auth-page">
      <u-card>
        <template #head>
          <CardHead
            title="Sign in to Enterprise Portal"
            sub-title="Choose a portal login identity."
          />
        </template>
        <template #body>
          <u-tabs
            :list="loginTabs"
            :current="activeIndex"
            @change="changeLoginType"
          ></u-tabs>
          <u-form :model="form">
            <u-form-item>
              <view class="form-field">
                <text>{{ currentLogin.label }}</text>
                <u-input
                  v-model="form[activeField]"
                  :placeholder="currentLogin.placeholder"
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
                  placeholder="Enter password"
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
            text="Sign In"
            type="primary"
            :loading="loading"
            @click="submit"
          ></u-button>
          <u-button
            text="Create Account"
            plain
            @click="openRegister"
          ></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import CaptchaField from '@/components/common/CaptchaField.vue'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { useRouteStore } from '@/stores/route'

type LoginType = 'ACCOUNT' | 'EMAIL' | 'PHONE'

const authStore = useAuthStore()
const dictStore = useDictStore()
const routeStore = useRouteStore()
const loading = ref(false)
const activeType = ref<LoginType>('ACCOUNT')
const captchaRef = ref<InstanceType<typeof CaptchaField> | null>(null)

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
    placeholder: 'Enter account',
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

const loginTabs = computed(() =>
  loginTypes.map((item) => ({ name: item.name, key: item.key }))
)
const currentLogin = computed(
  () =>
    loginTypes.find((item) => item.key === activeType.value) || loginTypes[0]
)
const activeField = computed(
  () => activeType.value.toLowerCase() as 'account' | 'email' | 'phone'
)
const activeIndex = computed(() =>
  loginTypes.findIndex((item) => item.key === activeType.value)
)

function changeLoginType(
  event: number | { index?: number; name?: string; key?: LoginType }
) {
  if (typeof event === 'number') {
    activeType.value = loginTypes[event]?.key || 'ACCOUNT'
    return
  }
  if (event.key) {
    activeType.value = event.key
    return
  }
  if (typeof event.index === 'number') {
    activeType.value = loginTypes[event.index]?.key || 'ACCOUNT'
    return
  }
  activeType.value =
    loginTypes.find((item) => item.name === event.name)?.key || 'ACCOUNT'
}

async function submit() {
  const identity = form[activeField.value].trim()
  if (!identity || !form.password || !form.captcha_value) {
    uni.showToast({ title: '请完整填写登录信息', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await authStore.login({
      account: identity,
      password: form.password,
      captcha_id: form.captcha_id,
      captcha_value: form.captcha_value,
      identity_type: activeType.value,
    })
    await Promise.all([dictStore.refreshDict(), routeStore.initRoutes()])
    uni.switchTab({ url: '/pages/usercenter/index' })
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}

function openRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
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
