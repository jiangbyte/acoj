<template>
  <a-row :gutter="16">
    <a-col :span="6">
      <a-card title="分类" size="small">
        <a-tree :treeData="treeData" :defaultExpandAll="true" @select="handleTreeSelect" />
      </a-card>
    </a-col>
    <a-col :span="18">
      <slot :selectedKey="selectedKey" />
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  treeData: any[]
}>()

const selectedKey = ref<string | undefined>()

function handleTreeSelect(keys: any[]) {
  selectedKey.value = keys[0]
}

defineExpose({ selectedKey })
</script>
