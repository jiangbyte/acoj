<script setup lang="ts">
import type { MenuInst, MenuOption } from 'naive-ui'
import { useRouterStore, useSiteStore } from '@/stores'

const siteStore = useSiteStore()
const routerStore = useRouterStore()

const route = useRoute()
const menuInstRef = ref<MenuInst | null>(null)
watch(
  () => route.path,
  () => {
    menuInstRef.value?.showOption(routerStore.activeMenu as string)
  },
  { immediate: true },
)

/**
 * Only leaf menus can be selected; selecting a directory only expands it without changing the route or selection.
 */
function hasMenuChildren(option: MenuOption | null | undefined) {
  return !!(option?.children && option.children.length)
}

function findMenuOption(options: MenuOption[] | undefined, key: string | number): MenuOption | null {
  if (!options?.length) {
    return null
  }
  for (const option of options) {
    if (option.key === key) {
      return option
    }
    const found = findMenuOption(option.children as MenuOption[] | undefined, key)
    if (found) {
      return found
    }
  }
  return null
}

function handleMenuUpdate(key: string | number) {
  const option = findMenuOption(routerStore.menus as MenuOption[], key)
  if (hasMenuChildren(option)) {
    return
  }
  routerStore.setActiveMenu(String(key))
}

/**
 * Ant Design / SmartAdmin dark Menu palette。
 * @see https://github.com/ant-design/ant-design/blob/master/components/menu/style/theme.ts
 */
const menuThemeOverrides = {
  color: '#001529',
  itemColorHover: 'transparent',
  itemColorActive: '#1890ff',
  itemColorActiveHover: '#1890ff',
  itemColorActiveCollapsed: '#1890ff',
  itemTextColor: 'rgba(255, 255, 255, 0.65)',
  itemTextColorHover: '#ffffff',
  itemTextColorActive: '#ffffff',
  itemTextColorActiveHover: '#ffffff',
  itemTextColorChildActive: '#ffffff',
  itemTextColorChildActiveHover: '#ffffff',
  itemIconColor: 'rgba(255, 255, 255, 0.65)',
  itemIconColorHover: '#ffffff',
  itemIconColorActive: '#ffffff',
  itemIconColorActiveHover: '#ffffff',
  itemIconColorChildActive: '#ffffff',
  itemIconColorChildActiveHover: '#ffffff',
  arrowColor: 'rgba(255, 255, 255, 0.65)',
  arrowColorHover: '#ffffff',
  arrowColorActive: '#ffffff',
  arrowColorChildActive: '#ffffff',
  borderRadius: '0',
  fontSize: '14px',
}
</script>

<template>
  <n-menu
    ref="menuInstRef"
    inverted
    :root-indent="24"
    :indent="24"
    :options="routerStore.menus"
    :collapsed="siteStore.collapsed"
    :value="routerStore.activeMenu"
    :collapsed-width="64"
    :collapsed-icon-size="22"
    :theme-overrides="menuThemeOverrides"
    class="admin-sider-menu"
    @update:value="handleMenuUpdate"
  />
</template>

<style scoped>
.admin-sider-menu {
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  padding: 4px 0 12px;
  border-right: 0;
}

/* Keep the default NMenu vertical spacing */

.admin-sider-menu :deep(.n-menu-item-content:not(.n-menu-item-content--disabled):hover) {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
}

/* Use a darker expanded area with transparent child items and matching gaps */
.admin-sider-menu :deep(.n-submenu-children) {
  background-color: #000c17 !important;
}

.admin-sider-menu :deep(.n-submenu-children .n-menu-item-content) {
  background-color: transparent;
}

/*
 * Hierarchy indentation (aligned with Ant Design and SmartAdmin):
 * level 1: 24px, level 2: 48px, level 3: 72px
 */
.admin-sider-menu :deep(.n-menu-item-content) {
  padding-left: 24px !important;
}

.admin-sider-menu :deep(.n-submenu-children > .n-menu-item > .n-menu-item-content),
.admin-sider-menu :deep(.n-submenu-children > .n-submenu > .n-menu-item-content) {
  padding-left: 48px !important;
}

.admin-sider-menu :deep(.n-submenu-children .n-submenu-children > .n-menu-item > .n-menu-item-content),
.admin-sider-menu :deep(.n-submenu-children .n-submenu-children > .n-submenu > .n-menu-item-content) {
  padding-left: 72px !important;
}

.admin-sider-menu :deep(.n-menu-item-content--selected),
.admin-sider-menu :deep(.n-menu-item-content--selected:hover) {
  color: #fff !important;
  background-color: #1890ff !important;
}

.admin-sider-menu :deep(.n-menu-item-content--selected .n-menu-item-content-header),
.admin-sider-menu :deep(.n-menu-item-content--selected .n-menu-item-content__icon) {
  color: #fff !important;
}

.admin-sider-menu :deep(.n-menu-item-content::before) {
  display: none !important;
}

.admin-sider-menu :deep(a) {
  color: inherit !important;
  text-decoration: none;
}
</style>
