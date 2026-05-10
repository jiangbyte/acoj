<template>
  <ADrawer
    :open="app.showSettings"
    title="主题设置"
    placement="right"
    width="360"
    @close="app.showSettings = false"
  >
    <div class="px-2">
      <!-- Theme Mode -->
      <h3 class="setting-section-title">整体风格</h3>
      <div class="flex gap-3 mb-6">
        <div
          v-for="item in themeList" :key="item.value"
          class="setting-card"
          :class="{ 'setting-card-active': app.theme === item.value }"
          @click="app.setTheme(item.value)"
        >
          <div class="setting-card-preview" :class="item.previewClass">
            <span class="preview-sider" />
            <span class="preview-header" />
          </div>
          <div class="setting-card-label">{{ item.label }}</div>
        </div>
      </div>

      <ADivider />

      <!-- Primary Color -->
      <h3 class="setting-section-title">主题色</h3>
      <div class="flex flex-wrap gap-3 mb-6">
        <ATooltip v-for="c in colorList" :key="c.color" :title="c.name">
          <ATag
            :color="c.color"
            class="!w-7 !h-7 !flex !items-center !justify-center !rounded-full !cursor-pointer !border-2 !p-0"
            :class="app.colorPrimary === c.color ? 'border-primary-dynamic' : 'border-transparent'"
            @click="app.setColorPrimary(c.color)"
          >
            <CheckOutlined v-if="app.colorPrimary === c.color" style="color: #fff; font-size: 12px" />
          </ATag>
        </ATooltip>
      </div>

      <ADivider />

      <!-- Toggle Options -->
      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <span>面包屑</span>
          <ASwitch :checked="app.showBreadcrumb" @change="app.setShowBreadcrumb($event)" />
        </div>
        <div class="flex items-center justify-between">
          <span>面包屑图标</span>
          <ASwitch :checked="app.showBreadcrumbIcon" @change="(v: boolean) => app.showBreadcrumbIcon = v" />
        </div>
        <div class="flex items-center justify-between">
          <span>多标签页</span>
          <ASwitch :checked="app.showTabs" @change="app.setShowTabs($event)" />
        </div>
        <div class="flex items-center justify-between">
          <span>页脚</span>
          <ASwitch :checked="app.showFooter" @change="app.setShowFooter($event)" />
        </div>
        <div class="flex items-center justify-between">
          <span>圆角风格</span>
          <ASwitch :checked="app.roundedCorners" @change="app.toggleRoundedCorners()" />
        </div>
        <div class="flex items-center justify-between">
          <span>灰色模式</span>
          <ASwitch :checked="app.grayMode" @change="app.toggleGrayMode()" />
        </div>
        <div class="flex items-center justify-between">
          <span>色弱模式</span>
          <ASwitch :checked="app.colorWeak" @change="app.toggleColorWeak()" />
        </div>
      </div>

      <ADivider />

      <AAlert message="以上配置可实时预览，建议只在开发环境下使用" type="warning" show-icon />
    </div>
  </ADrawer>
</template>

<script setup lang="ts">
import { CheckOutlined } from '@ant-design/icons-vue'
import { useAppStore } from '@/store'

const app = useAppStore()

const themeList = [
  { value: 'light' as const, label: '亮色', previewClass: 'theme-light' },
  { value: 'dark' as const, label: '暗色侧栏', previewClass: 'theme-dark' },
  { value: 'realDark' as const, label: '暗黑模式', previewClass: 'theme-realdark' },
]

const colorList = [
  { color: '#1677ff', name: '拂晓蓝' },
  { color: '#F5222D', name: '薄暮' },
  { color: '#FA541C', name: '火山' },
  { color: '#FAAD14', name: '日暮' },
  { color: '#52C41A', name: '极光绿' },
  { color: '#13C2C2', name: '明青' },
  { color: '#EB2F96', name: '胭脂粉' },
  { color: '#722ED1', name: '酱紫' },
  { color: '#2F54EB', name: '极客蓝' },
  { color: '#009688', name: '深绿' },
  { color: '#001529', name: '主题黑' },
]
</script>

<style scoped>
.setting-section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.setting-card {
  cursor: pointer;
  text-align: center;
}

.setting-card-preview {
  width: 48px;
  height: 40px;
  border-radius: 4px;
  border: 2px solid #d9d9d9;
  overflow: hidden;
  position: relative;
  background-color: #f0f0f0;
  box-shadow: 0 1px 2.5px rgba(0, 0, 0, 0.12);
  margin-bottom: 4px;
  transition: border-color 0.2s;
}

.setting-card-active .setting-card-preview {
  border-color: var(--primary-color);
}

.setting-card-label {
  font-size: 12px;
  color: #999;
}

/* ===== Shared preview element styles ===== */
.preview-sider,
.preview-header {
  position: absolute;
  display: block;
}

.preview-sider {
  top: 0;
  left: 0;
  width: 33%;
  height: 100%;
  z-index: 1;
}

.preview-header {
  top: 0;
  left: 0;
  width: 100%;
  height: 25%;
}

/* ===== Theme previews ===== */
/* Light */
.theme-light .preview-sider {
  background-color: #fff;
}
.theme-light .preview-header {
  background-color: #fff;
}

/* Dark (dark sider, light header) */
.theme-dark .preview-sider {
  background-color: #001529;
}
.theme-dark .preview-header {
  background-color: #fff;
}

/* realDark (dark all) */
.theme-realdark {
  background-color: #1a1a2e;
}
.theme-realdark .preview-sider {
  background-color: #16213e;
}
.theme-realdark .preview-header {
  background-color: #1a1a2e;
}

</style>
