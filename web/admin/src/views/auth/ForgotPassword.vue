<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { computed, reactive, ref } from 'vue'
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
      message: t('auth.accountRequired'),
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
    window.$message.success(t('auth.resetGuideSent'))
    router.push('/auth/reset-password')
  }, 500)
}
</script>

<template>
  <AuthLayout :title="t('auth.forgotTitle')" :subtitle="t('auth.forgotSubtitle')">
    <n-alert class="auth-alert" type="info" :bordered="false">
      {{ t('auth.forgotHint') }}
    </n-alert>

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

      <n-button type="primary" size="large" block attr-type="submit" :loading="loading">
        {{ t('auth.sendResetGuide') }}
      </n-button>

      <div class="auth-links">
        <RouterLink to="/auth/login">{{ t('auth.backToLogin') }}</RouterLink>
        <RouterLink to="/auth/reset-password">{{ t('auth.haveResetCode') }}</RouterLink>
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
