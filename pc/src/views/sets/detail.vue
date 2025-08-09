<script lang="ts" setup>
import { AesCrypto } from '@/utils'
import MdViewer from '@/components/common/editor/md/Viewer.vue'
import { useProSetFetch } from '@/composables'

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.set as string)
async function loadData() {
  try {
    const { proSetDetail } = useProSetFetch()
    const { data } = await proSetDetail({ id: originalId })

    if (data) {
      detailData.value = data
    }
  }
  catch {
  }
}
loadData()
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <div class="w-full">
      <!-- 题集头部信息 -->
      <div class="bg-white content-card shadow-sm overflow-hidden">
        <!-- 封面图区域 -->
        <div class="relative h-48 md:h-64">
          <img
            :src="detailData?.cover"
            class="w-full h-full object-cover"
          >
          <!-- 遮罩层和信息 -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent flex items-end">
            <div class="p-6 md:p-8 text-white">
              <!-- 标签组 -->
              <div class="flex flex-wrap items-center gap-2 mb-3">
                <n-tag
                  :type="detailData?.setType === 1 ? 'info' : 'success'"
                  size="small"
                >
                  {{ detailData?.setTypeName }}
                </n-tag>
                <n-tag
                  :type="detailData?.isRunning ? 'success' : 'warning'"
                  size="small"
                >
                  {{ detailData?.isRunning ? '进行中' : '已结束' }}
                </n-tag>
                <n-tag
                  size="small"
                  type="info"
                >
                  {{ detailData?.difficultyName }}
                </n-tag>
              </div>

              <!-- 标题 -->
              <h1 class="text-2xl md:text-3xl font-bold mb-2 text-shadow">
                {{ detailData?.title }}
              </h1>

              <!-- 附加信息 -->
              <div class="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm">
                <n-tag
                  size="small"
                  type="info"
                >
                  {{ detailData?.categoryName }}
                </n-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮区 -->
        <div class="px-6 md:px-8 py-4 border-t border-gray-100 flex flex-wrap items-center justify-between gap-4">
          <div class="flex items-center gap-4">
            <n-button
              type="primary"
              size="medium"
              class="flex items-center"
            >
              <n-icon size="16" class="mr-2">
                <icon-park-outline-play-one />
              </n-icon>
              开始训练
            </n-button>

            <n-button
              variant="default"
              size="medium"
              class="flex items-center"
            >
              <n-icon size="16" class="mr-2">
                <icon-park-outline-star />
              </n-icon>
              收藏
            </n-button>

            <n-button
              variant="default"
              size="medium"
              class="flex items-center"
            >
              <n-icon size="16" class="mr-2">
                <icon-park-outline-share />
              </n-icon>
              分享
            </n-button>
          </div>

          <div class="flex items-center gap-6 text-sm">
            <div class="flex items-center text-gray-600">
              <n-icon size="16" class="text-primary mr-1">
                <icon-park-outline-like />
              </n-icon>
              <span>{{ 0 }} 题</span>
            </div>

            <div class="flex items-center text-gray-600">
              <n-icon size="16" class="text-primary mr-1">
                <icon-park-outline-comment />
              </n-icon>
              <span>{{ 0 }} 人参与</span>
            </div>

            <div class="flex items-center text-gray-600">
              <n-icon size="16" class="text-warning mr-1">
                <icon-park-outline-star />
              </n-icon>
              <span>{{ 0 }}% 完成率</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <n-grid cols="12 m:24" :x-gap="16" :y-gap="16" responsive="screen">
      <n-gi span="12 m:5">
        <n-flex vertical>
          <n-card size="small" hoverable class="content-card" title="题集信息">
            <n-descriptions label-placement="left">
              <n-descriptions-item :span="12" label="题集类型">
                {{ detailData?.setTypeName }}
              </n-descriptions-item>
              <n-descriptions-item :span="12" label="题集分类">
                {{ detailData?.categoryName }}
              </n-descriptions-item>
              <n-descriptions-item :span="12" label="难度级别">
                {{ detailData?.difficultyName }}
              </n-descriptions-item>
              <n-descriptions-item v-if="detailData?.startTime" :span="12" label="开始时间">
                <n-time :time="detailData?.startTime" />
              </n-descriptions-item>
              <n-descriptions-item v-if="detailData?.endTime" :span="12" label="结束时间">
                <n-time :time="detailData?.endTime" />
              </n-descriptions-item>
              <n-descriptions-item :span="12" label="创建时间">
                <n-time :time="detailData?.createTime" />
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
          <n-card size="small" hoverable class="content-card" title="题集进度">
            <n-flex vertical>
              <n-progress
                type="multiple-circle"
                :stroke-width="6"
                :circle-gap="0.5"
                :percentage="[12, 45, 90]"
                :color="[
                  '#18a058',
                  '#2080f0',
                  '#f0a020',
                ]"
              >
                <div style="text-align: center">
                  题集进度
                </div>
              </n-progress>
              <n-descriptions label-placement="left">
                <n-descriptions-item :span="12" label="题集类型">
                  {{ detailData?.setTypeName }}
                </n-descriptions-item>
                <n-descriptions-item :span="12" label="题集分类">
                  {{ detailData?.categoryName }}
                </n-descriptions-item>
              </n-descriptions>
            </n-flex>
          </n-card>
        </n-flex>
      </n-gi>
      <n-gi span="12 m:13">
        <n-flex vertical>
          <n-card title="题集介绍" size="small" hoverable class="content-card">
            <MdViewer :model-value="detailData?.description" />
          </n-card>
          <n-card size="small" hoverable class="content-card">
            <n-tabs type="line" animated class="min-h-[calc(100vh-500px)]">
              <n-tab-pane name="xq" tab="题目列表">
                456456
              </n-tab-pane>
              <n-tab-pane name="xq1" tab="排行榜">
                456456
              </n-tab-pane>
              <n-tab-pane name="tmlb" tab="题目列表">
                <n-space vertical>
                  <n-h3>题目列表内容将在这里显示</n-h3>
                  <n-text depth="3">
                    这里将显示题集中的所有题目列表
                  </n-text>
                </n-space>
              </n-tab-pane>
            </n-tabs>
          </n-card>
        </n-flex>
      </n-gi>

      <n-gi span="12 m:6">
        <n-flex vertical>
          <set-notice />
        </n-flex>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>
</style>
