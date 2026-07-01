<script setup lang="ts">
import { usePreferredDark } from '@vueuse/core'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import DarkModeSwitch from '@/components/common/DarkModeSwitch.vue'
import LanguageSwitch from '@/layouts/components/header/LanguageSwitch.vue'
import { useAppStore } from '@/stores'

defineProps<{
  title: string
  subtitle: string
  wide?: boolean
}>()

const { t } = useI18n()
const appStore = useAppStore()
const prefersDark = usePreferredDark()
const appTitle = import.meta.env.VITE_APP_TITLE
const copyright = import.meta.env.VITE_COPYRIGHT_INFO
const isHeaderElevated = ref(false)

const isDarkTheme = computed(
  () =>
    appStore.storeColorMode === 'dark' || (appStore.storeColorMode === 'auto' && prefersDark.value),
)

const highlights = computed(() => [
  {
    icon: 'icon-park-outline:shield',
    title: t('auth.highlight_security_title'),
    text: t('auth.highlight_security_text'),
  },
  {
    icon: 'icon-park-outline:code-computer',
    title: t('auth.highlight_workspace_title'),
    text: t('auth.highlight_workspace_text'),
  },
  {
    icon: 'icon-park-outline:chart-line',
    title: t('auth.highlight_analytics_title'),
    text: t('auth.highlight_analytics_text'),
  },
])

function handleShellScroll(event: Event) {
  isHeaderElevated.value = (event.currentTarget as HTMLElement).scrollTop > 8
}
</script>

<template>
  <main class="auth-shell" :class="{ 'auth-shell--dark': isDarkTheme }" @scroll="handleShellScroll">
    <header class="auth-header" :class="{ 'auth-header--elevated': isHeaderElevated }">
      <RouterLink class="auth-brand" to="/auth/login" :aria-label="appTitle">
        <span class="auth-brand-icon">
          <NovaIcon icon="icon-park-outline:code-computer" :size="24" />
        </span>
        <span class="auth-brand-text">{{ appTitle }}</span>
      </RouterLink>

      <div class="auth-tools">
        <LanguageSwitch />
        <DarkModeSwitch />
      </div>
    </header>

    <section class="auth-content" :class="{ 'auth-content--wide': wide }">
      <aside class="auth-visual" aria-hidden="true">
        <div class="auth-visual-inner">
          <p class="auth-kicker">{{ t('auth.kicker') }}</p>
          <h1>{{ t('auth.hero_title') }}</h1>
          <p class="auth-hero-text">{{ t('auth.hero_subtitle') }}</p>

          <div class="auth-highlights">
            <div v-for="item in highlights" :key="item.title" class="auth-highlight">
              <NovaIcon :icon="item.icon" :size="22" />
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.text }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <section class="auth-panel" :aria-label="title">
        <n-card class="auth-card" :class="{ 'auth-card--wide': wide }" :bordered="false">
          <div class="auth-form-header">
            <h2>{{ title }}</h2>
            <p>{{ subtitle }}</p>
          </div>

          <slot />
        </n-card>
      </section>
    </section>

    <footer class="auth-footer">
      {{ copyright }}
    </footer>
  </main>
</template>

<style scoped>
.auth-shell {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 100vh;
  overflow: auto;
  --auth-bg: #f8fafc;
  --auth-text: #1f2937;
  --auth-text-2: #4b5563;
  --auth-text-3: #6b7280;
  --auth-border: rgba(148, 163, 184, 0.24);
  --auth-surface: rgba(255, 255, 255, 0.76);
  --auth-kicker-color: #0f766e;
  --auth-kicker-bg: rgba(20, 184, 166, 0.12);
  --auth-kicker-border: rgba(20, 184, 166, 0.22);
  --auth-header-bg: #ffffff;
  --auth-header-border: rgba(148, 163, 184, 0.18);
  --auth-header-shadow: 0 6px 16px rgba(15, 23, 42, 0.035);
  --auth-card-border: transparent;
  --auth-card-shadow: 0 24px 70px rgba(15, 23, 42, 0.14);
  --auth-primary: #2563eb;
  color: var(--auth-text);
  background: var(--auth-bg);
}

.auth-shell--dark {
  --auth-bg: #18181c;
  --auth-text: #e5e7eb;
  --auth-text-2: #b9bec9;
  --auth-text-3: #8f96a3;
  --auth-border: rgba(255, 255, 255, 0.08);
  --auth-surface: rgba(36, 36, 41, 0.72);
  --auth-kicker-color: #5eead4;
  --auth-kicker-bg: rgba(20, 184, 166, 0.1);
  --auth-kicker-border: rgba(94, 234, 212, 0.18);
  --auth-header-bg: #18181c;
  --auth-header-border: rgba(255, 255, 255, 0.08);
  --auth-header-shadow: 0 6px 16px rgba(0, 0, 0, 0.16);
  --auth-card-border: rgba(255, 255, 255, 0.08);
  --auth-card-shadow: 0 18px 48px rgba(0, 0, 0, 0.28);
}

