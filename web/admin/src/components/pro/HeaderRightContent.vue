<script setup lang="ts">
import {
  BellOutlined,
  BgColorsOutlined,
  CheckCircleOutlined,
  GithubOutlined,
  LogoutOutlined,
  MailOutlined,
  QuestionCircleOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { Modal, message } from 'ant-design-vue'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import type { ThemeMode } from '@/stores/app'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import LocaleSwitch from '@/components/pro/LocaleSwitch.vue'

interface InboxItem {
  id: string
  titleKey: string
  descriptionKey: string
  timeKey: string
  type: 'notice' | 'message' | 'todo'
  read?: boolean
}

const themeOptions = [
  { key: 'light', labelKey: 'layout.themeLightMode' },
  { key: 'dark', labelKey: 'layout.themeDarkMode' },
  { key: 'auto', labelKey: 'layout.themeAutoMode' },
] as const

const { t } = useI18n()
const app = useAppStore()
const auth = useAuthStore()
const user = useUserStore()
const router = useRouter()

const activeInboxTab = ref<'notice' | 'message' | 'todo'>('notice')
const inboxItems = ref<InboxItem[]>([
  {
    id: 'notice-1',
    titleKey: 'layout.inbox.noticeSyncTitle',
    descriptionKey: 'layout.inbox.noticeSyncDesc',
    timeKey: 'layout.inbox.minutes10',
    type: 'notice',
  },
  {
    id: 'notice-2',
    titleKey: 'layout.inbox.storageTitle',
    descriptionKey: 'layout.inbox.storageDesc',
    timeKey: 'layout.inbox.minutes32',
    type: 'notice',
  },
  {
    id: 'message-1',
    titleKey: 'layout.inbox.accountApplyTitle',
    descriptionKey: 'layout.inbox.accountApplyDesc',
    timeKey: 'layout.inbox.hour1',
    type: 'message',
  },
  {
    id: 'message-2',
    titleKey: 'layout.inbox.exportTitle',
    descriptionKey: 'layout.inbox.exportDesc',
    timeKey: 'layout.inbox.yesterday',
    type: 'message',
  },
  {
    id: 'todo-1',
    titleKey: 'layout.inbox.lockedTitle',
    descriptionKey: 'layout.inbox.lockedDesc',
    timeKey: 'layout.inbox.today',
    type: 'todo',
  },
])

const visibleInboxItems = computed(() => inboxItems.value.filter((item) => item.type === activeInboxTab.value))
const unreadNoticeCount = computed(() => inboxItems.value.filter((item) => !item.read && item.type === 'notice').length)
const unreadMessageCount = computed(() => inboxItems.value.filter((item) => !item.read && item.type === 'message').length)
const todoCount = computed(() => inboxItems.value.filter((item) => !item.read && item.type === 'todo').length)
const totalUnreadCount = computed(() => unreadNoticeCount.value + unreadMessageCount.value + todoCount.value)

const themeLabel = computed(() => {
  if (app.themeMode === 'light') {
    return t('layout.themeLight')
  }
  if (app.themeMode === 'dark') {
    return t('layout.themeDark')
  }
  return t('layout.themeAuto')
})
const profileName = computed(() => user.profile?.real_name || t('layout.profileFallback'))
const profileTitle = computed(() => user.profile?.title || t('layout.profileTitleFallback'))
const avatarText = computed(() => profileName.value.slice(0, 1))

const actionButtonClass =
  'inline-flex h-9 min-w-9 cursor-pointer items-center justify-center rounded-2 border-0 bg-transparent px-2 text-16px text-slate-600 transition hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500/35 dark:text-zinc-300 dark:hover:bg-zinc-800'
const themeButtonClass = `${actionButtonClass} header-theme-trigger`

function openGithub() {
  window.open('https://github.com/jiangbyte/hei-fastapi', '_blank', 'noopener,noreferrer')
}

function setThemeMode(mode: ThemeMode) {
  app.setThemeMode(mode)
}

function markVisibleRead() {
  inboxItems.value = inboxItems.value.map((item) =>
    item.type === activeInboxTab.value ? { ...item, read: true } : item,
  )
}

function markAllRead() {
  inboxItems.value = inboxItems.value.map((item) => ({ ...item, read: true }))
}

function openSearch() {
  message.info(t('layout.searchReady'))
}

function handleLogout() {
  Modal.confirm({
    title: t('layout.logout'),
    content: t('layout.logoutConfirm'),
    async onOk() {
      await auth.logout()
      router.push('/auth/login')
    },
  })
}

const handleThemeMenuClick: MenuProps['onClick'] = ({ key }) => {
  setThemeMode(key as ThemeMode)
}
</script>

<template>
  <div class="ml-auto flex h-14 shrink-0 items-center gap-1 overflow-hidden">
    <ATooltip :title="t('layout.siteSearch')">
      <button :class="`${actionButtonClass} hidden md:inline-flex`" type="button" @click="openSearch">
        <SearchOutlined />
      </button>
    </ATooltip>

    <APopover trigger="click" placement="bottomRight" :overlay-style="{ width: '320px' }">
      <button :class="`${actionButtonClass} hidden sm:inline-flex`" type="button">
        <QuestionCircleOutlined />
      </button>
      <template #content>
        <div class="w-72">
          <div class="mb-3 text-15px text-slate-900 font-600 dark:text-zinc-100">{{ t('layout.helpCenter') }}</div>
          <div class="grid gap-2">
            <AButton block class="justify-start" type="text">{{ t('layout.docs') }}</AButton>
            <AButton block class="justify-start" type="text">{{ t('layout.permissionGuide') }}</AButton>
            <AButton block class="justify-start" type="text">{{ t('layout.feedback') }}</AButton>
          </div>
          <ADivider class="my-3" />
          <div class="text-12px text-slate-500 dark:text-zinc-400">Hei Admin · Enterprise Console</div>
        </div>
      </template>
    </APopover>

    <ATooltip :title="t('layout.github')">
      <button :class="`${actionButtonClass} hidden sm:inline-flex`" type="button" @click="openGithub">
        <GithubOutlined />
      </button>
    </ATooltip>

    <LocaleSwitch :button-class="actionButtonClass" />

    <ATooltip :title="t('layout.theme', { theme: themeLabel })">
      <ADropdown placement="bottomRight" :trigger="['click']">
        <button :aria-label="t('layout.theme', { theme: themeLabel })" :class="themeButtonClass" type="button">
          <BgColorsOutlined />
        </button>
        <template #overlay>
          <AMenu :selected-keys="[app.themeMode]" @click="handleThemeMenuClick">
            <AMenuItem v-for="option in themeOptions" :key="option.key">
              <span class="inline-flex items-center">
                <BgColorsOutlined class="mr-2 text-15px" />
                <span>{{ t(option.labelKey) }}</span>
              </span>
            </AMenuItem>
          </AMenu>
        </template>
      </ADropdown>
    </ATooltip>

    <APopover trigger="click" placement="bottomRight" :overlay-style="{ width: '360px' }">
      <button :class="actionButtonClass" type="button">
        <ABadge :count="totalUnreadCount" size="small">
          <BellOutlined class="text-slate-600 dark:text-zinc-300" />
        </ABadge>
      </button>
      <template #content>
        <div class="w-82 max-w-[calc(100vw-32px)]">
          <ATabs v-model:active-key="activeInboxTab" size="small" centered>
            <ATabPane key="notice">
              <template #tab>{{ t('layout.notice') }} {{ unreadNoticeCount }}</template>
            </ATabPane>
            <ATabPane key="message">
              <template #tab>{{ t('layout.message') }} {{ unreadMessageCount }}</template>
            </ATabPane>
            <ATabPane key="todo">
              <template #tab>{{ t('layout.todo') }} {{ todoCount }}</template>
            </ATabPane>
          </ATabs>

          <AEmpty v-if="visibleInboxItems.length === 0" class="py-6" :description="t('common.empty')" />
          <AList v-else item-layout="horizontal" :data-source="visibleInboxItems">
            <template #renderItem="{ item }">
              <AListItem class="cursor-pointer rounded-2 px-2 transition hover:bg-slate-50 dark:hover:bg-zinc-800">
                <AListItemMeta :description="t(item.descriptionKey)">
                  <template #avatar>
                    <AAvatar :size="32" :class="item.read ? 'bg-slate-300' : 'bg-brand-500'">
                      <BellOutlined v-if="item.type === 'notice'" />
                      <MailOutlined v-else-if="item.type === 'message'" />
                      <CheckCircleOutlined v-else />
                    </AAvatar>
                  </template>
                  <template #title>
                    <div class="flex items-center justify-between gap-3">
                      <span class="truncate">{{ t(item.titleKey) }}</span>
                      <span class="shrink-0 text-12px text-slate-400">{{ t(item.timeKey) }}</span>
                    </div>
                  </template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>

          <div class="mt-3 flex border-t border-slate-100 pt-3 dark:border-zinc-800">
            <AButton block type="link" @click="markVisibleRead">{{ t('layout.markCurrentRead') }}</AButton>
            <AButton block type="link" @click="markAllRead">{{ t('layout.markAllRead') }}</AButton>
          </div>
        </div>
      </template>
    </APopover>

    <ADropdown placement="bottomRight" :trigger="['click']">
      <button
        class="inline-flex h-10 cursor-pointer items-center gap-2 rounded-2 border-0 bg-transparent px-1.5 text-slate-700 transition hover:bg-slate-100 dark:text-zinc-200 dark:hover:bg-zinc-800 lg:px-2"
        type="button"
      >
        <AAvatar :size="28" class="bg-brand-500 text-white">
          {{ avatarText }}
        </AAvatar>
        <span class="hidden max-w-24 truncate text-13px font-500 lg:inline">{{ profileName }}</span>
      </button>
      <template #overlay>
        <AMenu :selected-keys="[]">
          <AMenuItem key="profile-card" disabled>
            <div class="flex min-w-52 items-center gap-3 py-1">
              <AAvatar :size="38" class="bg-brand-500 text-white">{{ avatarText }}</AAvatar>
              <div class="min-w-0">
                <div class="truncate text-slate-900 font-600 dark:text-zinc-100">{{ profileName }}</div>
                <div class="truncate text-12px text-slate-500 dark:text-zinc-400">{{ profileTitle }}</div>
              </div>
            </div>
          </AMenuItem>
          <AMenuDivider />
          <AMenuItem key="center" @click="router.push('/profile')">
            <UserOutlined />
            {{ t('layout.profileCenter') }}
          </AMenuItem>
          <AMenuDivider />
          <AMenuItem key="logout" @click="handleLogout">
            <LogoutOutlined />
            {{ t('layout.logout') }}
          </AMenuItem>
        </AMenu>
      </template>
    </ADropdown>
  </div>
</template>
