<script lang="ts" setup>
import { useDataContestProblemFetch, useDataProblemFetch, useDataSetProblemFetch } from '@/composables/v1'
import { NButton, NDataTable, NDrawer, NDrawerContent, NInput, NMessageProvider, NSpace, NTag, useMessage } from 'naive-ui'
const emit = defineEmits(['close'])
const show = ref(false)
const message = useMessage()

const moduleTypeRef = ref()
const currentId = ref('') // Current set or contest ID

// Problem data
const problemRecords = ref()
// Selected problem data
const selectedProblems = ref([])

// Table column configuration
const problemColumns = [
  {
    title: 'Problem Name',
    key: 'title',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Source',
    key: 'source',
    width: 120,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Difficulty',
    key: 'difficultyName',
  },
  {
    title: 'Threshold',
    key: 'threshold',
  },
  {
    title: 'Public',
    key: 'isPublicName',
    render: (row: any) => {
      return h(NTag, {
        type: row.isPublic ? 'primary' : 'error',
      }, () => row.isPublicName)
    },
  },
  {
    title: 'Actions',
    key: 'actions',
    render: (row: any) => h(NButton, {
      size: 'small',
      type: 'primary',
      onClick: () => handleAddProblem(row),
    }, { default: () => 'Add' }),
  },
]

const selectedSetColumns = [
  {
    title: 'Problem Name',
    key: 'problemIdName',
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 80,
    render: row => h(NButton, {
      size: 'small',
      type: 'error',
      onClick: () => handleRemoveProblem(row.id),
    }, { default: () => 'Delete' }),
  },
]

const selectedContestColumns = [
  {
    title: 'Problem Number',
    key: 'problemCode',
    width: 100,
    render: row => h(NInput, {
      value: row.problemCode,
      disabled: true,
      onUpdateValue: value => handleCodeChange(row.id, value),
    }),
  },
  {
    title: 'Problem Name',
    key: 'problemIdName',
  },
  // {
  //   title: 'Sort Order',
  //   key: 'sort',
  //   width: 100,
  //   render: row => h(NInputNumber, {
  //     defaultValue: row.sort,
  //     min: 0,
  //     onUpdateValue: value => handleSortChange(row.id, value),
  //   }),
  // },
  {
    title: 'Actions',
    key: 'actions',
    width: 80,
    render: row => h(NButton, {
      size: 'small',
      type: 'error',
      onClick: () => handleRemoveProblem(row.id),
    }, { default: () => 'Delete' }),
  },
]

function doClose() {
  emit('close')
  show.value = false
  problemRecords.value = {}
  selectedProblems.value = []
  currentId.value = ''
}

const problemPageParam = ref({
  current: 1,
  size: 20,
  sortField: 'id',
  sortOrder: 'ASCEND',
  keyword: '',
})
const selectedSetProblems = ref([])
const selectedContestProblems = ref([])
function loadProblemDatas() {
  useDataProblemFetch().dataProblemPage(problemPageParam.value).then(({ data }) => {
    problemRecords.value = data
  })
}

function loadSetProblemDatas() {
  useDataSetProblemFetch().dataSetProblemManageList({ setId: currentId.value }).then(({ data }) => {
    selectedSetProblems.value = data
  })
}
function loadContestProblemDatas() {
  useDataContestProblemFetch().dataContestProblemList({ contestId: currentId.value }).then(({ data }) => {
    selectedContestProblems.value = data
    console.log(data)
  })
}

async function doOpen(row: any, moduleType: string) {
  moduleTypeRef.value = moduleType
  currentId.value = row.id
  show.value = true

  // Load problems
  loadProblemDatas()
  loadContestProblemDatas()
  // Load selected problems by type
  if (moduleType === 'SET') {
    loadSetProblemDatas()
  }
}

// Change the problem number
async function handleCodeChange(id, value) {
  const problem = selectedContestProblems.value.find(item => item.id === id)
  if (problem) {
    problem.problemCode = value.toUpperCase()

    // Backend persistence can be added here
    // await saveProblemCode(problem)
  }
}
// // Change the sort order
// async function handleSortChange(id, value) {
//   const problem = selectedContestProblems.value.find(item => item.id === id)
//   if (problem) {
//     problem.sort = value
//     // Reorder the array
//     selectedContestProblems.value.sort((a, b) => Number(a.sort) - Number(b.sort))

