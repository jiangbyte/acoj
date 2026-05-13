<template>
  <a-card title="最近登录" :bordered="false" :loading="loading" class="log-card">
    <div v-if="hasData" class="timeline-div">
      <div class="biz-section">
        <div class="biz-section-title">B端</div>
        <a-timeline v-if="props.sysData.length">
          <a-timeline-item v-for="log in props.sysData" :key="'b' + log.last_login_at + log.account" color="blue">
            <div class="log-item">
              <div class="log-header">
                <span class="log-name">{{ log.nickname || log.account }}</span>
                <span class="log-time">{{ formatTime(log.last_login_at) }}</span>
              </div>
              <p v-if="log.last_login_ip" class="log-address">{{ log.last_login_ip }}</p>
            </div>
          </a-timeline-item>
        </a-timeline>
        <div v-else class="empty-inline">暂无记录</div>
      </div>
      <div class="biz-divider" />
      <div class="biz-section">
        <div class="biz-section-title">C端</div>
        <a-timeline v-if="props.clientData.length">
          <a-timeline-item v-for="log in props.clientData" :key="'c' + log.last_login_at + log.account" color="green">
            <div class="log-item">
              <div class="log-header">
                <span class="log-name">{{ log.nickname || log.account }}</span>
                <span class="log-time">{{ formatTime(log.last_login_at) }}</span>
              </div>
              <p v-if="log.last_login_ip" class="log-address">{{ log.last_login_ip }}</p>
            </div>
          </a-timeline-item>
        </a-timeline>
        <div v-else class="empty-inline">暂无记录</div>
      </div>
    </div>
    <div v-else-if="!loading" class="empty-timeline">暂无登录记录</div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RecentLogin } from '@/api/dashboard'

const props = defineProps<{
  sysData: RecentLogin[]
  clientData: RecentLogin[]
  loading: boolean
}>()

const hasData = computed(() => props.sysData.length > 0 || props.clientData.length > 0)

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
.biz-section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-color-secondary);
  margin-bottom: 6px;
}
.biz-divider {
  height: 1px;
  background: var(--border-color-split, #e8e8e8);
  margin: 8px 0;
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
.empty-inline {
  padding: 16px 0;
  text-align: center;
  color: var(--text-color-secondary);
  font-size: 13px;
}
</style>
