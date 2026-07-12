<template>
  <u-card @click="$emit('detail')">
    <template #head>
      <CardHead :title="title" :sub-title="description" />
    </template>
    <template #body>
      <u-cell-group :border="false">
        <u-cell-item
          v-if="status"
          title="状态"
          :value="status"
          :arrow="false"
        ></u-cell-item>
        <u-cell-item
          v-for="field in fields"
          :key="field.label"
          :title="field.label"
          :value="field.value"
          :arrow="false"
        ></u-cell-item>
      </u-cell-group>
    </template>
    <template v-if="actions.length" #foot>
      <view class="record-actions" @click.stop>
        <u-button
          v-for="action in visibleAction"
          :key="action.key"
          :text="action.label"
          :icon="action.icon"
          plain
          @click="$emit('action', action)"
        ></u-button>
        <u-button
          v-if="actions.length > visibleAction.length"
          text="更多"
          icon="more-dot-fill"
          plain
          @click="$emit('more')"
        ></u-button>
      </view>
    </template>
  </u-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CardHead from '@/components/common/CardHead.vue'

const props = defineProps<{
  icon: string
  title: string
  description?: string
  status?: string
  fields: { label: string; value: string }[]
  actions: any[]
}>()

defineEmits<{
  (event: 'detail'): void
  (event: 'action', value: any): void
  (event: 'more'): void
}>()

const visibleAction = computed(() => props.actions.slice(0, 3))
</script>

<style lang="scss" scoped>
.record-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
</style>
