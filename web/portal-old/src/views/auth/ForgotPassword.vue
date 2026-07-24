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
    return new Error('请输入登录邮箱')
  }
  if (!isValidEmail(text)) {
    return new Error('请输入有效邮箱')
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
      message: '请输入验证码',
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
    window.$message.success('密码重置链接已发送')
    await captchaRef.value?.refresh()
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout :title="'找回密码'" :subtitle="'向已启用的门户登录邮箱发送密码重置链接。'">
    <n-alert class="auth-alert" type="info" :bordered="false">
      请使用已启用门户登录的邮箱地址。
    </n-alert>

    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="email" :label="'登录邮箱'">
        <n-input v-model:value="form.email" :placeholder="'请输入登录邮箱'" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="captcha_value" :label="'验证码'">
        <CaptchaInput
          ref="captchaRef"
          v-model:captcha-id="form.captcha_id"
          v-model:captcha-value="form.captcha_value"
        />
      </n-form-item>

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        发送重置链接
      </n-button>

      <div class="auth-links">
        <RouterLink to="/auth/login">返回登录</RouterLink>
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
