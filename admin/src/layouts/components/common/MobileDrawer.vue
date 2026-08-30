<!-- Author: Charlie -->

<script setup lang="ts">
import Logo from './Logo.vue'
import SidebarMenuProvider from './SidebarMenuProvider.vue'

// 与父组件通过 v-model:show 双向绑定抽屉显隐状态，移动端点击菜单入口后由父组件打开。
const showDrawer = defineModel<boolean>('show', { default: false })
</script>

<template>
  <n-drawer
    v-model:show="showDrawer"
    :width="280"
    placement="left"
    :mask-closable="true"
    :close-on-esc="true"
  >
    <n-drawer-content
      class="dark-sidebar-drawer"
      :native-scrollbar="false"
      :body-content-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column', minHeight: 0 }"
    >
      <template #header>
        <Logo />
      </template>
      <!-- fill=false：避免侧栏专用 flex:1 基线为 0，在抽屉内把菜单高度压没 -->
      <SidebarMenuProvider :fill="false">
        <n-scrollbar class="mobile-drawer-menu-scroll">
          <div class="mobile-drawer-menu">
            <slot />
          </div>
        </n-scrollbar>
      </SidebarMenuProvider>
    </n-drawer-content>
  </n-drawer>
</template>

<!--
  注意：n-drawer 会把内容 teleport 到 body，scoped/:deep 选择器无法命中抽屉内部 DOM，
  因此抽屉的深色样式必须用全局选择器；配色引用 style.css 的 --sidebar-* 变量。
-->
<style>
.dark-sidebar-drawer {
  background-color: var(--sidebar-bg);
  color: var(--sidebar-text);
}

.dark-sidebar-drawer .n-drawer-header {
  background-color: var(--sidebar-bg);
  border-bottom: none !important;
}

.dark-sidebar-drawer .n-drawer-body {
  background-color: var(--sidebar-bg);
  flex: 1 1 auto;
  min-height: 0;
}

.dark-sidebar-drawer .n-drawer-body-content-wrapper {
  height: 100%;
}

.dark-sidebar-drawer .n-scrollbar-rail {
  background-color: transparent;
}

.mobile-drawer-menu-scroll {
  flex: 1 1 auto;
  height: 100%;
  min-height: 0;
}

.mobile-drawer-menu {
  padding: 8px 0 16px;
}

.mobile-drawer-menu .n-menu {
  background: transparent;
}
</style>
