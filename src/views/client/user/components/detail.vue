<template>
  <a-drawer
    :open="open"
    title="用户详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="24" class="mb-2">
            <a-avatar :size="72" :src="data.avatar" />
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">账号</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.account || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">昵称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.nickname || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">邮箱</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.email || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">性别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ $dict.label('GENDER', data.gender) }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">座右铭</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.motto || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">GitHub</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.github || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              <a-tag :color="$dict.color('USER_STATUS', data.status)">
                {{ $dict.label('USER_STATUS', data.status) }}
              </a-tag>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">登录次数</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.login_count ?? '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">最后登录 IP</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.last_login_ip || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">最后登录时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.last_login_at || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建人</div>
            <div class="text-sm"><UserInfo :name="data.created_name" /></div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_at || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新人</div>
            <div class="text-sm"><UserInfo :name="data.updated_name" /></div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.updated_at || '-' }}</div>
          </a-col>
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'ClientUserDetail' })
import { ref, watch } from 'vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchClientUserDetail } from '@/api/client-user'
import UserInfo from '@/components/user/UserInfo.vue'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const { drawerWidth } = useMobile()

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchClientUserDetail({ id: row.id })
  data.value = detail
  loading.value = false
  emit('update:open', true)
}

watch(
  () => props.open,
  v => {
    if (!v) data.value = null
  }
)

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
