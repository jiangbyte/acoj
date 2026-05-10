<template>
  <div>
    <a-row :gutter="[16, 16]">
      <a-col v-for="card in stats" :key="card.title" :span="6">
        <a-card>
          <div class="flex items-center gap-3">
            <component :is="card.icon" class="text-2xl" :style="{ color: card.color }" />
            <div>
              <div class="text-gray-400 text-sm">{{ card.title }}</div>
              <div class="text-2xl font-bold">{{ card.value }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <a-card class="mt-4" title="欢迎使用 Hei Admin">
      <p>欢迎使用后台管理系统，请通过左侧菜单导航。</p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import {
  UserOutlined,
  TeamOutlined,
  AppstoreOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'

const stats = [
  { title: '用户数', value: '--', icon: UserOutlined, color: '#1677ff' },
  { title: '角色数', value: '--', icon: TeamOutlined, color: '#52c41a' },
  { title: '模块数', value: '--', icon: AppstoreOutlined, color: '#faad14' },
  { title: '配置项', value: '--', icon: SettingOutlined, color: '#ff4d4f' },
]
</script>
