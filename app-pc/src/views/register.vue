<script lang="ts" setup>
import { NButton, NForm, NFormItem, NInput, NText } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import AuthShell from '@/components/auth/AuthShell.vue'
import { useTokenStore, useUserStore } from '@/stores'
import { useAuthFetch } from '@/composables/v1'
import { captchaRule, emailRule, passwordRule, usernameRule } from '@/utils/authFormRules'
import { resolveAuthRedirect } from '@/utils/authRedirect'

const formRef = ref<FormInst | null>(null)
const formData = ref({
  username: '',
  password: '',
  email: '',
  captchaCode: '',
  uuid: '',
})
const formRules = {
  username: usernameRule,
  password: passwordRule,
  email: emailRule,
  captchaCode: captchaRule,
}

const { doRegister, captcha, getProfileNoe } = useAuthFetch()
const captchaRef = ref({
  captcha: '',
  uuid: '',
})
const isLoading = ref(false)

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const userStore = useUserStore()

/**
 * 拉取验证码并同步写入表单 uuid。
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

async function handleRegister(e?: Event) {
  e?.preventDefault()
  if (isLoading.value) {
    return
  }
  try {
    await formRef.value?.validate()
  }
  catch {
    window.$message.error('请填写完整且合法的信息')
    return
  }

  isLoading.value = true
  try {
    const res = await doRegister(formData.value)
    if (res?.data) {
      tokenStore.setToken(res.data)
      if (tokenStore.isLogined) {
        try {
          const { data: profile } = await getProfileNoe()
          if (profile?.id) {
            userStore.setUserId(profile.id)
          }
        }
        catch {
          // 资料拉取失败不阻断注册跳转
        }
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
    title="欢迎注册 ACOJ"
    subtitle="「在线评测、简洁、高效」的刷题平台"
  >
    <div class="auth-form-title">
      账号注册
    </div>
    <NForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      class="auth-form"
      :show-label="false"
      size="large"
      @keyup.enter="handleRegister"
    >
      <NFormItem path="username">
        <NInput
          v-model:value="formData.username"
          data-testid="username-inp"
          :input-props="{ autocomplete: 'username' }"
          placeholder="用户名（字母开头，2-15位）"
          clearable
        />
      </NFormItem>
      <NFormItem path="email">
        <NInput
          v-model:value="formData.email"
          data-testid="email-inp"
          :input-props="{ autocomplete: 'email' }"
          placeholder="请输入邮箱"
          clearable
        />
      </NFormItem>
      <NFormItem path="password">
        <NInput
          v-model:value="formData.password"
          type="password"
          show-password-on="click"
          data-testid="password-inp"
          :input-props="{ autocomplete: 'new-password' }"
          placeholder="密码（6-20位）"
        />
      </NFormItem>
      <NFormItem path="captchaCode">
        <div class="captcha-row">
          <NInput
            v-model:value="formData.captchaCode"
            data-testid="captcha-inp"
            placeholder="请输入验证码"
            class="captcha-row__input"
          />
          <button
            type="button"
            class="captcha-row__img"
            title="点击刷新验证码"
            @click="refreshCaptcha"
          >
            <img
              v-if="captchaRef.captcha"
              :src="captchaRef.captcha"
              alt="验证码"
            >
            <span v-else>点击刷新</span>
          </button>
        </div>
      </NFormItem>
    </NForm>
    <NButton
      block
      type="primary"
      size="large"
      data-testid="register-execute"
      :loading="isLoading"
      class="auth-submit"
      @click="handleRegister"
    >
      注册
    </NButton>
    <div class="auth-links">
      <NText depth="3">
        已有账号？
      </NText>
      <NButton
        text
        type="primary"
        @click="$router.push('/login')"
      >
        登录
      </NButton>
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

.auth-links {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}
</style>
