<!--
  由 HEI 代码生成器生成。
  Author: Charlie
  生成时间：2026-08-15 14:38:50
-->

<script setup lang="ts">
import { cgTestOrderApi } from '@/api'
import { createTagColor, dictTypeColor, dictTypeData, displayValue, formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  detail: {} as any,
})

async function openModal(id: string) {
  state.detail = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await cgTestOrderApi.childDetail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

function formatJsonValue(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return '{}'
  }
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

defineExpose({
  openModal,
})
</script>

<template>
  <HeiDetailContainer
    v-model:show="state.showModal"
    title="订单明细详情"
    :width="680"
    :mask-closable="false"
  >
      <NSpin :show="state.loading">
        <NDescriptions
          label-placement="left"
          bordered
          :column="1"
        >
          <NDescriptionsItem label="id">
            {{ displayValue(state.detail.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="order_id">
            {{ displayValue(state.detail.order_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="sku_code">
            {{ displayValue(state.detail.sku_code) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="name">
            {{ displayValue(state.detail.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="category">
            {{ displayValue(state.detail.category) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="status">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.detail.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.detail.status) ||
                  displayValue(state.detail.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="quantity">
            {{ displayValue(state.detail.quantity) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="unit_price">
            {{ displayValue(state.detail.unit_price) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="shipped_at">
            {{ formatDateTime(state.detail.shipped_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="is_gift">
            {{ displayValue(state.detail.is_gift) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="item_config">
            <NCode
              :code="formatJsonValue(state.detail.item_config)"
              language="json"
              word-wrap
            />
          </NDescriptionsItem>
          <NDescriptionsItem label="remark">
            {{ displayValue(state.detail.remark) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="extra">
            <NCode
              :code="formatJsonValue(state.detail.extra)"
              language="json"
              word-wrap
            />
          </NDescriptionsItem>
          <NDescriptionsItem label="创建时间">
            {{
              formatDateTime(state.detail.created_at)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem label="更新时间">
            {{
              formatDateTime(state.detail.updated_at)
            }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
  </HeiDetailContainer>
</template>
