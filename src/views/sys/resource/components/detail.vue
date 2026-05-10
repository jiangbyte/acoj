<template>
  <a-drawer
    :open="open"
    title="资源详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">资源名称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">资源编码</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.code || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">资源分类</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ categoryMap[data.category] || data.category || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">资源类型</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ typeMap[data.type] || data.type || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">路由路径</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.route_path || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">组件路径</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.component_path || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">图标</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.icon || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.sort_code ?? '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm">
              <a-tag :color="data.status === 'ENABLED' ? 'green' : 'red'">
                {{ data.status === 'ENABLED' ? '启用' : '禁用' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">描述</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.description || '-' }}</div>
          </a-col>
          <a-col v-if="data.external_url" :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">外链地址</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.external_url }}</div>
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const categoryMap: Record<string, string> = {
  BACKEND_MENU: '后台菜单',
  FRONTEND_MENU: '前台菜单',
  BACKEND_BUTTON: '后台按钮',
  FRONTEND_BUTTON: '前台按钮',
}

const typeMap: Record<string, string> = {
  DIRECTORY: '目录',
  MENU: '菜单',
  BUTTON: '按钮',
  INTERNAL_LINK: '内链',
  EXTERNAL_LINK: '外链',
}

const isMobile = ref(false)
onMounted(() => {
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

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
