<script setup lang="ts">
import { DownOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import type { VisitedTab } from '@/stores/app'
import { useAppStore } from '@/stores/app'
import { getIconComponent } from '@/utils/icons'
import { translateWithFallback } from '@/utils/i18n'

const app = useAppStore()
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

watch(
  () => route.fullPath,
  () => app.addVisitedTab(route),
  { immediate: true },
)

function fallbackPath(closedPath: string) {
  return app.getFallbackTabPath(closedPath)
}

function ensureCurrentTabVisible() {
  if (!app.all_tabs.some((item) => item.full_path === route.fullPath)) {
    router.push(app.getFallbackTabPath(route.fullPath))
  }
}

function closeTab(fullPath: string) {
  const nextPath = fallbackPath(fullPath)
  app.removeVisitedTab(fullPath)
  if (route.fullPath === fullPath) {
    router.push(nextPath)
  }
}

function handleMenuClick(command: string, fullPath: string) {
  if (command === 'close') {
    closeTab(fullPath)
  }
  if (command === 'closeLeft') {
    app.closeLeftTabs(fullPath)
  }
  if (command === 'closeRight') {
    app.closeRightTabs(fullPath)
  }
  if (command === 'closeOther') {
    app.closeOtherTabs(fullPath)
  }
  if (command === 'closeAll') {
    app.closeAllTabs()
  }
  ensureCurrentTabVisible()
}

function reloadCurrentTab() {
  router.go(0)
}

function getTabIcon(tab: VisitedTab) {
  return getIconComponent(tab.icon)
}

function isActiveTab(tab: VisitedTab) {
  return tab.full_path === route.fullPath
}

function getTagColor(tab: VisitedTab) {
  return isActiveTab(tab) ? 'processing' : undefined
}

function openTab(tab: VisitedTab) {
  if (tab.href) {
    window.open(tab.href, '_blank', 'noopener,noreferrer')
    return
  }
  if (!isActiveTab(tab)) {
    router.push(tab.full_path)
  }
}

function getTabTitle(tab: VisitedTab) {
  return translateWithFallback(tab.title_key, tab.title)
}

function handleTagClose(event: MouseEvent, tab: VisitedTab) {
  event.preventDefault()
  event.stopPropagation()
  closeTab(tab.full_path)
}

function handleActionClick(command: string) {
  if (command === 'refresh') {
    reloadCurrentTab()
    return
  }
  if (command === 'close') {
    closeTab(route.fullPath)
    return
  }
  if (command === 'closeLeft') {
    app.closeLeftTabs(route.fullPath)
  }
  if (command === 'closeRight') {
    app.closeRightTabs(route.fullPath)
  }
  if (command === 'closeOther') {
    app.closeOtherTabs(route.fullPath)
  }
  if (command === 'closeAll') {
    app.closeAllTabs()
  }
  ensureCurrentTabVisible()
}

const handleTagMenuClick = (tab: VisitedTab): MenuProps['onClick'] => ({ key }) => {
  handleMenuClick(String(key), tab.full_path)
}

const handleActionMenuClick: MenuProps['onClick'] = ({ key }) => {
  handleActionClick(String(key))
}
</script>

<template>
  <div class="h-10 border-t border-slate-100 bg-white dark:border-zinc-800 dark:bg-zinc-900">
    <div class="flex max-w-full items-center overflow-hidden">
      <div class="no-scrollbar min-w-0 flex-1 overflow-x-auto overflow-y-hidden">
        <div class="flex h-10 items-center gap-1.5 whitespace-nowrap px-2">
          <ADropdown
            v-for="tab in app.all_tabs"
            :key="tab.full_path"
            :trigger="['contextmenu']"
          >
            <ATag
              :aria-current="isActiveTab(tab) ? 'page' : undefined"
              :closable="tab.closable && app.all_tabs.length > 1"
              :color="getTagColor(tab)"
              class="relative m-0 inline-flex h-7 max-w-[132px] cursor-pointer select-none items-center overflow-hidden rounded-1.5 border px-2 text-12px leading-7 transition focus-within:ring-2 focus-within:ring-brand-500/20 sm:max-w-[180px]"
              :class="
                isActiveTab(tab)
                  ? 'border-brand-500! text-brand-700! font-600 dark:border-brand-400! dark:bg-brand-500/18! dark:text-brand-200!'
                  : 'border-slate-200! bg-slate-50! text-slate-500! hover:border-slate-300! hover:bg-white! hover:text-slate-700! dark:border-zinc-700! dark:bg-zinc-800! dark:text-zinc-400! dark:hover:border-zinc-600! dark:hover:bg-zinc-800/80! dark:hover:text-zinc-200!'
              "
              @click="openTab(tab)"
              @close="(event: MouseEvent) => handleTagClose(event, tab)"
            >
              <span
                v-if="isActiveTab(tab)"
                class="absolute left-1 right-1 top-0 h-0.5 rounded-b bg-brand-500 dark:bg-brand-300"
              />
              <span class="inline-flex min-w-0 items-center gap-1.5">
                <component :is="getTabIcon(tab)" v-if="getTabIcon(tab)" class="shrink-0 text-13px" />
                <span class="min-w-0 truncate">{{ getTabTitle(tab) }}</span>
              </span>
            </ATag>
            <template #overlay>
              <AMenu @click="handleTagMenuClick(tab)">
                <AMenuItem key="close" :disabled="!tab.closable">{{ t('tabs.closeCurrentTab') }}</AMenuItem>
                <AMenuItem key="closeRight">{{ t('tabs.closeRight') }}</AMenuItem>
                <AMenuItem key="closeLeft">{{ t('tabs.closeLeft') }}</AMenuItem>
                <AMenuItem key="closeOther">{{ t('tabs.closeOther') }}</AMenuItem>
                <AMenuDivider />
                <AMenuItem key="closeAll">{{ t('tabs.closeAll') }}</AMenuItem>
              </AMenu>
            </template>
          </ADropdown>
        </div>
      </div>
      <div class="flex h-10 shrink-0 items-center gap-0.5 border-l border-slate-100 px-1 sm:gap-1 sm:px-2 dark:border-zinc-800">
        <ATooltip :title="t('tabs.refreshCurrent')">
          <AButton
            :aria-label="t('tabs.refreshCurrent')"
            class="inline-flex! h-8! w-8! items-center! justify-center! rounded-2! p-0! text-slate-600! hover:bg-slate-100! dark:text-zinc-300! dark:hover:bg-zinc-800!"
            type="text"
            @click="reloadCurrentTab"
          >
            <ReloadOutlined />
          </AButton>
        </ATooltip>
        <ADropdown placement="bottomRight" :trigger="['click']">
          <AButton
            :aria-label="t('tabs.tabActions')"
            class="inline-flex! h-8! items-center! gap-1! rounded-2! px-1.5! text-13px! text-slate-600! hover:bg-slate-100! sm:px-2! dark:text-zinc-300! dark:hover:bg-zinc-800!"
            type="text"
          >
            <span class="hidden sm:inline">{{ t('tabs.operations') }}</span>
            <DownOutlined class="text-10px" />
          </AButton>
          <template #overlay>
            <AMenu @click="handleActionMenuClick">
              <AMenuItem key="refresh">{{ t('tabs.refresh') }}</AMenuItem>
              <AMenuItem key="close">{{ t('tabs.closeCurrent') }}</AMenuItem>
              <AMenuItem key="closeRight">{{ t('tabs.closeRight') }}</AMenuItem>
              <AMenuItem key="closeLeft">{{ t('tabs.closeLeft') }}</AMenuItem>
              <AMenuItem key="closeOther">{{ t('tabs.closeOther') }}</AMenuItem>
              <AMenuDivider />
              <AMenuItem key="closeAll">{{ t('tabs.closeAll') }}</AMenuItem>
            </AMenu>
          </template>
        </ADropdown>
      </div>
    </div>
  </div>
</template>
