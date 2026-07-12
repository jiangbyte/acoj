<template>
  <Layout :title="title" back>
    <view>
      <template v-if="type === 'thread'">
        <u-card
          v-for="item in messages"
          :key="item.id"
          :title="
            item.sender_name || item.sender_account_id || item.sender_type
          "
        >
          <template #body>
            <view class="detail-block">
              <text>{{ item.content }}</text>
              <text>{{ formatDateTime(item.created_at) }}</text>
            </view>
          </template>
        </u-card>
        <u-card :show-head="false">
          <template #body>
            <u-textarea
              v-model="replyContent"
              placeholder="请输入回复内容"
              border="surround"
            ></u-textarea>
          </template>
          <template #foot>
            <u-button
              text="回复"
              type="primary"
              :loading="loading"
              @click="reply"
            ></u-button>
          </template>
        </u-card>
      </template>

      <u-card v-else>
        <template #head>
          <CardHead
            :title="detail.title || detail.subject || '-'"
            :sub-title="formatDateTime(detail.created_at || detail.publish_at)"
          />
        </template>
        <template #body>
          <view class="detail-block">
            <view v-if="type === 'notification'">
              <text>级别</text>
              <text>{{
                dictTypeData('NOTIFICATION_SEVERITY', detail.severity) ||
                detail.severity ||
                '-'
              }}</text>
            </view>
            <view v-if="type === 'todo'">
              <text>优先级</text>
              <text>{{
                dictTypeData('TODO_PRIORITY', detail.priority) ||
                detail.priority ||
                '-'
              }}</text>
            </view>
            <view v-if="type === 'todo'">
              <text>状态</text>
              <text>{{
                dictTypeData('TODO_STATUS', detail.status) ||
                detail.status ||
                '-'
              }}</text>
            </view>
            <view>
              <text>内容</text>
              <text>{{ detail.content || detail.description || '-' }}</text>
            </view>
          </view>
        </template>
        <template v-if="type === 'todo'" #foot>
          <u-button
            v-if="detail.status === 'PENDING'"
            text="开始处理"
            type="primary"
            :loading="loading"
            @click="startTodo"
          ></u-button>
          <u-button
            v-if="detail.status === 'IN_PROGRESS'"
            text="完成"
            type="primary"
            :loading="loading"
            @click="completeTodo"
          ></u-button>
          <u-button
            v-if="['PENDING', 'IN_PROGRESS'].includes(detail.status)"
            text="取消"
            plain
            :loading="loading"
            @click="cancelTodo"
          ></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import { messageApi } from '@/api'
import { dictTypeData } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'

const type = ref<'notification' | 'thread' | 'todo'>('notification')
const id = ref('')
const detail = ref<any>({})
const messages = ref<any[]>([])
const replyContent = ref('')
const loading = ref(false)
const title = computed(() =>
  type.value === 'thread' ? '会话' : type.value === 'todo' ? '待办' : '通知'
)

onLoad(async (query: any) => {
  type.value = query.type || 'notification'
  id.value = query.id || ''
  await loadDetail()
})

async function loadDetail() {
  if (type.value === 'notification') {
    detail.value = await messageApi.myNotificationDetail({ id: id.value })
    await messageApi
      .readNotification({ ids: [id.value] })
      .catch(() => undefined)
  } else if (type.value === 'todo') {
    detail.value = await messageApi.myTodoDetail({ id: id.value })
  } else {
    detail.value = { id: id.value }
    const page = await messageApi.myThreadMessage({
      thread_id: id.value,
      current: 1,
      size: 50,
    })
    messages.value = page.records ?? []
    await messageApi.readThread({ thread_id: id.value }).catch(() => undefined)
  }
}

async function reply() {
  const content = replyContent.value.trim()
  if (!content) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await messageApi.replyMessage({ thread_id: id.value, content })
    replyContent.value = ''
    await loadDetail()
  } finally {
    loading.value = false
  }
}

async function startTodo() {
  await updateTodo(() => messageApi.startTodo({ id: id.value }))
}

async function completeTodo() {
  await updateTodo(() => messageApi.completeTodo({ id: id.value }))
}

async function cancelTodo() {
  await updateTodo(() => messageApi.cancelTodo({ id: id.value }))
}

async function updateTodo(action: () => Promise<any>) {
  loading.value = true
  try {
    await action()
    await loadDetail()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.detail-block,
.detail-block > view {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}
</style>
