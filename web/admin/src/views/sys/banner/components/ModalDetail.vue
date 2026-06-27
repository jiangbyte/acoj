<script setup lang="ts">
import { bannerApi } from '@/api'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  categoryLabelKeyMap,
  displayScopeLabelKeyMap,
  displayScopeTagTypeMap,
  linkTypeLabelKeyMap,
  positionLabelKeyMap,
  statusLabelKeyMap,
  statusTagTypeMap,
  typeLabelKeyMap,
} from '../constants'

const { t } = useI18n()
const showModal = ref(false)
const loading = ref(false)
const banner = ref<any>({})

const imageAlt = computed(() => banner.value?.title ?? t('pages.sys.banner.image'))

async function openModal(id: string) {
  banner.value = {}
  showModal.value = true
  await fetchBannerDetail(id)
}

async function fetchBannerDetail(id: string) {
  loading.value = true
  try {
    const response = await bannerApi.detail({ id })
    banner.value = response.data
  } finally {
    loading.value = false
  }
}

function displayValue(value?: string | number | null) {
  return value === undefined || value === null || value === '' ? '-' : String(value)
}

function displayLabel(map: Record<string, string>, value?: string | null) {
  if (!value) {
    return '-'
  }
  const labelKey = map[value]
  return labelKey ? t(labelKey) : value
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
    :title="t('pages.sys.banner.detailBanner')"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('pages.sys.banner.id')">
            {{ displayValue(banner.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.titleField')">
            {{ displayValue(banner.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.image')">
            <div class="banner-detail-image-box">
              <img
                class="banner-detail-image"
                :class="{ invisible: !banner.image }"
                :src="banner.image || undefined"
                :alt="imageAlt"
              />
            </div>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.url')">
            {{ displayValue(banner.url) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.linkType')">
            {{ displayLabel(linkTypeLabelKeyMap, banner.link_type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.category')">
            {{ displayLabel(categoryLabelKeyMap, banner.category) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.type')">
            {{ displayLabel(typeLabelKeyMap, banner.type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.position')">
            {{ displayLabel(positionLabelKeyMap, banner.position) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.displayScope')">
            <NTag
              :type="displayScopeTagTypeMap[banner.display_scope] ?? 'default'"
              :bordered="false"
            >
              {{ displayLabel(displayScopeLabelKeyMap, banner.display_scope) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.sort')">
            {{ displayValue(banner.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.interactionCount')">
            {{ displayValue(banner.interaction_count) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            <NTag :type="statusTagTypeMap[banner.status] ?? 'default'" :bordered="false">
              {{ displayLabel(statusLabelKeyMap, banner.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.startAt')">
            {{ displayValue(banner.start_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.endAt')">
            {{ displayValue(banner.end_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.summary')">
            {{ displayValue(banner.summary) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.description')">
            {{ displayValue(banner.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.banner.createdAt')">
            {{ displayValue(banner.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updateTime')">
            {{ displayValue(banner.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>

<style scoped>
.banner-detail-image-box {
  width: 180px;
  height: 72px;
}

.banner-detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.invisible {
  visibility: hidden;
}
</style>
