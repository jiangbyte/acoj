<template>
  <Layout :title="`${config.title}详情`" back>
    <view class="resource-detail">
      <u-card :show-head="false">
        <template #body>
          <view
            v-for="field in config.detailFields"
            :key="field.prop"
            class="resource-detail__row"
          >
            <text class="resource-detail__label">{{ field.label }}</text>
            <text class="resource-detail__value">{{ valueText(field.prop) }}</text>
          </view>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import {
  adminResourceApis,
  resourceConfigs,
  type ResourceKey,
} from '@/config/resource'
import { dictTypeData } from '@/utils/dict'
import { displayValue, formatDateTime } from '@/utils/format'

const props = defineProps<{
  resourceKey: ResourceKey
}>()

const id = ref('')
const detailParams = ref<Record<string, any>>({})
const detail = ref<Record<string, any>>({})
const config = computed(() => resourceConfigs[props.resourceKey])
const api = computed<any>(() => adminResourceApis[props.resourceKey])

onLoad(async (query: any) => {
  id.value = query.id || ''
  detailParams.value = buildDetailParams(query)
  await loadDetail()
})

async function loadDetail() {
  const data = await api.value.detail(detailParams.value)
  detail.value = Array.isArray(data?.records) ? (data.records[0] ?? {}) : data
}

function buildDetailParams(query: Record<string, any>) {
  const params: Record<string, any> = { id: query.id || id.value }
  const contextKeys = [
    'account_type',
    'account_id',
    'target_account_type',
    'target_account_id',
  ]
  contextKeys.forEach((key) => {
    if (query[key] !== undefined && query[key] !== null && query[key] !== '') {
      params[key] = query[key]
    }
  })
  return params
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

<style lang="scss" scoped>
.resource-detail {
  padding-top: var(--space-3);
}

.resource-detail__row {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-neutral-200);
}

.resource-detail__label {
  font-size: var(--text-sm);
  color: var(--color-neutral-500);
}

.resource-detail__value {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--color-neutral-900);
  text-align: right;
}
</style>
