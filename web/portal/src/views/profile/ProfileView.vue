<script setup lang="ts">
import { IdcardOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons-vue'
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

import { useUserStore } from '@/stores/user'

const user = useUserStore()
const { t } = useI18n()

const profile = computed(() => user.profile)
const profileName = computed(() => profile.value?.nickname || user.me?.account_id || t('layout.profileFallback'))
const avatarText = computed(() => profileName.value.slice(0, 1).toUpperCase())

onMounted(() => {
  user.ensureMe()
})
</script>

<template>
  <main class="min-h-[calc(100vh-129px)] bg-slate-50 px-4 py-10 dark:bg-zinc-950 sm:px-6 lg:px-10 xl:px-14">
    <section class="grid gap-6 lg:grid-cols-[360px_minmax(0,1fr)]">
      <aside class="rounded-2 border border-slate-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
        <div class="flex items-center gap-4">
          <AAvatar :size="64" class="bg-blue-600 text-24px text-white">{{ avatarText }}</AAvatar>
          <div class="min-w-0">
            <h1 class="m-0 truncate text-22px text-slate-950 font-700 dark:text-white">{{ profileName }}</h1>
            <p class="m-0 mt-1 truncate text-14px text-slate-500 dark:text-zinc-400">
              {{ profile?.level || t('layout.profileTitleFallback') }}
            </p>
          </div>
        </div>
      </aside>

      <section class="grid gap-4">
        <div class="rounded-2 border border-slate-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <div class="mb-5 flex items-center gap-2 text-18px text-slate-950 font-700 dark:text-white">
            <UserOutlined />
            <span>{{ t('profile.title') }}</span>
          </div>
          <ADescriptions bordered :column="{ xs: 1, md: 2 }">
            <ADescriptionsItem :label="t('profile.accountId')">{{ user.me?.account_id || '-' }}</ADescriptionsItem>
            <ADescriptionsItem :label="t('profile.nickname')">{{ profile?.nickname || '-' }}</ADescriptionsItem>
            <ADescriptionsItem :label="t('profile.level')">{{ profile?.level || '-' }}</ADescriptionsItem>
            <ADescriptionsItem :label="t('profile.bio')">{{ profile?.bio || '-' }}</ADescriptionsItem>
          </ADescriptions>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <article class="rounded-2 border border-slate-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <div class="mb-3 inline-flex h-11 w-11 items-center justify-center rounded-2 bg-blue-50 text-22px text-blue-600 dark:bg-blue-500/12 dark:text-blue-300">
              <SafetyCertificateOutlined />
            </div>
            <h2 class="m-0 text-17px text-slate-950 font-700 dark:text-white">{{ t('profile.securityTitle') }}</h2>
            <p class="mb-0 mt-2 text-14px text-slate-600 leading-6 dark:text-zinc-300">{{ t('profile.securityDesc') }}</p>
          </article>
          <article class="rounded-2 border border-slate-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
            <div class="mb-3 inline-flex h-11 w-11 items-center justify-center rounded-2 bg-emerald-50 text-22px text-emerald-600 dark:bg-emerald-500/12 dark:text-emerald-300">
              <IdcardOutlined />
            </div>
            <h2 class="m-0 text-17px text-slate-950 font-700 dark:text-white">{{ t('profile.serviceTitle') }}</h2>
            <p class="mb-0 mt-2 text-14px text-slate-600 leading-6 dark:text-zinc-300">{{ t('profile.serviceDesc') }}</p>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>
