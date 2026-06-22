<script setup lang="ts">
import {
  BellOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  RightOutlined,
} from '@ant-design/icons-vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import type {
  WorkplaceActivity,
  WorkplaceNotice,
  WorkplaceOverviewItem,
  WorkplaceSchedule,
  WorkplaceShortcut,
  WorkplaceTeam,
  WorkplaceTodoItem,
} from '@/types/api'
import { getWorkplaceData } from '@/apis/dashboard'
import { getIconComponent } from '@/utils/icons'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const { t, locale } = useI18n()

const overview = ref<WorkplaceOverviewItem[]>([])
const todos = ref<WorkplaceTodoItem[]>([])
const shortcuts = ref<WorkplaceShortcut[]>([])
const notices = ref<WorkplaceNotice[]>([])
const schedules = ref<WorkplaceSchedule[]>([])
const activities = ref<WorkplaceActivity[]>([])
const teams = ref<WorkplaceTeam[]>([])

const profileName = computed(() => user.profile?.real_name || t('profile.fallbackName'))
const profileTitle = computed(() => user.profile?.title || t('profile.fallbackTitle'))
const avatarText = computed(() => profileName.value.slice(0, 1))
const pendingTodoCount = computed(() => todos.value.filter((item) => item.status !== 'processing').length)

const overviewColorClass: Record<WorkplaceOverviewItem['color'], string> = {
  blue: 'bg-blue-50 text-blue-600 dark:bg-blue-500/12 dark:text-blue-300',
  green: 'bg-green-50 text-green-600 dark:bg-green-500/12 dark:text-green-300',
  orange: 'bg-orange-50 text-orange-600 dark:bg-orange-500/12 dark:text-orange-300',
  red: 'bg-red-50 text-red-600 dark:bg-red-500/12 dark:text-red-300',
}

const priorityColor: Record<WorkplaceTodoItem['priority'], string> = {
  high: 'red',
  medium: 'orange',
  low: 'blue',
}

const priorityText: Record<WorkplaceTodoItem['priority'], string> = {
  high: 'workplace.highPriority',
  medium: 'workplace.mediumPriority',
  low: 'workplace.lowPriority',
}

const statusColor: Record<WorkplaceTodoItem['status'], string> = {
  pending: 'processing',
  processing: 'success',
  overdue: 'error',
}

const statusText: Record<WorkplaceTodoItem['status'], string> = {
  pending: 'workplace.pending',
  processing: 'workplace.processing',
  overdue: 'workplace.overdue',
}

const noticeColor: Record<WorkplaceNotice['level'], string> = {
  info: 'processing',
  warning: 'warning',
  success: 'success',
}

const shortcutColorClass: Record<string, string> = {
  blue: 'bg-blue-50 text-blue-600 dark:bg-blue-500/12 dark:text-blue-300',
  green: 'bg-green-50 text-green-600 dark:bg-green-500/12 dark:text-green-300',
  purple: 'bg-purple-50 text-purple-600 dark:bg-purple-500/12 dark:text-purple-300',
  orange: 'bg-orange-50 text-orange-600 dark:bg-orange-500/12 dark:text-orange-300',
  cyan: 'bg-cyan-50 text-cyan-600 dark:bg-cyan-500/12 dark:text-cyan-300',
  red: 'bg-red-50 text-red-600 dark:bg-red-500/12 dark:text-red-300',
}

function go(path: string) {
  router.push(path)
}

function getShortcutIcon(item: WorkplaceShortcut) {
  return getIconComponent(item.icon)
}

function asTodoItem(item: unknown) {
  return item as WorkplaceTodoItem
}

function asActivityItem(item: unknown) {
  return item as WorkplaceActivity
}

async function loadData() {
  const data = await getWorkplaceData()
  overview.value = data.overview
  todos.value = data.todos
  shortcuts.value = data.shortcuts
  notices.value = data.notices
  schedules.value = data.schedules
  activities.value = data.activities
  teams.value = data.teams
}

onMounted(async () => {
  await user.ensureMe()
  await loadData()
})

watch(locale, loadData)
</script>

