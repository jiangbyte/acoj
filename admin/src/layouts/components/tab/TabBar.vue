<!-- Author: Charlie -->

<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDraggable } from 'vue-draggable-plus'
import { useTabScroll } from '@/hooks/useTabScroll'
import { useAppStore, useTabStore } from '@/stores'
import { getRouteTitle } from '@/stores/route'
import type { AppTab } from '@/stores/tab'
import { renderIcon } from '@/utils/icon'
import DropTabs from './DropTabs.vue'
import Reload from './Reload.vue'

const tabStore = useTabStore()
const appStore = useAppStore()
const router = useRouter()
const {
  scrollbar,
  touchScrolling,
  onWheel,
  onPointerDown,
  onPointerMove,
  onPointerEnd,
  onClickCapture,
} = useTabScroll(computed(() => tabStore.currentTabPath))
void scrollbar

const el = ref<HTMLElement>()

useDraggable(
  el,
  computed({
    get: () => tabStore.tabs,
    set: (value) => {
      tabStore.tabs = value
    },
  }),
  {
    animation: 150,
    ghostClass: 'ghost',
    delay: 180,
    delayOnTouchOnly: true,
    touchStartThreshold: 8,
    fallbackTolerance: 6,
  },
)

const currentTab = computed(() =>
  tabStore.allTabs.find((item) => item.fullPath === tabStore.currentTabPath),
)

const isCurrentAffixTab = computed(() => Boolean(currentTab.value?.meta.is_affix))

const options = computed<DropdownOption[]>(() => {
  const disabledCurrent = !currentTab.value || isCurrentAffixTab.value
  const disabledNormal = !tabStore.tabs.length

  return [
    {
      label: '刷新',
      key: 'reload',
      icon: renderIcon('icon-park-outline:redo'),
    },
    {
      label: '关闭当前',
      key: 'closeCurrent',
      icon: renderIcon('icon-park-outline:close'),
      disabled: disabledCurrent,
    },
    {
      label: '关闭其他',
      key: 'closeOther',
      icon: renderIcon('icon-park-outline:delete-four'),
      disabled: disabledCurrent || disabledNormal,
    },
    {
      label: '关闭左侧',
      key: 'closeLeft',
      icon: renderIcon('icon-park-outline:to-left'),
      disabled: disabledCurrent || disabledNormal,
    },
    {
      label: '关闭右侧',
      key: 'closeRight',
      icon: renderIcon('icon-park-outline:to-right'),
      disabled: disabledCurrent || disabledNormal,
    },
    {
      label: '关闭全部',
      key: 'closeAll',
      icon: renderIcon('icon-park-outline:fullwidth'),
      disabled: disabledNormal,
    },
  ]
})

function handleTab(route: AppTab) {
  router.push(route.fullPath)
}

function handleCloseTab(e: MouseEvent, fullPath: string) {
  e.stopPropagation()
  tabStore.closeTab(fullPath)
}

function handleSelect(key: string | number) {
  const path = currentTab.value?.fullPath
  if (!path) {
    return
  }

  const handleFn: Record<string, () => void> = {
    reload: () => appStore.reloadPage(),
    closeCurrent: () => tabStore.closeTab(path),
    closeOther: () => tabStore.closeOtherTabs(path),
    closeLeft: () => tabStore.closeLeftTabs(path),
    closeRight: () => tabStore.closeRightTabs(path),
    closeAll: () => tabStore.closeAllTabs(),
  }
  handleFn[String(key)]?.()
}

function isActive(fullPath: string) {
  return tabStore.currentTabPath === fullPath
}
</script>

