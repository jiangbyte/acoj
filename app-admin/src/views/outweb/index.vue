<script lang="ts" setup>
const route = useRoute()

function parseJson(json: string) {
  if (json === 'null' || json === 'undefined') {
    return null
  }
  try {
    return JSON.parse(json)
  }
  catch (e) {
    console.error(e)
    return null
  }
}

const href = ref('')
function loadHref() {
  const parameters = parseJson(String(route.meta.parameters))
  if (parameters) {
    if (parameters?.href_inner) {
      href.value = parameters.href_inner
    }
  }
}

loadHref()

const isLoading = ref(true)
const iframeRef = ref<HTMLIFrameElement>()

function handleLoad() {
  isLoading.value = false
}

function handleError() {
  isLoading.value = false
}

// 刷新iframe（保持当前URL）
function refreshIframe() {
  if (!iframeRef.value)
    return

  const iframe = iframeRef.value
  const currentSrc = iframe.src
  iframe.src = currentSrc
  isLoading.value = true
}

// 重新加载iframe（重新设置src）
function reloadIframe() {
  if (!iframeRef.value)
    return

  const iframe = iframeRef.value
  iframe.src = href.value
  isLoading.value = true
}

// 强制重新加载（添加时间戳避免缓存）
function forceReloadIframe() {
  if (!iframeRef.value)
    return

  const timestamp = new Date().getTime()
  const separator = href.value.includes('?') ? '&' : '?'
  const newUrl = `${href.value}${separator}_t=${timestamp}`

  iframeRef.value.src = newUrl
  isLoading.value = true
}
</script>

<template>
  <div class="w-full h-full flex flex-col relative">
    <!-- 操作条 -->
    <n-flex align="center" justify="space-between" class="p-2 bg-white">
      <!-- 状态显示 -->
      <div class="flex items-center space-x-4 text-sm text-gray-600">
        <div class="flex items-center space-x-1">
          <div
            class="w-2 h-2 rounded-full"
            :class="isLoading ? 'bg-yellow-500 animate-pulse' : 'bg-green-500'"
          />
          <span>{{ isLoading ? '加载中...' : '已加载' }}</span>
        </div>

        <n-tooltip trigger="hover">
          <template #trigger>
            <div class="text-xs text-gray-400 max-w-48 truncate">
              {{ href }}
            </div>
          </template>
          {{ href }}
        </n-tooltip>
      </div>

      <div class="flex items-center space-x-2">
        <n-button
          type="primary"
          size="small"
          @click="refreshIframe"
        >
          <template #icon>
            <n-icon>
              <icon-park-outline-refresh />
            </n-icon>
          </template>
          刷新
        </n-button>

        <n-button
          size="small"
          @click="reloadIframe"
        >
          <template #icon>
            <n-icon>
              <icon-park-outline-refresh />
            </n-icon>
          </template>
          重新加载
        </n-button>

        <n-button
          size="small"
          @click="forceReloadIframe"
        >
          <template #icon>
            <n-icon>
              <icon-park-outline-refresh />
            </n-icon>
          </template>
          强制刷新
        </n-button>
      </div>
    </n-flex>

    <!-- 内容区域 -->
    <div class="flex-1 relative">
      <!-- 加载状态 -->
      <div
        v-if="isLoading"
        class="absolute inset-0 flex items-center justify-center bg-white z-10"
      >
        <div class="text-center">
          <n-spin size="large" />
          <div class="mt-4 text-gray-500">
            正在加载...
          </div>
        </div>
      </div>

      <!-- iframe -->
      <iframe
        ref="iframeRef"
        :src="href"
        frameborder="0"
        class="w-full h-full"
        @load="handleLoad"
        @error="handleError"
      />
    </div>
  </div>
</template>

<style scoped>
</style>
