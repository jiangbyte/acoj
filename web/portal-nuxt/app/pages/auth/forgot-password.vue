<script setup lang="ts">
const toast = useToast()
const http = useHttp()
const captcha = useCaptcha()
const loading = ref(false)
const step = ref<'email' | 'reset' | 'done'>('email')

const form = reactive({
  email: '',
  token: '',
  password: '',
  confirmPassword: '',
  captchaValue: '',
})

onMounted(() => captcha.refresh())

async function sendResetEmail() {
  if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) {
    toast.add({ title: '错误', description: '邮箱格式不正确', color: 'error' })
    return
  }
  if (!captcha.captchaValue.value?.trim()) {
    toast.add({ title: '错误', description: '请输入验证码', color: 'error' })
    return
  }

  loading.value = true
  try {
    await http.post(
      '/api/v1/portal/forgot-password',
      {
        email: form.email.trim(),
        captcha_id: captcha.captchaId.value,
        captcha_value: captcha.captchaValue.value.trim(),
      },
      { addToken: false },
    )
    toast.add({ title: '重置邮件已发送', description: '请查收邮箱中的重置链接', color: 'success' })
    step.value = 'reset'
  } catch {
    captcha.refresh()
  } finally {
    loading.value = false
  }
}

async function resetPassword() {
  if (!form.password || form.password.length < 6) {
    toast.add({ title: '错误', description: '密码至少 6 个字符', color: 'error' })
    return
  }
  if (form.password !== form.confirmPassword) {
    toast.add({ title: '错误', description: '两次密码输入不一致', color: 'error' })
    return
  }

  loading.value = true
  try {
    const { data: key } = await http.get('/api/v1/portal/password-key', { addToken: false })
    const { encryptPassword } = await import('~/utils/crypto')
    const encrypted = await encryptPassword(form.password, {
      key_id: key.key_id,
      public_key: key.public_key,
    })

    await http.post(
      '/api/v1/portal/reset-password',
      {
        email: form.email.trim(),
        token: form.token,
        password: encrypted.encrypted,
        password_key_id: key.key_id,
        captcha_id: captcha.captchaId.value,
        captcha_value: captcha.captchaValue.value.trim(),
      },
      { addToken: false },
    )
    toast.add({ title: '密码已重置', description: '请使用新密码登录', color: 'success' })
    step.value = 'done'
  } catch {
    captcha.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-dvh items-center justify-center p-4">
    <UCard class="w-full max-w-md">
      <template #header>
        <div class="flex items-center gap-3">
          <div
            class="flex size-10 items-center justify-center rounded-full bg-primary/10 text-primary"
          >
            <UIcon name="i-lucide-lock" class="size-5" />
          </div>
          <div>
            <h2 class="text-lg font-semibold">忘记密码</h2>
            <p class="text-sm text-muted">通过邮箱重置密码</p>
          </div>
        </div>
      </template>

      <div v-if="step === 'email'" class="space-y-4">
        <UFormField label="邮箱" required>
          <UInput v-model="form.email" type="email" placeholder="your@example.com" class="w-full" />
        </UFormField>

        <UFormField label="验证码" required>
          <div class="flex gap-2 w-full items-stretch">
            <UInput v-model="captcha.captchaValue.value" placeholder="输入验证码" class="flex-1" />
            <div
              class="w-24 flex-shrink-0 cursor-pointer rounded overflow-hidden bg-muted"
              @click="captcha.refresh()"
            >
              <img
                v-if="captcha.captchaImage.value"
                :src="captcha.captchaImage.value"
                alt="验证码"
                class="h-full w-full object-contain"
              />
              <div v-else class="flex items-center justify-center h-full text-xs text-muted">
                点击获取
              </div>
            </div>
          </div>
        </UFormField>

        <UButton color="primary" size="lg" block :loading="loading" @click="sendResetEmail">
          发送重置邮件
        </UButton>
      </div>

      <div v-else-if="step === 'reset'" class="space-y-4">
        <UFormField label="邮箱">
          <UInput :model-value="form.email" disabled class="w-full" />
        </UFormField>
        <UFormField label="重置 Token" required hint="请从邮件中获取">
          <UInput v-model="form.token" placeholder="粘贴邮件中的 token" class="w-full" />
        </UFormField>
        <UFormField label="新密码" required>
          <UInput
            v-model="form.password"
            type="password"
            placeholder="至少 6 个字符"
            class="w-full"
          />
        </UFormField>
        <UFormField label="确认密码" required>
          <UInput
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            class="w-full"
          />
        </UFormField>
        <UFormField label="验证码" required>
          <div class="flex gap-2 w-full items-stretch">
            <UInput v-model="captcha.captchaValue.value" placeholder="输入验证码" class="flex-1" />
            <div
              class="w-24 flex-shrink-0 cursor-pointer rounded overflow-hidden bg-muted"
              @click="captcha.refresh()"
            >
              <img
                v-if="captcha.captchaImage.value"
                :src="captcha.captchaImage.value"
                alt="验证码"
                class="h-full w-full object-contain"
              />
              <div v-else class="flex items-center justify-center h-full text-xs text-muted">
                点击获取
              </div>
            </div>
          </div>
        </UFormField>
        <UButton color="primary" size="lg" block :loading="loading" @click="resetPassword">
          重置密码
        </UButton>
      </div>

      <div v-else class="text-center py-8 space-y-4">
        <UIcon name="i-lucide-check-circle" class="size-12 text-success mx-auto" />
        <p class="text-lg font-semibold">密码已重置</p>
        <UButton color="primary" to="/auth/login">返回登录</UButton>
      </div>

      <template #footer>
        <p class="text-center text-sm text-muted">
          想起密码了？
          <NuxtLink to="/auth/login" class="text-primary font-medium">返回登录</NuxtLink>
        </p>
      </template>
    </UCard>
  </div>
</template>
