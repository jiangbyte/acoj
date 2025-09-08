<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NButton, NSpace, NTag, NText, NTime } from 'naive-ui'
import CodeEditor from '@/components/common/editor/code/Editor.vue'

const columns: DataTableColumns<any> = [
  {
    title: '任务ID',
    key: 'taskId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '提交用户',
    key: 'originUser',
    width: 140,
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
                src: row.originUserAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.originUserName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '提交ID',
    key: 'originSubmitId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '样本用户',
    key: 'sampleUser',
    width: 140,
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
                src: row.originUserAvatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.originUserName },
            ),
          ],
        },
      )
    },
  },
  {
    title: '提交ID',
    key: 'originSubmitId',
    width: 100,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '相似度',
    key: 'similarity',
    width: 100,
    render: (row) => {
      return h(NTag, { }, () => row.similarity * 100)
    },
  },
  {
    title: '相似标记',
    key: 'similarityName',
    width: 100,
  },
  {
    title: '提交时间',
    key: 'submitTime',
    width: 120,
    render: (row) => {
      return h(NTime, { time: row.submitTime })
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 40,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        h(NButton, { type: 'primary', size: 'small', onClick: () => {} }, () => '查看详情'),
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   disabled: row.canUseSimilarReport !== true,
        //   onClick: () => { reportRef.value.doOpen(row) },
        // }, () => '相似报告'),
      ])
    },
  },
]

const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null,
  sortOrder: null,
  keyword: '',
  tagId: null,
  categoryId: null,
  difficulty: null,
})
const pageData = ref({
  total: 0,
  records: [],
})

async function loadData() {
  pageData.value.records = [
    {
      id: 1,
      taskId: 'CLONE-20230615-8762',
      originUser: '12',
      originSubmitId: 'CLONE-20230615-8762',
      originUserName: '12',
      originUserAvatar: 'https://avatars.githubusercontent.com/u/1020407?v=4',
      similarity: 0.8,
      submitTime: new Date(),
      similarityName: '相似',
    },
  ]
}

loadData()
</script>

