<script setup lang="ts">
import { authApi, messageApi } from '@/api'
import MessageDetailModal from '@/components/message/MessageDetailModal.vue'
import { useAuthStore } from '@/stores'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const authStore = useAuthStore()
const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
const avatarImgProps = { referrerPolicy: 'no-referrer' }

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
  return (items ?? []).map((item) => item.name).filter(Boolean).join(' / ')
}

function displayValue(value: unknown) {
  return value ? String(value) : t('app.user_center.empty_value')
}
</script>

<template>
  <NSpin :show="state.loading">
    <NGrid :cols="24" :x-gap="16" :y-gap="16" item-responsive responsive="screen">
      <NGridItem span="24 m:7">
        <NCard :bordered="false" class="h-full">
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
            <div class="mt-4 text-xl font-medium">
              {{ displayName }}
            </div>
            <div class="mt-1 text-sm text-gray-500">
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
          <div class="mt-2 min-h-18 rounded border border-gray-200 p-3 text-sm text-gray-500">
            {{ displayValue(profile.signature || profile.remark) }}
          </div>
        </NCard>
      </NGridItem>

      <NGridItem span="24 m:17">
        <NCard :bordered="false" content-class="min-h-140">
          <NTabs v-model:value="state.activeTab" type="line" animated>
            <NTabPane name="basic_info" :tab="t('app.user_center.basic_info')">
              <NForm
                :model="state.profileForm"
                label-placement="left"
                label-width="120"
                class="max-w-220 pt-4"
              >
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
                <NFormItem>
                  <NButton type="primary" :loading="state.savingProfile" @click="saveProfile">
                    {{ t('common.save') }}
                  </NButton>
                </NFormItem>
              </NForm>
            </NTabPane>

            <NTabPane name="password" :tab="t('app.user_center.password')">
              <NForm :model="state.passwordForm" label-placement="top" class="max-w-160 pt-4">
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
                <NButton type="primary" :loading="state.savingPassword" @click="savePassword">
                  {{ t('app.user_center.update_password') }}
                </NButton>
              </NForm>
            </NTabPane>

            <NTabPane name="phone" :tab="t('app.user_center.phone')">
              <NForm :model="state.phoneForm" label-placement="top" class="max-w-160 pt-4">
                <NFormItem :label="t('app.user_center.phone')">
                  <NInput v-model:value="state.phoneForm.phone" />
                </NFormItem>
                <NButton type="primary" :loading="state.savingPhone" @click="savePhone">
                  {{ t('app.user_center.update_phone') }}
                </NButton>
              </NForm>
            </NTabPane>

            <NTabPane name="email" :tab="t('app.user_center.email')">
              <NForm :model="state.emailForm" label-placement="top" class="max-w-160 pt-4">
                <NFormItem :label="t('app.user_center.email')">
                  <NInput v-model:value="state.emailForm.email" />
                </NFormItem>
                <NButton type="primary" :loading="state.savingEmail" @click="saveEmail">
                  {{ t('app.user_center.update_email') }}
                </NButton>
              </NForm>
            </NTabPane>

            <NTabPane name="notifications" :tab="t('app.notifications')">
              <NList v-if="state.notifications.length" class="pt-4" hoverable>
                <NListItem
                  v-for="item in state.notifications"
                  :key="item.id"
                  class="cursor-pointer"
                  @click="openDetail('notification', item)"
                >
                  <NThing :title="item.title" :description="item.publish_at || item.created_at" />
                </NListItem>
              </NList>
              <NEmpty v-else class="pt-12" :description="t('app.user_center.empty_data')" />
            </NTabPane>

            <NTabPane name="messages" :tab="t('app.messages')">
              <NList v-if="state.threads.length" class="pt-4" hoverable>
                <NListItem
                  v-for="item in state.threads"
                  :key="item.id"
                  class="cursor-pointer"
                  @click="openDetail('message', item)"
                >
                  <NThing
                    :title="item.title || item.name || item.last_message_content"
                    :description="item.updated_at || item.created_at"
                  />
                </NListItem>
              </NList>
              <NEmpty v-else class="pt-12" :description="t('app.user_center.empty_data')" />
            </NTabPane>

            <NTabPane name="todos" :tab="t('app.todos')">
              <NList v-if="state.todos.length" class="pt-4" hoverable>
                <NListItem
                  v-for="item in state.todos"
                  :key="item.id"
                  class="cursor-pointer"
                  @click="openDetail('todo', item)"
                >
                  <NThing :title="item.title" :description="item.due_at || item.updated_at" />
                </NListItem>
              </NList>
              <NEmpty v-else class="pt-12" :description="t('app.user_center.empty_data')" />
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
        <NButton
          type="primary"
          :loading="state.bindConfirm.loading"
          @click="confirmBind"
        >
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>

  <MessageDetailModal ref="detailModalRef" @changed="handleDetailChanged" />
</template>
