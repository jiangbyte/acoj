<template>
  <Layout title="会话管理" back>
    <view class="flex flex-col"><view class="bg-white mx-4 mt-3 rounded-lg overflow-hidden"><view class="flex items-center gap-2 p-2"><u-search v-model="keyword" placeholder="搜索账号ID" :show-action="false" @search="onSearch" class="flex-1" /></view></view><view v-for="item in records" :key="item.id" class="mx-4 mt-3 bg-white rounded-lg overflow-hidden"><view class="flex items-center justify-between px-4 py-3 border-b"><view class="flex flex-col"><text class="text-base font-bold text-gray-900">{{ item.account_id || '-' }}</text><text class="text-xs text-gray-500">{{ item.device_label || '-' }} · {{ item.client_ip || '-' }}</text></view><view class="flex gap-1"><u-button text="详情" size="mini" plain @click="openDetail(item)" /><u-button text="踢出" size="mini" plain type="error" @click="confirmExit(item)" /></view></view><view class="px-4 py-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500"><text>类型: {{ item.account_type || '-' }}</text><text>{{ formatDateTime(item.updated_at) }}</text></view></view><u-loadmore :status="loadStatus" /></view>
    <u-popup :show="detailVisible" mode="bottom" :safe-area-inset-bottom="true" @close="detailVisible = false"><view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;"><text class="block text-lg font-bold mb-4">会话详情</text><u-cell-group :border="false"><u-cell-item v-for="f in detailFields" :key="f" :title="detailLabel(f)" :value="displayVal(detail[f])" :arrow="false" /></u-cell-group></view></u-popup>
  </Layout>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'; import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'; import Layout from '@/layouts/index.vue'; import { sessionApi } from '@/api'; import { displayValue, formatDateTime } from '@/utils/format'
const records = ref<any[]>([]); const keyword = ref(''); const current = ref(1); const total = ref(0); const loading = ref(false); const detailVisible = ref(false); const detail = ref<any>({})
const detailFields = ['id', 'account_id', 'account_type', 'token_hash', 'client_ip', 'user_agent', 'device_label', 'created_at', 'updated_at']
const loadStatus = computed(() => loading.value ? 'loading' : records.value.length >= total.value ? 'nomore' : 'loadmore')
onLoad(() => { refresh() }); onPullDownRefresh(async () => { await refresh(); uni.stopPullDownRefresh() })
async function refresh() { current.value = 1; await loadPage(false) }
async function loadPage(a: boolean) { loading.value = true; try { const p: any = { current: current.value, size: 20 }; if (keyword.value) p.account_id = keyword.value; const pg = await sessionApi.page(p); records.value = a ? [...records.value, ...(pg.records ?? [])] : (pg.records ?? []); total.value = pg.total ?? 0 } finally { loading.value = false } }
function onSearch() { refresh() }
function displayVal(v: any) { const dt = formatDateTime(v); return displayValue(dt !== v ? dt : v) }
function detailLabel(p: string) { const m: Record<string, string> = { id: 'ID', account_id: '账号ID', account_type: '账号类型', token_hash: 'Token', client_ip: 'IP', user_agent: 'User Agent', device_label: '设备', created_at: '创建时间', updated_at: '更新时间' }; return m[p] ?? p }
function openDetail(item: any) { detail.value = item; detailVisible.value = true }
function confirmExit(item: any) { uni.showModal({ title: '踢出会话', content: `踢出 ${item.account_id || item.id} 的会话？`, success: async (r: any) => { if (r.confirm) { await sessionApi.remove({ ids: [item.id] }); await refresh() } } }) }
</script>