//     useDataSetProblemFetch().dataSetProblemEdit(problem).then(() => {
//       loadContestProblemDatas()
//       message.success('Updated successfully')
//     })
//   }
// }
// Generate the next problem number
function generateNextCode() {
  if (selectedContestProblems.value.length === 0)
    return 'A'

  const lastCode = selectedContestProblems.value[selectedContestProblems.value.length - 1].problemCode
  if (!lastCode)
    return 'A'

  return String.fromCharCode(lastCode.charCodeAt(0) + 1)
}
// Add a problem
async function handleAddProblem(row: any) {
  if (moduleTypeRef.value === 'SET') {
    if (selectedSetProblems.value.some((item: any) => item.problemId === row.id)) {
      message.warning('Problem already added')
      return
    }
    const newProblem = {
      setId: currentId.value,
      problemId: row.id,
      sort: selectedSetProblems.value.length + 1,
    }
    useDataSetProblemFetch().dataSetProblemAdd(newProblem).then(() => {
      loadSetProblemDatas()
      message.success('Added successfully')
    })
  }
  else if (moduleTypeRef.value === 'CONTEST') {
    if (selectedContestProblems.value.some((item: any) => item.problemId === row.id)) {
      message.warning('Problem already added')
      return
    }
    const newProblem = {
      contestId: currentId.value,
      problemId: row.id,
      problemCode: generateNextCode(),
      sort: selectedContestProblems.value.length + 1,
    }
    console.log(newProblem)

    useDataContestProblemFetch().dataContestProblemAdd(newProblem).then(() => {
      loadContestProblemDatas()
      message.success('Added successfully')
    })
  }
}

// DeleteProblem
async function handleRemoveProblem(sid: any) {
  if (moduleTypeRef.value === 'SET') {
    const param = [{
      id: sid,
    }]
    useDataSetProblemFetch().dataSetProblemDelete(param).then(() => {
      loadSetProblemDatas()
      message.success('Deleted successfully')
    })
  }
  else if (moduleTypeRef.value === 'CONTEST') {
    const param = [{
      id: sid,
    }]
    useDataContestProblemFetch().dataContestProblemDelete(param).then(() => {
      loadContestProblemDatas()
      message.success('Deleted successfully')
    })
  }
}

defineExpose({
  doOpen,
})
</script>

<template>
  <NMessageProvider>
    <NDrawer v-model:show="show" placement="right" width="1200" @after-leave="doClose">
      <NDrawerContent :title="`Problem Management - ${moduleTypeRef === 'SET' ? 'Problem Set' : 'Contest'}`">
        <n-grid
          cols="2"
          :x-gap="24"
          :y-gap="24"
          responsive="screen"
          class="flex-1 h-full"
        >
          <!-- Right sidebar -->
          <n-gi span="1">
            <n-card class="flex-1 h-full" size="small">
              <NDataTable
                :columns="problemColumns"
                :data="problemRecords?.records"
                flex-height
                class="flex-1 h-full"
              />
              <template #footer>
                <NPagination
                  v-model:page="problemPageParam.current"
                  v-model:page-size="problemPageParam.size"
                  class="flex justify-end"
                  :page-count="problemRecords ? Number(problemRecords.pages) : 0"
                  show-size-picker
                  :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                    label: `${(i + 1) * 10} per page`,
                    value: (i + 1) * 10,
                  }))"
                  :page-slot="5"
                  @update:page="loadProblemDatas"
                  @update:page-size="loadProblemDatas"
                />
              </template>
            </n-card>
          </n-gi>
          <n-gi span="1">
            <n-card class="flex-1 h-full" size="small">
              <NDataTable
                v-if="moduleTypeRef === 'SET'"
                :columns="selectedSetColumns"
                :data="selectedSetProblems"
                :bordered="false"
                flex-height
                class="flex-1 h-full"
              />
              <NDataTable
                v-if="moduleTypeRef === 'CONTEST'"
                :columns="selectedContestColumns"
                :data="selectedContestProblems"
                :bordered="false"
                flex-height
                class="flex-1 h-full"
              />
            </n-card>
          </n-gi>
        </n-grid>

        <template #footer>
          <NSpace align="center" justify="end">
            <NButton @click="doClose">
              Close
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
  </NMessageProvider>
</template>

<style scoped>

</style>
