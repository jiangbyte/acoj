<script setup lang="ts">
import { bannerApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { computed, reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  banner: {} as any,
})

const imageAlt = computed(() => state.banner?.title ?? t('resource.sys.banner.image'))

async function openModal(id: string) {
  state.banner = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await bannerApi.detail({ id })
    state.banner = response.data
  } finally {
    state.loading = false
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
    :title="t('resource.sys.banner.detail_banner')"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('resource.sys.banner.id')">
            {{ displayValue(state.banner.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.title_field')">
            {{ displayValue(state.banner.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.image')">
            <NImage
              v-if="state.banner.image"
              class="banner-detail-image"
              :src="state.banner.image"
              :alt="imageAlt"
              :width="180"
              :height="72"
              object-fit="cover"
            />
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.url')">
            {{ displayValue(state.banner.url) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.link_type')">
            {{ dictTypeData('BANNER_LINK_TYPE', state.banner.link_type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.category')">
            {{ dictTypeData('BANNER_CATEGORY', state.banner.category) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.type')">
            {{ dictTypeData('BANNER_TYPE', state.banner.type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.position')">
            {{ dictTypeData('BANNER_POSITION', state.banner.position) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.display_scope')">
            <NTag
              :color="
                createTagColor(dictTypeColor('BANNER_DISPLAY_SCOPE', state.banner.display_scope))
              "
              :bordered="false"
            >
              {{ dictTypeData('BANNER_DISPLAY_SCOPE', state.banner.display_scope) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.sort')">
            {{ displayValue(state.banner.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.interaction_count')">
            {{ displayValue(state.banner.interaction_count) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.banner.status))"
              :bordered="false"
            >
              {{ dictTypeData('COMMON_STATUS', state.banner.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.start_at')">
            {{ displayValue(state.banner.start_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.end_at')">
            {{ displayValue(state.banner.end_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.summary')">
            {{ displayValue(state.banner.summary) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.sys.banner.description')">
            {{ displayValue(state.banner.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_at')">
            {{ displayValue(state.banner.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_by')">
            {{ displayValue(state.banner.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.banner.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_by')">
            {{ displayValue(state.banner.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>

<style scoped>
.banner-detail-image {
  border-radius: 6px;
}
</style>
