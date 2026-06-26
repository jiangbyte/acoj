<script setup lang="ts">
import { computed, ref } from 'vue'
import NoticeList, { type NoticeItem } from '../common/NoticeList.vue'

// 当前为布局占位通知数据，后续接入接口时可替换为异步拉取结果，NoticeList 的展示结构无需变化。
const notices = ref<NoticeItem[]>([
  {
    id: 1,
    type: 0,
    title: '系统初始化完成',
    icon: 'icon-park-outline:tips-one',
    tagTitle: '系统',
    tagType: 'success',
    description: '管理端基础布局已经准备就绪。',
    date: '刚刚',
  },
  {
    id: 2,
    type: 1,
    title: '待接入真实通知',
    icon: 'icon-park-outline:message',
    tagTitle: '占位',
    tagType: 'info',
    description: '后续可以替换为后端消息接口。',
    date: '今天',
  },
  {
    id: 3,
    type: 2,
    title: '完善业务菜单',
    icon: 'icon-park-outline:checklist',
    tagTitle: '待办',
    tagType: 'warning',
    date: '近期',
  },
])

const currentTab = ref(0)

// 按 type 将通知拆分到三个页签，页签徽标和列表都复用该分组结果，避免模板中重复过滤主列表。
const groups = computed(() => ({
  0: notices.value.filter((item) => item.type === 0),
  1: notices.value.filter((item) => item.type === 1),
  2: notices.value.filter((item) => item.type === 2),
}))

// 顶部铃铛展示所有分组的未读总数，单个页签内再分别展示各自未读数。
const unreadCount = computed(() => notices.value.filter((item) => !item.isRead).length)

// 点击某条通知后只标记该条为已读，保持 ref 数组内对象响应式更新。
function handleRead(id: number) {
  const item = notices.value.find((item) => item.id === id)
  if (item) {
    item.isRead = true
  }
}
</script>

<template>
  <n-popover placement="bottom" trigger="click" arrow-point-to-center class="!p-0">
    <template #trigger>
      <n-tooltip placement="bottom" trigger="hover">
        <template #trigger>
          <CommonWrapper>
            <n-badge :value="unreadCount" :max="99" style="color: unset">
              <NovaIcon icon="icon-park-outline:remind" />
            </n-badge>
          </CommonWrapper>
        </template>
        通知
      </n-tooltip>
    </template>
    <n-tabs
      v-model:value="currentTab"
      type="line"
      animated
      justify-content="space-evenly"
      class="w-390px"
    >
      <n-tab-pane :name="0">
        <template #tab>
          <n-space class="w-130px" justify="center">
            通知
            <n-badge type="info" :value="groups[0].filter((i) => !i.isRead).length" :max="99" />
          </n-space>
        </template>
        <NoticeList :list="groups[0]" @read="handleRead" />
      </n-tab-pane>
      <n-tab-pane :name="1">
        <template #tab>
          <n-space class="w-130px" justify="center">
            消息
            <n-badge type="warning" :value="groups[1].filter((i) => !i.isRead).length" :max="99" />
          </n-space>
        </template>
        <NoticeList :list="groups[1]" @read="handleRead" />
      </n-tab-pane>
      <n-tab-pane :name="2">
        <template #tab>
          <n-space class="w-130px" justify="center">
            待办
            <n-badge type="error" :value="groups[2].filter((i) => !i.isRead).length" :max="99" />
          </n-space>
        </template>
        <NoticeList :list="groups[2]" @read="handleRead" />
      </n-tab-pane>
    </n-tabs>
  </n-popover>
</template>
