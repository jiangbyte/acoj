<template>
  <a-tabs v-model:active-key="activeKey" tab-position="left">
    <a-tab-pane key="local" tab="本地存储">
      <local-form />
    </a-tab-pane>
  </a-tabs>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LocalForm from './localForm.vue'

const activeKey = ref('local')
</script>
