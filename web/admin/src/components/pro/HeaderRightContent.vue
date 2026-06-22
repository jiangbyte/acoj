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
import { useRouter } from 'vue-router'

import type { ThemeMode } from '@/stores/app'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

interface InboxItem {
  id: string
  title: string
  description: string
  time: string
  type: 'notice' | 'message' | 'todo'
  read?: boolean
}

const themeOptions = [
  { key: 'light', label: '明亮模式' },
  { key: 'dark', label: '暗黑模式' },
  { key: 'auto', label: '跟随系统' },
] as const

const app = useAppStore()
const auth = useAuthStore()
const user = useUserStore()
const router = useRouter()

const activeInboxTab = ref<'notice' | 'message' | 'todo'>('notice')
const inboxItems = ref<InboxItem[]>([
  {
    id: 'notice-1',
    title: '权限资源同步完成',
    description: '系统菜单与接口权限已完成最新一次同步。',
    time: '10 分钟前',
    type: 'notice',
  },
  {
    id: 'notice-2',
    title: '文件存储巡检通过',
    description: '本地与对象存储连接状态正常。',
    time: '32 分钟前',
    type: 'notice',
  },
  {
    id: 'message-1',
    title: '运营部门提交账号申请',
    description: '请审核 3 个新成员的管理端访问权限。',
    time: '1 小时前',
    type: 'message',
  },
  {
    id: 'message-2',
    title: '审计员请求导出记录',
    description: '请求导出最近 7 天文件访问明细。',
    time: '昨天',
    type: 'message',
  },
  {
    id: 'todo-1',
    title: '处理锁定账号',
    description: '2 个账号因连续登录失败进入锁定状态。',
    time: '今天',
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
    return '明亮'
  }
  if (app.themeMode === 'dark') {
    return '暗黑'
  }
  return '自动'
})
const profileName = computed(() => user.profile?.real_name || '管理员')
const profileTitle = computed(() => user.profile?.title || '系统管理员')
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
  message.info('站内搜索功能已就绪，可在后续接入全局检索数据源。')
}

function handleLogout() {
  Modal.confirm({
    title: '退出登录',
    content: '确认退出当前管理端会话？',
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
    <ATooltip title="站内搜索">
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
          <div class="mb-3 text-15px text-slate-900 font-600 dark:text-zinc-100">帮助中心</div>
          <div class="grid gap-2">
            <AButton block class="justify-start" type="text">使用文档</AButton>
            <AButton block class="justify-start" type="text">权限配置指南</AButton>
            <AButton block class="justify-start" type="text">问题反馈</AButton>
          </div>
          <ADivider class="my-3" />
          <div class="text-12px text-slate-500 dark:text-zinc-400">Hei Admin · Enterprise Console</div>
        </div>
      </template>
    </APopover>

    <ATooltip title="GitHub">
      <button :class="`${actionButtonClass} hidden sm:inline-flex`" type="button" @click="openGithub">
        <GithubOutlined />
      </button>
    </ATooltip>

    <ATooltip :title="`主题：${themeLabel}`">
      <ADropdown placement="bottomRight" :trigger="['click']">
        <button :aria-label="`主题：${themeLabel}`" :class="themeButtonClass" type="button">
          <BgColorsOutlined />
        </button>
        <template #overlay>
          <AMenu :selected-keys="[app.themeMode]" @click="handleThemeMenuClick">
            <AMenuItem v-for="option in themeOptions" :key="option.key">
              <span class="inline-flex items-center">
                <BgColorsOutlined class="mr-2 text-15px" />
                <span>{{ option.label }}</span>
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
              <template #tab>通知 {{ unreadNoticeCount }}</template>
            </ATabPane>
            <ATabPane key="message">
              <template #tab>消息 {{ unreadMessageCount }}</template>
            </ATabPane>
            <ATabPane key="todo">
              <template #tab>待办 {{ todoCount }}</template>
            </ATabPane>
          </ATabs>

          <AEmpty v-if="visibleInboxItems.length === 0" class="py-6" description="暂无内容" />
          <AList v-else item-layout="horizontal" :data-source="visibleInboxItems">
            <template #renderItem="{ item }">
              <AListItem class="cursor-pointer rounded-2 px-2 transition hover:bg-slate-50 dark:hover:bg-zinc-800">
                <AListItemMeta :description="item.description">
                  <template #avatar>
                    <AAvatar :size="32" :class="item.read ? 'bg-slate-300' : 'bg-brand-500'">
                      <BellOutlined v-if="item.type === 'notice'" />
                      <MailOutlined v-else-if="item.type === 'message'" />
                      <CheckCircleOutlined v-else />
                    </AAvatar>
                  </template>
                  <template #title>
                    <div class="flex items-center justify-between gap-3">
                      <span class="truncate">{{ item.title }}</span>
                      <span class="shrink-0 text-12px text-slate-400">{{ item.time }}</span>
                    </div>
                  </template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>

          <div class="mt-3 flex border-t border-slate-100 pt-3 dark:border-zinc-800">
            <AButton block type="link" @click="markVisibleRead">当前已读</AButton>
            <AButton block type="link" @click="markAllRead">全部已读</AButton>
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
            个人中心
          </AMenuItem>
          <AMenuDivider />
          <AMenuItem key="logout" @click="handleLogout">
            <LogoutOutlined />
            退出登录
          </AMenuItem>
        </AMenu>
      </template>
    </ADropdown>
  </div>
</template>
