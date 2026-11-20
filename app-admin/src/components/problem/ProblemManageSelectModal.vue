<script lang="ts" setup>
import { useDataContestProblemFetch, useDataProblemFetch, useDataSetProblemFetch } from '@/composables/v1'
import { NButton, NDataTable, NDrawer, NDrawerContent, NInput, NMessageProvider, NSpace, NTag, useMessage } from 'naive-ui'

const emit = defineEmits(['close'])
const show = ref(false)
const message = useMessage()

const moduleTypeRef = ref()
const currentId = ref('') // 当前题集或竞赛ID

// 题目列表数据
const problemRecords = ref()
// 已选题目数据
const selectedProblems = ref([])

// 表格列配置
const problemColumns = [
  {
    title: '题目名称',
    key: 'title',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '来源',
    key: 'source',
    width: 120,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '难度',
    key: 'difficultyName',
  },
  {
    title: '阈值',
    key: 'threshold',
  },
  {
    title: '公开',
    key: 'isPublicName',
    render: (row: any) => {
      return h(NTag, {
        type: row.isPublic ? 'primary' : 'error',
      }, () => row.isPublicName)
    },
  },
  {
    title: '操作',
    key: 'actions',
    render: (row: any) => h(NButton, {
      size: 'small',
      type: 'primary',
      onClick: () => handleAddProblem(row),
    }, { default: () => '添加' }),
  },
]

const selectedSetColumns = [
  {
    title: '题目名称',
    key: 'problemIdName',
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: row => h(NButton, {
      size: 'small',
      type: 'error',
      onClick: () => handleRemoveProblem(row.id),
    }, { default: () => '删除' }),
  },
]

const selectedContestColumns = [
  {
    title: '题目编号',
    key: 'problemCode',
    width: 100,
    render: row => h(NInput, {
      value: row.problemCode,
      disabled: true,
      onUpdateValue: value => handleCodeChange(row.id, value),
    }),
  },
  {
    title: '题目名称',
    key: 'problemIdName',
  },
  // {
  //   title: '排序',
  //   key: 'sort',
  //   width: 100,
  //   render: row => h(NInputNumber, {
  //     defaultValue: row.sort,
  //     min: 0,
  //     onUpdateValue: value => handleSortChange(row.id, value),
  //   }),
  // },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: row => h(NButton, {
      size: 'small',
      type: 'error',
      onClick: () => handleRemoveProblem(row.id),
    }, { default: () => '删除' }),
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

  // 加载题目列表
  loadProblemDatas()
  loadContestProblemDatas()
  // 根据类型加载已选题目
  if (moduleType === 'SET') {
    loadSetProblemDatas()
  }
}

// 修改题目编号
async function handleCodeChange(id, value) {
  const problem = selectedContestProblems.value.find(item => item.id === id)
  if (problem) {
    problem.problemCode = value.toUpperCase()

    // 可以添加保存到后端的逻辑
    // await saveProblemCode(problem)
  }
}
// // 修改排序
// async function handleSortChange(id, value) {
//   const problem = selectedContestProblems.value.find(item => item.id === id)
//   if (problem) {
//     problem.sort = value
//     // 重新排序数组
//     selectedContestProblems.value.sort((a, b) => Number(a.sort) - Number(b.sort))

//     useDataSetProblemFetch().dataSetProblemEdit(problem).then(() => {
//       loadContestProblemDatas()
//       message.success('更改成功')
//     })
//   }
// }
// 生成下一个题目编号
function generateNextCode() {
  if (selectedContestProblems.value.length === 0)
    return 'A'

  const lastCode = selectedContestProblems.value[selectedContestProblems.value.length - 1].problemCode
  if (!lastCode)
    return 'A'

  return String.fromCharCode(lastCode.charCodeAt(0) + 1)
}
// 添加题目
async function handleAddProblem(row: any) {
  if (moduleTypeRef.value === 'SET') {
    if (selectedSetProblems.value.some((item: any) => item.problemId === row.id)) {
      message.warning('该题目已添加')
      return
    }
    const newProblem = {
      setId: currentId.value,
      problemId: row.id,
      sort: selectedSetProblems.value.length + 1,
    }
    useDataSetProblemFetch().dataSetProblemAdd(newProblem).then(() => {
      loadSetProblemDatas()
      message.success('添加成功')
    })
  }
  else if (moduleTypeRef.value === 'CONTEST') {
    if (selectedContestProblems.value.some((item: any) => item.problemId === row.id)) {
      message.warning('该题目已添加')
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
      message.success('添加成功')
    })
  }
}

// 删除题目
async function handleRemoveProblem(sid: any) {
  if (moduleTypeRef.value === 'SET') {
    const param = [{
      id: sid,
    }]
    useDataSetProblemFetch().dataSetProblemDelete(param).then(() => {
      loadSetProblemDatas()
      message.success('删除成功')
    })
  }
  else if (moduleTypeRef.value === 'CONTEST') {
    const param = [{
      id: sid,
    }]
    useDataContestProblemFetch().dataContestProblemDelete(param).then(() => {
      loadContestProblemDatas()
      message.success('删除成功')
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
      <NDrawerContent :title="`题目管理 - ${moduleTypeRef === 'SET' ? '题集' : '竞赛'}`">
        <n-grid
          cols="2"
          :x-gap="24"
          :y-gap="24"
          responsive="screen"
          class="flex-1 h-full"
        >
          <!-- 右侧边栏 -->
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
                    label: `${(i + 1) * 10} 每页`,
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
              关闭
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
  </NMessageProvider>
</template>

<style scoped>

</style>
