<script setup lang="ts">
import type { DropdownOption } from 'naive-ui'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTabStore } from '@/stores'
import { getRouteTitle } from '@/stores/route'
import { renderIcon } from '@/utils/icon'

const tabStore = useTabStore()
const router = useRouter()

const options = computed<DropdownOption[]>(() =>
  tabStore.allTabs.map((route) => ({
    label: getRouteTitle(route),
    key: route.fullPath,
    icon: renderIcon(route.meta.icon ?? undefined),
  })),
)

function handleDropTabs(key: string | number) {
  router.push(String(key))
}
</script>

<template>
  <n-dropdown :options="options" trigger="click" size="small" @select="handleDropTabs">
    <CommonWrapper>
      <NovaIcon icon="icon-park-outline:application-menu" />
    </CommonWrapper>
  </n-dropdown>
</template>
