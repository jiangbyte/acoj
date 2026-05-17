<template>
  <a-drawer
    :open="open"
    title="令牌管理"
    :width="drawerWidth"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="userRecord">
      <div class="flex items-center gap-3 mb-4">
        <a-avatar :src="userRecord.avatar" :size="40">
          {{ userRecord.nickname?.[0] || userRecord.username?.[0] }}
        </a-avatar>
        <div>
          <div class="text-sm font-medium">{{ userRecord.nickname || '-' }}</div>
          <div class="text-[13px] text-[var(--text-secondary)]">{{ userRecord.username }}</div>
        </div>
      </div>

      <a-table
        :data-source="tokens"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="token"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'token'">
            <a-tooltip :title="record.token">
              <span class="font-mono text-xs">{{ truncateToken(record.token) }}</span>
            </a-tooltip>
          </template>
          <template v-if="column.key === 'device'">
            <span>{{ record.device_type || '-' }}</span>
          </template>
          <template v-if="column.key === 'timeout'">
            <span :class="{ 'text-red-500': record.timeout_seconds < 300 }">
              {{ record.timeout || '-' }}
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm
              title="确定强制下线该令牌？"
              @confirm="handleExitToken(record)"
            >
              <a-button type="link" size="small" danger>强退</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>

      <a-divider />

      <a-space>
        <a-popconfirm
          title="确定强制下线该用户所有令牌？"
          @confirm="handleExitAll"
        >
          <a-button danger>强退全部令牌</a-button>
        </a-popconfirm>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'TokenDrawer' })
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useMobile } from '@/hooks/useMobile'

const props = defineProps<{
  open: boolean
  fetchApi: (userId: string) => Promise<any>
  exitTokenApi: (userId: string, token: string) => Promise<any>
  exitAllApi: (userId: string) => Promise<any>
}>()

const emit = defineEmits(['update:open', 'success'])

const loading = ref(false)
const tokens = ref<any[]>([])
const userRecord = ref<any>(null)

const { drawerWidth } = useMobile()

async function doOpen(row: any) {
  if (!row?.user_id) return
  userRecord.value = row
  loading.value = true
  tokens.value = []
  try {
    const { data } = await props.fetchApi(row.user_id)
    tokens.value = data || []
  } catch (e) {
    console.error('Failed to fetch tokens:', e)
  }
  loading.value = false
  emit('update:open', true)
}

watch(
  () => props.open,
  (v) => {
    if (!v) {
      tokens.value = []
      userRecord.value = null
    }
  }
)

function handleClose() {
  emit('update:open', false)
}

async function handleExitToken(record: any) {
  if (!userRecord.value) return
  const { success } = await props.exitTokenApi(userRecord.value.user_id, record.token)
  if (success) {
    message.success('令牌已强制下线')
    tokens.value = tokens.value.filter((t) => t.token !== record.token)
  }
}

async function handleExitAll() {
  if (!userRecord.value) return
  const { success } = await props.exitAllApi(userRecord.value.user_id)
  if (success) {
    message.success('已强制下线全部令牌')
    emit('success')
    handleClose()
  }
}

function truncateToken(token: string) {
  if (token.length <= 20) return token
  return token.substring(0, 10) + '...' + token.slice(-10)
}

const columns = [
  { title: '令牌', key: 'token', width: 200 },
  { title: '创建时间', dataIndex: 'created_at', key: 'created_at', width: 170 },
  { title: '过期时间', key: 'timeout', width: 130 },
  { title: '设备类型', key: 'device', width: 100 },
  { title: '操作', key: 'action', width: 60, fixed: 'right' },
]

defineExpose({ doOpen })
</script>
