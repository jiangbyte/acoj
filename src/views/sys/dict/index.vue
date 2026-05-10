<template>
  <a-row :gutter="16">
    <a-col :span="6">
      <a-card title="字典分类" size="small">
        <a-tree
          :treeData="dictTree"
          :defaultExpandAll="true"
          @select="handleTreeSelect"
        />
      </a-card>
    </a-col>
    <a-col :span="18">
      <AppTable ref="tableRef" :columns="columns" :fetchData="fetchDictPage" :searchForm="searchForm" v-if="selectedCategory">
        <template #search>
          <a-form-item label="关键词" name="keyword"><a-input v-model:value="searchForm.keyword" /></a-form-item>
        </template>
        <template #toolbar>
          <a-button type="primary" @click="openCreate">新增</a-button>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(record.id)">详情</a-button>
              <a-button type="link" size="small" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm title="确定删除？" @confirm="handleDelete(record.id)">
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </AppTable>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchDictTree, fetchDictPage, fetchDictRemove } from '@/api/dict'
import AppTable from '@/components/AppTable.vue'

const dictTree = ref<any[]>([])
const selectedCategory = ref('')
const tableRef = ref()
const searchForm = ref({ keyword: '' })
const columns = [
  { title: '字典值', dataIndex: 'value' },
  { title: '编码', dataIndex: 'code' },
  { title: '排序', dataIndex: 'sort_code' },
  { title: '操作', key: 'action', width: 200 },
]

async function loadTree() {
  const { data } = await fetchDictTree({})
  if (data) dictTree.value = data
}

function handleTreeSelect(keys: any[]) {
  selectedCategory.value = keys[0] || ''
}

function openCreate() {}
function openDetail(_id: string) {}
function openEdit(_id: string) {}
async function handleDelete(id: string) {
  await fetchDictRemove({ ids: [id] })
  tableRef.value?.refresh()
}

onMounted(loadTree)
</script>
