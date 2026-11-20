<script lang="ts" setup>
import { actionDeleteBtn, actionIconBtn } from '@/utils'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NEllipsis, NIcon, NPopconfirm, NSpace, NTag } from 'naive-ui'
import { useSysMenuFetch } from '@/composables/v1'
import Form from './form.vue'
import Detail from './detail.vue'
import Permission from './permission.vue'
import { Icon } from '@iconify/vue/offline'

const formRef = ref()
const detailRef = ref()
const permissionRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  // {
  //   title: 'Parent Menu ID',
  //   key: 'pid',
  // },
  {
    title: 'Menu',
    key: 'icon',
    // render: (row: any) => {
    //   return h(NIcon, { size: '18' }, { default: () => h(Icon, { icon: row.icon }) })
    // },
    render: (row: any) => {
      return h('div', { class: 'inline-flex items-center justify-center' }, [
        h(NIcon, { }, { default: () => h(Icon, { icon: row.icon }) }),
        h(NEllipsis, { style: { width: '80px' }, class: 'ml-2' }, () => row.title || row.name),
      ])
    },
  },
  {
    title: 'Menu Type',
    key: 'menuTypeName',
    width: 80,
    render: (row) => {
      return h(NTag, {
        type: row.menuType === 0 ? 'default' : 'warning',
      }, () => row.menuTypeName)
    },
  },
  // {
  //   title: 'Menu Title',
  //   key: 'title',
  //   // width: 120,
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Name',
    key: 'name',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Route Path',
    key: 'path',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Component Path',
    key: 'componentPath',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Sort Order',
    key: 'sort',
    width: 60,
  },
  {
    title: 'Visible',
    key: 'visibleName',
    width: 60,
    render: (row) => {
      return h(NTag, {
        type: row.visible ? 'primary' : 'error',
      }, () => row.visibleName)
    },
  },
  {
    title: 'Cache',
    key: 'keepAliveName',
    width: 60,
    render: (row) => {
      return h(NTag, {
        type: row.keepAlive ? 'primary' : 'error',
      }, () => row.keepAliveName)
    },
  },
  {
    title: 'Pinned',
    key: 'pinedName',
    width: 60,
    render: (row) => {
      return h(NTag, {
        type: row.pined ? 'primary' : 'error',
      }, () => row.pinedName)
    },
  },
  // {
  //   title: 'Extra Information',
  //   key: 'exJson',
  // },
  {
    title: 'Actions',
    key: 'action',
    width: 184,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        actionIconBtn({ kind: 'edit', onClick: () => formRef.value.doOpen(row, true) }),
        actionIconBtn({ kind: 'permission', disabled: row.menuType === 0, onClick: () => permissionRef.value.doOpen(row) }),
        actionIconBtn({ kind: 'detail', onClick: () => detailRef.value.doOpen(row) }),
        actionDeleteBtn(() => deleteHandle(row)),
      ])
    },
  },
]

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: 'Created At', value: 'createTime' },
    { label: 'Updated At', value: 'updateTime' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: 'Ascending', value: 'ASCEND' },
  { label: 'Descending', value: 'DESCEND' },
])
const displayKey = ref<string[]>(
  columns
    .filter(col => 'key' in col && col.key !== 'selection' && col.key !== 'action')
    .map((col: any) => col.key as string),
)
const columnDisplayOptions = computed(() => {
  const filteredColumns = columns
    .filter(col => 'key' in col)
    .filter(col => col.key !== 'action' && col.key !== 'selection')

  return filteredColumns.map((col, index) => ({
    label: col.title as string,
    value: col.key as string,
    disabled: index === 0,
  }))
})

const filteredColumns = computed(() => {
  return columns.filter((col: any) => {
    if (col.type === 'selection' || col.key === 'action')
      return true
    return 'key' in col ? displayKey.value.includes(col.key as string) : false
  })
})

const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: 'sort',
  sortOrder: 'ASCEND',
  keyword: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const { sysMenuPage, sysMenuDelete, sysMenuTreeList } = useSysMenuFetch()
