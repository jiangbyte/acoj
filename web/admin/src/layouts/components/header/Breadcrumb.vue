<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const routes = computed(() => route.matched.filter((item) => item.meta.name))
</script>

<template>
  <TransitionGroup name="list" tag="ul" class="flex items-center gap-2">
    <template v-for="(item, index) in routes" :key="item.path">
      <li v-if="index > 0" class="breadcrumb-separator">/</li>
      <n-el
        tag="li"
        class="flex items-center gap-1 breadcrumb-item cursor-pointer"
        @click="router.push(item.path || '/')"
      >
        <NovaIcon :icon="item.meta.icon ?? undefined" />
        <span class="whitespace-nowrap">{{ item.meta.name }}</span>
      </n-el>
    </template>
  </TransitionGroup>
</template>

<style scoped>
.breadcrumb-item:hover {
  color: var(--primary-color);
}

.breadcrumb-separator {
  color: var(--text-color-3);
  user-select: none;
}
</style>
