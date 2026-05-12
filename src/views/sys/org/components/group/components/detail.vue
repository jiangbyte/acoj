<template>
  <a-drawer
    :open="open"
    title="用户组详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">用户组编码</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.code || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">用户组名称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">用户组类别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ categoryMap[data.category] || data.category || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">所属组织</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ orgNameMap[data.org_id] || data.org_id || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm">
              <a-tag :color="data.status === 'ENABLED' ? 'green' : 'red'">
                {{ data.status === 'ENABLED' ? '启用' : '禁用' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.sort_code ?? '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">描述</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.description || '-' }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_at || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.updated_by || '-' }}</div>
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
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { orgApi } from '@/api/org'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const categoryMap: Record<string, string> = {
  ROLE: '角色组',
  DEPT: '部门组',
  PROJECT: '项目组',
  OTHER: '其他',
}

const orgNameMap = ref<Record<string, string>>({})

async function loadOrgNames() {
  const { data: tree } = await orgApi.tree({})
  const map: Record<string, string> = {}
  function walk(nodes: any[]) {
    for (const n of nodes) {
      map[n.id] = n.name
      if (n.children) walk(n.children)
    }
  }
  if (tree) walk(tree)
  orgNameMap.value = map
}

const isMobile = ref(false)
onMounted(() => {
  loadOrgNames()
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

function doOpen(row: any) {
  loading.value = true
  data.value = null
  try {
    data.value = row
    emit('update:open', true)
  } finally {
    loading.value = false
  }
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
