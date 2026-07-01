<script setup lang="ts">
import { authApi, messageApi } from '@/api'
import MessageDetailModal from '@/components/message/MessageDetailModal.vue'
import { useAuthStore } from '@/stores'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const authStore = useAuthStore()
const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const state = reactive({
  loading: false,
  savingProfile: false,
  savingPassword: false,
  savingPhone: false,
  savingEmail: false,
  activeTab: 'basic_info',
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
  },
  emailForm: {
    email: '',
  },
  bindConfirm: {
    show: false,
    type: 'phone' as 'phone' | 'email',
    password: '',
    loading: false,
  },
})

const profile = computed(() => state.me?.profile ?? {})
const displayName = computed(() => state.me?.nickname || state.me?.name || state.me?.account || '-')
const roleNames = computed(() => mapNames(state.me?.role_id_names))
const deptNames = computed(() => mapNames(state.me?.dept_id_names))
const mainDept = computed(() => deptNames.value || t('app.user_center.empty_value'))
const mainRole = computed(() => roleNames.value || t('app.user_center.empty_value'))
const contactText = computed(() => {
  const parts = [profile.value.phone, profile.value.email].filter(Boolean)
  return parts.length ? parts.join(' / ') : t('app.user_center.empty_value')
})
const bindConfirmTitle = computed(() =>
  state.bindConfirm.type === 'phone'
    ? t('app.user_center.confirm_phone_title')
    : t('app.user_center.confirm_email_title'),
)

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
}

async function saveProfile() {
  state.savingProfile = true
  try {
    await authApi.updateUserCenterProfile({
      name: state.profileForm.name,
      nickname: state.profileForm.nickname || null,
      avatar: state.profileForm.avatar || null,
      signature: state.profileForm.signature || null,
      title: state.profileForm.title || null,
      employee_no: state.profileForm.employee_no || null,
      remark: state.profileForm.remark || null,
    })
    await refreshMe()
    window.$message.success(t('app.user_center.save_success'))
  } finally {
    state.savingProfile = false
  }
}

async function savePassword() {
  if (state.passwordForm.new_password !== state.passwordForm.confirm_password) {
    window.$message.warning(t('app.user_center.password_not_match'))
    return
  }
  state.savingPassword = true
  try {
    await authApi.updateUserCenterPassword({
      old_password: state.passwordForm.old_password,
      new_password: state.passwordForm.new_password,
    })
    state.passwordForm.old_password = ''
    state.passwordForm.new_password = ''
    state.passwordForm.confirm_password = ''
    window.$message.success(t('app.user_center.password_success'))
  } finally {
    state.savingPassword = false
  }
}

function savePhone() {
  openBindConfirm('phone')
}

function saveEmail() {
  openBindConfirm('email')
}

function openBindConfirm(type: 'phone' | 'email') {
  state.bindConfirm.type = type
  state.bindConfirm.password = ''
  state.bindConfirm.show = true
}

