<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { authApi } from '@/api'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AuthLayout from './AuthLayout.vue'

const router = useRouter()
const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  account: '',
  name: '',
  nickname: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false,
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

function validateAgreement(_rule: FormItemRule, value: boolean) {
  return value || new Error(t('auth.agreement_required'))
}

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: t('auth.account_required'),
      trigger: ['input', 'blur'],
    },
  ],
  nickname: [
    {
      required: true,
      message: t('auth.nickname_required'),
      trigger: ['input', 'blur'],
    },
  ],
  name: [
    {
      required: true,
      message: t('auth.name_required'),
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
  agreement: [
    {
      validator: validateAgreement,
      trigger: ['change'],
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
    await authApi.register({
      account: form.account.trim(),
      name: form.name.trim(),
      nickname: form.nickname.trim() || null,
      phone: form.phone.trim() || null,
      email: form.email.trim() || null,
      password: form.password,
    })
    loading.value = false
    window.$message.success(t('auth.register_success'))
    router.push('/auth/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout :title="t('auth.register_title')" :subtitle="t('auth.register_subtitle')">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="t('auth.account')">
        <n-input v-model:value="form.account" :placeholder="t('auth.placeholder.account')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="nickname" :label="t('auth.nickname')">
        <n-input
          v-model:value="form.nickname"
          :placeholder="t('auth.placeholder.nickname')"
          clearable
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:people" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="name" :label="t('auth.name')">
        <n-input v-model:value="form.name" :placeholder="t('auth.placeholder.name')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:id-card" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="phone" :label="t('auth.phone')">
        <n-input v-model:value="form.phone" :placeholder="t('auth.placeholder.phone')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:phone" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="email" :label="t('auth.email')">
        <n-input v-model:value="form.email" :placeholder="t('auth.placeholder.email')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="t('auth.password')">
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

      <n-form-item path="agreement" :show-label="false">
        <n-checkbox v-model:checked="form.agreement">
          {{ t('auth.agree_prefix') }}
          <a href="#" @click.prevent>{{ t('auth.terms') }}</a>
        </n-checkbox>
      </n-form-item>

      <n-button
        class="auth-submit"
        type="primary"
        size="large"
        block
        attr-type="submit"
        :loading="loading"
      >
        {{ t('auth.register') }}
      </n-button>

      <p class="auth-switch">
        {{ t('auth.has_account') }}
        <RouterLink to="/auth/login">{{ t('auth.back_to_login') }}</RouterLink>
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

.auth-switch a,
:deep(.n-checkbox__label a) {
  color: var(--n-primary-color, #2563eb);
  text-decoration: none;
}
</style>
