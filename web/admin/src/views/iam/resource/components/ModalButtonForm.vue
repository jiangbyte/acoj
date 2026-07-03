<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { deptApi, resourceApi } from '@/api'
import { createRequiredRule, toNullableString, translateLocale } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ModalPermissionSelector from './ModalPermissionSelector.vue'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const permissionSelectorRef = ref<any>(null)
const defaultFormData = {
  id: '',
  parent_id: '',
  code: '',
  name: '',
  locale_key: '',
  permission_key: '',
  data_scope: 'ALL',
  custom_scope_dept_ids: [] as string[],
  sort: 99,
  status: 'ENABLED',
  description: '',
}
const state = reactive({
  showModal: false,
  submitLoading: false,
  treeLoading: false,
  parent: {} as any,
  deptTree: [] as any[],
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() => {
  const action = state.formModel.id
    ? t('resource.iam.resource.edit_button')
    : t('resource.iam.resource.add_button')
  return state.parent?.name
    ? `${action} - ${translateLocale(state.parent.locale_key, state.parent.name)}`
    : action
})
const rules = computed<FormRules>(() => ({
  code: createRequiredRule(t, t('resource.iam.resource.code'), 'input'),
  name: createRequiredRule(t, t('resource.iam.resource.name'), 'input'),
  permission_key: createRequiredRule(t, t('resource.iam.resource.permission_key'), 'change'),
  data_scope: createRequiredRule(t, t('resource.iam.resource.data_scope'), 'change'),
  status: createRequiredRule(t, t('common.often.status'), 'change'),
}))

async function openModal(parent: any, row?: any) {
  state.parent = parent ?? {}
  state.formModel = {
    ...defaultFormData,
    ...row,
    id: row?.id ?? '',
    parent_id: parent?.id ?? row?.parent_id ?? '',
    locale_key: row?.locale_key ?? '',
    permission_key: row?.permission_key ?? '',
    data_scope: row?.data_scope ?? 'ALL',
    custom_scope_dept_ids: row?.custom_scope_dept_ids ?? [],
    sort: Number(row?.sort ?? 99),
    status: row?.status ?? 'ENABLED',
    description: row?.description ?? row?.permission_description ?? '',
  }
  state.showModal = true
  await fetchDeptTree()
}

async function fetchDeptTree() {
  state.treeLoading = true
  try {
    const response = await deptApi.tree().catch(() => ({ data: [] }))
    state.deptTree = response.data ?? []
  } finally {
    state.treeLoading = false
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

function openPermissionSelector() {
  permissionSelectorRef.value?.openModal(state.formModel.permission_key)
}

function handlePermissionSelected(permission: any) {
  state.formModel.permission_key = permission.permission_key
  if (!state.formModel.description && permission.name !== permission.permission_key) {
    state.formModel.description = permission.name
  }
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = {
      parent_id: state.formModel.parent_id,
      code: state.formModel.code.trim(),
      name: state.formModel.name.trim(),
      locale_key: toNullableString(state.formModel.locale_key),
      permission_key: state.formModel.permission_key.trim(),
      data_scope: state.formModel.data_scope,
      custom_scope_dept_ids:
        state.formModel.data_scope === 'CUSTOM' ? state.formModel.custom_scope_dept_ids : [],
      sort: Number(state.formModel.sort ?? 99),
      status: state.formModel.status,
      description: toNullableString(state.formModel.description),
    }

    if (state.formModel.id) {
      await resourceApi.buttonUpdate({
        ...payload,
        id: state.formModel.id,
      })
      window.$message.success(t('common.often.update_success'))
    } else {
      await resourceApi.buttonCreate(payload)
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
    style="width: 680px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.treeLoading">
      <NForm
        ref="formRef"
        :model="state.formModel"
        :rules="rules"
        label-placement="left"
        label-width="120"
        :disabled="state.submitLoading"
      >
        <NFormItem :label="t('resource.iam.resource.name')" path="name">
          <NInput v-model:value="state.formModel.name" />
        </NFormItem>
        <NFormItem :label="t('common.often.locale_key')" path="locale_key">
          <NInput v-model:value="state.formModel.locale_key" />
        </NFormItem>
        <NFormItem :label="t('resource.iam.resource.code')" path="code">
          <NInput v-model:value="state.formModel.code" />
        </NFormItem>
        <NFormItem :label="t('resource.iam.resource.permission_key')" path="permission_key">
          <NInputGroup>
            <NInput v-model:value="state.formModel.permission_key" readonly />
            <NButton type="primary" secondary @click="openPermissionSelector">
              {{ t('resource.iam.resource.select_permission') }}
            </NButton>
          </NInputGroup>
        </NFormItem>
        <NFormItem :label="t('resource.iam.resource.data_scope')" path="data_scope">
          <DictSelect v-model="state.formModel.data_scope" dict-code="DATA_SCOPE" />
        </NFormItem>
        <NFormItem
          v-if="state.formModel.data_scope === 'CUSTOM'"
          :label="t('resource.iam.resource.custom_scope_dept_ids')"
          path="custom_scope_dept_ids"
        >
          <NTreeSelect
            v-model:value="state.formModel.custom_scope_dept_ids"
            multiple
            cascade
            checkable
            clearable
            filterable
            :options="state.deptTree"
            key-field="id"
            label-field="name"
            children-field="children"
          />
        </NFormItem>
        <NFormItem :label="t('resource.iam.resource.sort')" path="sort">
          <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
        </NFormItem>
        <NFormItem :label="t('common.often.status')" path="status">
          <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
        </NFormItem>
        <NFormItem :label="t('resource.iam.resource.description')" path="description">
          <NInput
            v-model:value="state.formModel.description"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 5 }"
          />
        </NFormItem>
      </NForm>
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

  <ModalPermissionSelector ref="permissionSelectorRef" @selected="handlePermissionSelected" />
</template>
