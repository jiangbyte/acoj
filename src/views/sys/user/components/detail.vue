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
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">用户名</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.username || '-' }}</div>
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
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">手机</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.phone || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">性别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ $dict.label('GENDER', data.gender) }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">生日</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.birthday || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">座右铭</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.motto || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">GitHub</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              <template v-if="data.github">
                <a :href="data.github" target="_blank">{{ data.github }}</a>
              </template>
              <template v-else>-</template>
            </div>
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

      <a-card size="small" title="组织信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">组织</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.org_names?.join(' / ') || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">用户组</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.group_names?.join(' / ') || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">职位</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.position_name || '-' }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="登录信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">最后登录时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.last_login_at || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">最后登录IP</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.last_login_ip || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">登录次数</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.login_count ?? 0 }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建人</div>
            <div class="text-sm">
              <UserInfo :name="data.created_name" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.created_at || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新人</div>
            <div class="text-sm">
              <UserInfo :name="data.updated_name" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.updated_at || '-' }}
            </div>
          </a-col>
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserDetail' })
import { ref, watch } from 'vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchUserDetail } from '@/api/user'
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
  const { data: detail } = await fetchUserDetail({ id: row.id })
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
