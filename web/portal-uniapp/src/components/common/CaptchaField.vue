<template>
  <view class="captcha-field">
    <u-input
      :model-value="captchaValue"
      placeholder="请输入验证码"
      border="surround"
      @update:model-value="$emit('update:captchaValue', $event)"
    ></u-input>
    <image
      v-if="captchaImage"
      class="captcha-field__image"
      :src="captchaImage"
      mode="aspectFit"
      @click="refresh"
    ></image>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { authApi } from '@/api'

defineProps<{
  captchaId: string
  captchaValue: string
}>()

const emit = defineEmits<{
  (event: 'update:captchaId', value: string): void
  (event: 'update:captchaValue', value: string): void
}>()

const captchaImage = ref('')

onMounted(refresh)

async function refresh() {
  const captcha = await authApi.captcha({ format: 'png' })
  emit('update:captchaId', captcha.captcha_id)
  emit('update:captchaValue', '')
  captchaImage.value = `data:${captcha.image_type || 'image/png'};base64,${captcha.image_base64}`
}

defineExpose({ refresh })
</script>

<style lang="scss" scoped>
.captcha-field {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.captcha-field__image {
  width: 156rpx;
  height: 70rpx;
}
</style>
