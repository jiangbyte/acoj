<script setup lang="ts">
import { useTabStore } from '@/stores'
// import type { RouteLocationNormalized } from 'vue-router'
import { Icon } from '@iconify/vue'
// import type { IconifyIcon } from '@iconify/vue'

// const scrollContainer = ref<HTMLElement | null>(null)

// function handleWheel(event: WheelEvent) {
//   if (event.deltaY !== 0) {
//     event.preventDefault()
//     if (scrollContainer.value) {
//       scrollContainer.value.scrollLeft += event.deltaY
//     }
//   }
// }

// onMounted(() => {
//   const container = scrollContainer.value
//   if (container) {
//     container.addEventListener('wheel', handleWheel)
//   }
// })

// onUnmounted(() => {
//   const container = scrollContainer.value
//   if (container) {
//     container.removeEventListener('wheel', handleWheel)
//   }
// })

const router = useRouter()
// function handleTab(route: RouteLocationNormalized) {
//   router.push(route.path)
// }

const tabStore = useTabStore()
// function closeHandle(path: string) {
//   tabStore.closeTab(path)
// }

// 切换标签页
function handleTabChange(path: string) {
  router.push(path)
  tabStore.setCurrent(path)
}

// 关闭标签页
function handleClose(name: string) {
  tabStore.closeTab(name)
}

const suffixOptions = [
  {
    label: '关闭所有标签',
    key: 'closeAll',
  },
  {
    label: '关闭其他标签',
    key: 'closeOther',
  },
]
function handleSelect(key: string | number) {
  if (key === 'closeAll') {
    tabStore.closeAllUnpinnedTabs()
  }
  else if (key === 'closeOther') {
    tabStore.closeOtherTabs()
  }
}
</script>

<template>
  <!-- <div ref="scrollContainer" class="flex gap-2 overflow-x-scroll overflow-y-hidden">
    <n-tag
      v-for="item in tabStore.pinedTabs"
      :key="item.path"
      :type="tabStore.currentPath === item.path ? 'info' : 'default'"
      @click="handleTab(item)"
      @close="closeHandle(item.path)"
    >
      <template #icon>
        <Icon :icon="(item.meta.icon as string | IconifyIcon)" />
      </template>
      {{ item.meta.title }}
    </n-tag>
    <n-tag
      v-for="item in tabStore.tabs"
      :key="item.path"
      closable
      :type="tabStore.currentPath === item.path ? 'info' : 'default'"
      @click="handleTab(item)"
      @close="closeHandle(item.path)"
    >
      <template #icon>
        <Icon :icon="(item.meta.icon as string | IconifyIcon)" />
      </template>
      {{ item.meta.title }}
    </n-tag>
  </div> -->
  <!-- <div></div> -->
  <n-tabs
    type="card"
    tab-style="min-width: 80px; padding: 3px 6px;"
    size="small"
    :value="tabStore.currentPath"
    :closable="true"
    @update:value="handleTabChange"
    @close="handleClose"
  >
    <!-- 固定的标签页 -->
    <n-tab
      v-for="item in tabStore.pinedTabs"
      :key="item.path"
      :name="item.path"
      :closable="false"
    >
      <template #default>
        <div class="flex items-center gap-1">
          <Icon :icon="String(item.meta.icon)" />
          <span>{{ item.meta.title }}</span>
        </div>
      </template>
    </n-tab>

    <!-- 普通标签页 -->
    <n-tab
      v-for="item in tabStore.tabs"
      :key="item.path"
      :name="item.path"
      :closable="true"
    >
      <template #default>
        <div class="flex items-center gap-1">
          <Icon :icon="String(item.meta.icon)" />
          <span>{{ item.meta.title }}</span>
        </div>
      </template>
    </n-tab>

    <template #prefix>
      <n-button text @click="$router.push('/')">
        <template #icon>
          <Icon icon="icon-park-outline:home" />
        </template>
      </n-button>
    </template>
    <template #suffix>
      <n-dropdown :options="suffixOptions" @select="handleSelect">
        <n-button text>
          <template #icon>
            <Icon icon="icon-park-outline:expand-down-one" />
          </template>
        </n-button>
      </n-dropdown>
    </template>
  </n-tabs>
</template>

<style scoped>
</style>
