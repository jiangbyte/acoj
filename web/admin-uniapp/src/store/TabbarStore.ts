import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTabbarStore = defineStore('tabbar', () => {
  const currentIndex = ref(0)

  function setCurrentIndex(index: number) {
    currentIndex.value = index
  }

  return {
    currentIndex,
    setCurrentIndex,
  }
})
