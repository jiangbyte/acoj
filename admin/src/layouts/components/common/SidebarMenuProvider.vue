<!-- Author: Charlie -->
<!--
  深色侧边栏专用主题 Provider：把包裹的 n-menu / n-scrollbar 固定渲染为深底浅字，
  不随全局 light/dark 模式切换（深色侧边栏 ≠ 暗黑模式）。
  折叠时子菜单走 NDropdown，需同步覆盖 Dropdown/Popover/common.popoverColor，
  否则浅色全局的白底会透出来（文字偏浅时像「空白白块」）。
  配色引用 style.css 中的模式感知变量（--sidebar-*）。
  fill=true（默认）：作为桌面侧栏 flex 子级撑满剩余高度。
  fill=false：用于移动端抽屉等场景，按内容/父级高度自然伸展，避免 flex 基线为 0 导致菜单不可见。
-->
<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'
import { darkTheme } from 'naive-ui'
import { computed } from 'vue'
import { useAppStore } from '@/stores'

const { fill = true } = defineProps<{
  fill?: boolean
}>()

const appStore = useAppStore()

const sidebarMenuThemeOverrides = computed<GlobalThemeOverrides>(() => {
  const radius = appStore.borderRadius
  return {
    // 折叠侧栏子菜单走 NDropdown/Popover，会吃到全局 light 的 popoverColor（白底）
    // 这里在侧栏 Provider 内覆盖，保证浅色模式下弹出层仍是深底浅字
    common: {
      popoverColor: 'var(--sidebar-bg)',
      cardColor: 'var(--sidebar-bg)',
      modalColor: 'var(--sidebar-bg)',
    },
    Menu: {
      color: 'transparent',
      itemColor: 'transparent',
      itemColorHover: 'var(--sidebar-item-hover-bg)',
      itemColorActive: 'var(--sidebar-item-active-bg)',
      itemColorActiveHover: 'var(--sidebar-item-active-hover-bg)',
      itemTextColor: 'var(--sidebar-text)',
      itemTextColorHover: 'var(--sidebar-text-hover)',
      itemTextColorActive: 'var(--sidebar-text-active)',
      itemTextColorActiveHover: 'var(--sidebar-text-active)',
      itemTextColorChildActive: 'var(--sidebar-text-active)',
      itemTextColorChildActiveHover: 'var(--sidebar-text-active)',
      itemIconColor: 'var(--sidebar-text)',
      itemIconColorHover: 'var(--sidebar-text-hover)',
      itemIconColorActive: 'var(--sidebar-text-active)',
      itemIconColorActiveHover: 'var(--sidebar-text-active)',
      itemIconColorChildActive: 'var(--sidebar-text-active)',
      itemIconColorChildActiveHover: 'var(--sidebar-text-active)',
      arrowColor: 'var(--sidebar-text)',
      arrowColorActive: 'var(--sidebar-text-active)',
      groupTextColor: 'var(--sidebar-group-text)',
      borderRadius: radius,
      popupColor: 'var(--sidebar-bg)',
      popupBorderRadius: radius,
    },
    Dropdown: {
      color: 'var(--sidebar-bg)',
      optionColorHover: 'var(--sidebar-item-hover-bg)',
      optionColorActive: 'var(--sidebar-item-active-bg)',
      optionTextColor: 'var(--sidebar-text)',
      optionTextColorHover: 'var(--sidebar-text-hover)',
      optionTextColorActive: 'var(--sidebar-text-active)',
      optionTextColorChildActive: 'var(--sidebar-text-active)',
      optionTextColorChildActiveHover: 'var(--sidebar-text-active)',
      optionIconColor: 'var(--sidebar-text)',
      optionIconColorHover: 'var(--sidebar-text-hover)',
      optionIconColorActive: 'var(--sidebar-text-active)',
      optionIconColorActiveHover: 'var(--sidebar-text-active)',
      optionIconColorChildActive: 'var(--sidebar-text-active)',
      optionIconColorChildActiveHover: 'var(--sidebar-text-active)',
      prefixColor: 'var(--sidebar-text)',
      suffixColor: 'var(--sidebar-text)',
      dividerColor: 'var(--sidebar-border)',
      borderRadius: radius,
    },
    Popover: {
      color: 'var(--sidebar-bg)',
      textColor: 'var(--sidebar-text)',
    },
  }
})
</script>

<template>
  <div
    class="sidebar-menu-provider"
    :class="{ 'sidebar-menu-provider--fill': fill }"
  >
    <n-config-provider
      :theme="darkTheme"
      :theme-overrides="sidebarMenuThemeOverrides"
    >
      <slot />
    </n-config-provider>
  </div>
</template>

<style scoped>
.sidebar-menu-provider {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
}

.sidebar-menu-provider :deep(.n-config-provider) {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
}

/* 桌面侧栏：作为 flex 子级撑满剩余高度 */
.sidebar-menu-provider--fill {
  min-height: 0;
  flex: 1 1 0;
}

.sidebar-menu-provider--fill :deep(.n-config-provider) {
  min-height: 0;
  flex: 1 1 0;
}
</style>
