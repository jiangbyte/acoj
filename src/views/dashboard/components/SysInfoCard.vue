<template>
  <a-card title="系统信息" :bordered="false" :loading="loading" class="sys-info-card">
    <a-descriptions :column="1" size="small">
      <a-descriptions-item label="Python版本">
        <a-tag color="blue">{{ data?.python_version || '-' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="操作系统">
        <span class="info-value">{{ data?.os_name || '-' }}</span>
      </a-descriptions-item>
      <a-descriptions-item label="服务器IP">
        <span class="info-value">{{ data?.server_ip || '-' }}</span>
      </a-descriptions-item>
      <a-descriptions-item label="运行时长">
        <a-tag color="green">{{ data?.run_time || '-' }}</a-tag>
      </a-descriptions-item>
    </a-descriptions>
  </a-card>
</template>

<script setup lang="ts">
import type { SysInfo } from '@/api/dashboard'

defineProps<{
  data: SysInfo | null
  loading: boolean
}>()
</script>

<style scoped>
:deep(.ant-card-body) {
  padding-top: 0 !important;
}
.sys-info-card {
  height: 100%;
}
.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
}
</style>
