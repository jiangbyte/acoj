<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { authApi } from '@/api'
import CaptchaInput from '@/components/common/CaptchaInput.vue'
import { isValidEmail } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import { computed, reactive, ref } from 'vue'
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

const isResetMode = computed(() => Boolean(form.token))

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
      required: isResetMode.value,
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
      required: isResetMode.value,
      validator: isResetMode.value ? validateConfirmPassword : undefined,
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

async function sendLink() {
  if (isResetMode.value) {
    const emailValidation = validateRequiredEmail({} as FormItemRule, form.email)
    if (emailValidation instanceof Error) {
      window.$message.warning(emailValidation.message)
      return
    }
    if (!form.captcha_value.trim()) {
      window.$message.warning('Please enter captcha')
      return
    }
  } else {
    try {
      await formRef.value?.validate()
    } catch {
      return
    }
  }
  loading.value = true
  try {
    await authApi.forgotPassword({
      email: form.email.trim(),
      captcha_id: form.captcha_id,
      captcha_value: form.captcha_value,
    })
    window.$message.success('Password reset link sent')
    await captchaRef.value?.refresh()
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}

async function resetPassword() {
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
  <AuthLayout :title="isResetMode ? 'Reset Admin Password' : 'Recover Admin Password'" :subtitle="isResetMode ? 'Set a new password from your email reset link.' : 'Send a password reset link to your enabled admin login email.'">
    <n-alert class="auth-alert" type="info" :bordered="false">
      {{ isResetMode ? 'This reset link can be used once before it expires.' : 'A reset link will be sent only when this email is enabled for admin login.' }}
    </n-alert>

    <n-form ref="formRef" :model="form" :rules="rules" size="large">
      <n-form-item path="email" :label="'Login Email'">
        <n-input v-model:value="form.email" :disabled="isResetMode" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <template v-if="isResetMode">
        <n-form-item path="password" :label="'New Password'">
          <n-input v-model:value="form.password" type="password" show-password-on="click" />
        </n-form-item>
        <n-form-item path="confirmPassword" :label="'Confirm Password'">
          <n-input v-model:value="form.confirmPassword" type="password" show-password-on="click" />
        </n-form-item>
      </template>

      <n-form-item path="captcha_value" :label="'Captcha'">
        <CaptchaInput
          ref="captchaRef"
          v-model:captcha-id="form.captcha_id"
          v-model:captcha-value="form.captcha_value"
        />
      </n-form-item>

      <n-button
        type="primary"
        size="large"
        block
        :loading="loading"
        @click="isResetMode ? resetPassword() : sendLink()"
      >
        {{ isResetMode ? 'Reset Password' : 'Send Reset Link' }}
      </n-button>

      <div class="auth-links">
        <RouterLink to="/auth/login">{{ 'Back to sign in' }}</RouterLink>
        <n-button v-if="isResetMode" text type="primary" @click="sendLink">
          {{ 'Send a new link' }}
        </n-button>
      </div>
    </n-form>
  </AuthLayout>
</template>

<style scoped>
.auth-alert {
  margin-bottom: 22px;
}

.auth-links {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 22px;
  font-size: 14px;
}

.auth-links a {
  color: var(--n-primary-color, #2563eb);
  text-decoration: none;
}
</style>
