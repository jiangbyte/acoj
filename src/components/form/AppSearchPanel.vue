<template>
  <a-card size="small">
    <a-form :model="model" @finish="$emit('search')">
      <a-row :gutter="16" align="middle" class="gap-y-4 [&_.ant-form-item]:mb-0">
        <SlotRenderer :collapse-after="collapseAfter" :advanced="advanced">
          <slot />
        </SlotRenderer>
        <a-col flex="auto" style="min-width: 200px">
          <a-form-item class="[&_.ant-form-item]:mb-0">
            <a-space>
              <slot name="extra" />
              <a-button type="primary" html-type="submit">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="$emit('reset')">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a v-if="collapseAfter! > 0" class="cursor-pointer whitespace-nowrap" @click="advanced = !advanced">
                {{ advanced ? '收起' : '展开' }}
                <component :is="advanced ? UpOutlined : DownOutlined" />
              </a>
            </a-space>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { ref, defineComponent } from 'vue'
import { SearchOutlined, ReloadOutlined, DownOutlined, UpOutlined } from '@ant-design/icons-vue'

const props = withDefaults(defineProps<{
  model: any
  collapseAfter?: number
}>(), {
  collapseAfter: 4,
})

const emit = defineEmits<{
  search: []
  reset: []
}>()

const advanced = ref(false)

const SlotRenderer = defineComponent({
  props: {
    collapseAfter: { type: Number, default: 4 },
    advanced: { type: Boolean, default: false },
  },
  setup(props, { slots }) {
    return () => {
      const items = slots.default?.() || []
      return items.filter((_, idx) => props.advanced || idx < props.collapseAfter)
    }
  },
})
</script>
