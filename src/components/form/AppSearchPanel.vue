<template>
  <a-card size="small">
    <a-form :model="model" @finish="$emit('search')">
      <a-row :gutter="16" align="middle" class="gap-y-4 [&_.ant-form-item]:mb-0">
        <template v-for="(item, idx) in visibleItems" :key="getItemKey(item, idx)">
          <RenderNode :node="item" />
        </template>
        <a-col :xs="24" :sm="24" :md="8" :lg="6">
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="$emit('reset')">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a v-if="hasCollapse" @click="advanced = !advanced">
                {{ advanced ? '收起' : '展开' }}
                <DownOutlined v-if="!advanced" />
                <UpOutlined v-else />
              </a>
            </a-space>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row v-if="hasCollapse" v-show="advanced" :gutter="16" align="middle" class="gap-y-4 [&_.ant-form-item]:mb-0">
        <template v-for="(item, idx) in collapsedItems" :key="getItemKey(item, idx)">
          <RenderNode :node="item" />
        </template>
      </a-row>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed, useSlots, defineComponent } from 'vue'
import { SearchOutlined, ReloadOutlined, DownOutlined, UpOutlined } from '@ant-design/icons-vue'

const props = defineProps<{
  model: any
  /** Number of initial items to show before the collapse. Default: 0 (show all, no collapse) */
  collapseAfter?: number
}>()

defineEmits<{
  search: []
  reset: []
}>()

const slots = useSlots()
const advanced = ref(false)

// Render raw VNodes in the template
const RenderNode = defineComponent({
  props: { node: { type: Object, required: true } },
  render() {
    return this.node
  },
})

function getItemKey(item: any, idx: number) {
  return item?.key ?? idx
}

const allItems = computed(() => slots.default?.() || [])

const hasCollapse = computed(() => {
  if (!props.collapseAfter || props.collapseAfter <= 0) return false
  return allItems.value.length > props.collapseAfter
})

const visibleItems = computed(() => {
  if (!hasCollapse.value) return allItems.value
  return allItems.value.slice(0, props.collapseAfter)
})

const collapsedItems = computed(() => {
  if (!hasCollapse.value) return []
  return allItems.value.slice(props.collapseAfter)
})
</script>
