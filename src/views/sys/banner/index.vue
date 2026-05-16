<template>
  <div class="flex flex-col gap-2">
    <!-- Search panel -->
    <AppSearchPanel
      :model="searchForm"
      perm="sys:banner:page"
      @search="handleSearch"
      @reset="resetSearch"
    >
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="关键词" name="keyword">
          <a-input v-model:value="searchForm.keyword" placeholder="标题" allow-clear />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="类别" name="category">
          <DictSelect
            v-model="searchForm.category"
            type-code="BANNER_CATEGORY"
            placeholder="全部"
            allow-clear
          />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="类型" name="type">
          <DictSelect
            v-model="searchForm.type"
            type-code="BANNER_TYPE"
            placeholder="全部"
            allow-clear
          />
        </a-form-item>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="6">
        <a-form-item label="位置" name="position">
          <DictSelect
            v-model="searchForm.position"
            type-code="BANNER_POSITION"
            placeholder="全部"
            allow-clear
          />
        </a-form-item>
      </a-col>
    </AppSearchPanel>

    <AppTable
      ref="tableRef"
      perm="sys:banner:page"
      :columns="columns"
      :fetch-data="fetchBannerPage"
      :search-form="searchForm"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button v-if="hasPermission('sys:banner:create')" type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增轮播图
        </a-button>
        <a-button
          v-if="hasPermission('sys:banner:remove')"
          danger
          :disabled="selectedKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'category'">
          {{ $dict.label('BANNER_CATEGORY', record.category) || '-' }}
        </template>
        <template v-else-if="column.key === 'type'">
          {{ $dict.label('BANNER_TYPE', record.type) || '-' }}
        </template>
        <template v-else-if="column.key === 'position'">
          {{ $dict.label('BANNER_POSITION', record.position) || '-' }}
        </template>
        <template v-else-if="column.key === 'image'">
          <img v-if="record.image" :src="record.image" class="w-16 h-10 object-cover rounded" />
          <span v-else class="text-gray-400">无</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="hasPermission('sys:banner:modify')"
              type="link"
              size="small"
              @click="openEdit(record)"
            >
              编辑
            </a-button>
            <a-popconfirm
              v-if="hasPermission('sys:banner:remove')"
              title="确定删除？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </AppTable>

    <!-- Drawers -->
    <DetailDrawer ref="detailRef" v-model:open="detailOpen" />
    <FormDrawer ref="formRef" v-model:open="formOpen" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysBanner' })
import { ref, reactive } from 'vue'
import {
  PlusOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import {
  fetchBannerPage,
  fetchBannerRemove,
} from '@/api/banner'
import { useCrud } from '@/hooks/useCrud'
import AppTable from '@/components/table/AppTable.vue'
import AppSearchPanel from '@/components/form/AppSearchPanel.vue'
import DetailDrawer from './components/detail.vue'
import FormDrawer from './components/form.vue'
import DictSelect from '@/components/form/DictSelect.vue'
import { useAuthStore } from '@/store'

const auth = useAuthStore()
const hasPermission = auth.hasPermission

const crud = useCrud({ name: '轮播图', deleteApi: fetchBannerRemove })
const { tableRef, selectedKeys, rowSelection, handleSearch, handleDelete, handleBatchDelete, handleFormSuccess } = crud

const searchForm = reactive({
  keyword: '',
  category: undefined,
  type: undefined,
  position: undefined,
})
const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 200, ellipsis: true },
  { title: '图片', dataIndex: 'image', key: 'image', width: 100 },
  { title: '类别', dataIndex: 'category', key: 'category', width: 100 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '位置', dataIndex: 'position', key: 'position', width: 100 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

function resetSearch() {
  searchForm.keyword = ''
  searchForm.category = undefined
  searchForm.type = undefined
  searchForm.position = undefined
  tableRef.value?.refresh(true)
}

// Drawer refs
const detailRef = ref()
const formRef = ref()
const detailOpen = ref(false)
const formOpen = ref(false)

function openDetail(record: any) {
  detailRef.value?.doOpen(record)
}
function openEdit(record: any) {
  formRef.value?.doOpen(record)
}
function openCreate() {
  formRef.value?.doOpen()
}

</script>
