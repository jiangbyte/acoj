<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NPopconfirm, NSpace, NSplit } from 'naive-ui'
import { useSysDictFetch } from '@/composables'
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
    title: '类型名称',
    key: 'typeLabel',
  },
  {
    title: '字典类型',
    key: 'dictType',
  },
  {
    title: '字典标签',
    key: 'dictLabel',
  },
  {
    title: '字典值',
    key: 'dictValue',
  },
  {
    title: '排序',
    key: 'sortOrder',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        h(NButton, {
          type: 'primary',
          size: 'small',
          onClick: () => formRef.value.doOpen(row, true),
        }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => detailRef.value.doOpen(row, true) }, () => '详情'),
        h(NPopconfirm, {
          onPositiveClick: () => deleteHandle(row),
        }, {
          default: () => '确认删除',
          trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
        }),
      ])
    },
  },
]

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: '创建时间', value: 'createTime' },
    { label: '更新时间', value: 'updateTime' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: '升序', value: 'ASCEND' },
  { label: '降序', value: 'DESCEND' },
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
  sortField: null,
  sortOrder: null,
  keyword: '',
})

const { sysDictDelete, sysDictTree, sysDictListGroup } = useSysDictFetch()
const listData = ref([])
const treeData = ref<any[]>([])
const loading = ref(false)
const treeKeyword = ref('')
const typeKey = ref<string[]>([''])
const typeCode = ref('')
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
  typeKey.value = ['']
  typeCode.value = ''
  loadData()
}
async function loadListData(typeKey: string) {
  const { data } = await sysDictListGroup({ dictType: typeKey })
  if (data) {
    listData.value = data
  }
}
async function handleTreeSelect(keys: string[]) {
  // 对于keys的每一个key，如果其中有:符号，截取前面的字符串
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
    window.$message.success('删除成功')
    refresh()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await sysDictDelete(param)
  if (success) {
    window.$message.success('删除成功')
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
                  placeholder="输入关键字过滤"
                  clearable
                  @clear="resetHandle"
                />
                <NButton type="primary" @click="loadData">
                  <template #icon>
                    <IconParkOutlineSearch />
                  </template>
                  搜索
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
                新增
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
                  <NButton type="primary" @click="formRef.doOpen(null, false)">
                    <template #icon>
                      <IconParkOutlinePlus />
                    </template>
                    创建
                  </NButton>
                  <NPopconfirm v-if="checkedRowKeys.length > 0" @positive-click="deleteBatchHandle">
                    <template #default>
                      确认删除
                    </template>
                    <template #trigger>
                      <NButton type="error">
                        删除
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
                  <NFormItem :show-feedback="false" label-placement="left">
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
                  </NFormItem>
                </NSpace>
              </NSpace>
            </NSpace>
          </NCard>
          <NCard size="small" class="flex-1">
            <n-alert type="warning" class="mb-3">
              请勿随意修改业务字典，否则可能会导致系统异常！请谨慎修改！
            </n-alert>
            <NDataTable
              v-model:checked-row-keys="checkedRowKeys"
              :columns="filteredColumns"
              :data="listData"
              :bordered="false"
              :row-key="(row: any) => row.id"
              :loading="loading"
              flex-height
              class="flex-1 h-full"
            />
            <template #action>
              <NSpace align="center" justify="space-between" class="w-full">
                <NSpace align="center">
                  <NP type="info" show-icon>
                    当前数据 {{ listData.length }} 条
                  </NP>
                  <NP type="info" show-icon>
                    选中了 {{ checkedRowKeys.length }} 行
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
