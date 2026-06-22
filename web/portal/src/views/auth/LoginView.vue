<script setup lang="ts">
import {
  CheckCircleOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { APP_TITLE } from '@/config/app'
import LocaleSwitch from '@/layouts/components/LocaleSwitch.vue'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  account: auth.rememberAccount,
  password: '',
  remember: Boolean(auth.rememberAccount),
})

async function handleSubmit() {
  loading.value = true
  try {
    await auth.login(form)
    message.success(t('login.success'))
    await router.replace(String(route.query.redirect || '/'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="min-h-screen w-full overflow-hidden bg-slate-50 text-slate-900 dark:bg-zinc-950 dark:text-zinc-100">
    <div class="grid min-h-screen grid-cols-1 lg:grid-cols-[minmax(0,1fr)_480px] xl:grid-cols-[minmax(0,1fr)_520px]">
      <section class="relative hidden min-h-screen overflow-hidden px-10 py-8 lg:flex lg:flex-col xl:px-14">
        <div class="absolute inset-0 bg-[linear-gradient(135deg,#f8fafc_0%,#eef4ff_45%,#f8fafc_100%)] dark:bg-[linear-gradient(135deg,#09090b_0%,#111827_48%,#18181b_100%)]" />
        <div class="absolute inset-x-10 top-28 h-px bg-slate-900/8 dark:bg-white/8" />

        <div class="relative z-1 flex items-center justify-between">
          <RouterLink to="/" class="inline-flex items-center gap-4 text-slate-950 no-underline dark:text-white">
            <span class="inline-flex h-12 w-12 items-center justify-center rounded-2 bg-blue-600 text-22px text-white font-700 shadow-lg shadow-blue-600/20">
              {{ APP_TITLE.charAt(0) }}
            </span>
            <span class="text-22px font-700 leading-7">{{ APP_TITLE }}</span>
          </RouterLink>
          <LocaleSwitch />
        </div>

        <div class="relative z-1 flex flex-1 items-center py-12">
          <div class="min-w-0">
            <div class="mb-6 inline-flex items-center gap-2 rounded-full border border-blue-200 bg-white/80 px-4 py-2 text-14px text-blue-700 shadow-sm backdrop-blur dark:border-blue-500/25 dark:bg-blue-500/10 dark:text-blue-300">
              <SafetyCertificateOutlined />
              <span>{{ t('login.badge') }}</span>
            </div>
            <h1 class="m-0 max-w-4xl text-48px text-slate-950 font-800 leading-[1.08] dark:text-white xl:text-56px">
              {{ t('login.heroTitle') }}
            </h1>
            <p class="mt-6 max-w-3xl text-17px text-slate-600 leading-8 dark:text-zinc-400">
              {{ t('login.heroDescription') }}
            </p>
            <div class="mt-10 grid grid-cols-3 gap-4">
              <div class="rounded-2 border border-white/80 bg-white/78 p-5 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-800 dark:bg-zinc-900/68 dark:shadow-black/20">
                <div class="text-13px text-slate-500 dark:text-zinc-400">{{ t('login.secure') }}</div>
                <div class="mt-4 text-28px text-slate-950 font-700 dark:text-white">Token</div>
              </div>
              <div class="rounded-2 border border-white/80 bg-white/78 p-5 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-800 dark:bg-zinc-900/68 dark:shadow-black/20">
                <div class="text-13px text-slate-500 dark:text-zinc-400">{{ t('login.portalScope') }}</div>
                <div class="mt-4 text-28px text-slate-950 font-700 dark:text-white">Portal</div>
              </div>
              <div class="rounded-2 border border-white/80 bg-white/78 p-5 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-800 dark:bg-zinc-900/68 dark:shadow-black/20">
                <div class="text-13px text-slate-500 dark:text-zinc-400">{{ t('login.responsive') }}</div>
                <div class="mt-4 text-28px text-slate-950 font-700 dark:text-white">Web</div>
              </div>
            </div>
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
          <div class="w-full max-w-100">
            <div class="mb-8">
              <div class="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2 bg-blue-50 text-22px text-blue-600 dark:bg-blue-500/10 dark:text-blue-300">
                <SafetyCertificateOutlined />
              </div>
              <h2 class="m-0 text-30px text-slate-950 font-700 leading-9 dark:text-white">{{ t('login.title') }}</h2>
              <p class="m-0 mt-3 text-14px text-slate-500 leading-6 dark:text-zinc-400">
                {{ t('login.description') }}
              </p>
            </div>

            <AForm layout="vertical" :model="form" @finish="handleSubmit">
              <AFormItem name="account" :rules="[{ required: true, message: t('login.accountRequired') }]">
                <AInput v-model:value="form.account" size="large" :placeholder="t('login.accountPlaceholder')">
                  <template #prefix>
                    <UserOutlined class="text-slate-400" />
                  </template>
                </AInput>
              </AFormItem>
              <AFormItem name="password" :rules="[{ required: true, message: t('login.passwordRequired') }]">
                <AInputPassword v-model:value="form.password" size="large" :placeholder="t('login.passwordPlaceholder')">
                  <template #prefix>
                    <LockOutlined class="text-slate-400" />
                  </template>
                </AInputPassword>
              </AFormItem>
              <div class="mb-6 flex items-center justify-between gap-4 text-14px">
                <ACheckbox v-model:checked="form.remember">{{ t('login.rememberAccount') }}</ACheckbox>
                <RouterLink to="/register" class="shrink-0 text-blue-600 no-underline hover:text-blue-500">
                  {{ t('login.createAccount') }}
                </RouterLink>
              </div>
              <AButton :loading="loading" block html-type="submit" size="large" type="primary">
                {{ t('login.submit') }}
              </AButton>
            </AForm>

            <div class="mt-6 flex items-center gap-2 rounded-2 border border-emerald-200 bg-emerald-50 px-4 py-3 text-12px text-emerald-700 dark:border-emerald-500/25 dark:bg-emerald-500/10 dark:text-emerald-300">
              <CheckCircleOutlined />
              <span>{{ t('login.secureConnection') }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
