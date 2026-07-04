<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { bannerApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

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
  state.dataId ? 'Edit Display Image' : 'Add Display Image',
)

const rules = computed<FormRules>(() => ({
  title: createRequiredRule('Title', 'input'),
  image: createRequiredRule('Image', 'input'),
  link_type: createRequiredRule('Link Type', 'change'),
  category: createRequiredRule('Category', 'change'),
  type: createRequiredRule('Type', 'change'),
  position: createRequiredRule('Position', 'change'),
  display_scope: createRequiredRule('Display Scope', 'change'),
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
      window.$message.success('Updated successfully')
    } else {
      await bannerApi.create(payload)
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
          <NFormItem :label="'Title'" path="title">
            <NInput v-model:value="state.formModel.title" />
          </NFormItem>
          <NFormItem :label="'Image'" path="image">
            <ImageUpload v-model:value="state.formModel.image" />
          </NFormItem>
          <NFormItem :label="'Target URL'" path="url">
            <NInput v-model:value="state.formModel.url" />
          </NFormItem>
          <NFormItem :label="'Link Type'" path="link_type">
            <DictSelect
              v-model="state.formModel.link_type"
              dict-code="BANNER_LINK_TYPE"
              type="radio"
            />
          </NFormItem>
          <NFormItem :label="'Category'" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="BANNER_CATEGORY" />
          </NFormItem>
          <NFormItem :label="'Type'" path="type">
            <DictSelect v-model="state.formModel.type" dict-code="BANNER_TYPE" />
          </NFormItem>
          <NFormItem :label="'Position'" path="position">
            <DictSelect v-model="state.formModel.position" dict-code="BANNER_POSITION" />
          </NFormItem>
          <NFormItem :label="'Display Scope'" path="display_scope">
            <DictSelect v-model="state.formModel.display_scope" dict-code="BANNER_DISPLAY_SCOPE" />
          </NFormItem>
          <NFormItem :label="'Sort'" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="'Status'" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="'Start At'" path="start_at">
            <NInput v-model:value="state.formModel.start_at" />
          </NFormItem>
          <NFormItem :label="'End At'" path="end_at">
            <NInput v-model:value="state.formModel.end_at" />
          </NFormItem>
          <NFormItem :label="'Summary'" path="summary">
            <NInput v-model:value="state.formModel.summary" />
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
