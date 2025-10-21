<script lang="ts" setup>
interface Props {
  resultTaskData: any
}

defineProps<Props>()
</script>

<template>
  <div class="grid grid-cols-3 md:grid-cols-3 gap-4">
    <n-card size="small" hoverable>
      <n-text>长度(B)</n-text>
      <div class="text-xl font-bold">
        {{ resultTaskData?.codeLength ? resultTaskData?.codeLength : 0 }}
      </div>
    </n-card>
    <n-card size="small" hoverable>
      <n-text>时间(ms)</n-text>
      <div class="text-xl font-bold">
        {{ resultTaskData?.time ? resultTaskData?.time : 0 }}
      </div>
    </n-card>
    <n-card size="small" hoverable>
      <n-text>内存(KB)</n-text>
      <div class="text-xl font-bold">
        {{ resultTaskData?.memory ? resultTaskData?.memory : 0 }}
      </div>
    </n-card>
  </div>
</template>