<template>
  <div class="text-slate-700 dark:text-zinc-300">
    <div class="mb-6 rounded-2 border border-slate-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900 sm:p-6">
      <div class="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex min-w-0 items-start gap-4 sm:items-center">
          <AAvatar :size="72" class="shrink-0 bg-brand-500 text-28px text-white">
            {{ avatarText }}
          </AAvatar>
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <h2 class="m-0 truncate text-20px text-slate-900 font-600 leading-8 dark:text-zinc-100">
                {{ t('workplace.greeting', { name: profileName }) }}
              </h2>
              <ATag color="processing" class="m-0">{{ profileTitle }}</ATag>
            </div>
            <p class="m-0 mt-1 text-14px text-slate-500 leading-6 dark:text-zinc-400">
              {{ t('workplace.todoSummary', { count: pendingTodoCount }) }}
            </p>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4 xl:min-w-150">
          <div
            v-for="item in overview"
            :key="item.title"
            class="min-w-0 rounded-2 border border-slate-100 p-3 dark:border-zinc-800"
          >
            <div class="text-12px text-slate-500 dark:text-zinc-400">{{ item.title }}</div>
            <div class="mt-1 flex items-end justify-between gap-2">
              <span class="truncate text-24px font-700 leading-8" :class="overviewColorClass[item.color]">
                {{ item.value }}
              </span>
            </div>
            <div class="mt-1 truncate text-12px text-slate-400 dark:text-zinc-500">
              {{ item.description }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <ARow :gutter="[24, 24]">
      <ACol :xs="24" :xl="14" class="flex">
        <ACard :title="t('workplace.myTodos')" :bordered="false" class="w-full" :body-style="{ padding: 0 }">
          <template #extra>
            <AButton type="link" size="small" @click="go('/dashboard/analysis')">{{ t('workplace.viewEfficiency') }}</AButton>
          </template>
          <AList :data-source="todos">
            <template #renderItem="{ item }">
              <AListItem class="px-4! py-3.5! sm:px-6!">
                <AListItemMeta>
                  <template #avatar>
                    <div
                      class="mt-1 inline-flex h-9 w-9 items-center justify-center rounded-2"
                      :class="asTodoItem(item).status === 'overdue' ? 'bg-red-50 text-red-600 dark:bg-red-500/12 dark:text-red-300' : 'bg-brand-50 text-brand-600 dark:bg-brand-500/12 dark:text-brand-300'"
                    >
                      <ClockCircleOutlined v-if="asTodoItem(item).status !== 'processing'" />
                      <CheckCircleOutlined v-else />
                    </div>
                  </template>
                  <template #title>
                    <div class="flex min-w-0 flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                      <AButton
                        class="h-auto! min-w-0! p-0! text-left! text-15px! text-slate-900! font-600! hover:text-brand-600! dark:text-zinc-100! dark:hover:text-brand-300!"
                        type="link"
                        @click="go(asTodoItem(item).path)"
                      >
                        <span class="block truncate">{{ asTodoItem(item).title }}</span>
                      </AButton>
                      <div class="flex shrink-0 flex-wrap items-center gap-1.5">
                        <ATag :color="priorityColor[asTodoItem(item).priority]" class="m-0">
                          {{ t(priorityText[asTodoItem(item).priority]) }}
                        </ATag>
                        <ATag :color="statusColor[asTodoItem(item).status]" class="m-0">
                          {{ t(statusText[asTodoItem(item).status]) }}
                        </ATag>
                      </div>
                    </div>
                  </template>
                  <template #description>
                    <div class="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-13px text-slate-500 dark:text-zinc-400">
                      <span>{{ asTodoItem(item).module }}</span>
                      <span>{{ t('workplace.owner', { owner: asTodoItem(item).owner }) }}</span>
                      <span>{{ t('workplace.due', { time: asTodoItem(item).due_time }) }}</span>
                    </div>
                  </template>
                </AListItemMeta>
                <template #actions>
                  <AButton size="small" type="link" @click="go(asTodoItem(item).path)">{{ t('workplace.handle') }}</AButton>
                </template>
              </AListItem>
            </template>
          </AList>
        </ACard>
      </ACol>

      <ACol :xs="24" :xl="10" class="flex">
        <ACard :title="t('workplace.shortcuts')" :bordered="false" class="w-full" :body-style="{ padding: '16px' }">
          <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <AButton
              v-for="item in shortcuts"
              :key="item.path"
              class="group h-auto! min-w-0! justify-start! rounded-2! border-slate-100! bg-white! p-3! text-left! hover:border-brand-300! dark:border-zinc-800! dark:bg-zinc-900! dark:hover:border-brand-500/70!"
              @click="go(item.path)"
            >
              <span class="flex min-w-0 items-center gap-3">
                <span
                  class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2 text-18px"
                  :class="shortcutColorClass[item.color]"
                >
                  <component :is="getShortcutIcon(item)" v-if="getShortcutIcon(item)" />
                </span>
                <span class="min-w-0 flex-1">
                  <span class="block truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{ item.title }}</span>
                  <span class="mt-0.5 block truncate text-12px text-slate-500 dark:text-zinc-400">{{ item.description }}</span>
                </span>
                <RightOutlined class="shrink-0 text-11px text-slate-300 transition group-hover:text-brand-500" />
              </span>
            </AButton>
          </div>
        </ACard>
      </ACol>
    </ARow>

    <ARow :gutter="[24, 24]" class="mt-6">
      <ACol :xs="24" :xl="14" class="flex">
        <ACard :title="t('workplace.activities')" :bordered="false" class="w-full" :body-style="{ minHeight: '238px' }">
          <AList :data-source="activities">
            <template #renderItem="{ item }">
              <AListItem class="px-0!">
                <AListItemMeta>
                  <template #avatar>
                    <AAvatar size="small" class="bg-slate-100 text-slate-600 dark:bg-zinc-800 dark:text-zinc-300">
                      {{ asActivityItem(item).user.slice(0, 1) }}
                    </AAvatar>
                  </template>
                  <template #title>
                    <span class="text-slate-700 dark:text-zinc-300">{{ asActivityItem(item).user }}</span>
                    <span class="text-slate-500 dark:text-zinc-400">&nbsp;{{ asActivityItem(item).action }}&nbsp;</span>
                    <a>{{ asActivityItem(item).target }}</a>
                  </template>
                  <template #description>{{ asActivityItem(item).time }}</template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>
        </ACard>
      </ACol>

      <ACol :xs="24" :xl="10" class="flex">
        <ACard :title="t('workplace.notices')" :bordered="false" class="w-full" :body-style="{ minHeight: '238px' }">
          <div class="space-y-4">
            <div v-for="item in notices" :key="item.id" class="flex gap-3">
              <BellOutlined class="mt-1 shrink-0 text-15px text-brand-500" />
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-2">
                  <div class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{ item.title }}</div>
                  <ATag :color="noticeColor[item.level]" class="m-0 shrink-0">{{ item.time }}</ATag>
                </div>
                <p class="m-0 mt-1 text-13px text-slate-500 leading-5 dark:text-zinc-400">{{ item.content }}</p>
              </div>
            </div>
          </div>
        </ACard>
      </ACol>
    </ARow>

    <ARow :gutter="[24, 24]" class="mt-6">
      <ACol :xs="24" :xl="12" class="flex">
        <ACard :title="t('workplace.todaySchedule')" :bordered="false" class="w-full" :body-style="{ minHeight: '210px' }">
          <div class="space-y-3">
            <div
              v-for="item in schedules"
              :key="item.id"
              class="flex items-start gap-3 rounded-2 border border-slate-100 p-3 dark:border-zinc-800"
            >
              <CalendarOutlined class="mt-1 shrink-0 text-brand-500" />
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-2">
                  <span class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{ item.title }}</span>
                  <ATag :color="item.status === 'done' ? 'success' : 'processing'" class="m-0 shrink-0">
                    {{ item.status === 'done' ? t('workplace.done') : t('workplace.todo') }}
                  </ATag>
                </div>
                <div class="mt-1 text-13px text-slate-500 dark:text-zinc-400">{{ item.time }} · {{ item.participant }}</div>
              </div>
            </div>
          </div>
        </ACard>
      </ACol>

      <ACol :xs="24" :xl="12" class="flex">
        <ACard :title="t('workplace.teams')" :bordered="false" class="w-full" :body-style="{ minHeight: '210px' }">
          <ARow :gutter="[12, 12]">
            <ACol v-for="item in teams" :key="item.name" :span="12">
              <div class="flex min-w-0 items-center rounded-2 border border-slate-100 p-2 dark:border-zinc-800">
                <AAvatar size="small" class="shrink-0 bg-slate-100 text-slate-600 dark:bg-zinc-800 dark:text-zinc-300">
                  {{ item.name.slice(0, 1) }}
                </AAvatar>
                <div class="ml-2 min-w-0">
                  <div class="truncate text-13px text-slate-900 font-600 dark:text-zinc-100">{{ item.name }}</div>
                  <div class="truncate text-12px text-slate-500 dark:text-zinc-400">{{ item.title }}</div>
                </div>
              </div>
            </ACol>
          </ARow>
        </ACard>
      </ACol>
    </ARow>
  </div>
</template>

<style scoped>
:deep(.ant-card) {
  height: 100%;
}
</style>
