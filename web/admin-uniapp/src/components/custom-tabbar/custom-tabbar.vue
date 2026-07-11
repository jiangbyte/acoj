<template>
  <u-tabbar
    v-model="currentIndex"
    :list="tabList"
    :mid-button="true"
    @before-change="handleBeforeChange"
  ></u-tabbar>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useTabbarStore } from '@/store/TabbarStore'

const tabbarStore = useTabbarStore()
const currentIndex = ref(0)

const tabList = ref([
  {
    iconPath: 'home',
    selectedIconPath: 'home-fill',
    text: '首页',
    pagePath: '/pages/index/index',
  },
  {
    iconPath: 'plus-circle',
    selectedIconPath: 'plus-circle-fill',
    pagePath: '/pages/publish/publish',
    midButton: true,
    text: '发布',
  },
  {
    iconPath: 'account',
    selectedIconPath: 'account-fill',
    text: '我的',
    pagePath: '/pages/mine/mine',
  },
])

let isSwitching = false

const getCurrentPageIndex = () => {
  const pages = getCurrentPages()
  if (pages.length === 0) return 0

  const currentPage = pages[pages.length - 1]
  const currentPath = '/' + currentPage.route

  const index = tabList.value.findIndex((item) => item.pagePath === currentPath)
  return index >= 0 ? index : 0
}

const handleBeforeChange = (index: number) => {
  if (isSwitching || currentIndex.value === index) return false

  const item = tabList.value[index]
  if (item.pagePath) {
    isSwitching = true
    currentIndex.value = index
    tabbarStore.setCurrentIndex(index)
    uni.switchTab({
      url: item.pagePath,
      complete: () => {
        isSwitching = false
      },
      fail: () => {
        currentIndex.value = getCurrentPageIndex()
        isSwitching = false
      },
    })
  }
  return false
}

onMounted(() => {
  currentIndex.value = getCurrentPageIndex()
})

watch(
  () => tabbarStore.currentIndex,
  (newIndex) => {
    if (newIndex !== currentIndex.value) {
      currentIndex.value = newIndex
    }
  }
)
</script>

<style lang="scss" scoped></style>
