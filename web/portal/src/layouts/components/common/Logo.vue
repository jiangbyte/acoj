<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const name = import.meta.env.VITE_APP_TITLE
const homePath = import.meta.env.VITE_HOME_PATH
</script>

<template>
  <n-el
    tag="button"
    class="h-12 min-w-0 inline-flex items-center gap-3 border-0 bg-transparent p-0 text-left text-[var(--text-color-base)] cursor-pointer"
    type="button"
    @click="router.push(homePath)"
  >
    <span
      class="h-38px w-38px shrink-0 inline-flex items-center justify-center rounded-2 bg-[var(--primary-color)] text-white"
    >
      <NovaIcon icon="icon-park-outline:code-computer" :size="24" />
    </span>
    <span class="min-w-0 flex flex-col">
      <span class="truncate text-base font-700 leading-5">{{ name }}</span>
    </span>
  </n-el>
</template>
