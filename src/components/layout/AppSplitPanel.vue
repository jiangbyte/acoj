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

    <!-- Collapse toggle -->
    <div
      v-if="!shouldHideLeft"
      class="max-md:hidden absolute left-[var(--toggle-left,0)] top-1/2 -translate-y-1/2 z-2 w-4 h-12 flex-center cursor-pointer text-[var(--text-secondary,#00000073)] bg-[var(--container-bg,#fff)] border border-[var(--border-color,#f0f0f0)] border-l-0 rounded-r text-[10px] hover:text-primary transition-colors"
      :title="collapsed ? '展开' : '收起'"
      @click="toggleCollapse"
    >
      <double-left-outlined v-if="!collapsed" />
      <double-right-outlined v-else />
    </div>

    <!-- Right panel -->
    <div class="flex-1 overflow-hidden min-w-0 flex flex-col">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { DoubleLeftOutlined, DoubleRightOutlined } from '@ant-design/icons-vue'

interface Props {
  initialSize?: number
  minSize?: number
  maxSize?: number
  /** Pass md={0} to hide left panel on viewport < 768px */
  md?: number
}

const props = withDefaults(defineProps<Props>(), {
  initialSize: 260,
  minSize: 200,
  maxSize: 400,
  md: undefined,
})

const collapsed = ref(false)
const leftWidth = ref(props.initialSize)

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

const shouldHideLeft = computed(() => {
  if (props.md === undefined) return false
  if (props.md === 0) return isMobile.value
  return false
})

const emit = defineEmits<{
  collapse: [collapsed: boolean]
  resize: [width: number]
}>()

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
  emit('collapse', collapsed.value)
}

defineExpose({ toggleCollapse, collapsed, leftWidth })
</script>
