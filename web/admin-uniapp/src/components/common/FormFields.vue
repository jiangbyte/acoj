<template>
  <u-form :model="localModel">
    <u-form-item
      v-for="field in visibleFields"
      :key="field.prop"
      :label="field.label"
      :required="field.required"
    >
      <u-input
        v-if="inputTypes.includes(field.type || 'text')"
        v-model="localModel[field.prop]"
        :type="field.type === 'password' ? 'password' : 'text'"
        :placeholder="field.placeholder || `请输入${field.label}`"
        :disabled="field.readonly"
        border="none"
      ></u-input>

      <u-textarea
        v-else-if="field.type === 'textarea'"
        v-model="localModel[field.prop]"
        :placeholder="field.placeholder || `请输入${field.label}`"
        :disabled="field.readonly"
        border="none"
      ></u-textarea>

      <u-number-box
        v-else-if="field.type === 'number'"
        v-model="localModel[field.prop]"
        :min="0"
      ></u-number-box>

      <u-switch
        v-else-if="field.type === 'switch'"
        v-model="localModel[field.prop]"
      ></u-switch>

      <view
        v-else-if="field.type === 'select' || field.type === 'radio'"
        class="picker"
        @click="openPicker(field)"
      >
        <u-input
          :value="selectedText(field)"
          :placeholder="`请选择${field.label}`"
          disabled
          border="none"
        ></u-input>
      </view>

      <view v-else-if="field.type === 'image'" class="image-field">
        <u-image
          v-if="localModel[field.prop]"
          :src="localModel[field.prop]"
          mode="aspectFill"
        ></u-image>
        <u-button text="选择图片" plain @click="chooseImage(field)"></u-button>
      </view>

      <u-input
        v-else-if="field.type === 'datetime'"
        v-model="localModel[field.prop]"
        :placeholder="field.placeholder || 'YYYY-MM-DD HH:mm:ss'"
        :disabled="field.readonly"
        border="none"
      ></u-input>
    </u-form-item>

    <u-picker
      :show="picker.show"
      :columns="picker.columns"
      key-name="label"
      @confirm="confirmPicker"
      @cancel="picker.show = false"
      @close="picker.show = false"
    ></u-picker>
  </u-form>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { FieldConfig, OptionItem } from '@/config/resource'
import { useDictStore } from '@/stores/dict'
import { dictList } from '@/utils/dict'
import { uploadFile } from '@/api'

const props = defineProps<{
  modelValue: Record<string, any>
  fields: FieldConfig[]
  mode?: 'create' | 'update' | 'search'
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: Record<string, any>): void
}>()

const dictStore = useDictStore()
const localModel = reactive<Record<string, any>>({})
const inputTypes = ['text', 'password']

const picker = reactive<{
  show: boolean
  field: FieldConfig | null
  columns: OptionItem[][]
}>({
  show: false,
  field: null,
  columns: [[]],
})

const visibleFields = computed(() =>
  props.fields.filter((field) => {
    if (field.type === 'hidden') {
      return false
    }
    if (props.mode === 'create' && field.updateOnly) {
      return false
    }
    if (props.mode === 'update' && field.createOnly) {
      return false
    }
    return true
  })
)

watch(
  () => props.modelValue,
  (value) => {
    Object.keys(localModel).forEach((key) => delete localModel[key])
    Object.assign(localModel, value ?? {})
  },
  { immediate: true, deep: true }
)

watch(
  localModel,
  () => {
    emit('update:modelValue', { ...localModel })
  },
  { deep: true }
)

function fieldOptions(field: FieldConfig) {
  if (field.options) {
    return field.options
  }
  const remoteOptions = field.dictCode ? dictList(field.dictCode) : []
  return remoteOptions.length
    ? remoteOptions
    : dictStore.options(field.dictCode)
}

function selectedText(field: FieldConfig) {
  const value = localModel[field.prop]
  if (value === undefined || value === null || value === '') {
    return ''
  }
  return (
    fieldOptions(field).find((item) => String(item.value) === String(value))
      ?.label ?? String(value)
  )
}

function openPicker(field: FieldConfig) {
  picker.field = field
  picker.columns = [fieldOptions(field)]
  picker.show = true
}

function confirmPicker(event: any) {
  const option = event.value?.[0]
  if (picker.field && option) {
    localModel[picker.field.prop] = option.value
  }
  picker.show = false
}

function chooseImage(field: FieldConfig) {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const path = res.tempFilePaths[0]
      try {
        const uploaded = await uploadFile('/sys/file/upload', path)
        localModel[field.prop] = uploaded.url || uploaded.avatar || uploaded
      } catch {
        localModel[field.prop] = path
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.picker {
  width: 100%;
}

.image-field {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
