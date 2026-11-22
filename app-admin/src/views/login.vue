<script lang="ts" setup>
import { NButton, NForm, NFormItem, NInput, NText } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import AuthShell from '@/components/auth/AuthShell.vue'
import { useTokenStore } from '@/stores'
import { useAuthFetch } from '@/composables/v1'
import { captchaRule, passwordRule, usernameRule } from '@/utils/authFormRules'
import { resolveAuthRedirect } from '@/utils/authRedirect'

const formRef = ref<FormInst | null>(null)
const formData = ref({
  username: '',
  password: '',
  captchaCode: '',
  uuid: '',
  platform: 'ADMIN',
})
const formRules = {
  username: usernameRule,
  password: passwordRule,
  captchaCode: captchaRule,
}

const { doLogin, captcha } = useAuthFetch()
const captchaRef = ref({
  captcha: '',
  uuid: '',
})
const isLoading = ref(false)

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()

/**
 * Load the verification code and update the form UUID together to avoid an asynchronous reset race.
 */
async function loadCaptcha() {
  const { data } = await captcha()
  if (data) {
    captchaRef.value = data
    formData.value.uuid = data.uuid
  }
}

async function refreshCaptcha() {
  formData.value.captchaCode = ''
  await loadCaptcha()
}

loadCaptcha()

async function handleLogin(e?: Event) {
  e?.preventDefault()
  if (isLoading.value) {
    return
  }
  try {
    await formRef.value?.validate()
  }
  catch {
    window.$message.error('Enter complete and valid information')
    return
  }

  isLoading.value = true
  try {
    const res = await doLogin(formData.value)
    if (res?.data) {
      tokenStore.setToken(res.data)
      if (tokenStore.isLogined) {
        const target = resolveAuthRedirect(route.query.redirect)
        await router.replace(target)
      }
      return
    }
    await refreshCaptcha()
  }
  catch {
    await refreshCaptcha()
  }
  finally {
    isLoading.value = false
  }
}
</script>

<template>
  <AuthShell
    title="Welcome to ACOJ Admin"
    subtitle="A simple, efficient, and secure admin console"
  >
    <div class="auth-form-title">
      Account Sign In
    </div>
    <NForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      class="auth-form"
      :show-label="false"
      size="large"
      @keyup.enter="handleLogin"
    >
      <NFormItem path="username">
        <NInput
          v-model:value="formData.username"
          data-testid="username-inp"
          :input-props="{ autocomplete: 'username' }"
          placeholder="Enter username"
          clearable
        />
      </NFormItem>
      <NFormItem path="password">
        <NInput
          v-model:value="formData.password"
          type="password"
          show-password-on="click"
          data-testid="password-inp"
          :input-props="{ autocomplete: 'current-password' }"
          placeholder="Enter password"
        />
      </NFormItem>
      <NFormItem path="captchaCode">
        <div class="captcha-row">
          <NInput
            v-model:value="formData.captchaCode"
            data-testid="captcha-inp"
            placeholder="Enter verification code"
            class="captcha-row__input"
          />
          <button
            type="button"
            class="captcha-row__img"
            title="Click to refresh the verification code"
            @click="refreshCaptcha"
          >
            <SafeImage
              v-if="captchaRef.captcha"
              :src="captchaRef.captcha"
              alt="verification code"
              class="w-full h-full"
              object-fit="contain"
              :icon-size="18"
            />
            <span v-else>Click to refresh</span>
          </button>
        </div>
      </NFormItem>
    </NForm>
    <NButton
      block
      type="primary"
      size="large"
      data-testid="login-execute"
      :loading="isLoading"
      class="auth-submit"
      @click="handleLogin"
    >
      Sign In
    </NButton>
    <div class="auth-hint">
      <NText depth="3">
        Only accounts with admin permissions can sign in
      </NText>
    </div>
  </AuthShell>
</template>

<style scoped>
.auth-form-title {
  margin-bottom: 35px;
  font-size: 20px;
  text-align: center;
  color: #1e1e1e;
}

.auth-form {
  width: 100%;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-row__input {
  flex: 1;
}

.captcha-row__img {
  flex-shrink: 0;
  height: 40px;
  min-width: 110px;
  padding: 0;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 12px;
}

.captcha-row__img img {
  height: 100%;
  display: block;
}

.auth-submit {
  margin-top: 4px;
  height: 50px;
  --n-color: #1748fd !important;
  --n-color-hover: #3b66fd !important;
  --n-color-pressed: #1239d0 !important;
  font-size: 16px;
  font-weight: 700;
}

.auth-hint {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
}
</style>
