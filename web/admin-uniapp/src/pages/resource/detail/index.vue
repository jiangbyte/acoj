<template>
  <Layout :title="config.title" back>
    <view>
      <u-card v-if="resource !== 'messageThread'" :show-head="false">
        <template #body>
          <u-cell-group :border="false">
            <u-cell-item
              v-for="field in config.detailFields"
              :key="field.prop"
              :title="field.label"
              :value="valueText(field.prop)"
              :arrow="false"
            ></u-cell-item>
          </u-cell-group>
        </template>
      </u-card>

      <template v-else>
        <u-card
          v-for="item in messages"
          :key="item.id"
          :title="
            item.sender_name || item.sender_account_id || item.sender_type
          "
          :sub-title="formatDateTime(item.created_at)"
        >
          <template #body>
            <u-cell-item :title="item.content" :arrow="false"></u-cell-item>
          </template>
        </u-card>
        <u-empty v-if="!messages.length" mode="list" text="暂无消息"></u-empty>
      </template>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { messageApi } from '@/api'
import {
  adminResourceApis,
  resourceConfigs,
  type ResourceKey,
} from '@/config/resource'
import { dictTypeData } from '@/utils/dict'
import { displayValue, formatDateTime } from '@/utils/format'

const resource = ref<ResourceKey>('account')
const id = ref('')
const mine = ref(false)
const detail = ref<Record<string, any>>({})
const messages = ref<any[]>([])
const config = computed(() => resourceConfigs[resource.value])
const api = computed<any>(() => adminResourceApis[resource.value])

onLoad(async (query: any) => {
  resource.value = query.resource || 'account'
  id.value = query.id || ''
  mine.value = query.mine === '1'
  await loadDetail()
})

async function loadDetail() {
  if (mine.value && resource.value === 'notification') {
    detail.value = await messageApi.myNotificationDetail({ id: id.value })
  } else if (mine.value && resource.value === 'todo') {
    detail.value = await messageApi.myTodoDetail({ id: id.value })
  } else {
    const data = await api.value.detail({ id: id.value })
    if (resource.value === 'messageThread') {
      messages.value = data.records ?? []
      detail.value = {}
    } else {
      detail.value = Array.isArray(data?.records)
        ? (data.records[0] ?? {})
        : data
    }
  }
}

function valueText(prop: string) {
  const value = detail.value?.[prop]
  if (prop.endsWith('_at')) {
    return formatDateTime(value)
  }
  const field = config.value.detailFields.find((item) => item.prop === prop)
  if (field?.dictCode) {
    return dictTypeData(field.dictCode, value) || displayValue(value)
  }
  return displayValue(value)
}
</script>
