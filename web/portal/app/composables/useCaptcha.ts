import { captcha as fetchCaptcha } from '~/api/auth'

export function useCaptcha() {
  const captchaId = ref('')
  const captchaImage = ref('')
  const captchaValue = ref('')
  const loading = ref(false)

  async function refresh() {
    if (loading.value) return
    loading.value = true
    try {
      const { data } = await fetchCaptcha('svg')
      captchaId.value = data.captcha_id
      captchaImage.value = `data:${data.image_type};base64,${data.image_base64}`
      captchaValue.value = ''
    } catch {
      captchaImage.value = ''
    } finally {
      loading.value = false
    }
  }

  return {
    captId: computed(() => captchaId.value),
    captchaId: computed(() => captchaId.value),
    captchaImage: computed(() => captchaImage.value),
    captchaValue,
    loading: computed(() => loading.value),
    refresh,
  }
}
