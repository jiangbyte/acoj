<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
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
})

const rules = computed<FormRules>(() => ({
  account: [
    {
      required: true,
      message: t('auth.account_required'),
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
    window.$message.success(t('auth.reset_guide_sent'))
    router.push('/auth/reset-password')
  }, 500)
}
</script>

<template>
  <AuthLayout :title="t('auth.forgot_title')" :subtitle="t('auth.forgot_subtitle')">
    <n-alert class="auth-alert" type="info" :bordered="false">
      {{ t('auth.forgot_hint') }}
    </n-alert>

    <n-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
      <n-form-item path="account" :label="t('auth.account')">
        <n-input v-model:value="form.account" :placeholder="t('auth.placeholder.account')" clearable>
          <template #prefix>
            <NovaIcon icon="icon-park-outline:mail" />
          </template>
        </n-input>
      </n-form-item>

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        {{ t('auth.send_reset_guide') }}
      </n-button>

      <div class="auth-links">
        <RouterLink to="/auth/login">{{ t('auth.back_to_login') }}</RouterLink>
        <RouterLink to="/auth/reset-password">{{ t('auth.have_reset_code') }}</RouterLink>
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

@media (max-width: 420px) {
  .auth-links {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
