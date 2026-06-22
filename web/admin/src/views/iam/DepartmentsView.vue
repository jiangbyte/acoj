<script setup lang="ts">
import { PlusOutlined } from '@ant-design/icons-vue'
import type { DataNode } from 'ant-design-vue/es/tree'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type { DeptNode } from '@/types/api'
import { listDeptTree } from '@/apis/iam'

const { t } = useI18n()
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
        <div class="text-slate-900 font-700 dark:text-zinc-100">{{ t('table.orgTree') }}</div>
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
        <h2 class="m-0 text-18px text-slate-900 font-700 dark:text-zinc-100">{{ selected?.name || t('table.departmentDetail') }}</h2>
        <ASpace>
          <AButton>{{ t('common.edit') }}</AButton>
          <AButton type="primary" @click="drawerOpen = true">{{ t('table.addChild') }}</AButton>
        </ASpace>
      </div>
      <ADescriptions bordered :column="{ xs: 1, md: 2 }">
        <ADescriptionsItem :label="t('table.deptCode')">{{ selected?.code || '-' }}</ADescriptionsItem>
        <ADescriptionsItem :label="t('common.type')">{{ selected?.category || '-' }}</ADescriptionsItem>
        <ADescriptionsItem :label="t('common.owner')">{{ selected?.manager || '-' }}</ADescriptionsItem>
        <ADescriptionsItem :label="t('table.parentId')">{{ selected?.parent_id || '-' }}</ADescriptionsItem>
      </ADescriptions>
    </div>

    <ADrawer v-model:open="drawerOpen" :title="t('table.maintainDept')" width="480">
      <AForm layout="vertical">
        <AFormItem :label="t('table.deptName')"><AInput :placeholder="t('table.deptNamePlaceholder')" /></AFormItem>
        <AFormItem :label="t('table.deptCode')"><AInput :placeholder="t('table.deptCodePlaceholder')" /></AFormItem>
        <AFormItem :label="t('common.type')"><ASelect :placeholder="t('table.deptTypePlaceholder')" /></AFormItem>
        <AFormItem :label="t('common.owner')"><AInput :placeholder="t('table.managerPlaceholder')" /></AFormItem>
      </AForm>
      <template #footer>
        <ASpace>
          <AButton @click="drawerOpen = false">{{ t('common.cancel') }}</AButton>
          <AButton type="primary" @click="drawerOpen = false">{{ t('common.save') }}</AButton>
        </ASpace>
      </template>
    </ADrawer>
  </div>
</template>
