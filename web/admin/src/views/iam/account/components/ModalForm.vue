<script setup lang="ts">
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { accountApi } from '@/api'
import { createRequiredRule, isValidEmail, toNullableString } from '@/utils'
import { encryptPasswords } from '@/utils/security'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  account: '',
  password: '',
  account_type: 'PORTAL',
  account_status: 'ENABLED',
  name: '',
  nickname: '',
  avatar: '',
  signature: '',
  phone: '',
  email: '',
  email_login_enabled: false,
  phone_login_enabled: false,
  email_identity: '',
  phone_identity: '',
  email_identity_verified: false,
  phone_identity_verified: false,
  email_identity_bind_status: 'BOUND',
  phone_identity_bind_status: 'BOUND',
  employee_no: '',
  title: '',
  bio: '',
  level: '',
  remark: '',
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? 'Edit Account' : 'Add Account',
)

function validateEmailIdentity(_rule: FormItemRule, value: string) {
  const text = String(value ?? '').trim()
  if (!text) {
    return state.formModel.email_login_enabled
      ? new Error('Please enter Email')
      : true
  }
  if (!isValidEmail(text)) {
    return new Error('Please enter a valid email')
  }
  return true
}

const rules = computed<FormRules>(() => ({
  account: createRequiredRule('Account', 'input'),
  password: [
    {
      validator: (_rule, value) => {
        if (!state.dataId && !String(value ?? '').trim()) {
          return new Error('Please enter Password')
        }
        return true
      },
      trigger: ['input', 'blur'],
    },
  ],
  account_type: createRequiredRule('Account Type', 'change'),
  account_status: createRequiredRule('Account Status', 'change'),
  email: [
    {
      validator: validateEmailIdentity,
      trigger: ['input', 'blur'],
    },
  ],
}))

