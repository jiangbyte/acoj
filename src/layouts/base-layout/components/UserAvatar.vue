<template>
  <ADropdown :trigger="['click']">
    <div class="flex items-center gap-2 cursor-pointer">
      <AAvatar :size="32" :src="auth.userInfo?.avatar || undefined">
        {{ auth.userInfo?.nickname?.[0] || 'U' }}
      </AAvatar>
      <span>{{ auth.userInfo?.nickname || '' }}</span>
    </div>
    <template #overlay>
      <AMenu>
        <AMenuItem key="logout" @click="handleLogout">
          <LogoutOutlined /> 退出登录
        </AMenuItem>
      </AMenu>
    </template>
  </ADropdown>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const auth = useAuthStore()

async function handleLogout() {
  await auth.logout()
  message.success('已退出登录')
  router.push({ name: 'login' })
}
</script>
