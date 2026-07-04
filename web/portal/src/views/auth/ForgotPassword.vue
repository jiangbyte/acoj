<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { authApi } from '@/api'
import CaptchaInput from '@/components/common/CaptchaInput.vue'
import { isValidEmail } from '@/utils'
import { computed, reactive, ref } from 'vue'
import AuthLayout from './AuthLayout.vue'

const formRef = ref<FormInst | null>(null)
const captchaRef = ref<InstanceType<typeof CaptchaInput> | null>(null)
const loading = ref(false)

const form = reactive({
  email: '',
  captcha_id: '',
  captcha_value: '',
})

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
</script>

<template>
  <AuthLayout :title="'Recover Password'" :subtitle="'Send a password reset link to your enabled portal login email.'">
    <n-alert class="auth-alert" type="info" :bordered="false">
      {{ 'Use the email address enabled for portal login.' }}
    </n-alert>

    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="email" :label="'Login Email'">
        <n-input v-model:value="form.email" :placeholder="'Enter login email'" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="captcha_value" :label="'Captcha'">
        <CaptchaInput
          ref="captchaRef"
          v-model:captcha-id="form.captcha_id"
          v-model:captcha-value="form.captcha_value"
        />
      </n-form-item>

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        {{ 'Send Reset Link' }}
      </n-button>

      <div class="auth-links">
        <RouterLink to="/auth/login">{{ 'Back to sign in' }}</RouterLink>
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
