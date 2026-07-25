<script setup lang="ts">
const dots = ref(Array.from({ length: 16 }, (_, i) => i))

onMounted(() => {
  setInterval(() => {
    dots.value = Array.from({ length: 16 }, () => Math.floor(Math.random() * 4))
  }, 300)
})
</script>

<template>
  <div class="grid grid-cols-4 gap-0.5">
    <div
      v-for="(dot, index) in dots"
      :key="index"
      class="size-1 rounded-full transition-all duration-300"
      :class="
        dot === 0
          ? 'bg-primary opacity-40 scale-75'
          : dot === 1
            ? 'bg-primary opacity-60 scale-90'
            : dot === 2
              ? 'bg-primary opacity-80 scale-100'
              : 'bg-primary scale-110 opacity-100'
      "
    />
  </div>
</template>
