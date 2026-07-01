<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { deptApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  name: '',
  code: '',
  category: null as string | null,
  parent_id: '',
  master_id: '',
  deputy_master_id: '',
  sort: 0,
  is_virtual: false,
  status: 'ENABLED',
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
  state.dataId ? t('resource.iam.dept.edit_dept') : t('resource.iam.dept.add_dept'),
)

const rules = computed<FormRules>(() => ({
  name: createRequiredRule(t, t('resource.iam.dept.name'), 'input'),
  code: createRequiredRule(t, t('resource.iam.dept.code'), 'input'),
  category: createRequiredRule(t, t('resource.iam.dept.category'), 'change'),
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
    const response = await deptApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      parent_id: response.data?.parent_id ?? '',
      master_id: response.data?.master_id ?? '',
      deputy_master_id: response.data?.deputy_master_id ?? '',
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
      name: state.formModel.name.trim(),
      code: state.formModel.code.trim(),
      parent_id: toNullableString(state.formModel.parent_id),
      master_id: toNullableString(state.formModel.master_id),
      deputy_master_id: toNullableString(state.formModel.deputy_master_id),
      sort: Number(state.formModel.sort ?? 0),
      is_virtual: Boolean(state.formModel.is_virtual),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await deptApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.update_success'))
    } else {
      await deptApi.create(payload)
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
          <NFormItem :label="t('resource.iam.dept.name')" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.code')" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.category')" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="DEPT_CATEGORY" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.parent_id')" path="parent_id">
            <NInput v-model:value="state.formModel.parent_id" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.master_id')" path="master_id">
            <NInput v-model:value="state.formModel.master_id" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.deputy_master_id')" path="deputy_master_id">
            <NInput v-model:value="state.formModel.deputy_master_id" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.sort')" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.dept.is_virtual')" path="is_virtual">
            <NSwitch v-model:value="state.formModel.is_virtual" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
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
