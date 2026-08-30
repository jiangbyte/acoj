<!-- Author: Charlie -->
<!-- 按 appStore.formStyle 在 NModal / NDrawer 间切换的详情外壳 -->
<script setup lang="ts">
import { computed } from 'vue'
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

const appStore = useAppStore()

const isModal = computed(() => appStore.formStyle === 'modal')

const widthStyle = computed(() => {
  const w = props.width
  return typeof w === 'number' ? `${w}px` : w
})

const modalStyle = computed(() => ({
  width: widthStyle.value,
  maxHeight: 'calc(100vh - 48px)',
}))

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
    :segmented="{ content: true }"
    @update:show="setShow"
  >
    <NScrollbar
      class="hei-modal-scroll"
      :style="{ maxHeight: 'min(620px, calc(100vh - 200px))' }"
    >
      <slot />
    </NScrollbar>
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
    </NDrawerContent>
  </NDrawer>
</template>
