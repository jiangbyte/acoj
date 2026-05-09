<template>
  <div>
    <a-card class="mb-4" v-if="$slots.search">
      <a-form layout="inline" :model="searchForm" @finish="$emit('search', searchForm)">
        <slot name="search" />
        <a-form-item>
          <a-space>
            <a-button html-type="submit" type="primary">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    <a-card>
      <div class="mb-4">
        <a-space>
          <slot name="toolbar" />
        </a-space>
      </div>
      <a-table
        :dataSource="dataSource"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        rowKey="id"
        @change="handleTableChange"
      >
        <template v-for="slot in Object.keys($slots)" :key="slot" #[slot]="args">
          <slot :name="slot" v-bind="args" />
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const props = defineProps<{
  columns: any[]
  fetchData: (params: any) => Promise<any>
  searchForm?: any
}>()

const emit = defineEmits<{
  search: [form: any]
}>()

const dataSource = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const queryParams = ref<any>({})

async function loadData() {
  loading.value = true
  try {
    const { isSuccess, data } = await props.fetchData({
      current: pagination.current,
      size: pagination.pageSize,
      ...queryParams.value,
    })
    if (isSuccess && data) {
      dataSource.value = data.records || []
      pagination.total = data.total || 0
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

function resetSearch() {
  if (props.searchForm) {
    Object.keys(props.searchForm).forEach(k => { props.searchForm[k] = undefined })
  }
  queryParams.value = {}
  pagination.current = 1
  loadData()
}

function refresh() {
  pagination.current = 1
  loadData()
}

defineExpose({ loadData, refresh })
</script>
