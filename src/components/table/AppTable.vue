<template>
  <a-card
    size="small"
    :class="['[&_.ant-spin-container]:min-h-none', fixedHeight && 'flex flex-col flex-1 min-h-0']"
  >
    <AppForbidden v-if="!hasPerm" />
    <template v-else>
      <!-- Toolbar -->
      <div class="flex items-center justify-between mb-3 min-h-8 flex-shrink-0">
        <div class="flex items-center gap-2 flex-wrap">
          <slot name="toolbar" />
        </div>
        <div class="flex items-center gap-1 flex-shrink-0">
          <a-tooltip title="刷新">
            <a-button type="text" size="small" @click="handleRefresh">
              <template #icon><ReloadOutlined /></template>
            </a-button>
          </a-tooltip>
        </div>
      </div>

      <!-- Selection alert -->
      <div v-if="selectedKeys.length > 0" class="mb-3 flex-shrink-0">
        <a-alert type="info" show-icon>
          <template #message>
            <span>已选择 {{ selectedKeys.length }} 项</span>
            <a-divider type="vertical" />
            <a @click="clearSelected">清空</a>
          </template>
        </a-alert>
      </div>

      <!-- Table -->
      <div
        ref="tableWrapperRef"
        :class="['min-w-0', fixedHeight && 'flex-1 min-h-0 overflow-hidden']"
      >
        <a-table
          :data-source="dataSource"
          :columns="columns"
          :loading="loading"
          :row-key="rowKey || 'id'"
          :pagination="pagination"
          :row-selection="selectionConfig"
          :scroll="tableScroll"
          size="middle"
          @change="handleTableChange"
        >
          <template v-for="slot in passthroughSlots" :key="slot" #[slot]="args">
            <slot :name="slot" v-bind="args" />
          </template>
        </a-table>
      </div>
    </template>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onUnmounted, useSlots } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import AppForbidden from '@/components/result/AppForbidden.vue'

const passthroughSlots = Object.keys(useSlots()).filter(s => s !== 'default' && s !== 'dragHandle')

const props = defineProps<{
  columns: any[]
  data?: (params: { current: number; size: number; [key: string]: any }) => Promise<any>
  fetchData?: (params: any) => Promise<any>
  searchForm?: Record<string, any>
  rowKey?: string
  rowSelection?: {
    selectedRowKeys?: string[]
    onChange?: (keys: string[]) => void
    type?: 'checkbox' | 'radio'
  }
  fixedHeight?: boolean
  perm?: string | string[]
}>()

const auth = useAuthStore()
const hasPerm = computed(() => {
  if (!props.perm) return true
  const codes = Array.isArray(props.perm) ? props.perm : [props.perm]
  return codes.some(code => auth.hasPermission(code as string))
})

const dataSource = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100'],
})

// ========== Row selection ==========
const selectedKeys = ref<string[]>([])

const selectionConfig = computed(() => {
  if (!props.rowSelection) return undefined
  return {
    type: props.rowSelection.type || 'checkbox',
    selectedRowKeys: selectedKeys.value,
    onChange: (keys: string[]) => {
      selectedKeys.value = keys
      props.rowSelection!.onChange?.(keys)
    },
  }
})

watch(
  () => props.rowSelection?.selectedRowKeys,
  keys => {
    if (keys !== undefined) selectedKeys.value = keys
  }
)

// ========== Dynamic scroll height (fixedHeight mode only) ==========
const tableWrapperRef = ref<HTMLElement>()
const scrollY = ref<number | undefined>(undefined)
let resizeObserver: ResizeObserver | null = null

const tableScroll = computed(() => {
  if (!props.fixedHeight) return { x: 'max-content' }
  return { y: scrollY.value, x: 'max-content' }
})

function updateScrollY() {
  if (!props.fixedHeight) return
  const el = tableWrapperRef.value
  if (!el) return
  scrollY.value = Math.max(200, el.clientHeight - 95)
}

onMounted(() => {
  updateScrollY()
  loadData()
  if (props.fixedHeight && tableWrapperRef.value) {
    resizeObserver = new ResizeObserver(updateScrollY)
    resizeObserver.observe(tableWrapperRef.value)
  }
})

onUnmounted(() => {
  if (resizeObserver) resizeObserver.disconnect()
})

// ========== Data loading ==========
function buildParams(): { current: number; size: number; [key: string]: any } {
  const params: { current: number; size: number; [key: string]: any } = {
    current: pagination.current,
    size: pagination.pageSize,
  }
  if (props.searchForm) {
    for (const key of Object.keys(props.searchForm)) {
      const val = props.searchForm[key]
      if (val !== undefined && val !== null && val !== '') {
        params[key] = val
      }
    }
  }
  return params
}

async function loadData() {
  loading.value = true
  try {
    let result: any
    const params = buildParams()

    if (props.data) {
      result = await props.data(params)
    } else if (props.fetchData) {
      result = await props.fetchData(params)
    }

    if (result) {
      const responseData = result.data || result
      dataSource.value = responseData.records || responseData.rows || []
      pagination.total = responseData.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

function handleRefresh() {
  loadData()
}

function refresh(resetPage = false) {
  if (resetPage) pagination.current = 1
  loadData()
}

function reload() {
  loadData()
}

function clearSelected() {
  selectedKeys.value = []
  props.rowSelection?.onChange?.([])
}

function clearRefreshSelected() {
  refresh(true)
  clearSelected()
}

function getSelectedKeys() {
  return selectedKeys.value
}

defineExpose({ loadData, refresh, reload, clearSelected, clearRefreshSelected, getSelectedKeys })
</script>
