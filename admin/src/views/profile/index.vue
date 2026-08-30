<!-- Author: Charlie -->

<script setup lang="ts">
import type { Component } from 'vue'
import { ProCard } from 'pro-naive-ui'
import { computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'
import './profile.css'
import BasicInfoPanel from './components/BasicInfoPanel.vue'
import CancelAccountPanel from './components/CancelAccountPanel.vue'
import EmailPanel from './components/EmailPanel.vue'
import IdentityPanel from './components/IdentityPanel.vue'
import MyLoginLogPanel from './components/MyLoginLogPanel.vue'
import MyMessagesPanel from './components/MyMessagesPanel.vue'
import PasswordPanel from './components/PasswordPanel.vue'
import PhonePanel from './components/PhonePanel.vue'
import OauthPanel from './components/OauthPanel.vue'
import ProfileSummary from './components/ProfileSummary.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const SECURITY_TAB_KEYS = new Set([
  'password',
  'identity',
  'phone',
  'email',
  'oauth',
  'cancel_account',
])

const primaryNavItems: Array<{ key: string; label: string }> = [
  { key: 'basic_info', label: '公开资料' },
  { key: 'my_messages', label: '我的消息' },
  { key: 'my_logins', label: '我的登录日志' },
]

const securityNavItems: Array<{ key: string; label: string }> = [
  { key: 'password', label: '密码' },
  { key: 'identity', label: '实名认证' },
  { key: 'phone', label: '手机号' },
  { key: 'email', label: '邮箱' },
  { key: 'oauth', label: '三方账号' },
  { key: 'cancel_account', label: '账号注销' },
]

const allNavKeys = [
  ...primaryNavItems.map((item) => item.key),
  ...securityNavItems.map((item) => item.key),
]

const panelMap: Record<string, Component> = {
  basic_info: BasicInfoPanel,
  identity: IdentityPanel,
  my_messages: MyMessagesPanel,
  my_logins: MyLoginLogPanel,
  password: PasswordPanel,
  phone: PhonePanel,
  email: EmailPanel,
  oauth: OauthPanel,
  cancel_account: CancelAccountPanel,
}

const state = reactive({
  loading: false,
  activeTab: resolveInitialTab(),
})

const lockedTabs = computed(() => {
  const user = authStore.userInfo
  if (user?.passwordExpired) return new Set(['password'])
  const allowed = new Set<string>()
  if (user?.forceBindEmail) allowed.add('email')
  if (user?.forceBindPhone) allowed.add('phone')
  if (user?.forceBindIdentity) allowed.add('identity')
  return allowed.size > 0 ? allowed : null
})

const activeMainTab = computed(() =>
  SECURITY_TAB_KEYS.has(state.activeTab) ? 'security' : state.activeTab,
)

function isTabDisabled(key: string) {
  return Boolean(lockedTabs.value && !lockedTabs.value.has(key))
}

function isSecurityGroupDisabled() {
  if (!lockedTabs.value) return false
  return !securityNavItems.some((item) => lockedTabs.value!.has(item.key))
}

function resolveInitialTab() {
  const tab = typeof route.query.tab === 'string' ? route.query.tab : ''
  if (tab && allNavKeys.includes(tab)) {
    return tab
  }
  return 'basic_info'
}

function selectTab(key: string) {
  if (!key || state.activeTab === key) {
    return
  }
  if (!allNavKeys.includes(key)) {
    return
  }
  if (lockedTabs.value && !lockedTabs.value.has(key)) {
    return
  }
  state.activeTab = key
  void router.replace({ query: { ...route.query, tab: key } })
}

function selectMainTab(key: string) {
  if (key === 'security') {
    const fallback =
      SECURITY_TAB_KEYS.has(state.activeTab) ? state.activeTab : 'password'
    selectTab(fallback)
    return
  }
  selectTab(key)
}

watch(
  () => route.query.tab,
  (tab) => {
    if (typeof tab === 'string' && allNavKeys.includes(tab) && state.activeTab !== tab) {
      state.activeTab = tab
    }
  },
)
</script>

<template>
  <div class="profile w-full min-w-0">
    <NSpin :show="state.loading">
      <div class="profile__layout">
        <ProCard
          class="profile__summary-card"
          content-class="profile__summary-body"
          :show-collapse="false"
        >
          <ProfileSummary />
        </ProCard>

        <ProCard
          class="profile__content-card"
          content-class="profile__content-body"
          :show-collapse="false"
        >
          <NTabs
            class="profile__tabs"
            type="line"
            :value="activeMainTab"
            @update:value="selectMainTab"
          >
            <NTabPane
              v-for="item in primaryNavItems"
              :key="item.key"
              :name="item.key"
              :tab="item.label"
              :disabled="isTabDisabled(item.key)"
            >
              <div class="profile__panel">
                <KeepAlive>
                  <component
                    :is="panelMap[item.key]"
                    :key="item.key"
                  />
                </KeepAlive>
              </div>
            </NTabPane>

            <NTabPane
              name="security"
              tab="访问与安全"
              :disabled="isSecurityGroupDisabled()"
            >
              <NTabs
                class="profile__security-tabs"
                type="line"
                placement="left"
                :value="state.activeTab"
                @update:value="selectTab"
              >
                <NTabPane
                  v-for="item in securityNavItems"
                  :key="item.key"
                  :name="item.key"
                  :tab="item.label"
                  :disabled="isTabDisabled(item.key)"
                >
                  <div class="profile__panel profile__panel--security">
                    <KeepAlive>
                      <component
                        :is="panelMap[item.key]"
                        :key="item.key"
                      />
                    </KeepAlive>
                  </div>
                </NTabPane>
              </NTabs>
            </NTabPane>
          </NTabs>
        </ProCard>
      </div>
    </NSpin>
  </div>
</template>
