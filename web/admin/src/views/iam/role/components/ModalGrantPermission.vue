<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { deptApi, roleApi } from '@/api'
import {
  NBadge,
  NButton,
  NCheckbox,
  NInput,
  NInputGroup,
  NRadio,
  NRadioGroup,
  NTreeSelect,
} from 'naive-ui'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const customValue = 'CUSTOM'
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  searchText: '',
  subject: {} as any,
  grantApi: roleApi as any,
  title: '',
  rows: [] as any[],
  deptTree: [] as any[],
  page: 1,
  pageSize: 10,
  scopeRadioValue: '',
})

const modalTitle = computed(() =>
  state.subject?.name
    ? `${state.title || t('resource.iam.role.grant_permission')} - ${state.subject.name}`
    : state.title || t('resource.iam.role.grant_permission'),
)
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
const allChecked = computed(
  () => state.rows.length > 0 && state.rows.every((item) => item.parentCheck),
)

const columns = computed<DataTableColumns<any>>(() => [
  {
    title: () => (
      <NCheckbox
        checked={allChecked.value}
        onUpdateChecked={(checked) => onCheckAllChange(Boolean(checked))}
      >
        {t('resource.iam.role.api_prefix')}
      </NCheckbox>
    ),
    key: 'prefix',
    fixed: 'left',
    width: 260,
    rowSpan: (row, rowIndex) => {
      const indexArr = firstShowMap.value[row.prefix] ?? []
      return rowIndex === indexArr[0] ? indexArr.length : 0
    },
    render: (row) => (
      <NCheckbox
        checked={row.parentCheck}
        onUpdateChecked={(checked) => changeParentApi(row, Boolean(checked))}
      >
        {row.prefix}
      </NCheckbox>
    ),
  },
  {
    title: () => (
      <NCheckbox
        checked={allChecked.value}
        onUpdateChecked={(checked) => onCheckAllChange(Boolean(checked))}
      >
        {t('resource.iam.role.api_resource')}
      </NCheckbox>
    ),
    key: 'suffix',
    width: 360,
    filter: true,
    renderFilterMenu: ({ hide }) => (
      <div class="grant-permission-filter">
        <NInput
          value={state.searchText}
          placeholder={t('resource.iam.role.placeholder.permission_search')}
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
            {t('resource.iam.role.search')}
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
      <NCheckbox
        checked={row.check}
        onUpdateChecked={(checked) => changeApi(row, Boolean(checked))}
      >
        {row.suffix}
      </NCheckbox>
    ),
  },
  {
    title: () => (
      <div class="grant-permission-scope-header">
        <span>{t('resource.iam.role.data_scope')}</span>
        <NRadioGroup
          value={state.scopeRadioValue}
          size="small"
          onUpdateValue={(value) => radioChange(String(value))}
        >
          {dataScopeOptions('')
            .slice(0, 4)
            .map((item: any) => (
              <NRadio key={item.value} value={item.value}>
                {item.title}
              </NRadio>
            ))}
        </NRadioGroup>
      </div>
    ),
    key: 'dataScope',
    minWidth: 520,
    render: (row) => (
      <div class="grant-permission-scope-cell">
        {row.dataScope.map((item: any) => (
          <NRadio
            key={item.id}
            checked={item.check}
            name={item.title}
            onUpdateChecked={(checked) => {
              if (checked) {
                changeDataScope(row, item)
              }
            }}
          >
            {item.value === customValue && item.check && item.custom_scope_dept_ids?.length ? (
              <NBadge value={item.custom_scope_dept_ids.length} type="success">
                <span>{item.title}</span>
              </NBadge>
            ) : (
              item.title
            )}
          </NRadio>
        ))}
        {row.dataScope[4]?.check ? (
          <NTreeSelect
            value={row.dataScope[4].custom_scope_dept_ids}
            multiple
            cascade
            checkable
            clearable
            filterable
            size="small"
            options={state.deptTree}
            keyField="id"
            labelField="name"
            childrenField="children"
            style="min-width: 240px"
            onUpdateValue={(value) => {
              row.dataScope[4].custom_scope_dept_ids = (value ?? []).map(String)
            }}
          />
        ) : null}
      </div>
    ),
  },
])

