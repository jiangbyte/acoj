<script setup lang="ts">
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { spaceApi } from '@/api'
import { useAuthStore } from '@/stores'
import { resolveFileUrl } from '@/utils'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

const state = reactive({
  loading: false,
  profile: null as any,
  activeTab: 'home',
})

const accountId = computed(() => {
  const id = route.params.accountId
  return typeof id === 'string' ? id : authStore.userInfo?.accountId
})

const displayName = computed(() => state.profile?.nickname || '-')
const avatarUrl = computed(() => resolveFileUrl(state.profile?.avatar))

const handle = computed(() => (state.profile?.account_id ? `ID ${state.profile.account_id}` : ''))
const signature = computed(() => displayValue(state.profile?.signature))
const bio = computed(() => displayValue(state.profile?.bio))

const stats = computed(() => [
  { label: '动态', value: '0' },
  { label: '关注', value: '0' },
  { label: '粉丝', value: '0' },
])

const showcaseItems = computed(() => [
  {
    key: 'intro',
    icon: 'icon-park-outline:topic',
    title: '简介',
    text: bio.value,
  },
  {
    key: 'signature',
    icon: 'icon-park-outline:quote',
    title: '签名',
    text: signature.value,
  },
])

const homeFeeds = computed(() => [
  {
    id: 'feed-1',
    type: '发布视频',
    title: '新内容正在整理',
    text: '公开视频、动态和更新记录将在这里展示。',
    time: '刚刚',
    icon: 'icon-park-outline:video-two',
  },
  {
    id: 'feed-2',
    type: '发布动态',
    title: '公开资料已上线',
    text: '公开资料、简介和精选内容会集中展示在这个空间中。',
    time: '今天',
    icon: 'icon-park-outline:doc-detail',
  },
])

const videos = computed(() => [
  {
    id: 'video-1',
    title: '快速开始',
    meta: '03:28',
  },
  {
    id: 'video-2',
    title: '实践日志',
    meta: '08:16',
  },
  {
    id: 'video-3',
    title: '经验分享',
    meta: '12:04',
  },
  {
    id: 'video-4',
    title: '问答精选',
    meta: '05:42',
  },
])

const collections = computed(() => [
  {
    id: 'collection-1',
    title: '精选',
    desc: '帮助快速了解该空间的代表性公开内容。',
    count: '4 项',
  },
  {
    id: 'collection-2',
    title: '学习记录',
    desc: '按主题长期整理的内容合集。',
    count: '6 项',
  },
])

const likes = computed(() => [
  {
    id: 'like-1',
    title: '值得回看',
    source: '来自公开视频',
    icon: 'icon-park-outline:like',
  },
  {
    id: 'like-2',
    title: '收藏集',
    source: '来自社区精选',
    icon: 'icon-park-outline:star',
  },
  {
    id: 'like-3',
    title: '关注话题',
    source: '来自讨论内容',
    icon: 'icon-park-outline:bookmark',
  },
])

onMounted(loadSpace)
watch(() => route.fullPath, loadSpace)

async function loadSpace() {
  const id = accountId.value
  if (!id) {
    await router.replace('/auth/login')
    return
  }
  state.loading = true
  try {
    const response = await spaceApi.detail(id)
    state.profile = response.data
  } finally {
    state.loading = false
  }
}

function displayValue(value: unknown) {
  return value ? String(value) : '未设置'
}
</script>

