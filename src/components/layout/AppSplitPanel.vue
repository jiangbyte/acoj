<template>
  <div
    :class="['flex h-full relative', collapsed && 'app-split--collapsed', shouldHideLeft && 'app-split--hide-left']"
  >
    <!-- Left panel -->
    <div
      v-show="!shouldHideLeft"
      class="max-md:hidden flex-shrink-0 overflow-hidden transition-all duration-200"
      :style="leftStyle"
    >
      <div v-show="!collapsed" class="h-full overflow-auto">
        <slot name="left" />
      </div>
    </div>

    <!-- Resizer -->
    <div
      v-if="!collapsed && !shouldHideLeft"
      class="max-md:hidden group w-1.5 cursor-col-resize flex items-center justify-center flex-shrink-0 relative -mx-[1px] z-1"
      @mousedown.prevent="startResize"
      @dblclick="toggleCollapse"
    >
      <div class="w-0.5 h-8 rounded-sm bg-[var(--border-color,#f0f0f0)] transition-colors duration-200 group-hover:bg-primary" />
    </div>

    <!-- Right panel -->
    <div class="flex-1 overflow-hidden min-w-0 flex flex-col">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAppStore } from '@/store'

interface Props {
  collapsed?: boolean
  initialSize?: number
  minSize?: number
  maxSize?: number
  /** Pass md={0} to hide left panel on viewport < 768px */
  md?: number
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false,
  initialSize: 260,
  minSize: 200,
  maxSize: 400,
  md: undefined,
})

const emit = defineEmits<{
  'update:collapsed': [value: boolean]
  collapse: [collapsed: boolean]
  resize: [width: number]
}>()

const collapsed = ref(props.collapsed)
const leftWidth = ref(props.initialSize)

const appStore = useAppStore()
const shouldHideLeft = computed(() => {
  if (props.md === undefined) return false
  if (props.md === 0) return appStore.isMobile
  return false
})

const leftStyle = computed(() => {
  if (shouldHideLeft.value) return {}
  return {
    width: collapsed.value ? '0px' : leftWidth.value + 'px',
    minWidth: collapsed.value ? '0px' : props.minSize + 'px',
  }
})

function startResize(e: MouseEvent) {
  const startX = e.clientX
  const startWidth = leftWidth.value

  function onMouseMove(ev: MouseEvent) {
    const diff = ev.clientX - startX
    const newWidth = Math.min(props.maxSize, Math.max(props.minSize, startWidth + diff))
    leftWidth.value = newWidth
  }

  function onMouseUp() {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    emit('resize', leftWidth.value)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

function toggleCollapse() {
  collapsed.value = !collapsed.value
  emit('update:collapsed', collapsed.value)
  emit('collapse', collapsed.value)
}

defineExpose({ toggleCollapse, collapsed, leftWidth })
</script>
