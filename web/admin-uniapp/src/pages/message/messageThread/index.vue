<template>
  <Layout title="站内信" back>
    <view class="flex flex-col"><view class="bg-white mx-4 mt-3 rounded-lg overflow-hidden"><view class="p-2"><u-search v-model="keyword" placeholder="搜索" :show-action="false" @search="onSearch" /></view></view><view v-for="item in records" :key="item.id" class="mx-4 mt-3 bg-white rounded-lg overflow-hidden"><view class="flex items-center justify-between px-4 py-3 border-b"><view class="flex flex-col"><text class="text-base font-bold text-gray-900">{{ item.title }}</text><text class="text-xs text-gray-500">{{ item.last_message?.content || '-' }}</text></view><view class="flex gap-1"><u-button text="详情" size="mini" plain @click="openDetail(item)" /></view></view><view class="px-4 py-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500"><text>类型: {{ item.thread_type || '-' }}</text><text>未读: {{ item.unread_count ?? 0 }}</text><text>{{ formatDateTime(item.last_message_at || item.updated_at) }}</text></view></view><u-loadmore :status="loadStatus" /></view>
    <u-popup :show="detailVisible" mode="bottom" :safe-area-inset-bottom="true" @close="detailVisible = false"><view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;"><text class="block text-lg font-bold mb-4">消息记录</text><view v-if="messages.length"><view v-for="msg in messages" :key="msg.id" class="py-2 border-b"><text class="text-sm text-gray-900">{{ msg.content }}</text><text class="block text-xs text-gray-400 mt-1">{{ msg.sender_name || msg.sender_account_id }} · {{ formatDateTime(msg.created_at) }}</text></view></view><u-empty v-else mode="list" text="暂无消息" /></view></u-popup>
  </Layout>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'; import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'; import Layout from '@/layouts/index.vue'; import { messageApi } from '@/api'; import { formatDateTime } from '@/utils/format'
const records = ref<any[]>([]); const keyword = ref(''); const current = ref(1); const total = ref(0); const loading = ref(false); const detailVisible = ref(false); const messages = ref<any[]>([])
const loadStatus = computed(() => loading.value ? 'loading' : records.value.length >= total.value ? 'nomore' : 'loadmore')
onLoad(() => { refresh() }); onPullDownRefresh(async () => { await refresh(); uni.stopPullDownRefresh() })
async function refresh() { current.value = 1; await loadPage(false) }
async function loadPage(a: boolean) { loading.value = true; try { const p: any = { current: current.value, size: 20 }; if (keyword.value) p.title = keyword.value; const pg = await messageApi.threadPage(p); records.value = a ? [...records.value, ...(pg.records ?? [])] : (pg.records ?? []); total.value = pg.total ?? 0 } finally { loading.value = false } }
function onSearch() { refresh() }
async function openDetail(item: any) { detailVisible.value = true; try { const data = await messageApi.threadMessages({ thread_id: item.id, current: 1, size: 20 }); messages.value = data.records ?? [] } catch { messages.value = [] } }
</script>
