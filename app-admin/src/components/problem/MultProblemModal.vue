<script lang="ts" setup>
import { useProProblemFetch } from '@/composables'
import type { DataTableColumns } from 'naive-ui'

// Define props and emits
const props = defineProps<{
  value?: string // Value passed by the parent
}>()

const emit = defineEmits<{
  (e: 'update:value', value: string): void // Define update event
  (e: 'update:select', value: string): void // Define update event
}>()

const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'Category',
    key: 'categoryName',
    ellipsis: true,
  },
  {
    title: 'Title',
    key: 'title',
    ellipsis: true,
  },
  {
    title: 'Source',
    key: 'source',
    ellipsis: true,
  },
  //   {
  //     title: 'Link',
  //     key: 'url',
  //     ellipsis: true,
  //   },
  {
    title: 'Time Limit',
    key: 'maxTime',
  },
  {
    title: 'Memory Limit',
    key: 'maxMemory',
  },
  {
    title: 'Threshold',
    key: 'threshold',
  },
  // {
  //   title: 'Test Cases',
  //   key: 'testCase',
  // },
  // {
  //   title: 'Allowed Languages',
  //   key: 'allowedLanguages',
  // },
  {
    title: 'Difficulty',
    key: 'difficultyName',
  },
  {
    title: 'Public',
    key: 'isPublicName',
  },
  {
    title: 'LLM Enabled',
    key: 'isLlmEnhancedName',
  },
  {
    title: 'Use Template',
    key: 'useTemplateName',
  },
  // {
  //   title: 'Template Code',
  //   key: 'codeTemplate',
  // },
//   {
//     title: 'Solved',
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
    emit('update:value', checkedRowKeys.value) // Emit update event
    emit('update:select', checkedRowKeys.value) // Emit update event
  }
  toggleShow(false)
}

// Watch selections and update immediately
watch(checkedRowKeys, (newVal) => {
  if (newVal.length > 0) {
    emit('update:value', newVal)
    // emit('update:select', newVal)
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
          <n-form-item :show-feedback="false" label="Keywords" label-placement="left">
            <n-input v-model:value="pageParam.keyword" placeholder="Enter keywords" />
          </n-form-item>
          <n-space align="center">
            <n-button type="primary" @click="loadData">
              <template #icon>
                <icon-park-outline-search />
              </template>
              Search
            </n-button>
            <n-button type="warning" @click="resetHandle">
              <template #icon>
                <icon-park-outline-refresh />
              </template>
              Reset
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
            Current: {{ pageData?.records.length }} records
          </NP>
          <NP type="info" show-icon>
            Selected: {{ checkedRowKeys.length }} rows
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
            Cancel
          </n-button>
          <n-button type="primary" @click="handleSubmit">
            Confirm
          </n-button>
        </n-space>
      </NSpace>
    </template>
  </n-modal>
</template>

<style scoped>

</style>
