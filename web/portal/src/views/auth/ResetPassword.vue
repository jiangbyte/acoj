<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
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
    return new Error(t('auth.confirm_password_required'))
  }
  if (value !== form.password) {
    return new Error(t('auth.password_mismatch'))
  }
  return true
}

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: t('auth.account_required'),
      trigger: ['input', 'blur'],
    },
  ],
  code: [
    {
      required: true,
      message: t('auth.reset_code_required'),
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: t('auth.password_required'),
      trigger: ['input', 'blur'],
    },
    {
      min: 8,
      message: t('auth.password_min'),
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
    window.$message.success(t('auth.reset_success'))
    router.push('/auth/login')
  }, 500)
}
</script>

<template>
  <AuthLayout :title="t('auth.reset_title')" :subtitle="t('auth.reset_subtitle')">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="t('auth.account')">
        <n-input v-model:value="form.account" :placeholder="t('auth.placeholder.account')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="code" :label="t('auth.reset_code')">
        <n-input v-model:value="form.code" :placeholder="t('auth.placeholder.reset_code')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:key" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="t('auth.new_password')">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :placeholder="t('auth.placeholder.password_create')"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:lock" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="confirmPassword" :label="t('auth.confirm_password')">
        <n-input
          v-model:value="form.confirmPassword"
          type="password"
          show-password-on="click"
          :placeholder="t('auth.placeholder.confirm_password')"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:check-correct" />
          </template>
        </n-input>
      </n-form-item>

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        {{ t('auth.reset_password') }}
      </n-button>

      <p class="auth-switch">
        <RouterLink to="/auth/login">{{ t('auth.back_to_login') }}</RouterLink>
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
