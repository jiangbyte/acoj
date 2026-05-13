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
            <div class="detail-label">资源名称</div>
            <div class="detail-value">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">资源编码</div>
            <div class="detail-value">{{ data.code || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">资源分类</div>
            <div class="detail-value">{{ categoryMap[data.category] || data.category || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">资源类型</div>
            <div class="detail-value">{{ typeMap[data.type] || data.type || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">排序</div>
            <div class="detail-value">{{ data.sort_code ?? '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">状态</div>
            <div class="detail-value">
              <a-tag :color="data.status === 'ENABLED' ? 'green' : 'red'">
                {{ data.status === 'ENABLED' ? '启用' : '禁用' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" v-if="data.description">
            <div class="detail-label">描述</div>
            <div class="detail-value">{{ data.description }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card v-if="isRouteType" size="small" title="路由配置" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" v-if="data.route_path">
            <div class="detail-label">路由路径</div>
            <div class="detail-value">{{ data.route_path }}</div>
          </a-col>
          <a-col :xs="24" :sm="12" v-if="data.component_path">
            <div class="detail-label">组件路径</div>
            <div class="detail-value">{{ data.component_path }}</div>
          </a-col>
          <a-col :xs="24" :sm="12" v-if="data.redirect_path">
            <div class="detail-label">重定向</div>
            <div class="detail-value">{{ data.redirect_path }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card v-if="isRouteType" size="small" title="显示配置" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" v-if="data.icon">
            <div class="detail-label">图标</div>
            <div class="detail-value">{{ data.icon }}</div>
          </a-col>
          <a-col :xs="24" :sm="12" v-if="data.color">
            <div class="detail-label">颜色</div>
            <div class="detail-value">{{ data.color }}</div>
          </a-col>
          <a-col :xs="8" :sm="6">
            <div class="detail-label">可见</div>
            <div class="detail-value">
              <a-tag :color="data.is_visible !== 'NO' ? 'green' : 'red'">{{ data.is_visible !== 'NO' ? '是' : '否' }}</a-tag>
            </div>
          </a-col>
          <a-col :xs="8" :sm="6" v-if="isMenuType">
            <div class="detail-label">缓存</div>
            <div class="detail-value">
              <a-tag :color="data.is_cache === 'YES' ? 'blue' : 'default'">{{ data.is_cache === 'YES' ? '是' : '否' }}</a-tag>
            </div>
          </a-col>
          <a-col :xs="8" :sm="6" v-if="isMenuType">
            <div class="detail-label">固定</div>
            <div class="detail-value">
              <a-tag :color="data.is_affix === 'YES' ? 'blue' : 'default'">{{ data.is_affix === 'YES' ? '是' : '否' }}</a-tag>
            </div>
          </a-col>
          <a-col :xs="8" :sm="6" v-if="isMenuType">
            <div class="detail-label">面包屑</div>
            <div class="detail-value">
              <a-tag :color="data.is_breadcrumb !== 'NO' ? 'green' : 'red'">{{ data.is_breadcrumb !== 'NO' ? '是' : '否' }}</a-tag>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card v-if="data.external_url" size="small" title="外链" class="mb-3">
        <div class="detail-label">外链地址</div>
        <div class="detail-value">{{ data.external_url }}</div>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="detail-label">创建人</div>
            <div class="detail-value">{{ data.created_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">创建时间</div>
            <div class="detail-value">{{ data.created_at || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">更新人</div>
            <div class="detail-value">{{ data.updated_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">更新时间</div>
            <div class="detail-value">{{ data.updated_at || '-' }}</div>
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

const isRouteType = computed(() =>
  data.value && ['DIRECTORY', 'MENU', 'INTERNAL_LINK'].includes(data.value.type)
)
const isMenuType = computed(() =>
  data.value && ['MENU', 'INTERNAL_LINK'].includes(data.value.type)
)

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

<style scoped>
.detail-label {
  font-size: 13px;
  color: var(--text-color-secondary, #00000073);
  margin-bottom: 4px;
}
.detail-value {
  font-size: 14px;
  color: var(--text-color, #000000d9);
}
</style>