async function openModal(subject: any, grantApi: any = roleApi, title = '') {
  state.subject = subject ?? {}
  state.grantApi = grantApi
  state.title = title
  state.rows = []
  state.searchText = ''
  state.page = 1
  state.pageSize = 10
  state.scopeRadioValue = ''
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.subject?.id) {
    return
  }
  state.loading = true
  try {
    const [permissionResponse, deptResponse] = await Promise.all([
      state.grantApi.ownPermissions(state.subject.id),
      deptApi.tree().catch(() => ({ data: [] })),
    ])
    state.deptTree = deptResponse.data ?? []
    state.rows = echoModuleData(
      permissionResponse.data?.permissions ?? [],
      permissionResponse.data?.grant_info_list ?? [],
    )
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    await state.grantApi.grantPermissions({
      id: state.subject.id,
      grant_info_list: convertData(),
    })
    window.$message.success(t('resource.iam.role.grant_success'))
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function closeModal() {
  state.rows = []
  state.deptTree = []
  state.scopeRadioValue = ''
  state.showModal = false
  state.submitLoading = false
}

function echoModuleData(apis: any[], grant_info_list: any[]) {
  const grantMap = new Map(grant_info_list.map((item: any) => [item.permission_key, item]))
  return setParentDataCheckedStatus(
    apis.map((api) => {
      const permissionKey = typeof api === 'string' ? subStrApi(api) : api.permission_key
      const apiText =
        typeof api === 'string' ? api : `${api.permission_key}[${api.name ?? api.permission_key}]`
      const grant = grantMap.get(permissionKey)
      const row = {
        api: apiText,
        permissionKey,
        prefix: splitByPermissionKey(permissionKey)[0],
        suffix: splitByPermissionKey(permissionKey)[1],
        dataScope: dataScopeOptions(permissionKey),
        check: Boolean(grant),
        parentCheck: false,
      }
      if (grant) {
        row.dataScope.forEach((item: any) => {
          if (item.value === grant.data_scope) {
            item.check = true
            if (item.value === customValue) {
              item.custom_scope_dept_ids = grant.custom_scope_dept_ids ?? []
            }
          }
        })
      }
      return row
    }),
  )
}

function dataScopeOptions(id: string) {
  return [
    {
      id: `SCOPE_ALL_${id}`,
      title: t('resource.iam.role.data_scope_all'),
      value: 'ALL',
      check: false,
    },
    {
      id: `SCOPE_SELF_${id}`,
      title: t('resource.iam.role.data_scope_self'),
      value: 'SELF',
      check: false,
    },
    {
      id: `SCOPE_ORG_${id}`,
      title: t('resource.iam.role.data_scope_org'),
      value: 'DEPT',
      check: false,
    },
    {
      id: `SCOPE_ORG_CHILD_${id}`,
      title: t('resource.iam.role.data_scope_org_child'),
      value: 'DEPT_AND_CHILD',
      check: false,
    },
    {
      id: `SCOPE_ORG_DEFINE_${id}`,
      title: t('resource.iam.role.data_scope_custom'),
      value: customValue,
      check: false,
      custom_scope_dept_ids: [],
    },
  ]
}

function onCheckAllChange(checked: boolean) {
  state.rows.forEach((row) => {
    changeApi(row, checked, false)
    row.parentCheck = checked
  })
}

function changeParentApi(record: any, checked: boolean) {
  state.rows.forEach((row) => {
    if (row.prefix === record.prefix) {
      changeApi(row, checked, false)
      row.parentCheck = checked
    }
  })
  state.rows = setParentDataCheckedStatus(state.rows)
}

function changeApi(record: any, checked: boolean, refreshParent = true) {
  record.check = checked
  if (checked) {
    const hasScope = record.dataScope.some((item: any) => item.check)
    if (!hasScope) {
      record.dataScope[0].check = true
    }
  } else {
    record.dataScope.forEach((item: any) => {
      item.check = false
      if (item.value === customValue) {
        item.custom_scope_dept_ids = []
      }
    })
  }
  if (refreshParent) {
    state.rows = setParentDataCheckedStatus(state.rows)
  }
}

function changeDataScope(record: any, scope: any) {
  record.dataScope.forEach((item: any) => {
    item.check = item.value === scope.value
  })
  record.check = true
  state.rows = setParentDataCheckedStatus(state.rows)
}

function radioChange(value: string) {
  state.scopeRadioValue = value
  state.rows.forEach((row) => {
    row.dataScope.forEach((scope: any) => {
      scope.check = scope.value === value
      if (scope.value === customValue) {
        scope.custom_scope_dept_ids = []
      }
    })
  })
}

function convertData() {
  return state.rows.flatMap((row) => {
    if (!row.check) {
      return []
    }
    return row.dataScope
      .filter((scope: any) => scope.check)
      .map((scope: any) => ({
        permission_key: row.permissionKey,
        data_scope: scope.value,
        custom_scope_dept_ids: scope.custom_scope_dept_ids ?? [],
      }))
  })
}

function subStrApi(api: string) {
  const index = api.indexOf('[')
  return index > -1 ? api.substring(0, index) : api
}

function splitByPermissionKey(permissionKey: string) {
  const parts = permissionKey.split(':')
  return [parts.slice(0, 2).join(':') || permissionKey, parts.slice(2).join(':') || permissionKey]
}

function setParentDataCheckedStatus(records: any[]) {
  return records.map((row) => {
    const children = records.filter((item) => item.prefix === row.prefix)
    return {
      ...row,
      parentCheck: children.every((item) => item.check),
    }
  })
}

function handleSearch() {
  state.page = 1
}

function resetSearch() {
  state.searchText = ''
  handleSearch()
}

defineExpose({
  openModal,
})
</script>

<template>
  <NDrawer
    v-model:show="state.showModal"
    :default-width="1000"
    placement="right"
    resizable
    :mask-closable="false"
  >
    <NDrawerContent :title="modalTitle" closable :native-scrollbar="false">
      <NAlert type="warning" closable>
        {{ t('resource.iam.role.grant_permission_tip') }}
      </NAlert>
      <NSpin :show="state.loading">
        <div class="mt-16px">
          <NDataTable
            size="medium"
            :row-key="(row) => row.api"
            :columns="columns"
            :data="tableRows"
            :bordered="true"
            :single-line="false"
            :scroll-x="980"
            max-height="calc(100vh - 300px)"
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
        </div>
      </NSpin>

      <template #footer>
        <NSpace justify="end" align="center">
          <NButton @click="closeModal">
            {{ t('common.close') }}
          </NButton>
          <NButton type="primary" :loading="state.submitLoading" @click="submitGrant">
            {{ t('common.save') }}
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>
.grant-permission-scope-header,
.grant-permission-scope-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
}
</style>
