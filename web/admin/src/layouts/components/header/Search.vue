<script setup lang="ts">
import { computed, nextTick, ref, watchEffect } from 'vue'
import { useMagicKeys } from '@vueuse/core'
import { useRouter } from 'vue-router'
import { useBoolean } from '@/hooks'
import { useAppStore, useRouteStore } from '@/stores'

const appStore = useAppStore()
const routeStore = useRouteStore()
const router = useRouter()

interface SearchOption {
  label: string
  value: string
  code: string
  icon?: string
}

const searchValue = ref('')
const selectedIndex = ref(0)
const scrollbarRef = ref()
const {
  bool: showModal,
  setTrue: openModal,
  setFalse: closeModal,
  toggle: toggleModal,
} = useBoolean(false)
const {
  bool: keyboardFlag,
  setTrue: setKeyboardTrue,
  setFalse: setKeyboardFalse,
} = useBoolean(false)

const { ctrl_k, arrowup, arrowdown, enter, escape } = useMagicKeys({
  passive: false,
  onEventFired(e) {
    if (e.ctrlKey && e.key.toLowerCase() === 'k' && e.type === 'keydown') {
      e.preventDefault()
    }
  },
})

watchEffect(() => {
  if (ctrl_k.value) {
    toggleModal()
  }
})

const options = computed<SearchOption[]>(() => {
  const keyword = searchValue.value.trim().toLowerCase()
  if (!keyword) {
    return []
  }

  return routeStore.rowRoutes
    .filter(
      (item) =>
        item.status === 'ENABLED' &&
        item.is_visible &&
        (item.resource_type === 'MENU' || item.resource_type === 'PAGE') &&
        item.path &&
        [item.name, item.path, item.code].some((value) => value.toLowerCase().includes(keyword)),
    )
    .map((item) => ({
      label: item.name,
      value: item.path!,
      code: item.code,
      icon: item.icon ?? undefined,
    }))
})

function handleClose() {
  searchValue.value = ''
  selectedIndex.value = 0
  closeModal()
}

function handleInputChange() {
  selectedIndex.value = 0
}

function handleSelect(value: string) {
  handleClose()
  router.push(value)
  nextTick(() => {
    searchValue.value = ''
  })
}

watchEffect(() => {
  if (!showModal.value) {
    return
  }

  if (escape.value) {
    handleClose()
    return
  }

  if (!options.value.length) {
    return
  }

  setKeyboardTrue()
  if (arrowup.value) {
    handleArrowup()
  }
  if (arrowdown.value) {
    handleArrowdown()
  }
  if (enter.value) {
    handleEnter()
  }
})

function handleArrowup() {
  selectedIndex.value =
    selectedIndex.value === 0 ? options.value.length - 1 : selectedIndex.value - 1
  handleScroll(selectedIndex.value)
}

function handleArrowdown() {
  selectedIndex.value =
    selectedIndex.value === options.value.length - 1 ? 0 : selectedIndex.value + 1
  handleScroll(selectedIndex.value)
}

function handleScroll(currentIndex: number) {
  const keepIndex = 5
  const optionHeight = 70
  const distance =
    currentIndex * optionHeight > keepIndex * optionHeight
      ? currentIndex * optionHeight - keepIndex * optionHeight
      : 0
  scrollbarRef.value?.scrollTo({ top: distance })
}

function handleEnter() {
  const target = options.value[selectedIndex.value]
  if (target) {
    handleSelect(target.value)
  }
}

function handleMouseEnter(index: number) {
  if (!keyboardFlag.value) {
    selectedIndex.value = index
  }
}
</script>

<template>
  <CommonWrapper @click="openModal" class="px-2">
    <NovaIcon icon="icon-park-outline:search" />
    <n-tag v-if="!appStore.isMobile" round size="small" class="font-mono cursor-pointer">
      CtrlK
    </n-tag>
  </CommonWrapper>

  <n-modal
    v-model:show="showModal"
    class="w-560px fixed top-60px inset-x-0 max-w-full"
    size="small"
    preset="card"
    :segmented="{ content: true, footer: true }"
    :closable="false"
    @after-leave="handleClose"
  >
    <template #header>
      <n-input
        v-model:value="searchValue"
        placeholder="搜索菜单 / 路径 / 编码"
        clearable
        size="large"
        @input="handleInputChange"
      >
        <template #prefix>
          <NovaIcon icon="icon-park-outline:search" />
        </template>
      </n-input>
    </template>

    <n-scrollbar ref="scrollbarRef" class="h-450px">
      <ul v-if="options.length" class="flex flex-col gap-8px p-8px p-r-3">
        <n-el
          v-for="(option, index) in options"
          :key="option.value"
          tag="li"
          role="option"
          class="cursor-pointer shadow h-62px transition-colors"
          :class="{
            'text-[var(--base-color)] bg-[var(--primary-color-hover)]': index === selectedIndex,
          }"
          @click="handleSelect(option.value)"
          @mouseenter="handleMouseEnter(index)"
          @mousemove="setKeyboardFalse"
        >
          <div class="grid grid-rows-2 grid-cols-[40px_1fr_30px] h-full p-2">
            <div class="row-span-2 place-self-center">
              <NovaIcon :icon="option.icon" />
            </div>
            <span>{{ option.label }}</span>
            <NovaIcon icon="icon-park-outline:right" class="row-span-2 place-self-center" />
            <span class="op-70">{{ option.value }} / {{ option.code }}</span>
          </div>
        </n-el>
      </ul>

      <n-empty v-else size="large" class="h-450px flex-center" description="暂无结果" />
    </n-scrollbar>

    <template #footer>
      <n-flex class="items-center">
        <span class="flex-y-center gap-1">
          <n-tag size="small" round>Enter</n-tag>
          <span>选择</span>
        </span>
        <span class="flex-y-center gap-1">
          <n-tag size="small" round>↑</n-tag>
          <n-tag size="small" round>↓</n-tag>
          <span>导航</span>
        </span>
        <span class="flex-y-center gap-1">
          <n-tag size="small" round>Esc</n-tag>
          <span>关闭</span>
        </span>
      </n-flex>
    </template>
  </n-modal>
</template>
