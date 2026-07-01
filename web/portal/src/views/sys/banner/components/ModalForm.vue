<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { bannerApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  title: '',
  image: '',
  url: '',
  link_type: 'URL',
  summary: '',
  description: '',
  category: 'HOME',
  type: 'CAROUSEL',
  position: 'HOME_TOP',
  display_scope: 'PORTAL',
  sort: 0,
  status: 'ENABLED',
  start_at: '',
  end_at: '',
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? t('resource.sys.banner.edit_banner') : t('resource.sys.banner.add_banner'),
)

const rules = computed<FormRules>(() => ({
  title: createRequiredRule(t, t('resource.sys.banner.title_field'), 'input'),
  image: createRequiredRule(t, t('resource.sys.banner.image'), 'input'),
  link_type: createRequiredRule(t, t('resource.sys.banner.link_type'), 'change'),
  category: createRequiredRule(t, t('resource.sys.banner.category'), 'change'),
  type: createRequiredRule(t, t('resource.sys.banner.type'), 'change'),
  position: createRequiredRule(t, t('resource.sys.banner.position'), 'change'),
  display_scope: createRequiredRule(t, t('resource.sys.banner.display_scope'), 'change'),
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
    const response = await bannerApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data)
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
      url: toNullableString(state.formModel.url),
      summary: toNullableString(state.formModel.summary),
      description: toNullableString(state.formModel.description),
      sort: Number(state.formModel.sort ?? 0),
      start_at: toNullableString(state.formModel.start_at),
      end_at: toNullableString(state.formModel.end_at),
    }

    if (state.dataId) {
      await bannerApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.update_success'))
    } else {
      await bannerApi.create(payload)
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
          label-width="100"
          :disabled="state.loading || state.submitLoading"
        >
          <NFormItem :label="t('resource.sys.banner.title_field')" path="title">
            <NInput v-model:value="state.formModel.title" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.image')" path="image">
            <NInput v-model:value="state.formModel.image" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.url')" path="url">
            <NInput v-model:value="state.formModel.url" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.link_type')" path="link_type">
            <DictSelect
              v-model="state.formModel.link_type"
              dict-code="BANNER_LINK_TYPE"
              type="radio"
            />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.category')" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="BANNER_CATEGORY" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.type')" path="type">
            <DictSelect v-model="state.formModel.type" dict-code="BANNER_TYPE" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.position')" path="position">
            <DictSelect v-model="state.formModel.position" dict-code="BANNER_POSITION" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.display_scope')" path="display_scope">
            <DictSelect v-model="state.formModel.display_scope" dict-code="BANNER_DISPLAY_SCOPE" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.sort')" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.start_at')" path="start_at">
            <NInput v-model:value="state.formModel.start_at" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.end_at')" path="end_at">
            <NInput v-model:value="state.formModel.end_at" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.summary')" path="summary">
            <NInput v-model:value="state.formModel.summary" />
          </NFormItem>
          <NFormItem :label="t('resource.sys.banner.description')" path="description">
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
