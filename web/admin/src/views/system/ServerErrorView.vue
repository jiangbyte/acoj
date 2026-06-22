<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import { DEFAULT_HOME_PATH } from '@/config/app'

const route = useRoute()
const retryPath = computed(() => String(route.query.redirect || DEFAULT_HOME_PATH))
</script>

<template>
  <AResult status="500" title="服务暂时不可用" sub-title="请检查网络连接或后端服务状态">
    <template #extra>
      <RouterLink :to="retryPath">
        <AButton type="primary">重试</AButton>
      </RouterLink>
      <RouterLink to="/auth/login">
        <AButton>返回登录</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
