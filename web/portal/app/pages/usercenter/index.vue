<script setup lang="ts">
const authStore = useAuthStore()
const toast = useToast()
const avatarModalOpen = ref(false)

const state = reactive({
  loading: false,
  savingProfile: false,
  savingPassword: false,
  savingEmail: false,
  activeTab: 'basic_info',
  profileForm: {
    name: '',
    nickname: '',
    signature: '',
  },
  passwordForm: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  },
  emailForm: {
    email: '',
    password: '',
    emailLoginEnabled: true,
  },
  bindConfirm: {
    show: false,
    type: '' as 'phone' | 'email',
    password: '',
    loading: false,
  },
})

const profile = computed(() => authStore.userInfo ?? {})

function resolveAvatarUrl(value?: string | null) {
  if (!value) return undefined
  const raw = String(value).trim()
  if (!raw) return undefined
  if (/^(https?:|data:|blob:)/i.test(raw)) return raw
  try {
    const baseURL = useRuntimeConfig().public.apiBaseUrl
    if (baseURL) return `${baseURL.replace(/\/$/, '')}/${raw.replace(/^\//, '')}`
  } catch {
    // ignore
  }
  return raw
}

const avatarUrl = computed(() => resolveAvatarUrl(profile.value?.avatar))
const displayName = computed(
  () => authStore.userInfo?.nickname || authStore.userInfo?.account || '-',
)

function syncForms() {
  const u = authStore.userInfo
  if (!u) return
  state.profileForm.name = u.name ?? ''
  state.profileForm.nickname = u.nickname ?? ''
  state.profileForm.signature = u.signature ?? ''
  state.emailForm.email = u.email ?? ''
}

// 初始加载
onMounted(async () => {
  state.loading = true
  try {
    await authStore.refreshUserInfo()
    syncForms()
  } finally {
    state.loading = false
  }
})

async function saveProfile() {
  state.savingProfile = true
  try {
    await useHttp().post('/api/v1/portal/user-center/profile/update', {
      name: state.profileForm.name || null,
      nickname: state.profileForm.nickname || null,
      signature: state.profileForm.signature || null,
    })
    await authStore.refreshUserInfo()
    toast.add({ title: '保存成功', color: 'success' })
  } catch {
    // error toast by interceptor
  } finally {
    state.savingProfile = false
  }
}

async function savePassword() {
  if (state.passwordForm.newPassword !== state.passwordForm.confirmPassword) {
    toast.add({ title: '错误', description: '两次密码输入不一致', color: 'error' })
    return
  }
  state.savingPassword = true
  try {
    const { data: key } = await useHttp().get('/api/v1/portal/password-key')
    const { encryptPassword } = await import('~/utils/crypto')
    const oldEncrypted = await encryptPassword(state.passwordForm.oldPassword, {
      key_id: key.key_id,
      public_key: key.public_key,
    })
    const newEncrypted = await encryptPassword(state.passwordForm.newPassword, {
      key_id: key.key_id,
      public_key: key.public_key,
    })
    await useHttp().post('/api/v1/portal/user-center/password/update', {
      old_password: oldEncrypted.encrypted,
      new_password: newEncrypted.encrypted,
      password_key_id: key.key_id,
    })
    state.passwordForm.oldPassword = ''
    state.passwordForm.newPassword = ''
    state.passwordForm.confirmPassword = ''
    toast.add({ title: '密码已更新', color: 'success' })
  } catch {
    // error toast by interceptor
  } finally {
    state.savingPassword = false
  }
}

function openBindConfirm(type: 'phone' | 'email') {
  state.bindConfirm.type = type
  state.bindConfirm.password = ''
  state.bindConfirm.show = true
}

async function confirmBind() {
  if (!state.bindConfirm.password) {
    toast.add({ title: '错误', description: '请输入当前密码', color: 'error' })
    return
  }
  const isEmail = state.bindConfirm.type === 'email'
  state.bindConfirm.loading = true
  state.savingEmail = isEmail
  try {
    const { data: key } = await useHttp().get('/api/v1/portal/password-key')
    const { encryptPassword } = await import('~/utils/crypto')
    const encrypted = await encryptPassword(state.bindConfirm.password, {
      key_id: key.key_id,
      public_key: key.public_key,
    })

    if (isEmail) {
      if (!state.emailForm.email || !/\S+@\S+\.\S+/.test(state.emailForm.email)) {
        toast.add({ title: '错误', description: '邮箱格式不正确', color: 'error' })
        return
      }
      await useHttp().post('/api/v1/portal/user-center/email/update', {
        password: encrypted.encrypted,
        email: state.emailForm.email.trim() || null,
        email_login_enabled: state.emailForm.emailLoginEnabled,
        password_key_id: key.key_id,
      })
    }
    state.bindConfirm.show = false
    state.bindConfirm.password = ''
    await authStore.refreshUserInfo()
    syncForms()
    toast.add({ title: '绑定已更新', color: 'success' })
  } catch {
    // error toast by interceptor
  } finally {
    state.bindConfirm.loading = false
    state.savingEmail = false
  }
}

function refreshMe() {
  authStore.refreshUserInfo().then(syncForms)
}
</script>

