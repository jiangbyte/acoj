<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { ojClazzApi } from '@/api'
import { renderButtonIcon } from '@/utils'
import { NButton, NTag } from 'naive-ui'
import { computed, reactive, watch } from 'vue'

export interface ClassOption {
  id: string
  code: string
  name: string
  status?: string
  visibility?: string
  member_count?: number
}

const props = withDefaults(
  defineProps<{
    visible: boolean
    mode?: 'single' | 'multiple'
    title?: string
    selected?: ClassOption[]
    /** 仅启用班级；默认 true */
    enabledOnly?: boolean
  }>(),
  {
    mode: 'single',
    title: '选择班级',
    selected: () => [],
    enabledOnly: true,
  },
)

const emit = defineEmits<{
  'update:visible': [value: boolean]
  select: [clazz: ClassOption]
  'update:selected': [value: ClassOption[]]
  confirm: [classes: ClassOption[]]
}>()

const state = reactive({
  loading: false,
  code: '',
  name: '',
  options: [] as ClassOption[],
  total: 0,
  page: 1,
  pageSize: 20,
  selectedData: [] as ClassOption[],
})

const selectedIds = computed(() => new Set(state.selectedData.map(item => String(item.id))))

const statusLabel: Record<string, string> = {
  ENABLED: '启用',
  DISABLED: '禁用',
}

const visibilityLabel: Record<string, string> = {
  PUBLIC: '公开',
  PRIVATE: '私有',
}

const singleColumns = computed<DataTableColumns<ClassOption>>(() => [
  { title: '编码', key: 'code', width: 120, ellipsis: { tooltip: true } },
  { title: '名称', key: 'name', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row => statusLabel[row.status || ''] ?? row.status ?? '-',
  },
  {
    title: '可见性',
    key: 'visibility',
    width: 80,
    render: row => visibilityLabel[row.visibility || ''] ?? row.visibility ?? '-',
  },
  {
    title: '成员',
    key: 'member_count',
    width: 70,
    render: row => row.member_count ?? 0,
  },
  {
    title: '操作',
    key: 'action',
    width: 70,
    align: 'center',
    render: row => (
      <NButton text type="primary" size="small" onClick={() => handleSelect(row)}>
        选择
      </NButton>
    ),
  },
])

const multipleLeftColumns = computed<DataTableColumns<ClassOption>>(() => [
  {
    title: '操作',
    key: 'action',
    align: 'center',
    width: 56,
    render: row => (
      <NButton
        text
        type="primary"
        size="small"
        disabled={selectedIds.value.has(String(row.id))}
        onClick={() => addRecord(row)}
      >
        {renderButtonIcon('icon-park-outline:plus')}
      </NButton>
    ),
  },
  { title: '编码', key: 'code', width: 110, ellipsis: { tooltip: true } },
  { title: '名称', key: 'name', minWidth: 140, ellipsis: { tooltip: true } },
  {
    title: '可见性',
    key: 'visibility',
    width: 70,
    render: row => (
      <NTag size="small" type={row.visibility === 'PUBLIC' ? 'success' : 'default'}>
        {visibilityLabel[row.visibility || ''] ?? row.visibility ?? '-'}
      </NTag>
    ),
  },
])

const multipleRightColumns = computed<DataTableColumns<ClassOption>>(() => [
  {
    title: '操作',
    key: 'action',
    align: 'center',
    width: 70,
    render: row => (
      <NButton text type="error" size="small" onClick={() => delRecord(row)}>
        {renderButtonIcon('icon-park-outline:delete')}
      </NButton>
    ),
  },
  { title: '编码', key: 'code', width: 100, ellipsis: { tooltip: true } },
  { title: '名称', key: 'name', minWidth: 120, ellipsis: { tooltip: true } },
])

watch(
  () => props.visible,
  (val) => {
    if (val) {
      state.selectedData = [...props.selected]
      state.code = ''
      state.name = ''
      state.page = 1
      void loadOptions()
    }
    else {
      state.selectedData = []
    }
  },
)

watch(
  () => [state.page, state.pageSize],
  () => {
    if (props.visible) void loadOptions()
  },
)

async function loadOptions() {
  state.loading = true
  try {
    const params: Record<string, string | number> = {
      current: state.page,
      size: state.pageSize,
    }
    if (props.enabledOnly) {
      params.status = 'ENABLED'
    }
    const code = state.code.trim()
    const name = state.name.trim()
    if (code) params.code = code
    if (name) params.name = name
    const res = await ojClazzApi.page(params)
    const records = res?.data?.records ?? []
    state.options = records.map((item: any) => ({
      id: String(item.id),
      code: item.code || '',
      name: item.name || '',
      status: item.status,
      visibility: item.visibility,
      member_count: item.member_count,
    }))
    state.total = res?.data?.total ?? 0
  }
  catch {
    state.options = []
    state.total = 0
  }
  finally {
    state.loading = false
  }
}

