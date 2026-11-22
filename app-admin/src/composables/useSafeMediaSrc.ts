/**
 * 预检远端图片地址：仅在加载成功后返回可展示的 src，避免破图闪烁。
 */
export function useSafeMediaSrc(source: MaybeRefOrGetter<string | null | undefined>) {
  const displaySrc = ref<string | undefined>()
  let requestId = 0

  // 过滤空值、占位符，避免对无效地址发起请求
  function isUsable(value?: string | null): value is string {
    if (value == null)
      return false
    const trimmed = String(value).trim()
    if (!trimmed || trimmed === '#')
      return false
    const lower = trimmed.toLowerCase()
    return lower !== 'null' && lower !== 'undefined'
  }

  watch(() => toValue(source), (value) => {
    // 1. 递增请求世代，丢弃过期 onload/onerror
    const current = ++requestId
    displaySrc.value = undefined
    // 2. 无效地址保持占位，不发请求
    if (!isUsable(value))
      return
    const resolved = String(value).trim()
    // 3. 用 Image 预加载；成功才露出正式地址，失败继续占位
    const img = new Image()
    img.onload = () => {
      if (current === requestId)
        displaySrc.value = resolved
    }
    img.onerror = () => {
      if (current === requestId)
        displaySrc.value = undefined
    }
    img.src = resolved
  }, { immediate: true })

  return { displaySrc }
}
