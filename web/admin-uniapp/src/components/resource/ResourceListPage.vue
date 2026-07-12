<template>
  <Layout :title="config.title" back>
    <view class="resource-list">
      <view class="resource-list__toolbar">
        <u-search
          v-model="keyword"
          :placeholder="`搜索${config.title}`"
          :show-action="false"
          @search="refresh"
        />
        <u-button
          v-if="canCreate"
          text="新增"
          type="primary"
          icon="plus"
          @click="openCreate"
        />
      </view>

      <view
        v-for="item in records"
        :key="item.id"
        class="resource-card"
        @click="openAction(item)"
      >
        <view class="resource-card__head">
          <view class="resource-card__title-block">
            <text class="resource-card__title">
              {{ displayField(item, config.primaryField) }}
            </text>
            <text class="resource-card__subtitle">
              {{ subtitle(item) }}
            </text>
          </view>
          <u-icon name="more-dot-fill" size="20" color="#9ca3af" />
        </view>

        <view class="resource-card__meta">
          <text v-for="field in cardFields(item)" :key="field.label">
            {{ field.label }}: {{ field.value }}
          </text>
        </view>
      </view>

      <u-empty v-if="!records.length && !loading" mode="list" text="暂无数据" />
      <u-loadmore :status="loadStatus" />
    </view>

    <u-action-sheet
      v-model="actionSheet.show"
      :list="actionSheet.actions"
      cancel-text="取消"
      @click="selectAction"
      @close="actionSheet.show = false"
    />
  </Layout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import {
  adminResourceApis,
  resourceConfigs,
  type ResourceKey,
} from '@/config/resource'
import { useAuthStore } from '@/stores/auth'
import { dictTypeData } from '@/utils/dict'
import { displayValue, formatDateTime } from '@/utils/format'
import { flattenTree } from '@/utils/tree'
import { messageApi } from '@/api'

const props = defineProps<{
  resourceKey: ResourceKey
  basePath: string
}>()

const authStore = useAuthStore()
const config = computed(() => resourceConfigs[props.resourceKey])
const api = computed<any>(() => adminResourceApis[props.resourceKey])
const records = ref<any[]>([])
const keyword = ref('')
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const actionSheet = reactive<{
  show: boolean
  row: any | null
  actions: Array<{ text: string; key: string }>
}>({
  show: false,
  row: null,
  actions: [],
})

const canCreate = computed(
  () =>
    Boolean(config.value.createPermission) &&
    Boolean(api.value.create) &&
    authStore.hasPermission(config.value.createPermission!)
)
const canUpdate = computed(
  () =>
    Boolean(config.value.updatePermission) &&
    Boolean(api.value.update) &&
    authStore.hasPermission(config.value.updatePermission!)
)
const canDelete = computed(
  () =>
    Boolean(config.value.deletePermission) &&
    Boolean(api.value.remove) &&
    authStore.hasPermission(config.value.deletePermission!)
)
const loadStatus = computed(() =>
  loading.value
    ? 'loading'
    : records.value.length >= total.value
      ? 'nomore'
      : 'loadmore'
)

onLoad(() => {
  refresh()
})

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  loadMore()
})

async function refresh() {
  current.value = 1
  await loadPage(false)
}

async function loadMore() {
  if (!loading.value && records.value.length < total.value) {
    current.value += 1
    await loadPage(true)
  }
}

async function loadPage(append: boolean) {
  loading.value = true
  try {
    const params = buildParams()
    const page = config.value.tree && api.value.tree
      ? normalizeTreePage(await api.value.tree(params))
      : await api.value.page(params)
    records.value = append
      ? [...records.value, ...(page.records ?? [])]
      : (page.records ?? [])
    total.value = page.total ?? records.value.length
  } finally {
    loading.value = false
  }
}

function normalizeTreePage(data: any) {
  const rows = Array.isArray(data) ? flattenTree(data) : (data.records ?? [])
  return { records: rows, total: rows.length }
}

function buildParams() {
  const params: Record<string, any> = {
    current: current.value,
    size: 20,
  }
  if (!keyword.value) {
    return params
  }
  if (config.value.searchFields.some((item) => item.prop === 'name')) {
    params.name = keyword.value
  } else if (config.value.searchFields.some((item) => item.prop === 'title')) {
    params.title = keyword.value
  } else if (
    config.value.searchFields.some((item) => item.prop === 'account')
  ) {
    params.account = keyword.value
  } else if (
    config.value.searchFields.some((item) => item.prop === 'original_name')
  ) {
    params.original_name = keyword.value
  } else {
    params.code = keyword.value
  }
  return params
}

function subtitle(item: any) {
  if (!config.value.descriptionField) {
    return item.code || item.description || formatDateTime(item.updated_at)
  }
  return displayField(item, config.value.descriptionField)
}

