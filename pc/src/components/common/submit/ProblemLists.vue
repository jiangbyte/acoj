<script lang="ts" setup>
import { useDataContestProblemFetch, useDataSetFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import { Icon } from '@iconify/vue'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NSpace, NTag } from 'naive-ui'

const emit = defineEmits(['close', 'submit'])

const router = useRouter()

const setProblemPageData = ref()
const contestProblemPageData = ref()
const moduleTypeRef = ref()
const moduleIdRef = ref()
const setProblemColumns: DataTableColumns<any> = [
  {
    title: '状态',
    align: 'center',
    key: 'currentUserSolved',
    width: 60,
    render: (row: any) => {
      return row.currentUserSolved
        ? h(Icon, {
            icon: 'icon-park-outline:check-one',
            style: {
              fontSize: '20px',
              color: '#52c41a',
              verticalAlign: 'middle',
            },
          })
        : null
    },
  },
  {
    title: '题目',
    key: 'title',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '难度',
    key: 'difficultyName',
    align: 'center',
    width: 60,
    render: (row) => {
      return h(NTag, { size: 'small', type: 'error' }, { default: () => row.difficultyName })
    },
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 60,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        onClick: () => {
          router.push({
            name: 'set_submit',
            query: { setId: AesCrypto.encrypt(moduleIdRef.value), problemId: AesCrypto.encrypt(row.id) },
          })
        },
      }, { default: () => '开始' })
    },
  },
]

const contestProblemColumns: DataTableColumns<any> = [
  {
    title: '状态',
    align: 'center',
    key: 'currentUserSolved',
    width: 60,
    render: (row: any) => {
      return row.currentUserSolved
        ? h(Icon, {
            icon: 'icon-park-outline:check-one',
            style: {
              fontSize: '20px',
              color: '#52c41a',
              verticalAlign: 'middle',
            },
          })
        : null
    },
  },
  {
    title: '编号',
    key: 'problemCode',
    width: 50,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '题目',
    key: 'problemIdName',
    width: 150,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 60,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        onClick: () => {
          router.push({
            name: 'contest_submit',
            query: { contestId: AesCrypto.encrypt(moduleIdRef.value), problemId: AesCrypto.encrypt(row.problemId) },
          })
        },
      }, { default: () => '开始' })
    },
  },
]

const show = ref(false)
function doClose() {
  emit('close')
  show.value = false
}
function doOpen(moduleType: string, moduleId: string) {
  show.value = true
  moduleTypeRef.value = moduleType
  moduleIdRef.value = moduleId

  console.log(moduleType)
  console.log(moduleId)

  if (moduleType === 'SET') {
    console.log('SET')
    useDataSetFetch().dataSetProblem({ id: moduleId }).then(({ data }) => {
      console.log(data)
      setProblemPageData.value = data
    })
  }
  else if (moduleType === 'CONTEST') {
    console.log('CONTEST')
    useDataContestProblemFetch().dataContestProblemList({ contestId: moduleId }).then(({ data }) => {
      contestProblemPageData.value = data
      console.log(data)
    })
  }
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="left" width="500" @after-leave="doClose">
    <NDrawerContent title="题目列表">
      <template v-if="moduleTypeRef === 'SET'">
        <n-data-table
          :columns="setProblemColumns"
          :data="setProblemPageData"
          :bordered="false"
          :row-key="(row: any) => row.id"
          class="h-full"
        />
      </template>
      <template v-if="moduleTypeRef === 'CONTEST'">
        <n-data-table
          :columns="contestProblemColumns"
          :data="contestProblemPageData"
          :bordered="false"
          :row-key="(row: any) => row.id"
          class="h-full"
        />
      </template>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
