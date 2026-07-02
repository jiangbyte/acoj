<script setup lang="ts">
import { fileApi } from '@/api'
import { displayValue, resolveFileUrl } from '@/utils'
import { computed, reactive } from 'vue'
import { dictTypeData } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  file: {} as any,
})

const fileUrl = computed(() => resolveFileUrl(state.file?.url))
const imageAlt = computed(() => state.file?.original_name ?? t('resource.sys.file.preview'))
const isImage = computed(() => String(state.file?.content_type || '').startsWith('image/'))

async function openModal(id: string) {
  state.file = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await fileApi.detail({ id })
    state.file = response.data
  } finally {
    state.loading = false
  }
}

function formatFileSize(size?: number | string | null) {
  const value = Number(size ?? 0)
  if (!Number.isFinite(value) || value <= 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let current = value
  let unitIndex = 0
  while (current >= 1024 && unitIndex < units.length - 1) {
    current /= 1024
    unitIndex += 1
  }
  return `${current.toFixed(unitIndex === 0 ? 0 : 2)} ${units[unitIndex]}`
}

function openFile() {
  if (!fileUrl.value) {
    return
  }
  window.open(fileUrl.value, '_blank', 'noopener,noreferrer')
}

async function copyText(value?: string | null) {
  const text = String(value || '')
  if (!text) {
    return
  }
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text)
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
  window.$message.success(t('resource.sys.file.copy_success'))
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
    :title="t('resource.sys.file.detail_file')"
    style="width: 700px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('resource.sys.file.preview')">
            <NImage
              v-if="isImage && fileUrl"
              class="file-detail-image"
              :src="fileUrl"
              :alt="imageAlt"
              :width="220"
              :height="140"
              object-fit="cover"
            />
            <NButton v-else-if="fileUrl" type="primary" text @click="openFile">
              {{ t('resource.sys.file.open') }}
            </NButton>
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.id')">
            {{ displayValue(state.file.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.original_name')">
            {{ displayValue(state.file.original_name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.object_name')">
            <NFlex align="center" :size="8">
              <NEllipsis class="file-detail-text">
                {{ displayValue(state.file.object_name) }}
              </NEllipsis>
              <NButton size="small" text type="primary" @click="copyText(state.file.object_name)">
                {{ t('resource.sys.file.copy') }}
              </NButton>
            </NFlex>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.url')">
            <NFlex align="center" :size="8">
              <NEllipsis class="file-detail-text">
                {{ displayValue(fileUrl) }}
              </NEllipsis>
              <NButton v-if="fileUrl" size="small" text type="primary" @click="openFile">
                {{ t('resource.sys.file.open') }}
              </NButton>
              <NButton v-if="fileUrl" size="small" text type="primary" @click="copyText(fileUrl)">
                {{ t('resource.sys.file.copy') }}
              </NButton>
            </NFlex>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.storage_provider')">
            {{
              dictTypeData('STORAGE_PROVIDER', state.file.storage_provider) ||
              displayValue(state.file.storage_provider)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.bucket')">
            {{ displayValue(state.file.bucket) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.content_type')">
            {{ displayValue(state.file.content_type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.file.size')">
            {{ formatFileSize(state.file.size) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_at')">
            {{ displayValue(state.file.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_by')">
            {{ displayValue(state.file.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.file.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_by')">
            {{ displayValue(state.file.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>

<style scoped>
.file-detail-image {
  border-radius: 6px;
}

.file-detail-text {
  max-width: 460px;
}
</style>
