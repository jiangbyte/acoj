<template>
  <n-space vertical size="large">
    <n-page-header title="表单示例" subtitle="基础录入表单" />

    <n-card :bordered="false">
      <n-form label-placement="left" label-width="90" :model="form">
        <n-form-item label="题目标题">
          <n-input v-model:value="form.title" placeholder="请输入题目标题" />
        </n-form-item>
        <n-form-item label="难度">
          <n-select v-model:value="form.level" :options="levelOptions" />
        </n-form-item>
        <n-form-item label="公开">
          <n-switch v-model:value="form.visible" />
        </n-form-item>
        <n-form-item label="标签">
          <n-dynamic-tags v-model:value="form.tags" />
        </n-form-item>
        <n-form-item label="说明">
          <n-input v-model:value="form.description" type="textarea" placeholder="请输入简要说明" />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary">保存</n-button>
            <n-button>重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const form = reactive({
  title: '示例题目',
  level: 'medium',
  visible: true,
  tags: ['数组', '排序'],
  description: '',
})

const levelOptions = [
  { label: '简单', value: 'easy' },
  { label: '中等', value: 'medium' },
  { label: '困难', value: 'hard' },
]
</script>
