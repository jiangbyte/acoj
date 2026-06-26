<template>
  <n-space vertical size="large">
    <n-page-header title="详情示例" subtitle="隐藏路由，菜单高亮到列表示例" @back="router.back">
      <template #extra>
        <n-button type="primary" @click="router.push('/demo/list')">返回列表</n-button>
      </template>
    </n-page-header>

    <n-card :bordered="false">
      <n-descriptions label-placement="left" bordered :column="1">
        <n-descriptions-item label="编号">{{ route.query.id ?? '1001' }}</n-descriptions-item>
        <n-descriptions-item label="标题">示例题目详情</n-descriptions-item>
        <n-descriptions-item label="状态">已发布</n-descriptions-item>
        <n-descriptions-item label="更新时间">2026-06-26</n-descriptions-item>
      </n-descriptions>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>
