<template>
  <a-card title="数据概览" :bordered="false" :loading="loading" class="stat-cards">
    <div class="stats-grid">
      <div v-for="card in statItems" :key="card.key" class="stat-item">
        <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <component :is="card.icon" />
        </div>
        <a-statistic
          :value="card.value ?? '--'"
          :title="card.label"
          :value-style="{ fontSize: '22px', fontWeight: 700, color: 'var(--text-color)' }"
        />
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  UserOutlined,
  TeamOutlined,
  ApartmentOutlined,
  SettingOutlined,
  BellOutlined,
} from '@ant-design/icons-vue'
import type { DashboardStats } from '@/api/dashboard'

const props = defineProps<{
  data: DashboardStats | null
  loading: boolean
}>()

const statItems = computed(() => [
  {
    key: 'total_users',
    icon: UserOutlined,
    label: '用户总数',
    color: '#1677ff',
    bg: '#e6f4ff',
    value: props.data?.total_users ?? null,
  },
  {
    key: 'active_users',
    icon: UserOutlined,
    label: '活跃用户',
    color: '#52c41a',
    bg: '#f6ffed',
    value: props.data?.active_users ?? null,
  },
  {
    key: 'total_roles',
    icon: TeamOutlined,
    label: '角色总数',
    color: '#faad14',
    bg: '#fffbe6',
    value: props.data?.total_roles ?? null,
  },
  {
    key: 'total_orgs',
    icon: ApartmentOutlined,
    label: '组织总数',
    color: '#722ed1',
    bg: '#f9f0ff',
    value: props.data?.total_orgs ?? null,
  },
  {
    key: 'total_configs',
    icon: SettingOutlined,
    label: '配置项数',
    color: '#eb2f96',
    bg: '#fff0f6',
    value: props.data?.total_configs ?? null,
  },
  {
    key: 'total_notices',
    icon: BellOutlined,
    label: '通知总数',
    color: '#fa8c16',
    bg: '#fff7e6',
    value: props.data?.total_notices ?? null,
  },
])
</script>

<style scoped>
:deep(.ant-card-body) {
  padding-top: 0 !important;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--background-color-light);
  border-radius: 6px;
  transition: all 0.3s;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
:deep(.ant-statistic-title) {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-bottom: 2px;
}
:deep(.ant-statistic-content) {
  line-height: 1.3;
}
</style>
