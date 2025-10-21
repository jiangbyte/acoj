<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <n-card size="small" hoverable>
    <n-space vertical class="w-full" :size="16">
      <n-space align="center" justify="space-between">
        <n-text>
          代码相似度
        </n-text>
        <n-space align="center">
          <n-tag v-if="resultTaskData?.similarityCategoryName" type="info">
            {{ resultTaskData?.similarityCategoryName }}
          </n-tag>
          <n-tag type="info">
            {{ (resultTaskData?.similarity || 0) * 100 }} %
          </n-tag>
        </n-space>
      </n-space>
      <n-progress
        type="line"
        :show-indicator="false"
        :percentage="(resultTaskData?.similarity || 0) * 100"
      />
      <n-alert type="info" show-icon>
        代码克隆检测用于辅助判断代码相似度，结果仅供参考
      </n-alert>
    </n-space>
  </n-card>
</template>
