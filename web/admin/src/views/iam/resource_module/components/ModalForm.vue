<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { resourceModuleApi } from '@/api'
import CommonColorPicker from '@/components/common/CommonColorPicker.vue'
import { createRequiredRule, isHexColor, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  name: '',
  code: '',
  client: 'ADMIN',
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
    ? 'Edit Resource Module'
    : 'Add Resource Module',
)

const rules = computed<FormRules>(() => ({
  name: createRequiredRule('Module Name', 'input'),
  code: createRequiredRule('Module Code', 'input'),
  client: createRequiredRule('Client', 'change'),
  color: [
    {
      validator: (_rule, value) => isHexColor(value),
      message: 'Enter a hex color, for example #1677ff',
      trigger: ['change', 'blur'],
    },
  ],
  status: createRequiredRule('Status', 'change'),
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
      client: response.data?.client ?? defaultFormData.client,
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
      client: state.formModel.client,
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
      window.$message.success('Updated successfully')
    } else {
      await resourceModuleApi.create(payload)
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
          label-width="100"
          :disabled="state.loading || state.submitLoading"
        >
          <NFormItem :label="'Module Name'" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="'Module Code'" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="'Client'" path="client">
            <DictSelect
              v-model="state.formModel.client"
              dict-code="RESOURCE_MODULE_CLIENT"
              type="radio"
            />
          </NFormItem>
          <NFormItem :label="'Icon'" path="icon">
            <NInput v-model:value="state.formModel.icon" />
          </NFormItem>
          <NFormItem :label="'Color'" path="color">
            <CommonColorPicker v-model="state.formModel.color" />
          </NFormItem>
          <NFormItem :label="'Sort'" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="'Status'" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="'Description'" path="description">
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
          {{ 'Cancel' }}
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ 'Confirm' }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
