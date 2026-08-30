<!-- Author: Charlie -->

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
  saving?: boolean
  hideActions?: boolean
}>()

const emit = defineEmits<{
  save: []
  reset: []
}>()
</script>

<template>
  <div>
    <div
      v-if="title || description"
      class="mb-12px"
    >
      <h3
        v-if="title"
        class="m-0 mb-4px text-14px font-600 leading-tight"
      >
        {{ title }}
      </h3>
      <p
        v-if="description"
        class="sys-config__hint"
      >
        {{ description }}
      </p>
    </div>

    <slot />

    <div
      v-if="!hideActions"
      class="sys-config-section__footer"
    >
      <NButton
        type="primary"
        :loading="saving"
        @click="emit('save')"
      >
        保存
      </NButton>
      <NButton
        :disabled="saving"
        @click="emit('reset')"
      >
        重置
      </NButton>
    </div>
  </div>
</template>
