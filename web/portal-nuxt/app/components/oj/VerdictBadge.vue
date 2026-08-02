<script setup lang="ts">
const props = defineProps<{
  result?: string | null
  status?: string | null
}>()

const label = computed(() => props.result || props.status || '-')

const color = computed(() => {
  const r = (props.result || props.status || '').toUpperCase()
  if (r === 'AC' || r === 'COMPLETED')
    return 'success' as const
  if (['WA', 'TLE', 'MLE', 'RE', 'CE', 'OLE', 'SE', 'IE', 'FAILED'].includes(r))
    return 'error' as const
  if (['QUEUED', 'JUDGING'].includes(r))
    return 'warning' as const
  return 'neutral' as const
})
</script>

<template>
  <UBadge :color="color" variant="subtle" class="font-mono">
    {{ label }}
  </UBadge>
</template>
