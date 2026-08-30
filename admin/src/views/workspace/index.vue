<!--
  Author: Charlie

  工作台：轮播与公告置顶并列，资料/应用、登录日志分区展示。
-->
<script setup lang="ts">
import { Icon } from '@iconify/vue/offline'
import { NIcon, NTag } from 'naive-ui'
import { bannerApi, myNoticeApi, workspaceApi } from '@/api'
import MessageDetailModal from '@/components/sys/MessageDetailModal.vue'
import { useAuthStore, useRouteStore } from '@/stores'
import { accountTypeLabel } from '@/constants/account'
import { createTagColor, formatDateTime, hasPermission, plainTextExcerpt } from '@/utils'
import { wireBool } from '@/utils/wire'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

type ShortcutItem = {
  id?: string
  resource_id: string
  name: string
  path: string
  icon?: string | null
  sort?: number
}

type ActivityItem = {
  id?: string
  module?: string
  action?: string
  summary?: string
  success?: boolean | string
  ip?: string
  user_agent?: string
  created_at?: string
}

type NoticeItem = {
  id: string
  title: string
  content?: string
  kind?: string
  severity?: string
  publish_at?: string
  is_read?: boolean
  publish_locations?: Record<string, unknown>
}

const authStore = useAuthStore()
const routeStore = useRouteStore()
const router = useRouter()
const clockTimer = ref<number | null>(null)
const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
const appTitle = import.meta.env.VITE_APP_TITLE || 'HEI Admin'

const state = reactive({
  loading: false,
  savingShortcuts: false,
  showShortcutEditor: false,
  draftResourceIds: [] as string[],
  now: Date.now(),
  banners: [] as any[],
  notices: [] as NoticeItem[],
  shortcuts: [] as ShortcutItem[],
  recentLogins: [] as ActivityItem[],
})

const candidateMenus = computed(() => {
  return [...routeStore.rowRoutes]
    .filter(
      (item) =>
        item.resource_type === 'MENU' &&
        item.path &&
        item.path !== '/workspace' &&
        item.is_visible !== false,
    )
    .sort((a, b) => Number(a.sort ?? 99) - Number(b.sort ?? 99))
    .map((item) => ({
      id: item.id,
      name: item.name,
      path: item.path as string,
      icon: item.icon || 'icon-park-outline:application-one',
    }))
})

const displayName = computed(() => {
  const nickname = String(authStore.userInfo?.nickname ?? '').trim()
  return nickname || '-'
})

const avatarUrl = computed(() => authStore.userInfo?.avatar || undefined)
const deptText = computed(() => mapNames(authStore.userInfo?.deptIdNames) || '未分配部门')
const roleText = computed(() => mapNames(authStore.userInfo?.roleIdNames) || '未分配角色')
const accountTypeText = computed(() => {
  const type = String(authStore.userInfo?.accountType ?? '')
  return accountTypeLabel(type) || type || '账号'
})

