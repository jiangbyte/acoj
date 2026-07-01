<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { roleApi } from '@/api'
import { translateLocale } from '@/utils'
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
  subject: {} as any,
  grantApi: roleApi as any,
  title: '',
  activeModuleId: null as string | null,
  modules: [] as any[],
})

const modalTitle = computed(() =>
  state.subject?.name
    ? `${state.title || t('resource.iam.role.grant_resource')} - ${state.subject.name}`
    : state.title || t('resource.iam.role.grant_resource'),
)
const activeModule = computed(
  () => state.modules.find((item) => item.id === state.activeModuleId) ?? state.modules[0],
)
const rows = computed(() => activeModule.value?.menu ?? [])
const firstShowMap = computed<Record<string, number[]>>(() => {
  const map: Record<string, number[]> = {}
  rows.value.forEach((item: any, index: number) => {
    if (map[item.parent_id_name_display]) {
      map[item.parent_id_name_display].push(index)
    } else {
      map[item.parent_id_name_display] = [index]
    }
  })
  return map
})
const columns = computed<DataTableColumns<any>>(() => [
  {
    title: t('resource.iam.role.parent_resource'),
    key: 'parent_id_name',
    fixed: 'left',
    width: 180,
    rowSpan: (row, rowIndex) => {
      const indexArr = firstShowMap.value[row.parent_id_name_display] ?? []
      return rowIndex === indexArr[0] ? indexArr.length : 0
    },
    render: (row) => (
      <NCheckbox
        checked={row.parentCheck}
        onUpdateChecked={(checked) => changeParent(row, Boolean(checked))}
      >
        {row.parent_id_name_display}
      </NCheckbox>
    ),
  },
  {
    title: t('resource.iam.role.menu_resource'),
    key: 'title',
    width: 220,
    render: (row) => (
      <NCheckbox
        checked={row.nameCheck}
        onUpdateChecked={(checked) => changeSub(row, Boolean(checked))}
      >
        {row.title_display}
      </NCheckbox>
    ),
  },
  {
    title: t('resource.iam.role.button_grant'),
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
              {button.title_display}
            </NCheckbox>
          ))}
        </div>
      )
    },
  },
])

async function openModal(subject: any, grantApi: any = roleApi, title = '') {
  state.subject = subject ?? {}
  state.grantApi = grantApi
  state.title = title
  state.modules = []
  state.activeModuleId = null
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.subject?.id) {
    return
  }
  state.loading = true
  try {
    const response = await state.grantApi.ownResources(state.subject.id)
    const modules = echoModuleData(
      response.data?.modules ?? [],
      response.data?.grant_info_list ?? [],
    )
    state.modules = modules
    state.activeModuleId = modules[0]?.id ?? null
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    await state.grantApi.grantResources({
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
  state.modules = []
  state.activeModuleId = null
  state.showModal = false
  state.submitLoading = false
}

function echoModuleData(modules: any[], grant_info_list: any[]) {
  const grantMap = new Map(
    grant_info_list.map((item: any) => [item.resource_id, new Set(item.permission_keys ?? [])]),
  )
  return JSON.parse(JSON.stringify(modules)).map((module: any) => ({
    ...module,
    title_display: translateLocale(module.locale_key, module.title),
    menu: (module.menu ?? [])
      .map((menu: any) => {
        const buttonSet = grantMap.get(menu.id)
        return {
          ...menu,
          parent_id_name_display: translateLocale(menu.parent_locale_key, menu.parent_id_name),
          title_display: translateLocale(menu.locale_key, menu.title),
          parentCheck: Boolean(buttonSet),
          nameCheck: Boolean(buttonSet),
          button: (menu.button ?? []).map((button: any) => ({
            ...button,
            title_display: translateLocale(button.locale_key, button.title),
            check: Boolean(buttonSet?.has(button.permission_key ?? button.id)),
          })),
        }
      })
      .sort((a: any, b: any) => {
        const nameComparison = String(b.parent_id_name_display).localeCompare(
          String(a.parent_id_name_display),
        )
        return nameComparison !== 0 ? nameComparison : Number(a.parent_id) - Number(b.parent_id)
      }),
  }))
}

function changeParent(record: any, checked: boolean) {
  record.parentCheck = checked
  const moduleMenu = state.modules.find((item) => item.id === record.module_id)?.menu ?? []
  moduleMenu
    .filter((item: any) => item.parent_id_name_display === record.parent_id_name_display)
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
        resource_id: menu.id,
        permission_keys: (menu.button ?? [])
          .filter((button: any) => button.check)
          .map((button: any) => button.permission_key ?? button.id),
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
        {{ t('resource.iam.role.grant_resource_tip') }}
      </NAlert>
      <NSpin :show="state.loading">
        <NRadioGroup v-model:value="state.activeModuleId" size="small" class="mb-10px">
          <NRadioButton
            v-for="module in state.modules"
            :key="module.id"
            :value="module.id"
            :label="module.title_display"
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