<template>
  <div class="min-h-full bg-[var(--body-color)]">
    <NSpin :show="state.loading">
      <section class="border-b border-[var(--border-color)] bg-[var(--card-color)]">
        <div class="space-cover h-42 sm:h-56 lg:h-68">
          <div class="h-full w-full border-b border-[var(--border-color)]" />
        </div>

        <div class="px-4 pb-5 sm:px-6 lg:px-8">
          <div class="-mt-14 flex flex-col gap-4 sm:-mt-16 sm:flex-row sm:items-end">
            <div class="shrink-0">
              <NAvatar
                v-if="avatarUrl"
                round
                :size="128"
                :src="avatarUrl"
                :img-props="avatarImgProps"
                class="space-avatar"
              />
              <NAvatar v-else round :size="128" class="space-avatar">
                <NovaIcon icon="icon-park-outline:user" :size="52" />
              </NAvatar>
            </div>

            <div class="min-w-0 flex-1 pb-1">
              <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
                <div class="min-w-0">
                  <div class="flex min-w-0 flex-wrap items-center gap-3">
                    <h1 class="m-0 max-w-full truncate text-3xl font-800 sm:text-4xl">
                      {{ displayName }}
                    </h1>
                    <NTag v-if="state.profile?.level" round type="primary">
                      {{ state.profile.level }}
                    </NTag>
                  </div>
                  <div class="mt-2 text-sm text-[var(--text-color-3)]">
                    {{ handle }}
                  </div>
                  <p
                    class="mt-3 max-w-4xl text-sm leading-6 text-[var(--text-color-2)] sm:text-base"
                  >
                    {{ signature }}
                  </p>
                </div>

                <div class="grid w-full grid-cols-3 gap-2 sm:w-auto sm:min-w-72">
                  <div
                    v-for="item in stats"
                    :key="item.label"
                    class="rounded border border-[var(--border-color)] bg-[var(--body-color)] px-4 py-3 text-center"
                  >
                    <div class="text-xl font-800">
                      {{ item.value }}
                    </div>
                    <div class="mt-1 text-xs text-[var(--text-color-3)]">
                      {{ item.label }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="space-tabs w-full min-w-0">
        <NTabs v-model:value="state.activeTab" type="line" animated pane-class="space-pane">
          <NTabPane name="home" :tab="'首页'">
            <div class="grid gap-4 px-4 py-4 sm:px-6 lg:grid-cols-[minmax(0,1fr)_360px] lg:px-8">
              <div class="min-w-0 space-y-4">
                <div class="grid gap-4 lg:hidden">
                  <section
                    class="rounded border border-[var(--border-color)] bg-[var(--card-color)] p-5"
                  >
                    <h2 class="m-0 text-base font-750">
                      公开资料
                    </h2>
                    <NDescriptions class="mt-4" :column="1" label-placement="left" size="small">
                      <NDescriptionsItem :label="'昵称'">
                        {{ displayValue(state.profile?.nickname) }}
                      </NDescriptionsItem>
                      <NDescriptionsItem :label="'等级'">
                        {{ displayValue(state.profile?.level) }}
                      </NDescriptionsItem>
                    </NDescriptions>
                  </section>

                  <section
                    class="rounded border border-[var(--border-color)] bg-[var(--card-color)] p-5"
                  >
                    <h2 class="m-0 text-base font-750">
                      关于
                    </h2>
                    <p
                      class="mt-4 whitespace-pre-wrap text-sm leading-7 text-[var(--text-color-2)]"
                    >
                      {{ bio }}
                    </p>
                  </section>
                </div>

                <div class="grid gap-4 lg:grid-cols-2">
                  <article
                    v-for="item in showcaseItems"
                    :key="item.key"
                    class="rounded border border-[var(--border-color)] bg-[var(--card-color)] p-5"
                  >
                    <div class="flex items-center gap-3">
                      <div
                        class="flex h-10 w-10 shrink-0 items-center justify-center rounded bg-[var(--body-color)] text-[var(--primary-color)]"
                      >
                        <NovaIcon :icon="item.icon" :size="22" />
                      </div>
                      <h2 class="m-0 text-base font-750">
                        {{ item.title }}
                      </h2>
                    </div>
                    <p
                      class="mt-4 whitespace-pre-wrap text-sm leading-7 text-[var(--text-color-2)]"
                    >
                      {{ item.text }}
                    </p>
                  </article>
                </div>

                <article class="rounded border border-[var(--border-color)] bg-[var(--card-color)]">
                  <div class="border-b border-[var(--border-color)] px-5 py-4">
                    <h2 class="m-0 text-base font-750">
                      最近动态
                    </h2>
                  </div>
                  <div class="divide-y divide-[var(--border-color)]">
                    <div v-for="item in homeFeeds" :key="item.id" class="flex gap-4 p-5">
                      <div
                        class="flex h-11 w-11 shrink-0 items-center justify-center rounded bg-[var(--body-color)] text-[var(--primary-color)]"
                      >
                        <NovaIcon :icon="item.icon" :size="22" />
                      </div>
                      <div class="min-w-0 flex-1">
                        <div
                          class="flex flex-wrap items-center gap-2 text-xs text-[var(--text-color-3)]"
                        >
                          <span>{{ item.type }}</span>
                          <span>{{ item.time }}</span>
                        </div>
                        <h3 class="mt-2 m-0 text-base font-750">
                          {{ item.title }}
                        </h3>
                        <p class="mt-2 text-sm leading-6 text-[var(--text-color-2)]">
                          {{ item.text }}
                        </p>
                      </div>
                    </div>
                  </div>
                </article>
              </div>

              <aside class="hidden space-y-4 lg:block">
                <section
                  class="rounded border border-[var(--border-color)] bg-[var(--card-color)] p-5"
                >
                  <h2 class="m-0 text-base font-750">
                    公开资料
                  </h2>
                  <NDescriptions class="mt-4" :column="1" label-placement="left" size="small">
                    <NDescriptionsItem :label="'昵称'">
                      {{ displayValue(state.profile?.nickname) }}
                    </NDescriptionsItem>
                    <NDescriptionsItem :label="'等级'">
                      {{ displayValue(state.profile?.level) }}
                    </NDescriptionsItem>
                  </NDescriptions>
                </section>

                <section
                  class="rounded border border-[var(--border-color)] bg-[var(--card-color)] p-5"
                >
                  <h2 class="m-0 text-base font-750">
                    关于
                  </h2>
                  <p class="mt-4 whitespace-pre-wrap text-sm leading-7 text-[var(--text-color-2)]">
                    {{ bio }}
                  </p>
                </section>
              </aside>
            </div>
          </NTabPane>

          <NTabPane name="videos" :tab="'视频'">
            <div class="px-4 py-4 sm:px-6 lg:px-8">
              <article class="rounded border border-[var(--border-color)] bg-[var(--card-color)]">
                <div class="border-b border-[var(--border-color)] px-5 py-4">
                  <h2 class="m-0 text-base font-750">
                    视频
                  </h2>
                </div>
                <div class="grid gap-4 p-5 sm:grid-cols-2 xl:grid-cols-4">
                  <div v-for="item in videos" :key="item.id" class="min-w-0">
                    <div
                      class="aspect-video rounded border border-[var(--border-color)] bg-[var(--body-color)] p-4"
                    >
                      <div class="flex h-full items-end justify-between">
                        <NovaIcon
                          icon="icon-park-outline:video-two"
                          :size="28"
                          class="text-[var(--primary-color)]"
                        />
                        <span
                          class="rounded bg-[var(--card-color)] px-2 py-1 text-xs text-[var(--text-color-3)]"
                        >
                          {{ item.meta }}
                        </span>
                      </div>
                    </div>
                    <h3 class="mt-3 truncate text-sm font-750">
                      {{ item.title }}
                    </h3>
                  </div>
                </div>
              </article>
            </div>
          </NTabPane>

          <NTabPane name="collections" :tab="'合集'">
            <div class="px-4 py-4 sm:px-6 lg:px-8">
              <article class="rounded border border-[var(--border-color)] bg-[var(--card-color)]">
                <div class="border-b border-[var(--border-color)] px-5 py-4">
                  <h2 class="m-0 text-base font-750">
                    合集
                  </h2>
                </div>
                <div class="grid gap-4 p-5 md:grid-cols-2">
                  <div
                    v-for="item in collections"
                    :key="item.id"
                    class="rounded border border-[var(--border-color)] bg-[var(--body-color)] p-5"
                  >
                    <div class="flex items-start gap-4">
                      <div
                        class="grid h-18 w-18 shrink-0 grid-cols-2 gap-1 rounded border border-[var(--border-color)] bg-[var(--card-color)] p-1"
                      >
                        <span
                          v-for="index in 4"
                          :key="index"
                          class="rounded bg-[var(--body-color)]"
                        />
                      </div>
                      <div class="min-w-0">
                        <h3 class="m-0 text-base font-750">
                          {{ item.title }}
                        </h3>
                        <p class="mt-2 text-sm leading-6 text-[var(--text-color-2)]">
                          {{ item.desc }}
                        </p>
                        <div class="mt-3 text-xs text-[var(--text-color-3)]">
                          {{ item.count }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </NTabPane>

          <NTabPane name="likes" :tab="'喜欢'">
            <div class="px-4 py-4 sm:px-6 lg:px-8">
              <article class="rounded border border-[var(--border-color)] bg-[var(--card-color)]">
                <div class="border-b border-[var(--border-color)] px-5 py-4">
                  <h2 class="m-0 text-base font-750">
                    喜欢
                  </h2>
                </div>
                <div class="grid gap-3 p-5 md:grid-cols-3">
                  <div
                    v-for="item in likes"
                    :key="item.id"
                    class="rounded border border-[var(--border-color)] bg-[var(--body-color)] p-4"
                  >
                    <NovaIcon :icon="item.icon" class="text-[var(--primary-color)]" :size="24" />
                    <h3 class="mt-4 m-0 text-sm font-750">
                      {{ item.title }}
                    </h3>
                    <div class="mt-2 text-xs text-[var(--text-color-3)]">
                      {{ item.source }}
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </NTabPane>
        </NTabs>
      </section>
    </NSpin>
  </div>
</template>

<style scoped>
.space-cover {
  background-color: color-mix(in srgb, var(--primary-color) 8%, var(--card-color));
}

.space-avatar {
  border: 4px solid var(--card-color);
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.12);
}

.space-tabs :deep(.n-tabs-nav) {
  position: sticky;
  top: 0;
  z-index: 30;
  background: var(--card-color);
  padding: 0 16px;
}
</style>
