<template>
  <Layout title="登录" back>
    <view class="auth-page">
      <u-card>
        <template #head>
          <CardHead
            title="登录企业门户"
            sub-title="请选择门户登录身份。"
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
                <text>密码</text>
                <u-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
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
            text="登录"
            type="primary"
            :loading="loading"
            @click="submit"
          ></u-button>
          <u-button
            text="创建账号"
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
import { useRouteStore } from '@/stores/route'
import { refreshDict } from '@/utils/dict'

type LoginType = 'ACCOUNT' | 'EMAIL' | 'PHONE'

const authStore = useAuthStore()
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
    name: '账号',
    label: '账号',
    placeholder: '请输入账号',
  },
  {
    key: 'EMAIL',
    name: '邮箱',
    label: '邮箱',
    placeholder: '请输入登录邮箱',
  },
  {
    key: 'PHONE',
    name: '手机号',
    label: '手机号',
    placeholder: '请输入登录手机号',
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
    await Promise.all([refreshDict(), routeStore.initRoutes()])
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
