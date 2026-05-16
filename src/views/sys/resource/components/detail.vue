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
            <div class="detail-value">{{ $dict.label('RESOURCE_CATEGORY', data.category) }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">资源类型</div>
            <div class="detail-value">{{ $dict.label('RESOURCE_TYPE', data.type) }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">排序</div>
            <div class="detail-value">{{ data.sort_code ?? '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="detail-label">状态</div>
            <div class="detail-value">
              <a-tooltip title="禁用后仅不可被选择，不影响已绑定的数据">
                <a-tag :color="$dict.color('SYS_STATUS', data.status)">
                  {{ $dict.label('SYS_STATUS', data.status) }}
                </a-tag>
              </a-tooltip>
            </div>
          </a-col>
          <a-col v-if="data.description" :xs="24" :sm="24">
            <div class="detail-label">描述</div>
            <div class="detail-value">{{ data.description }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card v-if="isRouteType" size="small" title="路由配置" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col v-if="data.route_path" :xs="24" :sm="12">
            <div class="detail-label">路由路径</div>
            <div class="detail-value">{{ data.route_path }}</div>
          </a-col>
          <a-col v-if="data.component_path" :xs="24" :sm="12">
            <div class="detail-label">组件路径</div>
            <div class="detail-value">{{ data.component_path }}</div>
          </a-col>
          <a-col v-if="data.redirect_path" :xs="24" :sm="12">
            <div class="detail-label">重定向</div>
            <div class="detail-value">{{ data.redirect_path }}</div>
          </a-col>
        </a-row>
      </a-card>

      <a-card v-if="isRouteType" size="small" title="显示配置" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col v-if="data.icon" :xs="24" :sm="12">
            <div class="detail-label">图标</div>
            <div class="detail-value">{{ data.icon }}</div>
          </a-col>
          <a-col v-if="data.color" :xs="24" :sm="12">
            <div class="detail-label">颜色</div>
            <div class="detail-value">{{ data.color }}</div>
          </a-col>
          <a-col :xs="8" :sm="6">
            <div class="detail-label">可见</div>
            <div class="detail-value">
              <a-tag :color="$dict.color('SYS_YES_NO', data.is_visible)">
                {{ $dict.label('SYS_YES_NO', data.is_visible) }}
              </a-tag>
            </div>
          </a-col>
          <a-col v-if="isMenuType" :xs="8" :sm="6">
            <div class="detail-label">缓存</div>
            <div class="detail-value">
              <a-tag :color="$dict.color('SYS_YES_NO', data.is_cache)">
                {{ $dict.label('SYS_YES_NO', data.is_cache) }}
              </a-tag>
            </div>
          </a-col>
          <a-col v-if="isMenuType" :xs="8" :sm="6">
            <div class="detail-label">固定</div>
            <div class="detail-value">
              <a-tag :color="$dict.color('SYS_YES_NO', data.is_affix)">
                {{ $dict.label('SYS_YES_NO', data.is_affix) }}
              </a-tag>
            </div>
          </a-col>
          <a-col v-if="isMenuType" :xs="8" :sm="6">
            <div class="detail-label">面包屑</div>
            <div class="detail-value">
              <a-tag :color="$dict.color('SYS_YES_NO', data.is_breadcrumb)">
                {{ $dict.label('SYS_YES_NO', data.is_breadcrumb) }}
              </a-tag>
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
            <div class="detail-label">创建时间</div>
            <div class="detail-value">{{ data.created_at || '-' }}</div>
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
import { ref, computed } from 'vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchResourceDetail } from '@/api/resource'
defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const isRouteType = computed(
  () => data.value && ['DIRECTORY', 'MENU', 'INTERNAL_LINK'].includes(data.value.type)
)
const isMenuType = computed(() => data.value && ['MENU', 'INTERNAL_LINK'].includes(data.value.type))

const { drawerWidth } = useMobile()

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchResourceDetail({ id: row.id })
  data.value = detail
  loading.value = false
  emit('update:open', true)
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
