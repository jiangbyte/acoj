<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores'
import { isValidEmail, resolveFileUrl } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import AvatarUploadModal from './components/AvatarUploadModal.vue'

const authStore = useAuthStore()
const emailFormRef = ref<FormInst | null>(null)
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const state = reactive({
  loading: false,
  savingProfile: false,
  savingPassword: false,
  savingPhone: false,
  savingEmail: false,
  activeTab: 'basic_info',
  avatarModalShow: false,
  me: null as any,
  profileForm: {
    name: '',
    nickname: '',
    avatar: '',
    signature: '',
    bio: '',
  },
  passwordForm: {
    old_password: '',
    new_password: '',
    confirm_password: '',
  },
  phoneForm: {
    phone: '',
    phone_login_enabled: false,
  },
  emailForm: {
    email: '',
    email_login_enabled: false,
  },
  bindConfirm: {
    show: false,
    type: 'phone' as 'phone' | 'email',
    password: '',
    loading: false,
  },
})

const profile = computed(() => state.me?.profile ?? {})
const avatarUrl = computed(() => resolveFileUrl(state.profileForm.avatar))
const displayName = computed(() => state.me?.nickname || '-')
const contactText = computed(() => {
  const parts = [profile.value.phone, profile.value.email].filter(Boolean)
  return parts.length ? parts.join(' / ') : '未设置'
})
const bindConfirmTitle = computed(() =>
  state.bindConfirm.type === 'phone'
    ? '确认更新手机号'
    : '确认更新邮箱',
)
const emailRules = computed<FormRules>(() => ({
  email: [
    {
      validator: validateEmailForm,
      trigger: ['input', 'blur'],
    },
  ],
}))

onMounted(async () => {
  await loadMe()
})

async function loadMe() {
  state.loading = true
  try {
    const data = await authStore.refreshUserInfo()
    state.me = data
    syncForms(data)
  } finally {
    state.loading = false
  }
}

function syncForms(data: any) {
  const currentProfile = data?.profile ?? {}
  state.profileForm.name = data?.name ?? currentProfile.name ?? ''
  state.profileForm.nickname = data?.nickname ?? currentProfile.nickname ?? ''
  state.profileForm.avatar = data?.avatar ?? currentProfile.avatar ?? ''
  state.profileForm.signature = currentProfile.signature ?? ''
  state.profileForm.bio = currentProfile.bio ?? ''
  state.phoneForm.phone = currentProfile.phone ?? ''
  state.emailForm.email = currentProfile.email ?? ''
  state.phoneForm.phone_login_enabled = Boolean(currentProfile.phone_login_enabled)
  state.emailForm.email_login_enabled = Boolean(currentProfile.email_login_enabled)
}

async function saveProfile() {
  state.savingProfile = true
  try {
    await authApi.updateUserCenterProfile({
      name: state.profileForm.name || null,
      nickname: state.profileForm.nickname || null,
      signature: state.profileForm.signature || null,
      bio: state.profileForm.bio || null,
    })
    await refreshMe()
    window.$message.success('保存成功')
  } finally {
    state.savingProfile = false
  }
}

async function savePassword() {
  if (state.passwordForm.new_password !== state.passwordForm.confirm_password) {
    window.$message.warning('两次输入的新密码不一致')
    return
  }
  state.savingPassword = true
  try {
    const encrypted = await encryptPasswords({
      old_password: state.passwordForm.old_password,
      new_password: state.passwordForm.new_password,
    })
    await authApi.updateUserCenterPassword({
      old_password: encrypted.values.old_password,
      new_password: encrypted.values.new_password,
      password_key_id: encrypted.password_key_id,
    })
    state.passwordForm.old_password = ''
    state.passwordForm.new_password = ''
    state.passwordForm.confirm_password = ''
    window.$message.success('密码已更新')
  } finally {
    state.savingPassword = false
  }
}

function savePhone() {
  openBindConfirm('phone')
}

async function saveEmail() {
  try {
    await emailFormRef.value?.validate()
  } catch {
    return
  }
  openBindConfirm('email')
}