function handleSelect(clazz: ClassOption) {
  emit('select', clazz)
  close()
}

function doSearch() {
  state.page = 1
  void loadOptions()
}

function resetSearch() {
  state.code = ''
  state.name = ''
  state.page = 1
  void loadOptions()
}

function addRecord(record: ClassOption) {
  if (!selectedIds.value.has(String(record.id))) {
    state.selectedData.push(record)
  }
}

function addAllPage() {
  state.options.forEach(addRecord)
}

function delRecord(record: ClassOption) {
  state.selectedData = state.selectedData.filter(item => String(item.id) !== String(record.id))
}

function delAll() {
  state.selectedData = []
}

function handleConfirm() {
  const next = [...state.selectedData]
  emit('update:selected', next)
  emit('confirm', next)
  close()
}

function close() {
  emit('update:visible', false)
}
</script>

<template>
  <NDrawer
    :show="visible"
    :default-width="mode === 'multiple' ? 1000 : 720"
    placement="right"
    :mask-closable="false"
    @update:show="(val) => emit('update:visible', val)"
  >
    <NDrawerContent :title="title" closable :native-scrollbar="false">
      <template v-if="mode === 'single'">
        <NSpace vertical>
          <NInputGroup>
            <NInput
              v-model:value="state.code"
              clearable
              placeholder="班级编码"
              @keyup.enter="doSearch"
            />
            <NInput
              v-model:value="state.name"
              clearable
              placeholder="班级名称"
              @keyup.enter="doSearch"
            />
            <NButton type="primary" @click="doSearch">
              搜索
            </NButton>
            <NButton @click="resetSearch">
              重置
            </NButton>
          </NInputGroup>
          <NDataTable
            :row-key="(row: ClassOption) => row.id"
            :columns="singleColumns"
            :data="state.options"
            :loading="state.loading"
            :bordered="true"
            :single-line="false"
            max-height="calc(100vh - 290px)"
          />
          <NPagination
            v-model:page="state.page"
            v-model:page-size="state.pageSize"
            show-size-picker
            :item-count="state.total"
            :page-sizes="[10, 20, 50, 100]"
          />
        </NSpace>
      </template>

      <template v-else>
        <NGrid :cols="24" :x-gap="10">
          <NGi :span="15">
            <NSpace vertical>
              <NInputGroup>
                <NInput
                  v-model:value="state.code"
                  clearable
                  placeholder="班级编码"
                  @keyup.enter="doSearch"
                />
                <NInput
                  v-model:value="state.name"
                  clearable
                  placeholder="班级名称"
                  @keyup.enter="doSearch"
                />
                <NButton type="primary" @click="doSearch">
                  搜索
                </NButton>
                <NButton @click="resetSearch">
                  重置
                </NButton>
              </NInputGroup>
              <NFlex justify="space-between" align="center">
                <NText>待选列表：{{ state.total }}</NText>
                <NButton dashed size="small" @click="addAllPage">
                  新增当前页
                </NButton>
              </NFlex>
              <NDataTable
                :row-key="(row: ClassOption) => row.id"
                :columns="multipleLeftColumns"
                :data="state.options"
                :loading="state.loading"
                :bordered="true"
                :single-line="false"
                max-height="calc(100vh - 340px)"
              />
              <NPagination
                v-model:page="state.page"
                v-model:page-size="state.pageSize"
                show-size-picker
                :item-count="state.total"
                :page-sizes="[10, 20, 50, 100]"
              />
            </NSpace>
          </NGi>
          <NGi :span="9">
            <NSpace vertical>
              <NFlex justify="space-between" align="center">
                <NText>已选择：{{ state.selectedData.length }}</NText>
                <NButton dashed type="error" size="small" @click="delAll">
                  全部移除
                </NButton>
              </NFlex>
              <NDataTable
                :row-key="(row: ClassOption) => row.id"
                :columns="multipleRightColumns"
                :data="state.selectedData"
                :bordered="true"
                :single-line="false"
                max-height="calc(100vh - 280px)"
              />
            </NSpace>
          </NGi>
        </NGrid>
      </template>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="close">
            关闭
          </NButton>
          <NButton v-if="mode === 'multiple'" type="primary" @click="handleConfirm">
            确认
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
