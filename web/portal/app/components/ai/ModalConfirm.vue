<script setup lang="ts">
defineProps<{
  open: boolean
  title: string
  description: string
  color?: 'error' | 'primary' | 'neutral'
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: []
  cancel: []
}>()
</script>

<template>
  <UModal :open="open" :title="title" :description="description" :dismissible="false">
    <template #body>
      <p class="text-sm text-muted">{{ description }}</p>
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          label="取消"
          color="neutral"
          variant="outline"
          type="button"
          @click="emit('cancel')"
        />
        <UButton :label="title" :color="color ?? 'error'" type="button" @click="emit('confirm')" />
      </div>
    </template>
  </UModal>
</template>
