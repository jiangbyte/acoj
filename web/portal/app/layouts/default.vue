<script setup lang="ts">
const { y } = useWindowScroll()

const showBackTop = computed(() => y.value > 300)

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const footerColumns = [
  {
    label: '产品',
    children: [
      { label: '功能', to: '/features' },
      { label: '关于', to: '/about' },
    ],
  },
  {
    label: '资源',
    children: [
      { label: '文档', to: '/docs' },
      { label: 'API', to: '/api' },
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
          &copy; {{ new Date().getFullYear() }} HEI. All rights reserved.
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
