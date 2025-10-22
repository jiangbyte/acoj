<script setup lang="ts">
import { useSysUserFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import { CalendarHeatmap } from 'vue3-calendar-heatmap'

defineOptions({
  inheritAttrs: false,
})

const endDate = ref(new Date())

const route = useRoute()
const originalId = AesCrypto.decrypt(route.query.userId as string)

const detailData = ref()

const setPageData = ref()
async function loadData() {
  const { sysUserDetailClient } = useSysUserFetch()
  sysUserDetailClient({ id: originalId }).then(({ data }) => {
    detailData.value = data
  })
  setPageData.value = {
    records: [],
  }
}
loadData()
</script>

<template>
  <!-- 页面内容 -->
  <div class="flex flex-col h-full w-full">
    <div
      class="relative mt-72px px-4 py-8"
      size="small"
      :bordered="false"
      :style="{
        backgroundImage: detailData?.background ? `linear-gradient(to left bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,0) 0, rgba(255,255,255,1) 65%), url(${detailData.background})` : '',
        backgroundSize: 'cover',
        backgroundPosition: 'right top',
        backgroundRepeat: 'no-repeat',
      }"
    >
      <NSpace vertical size="large" class="container mx-auto">
        <NSpace align="center" :wrap="false">
          <NSpace vertical align="center" size="small">
            <NAvatar
              round
              :size="90"
              :src="detailData?.avatar"
            />
          </NSpace>

          <NSpace vertical size="small" style="margin-left: 20px;">
            <NSpace align="center">
              <h2 style="margin: 0">
                {{ detailData?.nickname }}
              </h2>
            </NSpace>
            <NSpace vertical>
              <NSpace size="small" align="center">
                <n-tag size="small" type="info">
                  {{ detailData?.genderName || '未知' }}
                </n-tag>
              </NSpace>
            </NSpace>
          </NSpace>
        </NSpace>

        <n-space vertical>
          <NSpace size="small">
            <NText>
              所属组
            </NText>
            <NTag size="small" type="success">
              {{ detailData?.groupIdName }}
            </NTag>
          </NSpace>
          <n-space align="center">
            <NText>
              注册时间
            </NText>
            <n-tag size="small" type="info">
              <n-time :time="Number(detailData?.createTime) || Date.now()" />
            </n-tag>
          </n-space>
          <NSpace size="small">
            <NText>
              邮箱地址
            </NText>
            <n-tag size="small" type="info">
              {{ detailData?.email }}
            </n-tag>
          </NSpace>
          <NSpace size="small" align="center">
            <NText>
              个性签名
            </NText>
            <NText depth="3">
              {{ detailData?.quote }}
            </NText>
          </NSpace>
        </n-space>

        <!-- 统计数据卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-2">
          <n-card hoverable class="mt-2 rounded-xl text-center" size="small">
            <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-1">
              {{ detailData?.solvedProblem ? detailData?.solvedProblem : 0 }}
            </div>
            <div class="text-sm text-gray-500 dark:text-gray-400">
              已解决题目
            </div>
          </n-card>
          <n-card hoverable class="mt-2 rounded-xl text-center" size="small">
            <div class="text-3xl font-bold text-blue-600 dark:text-blue-400 mb-1">
              {{ detailData?.participatedSet ? detailData?.participatedSet : 0 }}
            </div>
            <div class="text-sm text-gray-500 dark:text-gray-400">
              参与题集
            </div>
          </n-card>
          <n-card hoverable class="mt-2 rounded-xl text-center" size="small">
            <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
              {{ detailData?.activeScore ? detailData?.activeScore : 0 }}
            </div>
            <div class="text-sm text-gray-500 dark:text-gray-400">
              活跃指数
            </div>
          </n-card>
        </div>

        <n-card class="mt-2 rounded-xl" size="small">
          <div class="overflow-scroll">
            <CalendarHeatmap
              :values="detailData?.acRecord ? detailData?.acRecord : []"
              tooltip-unit="AC"
              :end-date="endDate"
              :range-color="['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39']"
              class="custom-heatmap p-4 min-w-1000px"
            />
          </div>
          <n-text depth="3">
            注：AC热力图数据来自正式提交记录
          </n-text>
        </n-card>
      </NSpace>
    </div>
  </div>
</template>

<style scoped>
/* 使用深度选择器修改子组件样式 */
:deep(.custom-heatmap .vch__day__label) {
  font-size: 8px;
}
:deep(.custom-heatmap .vch__month__label) {
  font-size: 8px;
}
:deep(.custom-heatmap .vch__legend) {
  font-size: 10px;
}

/* 图片悬停效果 */
img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
}
</style>
