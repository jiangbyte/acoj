<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { bannerApi } from '@/api'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  categoryOptions,
  createEmptyBannerForm,
  displayScopeOptions,
  linkTypeOptions,
  positionOptions,
  statusOptions,
  typeOptions,
} from '../constants'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const showModal = ref(false)
const loading = ref(false)
const submitLoading = ref(false)
const bannerId = ref<string | null>(null)
const formModel = ref<any>(createEmptyBannerForm())

const isEdit = computed(() => Boolean(bannerId.value))
const modalTitle = computed(() =>
  isEdit.value ? t('pages.sys.banner.editBanner') : t('pages.sys.banner.addBanner'),
)
const categorySelectOptions = computed(() => translateOptions(categoryOptions))
const displayScopeSelectOptions = computed(() => translateOptions(displayScopeOptions))
const linkTypeSelectOptions = computed(() => translateOptions(linkTypeOptions))
const positionSelectOptions = computed(() => translateOptions(positionOptions))
const statusSelectOptions = computed(() => translateOptions(statusOptions))
const typeSelectOptions = computed(() => translateOptions(typeOptions))

const rules = computed<FormRules>(() => ({
  title: createRequiredRule(t('pages.sys.banner.titleField'), 'input'),
  image: createRequiredRule(t('pages.sys.banner.image'), 'input'),
  link_type: createRequiredRule(t('pages.sys.banner.linkType'), 'change'),
  category: createRequiredRule(t('pages.sys.banner.category'), 'change'),
  type: createRequiredRule(t('pages.sys.banner.type'), 'change'),
  position: createRequiredRule(t('pages.sys.banner.position'), 'change'),
  display_scope: createRequiredRule(t('pages.sys.banner.displayScope'), 'change'),
  status: createRequiredRule(t('common.often.status'), 'change'),
}))

async function openModal(id?: string) {
  bannerId.value = id ?? null
  formModel.value = createEmptyBannerForm()
  showModal.value = true

  if (id) {
    await fetchBannerDetail(id)
  }
}

async function fetchBannerDetail(id: string) {
  loading.value = true
  try {
    const response = await bannerApi.detail({ id })
    formModel.value = toFormModel(response.data)
  } finally {
    loading.value = false
  }
}

function closeModal() {
  showModal.value = false
  submitLoading.value = false
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = normalizePayload(formModel.value)

    if (bannerId.value) {
      await bannerApi.update({
        ...payload,
        id: bannerId.value,
      })
      window.$message.success(t('common.often.updateSuccess'))
    } else {
      await bannerApi.create(payload)
      window.$message.success(t('common.often.createSuccess'))
    }

    closeModal()
    emit('saved')
  } finally {
    submitLoading.value = false
  }
}

function toFormModel(data: any) {
  return {
    id: data.id,
    title: data.title ?? '',
    image: data.image ?? '',
    url: data.url ?? '',
    link_type: data.link_type ?? 'URL',
    summary: data.summary ?? '',
    description: data.description ?? '',
    category: data.category ?? 'HOME',
    type: data.type ?? 'CAROUSEL',
    position: data.position ?? 'HOME_TOP',
    display_scope: data.display_scope ?? 'PORTAL',
    sort: data.sort ?? 0,
    status: data.status ?? 'ENABLED',
    start_at: data.start_at ?? '',
    end_at: data.end_at ?? '',
  }
}

function normalizePayload(values: any) {
  return {
    title: values.title,
    image: values.image,
    url: toNullableString(values.url),
    link_type: values.link_type,
    summary: toNullableString(values.summary),
    description: toNullableString(values.description),
    category: values.category,
    type: values.type,
    position: values.position,
    display_scope: values.display_scope,
    sort: Number(values.sort ?? 0),
    status: values.status,
    start_at: toNullableString(values.start_at),
    end_at: toNullableString(values.end_at),
  }
}

function toNullableString(value: unknown) {
  if (value === undefined || value === null) {
    return null
  }
  const text = String(value).trim()
  return text ? text : null
}

function translateOptions(options: Array<{ labelKey: string; value: string }>) {
  return options.map((item) => ({
    label: t(item.labelKey),
    value: item.value,
  }))
}

function createRequiredRule(field: string, trigger: 'input' | 'change') {
  return {
    required: true,
    message: t('pages.sys.banner.required', { field }),
    trigger,
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="modalTitle"
    style="width: 720px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="loading">
      <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
        <NForm
          ref="formRef"
          :model="formModel"
          :rules="rules"
          label-placement="left"
          label-width="100"
          :disabled="loading || submitLoading"
        >
          <NFormItem :label="t('pages.sys.banner.titleField')" path="title">
            <NInput v-model:value="formModel.title" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.image')" path="image">
            <NInput v-model:value="formModel.image" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.url')" path="url">
            <NInput v-model:value="formModel.url" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.linkType')" path="link_type">
            <NRadioGroup v-model:value="formModel.link_type" :options="linkTypeSelectOptions" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.category')" path="category">
            <NSelect v-model:value="formModel.category" :options="categorySelectOptions" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.type')" path="type">
            <NSelect v-model:value="formModel.type" :options="typeSelectOptions" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.position')" path="position">
            <NSelect v-model:value="formModel.position" :options="positionSelectOptions" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.displayScope')" path="display_scope">
            <NSelect
              v-model:value="formModel.display_scope"
              :options="displayScopeSelectOptions"
            />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.sort')" path="sort">
            <NInput v-model:value="formModel.sort" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <NRadioGroup v-model:value="formModel.status" :options="statusSelectOptions" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.startAt')" path="start_at">
            <NInput v-model:value="formModel.start_at" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.endAt')" path="end_at">
            <NInput v-model:value="formModel.end_at" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.summary')" path="summary">
            <NInput v-model:value="formModel.summary" />
          </NFormItem>
          <NFormItem :label="t('pages.sys.banner.description')" path="description">
            <NInput
              v-model:value="formModel.description"
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
        <NButton type="primary" :loading="submitLoading" @click="submitForm">
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
