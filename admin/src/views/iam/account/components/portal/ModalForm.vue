<!-- Author: Charlie -->

<script setup lang="ts">
import type { FormInst } from 'naive-ui'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { accountApi, authApi, realNameApi } from '@/api'
import { displayValue, formatDateTime, hasPermission } from '@/utils'
import { computed, reactive, ref } from 'vue'
import {
  accountStatusOptions,
  buildAccountPayload,
  buildLoginIdentityPayload,
  createAccountFormRules,
  createDefaultLoginIdentityForm,
  createLoginIdentityFormRules,
  mapAccountFormFromDetail,
  mapLoginIdentityFormFromDetail,
} from '../../composables/useAccountForm'

const ACCOUNT_TYPE = 'PORTAL'

const IDENTITY_STATUS_LABELS: Record<string, string> = {
  UNVERIFIED: '未认证',
  VERIFIED: '已认证',
  REVOKED: '已撤销',
}

const emit = defineEmits<{ saved: [] }>()

const formRef = ref<FormInst | null>(null)
const identityFormRef = ref<FormInst | null>(null)

const defaultFormData = {
  account_type: ACCOUNT_TYPE,
  account_status: 'ENABLED',
  password: '',
  account: '',
  nickname: '',
  avatar: '',
  signature: '',
  phone: '',
  email: '',
}

const defaultIdentityForm = createDefaultLoginIdentityForm()

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  identitySaving: false,
  unbinding: '' as string,
  activeTab: 'account',
  dataId: null as string | null,
  formModel: { ...defaultFormData },
  identityForm: { ...defaultIdentityForm },
  detail: {} as any,
})

const statusOptions = computed(() => accountStatusOptions())
const modalTitle = computed(() => (state.dataId ? '编辑门户用户' : '新增门户用户'))
const rules = computed(() => createAccountFormRules(() => Boolean(state.dataId)))
const identityRules = computed(() => createLoginIdentityFormRules(state.identityForm))
const oauthBindings = computed(() =>
  Array.isArray(state.detail?.oauth_bindings) ? state.detail.oauth_bindings : [],
)
const identityStatus = computed(() => state.detail?.identity_status ?? null)

function labelOf(map: Record<string, string>, value?: string | null) {
  if (!value) return '—'
  return map[value] || value
}

async function openModal(id?: string) {
  state.dataId = id ?? null
  state.formModel = { ...defaultFormData }
  state.identityForm = { ...defaultIdentityForm }
  state.detail = {}
  state.activeTab = 'account'
  state.showModal = true
  if (id) await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await accountApi.detail({ id })
    const data = response.data ?? {}
    state.detail = data
    state.formModel = mapAccountFormFromDetail(data, ACCOUNT_TYPE)
    state.identityForm = mapLoginIdentityFormFromDetail(data)
  } finally {
    state.loading = false
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
  state.identitySaving = false
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = await buildAccountPayload(state.formModel)
    if (state.dataId) {
      await accountApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await accountApi.create(payload)
      window.$message.success('创建成功')
    }
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

async function saveLoginIdentity() {
  if (!state.dataId) return
  await identityFormRef.value?.validate()
  state.identitySaving = true
  try {
    await accountApi.updateLoginIdentity(
      buildLoginIdentityPayload(state.dataId, state.identityForm),
    )
    window.$message.success('登录身份已保存')
    await fetchDetail(state.dataId)
  } finally {
    state.identitySaving = false
  }
}

async function unbindOauth(provider: string) {
  if (!state.dataId || state.unbinding) return
  state.unbinding = provider
  try {
    await authApi.adminOauthUnbind({
      account_id: String(state.dataId),
      provider,
    })
    window.$message.success('已解绑')
    await fetchDetail(state.dataId)
  } finally {
    state.unbinding = ''
  }
}

async function revokeIdentity() {
  if (!state.dataId) return
  window.$dialog.warning({
    title: '撤销实名认证',
    content: '确认撤销该账号的实名认证？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await realNameApi.revokeIdentity({ account_id: state.dataId! })
      window.$message.success('已撤销实名认证')
      await fetchDetail(state.dataId!)
    },
  })
}

defineExpose({ openModal })
</script>

