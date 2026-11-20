// Series type names use the SeriesOption suffix
import type {
  BarSeriesOption,
  LineSeriesOption,
  PieSeriesOption,
  RadarSeriesOption,
} from 'echarts/charts'
// Component type names use the ComponentOption suffix
import type {
  DatasetComponentOption,
  GridComponentOption,
  LegendComponentOption,
  TitleComponentOption,
  ToolboxComponentOption,
  TooltipComponentOption,
} from 'echarts/components'
// import { useAppStore } from '@/store'
import { BarChart, LineChart, PieChart, RadarChart } from 'echarts/charts'

import {
  DatasetComponent, // Dataset component
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  TransformComponent, // Built-in data transformer components (filter, sort)
} from 'echarts/components'
import * as echarts from 'echarts/core'

import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { useTemplateRef } from 'vue'
import { useElementSize } from '@vueuse/core'

// Use ComposeOption to create an option type containing only required components and charts
export type ECOption = echarts.ComposeOption<
  | BarSeriesOption
  | PieSeriesOption
  | LineSeriesOption
  | TitleComponentOption
  | TooltipComponentOption
  | GridComponentOption
  | LegendComponentOption
  | DatasetComponentOption
  | ToolboxComponentOption
  | RadarSeriesOption
>

// Register required components
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  TransformComponent,
  BarChart,
  PieChart,
  LineChart,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer,
  ToolboxComponent,
  RadarChart,
])

/**
 * ECharts hook
 * @description Import chart components on demand; unregistered components must be imported first
 */
export function useEcharts(ref: string, chartOptions: Ref<ECOption>) {
  const el = useTemplateRef<HTMLLIElement>(ref)

  // const appStore = useAppStore()

  let chart: echarts.ECharts | null = null

  const { width, height } = useElementSize(el)

  const isRendered = () => Boolean(el && chart)

  async function render() {
    // Do not render without width or height
    if (!width || !height)
      return

    // const chartTheme = appStore.colorMode ? 'dark' : 'light'
    await nextTick()
    if (el) {
      chart = echarts.init(el.value, 'light')
      update(chartOptions.value)
    }
  }

  async function update(updateOptions: ECOption) {
    if (isRendered()) {
      chart!.setOption({ backgroundColor: 'transparent', ...updateOptions })
    }
  }

  function destroy() {
    chart?.dispose()
    chart = null
  }

  watch([width, height], async ([newWidth, newHeight]) => {
    if (isRendered() && newWidth && newHeight)
      chart?.resize()
  })

  watch(chartOptions, (newValue) => {
    update(newValue)
  })

  onMounted(() => {
    render()
  })
  onUnmounted(() => {
    destroy()
  })

  return {
    destroy,
    update,
  }
}
