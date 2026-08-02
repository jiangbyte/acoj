<script setup lang="ts">
const { y } = useWindowScroll()

const showBackTop = computed(() => y.value > 300)

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const footerColumns = [
  {
    label: '练习',
    children: [
      { label: '题库', to: '/problems' },
      { label: '提交', to: '/submissions' },
      { label: '排名', to: '/rank' },
    ],
  },
  {
    label: '竞赛',
    children: [
      { label: '竞赛列表', to: '/contests' },
      { label: '关于', to: '/about' },
    ],
  },
]
</script>

<template>
  <div>
    <AppHeader />

    <UMain>
      <slot />
    </UMain>

    <USeparator type="solid" class="h-px" />

    <UFooter>
      <template #top>
        <UContainer>
          <UFooterColumns :columns="footerColumns" />
        </UContainer>
      </template>
      <template #left>
        <p class="text-muted text-sm">
          &copy; {{ new Date().getFullYear() }} ACOJ. All rights reserved.
        </p>
      </template>
      <template #right>
        <UButton
          icon="icon-park-outline:github"
          color="neutral"
          variant="ghost"
          to="https://github.com"
          target="_blank"
          aria-label="GitHub"
        />
      </template>
    </UFooter>

    <Transition name="fade">
      <UButton
        v-if="showBackTop"
        icon="icon-park-outline:arrow-up"
        color="neutral"
        variant="soft"
        size="lg"
        class="fixed bottom-6 right-6 z-50 rounded-full"
        @click="scrollToTop"
      />
    </Transition>
  </div>
</template>