function validateEmailForm(_rule: FormItemRule, value: string) {
  const text = String(value ?? '').trim()
  if (!text) {
    return state.emailForm.email_login_enabled
      ? new Error('请输入邮箱')
      : true
  }
  if (!isValidEmail(text)) {
    return new Error('请输入有效邮箱')
  }
  return true
}

function openBindConfirm(type: 'phone' | 'email') {
  state.bindConfirm.type = type
  state.bindConfirm.password = ''
  state.bindConfirm.show = true
}

async function confirmBind() {
  if (!state.bindConfirm.password) {
    window.$message.warning('请输入当前密码')
    return
  }
  const isPhone = state.bindConfirm.type === 'phone'
  state.bindConfirm.loading = true
  state.savingPhone = isPhone
  state.savingEmail = !isPhone
  try {
    const encrypted = await encryptPasswords({ password: state.bindConfirm.password })
    if (isPhone) {
      await authApi.updateUserCenterPhone({
        password: encrypted.values.password,
        password_key_id: encrypted.password_key_id,
        phone: state.phoneForm.phone || null,
        phone_login_enabled: state.phoneForm.phone_login_enabled,
      })
    } else {
      await authApi.updateUserCenterEmail({
        password: encrypted.values.password,
        password_key_id: encrypted.password_key_id,
        email: state.emailForm.email.trim() || null,
        email_login_enabled: state.emailForm.email_login_enabled,
      })
    }
    state.bindConfirm.show = false
    state.bindConfirm.password = ''
    await refreshMe()
    window.$message.success('绑定已更新')
  } finally {
    state.bindConfirm.loading = false
    state.savingPhone = false
    state.savingEmail = false
  }
}

async function refreshMe() {
  const data = await authStore.refreshUserInfo()
  state.me = data
  syncForms(data)
}

function displayValue(value: unknown) {
  return value ? String(value) : '未设置'
}
</script>

