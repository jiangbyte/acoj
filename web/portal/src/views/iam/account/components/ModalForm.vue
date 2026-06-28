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
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? t('pages.iam.account.editAccount') : t('pages.iam.account.addAccount'),
)

const rules = computed<FormRules>(() => ({
  account: createRequiredRule(t, t('pages.iam.account.account'), 'input'),
  password: [
    {
      validator: (_rule, value) => {
        if (!state.dataId && !String(value ?? '').trim()) {
          return new Error(t('common.required', { field: t('pages.iam.account.password') }))
        }
        return true
      },
      trigger: ['input', 'blur'],
    },
  ],
  account_type: createRequiredRule(t, t('pages.iam.account.accountType'), 'change'),
  account_status: createRequiredRule(t, t('pages.iam.account.accountStatus'), 'change'),
  name: createRequiredRule(t, t('pages.iam.account.name'), 'input'),
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
    }

    if (state.dataId) {
      await accountApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.updateSuccess'))
    } else {
      await accountApi.create(payload)
      window.$message.success(t('common.often.createSuccess'))
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
          <NFormItem :label="t('pages.iam.account.account')" path="account">
            <NInput v-model:value="state.formModel.account" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.password')" path="password">
            <NInput
              v-model:value="state.formModel.password"
              type="password"
              show-password-on="click"
              :placeholder="
                state.dataId ? t('pages.iam.account.passwordEditPlaceholder') : undefined
              "
            />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.accountType')" path="account_type">
            <DictSelect v-model="state.formModel.account_type" dict-code="ACCOUNT_TYPE" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.accountStatus')" path="account_status">
            <DictSelect
              v-model="state.formModel.account_status"
              dict-code="ACCOUNT_STATUS"
              type="radio"
            />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.name')" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.nickname')" path="nickname">
            <NInput v-model:value="state.formModel.nickname" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.avatar')" path="avatar">
            <NInput v-model:value="state.formModel.avatar" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.signature')" path="signature">
            <NInput
              v-model:value="state.formModel.signature"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
            />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.phone')" path="phone">
            <NInput v-model:value="state.formModel.phone" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.account.email')" path="email">
            <NInput v-model:value="state.formModel.email" />
          </NFormItem>
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
