<script setup lang="ts">
import { Icon } from '@iconify/vue'
import type { IconifyIcon } from '@iconify/vue'

const router = useRouter()
const route = useRoute()
const routes = computed(() => {
  return route.matched
})
</script>

<template>
  <n-breadcrumb>
    <n-breadcrumb-item
      v-for="(item) in routes"
      :key="item.path"
      @click="router.push(item.path)"
    >
      <n-icon>
        <Icon :icon="(item.meta.icon as string | IconifyIcon)" />
      </n-icon>
      {{ item.meta.title }}
    </n-breadcrumb-item>
  </n-breadcrumb>
</template>

<style scoped>

</style>
