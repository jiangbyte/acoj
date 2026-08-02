<script setup lang="ts">
const props = defineProps<{ status: string }>()

const meta = computed(() => {
  switch (props.status) {
    case 'SCHEDULED':
      return { label: '未开始', color: 'neutral' as const }
    case 'RUNNING':
      return { label: '进行中', color: 'success' as const }
    case 'ENDED':
      return { label: '已结束', color: 'warning' as const }
    case 'LOCKED':
      return { label: '已锁定', color: 'error' as const }
    default:
      return { label: props.status, color: 'neutral' as const }
  }
})
</script>

<template>
  <UBadge :color="meta.color" variant="subtle">
    {{ meta.label }}
  </UBadge>
</template>
