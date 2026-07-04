<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { authApi, messageApi } from '@/api'
import MessageDetailModal from '@/components/message/MessageDetailModal.vue'
import { useAuthStore } from '@/stores'
import { isValidEmail, resolveFileUrl } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import { computed, onMounted, reactive, ref } from 'vue'
import AvatarUploadModal from './components/AvatarUploadModal.vue'

const authStore = useAuthStore()
const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
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
  notifications: [] as any[],
  threads: [] as any[],
  todos: [] as any[],
  profileForm: {
    name: '',
    nickname: '',
    avatar: '',
    signature: '',
    title: '',
    employee_no: '',
    remark: '',
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
const roleNames = computed(() => mapNames(state.me?.role_id_names))
const deptNames = computed(() => mapNames(state.me?.dept_id_names))
const mainDept = computed(() => deptNames.value || 'Not set')
const mainRole = computed(() => roleNames.value || 'Not set')
const contactText = computed(() => {
  const parts = [profile.value.phone, profile.value.email].filter(Boolean)
  return parts.length ? parts.join(' / ') : 'Not set'
})
const bindConfirmTitle = computed(() =>
  state.bindConfirm.type === 'phone'
    ? 'Confirm Phone Update'
    : 'Confirm Email Update',
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
  await loadPage()
})

async function loadPage() {
  state.loading = true
  try {
    await Promise.all([loadMe(), loadMessages()])
  } finally {
    state.loading = false
  }
}

async function loadMe() {
  const data = await authStore.refreshUserInfo()
  state.me = data
  syncForms(data)
}

async function loadMessages() {
  const [notificationsResponse, threadsResponse, todosResponse] = await Promise.all([
    messageApi.myNotifications({ current: 1, size: 5 }),
    messageApi.myThreads({ current: 1, size: 5 }),
    messageApi.myTodos({ current: 1, size: 5, include_done: true }),
  ])
  state.notifications = notificationsResponse.data?.records ?? []
  state.threads = threadsResponse.data?.records ?? []
  state.todos = todosResponse.data?.records ?? []
}

function syncForms(data: any) {
  const currentProfile = data?.profile ?? {}
  state.profileForm.name = data?.name ?? currentProfile.name ?? ''
  state.profileForm.nickname = data?.nickname ?? currentProfile.nickname ?? ''
  state.profileForm.avatar = data?.avatar ?? currentProfile.avatar ?? ''
  state.profileForm.signature = currentProfile.signature ?? ''
  state.profileForm.title = currentProfile.title ?? ''
  state.profileForm.employee_no = currentProfile.employee_no ?? ''
  state.profileForm.remark = currentProfile.remark ?? ''
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
      title: state.profileForm.title || null,
      employee_no: state.profileForm.employee_no || null,
      remark: state.profileForm.remark || null,
    })
    await refreshMe()
    window.$message.success('Saved')
  } finally {
    state.savingProfile = false
  }
}

