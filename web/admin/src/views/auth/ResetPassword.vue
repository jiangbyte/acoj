<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AuthLayout from './AuthLayout.vue'

const router = useRouter()
const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  account: '',
  code: '',
  password: '',
  confirmPassword: '',
})

function validateConfirmPassword(_rule: FormItemRule, value: string) {
  if (!value) {
    return new Error(t('auth.confirmPasswordRequired'))
  }
  if (value !== form.password) {
    return new Error(t('auth.passwordMismatch'))
  }
  return true
}

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: t('auth.accountRequired'),
      trigger: ['input', 'blur'],
    },
  ],
  code: [
    {
      required: true,
      message: t('auth.resetCodeRequired'),
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: t('auth.passwordRequired'),
      trigger: ['input', 'blur'],
    },
    {
      min: 8,
      message: t('auth.passwordMin'),
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
}))

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  window.setTimeout(() => {
    loading.value = false
    window.$message.success(t('auth.resetSuccess'))
    router.push('/auth/login')
  }, 500)
}
</script>

<template>
  <AuthLayout :title="t('auth.resetTitle')" :subtitle="t('auth.resetSubtitle')">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="t('auth.account')">
        <n-input
          v-model:value="form.account"
          :placeholder="t('auth.accountPlaceholder')"
          clearable
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="code" :label="t('auth.resetCode')">
        <n-input v-model:value="form.code" :placeholder="t('auth.resetCodePlaceholder')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:key" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="t('auth.newPassword')">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :placeholder="t('auth.passwordCreatePlaceholder')"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:lock" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="confirmPassword" :label="t('auth.confirmPassword')">
        <n-input
          v-model:value="form.confirmPassword"
          type="password"
          show-password-on="click"
          :placeholder="t('auth.confirmPasswordPlaceholder')"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:check-correct" />
          </template>
        </n-input>
      </n-form-item>

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        {{ t('auth.resetPassword') }}
      </n-button>

      <p class="auth-switch">
        <RouterLink to="/auth/login">{{ t('auth.backToLogin') }}</RouterLink>
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
