<script setup lang="ts">
defineProps<{
  collapsed?: boolean
  dark?: boolean
}>()
</script>

<template>
  <div
    class="flex h-16 shrink-0 items-center overflow-hidden whitespace-nowrap bg-inherit transition-all"
    :class="collapsed ? 'justify-center px-0' : 'px-4'"
  >
    <div
      class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-2 bg-blue text-18px text-white font-700 shadow-sm"
    >
      H
    </div>
    <div v-if="!collapsed" class="ml-3 min-w-0">
      <h1 class="m-0 truncate text-17px font-700 leading-6" :class="dark ? 'text-zinc-50' : 'text-slate-900'">
        Hei Admin
      </h1>
      <p class="m-0 truncate text-11px leading-4" :class="dark ? 'text-zinc-400' : 'text-slate-500'">
        Enterprise Console
      </p>
    </div>
  </div>
</template>
