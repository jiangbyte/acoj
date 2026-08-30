<!-- Author: Charlie -->
<!-- 按 appStore.formStyle 在 NModal / NDrawer 间切换的表单外壳 -->
<script setup lang="ts">
import { computed, useSlots } from 'vue'
import { useAppStore } from '@/stores'

const props = withDefaults(
  defineProps<{
    show: boolean
    title?: string
    width?: number | string
    maskClosable?: boolean
    draggable?: boolean
  }>(),
  {
    title: '',
    width: 720,
    maskClosable: false,
    draggable: true,
  },
)

const emit = defineEmits<{
  'update:show': [value: boolean]
}>()

const slots = useSlots()
const appStore = useAppStore()

const isModal = computed(() => appStore.formStyle === 'modal')
const hasAction = computed(() => !!slots.action)

const widthStyle = computed(() => {
  const w = props.width
  return typeof w === 'number' ? `${w}px` : w
})

const modalStyle = computed(() => ({
  width: widthStyle.value,
  maxHeight: 'calc(100vh - 48px)',
}))

const modalSegmented = computed(() =>
  hasAction.value ? { content: true, action: true } : { content: true },
)

function setShow(value: boolean) {
  emit('update:show', value)
}
</script>

<template>
  <NModal
    v-if="isModal"
    :show="show"
    preset="card"
    :draggable="draggable"
    :mask-closable="maskClosable"
    :title="title"
    :style="modalStyle"
    :segmented="modalSegmented"
    @update:show="setShow"
  >
    <!-- 弹窗限高滚动；抽屉交给 NDrawerContent，勿再套定高 -->
    <NScrollbar
      class="hei-modal-scroll"
      :style="{ maxHeight: 'min(620px, calc(100vh - 200px))' }"
    >
      <slot />
    </NScrollbar>
    <template
      v-if="hasAction"
      #action
    >
      <slot name="action" />
    </template>
  </NModal>

  <NDrawer
    v-else
    :show="show"
    :width="width"
    :mask-closable="maskClosable"
    placement="right"
    @update:show="setShow"
  >
    <NDrawerContent
      :title="title"
      closable
      :native-scrollbar="false"
    >
      <slot />
      <template
        v-if="hasAction"
        #footer
      >
        <slot name="action" />
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
