<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  status: string
}>()

const { t } = useI18n()

const meta = computed(() => {
  const map: Record<string, { color: string; label: string }> = {
    ENABLED: { color: 'success', label: '启用' },
    DISABLED: { color: 'default', label: '停用' },
    LOCKED: { color: 'error', label: '锁定' },
    enabled: { color: 'success', label: '启用' },
    disabled: { color: 'default', label: '停用' },
    locked: { color: 'error', label: '锁定' },
    up: { color: 'success', label: '上升' },
    down: { color: 'warning', label: '下降' },
    flat: { color: 'processing', label: '稳定' },
  }
  return map[props.status] || { color: 'default', label: props.status }
})

const label = computed(() => {
  const translated = t(`status.${props.status}`)
  return translated === `status.${props.status}` ? meta.value.label : translated
})
</script>

<template>
  <ATag :color="meta.color">{{ label }}</ATag>
</template>
