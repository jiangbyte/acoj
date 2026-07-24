<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { authApi } from '@/api'
import CaptchaInput from '@/components/common/CaptchaInput.vue'
import { isValidEmail } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from './AuthLayout.vue'

const router = useRouter()
const formRef = ref<FormInst | null>(null)
const captchaRef = ref<InstanceType<typeof CaptchaInput> | null>(null)
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

function validateConfirmPassword(_rule: FormItemRule, value: string) {
  if (!value) {
    return new Error('请确认密码')
  }
  if (value !== form.password) {
    return new Error('两次输入的密码不一致')
  }
  return true
}

function validateRequiredEmail(_rule: FormItemRule, value: string) {
  const text = String(value ?? '').trim()
  if (!text) {
    return new Error('请输入邮箱')
  }
  if (!isValidEmail(text)) {
    return new Error('请输入有效邮箱')
  }
  return true
}

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: '请输入账号',
      trigger: ['input', 'blur'],
    },
  ],
  nickname: [
    {
      required: true,
      message: '请输入昵称',
      trigger: ['input', 'blur'],
    },
  ],
  email: [
    {
      validator: validateRequiredEmail,
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: ['input', 'blur'],
    },
    {
      min: 8,
      message: '密码至少 8 个字符',
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
    const encrypted = await encryptPasswords({ password: form.password })
    await authApi.register({
      account: form.account.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim(),
      password: encrypted.values.password,
      password_key_id: encrypted.password_key_id,
      captcha_id: form.captcha_id,
      captcha_value: form.captcha_value,
    })
    window.$message.success('注册成功，请登录')
    router.push('/auth/login')
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout :title="'创建门户账号'" :subtitle="'创建默认启用邮箱登录的账号。'">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="'账号'">
        <n-input v-model:value="form.account" :placeholder="'请输入账号'" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:user" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="nickname" :label="'昵称'">
        <n-input v-model:value="form.nickname" :placeholder="'请输入昵称'" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:people" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="email" :label="'邮箱'">
        <n-input v-model:value="form.email" :placeholder="'请输入邮箱'" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="'密码'">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :placeholder="'至少 8 个字符'"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:lock" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="confirmPassword" :label="'确认密码'">
        <n-input
          v-model:value="form.confirmPassword"
          type="password"
          show-password-on="click"
          :placeholder="'请再次输入密码'"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:check-correct" />
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

      <n-button
        class="auth-submit"
        type="primary"
        size="large"
        block
        attr-type="submit"
        :loading="loading"
      >
        注册
      </n-button>

      <p class="auth-switch">
        已有账号？
        <RouterLink to="/auth/login">返回登录</RouterLink>
      </p>
    </n-form>
  </AuthLayout>
</template>

<style scoped>
.auth-submit {
  margin-top: 2px;
}

.auth-switch {
  margin-top: 22px;
  font-size: 14px;
  text-align: center;
  color: var(--n-text-color-2);
}

.auth-switch a {
  color: var(--n-primary-color, #2563eb);
  text-decoration: none;
}
</style>
