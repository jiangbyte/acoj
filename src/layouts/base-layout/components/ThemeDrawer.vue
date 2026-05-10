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
      <h3 class="text-sm font-semibold mb-3">整体风格</h3>
      <div class="flex gap-3 mb-6">
        <div
          v-for="item in themeList"
          :key="item.value"
          class="cursor-pointer text-center"
          @click="app.setTheme(item.value)"
        >
          <div
            class="w-12 h-10 rounded border-2 overflow-hidden relative shadow-[0_1px_2.5px_rgba(0,0,0,0.12)] mb-1 transition-colors duration-200"
            :class="[
              app.theme === item.value ? 'border-[var(--primary-color)]' : 'border-[#d9d9d9]',
              item.containerClass || 'bg-[#f0f0f0]',
            ]"
          >
            <span class="absolute top-0 left-0 w-[33%] h-full z-1" :class="item.siderClass" />
            <span class="absolute top-0 left-0 w-full h-[25%]" :class="item.headerClass" />
          </div>
          <div class="text-xs text-[#999]">{{ item.label }}</div>
        </div>
      </div>

      <ADivider />

      <!-- Primary Color -->
      <h3 class="text-sm font-semibold mb-3">主题色</h3>
      <div class="flex flex-wrap gap-3 mb-6">
        <ATooltip v-for="c in colorList" :key="c.color" :title="c.name">
          <ATag
            :color="c.color"
            class="!w-7 !h-7 !flex !items-center !justify-center !rounded-full !cursor-pointer !border-2 !p-0"
            :class="app.colorPrimary === c.color ? '!border-[var(--primary-color)]' : '!border-transparent'"
            @click="app.setColorPrimary(c.color)"
          >
            <CheckOutlined
              v-if="app.colorPrimary === c.color"
              style="color: #fff; font-size: 12px"
            />
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
          <ASwitch
            :checked="app.showBreadcrumbIcon"
            @change="(v: boolean) => (app.showBreadcrumbIcon = v)"
          />
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

interface ThemeItem {
  value: 'light' | 'dark' | 'realDark'
  label: string
  siderClass: string
  headerClass: string
  containerClass?: string
}

const themeList: ThemeItem[] = [
  { value: 'light', label: '亮色', siderClass: 'bg-white', headerClass: 'bg-white' },
  { value: 'dark', label: '暗色侧栏', siderClass: 'bg-[#001529]', headerClass: 'bg-white' },
  {
    value: 'realDark',
    label: '暗黑模式',
    siderClass: 'bg-[#16213e]',
    headerClass: 'bg-[#1a1a2e]',
    containerClass: 'bg-[#1a1a2e]',
  },
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
