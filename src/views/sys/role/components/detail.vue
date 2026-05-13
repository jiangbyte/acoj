<template>
  <a-drawer
    :open="open"
    title="角色详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">角色名称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">角色编码</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.code || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">角色类别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('ROLE_CATEGORY', data.category) }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.sort_code ?? '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm">
              <a-tooltip title="禁用后仅不可被选择，不影响已绑定的数据">
                <a-tag :color="$dict.color('SYS_STATUS', data.status)">
                  {{ $dict.label('SYS_STATUS', data.status) }}
                </a-tag>
              </a-tooltip>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">描述</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.description || '-' }}
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.created_by || '-' }}
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
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.updated_by || '-' }}
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { fetchRoleDetail } from '@/api/role'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => {
    isMobile.value = e.matches
  }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchRoleDetail({ id: row.id })
  data.value = detail
  loading.value = false
  emit('update:open', true)
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
