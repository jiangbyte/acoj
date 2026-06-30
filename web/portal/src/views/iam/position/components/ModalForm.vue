<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { positionApi } from '@/api'
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
  sort: 0,
  is_virtual: false,
  status: 'ENABLED',
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
  state.dataId ? t('resource.iam.position.edit_position') : t('resource.iam.position.add_position'),
)

const rules = computed<FormRules>(() => ({
  name: createRequiredRule(t, t('resource.iam.position.name'), 'input'),
  code: createRequiredRule(t, t('resource.iam.position.code'), 'input'),
  category: createRequiredRule(t, t('resource.iam.position.category'), 'change'),
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
    const response = await positionApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
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
      name: state.formModel.name.trim(),
      code: state.formModel.code.trim(),
      sort: Number(state.formModel.sort ?? 0),
      is_virtual: Boolean(state.formModel.is_virtual),
      description: toNullableString(state.formModel.description),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await positionApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.update_success'))
    } else {
      await positionApi.create(payload)
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
          <NFormItem :label="t('resource.iam.position.name')" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.position.code')" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.position.category')" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="POSITION_CATEGORY" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.position.sort')" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.position.is_virtual')" path="is_virtual">
            <NSwitch v-model:value="state.formModel.is_virtual" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="t('resource.iam.position.description')" path="description">
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
