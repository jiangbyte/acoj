<template>
  <Layout title="文件管理" back>
    <view class="flex flex-col"><view class="bg-white mx-4 mt-3 rounded-lg overflow-hidden"><view class="flex items-center gap-2 p-2"><u-search v-model="keyword" placeholder="搜索文件名" :show-action="false" @search="onSearch" class="flex-1" /></view></view><view v-for="item in records" :key="item.id" class="mx-4 mt-3 bg-white rounded-lg overflow-hidden"><view class="flex items-center justify-between px-4 py-3 border-b"><view class="flex flex-col"><text class="text-base font-bold text-gray-900">{{ item.original_name || item.object_name }}</text><text class="text-xs text-gray-500">{{ item.content_type || '-' }} · {{ formatSize(item.size) }}</text></view><view class="flex gap-1"><u-button text="详情" size="mini" plain @click="openDetail(item)" /><u-button text="删除" size="mini" plain type="error" @click="confirmDelete(item)" /></view></view><view class="px-4 py-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500"><text>存储: {{ item.storage_provider || '-' }}</text><text>{{ formatDateTime(item.updated_at) }}</text></view></view><u-loadmore :status="loadStatus" /></view>
    <u-popup :show="detailVisible" mode="bottom" :safe-area-inset-bottom="true" @close="detailVisible = false"><view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;"><text class="block text-lg font-bold mb-4">文件详情</text><u-cell-group :border="false"><u-cell-item v-for="f in detailFields" :key="f" :title="detailLabel(f)" :value="displayVal(detail[f])" :arrow="false" /></u-cell-group></view></u-popup>
  </Layout>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'; import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'; import Layout from '@/layouts/index.vue'; import { fileApi } from '@/api'; import { displayValue, formatDateTime } from '@/utils/format'
const records = ref<any[]>([]); const keyword = ref(''); const current = ref(1); const total = ref(0); const loading = ref(false); const detailVisible = ref(false); const detail = ref<any>({})
const detailFields = ['id', 'original_name', 'object_name', 'storage_provider', 'bucket', 'content_type', 'size', 'url', 'created_at', 'updated_at']
const loadStatus = computed(() => loading.value ? 'loading' : records.value.length >= total.value ? 'nomore' : 'loadmore')
onLoad(() => { refresh() }); onPullDownRefresh(async () => { await refresh(); uni.stopPullDownRefresh() })
async function refresh() { current.value = 1; await loadPage(false) }
async function loadPage(a: boolean) { loading.value = true; try { const p: any = { current: current.value, size: 20 }; if (keyword.value) p.original_name = keyword.value; const pg = await fileApi.page(p); records.value = a ? [...records.value, ...(pg.records ?? [])] : (pg.records ?? []); total.value = pg.total ?? 0 } finally { loading.value = false } }
function onSearch() { refresh() }
function formatSize(bytes: number) { if (!bytes) return '-'; if (bytes < 1024) return `${bytes}B`; if (bytes < 1024*1024) return `${(bytes/1024).toFixed(1)}KB`; return `${(bytes/(1024*1024)).toFixed(1)}MB` }
function displayVal(v: any) { const dt = formatDateTime(v); return displayValue(dt !== v ? dt : v) }
function detailLabel(p: string) { const m: Record<string, string> = { id: 'ID', original_name: '文件名', object_name: '对象名', storage_provider: '存储', bucket: 'Bucket', content_type: '类型', size: '大小', url: 'URL', created_at: '创建时间', updated_at: '更新时间' }; return m[p] ?? p }
function openDetail(item: any) { detail.value = item; detailVisible.value = true }
function confirmDelete(item: any) { uni.showModal({ title: '确认删除', content: `删除 ${item.original_name || item.object_name}？`, success: async (r: any) => { if (r.confirm) { await fileApi.remove({ ids: [item.id] }); await refresh() } } }) }
</script>