<template>
  <HeiFormContainer
    v-model:show="state.showModal"
    :title="modalTitle"
    :width="760"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <NTabs
        v-model:value="state.activeTab"
        type="line"
        animated
      >
        <NTabPane
          name="account"
          tab="账号"
        >
          <NForm
            ref="formRef"
            class="tab-form"
            :model="state.formModel"
            :rules="rules"
            label-placement="left"
            label-width="110"
            :disabled="state.loading || state.submitLoading"
          >
            <NFormItem
              label="账号"
              path="account"
            >
              <NInput v-model:value="state.formModel.account" />
            </NFormItem>
            <NFormItem
              label="密码"
              path="password"
            >
              <div class="password-field">
                <NInput
                  v-model:value="state.formModel.password"
                  type="password"
                  show-password-on="click"
                  :placeholder="state.dataId ? '留空则保持当前密码' : undefined"
                />
                <PasswordStrengthBar :password="state.formModel.password" />
              </div>
            </NFormItem>
            <NFormItem
              label="账号状态"
              path="account_status"
            >
              <NRadioGroup v-model:value="state.formModel.account_status">
                <NRadio
                  v-for="option in statusOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </NRadio>
              </NRadioGroup>
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane
          name="profile"
          tab="资料"
        >
          <NForm
            class="tab-form"
            :model="state.formModel"
            label-placement="left"
            label-width="110"
            :disabled="state.loading || state.submitLoading"
          >
            <NFormItem label="昵称">
              <NInput v-model:value="state.formModel.nickname" />
            </NFormItem>
            <NFormItem label="头像">
              <ImageUpload v-model:value="state.formModel.avatar" />
            </NFormItem>
            <NFormItem label="个性签名">
              <NInput
                v-model:value="state.formModel.signature"
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 5 }"
              />
            </NFormItem>
            <NFormItem label="手机号">
              <NInput v-model:value="state.formModel.phone" />
            </NFormItem>
            <NFormItem label="邮箱">
              <NInput v-model:value="state.formModel.email" />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane
          v-if="state.dataId"
          name="identity"
          tab="登录身份"
        >
          <NForm
            ref="identityFormRef"
            class="tab-form"
            :model="state.identityForm"
            :rules="identityRules"
            label-placement="left"
            label-width="110"
            :disabled="state.loading || state.identitySaving"
          >
            <NFormItem label="邮箱">
              <NInput v-model:value="state.identityForm.email" />
            </NFormItem>
            <NFormItem label="启用邮箱登录">
              <NSwitch v-model:value="state.identityForm.email_login_enabled" />
            </NFormItem>
            <NFormItem label="手机号">
              <NInput v-model:value="state.identityForm.phone" />
            </NFormItem>
            <NFormItem label="启用手机号登录">
              <NSwitch v-model:value="state.identityForm.phone_login_enabled" />
            </NFormItem>
            <NFormItem label=" ">
              <NButton
                type="primary"
                :loading="state.identitySaving"
                @click="saveLoginIdentity"
              >
                保存登录身份
              </NButton>
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane
          v-if="state.dataId"
          name="oauth"
          tab="三方绑定"
        >
          <div class="tab-form">
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
                    {{ displayValue(item.provider) }}
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
          v-if="state.dataId"
          name="realname"
          tab="实名认证"
        >
          <div class="tab-form">
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
                  {{ displayValue(identityStatus?.real_name_masked) }}
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
            </div>
            <NButton
              v-if="identityStatus?.status === 'VERIFIED' && hasPermission('sys:realnameidentity:revoke')"
              class="mt-16px"
              type="error"
              @click="revokeIdentity"
            >
              撤销实名认证
            </NButton>
          </div>
        </NTabPane>
      </NTabs>
    </NSpin>

    <template #action>
      <NSpace
        justify="end"
        align="center"
      >
        <NButton @click="closeModal">
          取消
        </NButton>
        <NButton
          type="primary"
          :loading="state.submitLoading"
          @click="submitForm"
        >
          确认
        </NButton>
      </NSpace>
    </template>
  </HeiFormContainer>
</template>

<style scoped>
.tab-form {
  padding-top: 8px;
}

.password-field {
  width: 100%;
}

.empty-text {
  margin: 0;
  color: var(--text-color-3, #999);
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

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 24px;
}

.meta-item {
  min-width: 0;
}

.meta-key {
  margin-bottom: 4px;
  color: var(--text-color-3, #999);
  font-size: 12px;
}

.meta-value {
  color: var(--text-color-1, #333);
  font-size: 14px;
  word-break: break-word;
}

.mt-16px {
  margin-top: 16px;
}
</style>
