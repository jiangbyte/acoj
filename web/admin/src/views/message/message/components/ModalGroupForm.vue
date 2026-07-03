<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { messageApi } from '@/api'
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
  avatar: '',
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
  state.dataId ? t('resource.message.message.edit_group') : t('resource.message.message.add_group'),
)

const rules = computed<FormRules>(() => ({
  name: createRequiredRule(t, t('resource.message.message.group_name'), 'input'),
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
    const response = await messageApi.groupDetail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      avatar: response.data?.avatar ?? '',
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
      avatar: toNullableString(state.formModel.avatar),
      description: toNullableString(state.formModel.description),
      extra: state.formModel.extra ?? {},
    }
    if (state.dataId) {
      await messageApi.updateGroup({ ...payload, id: state.dataId })
      window.$message.success(t('common.often.update_success'))
    } else {
      await messageApi.createGroup(payload)
      window.$message.success(t('common.often.create_success'))
    }
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

defineExpose({ openModal })
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
    <NSpin :show="state.loading">
      <NForm
        ref="formRef"
        :model="state.formModel"
        :rules="rules"
        label-placement="left"
        label-width="100"
        :disabled="state.loading || state.submitLoading"
      >
        <NFormItem :label="t('resource.message.message.group_name')" path="name">
          <NInput v-model:value="state.formModel.name" />
        </NFormItem>
        <NFormItem :label="t('resource.message.message.group_avatar')" path="avatar">
          <ImageUpload v-model:value="state.formModel.avatar" />
        </NFormItem>
        <NFormItem :label="t('common.often.status')" path="status">
          <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
        </NFormItem>
        <NFormItem :label="t('resource.message.message.group_description')" path="description">
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
        <NButton @click="closeModal">{{ t('common.cancel') }}</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
