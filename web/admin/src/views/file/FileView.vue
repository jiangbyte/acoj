<script setup lang="ts">
import { CloudUploadOutlined, DownOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import { computed, onMounted, reactive, ref } from 'vue'

import type { FileItem } from '@/types/api'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listFiles } from '@/apis/file'
import { formatBytes, formatDateTime } from '@hei/shared'

const query = reactive<{
  keyword: string
  content_type?: string
  storage_provider?: string
  uploader: string
  page: number
  page_size: number
}>({
  keyword: '',
  content_type: undefined,
  storage_provider: undefined,
  uploader: '',
  page: 1,
  page_size: 10,
})
const data = ref<FileItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<Key[]>([])

const columns: TableColumnsType<FileItem> = [
  { title: '#', key: 'serial', width: 70 },
  { title: '文件名', dataIndex: 'filename', key: 'filename' },
  { title: '类型', dataIndex: 'content_type', key: 'content_type', width: 160 },
  { title: '大小', dataIndex: 'size', key: 'size', width: 120 },
  { title: '存储', dataIndex: 'storage_provider', key: 'storage_provider', width: 120 },
  { title: '上传人', dataIndex: 'uploader', key: 'uploader', width: 120 },
  { title: '上传时间', dataIndex: 'created_at', key: 'created_at', width: 160 },
  { title: '操作', key: 'actions', width: 140 },
]

async function fetchData() {
  const result = await listFiles(query)
  data.value = result.items
  total.value = result.total
}

function resetQuery() {
  query.keyword = ''
  query.content_type = undefined
  query.storage_provider = undefined
  query.uploader = ''
  query.page = 1
  fetchData()
}

onMounted(fetchData)

const rowSelection = computed<TableRowSelection<FileItem>>(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  },
}))
</script>

<template>
  <QueryTable>
    <template #search="{ expanded, toggle }">
      <AForm layout="inline" :model="query">
        <ARow :gutter="[48, 16]" class="w-full">
          <ACol :md="8" :sm="24">
            <AFormItem label="关键词">
              <AInput v-model:value="query.keyword" allow-clear placeholder="文件名、类型" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem label="文件类型">
              <ASelect v-model:value="query.content_type" allow-clear placeholder="请选择">
                <ASelectOption value="text/csv">CSV</ASelectOption>
                <ASelectOption value="image/png">PNG</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="存储">
              <ASelect v-model:value="query.storage_provider" allow-clear placeholder="请选择">
                <ASelectOption value="s3">S3</ASelectOption>
                <ASelectOption value="local">本地</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem label="上传人">
              <AInput v-model:value="query.uploader" allow-clear placeholder="请输入上传人" />
            </AFormItem>
          </ACol>
          <ACol :md="expanded ? 24 : 8" :sm="24">
            <span class="inline-flex flex-wrap gap-2" :class="{ 'is-expanded': expanded }">
              <AButton type="link" @click="toggle">
                {{ expanded ? '收起' : '展开' }}
                <UpOutlined v-if="expanded" />
                <DownOutlined v-else />
              </AButton>
              <AButton type="primary" @click="fetchData">查询</AButton>
              <AButton class="ml-2" @click="resetQuery">重置</AButton>
            </span>
          </ACol>
        </ARow>
      </AForm>
    </template>

    <template #toolbar>
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">文件列表</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </AButton>
        <AButton type="primary">
          <template #icon><CloudUploadOutlined /></template>
          上传文件
        </AButton>
        <ADropdown v-if="selectedRowKeys.length > 0">
          <AButton>批量操作 <DownOutlined /></AButton>
          <template #overlay>
            <AMenu>
              <AMenuItem key="download">批量下载</AMenuItem>
              <AMenuItem key="delete">批量删除</AMenuItem>
            </AMenu>
          </template>
        </ADropdown>
      </ASpace>
    </template>

    <template #alert>
      <AAlert show-icon type="info">
        <template #message>
          已选择 <a>{{ selectedRowKeys.length }}</a> 项
          <a class="ml-3" @click="selectedRowKeys = []">清空</a>
        </template>
      </AAlert>
    </template>

    <ATable
      :columns="columns"
      :data-source="data"
      :pagination="{ current: query.page, pageSize: query.page_size, total }"
      :row-selection="rowSelection"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'serial'">
          {{ data.findIndex((item) => item.id === record.id) + 1 }}
        </template>
        <template v-if="column.key === 'size'">{{ formatBytes(record.size) }}</template>
        <template v-if="column.key === 'storage_provider'">
          <ATag :color="record.storage_provider === 's3' ? 'blue' : 'default'">
            {{ record.storage_provider }}
          </ATag>
        </template>
        <template v-if="column.key === 'created_at'">{{
          formatDateTime(record.created_at)
        }}</template>
        <template v-if="column.key === 'actions'">
          <span class="inline-flex flex-wrap gap-2">
            <AButton size="small" type="link">下载</AButton>
            <AButton size="small" type="link" danger>删除</AButton>
          </span>
        </template>
      </template>
    </ATable>
  </QueryTable>
</template>
