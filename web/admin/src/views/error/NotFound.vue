<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const homePath = import.meta.env.VITE_HOME_PATH
</script>

<template>
  <n-result
    status="404"
    :title="'Page not found'"
    :description="'Please check whether the URL is correct.'"
    class="flex h-full w-full items-center justify-center flex-col"
  >
    <template #footer>
      <n-button type="primary" @click="router.push(homePath)">
        {{ 'Back to workbench' }}
      </n-button>
    </template>
  </n-result>
</template>
