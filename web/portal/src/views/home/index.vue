<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const capabilities = computed(() => [
  {
    icon: 'icon-park-outline:connection-point',
    title: t('app.portal.capability_process_title'),
    text: t('app.portal.capability_process_text'),
  },
  {
    icon: 'icon-park-outline:people',
    title: t('app.portal.capability_team_title'),
    text: t('app.portal.capability_team_text'),
  },
  {
    icon: 'icon-park-outline:chart-line',
    title: t('app.portal.capability_insight_title'),
    text: t('app.portal.capability_insight_text'),
  },
])

const metrics = computed(() => [
  { value: '99.9%', label: t('app.portal.metric_availability') },
  { value: '24/7', label: t('app.portal.metric_service') },
  { value: '3', label: t('app.portal.metric_entry') },
])

function openPrimary() {
  router.push(authStore.isLogin ? '/usercenter' : '/auth/login')
}
</script>

<template>
  <section class="overflow-hidden">
    <div class="px-4 py-10 sm:px-6 sm:py-14 lg:px-8 lg:py-18">
      <div class="grid items-center gap-10 lg:grid-cols-[1.02fr_0.98fr]">
        <div class="min-w-0">
          <div
            class="mb-5 inline-flex items-center gap-2 rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] px-3 py-1 text-sm font-600 text-[var(--primary-color)]"
          >
            <NovaIcon icon="icon-park-outline:building-one" />
            {{ t('app.portal.kicker') }}
          </div>
          <h1 class="max-w-180 text-4xl font-800 leading-tight sm:text-5xl lg:text-6xl">
            {{ t('app.portal.hero_title') }}
          </h1>
          <p class="mt-5 max-w-170 text-base leading-7 text-[var(--text-color-2)] sm:text-lg">
            {{ t('app.portal.hero_subtitle') }}
          </p>
          <div class="mt-8 flex flex-col gap-3 sm:flex-row">
            <NButton type="primary" size="large" :focusable="false" @click="openPrimary">
              {{ authStore.isLogin ? t('app.portal.open_workspace') : t('auth.login') }}
            </NButton>
            <NButton size="large" :focusable="false" @click="router.push('/auth/register')">
              {{ t('auth.register') }}
            </NButton>
          </div>
        </div>

        <div
          class="relative min-h-86 rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] p-5 shadow-sm sm:min-h-100 sm:p-6"
        >
          <div class="grid gap-4">
            <div class="rounded-2 bg-[var(--primary-color)] p-5 text-white">
              <div class="text-sm opacity-80">{{ t('app.portal.panel_label') }}</div>
              <div class="mt-3 text-2xl font-750">{{ t('app.portal.panel_title') }}</div>
              <div class="mt-6 grid grid-cols-3 gap-3">
                <div v-for="item in metrics" :key="item.label">
                  <div class="text-xl font-800">{{ item.value }}</div>
                  <div class="mt-1 text-xs opacity-78">{{ item.label }}</div>
                </div>
              </div>
            </div>

            <div class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="item in capabilities.slice(0, 2)"
                :key="item.title"
                class="rounded-2 border border-[var(--border-color)] p-4"
              >
                <NovaIcon class="text-[var(--primary-color)]" :icon="item.icon" :size="24" />
                <div class="mt-3 font-700">{{ item.title }}</div>
                <div class="mt-1 text-sm leading-6 text-[var(--text-color-3)]">{{ item.text }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="border-y border-[var(--border-color)] bg-[var(--card-color)]">
      <div class="px-4 py-8 sm:px-6 lg:px-8">
        <div class="grid gap-4 md:grid-cols-3">
          <div
            v-for="item in capabilities"
            :key="item.title"
            class="rounded-2 border border-[var(--border-color)] p-5"
          >
            <NovaIcon class="text-[var(--primary-color)]" :icon="item.icon" :size="26" />
            <h2 class="mt-4 text-lg font-750">{{ item.title }}</h2>
            <p class="mt-2 text-sm leading-6 text-[var(--text-color-3)]">
              {{ item.text }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
