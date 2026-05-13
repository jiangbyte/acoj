<template>
  <a-card title="最近登录" :bordered="false" :loading="loading" class="log-card">
    <div class="timeline-div" v-if="data?.length">
      <a-timeline>
        <a-timeline-item v-for="log in data" :key="log.last_login_at + log.account" color="blue">
          <div class="log-item">
            <div class="log-header">
              <span class="log-name">{{ log.nickname || log.account }}</span>
              <span class="log-time">{{ formatTime(log.last_login_at) }}</span>
            </div>
            <p class="log-address" v-if="log.last_login_ip">{{ log.last_login_ip }}</p>
          </div>
        </a-timeline-item>
      </a-timeline>
    </div>
    <div v-else-if="!loading" class="empty-timeline">暂无登录记录</div>
  </a-card>
</template>

<script setup lang="ts">
import type { RecentLogin } from '@/api/dashboard'

defineProps<{
  data: RecentLogin[]
  loading: boolean
}>()

function formatTime(iso: string | null) {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
:deep(.ant-card-body) {
  padding-top: 0 !important;
}
.timeline-div {
  max-height: 330px;
  overflow-y: auto;
  padding: 8px 0;
}
.timeline-div::-webkit-scrollbar {
  width: 4px;
}
.timeline-div::-webkit-scrollbar-thumb {
  background: var(--border-color-split, #e8e8e8);
  border-radius: 2px;
}
.log-item {
  margin-bottom: 2px;
}
.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.log-name {
  font-weight: 500;
  color: var(--text-color);
  font-size: 13px;
}
.log-time {
  font-size: 12px;
  color: var(--text-color-secondary);
}
.log-address {
  margin-bottom: 0;
  font-size: 12px;
  color: var(--text-color-secondary);
  opacity: 0.7;
}
.empty-timeline {
  padding: 40px 0;
  text-align: center;
  color: var(--text-color-secondary);
}
</style>
