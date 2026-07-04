<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import CaptchaInput from '@/components/common/CaptchaInput.vue'
import { useAuthStore } from '@/stores'
import { isValidEmail } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import AuthLayout from './AuthLayout.vue'

type LoginType = 'ACCOUNT' | 'EMAIL' | 'PHONE'

const route = useRoute()
const authStore = useAuthStore()
const formRef = ref<FormInst | null>(null)
const captchaRef = ref<InstanceType<typeof CaptchaInput> | null>(null)
const loading = ref(false)
const activeType = ref<LoginType>('ACCOUNT')

const loginTypes: Array<{ key: LoginType; label: string; icon: string; placeholder: string }> = [
  {
    key: 'ACCOUNT',
    label: 'Account',
    icon: 'icon-park-outline:user',
    placeholder: 'Enter account',
  },
  {
    key: 'EMAIL',
    label: 'Email',
    icon: 'icon-park-outline:mail',
    placeholder: 'Enter login email',
  },
  {
    key: 'PHONE',
    label: 'Phone',
    icon: 'icon-park-outline:phone',
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
  remember: true,
})

const currentLogin = computed(() => loginTypes.find((item) => item.key === activeType.value)!)
const activeField = computed(() => activeType.value.toLowerCase() as 'account' | 'email' | 'phone')

function validateLoginIdentity(_rule: FormItemRule, value: string) {
  const text = String(value ?? '').trim()
  if (!text) {
    return new Error(`Please enter ${currentLogin.value.label.toLowerCase()}`)
  }
  if (activeType.value === 'EMAIL' && !isValidEmail(text)) {
    return new Error('Please enter a valid email')
  }
  return true
}

const rules = computed<FormRules>(() => ({
  [activeField.value]: [
    {
      validator: validateLoginIdentity,
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: 'Please enter password',
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
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : undefined
    const encrypted = await encryptPasswords({ password: form.password })
    await authStore.login(
      form[activeField.value].trim(),
      encrypted.values.password || '',
      redirect,
      activeType.value,
      {
        password_key_id: encrypted.password_key_id,
        captcha_id: form.captcha_id,
        captcha_value: form.captcha_value,
      },
    )
    window.$message.success('Signed in')
  } catch {
    await captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout :title="'Sign in to Enterprise Portal'" :subtitle="'Choose the identity type you enabled for portal access.'">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-tabs v-model:value="activeType" type="segment" animated class="auth-login-tabs">
        <n-tab-pane
          v-for="item in loginTypes"
          :key="item.key"
          :name="item.key"
          :tab="item.label"
        >
          <n-form-item :path="activeField" :label="item.label">
            <n-input
              v-model:value="form[activeField]"
              :placeholder="item.placeholder"
              clearable
            >
              <template #prefix>
                <NovaIcon :icon="item.icon" />
              </template>
            </n-input>
          </n-form-item>
        </n-tab-pane>
      </n-tabs>

      <n-form-item path="password" :label="'Password'">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :placeholder="'Enter password'"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:lock" />
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

      <div class="auth-form-row">
        <n-checkbox v-model:checked="form.remember">
          {{ 'Remember me' }}
        </n-checkbox>
        <RouterLink to="/auth/forgot-password">{{ 'Forgot password?' }}</RouterLink>
      </div>

      <n-button
        class="auth-submit"
        type="primary"
        size="large"
        block
        attr-type="submit"
        :loading="loading"
      >
        {{ 'Sign In' }}
      </n-button>

      <p class="auth-switch">
        {{ 'No account yet?' }}
        <RouterLink to="/auth/register">{{ 'Create one' }}</RouterLink>
      </p>
    </n-form>
  </AuthLayout>
</template>

<style scoped>
.auth-login-tabs {
  margin-bottom: 4px;
}

.auth-login-tabs :deep(.n-tabs-pane-wrapper) {
  overflow: visible;
}

.auth-form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: -2px 0 22px;
  font-size: 14px;
}

.auth-form-row a,
.auth-switch a {
  color: var(--n-primary-color, #2563eb);
  text-decoration: none;
}

.auth-submit {
  margin-top: 2px;
}

.auth-switch {
  margin-top: 22px;
  font-size: 14px;
  text-align: center;
  color: var(--n-text-color-2);
}

@media (max-width: 420px) {
  .auth-form-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