const greeting = computed(() => {
  const hour = new Date(state.now).getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const clockText = computed(() => {
  const date = new Date(state.now)
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}.${m}.${d} 周${weekdays[date.getDay()]} ${hh}:${mm}`
})

const unreadNoticeCount = computed(() => state.notices.filter((item) => !item.is_read).length)
const canViewWorkspace = computed(() => hasPermission('workspace:overview:view'))

onMounted(() => {
  void fetchOverview()
  void loadBanners()
  void loadNotices()
  clockTimer.value = window.setInterval(() => {
    state.now = Date.now()
  }, 30_000)
})

onBeforeUnmount(() => {
  if (clockTimer.value != null) {
    window.clearInterval(clockTimer.value)
    clockTimer.value = null
  }
})

async function fetchOverview() {
  if (!canViewWorkspace.value) {
    state.shortcuts = []
    state.recentLogins = []
    return
  }
  state.loading = true
  try {
    const response = await workspaceApi.overview()
    const data = response.data ?? {}
    state.shortcuts = Array.isArray(data.shortcuts) ? data.shortcuts : []
    state.recentLogins = Array.isArray(data.recent_logins) ? data.recent_logins : []
  } catch {
    state.shortcuts = []
    state.recentLogins = []
  } finally {
    state.loading = false
  }
}

async function loadBanners() {
  try {
    const response = await bannerApi.list({ position: 'ADMIN_TOP' })
    state.banners = Array.isArray(response.data) ? response.data : []
  } catch {
    state.banners = []
  }
}

async function loadNotices() {
  try {
    const response = await myNoticeApi.myPage({
      current: 1,
      size: 30,
      kind: 'ANNOUNCEMENT',
    })
    const records = Array.isArray(response.data?.records) ? response.data.records : []
    state.notices = records
      .filter((item: NoticeItem) => isWorkspaceNotice(item))
      .slice(0, 8)
      .map((item: NoticeItem) => ({
        ...item,
        is_read: wireBool(item.is_read ?? false),
      }))
  } catch {
    state.notices = []
  }
}

function isWorkspaceNotice(item: NoticeItem) {
  const locations = item.publish_locations
  if (!locations || typeof locations !== 'object') return false
  return wireBool(locations.workspace as boolean | string | undefined)
}

function noticeExcerpt(item: NoticeItem) {
  const text = plainTextExcerpt(item.content, 64)
  return text || '查看详情'
}

async function openNotice(item: NoticeItem) {
  await detailModalRef.value?.open(
    {
      id: item.id,
      sourceType: 'ANNOUNCEMENT',
      title: item.title,
      is_read: item.is_read,
      publish_at: item.publish_at,
      content: item.content,
      severity: item.severity,
      content_type: (item as any).content_type,
      kind: 'ANNOUNCEMENT',
    },
    { mode: 'detail', markReadOnOpen: true },
  )
}

function handleNoticeChanged(payload: { type: string; id: string }) {
  const row = state.notices.find((item) => item.id === payload.id)
  if (row) row.is_read = true
}

function openShortcutEditor() {
  if (!canViewWorkspace.value) {
    return
  }
  state.draftResourceIds = state.shortcuts.map((item) => item.resource_id).filter(Boolean)
  state.showShortcutEditor = true
}

function toggleDraft(resourceId: string) {
  const idx = state.draftResourceIds.indexOf(resourceId)
  if (idx >= 0) {
    state.draftResourceIds.splice(idx, 1)
    return
  }
  if (state.draftResourceIds.length >= 16) {
    window.$message.warning('最多添加 16 个快捷应用')
    return
  }
  state.draftResourceIds.push(resourceId)
}

function isDraftSelected(resourceId: string) {
  return state.draftResourceIds.includes(resourceId)
}

async function saveShortcuts() {
  state.savingShortcuts = true
  try {
    const response = await workspaceApi.saveShortcuts(state.draftResourceIds)
    state.shortcuts = Array.isArray(response.data) ? response.data : []
    state.showShortcutEditor = false
    window.$message.success('常用应用已保存')
  } finally {
    state.savingShortcuts = false
  }
}

function mapNames(items?: Array<{ id?: string; name?: string }>) {
  return (items ?? [])
    .map((item) => item.name)
    .filter(Boolean)
    .join(' / ')
}

function successLabel(value: boolean | string | undefined) {
  return wireBool(value) ? '成功' : '失败'
}

function go(path?: string) {
  if (!path) return
  router.push(path)
}

function goProfileTab(tab: string) {
  router.push({ path: '/profile', query: { tab } })
}

function openBanner(banner: any) {
  const link = String(banner?.url || '').trim()
  if (!link || banner?.link_type === 'NONE') return
  if (banner.link_type === 'ROUTE' || link.startsWith('/')) {
    if (!link.startsWith('/') || link.startsWith('//')) return
    router.push(link)
    return
  }
  if (!/^https?:\/\//i.test(link)) return
  window.open(link, '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <NSpin :show="state.loading">
    <n-el class="board">
      <section
        class="layout"
        :class="{ 'layout--banner': state.banners.length }"
      >
        <section
          v-if="state.banners.length"
          class="promo"
        >
          <NCarousel
            autoplay
            :interval="5000"
            show-dots
            draggable
          >
            <button
              v-for="banner in state.banners"
              :key="banner.id"
              type="button"
              class="promo__slide"
              @click="openBanner(banner)"
            >
              <img
                v-if="banner.image_url || banner.image"
                :src="banner.image_url || banner.image"
                :alt="banner.title"
                class="promo__image"
              >
              <div class="promo__veil" />
              <div class="promo__text">
                <strong>{{ banner.title }}</strong>
                <span v-if="banner.summary">{{ banner.summary }}</span>
              </div>
            </button>
          </NCarousel>
        </section>

        <section class="card notice">
          <div class="card__head">
            <div>
              <div class="card__title card__title--with-tag">
                公告
                <NTag
                  v-if="unreadNoticeCount"
                  size="small"
                  round
                  :bordered="false"
                  :color="createTagColor('#1677ff')"
                >
                  {{ unreadNoticeCount }} 未读
                </NTag>
              </div>
              <div class="card__sub">
                工作台通知与系统公告
              </div>
            </div>
            <NButton
              text
              type="primary"
              @click="goProfileTab('my_messages')"
            >
              更多
            </NButton>
          </div>
          <div
            v-if="state.notices.length"
            class="notice__list"
          >
            <button
              v-for="item in state.notices"
              :key="item.id"
              type="button"
              class="notice__item"
              :class="{ 'is-unread': !item.is_read }"
              @click="openNotice(item)"
            >
              <strong :title="item.title">{{ item.title }}</strong>
              <span :title="noticeExcerpt(item)">{{ noticeExcerpt(item) }}</span>
              <em v-if="item.publish_at">{{ formatDateTime(item.publish_at) }}</em>
            </button>
          </div>
          <NEmpty
            v-else
            description="暂无公告"
            size="small"
          />
        </section>

        <section class="card profile">
          <div class="profile__who">
            <NAvatar
              v-if="avatarUrl"
              round
              :size="52"
              :src="avatarUrl"
              :img-props="avatarImgProps"
            />
            <NAvatar
              v-else
              round
              :size="52"
            >
              <NIcon :size="24">
                <Icon icon="icon-park-outline:user" />
              </NIcon>
            </NAvatar>
            <div>
              <div class="profile__hello">
                {{ displayName }}，{{ greeting }}
              </div>
              <div class="profile__meta">
                {{ roleText }}
              </div>
            </div>
          </div>
          <div class="profile__list">
            <div><span>账号类型</span><strong>{{ accountTypeText }}</strong></div>
            <div><span>所属部门</span><strong>{{ deptText }}</strong></div>
            <div><span>当前系统</span><strong>{{ appTitle }}</strong></div>
            <div><span>本地时间</span><strong>{{ clockText }}</strong></div>
          </div>
          <NButton
            block
            secondary
            @click="go('/profile')"
          >
            个人中心
          </NButton>
        </section>

        <section class="card apps">
            <div class="card__head">
              <div>
                <div class="card__title">
                  常用应用
                </div>
                <div class="card__sub">
                  按个人习惯固定入口，最多 16 个
                </div>
              </div>
              <NButton
                v-if="canViewWorkspace"
                text
                type="primary"
                @click="openShortcutEditor"
              >
                管理应用
              </NButton>
            </div>

            <div
              v-if="state.shortcuts.length"
              class="apps__grid"
            >
              <button
                v-for="item in state.shortcuts"
                :key="item.resource_id"
                type="button"
                class="app"
                @click="go(item.path)"
              >
                <span class="app__icon">
                  <NIcon :size="22">
                    <Icon :icon="item.icon || 'icon-park-outline:application-one'" />
                  </NIcon>
                </span>
                <span class="app__name">{{ item.name }}</span>
              </button>
              <button
                v-if="canViewWorkspace"
                type="button"
                class="app app--add"
                @click="openShortcutEditor"
              >
                <span class="app__icon">
                  <NIcon :size="22">
                    <Icon icon="icon-park-outline:plus" />
                  </NIcon>
                </span>
                <span class="app__name">添加</span>
              </button>
            </div>
            <div
              v-else
              class="apps__empty"
            >
              <p>还没有常用应用，从已授权菜单里挑选几个放在这里。</p>
              <NButton
                v-if="canViewWorkspace"
                type="primary"
                secondary
                @click="openShortcutEditor"
              >
                添加常用应用
              </NButton>
            </div>
          </section>

        <section class="card activity">
            <div class="card__head">
              <div>
                <div class="card__title">
                  我的登录日志
                </div>
                <div class="card__sub">
                  最近 10 条本人登录记录
                </div>
              </div>
              <NButton
                text
                type="primary"
                @click="goProfileTab('my_logins')"
              >
                全部
              </NButton>
            </div>
            <table
              v-if="state.recentLogins.length"
              class="dense-table"
            >
              <thead>
                <tr>
                  <th>时间</th>
                  <th>结果</th>
                  <th>操作内容</th>
                  <th>IP</th>
                  <th>User-Agent</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in state.recentLogins"
                  :key="row.id"
                >
                  <td>{{ formatDateTime(row.created_at) }}</td>
                  <td>
                    <NTag
                      size="small"
                      :color="createTagColor(wireBool(row.success) ? '#52c41a' : '#ff4d4f')"
                      :bordered="false"
                    >
                      {{ successLabel(row.success) }}
                    </NTag>
                  </td>
                  <td
                    class="is-clip"
                    :title="row.summary"
                  >
                    {{ row.summary || '-' }}
                  </td>
                  <td>{{ row.ip || '-' }}</td>
                  <td
                    class="is-clip"
                    :title="row.user_agent"
                  >
                    {{ row.user_agent || '-' }}
                  </td>
                </tr>
              </tbody>
            </table>
            <NEmpty
              v-else
              description="暂无登录记录"
              size="small"
            />
          </section>
      </section>

      <NModal
        v-model:show="state.showShortcutEditor"
        preset="card"
        title="管理常用应用"
        style="width: min(720px, 92vw)"
        :bordered="false"
        :segmented="{ content: true, footer: true }"
      >
        <div class="editor__tip">
          已选 {{ state.draftResourceIds.length }} / 16。仅展示当前账号已授权菜单。
        </div>
        <div class="editor__grid">
          <button
            v-for="item in candidateMenus"
            :key="item.id"
            type="button"
            class="editor__item"
            :class="{ 'is-on': isDraftSelected(item.id) }"
            @click="toggleDraft(item.id)"
          >
            <NIcon :size="18">
              <Icon :icon="item.icon" />
            </NIcon>
            <span>{{ item.name }}</span>
          </button>
        </div>
        <NEmpty
          v-if="!candidateMenus.length"
          description="暂无可选菜单"
          size="small"
        />
        <template #footer>
          <div class="editor__footer">
            <NButton @click="state.showShortcutEditor = false">
              取消
            </NButton>
            <NButton
              type="primary"
              :loading="state.savingShortcuts"
              @click="saveShortcuts"
            >
              保存
            </NButton>
          </div>
        </template>
      </NModal>

      <MessageDetailModal
        ref="detailModalRef"
        @changed="handleNoticeChanged"
      />
    </n-el>
  </NSpin>
</template>

<style scoped>
.board {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.promo {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--border-color, #e8edf4);
  background: #0f172a;
}

.promo,
.promo :deep(.n-carousel),
.promo :deep(.n-carousel__slide),
.promo__slide {
  height: 188px;
}

.promo__slide {
  position: relative;
  display: block;
  width: 100%;
  padding: 0;
  border: 0;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(145deg, #0f172a, #1d4ed8);
  text-align: left;
}

.promo__image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.promo__veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.08), rgba(15, 23, 42, 0.72));
}

.promo__text {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 4px;
  height: 100%;
  padding: 16px 18px;
  color: #fff;
}

.promo__text strong {
  font-size: 18px;
  font-weight: 650;
  line-height: 1.35;
}

.promo__text span {
  font-size: 13px;
  line-height: 1.45;
  opacity: 0.9;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.layout {
  display: grid;
  grid-template-columns: minmax(240px, 280px) minmax(0, 1fr) minmax(320px, 400px);
  grid-template-areas:
    'profile apps notice'
    'login login notice';
  gap: 12px;
  align-items: stretch;
  min-width: 0;
}

.layout--banner {
  grid-template-areas:
    'banner banner notice'
    'profile apps notice'
    'login login notice';
}

.profile {
  grid-area: profile;
}

.apps {
  grid-area: apps;
}

.activity {
  grid-area: login;
  align-self: start;
}

.promo {
  grid-area: banner;
}

.notice {
  grid-area: notice;
  grid-row: 1 / -1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow-x: hidden;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--primary-color, #1677ff) 4%, var(--card-color, #fff)),
    var(--card-color, #fff) 120px
  );
}

.notice__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
}

.card {
  padding: 14px 16px;
  background: var(--card-color, #fff);
  border: 1px solid var(--border-color, #e8edf4);
}

.card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  min-width: 0;
}

.card__head > div:first-child {
  min-width: 0;
  flex: 1;
}

.card__title {
  font-size: 15px;
  font-weight: 650;
  color: var(--text-color-1, #1f1f1f);
}

.card__title--with-tag {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.card__sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-color-3, #8b95a5);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.apps__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(92px, 1fr));
  gap: 10px;
}

.app {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 12px 8px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-color-1, #1f1f1f);
  cursor: pointer;
}

.app:hover {
  border-color: color-mix(in srgb, var(--primary-color, #1677ff) 28%, transparent);
  background: var(--body-color, #f5f7fb);
}

.app__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--body-color, #f5f7fb);
  color: var(--primary-color, #1677ff);
}

.app--add .app__icon {
  border: 1px dashed var(--border-color, #d7dee8);
  background: transparent;
  color: var(--text-color-3, #8b95a5);
}

.app__name {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  text-align: center;
}

.apps__empty {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0 4px;
  color: var(--text-color-3, #8b95a5);
  font-size: 13px;
}

.dense-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 12px;
}

.dense-table th,
.dense-table td {
  padding: 8px 6px;
  border-bottom: 1px solid var(--border-color, #eef2f7);
  text-align: left;
  vertical-align: middle;
  color: var(--text-color-2, #5c6675);
}

.dense-table th {
  color: var(--text-color-3, #8b95a5);
  font-weight: 500;
  background: var(--body-color, #f5f7fb);
}

.dense-table th:first-child,
.dense-table td:first-child {
  width: 148px;
}

.dense-table th:nth-child(2),
.dense-table td:nth-child(2) {
  width: 68px;
}

.dense-table th:nth-child(4),
.dense-table td:nth-child(4) {
  width: 108px;
}

.dense-table th:last-child,
.dense-table td:last-child {
  width: auto;
}

.dense-table .is-clip {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile__who {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.profile__hello {
  font-size: 15px;
  font-weight: 650;
  line-height: 1.35;
  color: var(--text-color-1, #1f1f1f);
}

.profile__meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-color-3, #8b95a5);
}

.profile__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.profile__list > div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
}

.profile__list span {
  color: var(--text-color-3, #8b95a5);
}

.profile__list strong {
  font-weight: 500;
  color: var(--text-color-1, #1f1f1f);
  text-align: right;
}


.notice__item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 12px 10px;
  margin: 0;
  border: 0;
  border-radius: 8px;
  border-bottom: 1px solid var(--border-color, #eef2f7);
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.notice__item.is-unread {
  background: color-mix(in srgb, var(--primary-color, #1677ff) 6%, var(--card-color, #fff));
}

.notice__item:last-child {
  border-bottom: 0;
}

.notice__item:hover {
  background: var(--body-color, #f5f7fb);
}

.notice__item.is-unread strong {
  color: var(--primary-color, #1677ff);
}

.notice__item strong {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-1, #1f1f1f);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice__item span {
  display: -webkit-box;
  font-size: 12px;
  color: var(--text-color-3, #8b95a5);
  min-width: 0;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.notice__item em {
  font-size: 11px;
  font-style: normal;
  color: var(--text-color-3, #8b95a5);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor__tip {
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--text-color-3, #8b95a5);
}

.editor__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  max-height: min(420px, 55vh);
  overflow: auto;
}

.editor__item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--border-color, #e8edf4);
  background: var(--body-color, #f5f7fb);
  color: var(--text-color-1, #1f1f1f);
  cursor: pointer;
  text-align: left;
  font-size: 13px;
}

.editor__item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor__item.is-on {
  border-color: var(--primary-color, #1677ff);
  background: color-mix(in srgb, var(--primary-color, #1677ff) 8%, #fff);
  color: var(--primary-color, #1677ff);
}

.editor__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1100px) {
  .layout,
  .layout--banner {
    display: flex;
    flex-direction: column;
  }

  .profile,
  .apps,
  .activity,
  .promo,
  .notice {
    grid-area: unset;
    grid-row: auto;
  }

  .notice {
    min-height: 280px;
  }

  .apps__grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .apps__grid,
  .editor__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
