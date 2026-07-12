<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'

const router = useRouter()
const authStore = useAuthStore()

const capabilities = computed(() => [
  {
    icon: 'icon-park-outline:connection-point',
    title: '工作流入口',
    text: '将组织服务、业务流程和个人工作入口集中到清晰的门户导航中。',
  },
  {
    icon: 'icon-park-outline:people',
    title: '团队协作',
    text: '登录后按权限加载动态资源，每个角色只看到需要的内容。',
  },
  {
    icon: 'icon-park-outline:chart-line',
    title: '操作视图',
    text: '为通用企业场景中的数据、公告和服务扩展预留空间。',
  },
])

const metrics = computed(() => [
  { value: '99.9%', label: '可用性' },
  { value: '24/7', label: '服务' },
  { value: '3', label: '核心入口' },
])

function openPrimary() {
  router.push(authStore.isLogin ? '/usercenter' : '/auth/login')
}
</script>

<template>
  <section class="overflow-hidden">
    <div class="px-4 py-10 sm:px-6 sm:py-14 lg:px-8 lg:py-18">
      <div class="grid items-center gap-10 lg:grid-cols-[1.02fr_0.98fr]">
        <div class="min-w-0">
          <div
            class="mb-5 inline-flex items-center gap-2 rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] px-3 py-1 text-sm font-600 text-[var(--primary-color)]"
          >
            <NovaIcon icon="icon-park-outline:building-one" />
            企业门户
          </div>
          <h1 class="max-w-180 text-4xl font-800 leading-tight sm:text-5xl lg:text-6xl">
            团队、流程与服务的统一入口。
          </h1>
          <p class="mt-5 max-w-170 text-base leading-7 text-[var(--text-color-2)] sm:text-lg">
            面向团队的清晰、稳定、响应式业务入口。公开页面可配置，受保护能力在登录后按权限加载。
          </p>
          <div class="mt-8 flex flex-col gap-3 sm:flex-row">
            <NButton type="primary" size="large" :focusable="false" @click="openPrimary">
              {{ authStore.isLogin ? '打开工作台' : '登录' }}
            </NButton>
            <NButton size="large" :focusable="false" @click="router.push('/auth/register')">
              注册
            </NButton>
          </div>
        </div>

        <div
          class="relative min-h-86 rounded-2 border border-[var(--border-color)] bg-[var(--card-color)] p-5 shadow-sm sm:min-h-100 sm:p-6"
        >
          <div class="grid gap-4">
            <div class="rounded-2 bg-[var(--primary-color)] p-5 text-white">
              <div class="text-sm opacity-80">服务概览</div>
              <div class="mt-3 text-2xl font-750">覆盖多端的统一服务体验</div>
              <div class="mt-6 grid grid-cols-3 gap-3">
                <div v-for="item in metrics" :key="item.label">
                  <div class="text-xl font-800">{{ item.value }}</div>
                  <div class="mt-1 text-xs opacity-78">{{ item.label }}</div>
                </div>
              </div>
            </div>

            <div class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="item in capabilities.slice(0, 2)"
                :key="item.title"
                class="rounded-2 border border-[var(--border-color)] p-4"
              >
                <NovaIcon class="text-[var(--primary-color)]" :icon="item.icon" :size="24" />
                <div class="mt-3 font-700">{{ item.title }}</div>
                <div class="mt-1 text-sm leading-6 text-[var(--text-color-3)]">{{ item.text }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="border-y border-[var(--border-color)] bg-[var(--card-color)]">
      <div class="px-4 py-8 sm:px-6 lg:px-8">
        <div class="grid gap-4 md:grid-cols-3">
          <div
            v-for="item in capabilities"
            :key="item.title"
            class="rounded-2 border border-[var(--border-color)] p-5"
          >
            <NovaIcon class="text-[var(--primary-color)]" :icon="item.icon" :size="26" />
            <h2 class="mt-4 text-lg font-750">{{ item.title }}</h2>
            <p class="mt-2 text-sm leading-6 text-[var(--text-color-3)]">
              {{ item.text }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