<template>
  <div class="w-full min-w-0">
    <NSpin :show="state.loading">
      <NGrid
        class="w-full min-w-0"
        responsive="screen"
        item-responsive
        cols="1 m:24"
        :x-gap="16"
        :y-gap="16"
      >
        <NGridItem span="1 m:7" class="min-w-0">
          <NCard
            :bordered="false"
            class="user-center-profile h-full w-full min-w-0"
            content-class="min-w-0"
            size="small"
          >
            <div class="flex flex-col items-center text-center">
              <button
                class="avatar-trigger"
                type="button"
                :title="'更换头像'"
                @click="state.avatarModalShow = true"
              >
                <NAvatar
                  v-if="avatarUrl"
                  round
                  :size="104"
                  :src="avatarUrl"
                  :img-props="avatarImgProps"
                />
                <NAvatar v-else round :size="104">
                  <NovaIcon icon="icon-park-outline:user" :size="44" />
                </NAvatar>
              </button>
              <div class="mt-4 max-w-full truncate text-xl font-medium">
                {{ displayName }}
              </div>
              <div class="mt-1 max-w-full truncate text-sm text-[var(--text-color-3)]">
                {{ state.me?.account }}
              </div>
            </div>

            <NDivider />

            <NDescriptions :column="1" label-placement="left" size="small">
              <NDescriptionsItem :label="'联系方式'">
                {{ contactText }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'等级'">
                {{ displayValue(profile.level) }}
              </NDescriptionsItem>
            </NDescriptions>

            <NDivider />

            <div class="text-sm font-medium">
              签名
            </div>
            <div
              class="mt-2 min-h-18 rounded border border-[var(--border-color)] p-3 text-sm text-[var(--text-color-3)]"
            >
              {{ displayValue(profile.signature) }}
            </div>
            <div class="mt-4 text-sm font-medium">
              简介
            </div>
            <div
              class="mt-2 min-h-22 rounded border border-[var(--border-color)] p-3 text-sm text-[var(--text-color-3)]"
            >
              {{ displayValue(profile.bio) }}
            </div>
          </NCard>
        </NGridItem>

        <NGridItem span="1 m:17" class="min-w-0">
          <NCard
            :bordered="false"
            class="w-full min-w-0"
            content-class="min-h-140 min-w-0"
            size="small"
          >
            <NTabs
              v-model:value="state.activeTab"
              type="line"
              animated
              class="user-center-tabs w-full min-w-0"
            >
              <NTabPane name="basic_info" :tab="'基本信息'">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="'账号'">
                    <NInput :value="state.me?.account" disabled />
                  </NFormItem>
                  <NFormItem :label="'名称'">
                    <NInput v-model:value="state.profileForm.name" />
                  </NFormItem>
                  <NFormItem :label="'昵称'">
                    <NInput v-model:value="state.profileForm.nickname" />
                  </NFormItem>
                  <NFormItem :label="'签名'">
                    <NInput v-model:value="state.profileForm.signature" type="textarea" />
                  </NFormItem>
                  <NFormItem :label="'简介'">
                    <NInput v-model:value="state.profileForm.bio" type="textarea" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingProfile" @click="saveProfile">
                      保存
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="password" :tab="'密码'">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="'旧密码'">
                    <NInput
                      v-model:value="state.passwordForm.old_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :label="'新密码'">
                    <NInput
                      v-model:value="state.passwordForm.new_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :label="'确认密码'">
                    <NInput
                      v-model:value="state.passwordForm.confirm_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingPassword" @click="savePassword">
                      修改密码
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="phone" :tab="'手机号'">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="'手机号'">
                    <NInput v-model:value="state.phoneForm.phone" />
                  </NFormItem>
                  <NFormItem :label="'启用手机号登录'">
                    <NSwitch v-model:value="state.phoneForm.phone_login_enabled" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingPhone" @click="savePhone">
                      修改手机号
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="email" :tab="'邮箱'">
                <NForm
                  ref="emailFormRef"
                  class="user-center-form w-full min-w-0"
                  :model="state.emailForm"
                  :rules="emailRules"
                  label-placement="top"
                >
                  <NFormItem :label="'邮箱'" path="email">
                    <NInput v-model:value="state.emailForm.email" />
                  </NFormItem>
                  <NFormItem :label="'启用邮箱登录'">
                    <NSwitch v-model:value="state.emailForm.email_login_enabled" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingEmail" @click="saveEmail">
                      修改邮箱
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>
            </NTabs>
          </NCard>
        </NGridItem>
      </NGrid>
    </NSpin>

    <NModal
      v-model:show="state.bindConfirm.show"
      preset="card"
      :title="bindConfirmTitle"
      class="max-w-120"
      :bordered="false"
      :mask-closable="false"
    >
      <NForm label-placement="top">
        <NFormItem :label="'当前密码'">
          <NInput
            v-model:value="state.bindConfirm.password"
            type="password"
            show-password-on="click"
            :placeholder="'请输入当前密码'"
            @keydown.enter="confirmBind"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="state.bindConfirm.show = false">
            取消
          </NButton>
          <NButton type="primary" :loading="state.bindConfirm.loading" @click="confirmBind">
            确认
          </NButton>
        </NSpace>
      </template>
    </NModal>

    <AvatarUploadModal
      v-model:show="state.avatarModalShow"
      :avatar="avatarUrl"
      @uploaded="refreshMe"
    />
  </div>
</template>

<style scoped>
.user-center-tabs {
  min-width: 0;
}

.user-center-tabs :deep(.n-tabs-nav-scroll-content) {
  -webkit-overflow-scrolling: touch;
  touch-action: pan-x;
}

.user-center-form {
  min-width: 0;
}

.user-center-form :deep(.n-form-item) {
  min-width: 0;
}

.user-center-form :deep(.n-input) {
  width: 100%;
  min-width: min(180px, 100%);
}

.avatar-trigger {
  border: 0;
  border-radius: 999px;
  background: transparent;
  padding: 0;
  cursor: pointer;
  line-height: 0;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.avatar-trigger:hover,
.avatar-trigger:focus-visible {
  background: var(--hover-color);
  box-shadow:
    0 0 0 3px var(--card-color),
    0 0 0 5px var(--primary-color-hover);
  transform: translateY(-1px);
  outline: none;
}

.user-center-profile :deep(.n-descriptions-table) {
  width: 100%;
  table-layout: fixed;
}

.user-center-profile :deep(.n-descriptions-table-content) {
  overflow-wrap: anywhere;
}
</style>