async function savePassword() {
  if (state.passwordForm.new_password !== state.passwordForm.confirm_password) {
    window.$message.warning('The new passwords do not match')
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
    window.$message.success('Password updated')
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
      ? new Error('Please enter email')
      : true
  }
  if (!isValidEmail(text)) {
    return new Error('Please enter a valid email')
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
    window.$message.warning('Please enter the current password')
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
    window.$message.success('Binding updated')
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

async function openDetail(type: 'notification' | 'message' | 'todo', item: any) {
  await detailModalRef.value?.open(type, item)
}

async function handleDetailChanged() {
  await loadMessages()
}

function mapNames(items?: Array<{ id: string; name: string }>) {
  return (items ?? [])
    .map((item) => item.name)
    .filter(Boolean)
    .join(' / ')
}

function displayValue(value: unknown) {
  return value ? String(value) : 'Not set'
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
                :title="'Change Avatar'"
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
              <NDescriptionsItem :label="'Title'">
                {{ displayValue(profile.title) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Department'">
                {{ mainDept }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Role'">
                {{ mainRole }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Contact'">
                {{ contactText }}
              </NDescriptionsItem>
            </NDescriptions>

            <NDivider />

            <div class="text-sm font-medium">
              {{ 'Signature' }}
            </div>
            <div
              class="mt-2 min-h-18 rounded border border-[var(--border-color)] p-3 text-sm text-[var(--text-color-3)]"
            >
              {{ displayValue(profile.signature || profile.remark) }}
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
              <NTabPane name="basic_info" :tab="'Basic Info'">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="'Account'">
                    <NInput :value="state.me?.account" disabled />
                  </NFormItem>
                  <NFormItem :label="'Name'">
                    <NInput v-model:value="state.profileForm.name" />
                  </NFormItem>
                  <NFormItem :label="'Nickname'">
                    <NInput v-model:value="state.profileForm.nickname" />
                  </NFormItem>
                  <NFormItem :label="'Title'">
                    <NInput v-model:value="state.profileForm.title" />
                  </NFormItem>
                  <NFormItem :label="'Employee No.'">
                    <NInput v-model:value="state.profileForm.employee_no" />
                  </NFormItem>
                  <NFormItem :label="'Signature'">
                    <NInput v-model:value="state.profileForm.signature" type="textarea" />
                  </NFormItem>
                  <NFormItem :label="'Remark'">
                    <NInput v-model:value="state.profileForm.remark" type="textarea" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingProfile" @click="saveProfile">
                      {{ 'Save' }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="password" :tab="'Password'">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="'Old Password'">
                    <NInput
                      v-model:value="state.passwordForm.old_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :label="'New Password'">
                    <NInput
                      v-model:value="state.passwordForm.new_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :label="'Confirm Password'">
                    <NInput
                      v-model:value="state.passwordForm.confirm_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingPassword" @click="savePassword">
                      {{ 'Update Password' }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="phone" :tab="'Phone'">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="'Phone'">
                    <NInput v-model:value="state.phoneForm.phone" />
                  </NFormItem>
                  <NFormItem :label="'Enable Phone Login'">
                    <NSwitch v-model:value="state.phoneForm.phone_login_enabled" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingPhone" @click="savePhone">
                      {{ 'Update Phone' }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="email" :tab="'Email'">
                <NForm
                  ref="emailFormRef"
                  class="user-center-form w-full min-w-0"
                  :model="state.emailForm"
                  :rules="emailRules"
                  label-placement="top"
                >
                  <NFormItem :label="'Email'" path="email">
                    <NInput v-model:value="state.emailForm.email" />
                  </NFormItem>
                  <NFormItem :label="'Enable Email Login'">
                    <NSwitch v-model:value="state.emailForm.email_login_enabled" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingEmail" @click="saveEmail">
                      {{ 'Update Email' }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="notifications" :tab="'Notifications'">
                <NList v-if="state.notifications.length" bordered clickable hoverable>
                  <NListItem
                    v-for="item in state.notifications"
                    :key="item.id"
                    class="cursor-pointer"
                    @click="openDetail('notification', item)"
                  >
                    <NThing :title="item.title">
                      <template #description>
                        {{ item.publish_at || item.created_at }}
                      </template>
                    </NThing>
                  </NListItem>
                </NList>
                <NEmpty v-else class="py-12" :description="'No data'" />
              </NTabPane>

              <NTabPane name="messages" :tab="'Messages'">
                <NList v-if="state.threads.length" bordered clickable hoverable>
                  <NListItem
                    v-for="item in state.threads"
                    :key="item.id"
                    class="cursor-pointer"
                    @click="openDetail('message', item)"
                  >
                    <NThing :title="item.title || item.name || item.last_message_content">
                      <template #description>
                        {{ item.updated_at || item.created_at }}
                      </template>
                    </NThing>
                  </NListItem>
                </NList>
                <NEmpty v-else class="py-12" :description="'No data'" />
              </NTabPane>

              <NTabPane name="todos" :tab="'Todos'">
                <NList v-if="state.todos.length" bordered clickable hoverable>
                  <NListItem
                    v-for="item in state.todos"
                    :key="item.id"
                    class="cursor-pointer"
                    @click="openDetail('todo', item)"
                  >
                    <NThing :title="item.title">
                      <template #description>
                        {{ item.due_at || item.updated_at }}
                      </template>
                    </NThing>
                  </NListItem>
                </NList>
                <NEmpty v-else class="py-12" :description="'No data'" />
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
        <NFormItem :label="'Current Password'">
          <NInput
            v-model:value="state.bindConfirm.password"
            type="password"
            show-password-on="click"
            :placeholder="'Enter current password'"
            @keydown.enter="confirmBind"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="state.bindConfirm.show = false">
            {{ 'Cancel' }}
          </NButton>
          <NButton type="primary" :loading="state.bindConfirm.loading" @click="confirmBind">
            {{ 'Confirm' }}
          </NButton>
        </NSpace>
      </template>
    </NModal>

    <AvatarUploadModal
      v-model:show="state.avatarModalShow"
      :avatar="avatarUrl"
      @uploaded="refreshMe"
    />

    <MessageDetailModal ref="detailModalRef" @changed="handleDetailChanged" />
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
