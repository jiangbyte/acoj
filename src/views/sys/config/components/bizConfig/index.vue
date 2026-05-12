<template>
  <div class="flex flex-col gap-2">
    <AppSearchPanel :model="searchForm" @search="handleSearch" @reset="resetSearch">
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="配置键/备注" allow-clear />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      :columns="columns"
      :fetch-data="fetchConfigPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:config:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增配置
        </a-button>
        <a-button
          v-if="hasPermission('sys:config:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button v-if="hasPermission('sys:config:modify')" type="link" size="small" @click="openEdit(record)">
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:config:remove')"
              title="确定删除该配置？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTable>

    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { fetchConfigPage, fetchConfigRemove } from '@/api/config'
import { confirmDelete } from '@/utils'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import DetailDrawer from './detail.vue'
import FormDrawer from './form.vue'

const auth = useAuthStore()
const hasPermission = auth.hasPermission
const tableRef = ref()

const searchForm = reactive({ keyword: '', category: 'BIZ_DEFINE' })
const selectedKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: string[]) => { selectedKeys.value = keys },
}))

const columns = [
  { title: '配置键', dataIndex: 'config_key', key: 'config_key', width: 250, ellipsis: true },
  { title: '配置值', dataIndex: 'config_value', key: 'config_value', width: 300, ellipsis: true },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 200, ellipsis: true },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) { detailRef.value?.doOpen(record) }
function openEdit(record: any) { formRef.value?.doOpen(record) }
function openCreate() { formRef.value?.doOpen() }

async function handleDelete(id: string) {
  const { success } = await fetchConfigRemove({ ids: [id] })
  if (success) {
    message.success('删除成功')
    tableRef.value?.refresh()
  }
}

function handleBatchDelete() {
  confirmDelete({
    name: '配置',
    selectedKeys: selectedKeys.value,
    deleteApi: fetchConfigRemove,
    onSuccess: () => {
      selectedKeys.value = []
      tableRef.value?.refresh()
    },
  })
}

function handleFormSuccess() { tableRef.value?.refresh() }
function handleSearch() { tableRef.value?.refresh(true) }
function resetSearch() {
  searchForm.keyword = ''
  searchForm.category = 'BIZ_DEFINE'
  tableRef.value?.refresh(true)
}
</script>
