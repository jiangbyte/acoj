<!-- Author: Charlie -->

<script setup lang="ts">
import { ref } from 'vue'
import ThemeSettingsModal from './ThemeSettingsModal.vue'

const show = ref(false)
</script>

<template>
  <CommonWrapper @click="show = true">
    <NovaIcon icon="icon-park-outline:setting-two" />
  </CommonWrapper>
  <ThemeSettingsModal v-model:show="show" />
</template>
