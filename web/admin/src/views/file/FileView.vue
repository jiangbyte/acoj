<script setup lang="ts">
import { CloudUploadOutlined, DownOutlined, ReloadOutlined, UpOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { Key } from 'ant-design-vue/es/_util/type'
import type { TableRowSelection } from 'ant-design-vue/es/table/interface'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type { FileItem } from '@/types/api'
import QueryTable from '@/components/pro/QueryTable.vue'
import { listFiles } from '@/apis/file'
import { formatBytes, formatDateTime } from '@hei/shared'

const { t } = useI18n()
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

const columns = computed<TableColumnsType<FileItem>>(() => [
  { title: '#', key: 'serial', width: 70 },
  { title: t('table.filename'), dataIndex: 'filename', key: 'filename' },
  { title: t('common.type'), dataIndex: 'content_type', key: 'content_type', width: 160 },
  { title: t('table.size'), dataIndex: 'size', key: 'size', width: 120 },
  { title: t('table.storage'), dataIndex: 'storage_provider', key: 'storage_provider', width: 120 },
  { title: t('table.uploader'), dataIndex: 'uploader', key: 'uploader', width: 120 },
  { title: t('table.uploadTime'), dataIndex: 'created_at', key: 'created_at', width: 160 },
  { title: t('common.actions'), key: 'actions', width: 140 },
])

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
            <AFormItem :label="t('common.keyword')">
              <AInput v-model:value="query.keyword" allow-clear :placeholder="t('table.fileKeyword')" @press-enter="fetchData" />
            </AFormItem>
          </ACol>
          <ACol :md="8" :sm="24">
            <AFormItem :label="t('table.fileType')">
              <ASelect v-model:value="query.content_type" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption value="text/csv">CSV</ASelectOption>
                <ASelectOption value="image/png">PNG</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('table.storage')">
              <ASelect v-model:value="query.storage_provider" allow-clear :placeholder="t('common.pleaseSelect')">
                <ASelectOption value="s3">S3</ASelectOption>
                <ASelectOption value="local">{{ t('table.local') }}</ASelectOption>
              </ASelect>
            </AFormItem>
          </ACol>
          <ACol v-show="expanded" :md="8" :sm="24">
            <AFormItem :label="t('table.uploader')">
              <AInput v-model:value="query.uploader" allow-clear :placeholder="t('table.uploaderPlaceholder')" />
            </AFormItem>
          </ACol>
          <ACol :md="expanded ? 24 : 8" :sm="24">
            <span class="inline-flex flex-wrap gap-2" :class="{ 'is-expanded': expanded }">
              <AButton type="link" @click="toggle">
                {{ expanded ? t('common.collapse') : t('common.expand') }}
                <UpOutlined v-if="expanded" />
                <DownOutlined v-else />
              </AButton>
              <AButton type="primary" @click="fetchData">{{ t('common.search') }}</AButton>
              <AButton class="ml-2" @click="resetQuery">{{ t('common.reset') }}</AButton>
            </span>
          </ACol>
        </ARow>
      </AForm>
    </template>

    <template #toolbar>
      <div class="text-16px text-slate-900 font-600 dark:text-zinc-100">{{ t('table.fileList') }}</div>
      <ASpace>
        <AButton @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.refresh') }}
        </AButton>
        <AButton type="primary">
          <template #icon><CloudUploadOutlined /></template>
          {{ t('table.uploadFile') }}
        </AButton>
        <ADropdown v-if="selectedRowKeys.length > 0">
          <AButton>{{ t('common.batchActions') }} <DownOutlined /></AButton>
          <template #overlay>
            <AMenu>
              <AMenuItem key="download">{{ t('table.batchDownload') }}</AMenuItem>
              <AMenuItem key="delete">{{ t('table.batchDelete') }}</AMenuItem>
            </AMenu>
          </template>
        </ADropdown>
      </ASpace>
    </template>

    <template #alert>
      <AAlert show-icon type="info">
        <template #message>
          {{ t('common.selectedCount', { count: selectedRowKeys.length }) }}
          <a class="ml-3" @click="selectedRowKeys = []">{{ t('common.clear') }}</a>
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
            <AButton size="small" type="link">{{ t('common.download') }}</AButton>
            <AButton size="small" type="link" danger>{{ t('common.delete') }}</AButton>
          </span>
        </template>
      </template>
    </ATable>
  </QueryTable>
</template>
