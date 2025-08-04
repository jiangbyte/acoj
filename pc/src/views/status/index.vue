<script lang="ts" setup>
import { useProSubmitFetch, useSysDictFetch } from '@/composables'
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NSpace, NText, NTime } from 'naive-ui'
import { AesCrypto } from '@/utils'

const { proSubmitPage } = useProSubmitFetch()
const { sysDictOptions } = useSysDictFetch()

const languageOptions = ref()
const submitTypeOptions = ref()
const statusOptions = ref()

const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  problem: '',
  language: null,
  submitType: null,
  status: null,
})

const pageData = ref()

async function loadData() {
  const { data } = await proSubmitPage(pageParam.value)
  if (data) {
    pageData.value = data
  }

  // 获取下拉列表数据
  const { data: languageData } = await sysDictOptions({ dictType: 'ALLOW_LANGUAGE' })
  if (languageData) {
    languageOptions.value = languageData
  }

  const { data: submitTypeData } = await sysDictOptions({ dictType: 'SUBMIT_TYPE' })
  if (submitTypeData) {
    submitTypeOptions.value = submitTypeData
  }

  const { data: statusData } = await sysDictOptions({ dictType: 'JUDGE_STATUS' })
  if (statusData) {
    statusOptions.value = statusData
  }
}
loadData()

const columns: DataTableColumns<any> = [
  {
    title: '用户',
    key: 'user',
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            h(
              NAvatar,
              {
                size: 'small',
                round: true,
                src: row.userAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.userIdName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '题目',
    key: 'problemIdName',
  },
  {
    title: '编程语言',
    key: 'languageName',
  },
  {
    title: '执行类型',
    key: 'submitTypeName',
  },
  {
    title: '最大耗时',
    key: 'maxTime',
  },
  {
    title: '最大内存使用',
    key: 'maxMemory',
  },
  {
    title: '状态',
    key: 'statusName',
  },
  {
    title: '相似度',
    key: 'similarity',
  },
  {
    title: '检测任务',
    key: 'taskId',
  },
  {
    title: '提交时间',
    key: 'createTime',
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
    },
  },
  {
    title: '更新时间',
    key: 'updateTime',
    render(row: any) {
      return h(NTime, { time: row.createTime, type: 'relative' })
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
function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const router = useRouter()

function rowProps(row: any) {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      router.push({
        name: 'problem_submit_detail',
        query: { submit: AesCrypto.encrypt(row.id) },
      })
    },
  }
}
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-grid cols="12 m:24" :x-gap="16" :y-gap="16" responsive="screen">
      <!-- 左侧筛选区域 -->
      <n-gi span="12 m:6">
        <NSpace vertical>
          <n-card title="筛选条件" class="content-card" size="small">
            <NSpace vertical>
              <n-input
                v-model:value="pageParam.keyword"
                placeholder="搜索用户"
                clearable
                @keyup.enter="loadData"
                @clear="resetHandle"
              />
              <n-input
                v-model:value="pageParam.problem"
                placeholder="搜索题目"
                clearable
                @keyup.enter="loadData"
                @clear="resetHandle"
              />
              <n-select
                v-model:value="pageParam.language"
                placeholder="选择语言"
                clearable
                :options="languageOptions"
                @clear="resetHandle"
              />
              <n-select
                v-model:value="pageParam.submitType"
                placeholder="选择执行类型"
                clearable
                :options="submitTypeOptions"
                @clear="resetHandle"
              />
              <n-select
                v-model:value="pageParam.status"
                placeholder="选择判题状态"
                clearable
                :options="statusOptions"
                @clear="resetHandle"
              />
              <n-button
                type="primary"
                block
                @click="loadData"
              >
                搜索
              </n-button>

              <n-button
                secondary
                block
                @click="resetHandle"
              >
                重置
              </n-button>
            </NSpace>
          </n-card>
        </NSpace>
      </n-gi>

      <!-- 右侧题目列表区域 -->
      <n-gi span="12 m:18">
        <n-card
          hoverable
          class="content-card"
          size="small"
        >
          <template #header>
            <n-flex
              align="center"
              justify="space-between"
            >
              <div>
                状态列表
              </div>
              <n-flex
                align="center"
                :size="12"
              >
                <NText depth="3">
                  共 {{ pageData?.total }} 次提交
                </NText>
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
              </n-flex>
            </n-flex>
          </template>

          <!-- 题目表格 -->
          <n-data-table
            :columns="columns"
            :data="pageData?.records"
            :bordered="false"
            :row-key="(row: any) => row.id"
            :row-props="rowProps"
            scroll-x="1400"
            class="flex-1 h-full"
          />

          <!-- 分页 -->
          <template #footer>
            <n-flex justify="center">
              <n-pagination
                v-model:page="pageParam.current"
                v-model:page-size="pageParam.size"
                show-size-picker
                :page-count="pageData ? Number(pageData.pages) : 0"
                :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                  label: `${(i + 1) * 10} 每页`,
                  value: (i + 1) * 10,
                }))"
                @update:page="loadData"
                @update:page-size="loadData"
              />
            </n-flex>
          </template>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>
</style>
