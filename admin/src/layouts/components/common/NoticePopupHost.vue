<!--
  Author: Charlie

  登录后自动弹出未读「弹窗」位置公告；稍后提醒仅本会话跳过，不再提示则标记已读。
-->
<script setup lang="ts">
import { myNoticeApi } from '@/api'
import MessageDetailModal from '@/components/sys/MessageDetailModal.vue'
import { wireBool } from '@/utils'
import { onMounted, ref } from 'vue'

const SESSION_KEY = 'hei.notice.popup.skipped'

const detailModalRef = ref<InstanceType<typeof MessageDetailModal> | null>(null)
const queue = ref<any[]>([])
const showing = ref(false)

onMounted(() => {
  void bootstrap()
})

async function bootstrap() {
  try {
    const response = await myNoticeApi.myPage({
      current: 1,
      size: 30,
      kind: 'ANNOUNCEMENT',
    })
    const records = Array.isArray(response.data?.records) ? response.data.records : []
    const skipped = readSkipped()
    queue.value = records.filter((item: any) => {
      if (!isPopupLocation(item)) return false
      if (wireBool(item.is_read ?? false)) return false
      if (skipped.has(String(item.id))) return false
      return true
    })
    await showNext()
  } catch {
    queue.value = []
  }
}

function isPopupLocation(item: any) {
  const locations = item?.publish_locations
  if (!locations || typeof locations !== 'object' || Array.isArray(locations)) return false
  const flag = locations.popup
  if (typeof flag === 'boolean') return flag
  if (typeof flag === 'number') return flag === 1
  if (typeof flag === 'string') return flag === 'true' || flag === '1'
  return false
}

function readSkipped(): Set<string> {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY)
    const list = raw ? (JSON.parse(raw) as string[]) : []
    return new Set(Array.isArray(list) ? list.map(String) : [])
  } catch {
    return new Set()
  }
}

function rememberSkipped(id: string) {
  const set = readSkipped()
  set.add(id)
  sessionStorage.setItem(SESSION_KEY, JSON.stringify([...set]))
}

async function showNext() {
  if (showing.value) return
  const next = queue.value.shift()
  if (!next) return
  showing.value = true
  await detailModalRef.value?.open(next, {
    mode: 'popup',
    markReadOnOpen: false,
  })
}

function handleClosed(payload: { id: string; dismissed: boolean }) {
  showing.value = false
  if (!payload.dismissed && payload.id) {
    rememberSkipped(payload.id)
  }
  void showNext()
}
</script>

<template>
  <MessageDetailModal
    ref="detailModalRef"
    @closed="handleClosed"
  />
</template>
