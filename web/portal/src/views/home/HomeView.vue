<script setup lang="ts">
import {
  ApiOutlined,
  BellOutlined,
  CheckCircleOutlined,
  CloudServerOutlined,
  CompassOutlined,
  CustomerServiceOutlined,
  FileSearchOutlined,
  RightOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import heroImage from '@/assets/hero.png'
import { listPublicBanners, recordBannerInteraction } from '@/apis/banner'
import { APP_TITLE } from '@/config/app'
import type { SysBannerItem } from '@/types/api'

interface FeatureItem {
  title: string
  description: string
  icon: typeof SafetyCertificateOutlined
  color: string
}

interface NoticeItem {
  title: string
  description: string
  tag: string
}

const router = useRouter()
const { t } = useI18n()
const banners = ref<SysBannerItem[]>([])
const loading = ref(false)

const fallbackBanner = computed<SysBannerItem>(() => ({
  id: 'fallback-home-hero',
  title: `${APP_TITLE} ${t('home.titleSuffix')}`,
  image: heroImage,
  url: null,
  link_type: 'NONE',
  summary: t('home.fallbackSummary'),
  description: t('home.fallbackDescription'),
  category: 'HOME',
  type: 'HERO',
  position: 'HOME_TOP',
  display_scope: 'PORTAL',
  sort: 0,
  interaction_count: 0,
  status: 'ENABLED',
  created_at: '',
  updated_at: '',
}))
const heroBanners = computed(() => (banners.value.length ? banners.value : [fallbackBanner.value]))

const features = computed<FeatureItem[]>(() => [
  {
    title: t('home.features.access.title'),
    description: t('home.features.access.description'),
    icon: CompassOutlined,
    color: 'blue',
  },
  {
    title: t('home.features.security.title'),
    description: t('home.features.security.description'),
    icon: SafetyCertificateOutlined,
    color: 'emerald',
  },
  {
    title: t('home.features.content.title'),
    description: t('home.features.content.description'),
    icon: BellOutlined,
    color: 'amber',
  },
  {
    title: t('home.features.extension.title'),
    description: t('home.features.extension.description'),
    icon: ApiOutlined,
    color: 'violet',
  },
])

const notices = computed<NoticeItem[]>(() => [
  {
    title: t('home.notices.launch.title'),
    description: t('home.notices.launch.description'),
    tag: t('home.notices.launch.tag'),
  },
  {
    title: t('home.notices.profile.title'),
    description: t('home.notices.profile.description'),
    tag: t('home.notices.profile.tag'),
  },
  {
    title: t('home.notices.content.title'),
    description: t('home.notices.content.description'),
    tag: t('home.notices.content.tag'),
  },
])

const quickLinks = computed(() => [
  {
    title: t('home.quickLinks.status.title'),
    description: t('home.quickLinks.status.description'),
    icon: CloudServerOutlined,
  },
  {
    title: t('home.quickLinks.files.title'),
    description: t('home.quickLinks.files.description'),
    icon: FileSearchOutlined,
  },
  {
    title: t('home.quickLinks.support.title'),
    description: t('home.quickLinks.support.description'),
    icon: CustomerServiceOutlined,
  },
])

function featureColorClass(color: FeatureItem['color']) {
  const colorMap: Record<string, string> = {
    blue: 'bg-blue-50 text-blue-600 dark:bg-blue-500/12 dark:text-blue-300',
    emerald: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/12 dark:text-emerald-300',
    amber: 'bg-amber-50 text-amber-600 dark:bg-amber-500/12 dark:text-amber-300',
    violet: 'bg-violet-50 text-violet-600 dark:bg-violet-500/12 dark:text-violet-300',
  }
  return colorMap[color] || colorMap.blue
}

async function fetchBanners() {
  loading.value = true
  try {
    banners.value = await listPublicBanners({
      position: 'HOME_TOP',
      category: 'HOME',
    })
  } catch (error) {
    console.warn('[portal] public banners unavailable', error)
    banners.value = []
  } finally {
    loading.value = false
  }
}

async function handleBannerClick(banner: SysBannerItem) {
  if (banner.id !== fallbackBanner.value.id) {
    recordBannerInteraction(banner.id).catch((error) => {
      console.warn('[portal] record banner interaction failed', error)
    })
  }

  if (!banner.url || banner.link_type === 'NONE') {
    return
  }

  if (banner.link_type === 'URL') {
    window.open(banner.url, '_blank', 'noopener,noreferrer')
    return
  }

  await router.push(banner.url)
}

onMounted(fetchBanners)
</script>

<template>
  <main class="overflow-hidden bg-slate-50 dark:bg-zinc-950">
    <section class="relative border-b border-slate-200 bg-white dark:border-zinc-800 dark:bg-zinc-950">
      <div class="portal-container grid min-h-[calc(100vh-64px)] items-center gap-10 py-12 lg:grid-cols-[minmax(0,0.9fr)_minmax(420px,1.1fr)] lg:py-16">
        <div class="min-w-0">
          <div
            class="mb-5 inline-flex items-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-13px text-blue-700 font-600 dark:border-blue-500/25 dark:bg-blue-500/10 dark:text-blue-300"
          >
            <ThunderboltOutlined />
            <span>{{ t('home.heroBadge') }}</span>
          </div>
          <h1 class="m-0 max-w-720px text-36px text-slate-950 font-800 leading-tight dark:text-white sm:text-48px lg:text-56px">
            {{ APP_TITLE }}
          </h1>
          <p class="mb-0 mt-5 max-w-660px text-16px text-slate-600 leading-8 dark:text-zinc-300 sm:text-18px">
            {{ t('home.subtitle') }}
          </p>
          <div class="mt-8 flex flex-col gap-3 sm:flex-row">
            <RouterLink to="/login">
              <AButton class="h-11! px-5!" type="primary">
                {{ t('home.primaryAction') }}
                <RightOutlined />
              </AButton>
            </RouterLink>
            <RouterLink to="/#features">
              <AButton class="h-11! px-5!">{{ t('home.secondaryAction') }}</AButton>
            </RouterLink>
          </div>
          <div class="mt-8 grid max-w-560px grid-cols-1 gap-3 sm:grid-cols-3">
            <div class="inline-flex items-center gap-2 text-14px text-slate-600 dark:text-zinc-300">
              <CheckCircleOutlined class="text-emerald-500" />
              <span>{{ t('home.checks.responsive') }}</span>
            </div>
            <div class="inline-flex items-center gap-2 text-14px text-slate-600 dark:text-zinc-300">
              <CheckCircleOutlined class="text-emerald-500" />
              <span>{{ t('home.checks.banner') }}</span>
            </div>
            <div class="inline-flex items-center gap-2 text-14px text-slate-600 dark:text-zinc-300">
              <CheckCircleOutlined class="text-emerald-500" />
              <span>{{ t('home.checks.theme') }}</span>
            </div>
          </div>
        </div>

        <div class="min-w-0">
          <ACarousel autoplay class="portal-hero-carousel">
            <button
              v-for="banner in heroBanners"
              :key="banner.id"
              class="portal-hero-slide"
              type="button"
              @click="handleBannerClick(banner)"
            >
              <img :alt="banner.title" :src="banner.image" class="portal-hero-image" />
              <span class="portal-hero-overlay" />
              <span class="portal-hero-copy">
                <span class="portal-hero-kicker">{{ loading ? t('home.loading') : t('home.recommended') }}</span>
                <span class="portal-hero-title">{{ banner.title }}</span>
                <span class="portal-hero-summary">{{ banner.summary || banner.description || t('home.defaultBannerSummary') }}</span>
              </span>
            </button>
          </ACarousel>
        </div>
      </div>
    </section>

    <section id="features" class="portal-section">
      <div class="portal-container">
        <div class="portal-section-head">
          <span class="portal-section-kicker">{{ t('home.featuresKicker') }}</span>
          <h2>{{ t('home.featuresTitle') }}</h2>
        </div>
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <article
            v-for="feature in features"
            :key="feature.title"
            class="rounded-2 border border-slate-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
          >
            <div
              class="mb-5 inline-flex h-11 w-11 items-center justify-center rounded-2 text-22px"
              :class="featureColorClass(feature.color)"
            >
              <component :is="feature.icon" />
            </div>
            <h3 class="m-0 text-18px text-slate-950 font-700 dark:text-white">{{ feature.title }}</h3>
            <p class="mb-0 mt-3 text-14px text-slate-600 leading-7 dark:text-zinc-300">
              {{ feature.description }}
            </p>
          </article>
        </div>
      </div>
    </section>

    <section id="notice" class="portal-section bg-white dark:bg-zinc-950">
      <div class="portal-container grid gap-6 lg:grid-cols-[minmax(0,0.9fr)_minmax(360px,0.6fr)]">
        <div>
          <div class="portal-section-head items-start text-left">
            <span class="portal-section-kicker">{{ t('home.noticesKicker') }}</span>
            <h2>{{ t('home.noticesTitle') }}</h2>
          </div>
          <div class="grid gap-3">
            <article
              v-for="notice in notices"
              :key="notice.title"
              class="rounded-2 border border-slate-200 bg-slate-50 p-5 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div class="min-w-0">
                  <h3 class="m-0 text-16px text-slate-950 font-700 dark:text-white">{{ notice.title }}</h3>
                  <p class="mb-0 mt-2 text-14px text-slate-600 leading-6 dark:text-zinc-300">
                    {{ notice.description }}
                  </p>
                </div>
                <ATag color="blue" class="m-0! shrink-0">{{ notice.tag }}</ATag>
              </div>
            </article>
          </div>
        </div>

        <aside id="support" class="rounded-2 border border-slate-200 bg-slate-50 p-6 dark:border-zinc-800 dark:bg-zinc-900">
          <div class="mb-5 text-18px text-slate-950 font-700 dark:text-white">{{ t('home.quickTitle') }}</div>
          <div class="grid gap-3">
            <button
              v-for="item in quickLinks"
              :key="item.title"
              class="portal-quick-link"
              type="button"
            >
              <span class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2 bg-white text-20px text-blue-600 dark:bg-zinc-800 dark:text-blue-300">
                <component :is="item.icon" />
              </span>
              <span class="min-w-0 flex-1 text-left">
                <span class="block truncate text-15px text-slate-950 font-700 dark:text-white">{{ item.title }}</span>
                <span class="mt-1 block text-13px text-slate-500 dark:text-zinc-400">{{ item.description }}</span>
              </span>
              <RightOutlined class="shrink-0 text-slate-400" />
            </button>
          </div>
        </aside>
      </div>
    </section>
  </main>
</template>
