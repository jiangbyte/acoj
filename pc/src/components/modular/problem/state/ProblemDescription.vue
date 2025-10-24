<script lang="ts" setup>
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'
import {
  BarChartOutline,
  HardwareChipOutline,
  TimeOutline,
} from '@vicons/ionicons5'
// import { NSpin } from 'naive-ui'

interface Props {
  detailData?: any
}

defineProps<Props>()

// 懒加载重量级组件
// const MdViewer = defineAsyncComponent(() =>
//   import('@/components/common/editor/md/MarkdownViewer.vue'),
// )
// const MdViewer = defineAsyncComponent({
//   loader: () => import('@/components/common/editor/md/MarkdownViewer.vue'),
//   loadingComponent: {
//     setup() {
//       return () => h('div', {
//         class: 'h-full',
//         style: {
//           display: 'flex',
//           alignItems: 'center',
//           justifyContent: 'center',
//         },
//       }, [
//         h(NSpin, { size: 'small', description: '加载中...' }, { }),
//       ])
//     },
//   },
//   delay: 200,
//   timeout: 10000,
// })
</script>

<template>
  <n-space vertical>
    <n-space align="center">
      <n-tag type="success">
        {{ detailData?.categoryName }}
      </n-tag>
      <n-tag type="error">
        {{ detailData?.difficultyName }}
      </n-tag>
      <n-h3 style="margin: 0;">
        {{ detailData?.title }}
      </n-h3>
    </n-space>
    <n-space align="center">
      <NTag
        v-for="tag in detailData?.tagNames"
        :key="tag"
        size="small"
        type="info"
      >
        {{ tag }}
      </NTag>
    </n-space>
    <n-flex>
      <ProblemStatItem
        :icon="TimeOutline"
        label="时间限制"
        :value="`${detailData?.maxTime} ms`"
      />
      <ProblemStatItem
        :icon="HardwareChipOutline"
        label="内存限制"
        :value="`${detailData?.maxMemory} KB`"
      />
      <ProblemStatItem
        :icon="BarChartOutline"
        label="通过率"
        :value="`${detailData?.acceptance} %`"
      />
    </n-flex>
    <MdViewer :model-value="detailData?.description" />
  </n-space>
</template>

<style scoped></style>