<template>
  <div class="w-full min-w-0">
    <div v-if="state.loading" class="flex justify-center py-16">
      <UIcon name="i-lucide-loader-circle" class="size-8 animate-spin text-muted" />
    </div>
    <template v-else>
      <div class="flex flex-col lg:flex-row gap-4 p-6">
        <!-- Left card -->
        <UCard class="w-full lg:w-72 shrink-0 h-fit">
          <div class="flex flex-col items-center text-center py-2">
            <button
              type="button"
              title="更换头像"
              class="avatar-trigger"
              @click="avatarModalOpen = true"
            >
              <UAvatar v-if="avatarUrl" :src="avatarUrl" size="2xl" class="rounded-full" />
              <UAvatar v-else size="2xl" icon="i-lucide-user" class="rounded-full" />
            </button>
            <div class="mt-3 text-lg font-semibold truncate max-w-full">
              {{ displayName }}
            </div>
            <div class="text-sm text-muted">
              {{ authStore.userInfo?.account }}
            </div>
          </div>

          <USeparator class="my-3" />

          <dl class="space-y-2 text-sm">
            <div class="flex justify-between">
              <dt class="text-muted">昵称</dt>
              <dd class="font-medium">{{ authStore.userInfo?.nickname || '未设置' }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-muted">邮箱</dt>
              <dd class="font-medium">{{ authStore.userInfo?.email || '未设置' }}</dd>
            </div>
          </dl>

          <USeparator class="my-3" />

          <div class="text-sm font-medium mb-1">个性签名</div>
          <div class="min-h-12 rounded p-2 text-sm text-muted bg-muted">
            {{ authStore.userInfo?.signature || '未设置' }}
          </div>
        </UCard>

        <!-- Right card with tabs -->
        <UCard class="flex-1 min-w-0">
          <UTabs
            v-model="state.activeTab"
            :items="[
              { label: '基本信息', value: 'basic_info' },
              { label: '密码', value: 'password' },
              { label: '邮箱', value: 'email' },
            ]"
          >
            <template #content="{ item }">
              <!-- Basic Info -->
              <div v-if="item.value === 'basic_info'" class="space-y-4 pt-4 max-w-lg">
                <UFormField label="账号">
                  <UInput :model-value="authStore.userInfo?.account" disabled class="w-full" />
                </UFormField>
                <UFormField label="姓名">
                  <UInput v-model="state.profileForm.name" class="w-full" />
                </UFormField>
                <UFormField label="昵称">
                  <UInput v-model="state.profileForm.nickname" class="w-full" />
                </UFormField>
                <UFormField label="个性签名">
                  <UTextarea v-model="state.profileForm.signature" class="w-full" />
                </UFormField>
                <UButton color="primary" :loading="state.savingProfile" @click="saveProfile">
                  保存
                </UButton>
              </div>

              <!-- Password -->
              <div v-if="item.value === 'password'" class="space-y-4 pt-4 max-w-lg">
                <UFormField label="旧密码">
                  <UInput v-model="state.passwordForm.oldPassword" type="password" class="w-full" />
                </UFormField>
                <UFormField label="新密码">
                  <UInput v-model="state.passwordForm.newPassword" type="password" class="w-full" />
                </UFormField>
                <UFormField label="确认密码">
                  <UInput
                    v-model="state.passwordForm.confirmPassword"
                    type="password"
                    class="w-full"
                  />
                </UFormField>
                <UButton color="primary" :loading="state.savingPassword" @click="savePassword">
                  修改密码
                </UButton>
              </div>

              <!-- Email -->
              <div v-if="item.value === 'email'" class="space-y-4 pt-4 max-w-lg">
                <UFormField label="邮箱">
                  <UInput v-model="state.emailForm.email" type="email" class="w-full" />
                </UFormField>
                <UFormField label="启用邮箱登录">
                  <USwitch v-model="state.emailForm.emailLoginEnabled" />
                </UFormField>
                <UButton
                  color="primary"
                  :loading="state.savingEmail"
                  @click="openBindConfirm('email')"
                >
                  修改邮箱
                </UButton>
              </div>
            </template>
          </UTabs>
        </UCard>
      </div>
    </template>

    <!-- Bind confirm modal -->
    <UModal v-model:open="state.bindConfirm.show">
      <template #title>确认更新</template>
      <template #body>
        <UFormField label="当前密码">
          <UInput
            v-model="state.bindConfirm.password"
            type="password"
            placeholder="请输入当前密码"
            class="w-full"
            @keydown.enter="confirmBind"
          />
        </UFormField>
      </template>
      <template #footer>
        <div class="flex justify-end gap-2 w-full">
          <UButton color="neutral" variant="ghost" @click="state.bindConfirm.show = false"
            >取消</UButton
          >
          <UButton :loading="state.bindConfirm.loading" @click="confirmBind">确认</UButton>
        </div>
      </template>
    </UModal>

    <AvatarCropperModal v-model:open="avatarModalOpen" :avatar="avatarUrl" @uploaded="refreshMe" />
  </div>
</template>

<style scoped>
.avatar-trigger {
  border: 0;
  border-radius: 999px;
  background: transparent;
  padding: 0;
  cursor: pointer;
  line-height: 0;
  transition:
    background-color 0.2s,
    box-shadow 0.2s,
    transform 0.2s;
}
.avatar-trigger:hover {
  box-shadow:
    0 0 0 3px var(--ui-bg),
    0 0 0 5px var(--ui-primary);
  transform: translateY(-1px);
  outline: none;
}
</style>
