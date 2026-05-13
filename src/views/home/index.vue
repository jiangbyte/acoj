<template>
  <div class="max-w-1400px" mx-auto>
    <a-row :gutter="12">
      <a-col :xs="24" :lg="16" class="mb-4">
        <div class="flex flex-col gap-3">
          <UserInfoCard />

          <a-card :bordered="false" :loading="loading">
            <template #title>
              <div class="flex justify-between items-center">
                <div class="flex items-center gap-2">
                  <span class="text-base font-medium">快捷工作台</span>
                  <span class="text-xs text-$text-color-secondary hidden sm:inline">常用功能快捷入口</span>
                </div>
                <a-button type="link" size="small" @click="showAddDrawer = true" v-if="available.length">
                  + 添加
                </a-button>
              </div>
            </template>
            <div v-if="actions.length" class="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div
                v-for="item in actions"
                :key="item.id"
                class="flex flex-col items-center gap-2 p-5 pb-4 bg-$background-color-light rounded-lg cursor-pointer transition duration-250 relative hover:-translate-y-1 hover:shadow-sm"
                @click="navigate(item)"
                @mouseenter="hoverId = item.id"
                @mouseleave="hoverId = null"
              >
                <div class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl" :style="{ background: iconBg(item.name) }">
                  <component :is="resolveIcon(item.icon)" />
                </div>
                <span class="text-sm font-medium text-$text-color">{{ item.name }}</span>
                <a-button
                  v-if="hoverId === item.id"
                  type="text"
                  size="small"
                  class="absolute top-1 right-1 text-$color-danger"
                  @click.stop="removeAction(item)"
                >
                  <template #icon><CloseOutlined /></template>
                </a-button>
              </div>
            </div>
            <div v-else class="flex items-center justify-center gap-1 py-8 text-$text-color-secondary">
              <span>暂无快捷入口</span>
              <a-button type="link" @click="showAddDrawer = true" v-if="available.length">去添加</a-button>
            </div>
          </a-card>
        </div>
      </a-col>

      <a-col :xs="24" :lg="8" class="mb-4">
        <a-card title="系统通知" :bordered="false" :loading="loading">
          <div v-if="data?.notices.length" class="flex flex-col">
            <div
              v-for="notice in data.notices"
              :key="notice.id"
              class="flex items-center gap-2.5 py-2.5 border-b border-$border-color-split last:border-b-0"
            >
              <a-tag :color="$dict.color('NOTICE_LEVEL', notice.level)" class="text-11px leading-18px px-1.5 flex-shrink-0">{{ $dict.label('NOTICE_LEVEL', notice.level) }}</a-tag>
              <span class="flex-1 text-sm text-$text-color truncate">{{ notice.title }}</span>
              <span class="text-xs text-$text-color-secondary flex-shrink-0">{{ notice.created_at }}</span>
            </div>
          </div>
          <div v-else class="py-8 text-center text-$text-color-secondary">暂无系统通知</div>
        </a-card>
      </a-col>
    </a-row>

    <a-drawer title="添加快捷方式" placement="right" :open="showAddDrawer" @close="showAddDrawer = false" width="360">
      <div class="flex flex-col gap-0.5">
        <div
          v-for="res in available"
          :key="res.resource_id"
          class="flex items-center gap-3 p-3 rounded-lg cursor-pointer transition duration-200 hover:bg-$background-color-light"
          @click="addAction(res)"
        >
          <component :is="resolveIcon(res.icon)" class="text-lg text-$primary-color flex-shrink-0" />
          <span class="flex-1 text-sm text-$text-color">{{ res.name }}</span>
          <PlusOutlined class="text-base text-$primary-color" />
        </div>
        <div v-if="!available.length" class="py-8 text-center text-$text-color-secondary">已全部添加</div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { CloseOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { resolveIcon } from '@/utils/iconUtil'
import { fetchHome, addQuickAction, removeQuickAction } from '@/api/home'
import UserInfoCard from '@/views/dashboard/components/UserInfoCard.vue'

interface QuickAction {
  id: string
  resource_id: string
  name: string
  icon: string
  route_path: string
  sort_code: number
}

interface HomeNotice {
  id: string
  title: string
  level: string
  created_at: string | null
}

interface HomeData {
  quick_actions: QuickAction[]
  available_resources: QuickAction[]
  notices: HomeNotice[]
}

defineOptions({ name: 'SysHome' })

const router = useRouter()
const data = shallowRef<HomeData | null>(null)
const loading = ref(true)
const showAddDrawer = ref(false)
const hoverId = ref<string | null>(null)

const actions = ref<QuickAction[]>([])
const available = ref<QuickAction[]>([])

const iconColors = ['#1677ff', '#52c41a', '#faad14', '#722ed1', '#eb2f96', '#fa8c16', '#13c2c2', '#f5222d']

function iconBg(_name: string) {
  const idx = actions.value.findIndex(a => a.name === _name)
  const c = iconColors[idx % iconColors.length]
  return `${c}15`
}

function navigate(item: QuickAction) {
  if (item.route_path) {
    router.push(item.route_path)
  }
}

async function addAction(res: QuickAction) {
    await addQuickAction(res.resource_id)
    actions.value.push({ ...res, id: '', sort_code: actions.value.length * 10 })
    available.value = available.value.filter(a => a.resource_id !== res.resource_id)
    if (!available.value.length) showAddDrawer.value = false
}

async function removeAction(item: QuickAction) {
    await removeQuickAction(item.id)
    actions.value = actions.value.filter(a => a.id !== item.id)
    available.value.push(item)
}

onMounted(async () => {
    const res = await fetchHome()
    if (res.success && res.data) {
      data.value = res.data
      actions.value = res.data.quick_actions || []
      available.value = res.data.available_resources || []
    }
  loading.value = false
})
</script>
