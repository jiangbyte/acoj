<script lang="ts" setup>
import { renderSafeImage } from '@/utils'
import { useProSetFetch } from '@/composables'
import type { DataTableColumns } from 'naive-ui'
import { NImage } from 'naive-ui'
// Define props and emits
const props = defineProps<{
  setId?: string
  problemId?: string
  setTitle?: string
  problemTitle?: string
}>()

const emit = defineEmits<{
  (e: 'update:setId', value: string): void
  (e: 'update:problemId', value: string): void
  (e: 'update:setTitle', value: string): void
  (e: 'update:problemTitle', value: string): void
  (e: 'update:selected', value: boolean): void
}>()

const setColumns: DataTableColumns<any> = [
  {
    type: 'selection',
    multiple: false,
  },
  {
    title: 'Title',
    key: 'title',
    ellipsis: true,
  },
  {
    title: 'Set Type',
    key: 'setTypeName',
    ellipsis: true,
  },
  {
    title: 'Cover',
    key: 'cover',
    render: (row) => {
      return renderSafeImage({ src: row.cover, width: 50, height: 50, objectFit: 'cover', previewable: true })
    },
  },
  {
    title: 'Category',
    key: 'categoryName',
    ellipsis: true,
  },
  {
    title: 'Difficulty',
    key: 'difficultyName',
  },
  {
    title: 'LLM Enabled',
    key: 'isLlmEnhancedName',
  },
]
const problemColumns: DataTableColumns<any> = [
  {
    type: 'selection',
    multiple: false,
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
    ellipsis: true,
  },
  {
    title: 'Memory Limit',
    key: 'maxMemory',
    ellipsis: true,
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

const loading = ref(false)
const problemLoading = ref(false)
const setPageData = ref()
const setPageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
})
const setCheckedRowKeys = ref<string[]>([])
const setTitle = ref('')
async function loadData() {
  loading.value = true
  const { proSetPage } = useProSetFetch()
  proSetPage(setPageParam.value).then(({ data }) => {
    setPageData.value = data
  })
  loading.value = false
}

loadData()

const problemDataParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  setId: '',
})
const problemData = ref<any[]>([])
const problemCheckedRowKeys = ref<string[]>([])
const problemTitle = ref('')
async function loadProblemsData() {
  problemLoading.value = true
  problemCheckedRowKeys.value = []
  problemTitle.value = ''
  const { proSetProblemList } = useProSetFetch()
  proSetProblemList(problemDataParam.value).then(({ data }) => {
    problemData.value = data
  })
  problemLoading.value = false
}

function setResetHandle() {
  setPageParam.value.keyword = ''
  loadData()
}
function problemResetHandle() {
  problemDataParam.value.keyword = ''
  loadProblemsData()
}

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
    setCheckedRowKeys.value = []
    problemCheckedRowKeys.value = []
    setTitle.value = ''
    problemTitle.value = ''
  }
}

function handleSubmit() {
  emit('update:selected', setCheckedRowKeys.value.length > 0 && problemCheckedRowKeys.value.length > 0)
  toggleShow(false)
}

// Watch selections and update immediately
watch(setCheckedRowKeys, (newVal) => {
  if (newVal.length > 0) {
    // Find the selected set title
    const selectedSet = setPageData.value?.records.find((item: any) => item.id === newVal[0])
    setTitle.value = selectedSet?.title || ''
    emit('update:setId', newVal[0])
    emit('update:setTitle', selectedSet?.title || '')
    problemDataParam.value.setId = newVal[0]
    loadProblemsData()
  }
}, { deep: true })

watch(problemCheckedRowKeys, (newVal) => {
  if (newVal.length > 0) {
    // Find the selected problem
    const selectedProblem = problemData.value?.find((item: any) => item.id === newVal[0])
    problemTitle.value = selectedProblem?.title || ''
    emit('update:problemId', newVal[0])
    emit('update:problemTitle', selectedProblem?.title || '')
  }
})
</script>

