<script lang="ts" setup>
const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:value'])

const localValue = ref<string>(props.modelValue)

// Update local state when the parent value changes
watch(
  () => props.modelValue,
  (newValue) => {
    localValue.value = newValue
  },
)

// Notify the parent when local state changes
watch(localValue, (newValue) => {
  emit('update:value', newValue)
})

const sortOptions = [
  { label: 'Ascending', value: 'asc' },
  { label: 'Descending', value: 'desc' },
]
</script>

<template>
  <NSelect
    v-model:value="localValue"
    :options="sortOptions"
    placeholder="Select sort order"
  />
  <!-- <NRadioGroup v-model:value="localValue">
    <NRadio
      v-for="option in sortOptions"
      :key="option.value"
      :value="option.value"
    >
      {{ option.label }}
    </NRadio>
  </NRadioGroup> -->
</template>

  <style>

  </style>
