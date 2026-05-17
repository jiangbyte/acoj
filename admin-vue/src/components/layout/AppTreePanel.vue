<template>
  <AppSplitPanel
    ref="splitRef"
    v-model:collapsed="collapsed"
    :initial-size="280"
    :min-size="200"
    :max-size="400"
    :md="0"
  >
    <template #left>
      <a-card
        size="small"
        class="h-full flex flex-col max-md:hidden"
        :body-style="{ flex: '1', overflow: 'auto', padding: '12px' }"
      >
        <a-input-search
          v-model:value="searchKey"
          :placeholder="`搜索${title}`"
          allow-clear
          class="mb-2"
        />
        <a-spin :spinning="loading">
          <a-tree
            v-if="data.length"
            v-model:expanded-keys="expandedKeys"
            :tree-data="data"
            :field-names="fieldNames"
            :selected-keys="selectedKeys"
            block-node
            show-line
            @select="handleSelect"
          />
          <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
        </a-spin>
      </a-card>
    </template>
    <template #right>
      <slot name="right" :parent-id="currentParentId" :refresh-tree="refresh" />
    </template>
  </AppSplitPanel>

  <a-drawer
    :open="mobileOpen"
    :title="title"
    placement="left"
    :width="280"
    destroy-on-close
    @close="mobileOpen = false"
  >
    <a-input-search
      v-model:value="searchKey"
      :placeholder="`搜索${title}`"
      allow-clear
      class="mb-2"
    />
    <a-spin :spinning="loading">
      <a-tree
        v-if="data.length"
        v-model:expanded-keys="expandedKeys"
        :tree-data="data"
        :field-names="fieldNames"
        :selected-keys="selectedKeys"
        block-node
        show-line
        @select="handleSelect"
      >
        <template #switcherIcon="{ expanded }">
          <CaretDownOutlined :class="expanded ? '' : '-rotate-90'" class="text-[12px]" />
        </template>
        <template #icon>
          <component :is="icon" class="text-[var(--primary-color)]" />
        </template>
      </a-tree>
      <div v-else class="text-center text-gray-400 py-8">暂无数据</div>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { CaretDownOutlined } from '@ant-design/icons-vue'
import AppSplitPanel from './AppSplitPanel.vue'

const props = withDefaults(
  defineProps<{
    title?: string
    fetchTree: (params?: any) => Promise<any>
    fieldNames?: { children: string; title: string; key: string }
    icon?: any
  }>(),
  {
    title: '',
    fieldNames: () => ({ children: 'children', title: 'name', key: 'id' }),
    icon: undefined,
  }
)

const emit = defineEmits<{
  select: [parentId: string | undefined]
}>()

const splitRef = ref()
const collapsed = ref(false)
const mobileOpen = ref(false)
const loading = ref(false)
const data = ref<any[]>([])
const originData = ref<any[]>([])
const expandedKeys = ref<string[]>([])
const selectedKeys = ref<string[]>([])
const searchKey = ref('')

function filterTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes
  return nodes.reduce((acc: any[], node) => {
    const label = node[props.fieldNames.title]
    const match = label?.includes(keyword)
    const filteredChildren = node.children ? filterTree(node.children, keyword) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

function getAllKeys(nodes: any[]): string[] {
  return nodes.reduce((keys: string[], n) => {
    keys.push(n[props.fieldNames.key])
    if (n.children) keys.push(...getAllKeys(n.children))
    return keys
  }, [])
}

watch(searchKey, val => {
  data.value = filterTree(originData.value, val)
  if (val) expandedKeys.value = getAllKeys(data.value)
})

const currentParentId = ref<string | undefined>()

function handleSelect(keys: any[]) {
  selectedKeys.value = keys
  currentParentId.value = keys.length > 0 ? keys[0] : undefined
  mobileOpen.value = false
  emit('select', currentParentId.value)
}

async function refresh() {
  loading.value = true
  try {
    const res = await props.fetchTree()
    originData.value = res.data || []
    data.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(refresh)

defineExpose({ refresh, collapsed, splitRef })
</script>
