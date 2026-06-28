<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { roleApi } from '@/api'
import { NBadge, NButton, NCheckbox, NInput, NInputGroup, NRadio, NRadioGroup } from 'naive-ui'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const customValue = 'SCOPE_ORG_DEFINE'
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  searchText: '',
  role: {} as any,
  rows: [] as any[],
  page: 1,
  pageSize: 10,
  scopeRadioValue: '',
})

const modalTitle = computed(() =>
  state.role?.name
    ? `${t('pages.iam.role.grantPermission')} - ${state.role.name}`
    : t('pages.iam.role.grantPermission'),
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
        {t('pages.iam.role.apiPrefix')}
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
        {t('pages.iam.role.apiResource')}
      </NCheckbox>
    ),
    key: 'suffix',
    width: 360,
    filter: true,
    renderFilterMenu: ({ hide }) => (
      <div class="grant-permission-filter">
        <NInput
          value={state.searchText}
          placeholder={t('pages.iam.role.permissionSearchPlaceholder')}
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
            {t('pages.iam.role.search')}
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
        <span>{t('pages.iam.role.dataScope')}</span>
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
            {item.value === customValue && item.check && item.scopeDefineOrgIdList?.length ? (
              <NBadge value={item.scopeDefineOrgIdList.length} type="success">
                <span>{item.title}</span>
              </NBadge>
            ) : (
              item.title
            )}
          </NRadio>
        ))}
        {row.dataScope[4]?.check ? (
          <NButton text type="primary" size="small" onClick={() => handleDefineOrg(row)}>
            {t('pages.iam.role.chooseOrg')}
          </NButton>
        ) : null}
      </div>
    ),
  },
])

async function openModal(role: any) {
  state.role = role ?? {}
  state.rows = []
  state.searchText = ''
  state.page = 1
  state.pageSize = 10
  state.scopeRadioValue = ''
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.role?.id) {
    return
  }
  state.loading = true
  try {
    const response = await roleApi.ownPermissions(state.role.id)
    state.rows = echoModuleData(response.data?.apis ?? [], response.data?.grantInfoList ?? [])
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    await roleApi.grantPermissions({
      roleId: state.role.id,
      grantInfoList: convertData(),
    })
    window.$message.success(t('pages.iam.role.grantSuccess'))
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function closeModal() {
  state.rows = []
  state.scopeRadioValue = ''
  state.showModal = false
  state.submitLoading = false
}

function echoModuleData(apis: string[], grantInfoList: any[]) {
  const grantMap = new Map(grantInfoList.map((item: any) => [item.apiUrl, item]))
  return setParentDataCheckedStatus(
    apis.map((api) => {
      const grant = grantMap.get(subStrApi(api))
      const row = {
        api,
        prefix: splitByThirdSlash(api)[0],
        suffix: splitByThirdSlash(api)[1],
        dataScope: dataScopeOptions(api),
        check: Boolean(grant),
        parentCheck: false,
      }
      if (grant) {
        row.dataScope.forEach((item: any) => {
          if (item.value === grant.scopeCategory) {
            item.check = true
            if (item.value === customValue) {
              item.scopeDefineOrgIdList = grant.scopeDefineOrgIdList ?? []
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
      title: t('pages.iam.role.dataScopeAll'),
      value: 'SCOPE_ALL',
      check: false,
    },
    {
      id: `SCOPE_SELF_${id}`,
      title: t('pages.iam.role.dataScopeSelf'),
      value: 'SCOPE_SELF',
      check: false,
    },
    {
      id: `SCOPE_ORG_${id}`,
      title: t('pages.iam.role.dataScopeOrg'),
      value: 'SCOPE_ORG',
      check: false,
    },
    {
      id: `SCOPE_ORG_CHILD_${id}`,
      title: t('pages.iam.role.dataScopeOrgChild'),
      value: 'SCOPE_ORG_CHILD',
      check: false,
    },
    {
      id: `SCOPE_ORG_DEFINE_${id}`,
      title: t('pages.iam.role.dataScopeCustom'),
      value: customValue,
      check: false,
      scopeDefineOrgIdList: [],
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
        item.scopeDefineOrgIdList = []
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
        scope.scopeDefineOrgIdList = []
      }
    })
  })
}

function handleDefineOrg(record: any) {
  const customScope = record.dataScope.find((item: any) => item.value === customValue)
  if (!customScope) {
    return
  }
  customScope.scopeDefineOrgIdList = customScope.scopeDefineOrgIdList?.length
    ? []
    : ['mock-org-1', 'mock-org-2']
}

function convertData() {
  return state.rows.flatMap((row) => {
    if (!row.check) {
      return []
    }
    return row.dataScope
      .filter((scope: any) => scope.check)
      .map((scope: any) => ({
        apiUrl: subStrApi(row.api),
        scopeCategory: scope.value,
        scopeDefineOrgIdList: scope.scopeDefineOrgIdList ?? [],
      }))
  })
}

function subStrApi(api: string) {
  const index = api.indexOf('[')
  return index > -1 ? api.substring(0, index) : api
}

function splitByThirdSlash(api: string) {
  const arr = api.split('/').filter(Boolean)
  const leftPart = `/${arr.slice(0, 2).join('/')}`
  const rightPart = `/${arr.slice(2).join('/')}`
  return [leftPart, rightPart]
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
        {{ t('pages.iam.role.grantPermissionTip') }}
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
