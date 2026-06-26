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
const { scrollbar, onWheel } = useTabScroll(computed(() => tabStore.currentTabPath))
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
</script>

<template>
  <div class="relative flex h-full w-full min-w-0 overflow-hidden px-2">
    <n-scrollbar
      ref="scrollbar"
      class="relative h-full flex-1 min-w-0 tab-bar-scroller-wrapper"
      content-class="h-full tab-bar-scroller-content"
      :x-scrollable="true"
      @wheel="onWheel"
    >
      <div class="p-l-2 inline-flex h-full min-w-full items-center gap-1 relative">
        <div class="flex items-center gap-1">
          <n-tag
            v-for="item in tabStore.affixTabs"
            :key="item.fullPath"
            class="tab-item"
            :type="tabStore.currentTabPath === item.fullPath ? 'primary' : 'default'"
            @click="handleTab(item)"
          >
            <template #icon>
              <NovaIcon v-if="item.meta.icon" :icon="item.meta.icon" />
            </template>
            {{ getRouteTitle(item) }}
          </n-tag>
        </div>
        <div ref="el" class="flex items-center gap-1 flex-1">
          <n-tag
            v-for="item in tabStore.tabs"
            :key="item.fullPath"
            closable
            class="tab-item"
            :data-tab-path="item.fullPath"
            :type="tabStore.currentTabPath === item.fullPath ? 'primary' : 'default'"
            @close="handleCloseTab($event, item.fullPath)"
            @click="handleTab(item)"
          >
            <template #icon>
              <NovaIcon v-if="item.meta.icon" :icon="item.meta.icon" />
            </template>
            {{ getRouteTitle(item) }}
          </n-tag>
        </div>
      </div>
    </n-scrollbar>
    <n-el class="flex h-full shrink-0 items-center gap-1 bg-[var(--card-color)]">
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
    </n-el>
  </div>
</template>

<style scoped>
.tab-item {
  cursor: pointer;
  white-space: nowrap;
}
</style>
