<script setup lang="ts">
import { PlusOutlined } from '@ant-design/icons-vue'
import type { DataNode } from 'ant-design-vue/es/tree'
import { computed, onMounted, ref } from 'vue'

import type { DeptNode } from '@/types/api'
import { listDeptTree } from '@/apis/iam'

const treeData = ref<DeptNode[]>([])
const selected = ref<DeptNode | null>(null)
const drawerOpen = ref(false)

const antTreeData = computed<DataNode[]>(() => mapTreeNodes(treeData.value))

function mapTreeNodes(nodes: DeptNode[]): DataNode[] {
  return nodes.map((node) => ({
    ...node,
    key: node.id,
    title: node.name,
    children: node.children ? mapTreeNodes(node.children) : undefined,
  }))
}

function findNode(nodes: DeptNode[], id: string): DeptNode | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    const child = findNode(node.children || [], id)
    if (child) {
      return child
    }
  }
  return null
}

function handleSelect(keys: unknown[]) {
  selected.value = findNode(treeData.value, String(keys[0])) || selected.value
}

onMounted(async () => {
  treeData.value = await listDeptTree()
  selected.value = treeData.value[0] || null
})
</script>

<template>
  <div class="grid grid-cols-1 gap-4 xl:grid-cols-[320px_minmax(0,1fr)]">
    <div class="page-card p-6">
      <div class="toolbar-row mb-4">
        <div class="text-slate-900 font-700 dark:text-zinc-100">组织架构</div>
        <AButton size="small" type="primary" @click="drawerOpen = true">
          <template #icon><PlusOutlined /></template>
        </AButton>
      </div>
      <ATree
        :tree-data="antTreeData"
        default-expand-all
        @select="handleSelect"
      />
    </div>

    <div class="page-card p-6">
      <div class="toolbar-row mb-6">
        <h2 class="m-0 text-18px text-slate-900 font-700 dark:text-zinc-100">{{ selected?.name || '部门详情' }}</h2>
        <ASpace>
          <AButton>编辑</AButton>
          <AButton type="primary" @click="drawerOpen = true">新增下级</AButton>
        </ASpace>
      </div>
      <ADescriptions bordered :column="{ xs: 1, md: 2 }">
        <ADescriptionsItem label="部门编码">{{ selected?.code || '-' }}</ADescriptionsItem>
        <ADescriptionsItem label="类型">{{ selected?.category || '-' }}</ADescriptionsItem>
        <ADescriptionsItem label="负责人">{{ selected?.manager || '-' }}</ADescriptionsItem>
        <ADescriptionsItem label="上级ID">{{ selected?.parent_id || '-' }}</ADescriptionsItem>
      </ADescriptions>
    </div>

    <ADrawer v-model:open="drawerOpen" title="维护部门" width="480">
      <AForm layout="vertical">
        <AFormItem label="部门名称"><AInput placeholder="请输入部门名称" /></AFormItem>
        <AFormItem label="部门编码"><AInput placeholder="请输入部门编码" /></AFormItem>
        <AFormItem label="类型"><ASelect placeholder="请选择类型" /></AFormItem>
        <AFormItem label="负责人"><AInput placeholder="请输入负责人" /></AFormItem>
      </AForm>
      <template #footer>
        <ASpace>
          <AButton @click="drawerOpen = false">取消</AButton>
          <AButton type="primary" @click="drawerOpen = false">保存</AButton>
        </ASpace>
      </template>
    </ADrawer>
  </div>
</template>
