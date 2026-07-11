<template>
  <u-popup
    :show="visible"
    mode="right"
    width="86%"
    :safe-area-inset-bottom="true"
    @close="close"
  >
    <u-card title="筛选">
      <template #body>
        <FormFields v-model="model" :fields="fields" mode="search" />
      </template>
      <template #foot>
        <view class="filter-actions">
          <u-button text="重置" plain @click="reset"></u-button>
          <u-button text="应用" type="primary" @click="apply"></u-button>
        </view>
      </template>
    </u-card>
  </u-popup>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FieldConfig } from '@/config/resource'
import FormFields from './FormFields.vue'

const props = defineProps<{
  modelValue: boolean
  fields: FieldConfig[]
  values: Record<string, any>
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'apply', value: Record<string, any>): void
}>()

const visible = ref(props.modelValue)
const model = ref<Record<string, any>>({ ...props.values })

watch(
  () => props.modelValue,
  (value) => {
    visible.value = value
    if (value) {
      model.value = { ...props.values }
    }
  }
)

watch(visible, (value) => emit('update:modelValue', value))

function close() {
  visible.value = false
}

function reset() {
  model.value = {}
}

function apply() {
  emit('apply', { ...model.value })
  close()
}
</script>

<style lang="scss" scoped>
.filter-actions {
  display: flex;
  gap: 12px;
}
</style>