const loading = ref(false)
async function loadData() {
  loading.value = true
  // const { data } = await sysMenuPage(pageParam.value)
  const { data } = await sysMenuTreeList()
  if (data) {
    pageData.value = data
    loading.value = false
    console.log(data)
  }
}

loadData()

async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await sysMenuDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await sysMenuDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
    checkedRowKeys.value = []
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NCard size="small">
      <NSpace vertical>
        <NSpace align="center">
          <NSpace align="center">
            <NFormItem :show-feedback="false" label="Keywords" label-placement="left">
              <NInput v-model:value="pageParam.keyword" placeholder="Enter keywords" clearable @clear="resetHandle" />
            </NFormItem>
            <NSpace align="center">
              <NButton type="primary" @click="loadData">
                <template #icon>
                  <IconParkOutlineSearch />
                </template>
                Search
              </NButton>
              <NButton type="warning" @click="resetHandle">
                <template #icon>
                  <IconParkOutlineRefresh />
                </template>
                Reset
              </NButton>
            </NSpace>
          </NSpace>
        </NSpace>
        <NSpace align="center" justify="space-between">
          <NSpace align="center">
            <NButton type="primary" @click="formRef.doOpen(null, false)">
              <template #icon>
                <IconParkOutlinePlus />
              </template>
              Create
            </NButton>
            <NPopconfirm v-if="checkedRowKeys.length > 0" @positive-click="deleteBatchHandle">
              <template #default>
                Confirm delete
              </template>
              <template #trigger>
                <NButton type="error">
                  Delete
                </NButton>
              </template>
            </NPopconfirm>
          </NSpace>
          <NSpace align="center">
            <NFormItem :show-feedback="false" label-placement="left">
              <NButton text @click="loadData">
                <template #icon>
                  <IconParkOutlineRefresh />
                </template>
              </NButton>
            </NFormItem>
            <NFormItem :show-feedback="false" label-placement="left">
              <NPopselect
                v-model:value="displayKey"
                :options="columnDisplayOptions"
                multiple
                @update:value="loadData"
              >
                <NButton text>
                  <template #icon>
                    <IconParkOutlineEyes />
                  </template>
                </NButton>
              </NPopselect>
            </NFormItem>
            <!-- <NFormItem :show-feedback="false" label-placement="left">
              <NPopselect
                v-model:value="pageParam.sortField"
                :options="columnSortFieldOptions"
                @update:value="loadData"
              >
                <NButton text>
                  <template #icon>
                    <IconParkOutlineSortOne />
                  </template>
                </NButton>
              </NPopselect>
            </NFormItem>
            <NFormItem :show-feedback="false" label-placement="left">
              <NPopselect
                v-model:value="pageParam.sortOrder"
                :options="sortOrderOptions"
                @update:value="loadData"
              >
                <NButton text>
                  <template #icon>
                    <IconParkOutlineSort />
                  </template>
                </NButton>
              </NPopselect>
            </NFormItem> -->
          </NSpace>
        </NSpace>
      </NSpace>
    </NCard>
    <NCard size="small" class="flex-1">
      <NDataTable
        v-model:checked-row-keys="checkedRowKeys"
        :columns="filteredColumns"
        :data="pageData"
        :bordered="false"
        :row-key="(row: any) => row.id"
        :loading="loading"
        :scroll-x="1400"
        flex-height
        class="flex-1 h-full"
      />
      <!-- <template #action>
        <NSpace align="center" justify="space-between" class="w-full">
          <NSpace align="center">
            <NP type="info" show-icon>
              Current: {{ pageData?.length }} records
            </NP>
            <NP type="info" show-icon>
              Selected: {{ checkedRowKeys.length }} rows
            </NP>
          </NSpace>
          <NPagination
            v-model:page="pageParam.current"
            v-model:page-size="pageParam.size"
            class="flex justify-end"
            :page-count="pageData ? Number(pageData.pages) : 0"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </NSpace>
      </template> -->
    </NCard>

    <Form ref="formRef" @submit="loadData" />
    <Detail ref="detailRef" @submit="loadData" />
    <Permission ref="permissionRef" @submit="loadData" />
  </div>
</template>

<style scoped>
</style>
