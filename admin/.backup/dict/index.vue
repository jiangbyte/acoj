<script lang="ts" setup>
import { NButton, NPopconfirm, NSpace, useMessage } from 'naive-ui'
import type { DataTableColumns, TreeOption } from 'naive-ui'
import Form from './dataform.vue'
import Detail from './datadetail.vue'
import { useSysDictDataFetch, useSysDictTypeFetch } from '@/composables'

const { sysDictTypeTreeOptions } = useSysDictTypeFetch()
const { sysDictDataListById } = useSysDictDataFetch()

// 字典类型树数据
const treeData = ref<any[]>([])
const dataTypeKeyword = ref('')
async function loadData() {
  const { data } = await sysDictTypeTreeOptions({
    keyword: dataTypeKeyword.value,
  })
  if (data) {
    treeData.value = data
  }
}
loadData()
function dataTypeFilterReset() {
  dataTypeKeyword.value = ''
  loadData()
}

// 字典值列表数据
const listData = ref([])

// 选中的树节点
const selectedTreeKeys = ref<string[]>(['user-type'])
const treeFilterValue = ref('')
const listFilterValue = ref('')

const message = useMessage()

// 树节点渲染
function renderTreeLabel({ option }: { option: TreeOption }) {
  return h(
    'span',
    {
      style: {
        display: 'inline-flex',
        alignItems: 'center',
      },
    },
    [
      h('span', { style: { marginLeft: '8px' } }, option.label as string),
    ],
  )
}

const checkedRowKeys = ref<string[]>([])
// 处理树节点选择
async function handleTreeSelect(keys: string[]) {
  selectedTreeKeys.value = keys
  if (keys.length === 0) {
    listData.value = []
    return
  }
  const { data } = await sysDictDataListById({ id: keys[0] })
  listData.value = data
  checkedRowKeys.value = []
}
const formRef = ref()
const detailRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: '类型',
    key: 'typeIdName',
  },
  {
    title: '标签',
    key: 'dictLabel',
  },
  {
    title: '值',
    key: 'dictValue',
  },
  {
    title: '排序',
    key: 'sort',
  },
  {
    title: '默认状态',
    key: 'isDefault',
    render(row: any) {
      return row.isDefault ? '是' : '否'
    },
  },
  {
    title: '操作',
    key: 'action',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        h(NButton, {
          type: 'primary',
          size: 'small',
          onClick: () => formRef.value.doOpen(row, true),
        }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => detailRef.value.doOpen(row) }, () => '详情'),
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
const { sysDictDataDelete } = useSysDictDataFetch()
async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await sysDictDataDelete(param)
  if (success) {
    window.$message.success('删除成功')
    await refresh()
  }
}

async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await sysDictDataDelete(param)
  if (success) {
    window.$message.success('删除成功')
    await loadData()
    checkedRowKeys.value = []
    await refresh()
  }
}
async function refresh() {
  await loadData()
  await handleTreeSelect(selectedTreeKeys.value)
}
// 新增字典值
function handleAdd() {
  message.info('点击了新增按钮')
  // 这里可以打开新增对话框
}

// 编辑字典值
function handleEdit(row: any) {
  message.info(`编辑: ${row.label}`)
  // 这里可以打开编辑对话框
}

// 删除字典值
function handleDelete(row: any) {
  message.warning(`删除: ${row.label}`)
  // 这里可以弹出确认对话框并执行删除操作
}
</script>

<template>
  <div class="h-full">
    <NSplit direction="horizontal" :default-size="0.2">
      <template #1>
        <NCard class="h-[calc(100vh-2.5rem-5rem)]" title="字典类型" :bordered="false" size="small">
          <NSpace vertical>
            <NInputGroup>
              <NInput
                v-model:value="dataTypeKeyword"
                placeholder="输入关键字过滤"
                clearable
                @clear="dataTypeFilterReset"
              />
              <NButton type="primary" @click="loadData">
                <template #icon>
                  <IconParkOutlineSearch />
                </template>
                搜索
              </NButton>
            </NInputGroup>
            <NScrollbar>
              <NTree
                show-line
                block-line
                key-field="value"
                :data="treeData"
                :pattern="treeFilterValue"
                :render-label="renderTreeLabel"
                :selected-keys="selectedTreeKeys"
                @update:selected-keys="handleTreeSelect"
              />
            </NScrollbar>
          </NSpace>
        </NCard>
      </template>
      <template #2>
        <NCard title="字典值列表" class="h-[calc(100vh-2.5rem-5rem)]" size="small" :bordered="false">
          <NSpace vertical>
            <NSpace>
              <NInput
                v-model:value="listFilterValue"
                placeholder="输入关键字过滤"
                clearable
                style="width: 300px"
              />
              <NButton type="primary" @click="loadData">
                <template #icon>
                  <IconParkOutlineSearch />
                </template>
                搜索
              </NButton>
              <NButton type="primary" @click="formRef.doOpen(null, false)">
                <template #icon>
                  <IconParkOutlinePlus />
                </template>
                新增
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
            <NScrollbar>
              <NDataTable
                v-model:checked-row-keys="checkedRowKeys"
                :columns="columns"
                :data="listData"
                :bordered="false"
                :row-key="(row) => row.id"
              />
            </NScrollbar>
          </NSpace>
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
      </template>
    </NSplit>

    <Form ref="formRef" @submit="refresh" />
    <Detail ref="detailRef" @submit="refresh" />
  </div>
</template>

<style scoped>

</style>
