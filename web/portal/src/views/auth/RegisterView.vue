<script setup lang="ts">
import { LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'

import { APP_TITLE } from '@/config/app'
import LocaleSwitch from '@/layouts/components/LocaleSwitch.vue'

const { t } = useI18n()
const form = reactive({
  account: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

function validateConfirmPassword() {
  if (!form.confirmPassword || form.confirmPassword === form.password) {
    return Promise.resolve()
  }
  return Promise.reject(t('register.passwordMismatch'))
}

function handleSubmit() {
  message.info(t('register.unavailable'))
}
</script>

<template>
  <main class="min-h-screen w-full overflow-hidden bg-slate-50 text-slate-900 dark:bg-zinc-950 dark:text-zinc-100">
    <div class="grid min-h-screen grid-cols-1 lg:grid-cols-[minmax(0,1fr)_520px]">
      <section class="relative hidden min-h-screen overflow-hidden px-10 py-8 lg:flex lg:flex-col xl:px-14">
        <div class="absolute inset-0 bg-[linear-gradient(135deg,#f8fafc_0%,#eff6ff_48%,#f8fafc_100%)] dark:bg-[linear-gradient(135deg,#09090b_0%,#111827_48%,#18181b_100%)]" />
        <div class="relative z-1 flex items-center justify-between">
          <RouterLink to="/" class="inline-flex items-center gap-4 text-slate-950 no-underline dark:text-white">
            <span class="inline-flex h-12 w-12 items-center justify-center rounded-2 bg-blue-600 text-22px text-white font-700">
              {{ APP_TITLE.charAt(0) }}
            </span>
            <span class="text-22px font-700">{{ APP_TITLE }}</span>
          </RouterLink>
          <LocaleSwitch />
        </div>
        <div class="relative z-1 flex flex-1 items-center">
          <div>
            <div class="mb-6 inline-flex rounded-full border border-blue-200 bg-white/80 px-4 py-2 text-14px text-blue-700 dark:border-blue-500/25 dark:bg-blue-500/10 dark:text-blue-300">
              {{ t('register.badge') }}
            </div>
            <h1 class="m-0 max-w-4xl text-48px text-slate-950 font-800 leading-[1.08] dark:text-white xl:text-56px">
              {{ t('register.heroTitle') }}
            </h1>
            <p class="mt-6 max-w-3xl text-17px text-slate-600 leading-8 dark:text-zinc-400">
              {{ t('register.heroDescription') }}
            </p>
          </div>
        </div>
      </section>

      <section class="flex min-h-screen flex-col border-l border-slate-200 bg-white px-5 py-6 shadow-[-20px_0_60px_rgb(15_23_42/0.08)] dark:border-zinc-800 dark:bg-zinc-950 sm:px-8 lg:px-10">
        <div class="flex items-center justify-between gap-3 lg:hidden">
          <RouterLink to="/" class="inline-flex min-w-0 items-center gap-3 text-slate-950 no-underline dark:text-white">
            <span class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2 bg-blue-600 text-18px text-white font-700">
              {{ APP_TITLE.charAt(0) }}
            </span>
            <span class="truncate text-18px font-700">{{ APP_TITLE }}</span>
          </RouterLink>
          <LocaleSwitch />
        </div>

        <div class="flex flex-1 items-center justify-center py-10">
          <div class="w-full max-w-108">
            <div class="mb-8">
              <h2 class="m-0 text-30px text-slate-950 font-700 leading-9 dark:text-white">{{ t('register.title') }}</h2>
              <p class="m-0 mt-3 text-14px text-slate-500 leading-6 dark:text-zinc-400">
                {{ t('register.description') }}
              </p>
            </div>

            <AForm layout="vertical" :model="form" @finish="handleSubmit">
              <AFormItem name="account" :rules="[{ required: true, message: t('register.accountRequired') }]">
                <AInput v-model:value="form.account" size="large" :placeholder="t('register.accountPlaceholder')">
                  <template #prefix><UserOutlined class="text-slate-400" /></template>
                </AInput>
              </AFormItem>
              <AFormItem name="nickname" :rules="[{ required: true, message: t('register.nicknameRequired') }]">
                <AInput v-model:value="form.nickname" size="large" :placeholder="t('register.nicknamePlaceholder')" />
              </AFormItem>
              <AFormItem name="email">
                <AInput v-model:value="form.email" size="large" :placeholder="t('register.emailPlaceholder')">
                  <template #prefix><MailOutlined class="text-slate-400" /></template>
                </AInput>
              </AFormItem>
              <AFormItem name="password" :rules="[{ required: true, message: t('register.passwordRequired') }]">
                <AInputPassword v-model:value="form.password" size="large" :placeholder="t('register.passwordPlaceholder')">
                  <template #prefix><LockOutlined class="text-slate-400" /></template>
                </AInputPassword>
              </AFormItem>
              <AFormItem
                name="confirmPassword"
                :rules="[
                  { required: true, message: t('register.confirmPasswordRequired') },
                  { validator: validateConfirmPassword },
                ]"
              >
                <AInputPassword v-model:value="form.confirmPassword" size="large" :placeholder="t('register.confirmPasswordPlaceholder')">
                  <template #prefix><LockOutlined class="text-slate-400" /></template>
                </AInputPassword>
              </AFormItem>
              <AButton block html-type="submit" size="large" type="primary">
                {{ t('register.submit') }}
              </AButton>
            </AForm>

            <div class="mt-6 text-center text-14px text-slate-500 dark:text-zinc-400">
              {{ t('register.hasAccount') }}
              <RouterLink to="/login" class="text-blue-600 no-underline hover:text-blue-500">
                {{ t('layout.login') }}
              </RouterLink>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