async function openModal(id?: string) {
  state.dataId = id ?? null
  state.formModel = { ...defaultFormData }
  state.showModal = true

  if (id) {
    await fetchDetail(id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await accountApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      password: '',
      nickname: response.data?.nickname ?? '',
      avatar: response.data?.avatar ?? '',
      signature: response.data?.signature ?? '',
      phone: response.data?.phone ?? '',
      email: response.data?.email ?? '',
      email_login_enabled: Boolean(response.data?.email_login_enabled),
      phone_login_enabled: Boolean(response.data?.phone_login_enabled),
      email_identity: response.data?.email_identity ?? '',
      phone_identity: response.data?.phone_identity ?? '',
      email_identity_verified: Boolean(response.data?.email_identity_verified),
      phone_identity_verified: Boolean(response.data?.phone_identity_verified),
      email_identity_bind_status: response.data?.email_identity_bind_status ?? 'BOUND',
      phone_identity_bind_status: response.data?.phone_identity_bind_status ?? 'BOUND',
      employee_no: response.data?.employee_no ?? '',
      title: response.data?.title ?? '',
      bio: response.data?.bio ?? '',
      level: response.data?.level ?? '',
      remark: response.data?.remark ?? '',
    })
  } finally {
    state.loading = false
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload: any = {
      ...state.formModel,
      account: state.formModel.account.trim(),
      password: toNullableString(state.formModel.password),
      name: state.formModel.name.trim(),
      nickname: toNullableString(state.formModel.nickname),
      avatar: toNullableString(state.formModel.avatar),
      signature: toNullableString(state.formModel.signature),
      phone: toNullableString(state.formModel.phone),
      email: toNullableString(state.formModel.email),
      email_login_enabled: Boolean(state.formModel.email_login_enabled),
      phone_login_enabled: Boolean(state.formModel.phone_login_enabled),
      email_identity: state.formModel.email_login_enabled
        ? toNullableString(state.formModel.email)
        : null,
      phone_identity: state.formModel.phone_login_enabled
        ? toNullableString(state.formModel.phone)
        : null,
      email_identity_verified: Boolean(state.formModel.email_login_enabled),
      phone_identity_verified: Boolean(state.formModel.phone_login_enabled),
      email_identity_bind_status: 'BOUND',
      phone_identity_bind_status: 'BOUND',
      employee_no: toNullableString(state.formModel.employee_no),
      title: toNullableString(state.formModel.title),
      bio: toNullableString(state.formModel.bio),
      level: toNullableString(state.formModel.level),
      remark: toNullableString(state.formModel.remark),
    }
    if (payload.password) {
      const encrypted = await encryptPasswords({ password: payload.password })
      payload.password = encrypted.values.password
      payload.password_key_id = encrypted.password_key_id
    } else {
      payload.password_key_id = null
    }
    if (payload.account_type === 'ADMIN') {
      payload.bio = null
      payload.level = null
    } else {
      payload.employee_no = null
      payload.title = null
      payload.remark = null
    }

    if (state.dataId) {
      await accountApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success('Updated successfully')
    } else {
      await accountApi.create(payload)
      window.$message.success('Created successfully')
    }

    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="modalTitle"
    style="width: 720px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.loading">
      <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
        <NForm
          ref="formRef"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="110"
          :disabled="state.loading || state.submitLoading"
        >
          <NTabs type="line" animated>
            <NTabPane name="account" :tab="'Account Info'">
              <NFormItem :label="'Password'" path="password">
                <NInput
                  v-model:value="state.formModel.password"
                  type="password"
                  show-password-on="click"
                  :placeholder="
                    state.dataId ? 'Leave blank to keep current password' : undefined
                  "
                />
              </NFormItem>
              <NFormItem :label="'Account Type'" path="account_type">
                <DictSelect v-model="state.formModel.account_type" dict-code="ACCOUNT_TYPE" />
              </NFormItem>
              <NFormItem :label="'Account Status'" path="account_status">
                <DictSelect
                  v-model="state.formModel.account_status"
                  dict-code="ACCOUNT_STATUS"
                  type="radio"
                />
              </NFormItem>
            </NTabPane>

            <NTabPane name="identity" :tab="'Login Identity'">
              <NFormItem :label="'Account'" path="account">
                <NInput v-model:value="state.formModel.account" />
              </NFormItem>
              <NFormItem :label="'Email'" path="email">
                <NInput v-model:value="state.formModel.email" />
              </NFormItem>
              <NFormItem :label="'Enable Email Login'">
                <NSwitch v-model:value="state.formModel.email_login_enabled" />
              </NFormItem>
              <NFormItem :label="'Phone'" path="phone">
                <NInput v-model:value="state.formModel.phone" />
              </NFormItem>
              <NFormItem :label="'Enable Phone Login'">
                <NSwitch v-model:value="state.formModel.phone_login_enabled" />
              </NFormItem>
            </NTabPane>

            <NTabPane name="profile" :tab="'User Profile'">
              <NFormItem :label="'Name'" path="name">
                <NInput v-model:value="state.formModel.name" />
              </NFormItem>
              <NFormItem :label="'Nickname'" path="nickname">
                <NInput v-model:value="state.formModel.nickname" />
              </NFormItem>
              <NFormItem :label="'Avatar'" path="avatar">
                <ImageUpload v-model:value="state.formModel.avatar" />
              </NFormItem>
              <NFormItem :label="'Signature'" path="signature">
                <NInput
                  v-model:value="state.formModel.signature"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 5 }"
                />
              </NFormItem>
              <NFormItem
                v-if="state.formModel.account_type === 'ADMIN'"
                :label="'Employee No.'"
                path="employee_no"
              >
                <NInput v-model:value="state.formModel.employee_no" />
              </NFormItem>
              <NFormItem
                v-if="state.formModel.account_type === 'ADMIN'"
                :label="'Title'"
                path="title"
              >
                <NInput v-model:value="state.formModel.title" />
              </NFormItem>
              <NFormItem
                v-if="state.formModel.account_type === 'PORTAL'"
                :label="'Level'"
                path="level"
              >
                <NInput v-model:value="state.formModel.level" />
              </NFormItem>
              <NFormItem
                v-if="state.formModel.account_type === 'PORTAL'"
                :label="'Bio'"
                path="bio"
              >
                <NInput
                  v-model:value="state.formModel.bio"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 5 }"
                />
              </NFormItem>
              <NFormItem
                v-if="state.formModel.account_type === 'ADMIN'"
                :label="'Remark'"
                path="remark"
              >
                <NInput
                  v-model:value="state.formModel.remark"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 5 }"
                />
              </NFormItem>
            </NTabPane>
          </NTabs>
        </NForm>
      </NScrollbar>
    </NSpin>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          {{ 'Cancel' }}
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ 'Confirm' }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