<template>
  <div class="flex flex-col h-full w-full overflow-scroll">
    <n-card size="small" class="flex-1">
      <NSpace vertical :size="20">
        <div class="flex justify-between items-center">
          <div>
            <h2 class="text-[clamp(1.25rem,3vw,1.75rem)] font-bold mb-2">
              代码克隆检测报告
            </h2>
            <NText depth="3">
              检测时间: 2023-06-15 14:30:22 | 任务ID: CLONE-20230615-8762
            </NText>
          </div>
          <div class="flex space-x-3">
          <!-- <button class="px-4 py-2 bg-white border border-gray-medium rounded-md text-sm text-gray-dark hover:bg-gray-light transition-colors">
            <i class="fa fa-download mr-1" /> 导出报告
          </button>
          <button class="px-4 py-2 bg-primary text-white rounded-md text-sm hover:bg-primary/90 transition-colors">
            <i class="fa fa-refresh mr-1" /> 重新检测
          </button> -->
          </div>
        </div>

        <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
          <n-card size="small" title="检测题目" class="rounded-xl">
            <div class="flex items-center">
              <n-icon size="30" class="mr-3">
                <icon-park-outline-add />
              </n-icon>
              <NSpace vertical :size="2">
                <p class="font-medium">
                  题目标题
                </p>
                <NSpace align="center">
                  <div>
                    难度
                    <NTag size="small">
                      中等
                    </NTag>
                  </div>
                  <div>
                    提交次数
                    <NTag size="small">
                      101
                    </NTag>
                  </div>
                </NSpace>
              </NSpace>
            </div>
          </n-card>
          <n-card size="small" title="检测范围" class="rounded-xl">
            <div class="flex items-center">
              <n-icon size="30" class="mr-3">
                <icon-park-outline-add />
              </n-icon>
              <NSpace class="w-full" justify="space-between">
                <NSpace :size="1" vertical>
                  <p class="font-medium">
                    近7天提交记录
                  </p>
                  <p class="font-medium">
                    共检测 42 份有效代码
                  </p>
                </NSpace>
                <NSpace :size="1" vertical>
                  <p class="font-medium">
                    发现 8 组疑似克隆
                  </p>
                  <p class="font-medium">
                    克隆率：19.05%
                  </p>
                </NSpace>
              </NSpace>
            </div>
          </n-card>
          <n-card size="small" title="检测范围" class="rounded-xl">
            <div class="flex items-center">
              <n-icon size="30" class="mr-3">
                <icon-park-outline-add />
              </n-icon>
              <NSpace class="w-full" justify="space-between">
                <NSpace :size="1" vertical>
                  <p class="font-medium">
                    相似度阈值：75%
                  </p>
                  <p class="font-medium">
                    代码预处理：已启用
                  </p>
                </NSpace>
                <NSpace :size="1" vertical>
                  <p class="font-medium">
                    检测算法：CCFinder
                  </p>
                  <p class="font-medium">
                    检测模式：深度检测
                  </p>
                </NSpace>
              </NSpace>
            </div>
          </n-card>
        </div>

        <n-card size="small" title="相似度分布统计" class="rounded-xl">
          <div class="grid grid-cols-3 gap-8">
            <div class="col-span-2">
              <SimilarityDistributionChart />
            </div>
            <div class="flex flex-col w-full gap-3">
              <n-card size="small" title="检测指标" class="rounded-xl">
                <div class="flex items-center">
                  <NSpace class="w-full" justify="space-between">
                    <NSpace :size="1" vertical>
                      <p class="font-medium">
                        平均相似度：75%
                      </p>
                      <p class="font-medium">
                        最高相似度：98.7%
                      </p>
                    </NSpace>
                    <NSpace :size="1" vertical>
                      <p class="font-medium">
                        平均代码长度：187
                      </p>
                      <p class="font-medium">
                        检测覆盖率：100%
                      </p>
                    </NSpace>
                  </NSpace>
                </div>
              </n-card>
              <div>
                <div class="flex items-center justify-between w-full">
                  <NText>
                    高度疑似克隆 (≥90%)
                  </NText>
                  <NText class="mr-3">
                    3组
                  </NText>
                </div>
                <n-progress
                  type="line"
                  :percentage="20"
                />
              </div>
              <div>
                <div class="flex items-center justify-between w-full">
                  <NText>
                    高度疑似克隆 (≥90%)
                  </NText>
                  <NText class="mr-3">
                    3组
                  </NText>
                </div>
                <n-progress
                  type="line"
                  :percentage="20"
                />
              </div>
              <div>
                <div class="flex items-center justify-between w-full">
                  <NText>
                    高度疑似克隆 (≥90%)
                  </NText>
                  <NText class="mr-3">
                    3组
                  </NText>
                </div>
                <n-progress
                  type="line"
                  :percentage="20"
                />
              </div>
              <n-alert type="info">
                高度疑似克隆可能涉及抄袭行为；轻度疑似克隆可能是思路巧合。
              </n-alert>
            </div>
          </div>
        </n-card>

        <n-card size="small" title="疑似克隆对列表" class="rounded-xl">
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <n-data-table
              :columns="columns"
              :data="pageData?.records"
              :bordered="false"
              :row-key="(row: any) => row.userId"
              class="flex-1 h-full"
            />
          </div>
          <n-pagination
            v-model:page="pageParam.current"
            v-model:page-size="pageParam.size"
            show-size-picker
            :page-count="pageData ? Number(pageData.pages) : 0"
            :page-sizes="Array.from({ length: 10 }, (_, i) => ({
              label: `${(i + 1) * 10} 每页`,
              value: (i + 1) * 10,
            }))"
            class="flex justify-center items-center p-x-6 pt-6 pb-2"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </n-card>

        <n-card size="small" title="疑似克隆对详情" class="rounded-xl">
          <div class="grid grid-cols-4 gap-8">
            <div class="col-span-2">
              <n-card class="rounded-xl" size="small">
                <NSpace vertical>
                  <NSpace align="center" justify="space-between">
                    <NSpace align="center">
                      <NAvatar round src="https://avatar.csdn.net/8/D/E/3_xiaofeng_xiao.jpg" />
                      <div class="flex flex-col gap-2">
                        <div>
                          用户名
                        </div>
                        <div class="flex items-center gap-2">
                          <div>
                            创建时间
                          </div>
                          <div>
                            最后登录时间
                          </div>
                        </div>
                      </div>
                    </NSpace>

                    <!-- <NButton type="primary">
                      全屏
                    </NButton> -->
                  </NSpace>
                  <CodeEditor
                    width="100%"
                    height="500px"
                  />
                </NSpace>
              </n-card>
            </div>
            <div class="col-span-2">
              <n-card class="rounded-xl" size="small">
                <NSpace vertical>
                  <NSpace align="center" justify="space-between">
                    <NSpace align="center">
                      <NAvatar round src="https://avatar.csdn.net/8/D/E/3_xiaofeng_xiao.jpg" />
                      <div class="flex flex-col gap-2">
                        <div>
                          用户名
                        </div>
                        <div class="flex items-center gap-2">
                          <div>
                            创建时间
                          </div>
                          <div>
                            最后登录时间
                          </div>
                        </div>
                      </div>
                    </NSpace>

                    <!-- <NButton type="primary">
                      全屏
                    </NButton> -->
                  </NSpace>
                  <CodeEditor
                    width="100%"
                    height="500px"
                  />
                </NSpace>
              </n-card>
            </div>
          </div>
        </n-card>
        <!-- <div v-for="i in 100" :key="i">
          {{ i }}
        </div> -->
      </NSpace>
    </n-card>
  </div>
</template>

<style scoped>
</style>
