<template>
  <a-select
    v-if="optionType === 'dropdown'"
    v-model:value="modelValue"
    :options="dictOptions"
    :placeholder="placeholder"
    :disabled="disabled"
    :size="size"
    :allow-clear="allowClear"
    @change="handleChange"
  />
  <a-radio-group
    v-else-if="optionType === 'radio' || optionType === 'button'"
    v-model:value="modelValue"
    :options="dictOptions"
    :option-type="optionType === 'button' ? 'button' : undefined"
    :disabled="disabled"
    :size="size"
    @change="handleChange"
  />
  <a-checkbox-group
    v-else-if="optionType === 'checkbox'"
    v-model:value="modelValue"
    :options="dictOptions"
    :disabled="disabled"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useDictStore } from '@/store'

defineOptions({ name: 'DictSelect' })

const props = withDefaults(
  defineProps<{
    typeCode: string
    optionType?: 'dropdown' | 'radio' | 'button' | 'checkbox'
    placeholder?: string
    disabled?: boolean
    size?: 'small' | 'middle' | 'large'
    allowClear?: boolean
  }>(),
  {
    optionType: 'dropdown',
    placeholder: '请选择',
    disabled: false,
    size: 'middle',
    allowClear: true,
  }
)

const modelValue = defineModel<any>({ required: true })
const emit = defineEmits<{ change: [value: any] }>()

const dictStore = useDictStore()
const dictOptions = computed(() =>
  dictStore.getDictItems(props.typeCode).map((item: any) => ({
    ...item,
    disabled: item.status === 'DISABLED',
  }))
)

onMounted(() => {
  dictStore.loadDict()
})

function handleChange(e: any) {
  emit('change', e?.target?.value !== undefined ? e.target.value : e)
}
</script>
