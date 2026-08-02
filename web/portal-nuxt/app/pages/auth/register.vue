<script setup lang="ts">
const router = useRouter()
const toast = useToast()
const { register } = useAuth()
const captcha = useCaptcha()
const loading = ref(false)

const formState = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

onMounted(() => {
  captcha.refresh()
})

async function onSubmit() {
  const { username, nickname, email, password, confirmPassword } = formState

  if (!username || username.length < 3 || username.length > 64) {
    toast.add({ title: '错误', description: '用户名需 3-64 个字符', color: 'error' })
    return
  }
  if (!nickname || nickname.length > 64) {
    toast.add({ title: '错误', description: '昵称为必填项', color: 'error' })
    return
  }
  if (!email || !/\S+@\S+\.\S+/.test(email) || email.length > 128) {
    toast.add({ title: '错误', description: '邮箱格式不正确', color: 'error' })
    return
  }
  if (!password || password.length < 6) {
    toast.add({ title: '错误', description: '密码至少 6 个字符', color: 'error' })
    return
  }
  if (password !== confirmPassword) {
    toast.add({ title: '错误', description: '两次密码输入不一致', color: 'error' })
    return
  }
  if (!captcha.captchaValue.value?.trim()) {
    toast.add({ title: '错误', description: '请输入验证码', color: 'error' })
    return
  }

  loading.value = true
  try {
    await register(
      username,
      email,
      password,
      nickname,
      captcha.captchaId.value,
      captcha.captchaValue.value.trim(),
    )
    toast.add({ title: '注册成功，请登录', color: 'success' })
    router.push('/auth/login')
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
            <UIcon name="i-lucide-user-plus" class="size-5" />
          </div>
          <div>
            <h2 class="text-lg font-semibold">创建账号</h2>
            <p class="text-sm text-muted">注册一个新账号</p>
          </div>
        </div>
      </template>

      <UForm :state="formState" @submit="onSubmit">
        <UFormField label="用户名" name="username" required>
          <UInput v-model="formState.username" placeholder="输入用户名" class="w-full" />
        </UFormField>

        <UFormField label="昵称" name="nickname" required>
          <UInput v-model="formState.nickname" placeholder="输入昵称" class="w-full" />
        </UFormField>

        <UFormField label="邮箱" name="email" required>
          <UInput
            v-model="formState.email"
            type="email"
            placeholder="your@example.com"
            class="w-full"
          />
        </UFormField>

        <UFormField label="密码" name="password" required>
          <UInput
            v-model="formState.password"
            type="password"
            placeholder="至少 6 个字符"
            class="w-full"
          />
          <PasswordStrengthBar :password="formState.password" />
        </UFormField>

        <UFormField label="确认密码" name="confirmPassword" required>
          <UInput
            v-model="formState.confirmPassword"
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

        <UButton type="submit" color="primary" size="lg" block :loading="loading" class="mt-6">
          注册
        </UButton>
      </UForm>

      <template #footer>
        <p class="text-center text-sm text-muted">
          已有账号？
          <NuxtLink to="/auth/login" class="text-primary font-medium">立即登录</NuxtLink>
        </p>
      </template>
    </UCard>
  </div>
</template>
