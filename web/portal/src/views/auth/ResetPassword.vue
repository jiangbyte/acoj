<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { authApi } from '@/api'
import CaptchaInput from '@/components/common/CaptchaInput.vue'
import { isValidEmail } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthLayout from './AuthLayout.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const captchaRef = ref<InstanceType<typeof CaptchaInput> | null>(null)
const loading = ref(false)

const form = reactive({
  email: typeof route.query.email === 'string' ? route.query.email : '',
  token: typeof route.query.token === 'string' ? route.query.token : '',
  password: '',
  confirmPassword: '',
  captcha_id: '',
  captcha_value: '',
})

function validateConfirmPassword(_rule: FormItemRule, value: string) {
  if (!value) {
    return new Error('Please confirm password')
  }
  if (value !== form.password) {
    return new Error('The two passwords do not match')
  }
  return true
}

function validateRequiredEmail(_rule: FormItemRule, value: string) {
  const text = String(value ?? '').trim()
  if (!text) {
    return new Error('Please enter login email')
  }
  if (!isValidEmail(text)) {
    return new Error('Please enter a valid email')
  }
  return true
}

const rules = computed<FormRules>(() => ({
  email: [
    {
      validator: validateRequiredEmail,
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: 'Please enter new password',
      trigger: ['input', 'blur'],
    },
    {
      min: 8,
      message: 'Password must be at least 8 characters',
      trigger: ['input', 'blur'],
    },
  ],
  confirmPassword: [
    {
      required: true,
      validator: validateConfirmPassword,
      trigger: ['input', 'blur'],
    },
  ],
  captcha_value: [
    {
      required: true,
      message: 'Please enter captcha',
      trigger: ['input', 'blur'],
    },
  ],
}))

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const encrypted = await encryptPasswords({ password: form.password })
    await authApi.resetPassword({
      email: form.email.trim(),
      token: form.token,
      password: encrypted.values.password,
      password_key_id: encrypted.password_key_id,
      captcha_id: form.captcha_id,
      captcha_value: form.captcha_value,
    })
    window.$message.success('Password reset. Please sign in again')
    router.push('/auth/login')
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout :title="'Reset Password'" :subtitle="'Use the email reset link to set a new password.'">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="email" :label="'Login Email'">
        <n-input v-model:value="form.email" :placeholder="'Enter login email'" :disabled="Boolean(form.token)" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="'New Password'">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :placeholder="'At least 8 characters'"
        />
      </n-form-item>

      <n-form-item path="confirmPassword" :label="'Confirm Password'">
        <n-input
          v-model:value="form.confirmPassword"
          type="password"
          show-password-on="click"
          :placeholder="'Enter password again'"
        />
      </n-form-item>

      <n-form-item path="captcha_value" :label="'Captcha'">
        <CaptchaInput
          ref="captchaRef"
          v-model:captcha-id="form.captcha_id"
          v-model:captcha-value="form.captcha_value"
        />
      </n-form-item>

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        {{ 'Reset Password' }}
      </n-button>

      <p class="auth-switch">
        <RouterLink to="/auth/login">{{ 'Back to sign in' }}</RouterLink>
      </p>
    </n-form>
  </AuthLayout>
</template>

<style scoped>
.auth-switch {
  margin-top: 22px;
  font-size: 14px;
  text-align: center;
}

.auth-switch a {
  color: var(--n-primary-color, #2563eb);
  text-decoration: none;
}
</style>
