<script lang="ts" setup>
const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:value'])

const localValue = ref<string>(props.modelValue)

// 当父组件传递的值变化时，更新本地状态
watch(
  () => props.modelValue,
  (newValue) => {
    localValue.value = newValue
  },
)

// 当本地状态变化时，通知父组件
watch(localValue, (newValue) => {
  emit('update:value', newValue)
})

const sortOptions = [
  { label: '升序', value: 'asc' },
  { label: '降序', value: 'desc' },
]
</script>

<template>
  <NSelect
    v-model:value="localValue"
    :options="sortOptions"
    placeholder="请选择排序方式"
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