<template>
  <n-modal
    v-model:show="showModal"
    class="w-300 h-150"
    :mask-closable="false"
    preset="card"
    @update:show="toggleShow"
  >
    <n-grid :x-gap="16" :y-gap="16" :cols="24" class="h-full">
      <n-gi :span="10" class="bg-red h-full">
        <n-card size="small" class="h-full">
          <template #header>
            <n-space vertical>
              <n-space align="center">
                <n-form-item :show-feedback="false" label="Keywords" label-placement="left">
                  <n-input v-model:value="setPageParam.keyword" placeholder="Enter keywords" />
                </n-form-item>
                <n-space align="center">
                  <n-button type="primary" @click="loadData">
                    <template #icon>
                      <icon-park-outline-search />
                    </template>
                    Search
                  </n-button>
                  <n-button type="warning" @click="setResetHandle">
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
            v-model:checked-row-keys="setCheckedRowKeys"
            :columns="setColumns"
            :data="setPageData?.records"
            :bordered="false"
            :row-key="(row: any) => row.id"
            :loading="loading"
            flex-height
            class="flex-1 h-full"
            :scroll-x="700"
          />
          <template #action>
            <NSpace align="center" justify="space-between" class="w-full">
              <NSpace align="center">
                <NP type="info" show-icon>
                  Current: {{ setPageData?.records.length }} records
                </NP>
                <NP type="info" show-icon>
                  Selected: {{ setCheckedRowKeys.length }} rows
                </NP>
              </NSpace>
              <NPagination
                v-model:page="setPageParam.current"
                v-model:page-size="setPageParam.size"
                :page-count="setPageData ? Number(setPageData.pages) : 0"
                @update:page="loadData"
                @update:page-size="loadData"
              />
            </NSpace>
          </template>
        </n-card>
      </n-gi>
      <n-gi :span="14" class="bg-green h-full">
        <n-card size="small" class="h-full">
          <template #header>
            <n-space vertical>
              <n-space align="center">
                <n-form-item :show-feedback="false" label="Keywords" label-placement="left">
                  <n-input v-model:value="problemDataParam.keyword" placeholder="Enter keywords" />
                </n-form-item>
                <n-space align="center">
                  <n-button type="primary" @click="loadProblemsData">
                    <template #icon>
                      <icon-park-outline-search />
                    </template>
                    Search
                  </n-button>
                  <n-button type="warning" @click="problemResetHandle">
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
            v-model:checked-row-keys="problemCheckedRowKeys"
            :columns="problemColumns"
            :bordered="false"
            :data="problemData"
            :row-key="(row: any) => row.id"
            :loading="problemLoading"
            :scroll-x="700"
            flex-height
            class="flex-1 h-full"
          />
          <template #action>
            <NSpace align="center" justify="space-between" class="w-full">
              <NSpace align="center">
                <NP type="info" show-icon>
                  Current: {{ problemData.length }} records
                </NP>
                <NP type="info" show-icon>
                  Selected: {{ problemCheckedRowKeys.length }} rows
                </NP>
              </NSpace>
              <!-- <NPagination
                @update:page="loadData"
                @update:page-size="loadData"
              /> -->
            </NSpace>
          </template>
        </n-card>
      </n-gi>
    </n-grid>
    <template #action>
      <NSpace align="center" justify="space-between" class="w-full">
        <NSpace align="center">
          <NP type="info" show-icon>
            Current set: {{ setTitle ? setTitle : '-' }} {{ setCheckedRowKeys.length > 0 ? `(${setCheckedRowKeys[0]})` : '' }}
          </NP>
          <NP type="info" show-icon>
            Selected problem: {{ problemTitle ? problemTitle : '-' }} {{ problemCheckedRowKeys.length > 0 ? `(${problemCheckedRowKeys[0]})` : '' }}
          </NP>
        </NSpace>

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