.auth-header {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 72px;
  padding: 0 clamp(20px, 4vw, 56px);
  background: var(--auth-header-bg);
  border-bottom: 1px solid var(--auth-header-border);
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.auth-header--elevated {
  box-shadow: var(--auth-header-shadow);
}

.auth-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.auth-brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  color: #ffffff;
  background: var(--auth-primary);
  border-radius: 8px;
}

.auth-brand-text {
  overflow: hidden;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-tools {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 6px;
}

.auth-content {
  z-index: 1;
  display: grid;
  flex: 1 0 auto;
  grid-template-columns: minmax(0, 1.05fr) minmax(440px, 0.95fr);
  gap: clamp(28px, 5vw, 72px);
  align-items: center;
  width: min(1180px, calc(100% - 40px));
  min-height: 0;
  margin: 0 auto;
  padding: 28px 0 36px;
}

.auth-content--wide {
  grid-template-columns: minmax(0, 0.8fr) minmax(640px, 1.2fr);
  width: min(1320px, calc(100% - 40px));
}

.auth-visual {
  min-width: 0;
}

.auth-visual-inner {
  display: grid;
  gap: 24px;
  max-width: 560px;
}

.auth-kicker {
  width: fit-content;
  padding: 6px 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--auth-kicker-color);
  background: var(--auth-kicker-bg);
  border: 1px solid var(--auth-kicker-border);
  border-radius: 8px;
}

.auth-visual h1 {
  max-width: 540px;
  font-size: clamp(36px, 5vw, 56px);
  font-weight: 800;
  line-height: 1.05;
}

.auth-hero-text {
  max-width: 520px;
  font-size: 16px;
  line-height: 1.8;
  color: var(--auth-text-2);
}

.auth-highlights {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.auth-highlight {
  display: grid;
  gap: 10px;
  min-width: 0;
  padding: 14px;
  background: var(--auth-surface);
  border: 1px solid var(--auth-border);
  border-radius: 8px;
  backdrop-filter: blur(16px);
}

.auth-highlight strong,
.auth-highlight span {
  display: block;
}

.auth-highlight strong {
  margin-bottom: 4px;
  font-size: 14px;
}

.auth-highlight span {
  font-size: 12px;
  line-height: 1.6;
  color: var(--auth-text-2);
}

.auth-panel {
  display: flex;
  justify-content: center;
  min-width: 0;
}

.auth-card {
  width: min(100%, 480px);
  border-radius: 8px;
  border: 1px solid var(--auth-card-border);
  box-shadow: var(--auth-card-shadow);
}

.auth-card--wide {
  width: min(100%, 720px);
}

.auth-form-header {
  margin-bottom: 24px;
}

.auth-form-header h2 {
  margin-bottom: 8px;
  font-size: 28px;
  line-height: 1.2;
  color: var(--auth-text);
}

.auth-form-header p {
  line-height: 1.6;
  color: var(--auth-text-2);
}

.auth-footer {
  z-index: 1;
  flex: 0 0 auto;
  padding: 0 20px 18px;
  font-size: 13px;
  text-align: center;
  color: var(--auth-text-3);
}

:deep(.n-card__content) {
  padding: clamp(24px, 4vw, 36px);
}

@media (max-width: 920px) {
  .auth-content {
    display: grid;
    grid-template-columns: 1fr;
    align-items: center;
    justify-content: center;
    gap: 20px;
    width: min(100% - 32px, 520px);
    padding: 24px 0 28px;
  }

  .auth-panel,
  .auth-card {
    width: 100%;
  }

  .auth-visual {
    width: 100%;
  }

  .auth-visual-inner {
    gap: 10px;
    max-width: none;
    text-align: center;
  }

  .auth-kicker {
    margin: 0 auto;
  }

  .auth-visual h1 {
    max-width: none;
    font-size: 28px;
    line-height: 1.2;
  }

  .auth-hero-text {
    max-width: none;
    font-size: 14px;
    line-height: 1.7;
  }

  .auth-highlights {
    display: none;
  }
}

@media (min-width: 921px) and (max-width: 1120px) {
  .auth-content--wide {
    grid-template-columns: 1fr;
    width: min(100% - 32px, 760px);
  }

  .auth-content--wide .auth-visual {
    display: none;
  }
}

@media (max-width: 520px) {
  .auth-content {
    width: calc(100% - 28px);
    gap: 16px;
    padding: 16px 0 24px;
  }

  .auth-kicker {
    font-size: 12px;
  }

  .auth-visual h1 {
    font-size: 24px;
  }

  .auth-card {
    box-shadow: var(--auth-card-shadow);
  }

  .auth-form-header h2 {
    font-size: 24px;
  }
}
</style>
