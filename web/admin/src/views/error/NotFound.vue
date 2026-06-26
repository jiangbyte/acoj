<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const homePath = import.meta.env.VITE_HOME_PATH
</script>

<template>
  <n-result status="404" title="页面不存在" description="请检查访问地址是否正确">
    <template #footer>
      <n-button type="primary" @click="router.push(homePath)"> 返回工作台 </n-button>
    </template>
  </n-result>
</template>
