<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { resourceApi } from '@/api'
import { NButton, NInput, NInputGroup, NTag } from 'naive-ui'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  selected: [permission: any]
}>()

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  searchText: '',
  currentKey: '',
  rows: [] as any[],
  page: 1,
  pageSize: 10,
})

const filteredRows = computed(() => {
  const keyword = state.searchText.trim().toUpperCase()
  if (!keyword) {
    return state.rows
  }
  return state.rows.filter((item) => item.api.toUpperCase().includes(keyword))
})
const tableRows = computed(() => {
  const start = (state.page - 1) * state.pageSize
  return filteredRows.value.slice(start, start + state.pageSize)
})
const firstShowMap = computed<Record<string, number[]>>(() => {
  const map: Record<string, number[]> = {}
  tableRows.value.forEach((item: any, index: number) => {
    if (map[item.prefix]) {
      map[item.prefix].push(index)
    } else {
      map[item.prefix] = [index]
    }
  })
  return map
})

const columns = computed<DataTableColumns<any>>(() => [
  {
    title: t('resource.iam.resource.permission_prefix'),
    key: 'prefix',
    fixed: 'left',
    width: 220,
    rowSpan: (row, rowIndex) => {
      const indexArr = firstShowMap.value[row.prefix] ?? []
      return rowIndex === indexArr[0] ? indexArr.length : 0
    },
    render: (row) => row.prefix,
  },
  {
    title: t('resource.iam.resource.permission_key'),
    key: 'suffix',
    minWidth: 360,
    filter: true,
    renderFilterMenu: ({ hide }) => (
      <div class="permission-selector-filter">
        <NInput
          value={state.searchText}
          placeholder={t('resource.iam.resource.permission_search_placeholder')}
          onUpdateValue={(value) => {
            state.searchText = value
          }}
          onKeyup={(event: KeyboardEvent) => {
            if (event.key === 'Enter') {
              handleSearch()
              hide()
            }
          }}
        />
        <NInputGroup>
          <NButton
            type="primary"
            size="small"
            onClick={() => {
              handleSearch()
              hide()
            }}
          >
            {t('common.search_form.search')}
          </NButton>
          <NButton
            size="small"
            onClick={() => {
              resetSearch()
              hide()
            }}
          >
            {t('common.reset')}
          </NButton>
        </NInputGroup>
      </div>
    ),
    render: (row) => (
      <div class="permission-selector-key">
        <span>{row.suffix}</span>
        {row.permission_key === state.currentKey ? (
          <NTag size="small" type="success" bordered={false}>
            {t('resource.iam.resource.selected')}
          </NTag>
        ) : null}
      </div>
    ),
  },
  {
    title: t('resource.iam.resource.permission_name'),
    key: 'name',
    minWidth: 200,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 100,
    fixed: 'right',
    render: (row) => (
      <NButton type="primary" size="small" text={true} onClick={() => selectPermission(row)}>
        {t('resource.iam.resource.select_permission')}
      </NButton>
    ),
  },
])

async function openModal(currentKey = '') {
  state.currentKey = currentKey
  state.searchText = ''
  state.page = 1
  state.showModal = true
  if (!state.rows.length) {
    await fetchPermissions()
  }
}

async function fetchPermissions() {
  state.loading = true
  try {
    const response = await resourceApi.permissionRegistry()
    state.rows = (response.data ?? []).map((item: any) => {
      const permissionKey = item.permission_key ?? item
      const [prefix, suffix] = splitByPermissionKey(permissionKey)
      return {
        ...item,
        permission_key: permissionKey,
        name: item.name ?? permissionKey,
        api: `${permissionKey}[${item.name ?? permissionKey}]`,
        prefix,
        suffix,
      }
    })
  } finally {
    state.loading = false
  }
}

function closeModal() {
  state.showModal = false
}

function selectPermission(row: any) {
  emit('selected', row)
  closeModal()
}

function handleSearch() {
  state.page = 1
}

function resetSearch() {
  state.searchText = ''
  handleSearch()
}

function splitByPermissionKey(permissionKey: string) {
  const parts = permissionKey.split(':')
  return [parts.slice(0, 2).join(':') || permissionKey, parts.slice(2).join(':') || permissionKey]
}

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="t('resource.iam.resource.select_permission')"
    style="width: 980px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.loading">
      <NDataTable
        size="medium"
        :row-key="(row) => row.permission_key"
        :columns="columns"
        :data="tableRows"
        :bordered="true"
        :single-line="false"
        :scroll-x="900"
        max-height="calc(100vh - 340px)"
      />
      <NFlex justify="end" class="mt-10px">
        <NPagination
          v-model:page="state.page"
          v-model:page-size="state.pageSize"
          show-size-picker
          size="small"
          :item-count="filteredRows.length"
          :page-sizes="[10, 20, 50, 100]"
        />
      </NFlex>
    </NSpin>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          {{ t('common.close') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<style scoped>
.permission-selector-filter {
  width: 300px;
  padding: 12px;
}

.permission-selector-key {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
