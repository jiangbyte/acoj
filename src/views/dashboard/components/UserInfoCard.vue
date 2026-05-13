<template>
  <a-card :bordered="false" class="user-info-card">
    <div class="user-info-inner">
      <div class="user-info-left">
        <a-avatar :size="56" :src="userInfo.avatar">
          {{ userInfo.nickname?.charAt(0) || '?' }}
        </a-avatar>
        <div class="user-details">
          <div class="greeting-line">
            <span class="greeting">{{ greeting }}，{{ userInfo.nickname || userInfo.account }}</span>
            <a-tag v-if="userInfo.login_count" color="blue" class="login-count-tag">
              第 {{ userInfo.login_count }} 次登录
            </a-tag>
          </div>
          <div class="user-meta">
            <template v-if="userInfo.org_name">{{ userInfo.org_name }}</template>
            <template v-if="userInfo.org_name && userInfo.position_name"><span class="meta-divider">|</span></template>
            <template v-if="userInfo.position_name">{{ userInfo.position_name }}</template>
            <template v-if="!userInfo.org_name && !userInfo.position_name">{{ userInfo.account }}</template>
          </div>
          <div class="user-footer" v-if="userInfo.last_login_at">
            <span class="footer-item">
              上次登录 {{ formatTime(userInfo.last_login_at) }}
            </span>
            <span class="footer-divider" v-if="userInfo.last_login_ip">|</span>
            <span class="footer-item" v-if="userInfo.last_login_ip">
              {{ userInfo.last_login_ip }}
            </span>
          </div>
        </div>
      </div>
      <div class="user-info-right">
        <div class="current-time">{{ currentTime }}</div>
        <div class="current-date">{{ currentDate }}</div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo || {})

const currentTime = ref('')
const currentDate = ref('')

const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function formatTime(iso: string | null) {
  if (!iso) return '-'
  return iso.replace('T', ' ').substring(0, 19)
}

function updateClock() {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  currentTime.value = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  currentDate.value = `${now.getFullYear()}年${pad(now.getMonth() + 1)}月${pad(now.getDate())}日 ${weekdays[now.getDay()]}`
}

let timer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.user-info-card {
  border-radius: 8px;
}
.user-info-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.user-info-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.greeting-line {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.greeting {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
}
.login-count-tag {
  font-size: 11px;
  line-height: 18px;
  border-radius: 10px;
  padding: 0 8px;
}
.user-meta {
  font-size: 14px;
  color: var(--text-color-secondary);
}
.meta-divider {
  color: var(--border-color-split);
  margin: 0 2px;
}
.user-footer {
  font-size: 12px;
  color: var(--text-color-secondary);
  opacity: 0.7;
  display: flex;
  align-items: center;
  gap: 4px;
}
.footer-divider {
  color: var(--border-color-split);
}
.user-info-right {
  text-align: right;
  flex-shrink: 0;
}
.current-date {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-top: 1px;
}
.current-time {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  line-height: 1.2;
  letter-spacing: 1px;
  font-variant-numeric: tabular-nums;
}
@media (max-width: 768px) {
  .user-info-right { display: none; }
}
</style>
