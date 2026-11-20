<script lang="ts" setup>
import { actionDeleteBtn, actionIconBtn } from '@/utils'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NPopconfirm, NSpace, NSplit } from 'naive-ui'
import { useSysDictFetch } from '@/composables/v1'
import DictForm from './dictform.vue'
import Form from './form.vue'
import Detail from './detail.vue'

const formRef = ref()
const dictformRef = ref()
const detailRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'Type Name',
    key: 'typeLabel',
  },
  {
    title: 'Dictionary Type',
    key: 'dictType',
  },
  {
    title: 'Dictionary Label',
    key: 'dictLabel',
  },
  {
    title: 'Dictionary Value',
    key: 'dictValue',
  },
  {
    title: 'Sort Order',
    key: 'sortOrder',
  },
  {
    title: 'Actions',
    key: 'action',
    width: 140,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        actionIconBtn({ kind: 'edit', onClick: () => formRef.value.doOpen(null, row, true) }),
        actionIconBtn({ kind: 'detail', onClick: () => detailRef.value.doOpen(row, true) }),
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
  sortField: 'id',
  sortOrder: 'ASCEND',
  keyword: '',
})

const { sysDictDelete, sysDictTree, sysDictListGroup } = useSysDictFetch()
const listData = ref([])
const treeData = ref<any[]>([])
const loading = ref(false)
const treeKeyword = ref('')
const typeKey = ref<string[]>([''])
const typeCode = ref()
async function loadData() {
  const { data: treeDataD } = await sysDictTree({
    keyword: treeKeyword.value,
  })
  if (treeDataD) {
    treeData.value = treeDataD
  }
}

loadData()
function resetHandle() {
  treeKeyword.value = ''
  typeKey.value = []
  typeCode.value = null as any
  loadData()
}
async function loadListData(typeKey: string) {
  const { data } = await sysDictListGroup({ dictType: typeKey })
  if (data) {
    listData.value = data
  }
}
async function handleTreeSelect(keys: string[]) {
  console.log(keys)
  // For each key, keep the text before the colon when present
  const newKeys = keys.map((key) => {
    if (key.includes(':')) {
      return key.split(':')[0]
    }
    return key
  })
  if (newKeys.length === 0) {
    listData.value = []
    return
  }
  typeCode.value = newKeys[0]
  loadListData(typeCode.value)
}
function refresh() {
  loadData()
  loadListData(typeCode.value)
}

async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await sysDictDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    refresh()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await sysDictDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    refresh()
    checkedRowKeys.value = []
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NSplit direction="horizontal" :default-size="0.2" :max="0.8" :min="0.2">
      <template #1>
        <div class="flex flex-col h-full w-full">
          <NCard size="small">
            <NSpace vertical>
              <NInputGroup>
                <NInput
                  v-model:value="treeKeyword"
                  placeholder="Enter keywords"
                  clearable
                  @clear="resetHandle"
                />
                <NButton type="primary" @click="loadData">
                  <template #icon>
                    <IconParkOutlineSearch />
                  </template>
                  Search
                </NButton>
              </NInputGroup>
              <NScrollbar class="h-[calc(100vh-2.5rem-11.5rem)]">
                <NTree
                  show-line
                  block-line
                  key-field="value"
                  label-field="label"
                  :data="treeData"
                  :indent="12"
                  :selected-keys="typeKey"
                  @update:selected-keys="handleTreeSelect"
                />
              </NScrollbar>
            </NSpace>
            <template #header>
              <NButton type="primary" @click="dictformRef.doOpen(null, false)">
                <template #icon>
                  <IconParkOutlinePlus />
                </template>
                Add
              </NButton>
            </template>
          </NCard>
        </div>
      </template>

      <template #2>
        <div class="flex flex-col h-full w-full">
          <NCard size="small">
            <NSpace vertical>
              <NSpace align="center" justify="space-between">
                <NSpace align="center">
                  <NButton type="primary" @click="formRef.doOpen(typeCode, null, false)">
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
            <n-alert type="warning" class="mb-3">
              Do not change business dictionaries unless necessary; incorrect changes may cause system errors.
            </n-alert>
            <NDataTable
              v-model:checked-row-keys="checkedRowKeys"
              :columns="filteredColumns"
              :data="listData"
              :bordered="false"
              :row-key="(row: any) => row.id"
              :loading="loading"
              flex-height
              :scroll-x="1400"
              class="flex-1 h-full"
            />
            <template #action>
              <NSpace align="center" justify="space-between" class="w-full">
                <NSpace align="center">
                  <NP type="info" show-icon>
                    Current: {{ listData.length }} records
                  </NP>
                  <NP type="info" show-icon>
                    Selected: {{ checkedRowKeys.length }} rows
                  </NP>
                </NSpace>
              </NSpace>
            </template>
          </NCard>
        </div>
      </template>
    </NSplit>

    <DictForm ref="dictformRef" @submit="refresh" />
    <Form ref="formRef" @submit="refresh" />
    <Detail ref="detailRef" @submit="refresh" />
  </div>
</template>

<style scoped>

</style>
