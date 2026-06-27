<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { roleApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  code: '',
  name: '',
  category: '',
  scope_type: '',
  owner_dept_id: '',
  sort: 0,
  status: 'ENABLED',
  is_builtin: false,
  description: '',
  extra: {},
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? t('pages.iam.role.editRole') : t('pages.iam.role.addRole'),
)

const rules = computed<FormRules>(() => ({
  code: createRequiredRule(t, t('pages.iam.role.code'), 'input'),
  name: createRequiredRule(t, t('pages.iam.role.name'), 'input'),
  category: createRequiredRule(t, t('pages.iam.role.category'), 'change'),
  scope_type: createRequiredRule(t, t('pages.iam.role.scopeType'), 'change'),
  status: createRequiredRule(t, t('common.often.status'), 'change'),
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
    const response = await roleApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      owner_dept_id: response.data?.owner_dept_id ?? '',
      description: response.data?.description ?? '',
      extra: response.data?.extra ?? {},
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
    const payload = {
      ...state.formModel,
      code: state.formModel.code.trim(),
      name: state.formModel.name.trim(),
      owner_dept_id: toNullableString(state.formModel.owner_dept_id),
      sort: Number(state.formModel.sort ?? 0),
      is_builtin: Boolean(state.formModel.is_builtin),
      description: toNullableString(state.formModel.description),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await roleApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.updateSuccess'))
    } else {
      await roleApi.create(payload)
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
          <NFormItem :label="t('pages.iam.role.code')" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.name')" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.category')" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="ROLE_CATEGORY" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.scopeType')" path="scope_type">
            <DictSelect v-model="state.formModel.scope_type" dict-code="ROLE_SCOPE_TYPE" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.ownerDeptId')" path="owner_dept_id">
            <NInput v-model:value="state.formModel.owner_dept_id" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.sort')" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.isBuiltin')" path="is_builtin">
            <NSwitch v-model:value="state.formModel.is_builtin" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.role.description')" path="description">
            <NInput
              v-model:value="state.formModel.description"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
            />
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