<template>
  <div class="layout-tabs relative flex h-full w-full min-w-0 overflow-hidden">
    <n-scrollbar
      ref="scrollbar"
      class="relative h-full flex-1 min-w-0 tab-bar-scroller-wrapper"
      content-class="h-full tab-bar-scroller-content"
      :x-scrollable="true"
      trigger="none"
      @wheel="onWheel"
    >
      <div
        class="layout-tabs__track inline-flex h-full min-w-full relative"
        :class="{ 'is-touch-scrolling': touchScrolling }"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerEnd"
        @pointercancel="onPointerEnd"
        @click.capture="onClickCapture"
      >
        <div class="layout-tabs__group flex">
          <div
            v-for="item in tabStore.affixTabs"
            :key="item.fullPath"
            class="layout-tab"
            :class="{ 'is-active': isActive(item.fullPath) }"
            role="tab"
            :aria-selected="isActive(item.fullPath)"
            @click="handleTab(item)"
          >
            <NovaIcon
              v-if="item.meta.icon"
              class="layout-tab__icon"
              :icon="item.meta.icon"
              :size="14"
            />
            <span class="layout-tab__label">{{ getRouteTitle(item) }}</span>
          </div>
        </div>
        <div
          ref="el"
          class="layout-tabs__group flex flex-1"
        >
          <div
            v-for="item in tabStore.tabs"
            :key="item.fullPath"
            class="layout-tab"
            :class="{ 'is-active': isActive(item.fullPath) }"
            role="tab"
            :aria-selected="isActive(item.fullPath)"
            :data-tab-path="item.fullPath"
            @click="handleTab(item)"
          >
            <NovaIcon
              v-if="item.meta.icon"
              class="layout-tab__icon"
              :icon="item.meta.icon"
              :size="14"
            />
            <span class="layout-tab__label">{{ getRouteTitle(item) }}</span>
            <button
              type="button"
              class="layout-tab__close"
              :aria-label="'关闭'"
              @click="handleCloseTab($event, item.fullPath)"
            >
              <NovaIcon
                icon="icon-park-outline:close"
                :size="12"
              />
            </button>
          </div>
        </div>
      </div>
    </n-scrollbar>

    <div class="layout-tabs__actions flex h-full shrink-0 items-center">
      <Reload />
      <n-dropdown
        :options="options"
        trigger="click"
        placement="bottom-start"
        @select="handleSelect"
      >
        <CommonWrapper>
          <NovaIcon icon="icon-park-outline:setting-two" />
        </CommonWrapper>
      </n-dropdown>
      <DropTabs />
    </div>
  </div>
</template>

<style scoped>
/*
 * 白底整栏：与顶栏同色连贯；页签为下划线风格（非灰底芯片），右侧操作同白底。
 */
.layout-tabs {
  background: var(--card-color);
}

.layout-tabs :deep(.n-scrollbar-rail) {
  display: none !important;
}

.layout-tabs :deep(.n-scrollbar-container) {
  scrollbar-width: none;
}

.layout-tabs :deep(.n-scrollbar-container::-webkit-scrollbar) {
  width: 0;
  height: 0;
  display: none;
}

.layout-tabs__track {
  align-items: stretch;
  gap: 0;
  padding: 0 4px;
  touch-action: pan-y;
  overscroll-behavior-inline: contain;
}

.layout-tabs__track.is-touch-scrolling {
  user-select: none;
}

.layout-tabs__group {
  align-items: stretch;
  gap: 0;
  min-width: 0;
}

.layout-tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 100%;
  max-width: 168px;
  padding: 0 14px;
  margin: 0;
  border: 0;
  background: transparent;
  color: var(--n-text-color-2, #666);
  font-size: 13px;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.15s ease;
}

.layout-tab:hover {
  color: var(--primary-color);
}

.layout-tab.is-active {
  color: var(--primary-color);
  font-weight: 600;
}

.layout-tab.is-active::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 0;
  height: 2px;
  background: var(--primary-color);
}

.layout-tab__icon {
  flex-shrink: 0;
  opacity: 0.9;
}

.layout-tab__label {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.layout-tab__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  margin-left: 2px;
  padding: 0;
  border: 0;
  border-radius: var(--border-radius);
  background: transparent;
  color: inherit;
  opacity: 0.45;
  cursor: pointer;
  flex-shrink: 0;
}

.layout-tab__close:hover {
  opacity: 1;
  background: color-mix(in srgb, var(--primary-color) 12%, transparent);
  color: var(--primary-color);
}

.layout-tabs__actions {
  gap: 2px;
  padding: 0 8px;
  background: var(--card-color);
  border-left: 1px solid color-mix(in srgb, #000 5%, transparent);
}

html.dark .layout-tabs__actions {
  border-left-color: color-mix(in srgb, #fff 8%, transparent);
}

.layout-tabs__actions :deep(.common-wrapper) {
  min-width: 28px;
  height: 28px;
}

.ghost {
  opacity: 0.35;
}
</style>
