<template>
  <div class="flex flex-col items-center justify-center h-screen">
    <AResult status="403" title="403" sub-title="抱歉，您没有访问此页面的权限">
      <template #extra>
        <AButton type="primary" @click="router.push('/')">返回首页</AButton>
      </template>
    </AResult>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
