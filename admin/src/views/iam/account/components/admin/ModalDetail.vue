<!-- Author: Charlie -->

<script setup lang="ts">
import { accountApi, authApi } from '@/api'
import { createTagColor, displayValue, formatDateTime, hasPermission } from '@/utils'
import { accountBoolLabel } from '../../composables/useAccountDetail'
import { computed, reactive } from 'vue'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

const IDENTITY_STATUS_LABELS: Record<string, string> = {
  UNVERIFIED: '未认证',
  VERIFIED: '已认证',
  REVOKED: '已撤销',
}

const DOCUMENT_TYPE_LABELS: Record<string, string> = {
  ID_CARD: '身份证',
  PASSPORT: '护照',
  EID_CARD: '电子身份证',
  EID: '电子身份证',
}

const VERIFY_CHANNEL_LABELS: Record<string, string> = {
  MANUAL: '人工审核',
  THIRD_PARTY: '第三方认证',
}

const state = reactive({
  showModal: false,
  loading: false,
  activeTab: 'profile',
  unbinding: '' as string,
  account: {} as any,
})

const displayTitle = computed(
  () => state.account?.nickname || state.account?.account || '管理员详情',
)
const avatarAlt = computed(() => state.account?.nickname || state.account?.name || '管理员头像')
const avatarUrl = computed(() => state.account?.avatar || undefined)
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
const oauthBindings = computed(() =>
  Array.isArray(state.account?.oauth_bindings) ? state.account.oauth_bindings : [],
)
const identityStatus = computed(() => state.account?.identity_status ?? null)

function labelOf(map: Record<string, string>, value?: string | null) {
  if (!value) return '—'
  return map[value] || value
}

function maskOpenId(value?: string) {
  const text = String(value || '')
  if (text.length <= 8) return text ? '****' : '—'
  return `${text.slice(0, 4)}****${text.slice(-4)}`
}

function providerLabel(provider?: string) {
  return dictTypeData('OAUTH_PROVIDER', provider || '') || displayValue(provider)
}

async function openModal(id: string) {
  state.account = {}
  state.activeTab = 'profile'
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await accountApi.detail({ id })
    state.account = response.data ?? {}
  } finally {
    state.loading = false
  }
}

async function unbindOauth(provider: string) {
  if (!state.account?.id || state.unbinding) return
  state.unbinding = provider
  try {
    await authApi.adminOauthUnbind({
      account_id: String(state.account.id),
      provider,
    })
    window.$message.success('已解绑')
    await fetchDetail(String(state.account.id))
  } finally {
    state.unbinding = ''
  }
}

defineExpose({ openModal })
</script>

