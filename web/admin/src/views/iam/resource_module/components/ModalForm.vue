<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { resourceModuleApi } from '@/api'
import CommonColorPicker from '@/components/common/CommonColorPicker.vue'
import { createRequiredRule, isHexColor, toNullableString } from '@/utils'
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
  icon: '',
  color: '',
  sort: 0,
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
  state.dataId
    ? t('pages.iam.resource_module.editModule')
    : t('pages.iam.resource_module.addModule'),
)

const rules = computed<FormRules>(() => ({
  name: createRequiredRule(t, t('pages.iam.resource_module.name'), 'input'),
  code: createRequiredRule(t, t('pages.iam.resource_module.code'), 'input'),
  color: [
    {
      validator: (_rule, value) => isHexColor(value),
      message: t('pages.iam.resource_module.colorPattern'),
      trigger: ['change', 'blur'],
    },
  ],
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
    const response = await resourceModuleApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      icon: response.data?.icon ?? '',
      color: response.data?.color ?? '',
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
      icon: toNullableString(state.formModel.icon),
      color: toNullableString(state.formModel.color),
      sort: Number(state.formModel.sort ?? 0),
      description: toNullableString(state.formModel.description),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await resourceModuleApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.updateSuccess'))
    } else {
      await resourceModuleApi.create(payload)
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
          label-width="100"
          :disabled="state.loading || state.submitLoading"
        >
          <NFormItem :label="t('pages.iam.resource_module.name')" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource_module.code')" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource_module.icon')" path="icon">
            <NInput v-model:value="state.formModel.icon" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource_module.color')" path="color">
            <CommonColorPicker v-model="state.formModel.color" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource_module.sort')" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource_module.description')" path="description">
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
