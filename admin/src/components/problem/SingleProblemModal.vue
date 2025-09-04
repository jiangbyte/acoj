<script lang="ts" setup>
import { useProProblemFetch } from '@/composables'
import type { DataTableColumns } from 'naive-ui'

// 定义 props 和 emits
const props = defineProps<{
  value?: string // 接收父组件传递的值
  title?: string
}>()

const emit = defineEmits<{
  (e: 'update:value', value: string): void // 定义更新事件
  (e: 'update:select', value: string): void // 定义更新事件
  // 更新标题
  (e: 'update:title', value: string): void
}>()

const columns: DataTableColumns<any> = [
  {
    type: 'selection',
    multiple: false,
  },
  {
    title: '分类',
    key: 'categoryName',
    ellipsis: true,
  },
  {
    title: '标题',
    key: 'title',
    ellipsis: true,
  },
  {
    title: '来源',
    key: 'source',
    ellipsis: true,
  },
  //   {
  //     title: '链接',
  //     key: 'url',
  //     ellipsis: true,
  //   },
  {
    title: '时间限制',
    key: 'maxTime',
  },
  {
    title: '内存限制',
    key: 'maxMemory',
  },
  {
    title: '阈值',
    key: 'threshold',
  },
  // {
  //   title: '用例',
  //   key: 'testCase',
  // },
  // {
  //   title: '开放语言',
  //   key: 'allowedLanguages',
  // },
  {
    title: '难度',
    key: 'difficultyName',
  },
  {
    title: '公开',
    key: 'isPublicName',
  },
  {
    title: 'LLM启用',
    key: 'isLlmEnhancedName',
  },
  {
    title: '使用模板',
    key: 'useTemplateName',
  },
  // {
  //   title: '模板代码',
  //   key: 'codeTemplate',
  // },
//   {
//     title: '解决',
//     key: 'solved',
//   },
]
const showModal = ref(false)
const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const { proProblemPage } = useProProblemFetch()
const loading = ref(false)
const checkedRowKeys = ref<string[]>([])
async function loadData() {
  loading.value = true
  const { data } = await proProblemPage(pageParam.value)
  if (data) {
    pageData.value = data
    loading.value = false
  }
}

loadData()

function doOpen() {
  showModal.value = true
  loadData()
}
defineExpose({
  doOpen,
})

function toggleShow(show: boolean) {
  showModal.value = show
  if (!show) {
    checkedRowKeys.value = []
  }
}

function handleSubmit() {
  if (checkedRowKeys.value.length > 0) {
    emit('update:value', checkedRowKeys.value[0]) // 发送更新事件
    emit('update:select', checkedRowKeys.value[0]) // 发送更新事件
    // 更新标题
    const selectedItem = pageData.value?.records.find((item: any) => item.id === checkedRowKeys.value[0])
    emit('update:title', selectedItem?.title)
  }
  toggleShow(false)
}

// 添加监听，当选择变化时立即更新
watch(checkedRowKeys, (newVal) => {
  if (newVal.length > 0) {
    emit('update:value', newVal[0])
    // emit('update:select', newVal[0])
  }
}, { deep: true })
</script>

<template>
  <n-modal
    v-model:show="showModal"
    class="w-250 h-150"
    :mask-closable="false"
    preset="card"
    @update:show="toggleShow"
  >
    <template #header>
      <n-space vertical>
        <n-space align="center">
          <n-form-item :show-feedback="false" label="关键词" label-placement="left">
            <n-input v-model:value="pageParam.keyword" placeholder="请输入关键词" />
          </n-form-item>
          <n-space align="center">
            <n-button type="primary" @click="loadData">
              <template #icon>
                <icon-park-outline-search />
              </template>
              搜索
            </n-button>
            <n-button type="warning" @click="resetHandle">
              <template #icon>
                <icon-park-outline-refresh />
              </template>
              重置
            </n-button>
          </n-space>
        </n-space>
      </n-space>
    </template>
    <NDataTable
      v-model:checked-row-keys="checkedRowKeys"
      :columns="columns"
      :data="pageData?.records"
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
            当前数据 {{ pageData?.records.length }} 条
          </NP>
          <NP type="info" show-icon>
            选中了 {{ checkedRowKeys.length }} 行
          </NP>
        </NSpace>
        <NPagination
          v-model:page="pageParam.current"
          v-model:page-size="pageParam.size"
          :page-count="pageData ? Number(pageData.pages) : 0"
          @update:page="loadData"
          @update:page-size="loadData"
        />

        <n-space align="center" justify="end">
          <n-button @click="toggleShow(false)">
            取消
          </n-button>
          <n-button type="primary" @click="handleSubmit">
            确认
          </n-button>
        </n-space>
      </NSpace>
    </template>
  </n-modal>
</template>

<style scoped>

</style>
