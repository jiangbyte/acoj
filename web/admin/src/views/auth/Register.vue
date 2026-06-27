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
  nickname: '',
  password: '',
  confirmPassword: '',
  agreement: false,
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

function validateAgreement(_rule: FormItemRule, value: boolean) {
  return value || new Error(t('auth.agreementRequired'))
}

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: t('auth.accountRequired'),
      trigger: ['input', 'blur'],
    },
  ],
  nickname: [
    {
      required: true,
      message: t('auth.nicknameRequired'),
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
  window.setTimeout(() => {
    loading.value = false
    window.$message.success(t('auth.registerSuccess'))
    router.push('/auth/login')
  }, 500)
}
</script>

<template>
  <AuthLayout :title="t('auth.registerTitle')" :subtitle="t('auth.registerSubtitle')">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="t('auth.account')">
        <n-input v-model:value="form.account" :placeholder="t('auth.accountPlaceholder')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="nickname" :label="t('auth.nickname')">
        <n-input
          v-model:value="form.nickname"
          :placeholder="t('auth.nicknamePlaceholder')"
          clearable
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:people" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="t('auth.password')">
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

      <n-form-item path="agreement" :show-label="false">
        <n-checkbox v-model:checked="form.agreement">
          {{ t('auth.agreePrefix') }}
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
        {{ t('auth.hasAccount') }}
        <RouterLink to="/auth/login">{{ t('auth.backToLogin') }}</RouterLink>
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
