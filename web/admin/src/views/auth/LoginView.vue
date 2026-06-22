<script setup lang="ts">
import {
  ApiOutlined,
  CheckCircleOutlined,
  CloudServerOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { DEFAULT_HOME_PATH, APP_TITLE } from '@/config/app'
import LocaleSwitch from '@/components/pro/LocaleSwitch.vue'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const appTitle = APP_TITLE
const appTitleFirstChar = APP_TITLE.charAt(0)
const form = reactive({
  account: auth.rememberAccount || 'admin',
  password: '123456789',
  remember: Boolean(auth.rememberAccount),
})

async function handleSubmit() {
  loading.value = true
  try {
    await auth.login(form)
    message.success(t('login.success'))
    await router.replace(String(route.query.redirect || DEFAULT_HOME_PATH))
  } catch (error) {
    console.error(t('login.failedLog'), error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    class="min-h-screen w-full overflow-hidden bg-[#f4f7fb] text-slate-900 dark:bg-zinc-950 dark:text-zinc-100"
  >
    <div class="grid min-h-screen w-full grid-cols-1 lg:grid-cols-[minmax(0,1fr)_480px] xl:grid-cols-[minmax(0,1fr)_520px]">
      <section class="relative hidden min-h-screen overflow-hidden px-10 py-8 lg:flex lg:flex-col xl:px-14">
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_18%_18%,rgba(37,99,235,0.16),transparent_34%),radial-gradient(circle_at_78%_12%,rgba(20,184,166,0.14),transparent_28%),linear-gradient(135deg,#f8fafc_0%,#eef4ff_44%,#f6f8fb_100%)] dark:bg-[radial-gradient(circle_at_18%_18%,rgba(59,130,246,0.18),transparent_34%),radial-gradient(circle_at_78%_12%,rgba(20,184,166,0.12),transparent_28%),linear-gradient(135deg,#09090b_0%,#111827_50%,#18181b_100%)]" />
        <div class="absolute inset-x-10 top-28 h-px bg-slate-900/8 dark:bg-white/8" />
        <div class="absolute bottom-0 left-0 right-0 h-48 bg-gradient-to-t from-white/70 to-transparent dark:from-zinc-950/70" />

        <div class="relative z-1 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <span
              class="inline-flex h-12 w-12 items-center justify-center rounded-2 bg-slate-950 text-22px text-white font-700 shadow-lg shadow-slate-900/18 dark:bg-white dark:text-zinc-950"
            >
              {{ APP_TITLE.charAt(0) }}
            </span>
            <div>
              <div class="text-22px text-slate-950 font-700 leading-7 dark:text-white">{{ APP_TITLE }}</div>
              <div class="mt-1 text-13px text-slate-500 dark:text-zinc-400">{{ t('login.subtitle') }}</div>
            </div>
          </div>
          <LocaleSwitch />
          <div
            class="inline-flex items-center gap-2 rounded-2 border border-emerald-200 bg-emerald-50 px-3 py-2 text-13px text-emerald-700 dark:border-emerald-500/25 dark:bg-emerald-500/10 dark:text-emerald-300"
          >
            <CheckCircleOutlined />
            <span>{{ t('login.secureConnection') }}</span>
          </div>
        </div>

        <div class="relative z-1 flex flex-1 items-center py-12">
          <div class="min-w-0">
            <div
              class="mb-6 inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white/80 px-4 py-2 text-14px text-slate-700 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-700 dark:bg-zinc-900/70 dark:text-zinc-300 dark:shadow-black/20"
            >
              <SafetyCertificateOutlined class="text-brand-600 dark:text-brand-400" />
              <span>{{ t('login.badge') }}</span>
            </div>
            <h1 class="m-0 max-w-4xl text-48px text-slate-950 font-700 leading-[1.08] dark:text-white xl:text-56px">
              {{ t('login.heroTitle') }}
            </h1>
            <p class="mt-6 max-w-3xl text-17px text-slate-600 leading-8 dark:text-zinc-400">
              {{ t('login.heroDescription') }}
            </p>

            <div class="mt-10 grid grid-cols-3 gap-4">
              <div class="rounded-2 border border-white/80 bg-white/78 p-5 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-800 dark:bg-zinc-900/68 dark:shadow-black/20">
                <div class="flex items-center gap-3 text-slate-500 dark:text-zinc-400">
                  <CloudServerOutlined class="text-20px text-brand-600 dark:text-brand-400" />
                  <span class="text-13px">{{ t('login.availability') }}</span>
                </div>
                <div class="mt-4 text-30px text-slate-950 font-700 dark:text-white">99.9%</div>
              </div>
              <div class="rounded-2 border border-white/80 bg-white/78 p-5 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-800 dark:bg-zinc-900/68 dark:shadow-black/20">
                <div class="flex items-center gap-3 text-slate-500 dark:text-zinc-400">
                  <TeamOutlined class="text-20px text-brand-600 dark:text-brand-400" />
                  <span class="text-13px">{{ t('login.permissionModel') }}</span>
                </div>
                <div class="mt-4 text-30px text-slate-950 font-700 dark:text-white">RBAC</div>
              </div>
              <div class="rounded-2 border border-white/80 bg-white/78 p-5 shadow-sm shadow-slate-200/70 backdrop-blur dark:border-zinc-800 dark:bg-zinc-900/68 dark:shadow-black/20">
                <div class="flex items-center gap-3 text-slate-500 dark:text-zinc-400">
                  <ApiOutlined class="text-20px text-brand-600 dark:text-brand-400" />
                  <span class="text-13px">{{ t('login.integration') }}</span>
                </div>
                <div class="mt-4 text-30px text-slate-950 font-700 dark:text-white">API</div>
              </div>
            </div>
          </div>
        </div>

        <div class="relative z-1 flex items-center gap-6 text-13px text-slate-500 dark:text-zinc-400">
          <span>{{ t('login.copyright', { appTitle }) }}</span>
          <a href="#" class="text-slate-500 transition hover:text-brand-600 dark:text-zinc-400 dark:hover:text-brand-400">{{ t('login.helpCenter') }}</a>
          <a href="#" class="text-slate-500 transition hover:text-brand-600 dark:text-zinc-400 dark:hover:text-brand-400">{{ t('login.privacy') }}</a>
          <a href="#" class="text-slate-500 transition hover:text-brand-600 dark:text-zinc-400 dark:hover:text-brand-400">{{ t('login.terms') }}</a>
        </div>
      </section>

      <section class="flex min-h-screen flex-col border-l border-slate-200 bg-white px-5 py-6 shadow-[-20px_0_60px_rgb(15_23_42/0.08)] dark:border-zinc-800 dark:bg-zinc-950 sm:px-8 lg:px-10">
        <div class="flex items-center justify-between gap-3 lg:hidden">
          <div class="flex min-w-0 items-center gap-3">
            <span class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2 bg-slate-950 text-18px text-white font-700 dark:bg-white dark:text-zinc-950">
              {{ appTitleFirstChar }}
            </span>
            <div class="min-w-0">
              <div class="truncate text-18px text-slate-950 font-700 leading-6 dark:text-white">{{ appTitle }}</div>
              <div class="truncate text-12px text-slate-500 dark:text-zinc-400">{{ t('login.subtitle') }}</div>
            </div>
          </div>
          <LocaleSwitch />
        </div>

        <div class="flex flex-1 items-center justify-center py-10">
          <div class="w-full max-w-100">
            <div class="mb-8">
              <div class="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2 bg-brand-50 text-22px text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
                <SafetyCertificateOutlined />
              </div>
              <h2 class="m-0 text-30px text-slate-950 font-700 leading-9 dark:text-white">{{ t('login.consoleLogin') }}</h2>
              <p class="m-0 mt-3 text-14px text-slate-500 leading-6 dark:text-zinc-400">
                {{ t('login.formDescription') }}
              </p>
            </div>

            <div class="mb-6 grid grid-cols-2 gap-3">
              <div class="rounded-2 border border-slate-200 bg-slate-50 px-4 py-3 dark:border-zinc-800 dark:bg-zinc-900">
                <div class="text-12px text-slate-500 dark:text-zinc-400">{{ t('login.authMethod') }}</div>
                <div class="mt-1 text-14px text-slate-950 font-600 dark:text-white">{{ t('login.accountPassword') }}</div>
              </div>
              <div class="rounded-2 border border-slate-200 bg-slate-50 px-4 py-3 dark:border-zinc-800 dark:bg-zinc-900">
                <div class="text-12px text-slate-500 dark:text-zinc-400">{{ t('login.environment') }}</div>
                <div class="mt-1 text-14px text-slate-950 font-600 dark:text-white">Local Mock</div>
              </div>
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
                <a class="shrink-0 text-brand-600 transition hover:text-brand-500 dark:text-brand-400 dark:hover:text-brand-300">
                  {{ t('login.forgotPassword') }}
                </a>
              </div>
              <AButton :loading="loading" block html-type="submit" size="large" type="primary">
                {{ t('login.loginButton') }}
              </AButton>
            </AForm>

            <div class="mt-6 rounded-2 border border-amber-200 bg-amber-50 px-4 py-3 text-12px text-amber-800 leading-5 dark:border-amber-500/25 dark:bg-amber-500/10 dark:text-amber-200">
              {{ t('login.demoWarning') }}
            </div>
          </div>
        </div>

        <div class="flex flex-wrap justify-center gap-x-6 gap-y-2 text-12px text-slate-500 dark:text-zinc-400 lg:hidden">
          <a href="#" class="text-slate-500 hover:text-brand-600 dark:text-zinc-400">{{ t('login.help') }}</a>
          <a href="#" class="text-slate-500 hover:text-brand-600 dark:text-zinc-400">{{ t('login.privacy') }}</a>
          <a href="#" class="text-slate-500 hover:text-brand-600 dark:text-zinc-400">{{ t('login.terms') }}</a>
          <span class="w-full text-center">{{ t('login.copyright', { appTitle }) }}</span>
        </div>
      </section>
    </div>
  </div>
</template>
