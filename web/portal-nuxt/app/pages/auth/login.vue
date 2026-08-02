<script setup lang="ts">
const router = useRouter()
const toast = useToast()
const { login } = useAuth()
const captcha = useCaptcha()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
})

onMounted(() => {
  captcha.refresh()
})

async function onSubmit() {
  const account = formState.username.trim()
  const password = formState.password
  const captchaValue = captcha.captchaValue.value?.trim()

  if (!account || !password || !captchaValue) {
    toast.add({ title: '错误', description: '请填写完整信息', color: 'error' })
    return
  }

  loading.value = true
  try {
    await login(account, password, captcha.captchaId.value, captchaValue)
    toast.add({ title: '登录成功', color: 'success' })
    router.push('/')
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
            <UIcon name="i-lucide-user" class="size-5" />
          </div>
          <div>
            <h2 class="text-lg font-semibold">欢迎回来</h2>
            <p class="text-sm text-muted">登录你的账号</p>
          </div>
        </div>
      </template>

      <UForm :state="formState" @submit="onSubmit">
        <UFormField label="用户名" name="username" required>
          <UInput v-model="formState.username" placeholder="输入用户名" class="w-full" />
        </UFormField>

        <UFormField label="密码" name="password" required>
          <UInput
            v-model="formState.password"
            type="password"
            placeholder="输入密码"
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

        <UButton type="submit" color="primary" size="lg" block :loading="loading" class="mt-6">
          登录
        </UButton>
      </UForm>

      <template #footer>
        <p class="text-center text-sm text-muted">
          还没有账号？
          <NuxtLink to="/auth/register" class="text-primary font-medium">立即注册</NuxtLink>
          <span class="mx-2">|</span>
          <NuxtLink to="/auth/forgot-password" class="text-primary font-medium">忘记密码</NuxtLink>
        </p>
      </template>
    </UCard>
  </div>
</template>
