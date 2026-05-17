<template>
  <a-card size="small" class="flex flex-col flex-1 min-h-0 [&_.ant-spin-container]:min-h-none">
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

      <!-- Tree table -->
      <div class="flex-1 min-h-0 overflow-auto">
        <a-table
          :data-source="dataSource"
          :columns="columns"
          :loading="loading"
          :row-key="rowKey || 'id'"
          :pagination="false"
          :row-selection="selectionConfig"
          :default-expand-all-rows="defaultExpandAll"
          :children-column-name="childrenColumnName"
          :scroll="{ x: 'max-content' }"
          size="middle"
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
import { ref, computed, watch, useSlots } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import AppForbidden from '@/components/result/AppForbidden.vue'

const passthroughSlots = Object.keys(useSlots()).filter(s => s !== 'default' && s !== 'dragHandle')

const props = defineProps<{
  columns: any[]
  dataSource: any[]
  loading?: boolean
  rowKey?: string
  defaultExpandAll?: boolean
  childrenColumnName?: string
  rowSelection?: {
    selectedRowKeys?: string[]
    onChange?: (keys: string[]) => void
    type?: 'checkbox' | 'radio'
  }
  perm?: string | string[]
}>()

const auth = useAuthStore()
const hasPerm = computed(() => {
  if (!props.perm) return true
  const codes = Array.isArray(props.perm) ? props.perm : [props.perm]
  return codes.some(code => auth.hasPermission(code as string))
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

function clearSelected() {
  selectedKeys.value = []
  props.rowSelection?.onChange?.([])
}

function getSelectedKeys() {
  return selectedKeys.value
}

const emit = defineEmits<{ refresh: [] }>()

function handleRefresh() {
  emit('refresh')
}

defineExpose({ clearSelected, getSelectedKeys })
</script>