function cardFields(item: any) {
  return config.value.cardFields.map((field) => ({
    label: fieldConfig(field)?.label || field.replace(/_/g, ' '),
    value: displayField(item, field),
  }))
}

function displayField(item: any, field: string) {
  const value = item[field]
  const fieldMeta = fieldConfig(field)
  if (isDateTimeField(field, fieldMeta)) {
    return formatDateTime(value)
  }
  if (fieldMeta?.dictCode) {
    return dictTypeData(fieldMeta.dictCode, value) || displayValue(value)
  }
  return displayValue(value)
}

function isDateTimeField(field: string, fieldMeta?: { type?: string }) {
  return (
    fieldMeta?.type === 'datetime' ||
    field.endsWith('_at') ||
    field.endsWith('_time') ||
    field === 'expires_at' ||
    field === 'last_active_at' ||
    field === 'latest_active_at'
  )
}

function fieldConfig(field: string) {
  return [
    ...config.value.searchFields,
    ...config.value.formFields,
    ...config.value.detailFields,
  ].find((item) => item.prop === field)
}

function openCreate() {
  uni.navigateTo({ url: `${props.basePath}/form?mode=create` })
}

function openEdit(item: any) {
  uni.navigateTo({
    url: `${props.basePath}/form?${buildQueryString(item, { mode: 'update' })}`,
  })
}

function openDetail(item: any) {
  uni.navigateTo({ url: `${props.basePath}/detail?${buildQueryString(item)}` })
}

function openAction(item: any) {
  const actions: Array<{ text: string; key: string }> = [
    { text: '详情', key: 'detail' },
  ]
  if (canUpdate.value) {
    actions.push({ text: '编辑', key: 'edit' })
  }
  if (canDelete.value) {
    actions.push({ text: '删除', key: 'delete' })
  }
  actionSheet.row = item
  actionSheet.actions = actions
  actionSheet.show = true
}

function selectAction(index: number) {
  const item = actionSheet.row
  const action = actionSheet.actions[index]
  actionSheet.show = false
  if (!item || !action) {
    return
  }
  const key = action.key
  if (key === 'detail') {
    openDetail(item)
  } else if (key === 'edit') {
    openEdit(item)
  } else if (key === 'delete') {
    confirmDelete(item)
  }
}

function buildQueryString(item: any, extra: Record<string, any> = {}) {
  const params: Record<string, any> = {
    ...extra,
    id: item.id,
  }
  const contextKeys = [
    'account_type',
    'account_id',
    'target_account_type',
    'target_account_id',
  ]
  contextKeys.forEach((key) => {
    if (item[key] !== undefined && item[key] !== null && item[key] !== '') {
      params[key] = item[key]
    }
  })
  return Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(
      ([key, value]) =>
        `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`
    )
    .join('&')
}

function confirmDelete(item: any) {
  uni.showModal({
    title: '确认删除',
    content: `删除 ${displayField(item, config.value.primaryField)}？`,
    success: async (res) => {
      if (res.confirm) {
        await api.value.remove({ ids: [item.id] })
        await refresh()
      }
    },
  })
}

async function runStateAction(item: any, actionType: string) {
  if (actionType === 'publish') {
    await messageApi.publishNotification({ id: item.id })
  } else if (actionType === 'revoke') {
    await messageApi.revokeNotification({ id: item.id })
  } else if (actionType === 'start') {
    await messageApi.startTodo({ todo_id: item.id })
  } else if (actionType === 'complete') {
    await messageApi.completeTodo({ todo_id: item.id })
  } else if (actionType === 'cancel') {
    await messageApi.cancelTodoAdmin({ id: item.id })
  }
  await refresh()
}
</script>

<style lang="scss" scoped>
.resource-list {
  display: flex;
  flex-direction: column;
}

.resource-list__toolbar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-2);
  align-items: center;
  margin: var(--space-3) var(--space-4) 0;
  padding: var(--space-2);
  background-color: #ffffff;
  border-radius: var(--radius-md);
}

.resource-card {
  margin: var(--space-3) var(--space-4) 0;
  overflow: hidden;
  background-color: #ffffff;
  border-radius: var(--radius-md);
}

.resource-card__head {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-neutral-200);
}

.resource-card__title-block {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-1);
}

.resource-card__title {
  font-size: var(--text-base);
  font-weight: 600;
  line-height: 1.25;
  color: var(--color-neutral-900);
}

.resource-card__subtitle,
.resource-card__meta {
  font-size: var(--text-xs);
  color: var(--color-neutral-500);
}

.resource-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.resource-card__meta {
  padding: var(--space-2) var(--space-4);
}
</style>
