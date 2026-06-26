<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { renderIcon } from '@/utils/icon'
import { useAppStore } from '@/stores'

const router = useRouter()
const appStore = useAppStore()
const homePath = import.meta.env.VITE_HOME_PATH

// 移动端抽屉头部需要展示用户名，桌面端下拉入口默认直接显示用户名。
const { showName = false } = defineProps<{
  showName?: boolean
}>()

// 移动端点击用户信息时不展开桌面下拉菜单，而是通知父组件打开右侧抽屉。
const emit = defineEmits<{
  openMobileDrawer: []
}>()

// 桌面端用户菜单项。当前用户中心和退出登录仍是占位行为，项目首页使用环境配置路径。
const options = computed<DropdownOption[]>(() => [
  {
    label: '用户中心',
    key: 'userCenter',
    icon: renderIcon('icon-park-outline:user'),
  },
  {
    type: 'divider',
    key: 'divider-1',
  },
  {
    label: '项目首页',
    key: 'home',
    icon: renderIcon('icon-park-outline:home'),
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon('icon-park-outline:logout'),
  },
])

// 根据下拉菜单 key 分发行为；保留弹窗确认可以避免后续接入真实登出时误触。
function handleSelect(key: string | number) {
  if (key === 'userCenter') {
    window.$message.info('用户中心待接入')
  }
  if (key === 'home') {
    router.push(homePath)
  }
  if (key === 'logout') {
    window.$dialog.info({
      title: '退出登录',
      content: '确认退出当前账号？',
      positiveText: '确认',
      negativeText: '取消',
      onPositiveClick: () => window.$message.success('已退出'),
    })
  }
}
</script>

<template>
  <div>
    <div v-if="appStore.isMobile">
      <div class="flex items-center gap-2" @click="emit('openMobileDrawer')">
        <n-avatar round class="cursor-pointer">
          <NovaIcon icon="icon-park-outline:user" />
        </n-avatar>
        <span v-if="showName">用户昵称</span>
      </div>
    </div>
    <n-dropdown v-else trigger="click" :options="options" @select="handleSelect">
      <div class="flex items-center gap-2">
        <n-avatar round class="cursor-pointer">
          <NovaIcon icon="icon-park-outline:user" />
        </n-avatar>
        用户昵称
      </div>
    </n-dropdown>
  </div>
</template>
