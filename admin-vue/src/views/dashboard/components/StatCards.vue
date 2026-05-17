<template>
  <a-card title="数据概览" :bordered="false" :loading="loading" class="stat-cards">
    <div class="stats-grid">
      <div v-for="card in allItems" :key="card.key" class="stat-item">
        <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <component :is="card.icon" />
        </div>
        <a-statistic
          :value="card.value ?? '--'"
          :title="card.label"
          :value-style="{ fontSize: '22px', fontWeight: 700, color: 'var(--text-color)' }"
        />
        <a-tag v-if="card.bizTag" class="biz-tag" :color="card.tagColor">{{ card.bizTag }}</a-tag>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { UserOutlined, TeamOutlined, ApartmentOutlined, SettingOutlined, BellOutlined } from '@ant-design/icons-vue'
import type { DashboardStats, ClientStats } from '@/api/dashboard'

const props = defineProps<{
  sysData: DashboardStats | null
  clientData: ClientStats | null
  loading: boolean
}>()

const allItems = computed(() => [
  {
    key: 'total_users',
    icon: UserOutlined,
    label: '用户总数',
    color: '#1677ff',
    bg: '#e6f4ff',
    value: props.sysData?.total_users ?? null,
    bizTag: 'B端',
    tagColor: 'blue',
  },
  {
    key: 'active_users',
    icon: UserOutlined,
    label: '活跃用户',
    color: '#52c41a',
    bg: '#f6ffed',
    value: props.sysData?.active_users ?? null,
    bizTag: 'B端',
    tagColor: 'blue',
  },
  {
    key: 'total_roles',
    icon: TeamOutlined,
    label: '角色总数',
    color: '#faad14',
    bg: '#fffbe6',
    value: props.sysData?.total_roles ?? null,
    bizTag: 'B端',
    tagColor: 'blue',
  },
  {
    key: 'total_orgs',
    icon: ApartmentOutlined,
    label: '组织总数',
    color: '#722ed1',
    bg: '#f9f0ff',
    value: props.sysData?.total_orgs ?? null,
    bizTag: 'B端',
    tagColor: 'blue',
  },
  {
    key: 'total_configs',
    icon: SettingOutlined,
    label: '配置项数',
    color: '#eb2f96',
    bg: '#fff0f6',
    value: props.sysData?.total_configs ?? null,
    bizTag: 'B端',
    tagColor: 'blue',
  },
  {
    key: 'total_notices',
    icon: BellOutlined,
    label: '通知总数',
    color: '#fa8c16',
    bg: '#fff7e6',
    value: props.sysData?.total_notices ?? null,
    bizTag: 'B端',
    tagColor: 'blue',
  },
  {
    key: 'client_total_users',
    icon: UserOutlined,
    label: 'C端用户总数',
    color: '#13c2c2',
    bg: '#e6fffb',
    value: props.clientData?.total_users ?? null,
    bizTag: 'C端',
    tagColor: 'green',
  },
  {
    key: 'client_active_users',
    icon: UserOutlined,
    label: 'C端活跃用户',
    color: '#52c41a',
    bg: '#f6ffed',
    value: props.clientData?.active_users ?? null,
    bizTag: 'C端',
    tagColor: 'green',
  },
])
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  position: relative;
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
.biz-tag {
  position: absolute;
  top: 4px;
  right: 4px;
  font-size: 10px;
  line-height: 16px;
  padding: 0 4px;
  border-radius: 4px;
}
</style>
