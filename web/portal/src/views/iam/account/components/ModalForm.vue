<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { accountApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
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
  email_identity: '',
  phone_identity: '',
  email_identity_verified: false,
  phone_identity_verified: false,
  email_identity_bind_status: 'BOUND',
  phone_identity_bind_status: 'BOUND',
  employee_no: '',
  title: '',
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
  state.dataId ? t('resource.iam.account.edit_account') : t('resource.iam.account.add_account'),
)

const rules = computed<FormRules>(() => ({
  account: createRequiredRule(t, t('resource.iam.account.account'), 'input'),
  password: [
    {
      validator: (_rule, value) => {
        if (!state.dataId && !String(value ?? '').trim()) {
          return new Error(t('common.required', { field: t('resource.iam.account.password') }))
        }
        return true
      },
      trigger: ['input', 'blur'],
    },
  ],
  account_type: createRequiredRule(t, t('resource.iam.account.account_type'), 'change'),
  account_status: createRequiredRule(t, t('resource.iam.account.account_status'), 'change'),
  name: createRequiredRule(t, t('resource.iam.account.name'), 'input'),
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
      email_identity: response.data?.email_identity ?? '',
      phone_identity: response.data?.phone_identity ?? '',
      email_identity_verified: Boolean(response.data?.email_identity_verified),
      phone_identity_verified: Boolean(response.data?.phone_identity_verified),
      email_identity_bind_status: response.data?.email_identity_bind_status ?? 'BOUND',
      phone_identity_bind_status: response.data?.phone_identity_bind_status ?? 'BOUND',
      employee_no: response.data?.employee_no ?? '',
      title: response.data?.title ?? '',
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
      email_identity: toNullableString(state.formModel.email_identity),
      phone_identity: toNullableString(state.formModel.phone_identity),
      email_identity_verified: Boolean(state.formModel.email_identity_verified),
      phone_identity_verified: Boolean(state.formModel.phone_identity_verified),
      email_identity_bind_status: state.formModel.email_identity_bind_status,
      phone_identity_bind_status: state.formModel.phone_identity_bind_status,
      employee_no: toNullableString(state.formModel.employee_no),
      title: toNullableString(state.formModel.title),
      remark: toNullableString(state.formModel.remark),
    }

    if (state.dataId) {
      await accountApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.update_success'))
    } else {
      await accountApi.create(payload)
      window.$message.success(t('common.often.create_success'))
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
            <NTabPane name="account" :tab="t('resource.iam.account.account_info')">
              <NFormItem :label="t('resource.iam.account.password')" path="password">
                <NInput
                  v-model:value="state.formModel.password"
                  type="password"
                  show-password-on="click"
                  :placeholder="
                    state.dataId ? t('resource.iam.account.placeholder.password_edit') : undefined
                  "
                />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.account_type')" path="account_type">
                <DictSelect v-model="state.formModel.account_type" dict-code="ACCOUNT_TYPE" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.account_status')" path="account_status">
                <DictSelect
                  v-model="state.formModel.account_status"
                  dict-code="ACCOUNT_STATUS"
                  type="radio"
                />
              </NFormItem>
            </NTabPane>

            <NTabPane name="identity" :tab="t('resource.iam.account.login_identity')">
              <NFormItem :label="t('resource.iam.account.account')" path="account">
                <NInput v-model:value="state.formModel.account" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.email_identity')" path="email_identity">
                <NInput v-model:value="state.formModel.email_identity" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.email_identity_verified')">
                <NSwitch v-model:value="state.formModel.email_identity_verified" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.email_identity_bind_status')">
                <DictSelect
                  v-model="state.formModel.email_identity_bind_status"
                  dict-code="ACCOUNT_IDENTITY_BIND_STATUS"
                />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.phone_identity')" path="phone_identity">
                <NInput v-model:value="state.formModel.phone_identity" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.phone_identity_verified')">
                <NSwitch v-model:value="state.formModel.phone_identity_verified" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.phone_identity_bind_status')">
                <DictSelect
                  v-model="state.formModel.phone_identity_bind_status"
                  dict-code="ACCOUNT_IDENTITY_BIND_STATUS"
                />
              </NFormItem>
            </NTabPane>

            <NTabPane name="profile" :tab="t('resource.iam.account.profile_info')">
              <NFormItem :label="t('resource.iam.account.name')" path="name">
                <NInput v-model:value="state.formModel.name" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.nickname')" path="nickname">
                <NInput v-model:value="state.formModel.nickname" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.avatar')" path="avatar">
                <NInput v-model:value="state.formModel.avatar" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.signature')" path="signature">
                <NInput
                  v-model:value="state.formModel.signature"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 5 }"
                />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.phone')" path="phone">
                <NInput v-model:value="state.formModel.phone" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.email')" path="email">
                <NInput v-model:value="state.formModel.email" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.employee_no')" path="employee_no">
                <NInput v-model:value="state.formModel.employee_no" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.title_name')" path="title">
                <NInput v-model:value="state.formModel.title" />
              </NFormItem>
              <NFormItem :label="t('resource.iam.account.remark')" path="remark">
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
          {{ t('common.cancel') }}
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
