<script setup lang="ts">
import { bannerApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { computed, reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  banner: {} as any,
})

const imageAlt = computed(() => state.banner?.title ?? 'Image')

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
    :title="'Display Image Detail'"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'Display Image ID'">
            {{ displayValue(state.banner.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Title'">
            {{ displayValue(state.banner.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Image'">
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
          <NDescriptionsItem :label="'Target URL'">
            {{ displayValue(state.banner.url) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Link Type'">
            {{ dictTypeData('BANNER_LINK_TYPE', state.banner.link_type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Category'">
            {{ dictTypeData('BANNER_CATEGORY', state.banner.category) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Type'">
            {{ dictTypeData('BANNER_TYPE', state.banner.type) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Position'">
            {{ dictTypeData('BANNER_POSITION', state.banner.position) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Display Scope'">
            <NTag
              :color="
                createTagColor(dictTypeColor('BANNER_DISPLAY_SCOPE', state.banner.display_scope))
              "
              :bordered="false"
            >
              {{ dictTypeData('BANNER_DISPLAY_SCOPE', state.banner.display_scope) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.banner.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Interactions'">
            {{ displayValue(state.banner.interaction_count) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.banner.status))"
              :bordered="false"
            >
              {{ dictTypeData('COMMON_STATUS', state.banner.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Start At'">
            {{ displayValue(state.banner.start_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'End At'">
            {{ displayValue(state.banner.end_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Summary'">
            {{ displayValue(state.banner.summary) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Description'">
            {{ displayValue(state.banner.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.banner.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.banner.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.banner.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
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
