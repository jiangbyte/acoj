<script setup lang="ts">
import { useModels } from '~/composables/ai/useModels'

const { model, models } = useModels()
</script>

<template>
  <USelectMenu
    v-model="model"
    :items="models"
    color="neutral"
    variant="ghost"
    size="sm"
    class="min-w-0"
  >
    <template #label>
      <div class="flex items-center gap-1.5 truncate max-w-32">
        <UIcon name="i-lucide-sparkles" class="size-3.5 shrink-0 text-muted" />
        <span class="truncate text-xs">{{
          models.find((m) => m.value === model)?.label ?? model
        }}</span>
      </div>
    </template>

    <template #item="{ item }">
      <div class="flex items-center gap-2">
        <UIcon :name="item.icon" class="size-4 text-muted" />
        <span>{{ item.label }}</span>
      </div>
    </template>
  </USelectMenu>
</template>