async function confirmBind() {
  if (!state.bindConfirm.password) {
    window.$message.warning(t('app.user_center.password_required'))
    return
  }
  const isPhone = state.bindConfirm.type === 'phone'
  state.bindConfirm.loading = true
  state.savingPhone = isPhone
  state.savingEmail = !isPhone
  try {
    if (isPhone) {
      await authApi.updateUserCenterPhone({
        password: state.bindConfirm.password,
        phone: state.phoneForm.phone || null,
      })
    } else {
      await authApi.updateUserCenterEmail({
        password: state.bindConfirm.password,
        email: state.emailForm.email || null,
      })
    }
    state.bindConfirm.show = false
    state.bindConfirm.password = ''
    await refreshMe()
    window.$message.success(t('app.user_center.bind_success'))
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
  return value ? String(value) : t('app.user_center.empty_value')
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
              <NAvatar
                v-if="state.profileForm.avatar"
                round
                :size="104"
                :src="state.profileForm.avatar"
                :img-props="avatarImgProps"
              />
              <NAvatar v-else round :size="104">
                <NovaIcon icon="icon-park-outline:user" :size="44" />
              </NAvatar>
              <div class="mt-4 max-w-full truncate text-xl font-medium">
                {{ displayName }}
              </div>
              <div class="mt-1 max-w-full truncate text-sm text-[var(--text-color-3)]">
                {{ state.me?.account }}
              </div>
            </div>

            <NDivider />

            <NDescriptions :column="1" label-placement="left" size="small">
              <NDescriptionsItem :label="t('app.user_center.title_field')">
                {{ displayValue(profile.title) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('app.user_center.department')">
                {{ mainDept }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('app.user_center.role')">
                {{ mainRole }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('app.user_center.contact')">
                {{ contactText }}
              </NDescriptionsItem>
            </NDescriptions>

            <NDivider />

            <div class="text-sm font-medium">
              {{ t('app.user_center.signature') }}
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
              <NTabPane name="basic_info" :tab="t('app.user_center.basic_info')">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="t('app.user_center.account')">
                    <NInput :value="state.me?.account" disabled />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.name')" required>
                    <NInput v-model:value="state.profileForm.name" />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.nickname')">
                    <NInput v-model:value="state.profileForm.nickname" />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.avatar')">
                    <NInput
                      v-model:value="state.profileForm.avatar"
                      :placeholder="t('app.user_center.placeholder.avatar')"
                    />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.title_field')">
                    <NInput v-model:value="state.profileForm.title" />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.employee_no')">
                    <NInput v-model:value="state.profileForm.employee_no" />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.signature')">
                    <NInput v-model:value="state.profileForm.signature" type="textarea" />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.remark')">
                    <NInput v-model:value="state.profileForm.remark" type="textarea" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingProfile" @click="saveProfile">
                      {{ t('common.save') }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="password" :tab="t('app.user_center.password')">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="t('app.user_center.old_password')">
                    <NInput
                      v-model:value="state.passwordForm.old_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.new_password')">
                    <NInput
                      v-model:value="state.passwordForm.new_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.confirm_password')">
                    <NInput
                      v-model:value="state.passwordForm.confirm_password"
                      type="password"
                      show-password-on="click"
                    />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingPassword" @click="savePassword">
                      {{ t('app.user_center.update_password') }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="phone" :tab="t('app.user_center.phone')">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="t('app.user_center.phone')">
                    <NInput v-model:value="state.phoneForm.phone" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingPhone" @click="savePhone">
                      {{ t('app.user_center.update_phone') }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="email" :tab="t('app.user_center.email')">
                <NForm class="user-center-form w-full min-w-0" label-placement="top">
                  <NFormItem :label="t('app.user_center.email')">
                    <NInput v-model:value="state.emailForm.email" />
                  </NFormItem>
                  <NFormItem :show-label="false">
                    <NButton type="primary" :loading="state.savingEmail" @click="saveEmail">
                      {{ t('app.user_center.update_email') }}
                    </NButton>
                  </NFormItem>
                </NForm>
              </NTabPane>

              <NTabPane name="notifications" :tab="t('app.notifications')">
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
                <NEmpty v-else class="py-12" :description="t('app.user_center.empty_data')" />
              </NTabPane>

              <NTabPane name="messages" :tab="t('app.messages')">
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
                <NEmpty v-else class="py-12" :description="t('app.user_center.empty_data')" />
              </NTabPane>

              <NTabPane name="todos" :tab="t('app.todos')">
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
                <NEmpty v-else class="py-12" :description="t('app.user_center.empty_data')" />
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
        <NFormItem :label="t('app.user_center.current_password')">
          <NInput
            v-model:value="state.bindConfirm.password"
            type="password"
            show-password-on="click"
            :placeholder="t('app.user_center.placeholder.current_password')"
            @keydown.enter="confirmBind"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="state.bindConfirm.show = false">
            {{ t('common.cancel') }}
          </NButton>
          <NButton type="primary" :loading="state.bindConfirm.loading" @click="confirmBind">
            {{ t('common.confirm') }}
          </NButton>
        </NSpace>
      </template>
    </NModal>

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

.user-center-profile :deep(.n-descriptions-table) {
  width: 100%;
  table-layout: fixed;
}

.user-center-profile :deep(.n-descriptions-table-content) {
  overflow-wrap: anywhere;
}
</style>
