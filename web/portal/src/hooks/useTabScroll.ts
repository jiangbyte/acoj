import type { NScrollbar } from 'naive-ui'
import type { Ref } from 'vue'
import { nextTick, ref, watchEffect } from 'vue'

/**
 * 标签栏横向滚动控制。
 *
 * 负责两件事：
 * 1. 当前标签变化时，自动把激活标签滚动到可视区域；
 * 2. 用户在标签栏上滚动鼠标滚轮时，把纵向滚轮转换成横向滚动。
 */
export function useTabScroll(currentTabPath: Ref<string>) {
  // Naive UI NScrollbar 实例，TabBar.vue 会把 ref 绑定到 n-scrollbar 上。
  const scrollbar = ref<InstanceType<typeof NScrollbar>>()

  // 激活标签靠近左右边缘时预留的安全距离，避免标签贴边显示。
  const safeArea = ref(150)

  // 滚轮节流定时器，避免高频 wheel 事件导致滚动过于抖动。
  let timer: number | undefined

  /**
   * 滚动到指定横向位置。
   *
   * 这里统一使用 smooth 行为，让点击标签、关闭标签后的自动定位更自然。
   */
  function handleTabSwitch(distance: number) {
    scrollbar.value?.scrollTo({
      left: distance,
      behavior: 'smooth',
    })
  }

  /**
   * 把当前激活标签滚动到可视区域。
   *
   * DOM 更新发生在路由和标签状态更新之后，所以需要 nextTick 后再读取元素位置。
   */
  function scrollToCurrentTab() {
    nextTick(() => {
      // 当前激活标签，TabBar.vue 会在普通标签上写入 data-tab-path。
      const currentTabElement = document.querySelector(
        `[data-tab-path="${currentTabPath.value}"]`,
      ) as HTMLElement | null

      // n-scrollbar 内部真正产生滚动的容器。
      const tabBarScrollWrapper = document.querySelector(
        '.tab-bar-scroller-wrapper .n-scrollbar-container',
      ) as HTMLElement | null

      // 标签内容容器，用于读取右侧 padding，避免计算滚动距离时忽略内边距。
      const tabBarScrollContent = document.querySelector(
        '.tab-bar-scroller-content',
      ) as HTMLElement | null

      // DOM 不完整时直接跳过，避免初始化阶段或组件卸载阶段报错。
      if (!currentTabElement || !tabBarScrollWrapper || !tabBarScrollContent) {
        return
      }

      // 当前标签相对内容容器的左偏移。
      const tabLeft = currentTabElement.offsetLeft

      // 当前滚动容器已滚动的横向距离。
      const tabBarLeft = tabBarScrollWrapper.scrollLeft

      // 滚动容器可视宽度。
      const wrapperWidth = tabBarScrollWrapper.getBoundingClientRect().width

      // 当前标签自身宽度。
      const tabWidth = currentTabElement.getBoundingClientRect().width

      // 内容容器右侧 padding，确保最后一个标签滚动到右侧时不会被操作区遮挡。
      const containerPR = Number.parseFloat(
        window.getComputedStyle(tabBarScrollContent).paddingRight,
      )

      // 标签右侧超出可视区域时，向右滚动到标签完整可见。
      if (tabLeft + tabWidth + safeArea.value + containerPR > wrapperWidth + tabBarLeft) {
        handleTabSwitch(tabLeft + tabWidth + containerPR - wrapperWidth + safeArea.value)
      } else if (tabLeft - safeArea.value < tabBarLeft) {
        // 标签左侧超出可视区域时，向左滚动到标签完整可见。
        handleTabSwitch(tabLeft - safeArea.value)
      }
    })
  }

  /**
   * 鼠标滚轮横向滚动标签栏。
   *
   * 大多数鼠标只有纵向滚轮，这里把 deltaY 转换为横向滚动；如果本身是横向滚动手势则不处理。
   */
  function onWheel(e: WheelEvent) {
    e.preventDefault()
    if (Math.abs(e.deltaY) <= Math.abs(e.deltaX)) {
      return
    }

    window.clearTimeout(timer)
    timer = window.setTimeout(() => {
      scrollbar.value?.scrollBy({
        left: e.deltaY > 0 ? 400 : -400,
        behavior: 'smooth',
      })
    }, 40)
  }

  // 当前标签变化时自动修正滚动位置。
  watchEffect(() => {
    if (currentTabPath.value) {
      scrollToCurrentTab()
    }
  })

  // 暴露给 TabBar.vue 使用的滚动实例、滚轮处理函数和可调安全距离。
  return {
    scrollbar,
    onWheel,
    safeArea,
    handleTabSwitch,
  }
}
