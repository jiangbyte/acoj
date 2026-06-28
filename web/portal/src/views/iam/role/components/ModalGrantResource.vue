<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { roleApi } from '@/api'
import { NCheckbox } from 'naive-ui'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  role: {} as any,
  activeModuleId: '',
  modules: [] as any[],
})

const modalTitle = computed(() =>
  state.role?.name
    ? `${t('pages.iam.role.grantResource')} - ${state.role.name}`
    : t('pages.iam.role.grantResource'),
)
const activeModule = computed(
  () => state.modules.find((item) => item.id === state.activeModuleId) ?? state.modules[0],
)
const rows = computed(() => activeModule.value?.menu ?? [])
const firstShowMap = computed<Record<string, number[]>>(() => {
  const map: Record<string, number[]> = {}
  rows.value.forEach((item: any, index: number) => {
    if (map[item.parentName]) {
      map[item.parentName].push(index)
    } else {
      map[item.parentName] = [index]
    }
  })
  return map
})
const columns = computed<DataTableColumns<any>>(() => [
  {
    title: t('pages.iam.role.parentResource'),
    key: 'parentName',
    fixed: 'left',
    width: 180,
    rowSpan: (row, rowIndex) => {
      const indexArr = firstShowMap.value[row.parentName] ?? []
      return rowIndex === indexArr[0] ? indexArr.length : 0
    },
    render: (row) => (
      <NCheckbox
        checked={row.parentCheck}
        onUpdateChecked={(checked) => changeParent(row, Boolean(checked))}
      >
        {row.parentName}
      </NCheckbox>
    ),
  },
  {
    title: t('pages.iam.role.menuResource'),
    key: 'title',
    width: 220,
    render: (row) => (
      <NCheckbox
        checked={row.nameCheck}
        onUpdateChecked={(checked) => changeSub(row, Boolean(checked))}
      >
        {row.title}
      </NCheckbox>
    ),
  },
  {
    title: t('pages.iam.role.buttonGrant'),
    key: 'button',
    minWidth: 520,
    render: (row) => {
      if (!row.button?.length) {
        return null
      }
      return (
        <div class="grant-check-list">
          {row.button.map((button: any) => (
            <NCheckbox
              key={button.id}
              checked={button.check}
              onUpdateChecked={(checked) => changeChildCheckBox(row, button, Boolean(checked))}
            >
              {button.title}
            </NCheckbox>
          ))}
        </div>
      )
    },
  },
])

async function openModal(role: any) {
  state.role = role ?? {}
  state.modules = []
  state.activeModuleId = ''
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.role?.id) {
    return
  }
  state.loading = true
  try {
    const response = await roleApi.ownResources(state.role.id)
    const modules = echoModuleData(response.data?.modules ?? [], response.data?.grantInfoList ?? [])
    state.modules = modules
    state.activeModuleId = modules[0]?.id ?? ''
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    await roleApi.grantResources({
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
  state.modules = []
  state.activeModuleId = ''
  state.showModal = false
  state.submitLoading = false
}

function echoModuleData(modules: any[], grantInfoList: any[]) {
  const grantMap = new Map(
    grantInfoList.map((item: any) => [item.menuId, new Set(item.buttonInfo ?? [])]),
  )
  return JSON.parse(JSON.stringify(modules)).map((module: any) => ({
    ...module,
    menu: (module.menu ?? [])
      .map((menu: any) => {
        const buttonSet = grantMap.get(menu.id)
        return {
          ...menu,
          parentCheck: Boolean(buttonSet),
          nameCheck: Boolean(buttonSet),
          button: (menu.button ?? []).map((button: any) => ({
            ...button,
            check: Boolean(buttonSet?.has(button.id)),
          })),
        }
      })
      .sort((a: any, b: any) => {
        const nameComparison = String(b.parentName).localeCompare(String(a.parentName))
        return nameComparison !== 0 ? nameComparison : Number(a.parentId) - Number(b.parentId)
      }),
  }))
}

function changeParent(record: any, checked: boolean) {
  record.parentCheck = checked
  const moduleMenu = state.modules.find((item) => item.id === record.module)?.menu ?? []
  moduleMenu
    .filter((item: any) => item.parentName === record.parentName)
    .forEach((item: any) => changeSub(item, checked))
}

function changeSub(record: any, checked: boolean) {
  record.nameCheck = checked
  ;(record.button ?? []).forEach((button: any) => {
    button.check = checked
  })
}

function changeChildCheckBox(record: any, button: any, checked: boolean) {
  button.check = checked
  if (checked) {
    record.nameCheck = true
    record.parentCheck = true
  }
}

function convertData() {
  return state.modules.flatMap((module) =>
    (module.menu ?? [])
      .filter((menu: any) => menu.nameCheck)
      .map((menu: any) => ({
        menuId: menu.id,
        buttonInfo: (menu.button ?? [])
          .filter((button: any) => button.check)
          .map((button: any) => button.id),
      })),
  )
}

defineExpose({
  openModal,
})
</script>

<template>
  <NDrawer
    v-model:show="state.showModal"
    :default-width="1000"
    resizable
    placement="right"
    :mask-closable="false"
  >
    <NDrawerContent :title="modalTitle" closable :native-scrollbar="false">
      <NAlert type="warning" :bordered="false" class="mb-10px">
        {{ t('pages.iam.role.grantResourceTip') }}
      </NAlert>
      <NSpin :show="state.loading">
        <NRadioGroup v-model:value="state.activeModuleId" size="small" class="mb-10px">
          <NRadioButton
            v-for="module in state.modules"
            :key="module.id"
            :value="module.id"
            :label="module.title"
          />
        </NRadioGroup>

        <NDataTable
          size="medium"
          :row-key="(row) => row.id"
          :columns="columns"
          :data="rows"
          :bordered="true"
          :single-line="false"
          :scroll-x="920"
          max-height="calc(100vh - 230px)"
        />
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
.grant-check-list {
  display: grid;
  grid-template-columns: repeat(5, max-content);
  gap: 6px 14px;
  align-items: center;
}
</style>
