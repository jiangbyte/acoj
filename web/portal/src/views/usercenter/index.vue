<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores'

const { t } = useI18n()
const authStore = useAuthStore()
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const state = reactive({
  loading: false,
  savingProfile: false,
  savingPassword: false,
  savingPhone: false,
  savingEmail: false,
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
}

async function saveProfile() {
  state.savingProfile = true
  try {
    await authApi.updateUserCenterProfile({
      name: state.profileForm.name,
      nickname: state.profileForm.nickname || null,
      avatar: state.profileForm.avatar || null,
      signature: state.profileForm.signature || null,
      bio: state.profileForm.bio || null,
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

function displayValue(value: unknown) {
  return value ? String(value) : t('app.user_center.empty_value')
}
</script>

<template>
  <div>
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
              <div class="mt-1 text-sm text-[var(--text-color-3)]">
                {{ state.me?.account }}
              </div>
            </div>

            <NDivider />

            <NDescriptions :column="1" label-placement="left" size="small">
              <NDescriptionsItem :label="t('app.user_center.contact')">
                {{ contactText }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('app.user_center.level')">
                {{ displayValue(profile.level) }}
              </NDescriptionsItem>
            </NDescriptions>

            <NDivider />

            <div class="text-sm font-medium">
              {{ t('app.user_center.signature') }}
            </div>
            <div
              class="mt-2 min-h-18 rounded border border-[var(--border-color)] p-3 text-sm text-[var(--text-color-3)]"
            >
              {{ displayValue(profile.signature) }}
            </div>
            <div class="mt-4 text-sm font-medium">
              {{ t('app.user_center.bio') }}
            </div>
            <div
              class="mt-2 min-h-22 rounded border border-[var(--border-color)] p-3 text-sm text-[var(--text-color-3)]"
            >
              {{ displayValue(profile.bio) }}
            </div>
          </NCard>
        </NGridItem>

        <NGridItem span="24 m:17">
          <NCard :bordered="false" content-class="min-h-140">
            <NTabs type="line" animated>
              <NTabPane name="basic_info" :tab="t('app.user_center.basic_info')">
                <NForm
                  :model="state.profileForm"
                  label-placement="top"
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
                  <NFormItem :label="t('app.user_center.signature')">
                    <NInput v-model:value="state.profileForm.signature" type="textarea" />
                  </NFormItem>
                  <NFormItem :label="t('app.user_center.bio')">
                    <NInput v-model:value="state.profileForm.bio" type="textarea" />
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
  </div>
</template>
