<script setup lang="ts">
defineProps<{
  location: string
  temperature: number
  condition: string
  humidity: number
  windSpeed: number
}>()

const conditionIcons: Record<string, string> = {
  sunny: 'i-lucide-sun',
  cloudy: 'i-lucide-cloud',
  rainy: 'i-lucide-cloud-rain',
  snowy: 'i-lucide-cloud-snow',
}

const conditionLabels: Record<string, string> = {
  sunny: '晴',
  cloudy: '多云',
  rainy: '雨',
  snowy: '雪',
}
</script>

<template>
  <div
    class="rounded-lg border border-default p-4 bg-gradient-to-br from-sky-400 via-blue-500 to-indigo-600 text-white"
  >
    <p class="text-sm font-medium opacity-90">{{ location }}</p>
    <div class="flex items-end gap-2 mt-1">
      <UIcon :name="conditionIcons[condition] || 'i-lucide-sun'" class="size-8" />
      <span class="text-3xl font-bold">{{ temperature }}°</span>
      <span class="text-sm opacity-80 mb-1">{{ conditionLabels[condition] || condition }}</span>
    </div>
    <div class="flex gap-4 mt-2 text-xs opacity-80">
      <span>湿度: {{ humidity }}%</span>
      <span>风速: {{ windSpeed }} km/h</span>
    </div>
  </div>
</template>
