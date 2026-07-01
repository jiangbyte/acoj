<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores'
import AuthLayout from './AuthLayout.vue'

const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  account: '',
  password: '',
  remember: true,
})

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: t('auth.account_required'),
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: t('auth.password_required'),
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
    await authStore.login(form.account, form.password, redirect)
    window.$message.success(t('auth.login_success'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout :title="t('auth.login_title')" :subtitle="t('auth.login_subtitle')">
    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="t('auth.account')">
        <n-input
          v-model:value="form.account"
          :placeholder="t('auth.placeholder.account')"
          clearable
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:user" />
          </template>
        </n-input>
      </n-form-item>

      <n-form-item path="password" :label="t('auth.password')">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :placeholder="t('auth.placeholder.password')"
        >
          <template #prefix>
            <NovaIcon icon="icon-park-outline:lock" />
          </template>
        </n-input>
      </n-form-item>

      <div class="auth-form-row">
        <n-checkbox v-model:checked="form.remember">
          {{ t('auth.remember_me') }}
        </n-checkbox>
        <RouterLink to="/auth/forgot-password">{{ t('auth.forgot_password') }}</RouterLink>
      </div>

      <n-button
        class="auth-submit"
        type="primary"
        size="large"
        block
        attr-type="submit"
        :loading="loading"
      >
        {{ t('auth.login') }}
      </n-button>

      <p class="auth-switch">
        {{ t('auth.no_account') }}
        <RouterLink to="/auth/register">{{ t('auth.create_account') }}</RouterLink>
      </p>
    </n-form>
  </AuthLayout>
</template>

<style scoped>
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