<template>
  <HeiDetailContainer
    v-model:show="state.showModal"
    title="管理员详情"
    :width="760"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <div class="detail-page">
        <header class="detail-header">
          <h1 class="detail-title">
            {{ displayTitle }}
          </h1>
          <p
            v-if="state.account.account"
            class="detail-subtitle"
          >
            {{ state.account.account }}
          </p>
        </header>

        <NTabs
          v-model:value="state.activeTab"
          type="line"
          animated
        >
          <NTabPane
            name="profile"
            tab="基本信息"
          >
            <div class="tab-pane">
              <div class="meta-grid">
            <div class="meta-item">
              <div class="meta-key">
                账号 ID
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.id) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                账号
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.account) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                账号状态
              </div>
              <div class="meta-value">
                <NTag
                  :color="
                    createTagColor(dictTypeColor('ACCOUNT_STATUS', state.account.account_status))
                  "
                  :bordered="false"
                >
                  {{ dictTypeData('ACCOUNT_STATUS', state.account.account_status) }}
                </NTag>
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                昵称
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.nickname) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                头像
              </div>
              <div class="meta-value">
                <NAvatar
                  v-if="avatarUrl"
                  round
                  :size="40"
                  :src="avatarUrl"
                  :alt="avatarAlt"
                  :img-props="avatarImgProps"
                />
                <span
                  v-else
                  class="text-muted"
                >—</span>
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                个性签名
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.signature) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                手机号
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.phone) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                邮箱
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.email) }}
              </div>
            </div>
            <div class="meta-item meta-item--wide">
              <div class="meta-key">
                备注
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.remark) }}
              </div>
            </div>
            </div>
            </div>
          </NTabPane>

          <NTabPane
            name="login"
            tab="登录信息"
          >
            <div class="tab-pane">
              <div class="meta-grid">
            <div class="meta-item">
              <div class="meta-key">
                上次登录 IP
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.last_login_ip) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                上次登录地址
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.last_login_address) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                上次登录时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(state.account.last_login_time) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                上次登录设备
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.last_login_device) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                最近登录 IP
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.latest_login_ip) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                最近登录地址
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.latest_login_address) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                最近登录时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(state.account.latest_login_time) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                最近登录设备
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.latest_login_device) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                创建时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(state.account.created_at) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                更新时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(state.account.updated_at) }}
              </div>
            </div>
              </div>

              <template v-if="state.account.cancelled_at || state.account.cancel_reason">
                <h3 class="subsection-label">
                  注销信息
                </h3>
                <div class="meta-grid">
            <div class="meta-item">
              <div class="meta-key">
                注销时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(state.account.cancelled_at) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                注销人
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.cancelled_by) }}
              </div>
            </div>
            <div class="meta-item meta-item--wide">
              <div class="meta-key">
                注销原因
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.cancel_reason) }}
              </div>
            </div>
                </div>
              </template>
            </div>
          </NTabPane>

          <NTabPane
            name="identity"
            tab="登录身份"
          >
            <div class="tab-pane">
              <div class="meta-grid">
            <div class="meta-item">
              <div class="meta-key">
                邮箱身份
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.email_identity) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                启用邮箱登录
              </div>
              <div class="meta-value">
                {{ accountBoolLabel(state.account.email_login_enabled) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                邮箱已验证
              </div>
              <div class="meta-value">
                {{ accountBoolLabel(state.account.email_identity_verified) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                邮箱绑定状态
              </div>
              <div class="meta-value">
                {{
                  dictTypeData(
                    'ACCOUNT_IDENTITY_BIND_STATUS',
                    state.account.email_identity_bind_status,
                  ) || displayValue(state.account.email_identity_bind_status)
                }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                手机号身份
              </div>
              <div class="meta-value">
                {{ displayValue(state.account.phone_identity) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                启用手机号登录
              </div>
              <div class="meta-value">
                {{ accountBoolLabel(state.account.phone_login_enabled) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                手机号已验证
              </div>
              <div class="meta-value">
                {{ accountBoolLabel(state.account.phone_identity_verified) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                手机号绑定状态
              </div>
              <div class="meta-value">
                {{
                  dictTypeData(
                    'ACCOUNT_IDENTITY_BIND_STATUS',
                    state.account.phone_identity_bind_status,
                  ) || displayValue(state.account.phone_identity_bind_status)
                }}
              </div>
            </div>
              </div>
            </div>
          </NTabPane>

          <NTabPane
            name="oauth"
            tab="三方绑定"
          >
            <div class="tab-pane">
          <p
            v-if="!oauthBindings.length"
            class="empty-text"
          >
            暂无三方绑定
          </p>
          <div
            v-for="item in oauthBindings"
            :key="item.provider"
            class="binding-card"
          >
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-key">
                  提供商
                </div>
                <div class="meta-value">
                  {{ providerLabel(item.provider) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  OpenID
                </div>
                <div class="meta-value">
                  {{ maskOpenId(item.open_id) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  昵称
                </div>
                <div class="meta-value">
                  {{ displayValue(item.nickname) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  绑定时间
                </div>
                <div class="meta-value">
                  {{ formatDateTime(item.bound_at) }}
                </div>
              </div>
              <div
                v-if="hasPermission('iam:account:update')"
                class="meta-item"
              >
                <div class="meta-key">
                  操作
                </div>
                <div class="meta-value">
                  <NButton
                    text
                    type="error"
                    :loading="state.unbinding === item.provider"
                    @click="unbindOauth(item.provider)"
                  >
                    解绑
                  </NButton>
                </div>
              </div>
            </div>
            </div>
            </div>
          </NTabPane>

          <NTabPane
            name="realname"
            tab="实名认证"
          >
            <div class="tab-pane">
              <div class="meta-grid">
            <div class="meta-item">
              <div class="meta-key">
                认证状态
              </div>
              <div class="meta-value">
                {{ labelOf(IDENTITY_STATUS_LABELS, identityStatus?.status) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                真实姓名
              </div>
              <div class="meta-value">
                {{ displayValue(identityStatus?.real_name_masked || state.account.name) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                证件类型
              </div>
              <div class="meta-value">
                {{ labelOf(DOCUMENT_TYPE_LABELS, identityStatus?.document_type) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                证件号码
              </div>
              <div class="meta-value">
                {{ displayValue(identityStatus?.document_no_masked) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                认证通道
              </div>
              <div class="meta-value">
                {{ labelOf(VERIFY_CHANNEL_LABELS, identityStatus?.verify_channel) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                服务提供方
              </div>
              <div class="meta-value">
                {{ displayValue(identityStatus?.provider) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                认证时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(identityStatus?.verified_at) }}
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-key">
                撤销时间
              </div>
              <div class="meta-value">
                {{ formatDateTime(identityStatus?.revoked_at) }}
              </div>
            </div>
              </div>
            </div>
          </NTabPane>
        </NTabs>
      </div>
    </NSpin>
  </HeiDetailContainer>
</template>

<style scoped>
.detail-page {
  max-width: 100%;
}

.detail-header {
  margin-bottom: 16px;
}

.tab-pane {
  padding-top: 4px;
}

.subsection-label {
  margin: 20px 0 14px;
  color: var(--text-color-2, #666);
  font-size: 13px;
  font-weight: 600;
}

.detail-title {
  margin: 0 0 6px;
  color: var(--text-color-1, #1f1f1f);
  font-size: 20px;
  font-weight: 650;
  line-height: 1.35;
}

.detail-subtitle {
  margin: 0;
  color: var(--text-color-3, #999);
  font-size: 13px;
  line-height: 1.5;
}

.meta-section {
  margin-bottom: 24px;
}

.section-label {
  margin: 0 0 14px;
  color: var(--text-color-2, #666);
  font-size: 13px;
  font-weight: 600;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 24px;
}

.meta-item {
  min-width: 0;
}

.meta-item--wide {
  grid-column: 1 / -1;
}

.meta-key {
  margin-bottom: 4px;
  color: var(--text-color-3, #999);
  font-size: 12px;
  line-height: 1.4;
}

.meta-value {
  color: var(--text-color-1, #333);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.text-muted,
.empty-text {
  color: var(--text-color-3, #999);
}

.empty-text {
  margin: 0;
  font-size: 14px;
}

.binding-card {
  padding: 14px 16px;
  border: 1px solid var(--border-color, #efeff5);
  border-radius: 8px;
}

.binding-card + .binding-card {
  margin-top: 12px;
}

@media (max-width: 720px) {
  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
