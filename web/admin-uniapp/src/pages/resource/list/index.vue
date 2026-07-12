<template>
  <Layout :title="config.title" back>
    <view>
      <u-card :show-head="false">
        <template #body>
          <view class="toolbar">
            <u-search
              v-model="keyword"
              placeholder="搜索名称或编码"
              :show-action="false"
              @search="refresh"
            ></u-search>
            <u-button
              v-if="
                config.createPermission &&
                authStore.hasPermission(config.createPermission)
              "
              text="新增"
              icon="plus"
              type="primary"
              @click="openCreate"
            ></u-button>
          </view>
        </template>
      </u-card>

      <view class="records">
        <RecordCard
          v-for="item in records"
          :key="item.id"
          :icon="config.icon"
          :title="displayField(item, config.primaryField)"
          :description="
            config.descriptionField
              ? displayField(item, config.descriptionField)
              : ''
          "
          :status="
            config.statusField ? displayField(item, config.statusField) : ''
          "
          :fields="cardFields(item)"
          :actions="rowActions"
          @detail="openDetail(item)"
          @action="handleAction(item, $event)"
          @more="openMore(item)"
        />
        <u-empty
          v-if="!records.length && !loading"
          mode="list"
          text="暂无数据"
        ></u-empty>
        <u-loadmore :status="loadStatus"></u-loadmore>
      </view>
    </view>

    <FilterPopup
      v-model="filterVisible"
      :fields="config.searchFields"
      :values="filters"
      @apply="applyFilters"
    />
    <u-action-sheet
      :show="actionSheet.show"
      :actions="actionSheet.actions"
      cancel-text="取消"
      @select="selectMoreAction"
      @close="actionSheet.show = false"
    ></u-action-sheet>
  </Layout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import FilterPopup from '@/components/common/FilterPopup.vue'
import RecordCard from '@/components/common/RecordCard.vue'
import { messageApi } from '@/api'
import {
  adminResourceApis,
  resourceConfigs,
  type ResourceKey,
} from '@/config/resource'
import { useAuthStore } from '@/stores/auth'
import { dictTypeData } from '@/utils/dict'
import { displayValue, formatDateTime } from '@/utils/format'

const authStore = useAuthStore()
const resource = ref<ResourceKey>('account')
const config = computed(() => resourceConfigs[resource.value])
const api = computed<any>(() => adminResourceApis[resource.value])
const records = ref<any[]>([])
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const keyword = ref('')
const filters = ref<Record<string, any>>({})
const filterVisible = ref(false)
const actionSheet = reactive<{
  show: boolean
  row: any | null
  actions: any[]
}>({
  show: false,
  row: null,
  actions: [],
})

const rowActions = computed(() =>
  config.value.actions.filter((action) =>
    authStore.hasPermission(action.permission)
  )
)
const loadStatus = computed(() =>
  loading.value
    ? 'loading'
    : records.value.length >= total.value
      ? 'nomore'
      : 'loadmore'
)

onLoad((query: any) => {
  resource.value = query.resource || 'account'
  refresh()
})

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
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
    const page =
      config.value.tree && api.value.tree
        ? await api.value.page(params)
        : await api.value.page(params)
    records.value = append
      ? [...records.value, ...(page.records ?? [])]
      : (page.records ?? [])
    total.value = page.total ?? 0
  } finally {
    loading.value = false
  }
}

function buildParams() {
  const params: Record<string, any> = {
    current: current.value,
    size: 20,
    ...filters.value,
  }
  if (keyword.value) {
    if (config.value.searchFields.some((item) => item.prop === 'name')) {
      params.name = keyword.value
    } else if (
      config.value.searchFields.some((item) => item.prop === 'title')
    ) {
      params.title = keyword.value
    } else if (
      config.value.searchFields.some((item) => item.prop === 'account')
    ) {
      params.account = keyword.value
    } else {
      params.code = keyword.value
    }
  }
  return params
}

function cardFields(item: any) {
  return config.value.cardFields.map((field) => ({
    label: field.replace(/_/g, ' '),
    value: normalizeDisplay(field, item[field], fieldConfig(field)),
  }))
}

function displayField(item: any, field: string) {
  return normalizeDisplay(field, item[field], fieldConfig(field))
}

function fieldConfig(field: string) {
  return [
    ...config.value.searchFields,
    ...config.value.formFields,
    ...config.value.detailFields,
  ].find((item) => item.prop === field)
}

function normalizeDisplay(
  field: string,
  value: any,
  fieldConfig?: { dictCode?: string }
) {
  if (field.endsWith('_at')) {
    return formatDateTime(value)
  }
  if (field === 'last_message') {
    return displayValue(value?.content)
  }
  if (fieldConfig?.dictCode) {
    return dictTypeData(fieldConfig.dictCode, value) || displayValue(value)
  }
  return displayValue(value)
}

function applyFilters(value: Record<string, any>) {
  filters.value = value
  refresh()
}

function openCreate() {
  uni.navigateTo({
    url: `/pages/resource/form/index?resource=${resource.value}&mode=create`,
  })
}

function openDetail(item: any) {
  uni.navigateTo({
    url: `/pages/resource/detail/index?resource=${resource.value}&id=${item.id}`,
  })
}

function handleAction(item: any, action: any) {
  if (action.type === 'detail') {
    openDetail(item)
  } else if (action.type === 'edit') {
    uni.navigateTo({
      url: `/pages/resource/form/index?resource=${resource.value}&mode=update&id=${item.id}`,
    })
  } else if (action.type === 'delete') {
    confirmDelete(item)
  } else if (action.type === 'grant') {
    uni.navigateTo({
      url: `/pages/resource/grant/index?resource=${resource.value}&id=${item.id}&grant=${action.grant.key}`,
    })
  } else if (action.type === 'buttons') {
    uni.navigateTo({
      url: `/pages/resource/buttons/index?resourceId=${item.id}&name=${encodeURIComponent(item.name)}`,
    })
  } else {
    runStateAction(item, action)
  }
}

function openMore(item: any) {
  actionSheet.row = item
  actionSheet.actions = rowActions.value.map((action) => ({
    name: action.label,
    action,
  }))
  actionSheet.show = true
}

function selectMoreAction(event: any) {
  actionSheet.show = false
  if (actionSheet.row && event.action) {
    handleAction(actionSheet.row, event.action)
  }
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

async function runStateAction(item: any, action: any) {
  if (action.type === 'publish') {
    await messageApi.publishNotification({ id: item.id })
  } else if (action.type === 'revoke') {
    await messageApi.revokeNotification({ id: item.id })
  } else if (action.type === 'start') {
    await messageApi.startTodo({ todo_id: item.id })
  } else if (action.type === 'complete') {
    await messageApi.completeTodo({ todo_id: item.id })
  } else if (action.type === 'cancel') {
    await messageApi.cancelTodoAdmin({ id: item.id })
  }
  await refresh()
}
</script>

<style lang="scss" scoped>
.toolbar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
}

.records {
  display: flex;
  flex-direction: column;
}
</style>
