<script lang="ts" setup>
import { NButton, NCard, NForm, NFormItem, NInput, NText } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { useTokenStore } from '@/stores'
import { useAuthFetch } from '@/composables/v1'

// Implementation note.
const formRef = ref<FormInst | null>(null)
const formData = ref({
  username: 'super',
  password: '123456789',
  captchaCode: '',
  uuid: '',
  platform: 'ADMIN',
})
const formRules = {
  username: {
    required: true,
    message: 'Enter your username',
    trigger: 'blur',
  },
  password: {
    required: true,
    message: 'Enter your password',
    trigger: 'blur',
  },
  captchaCode: {
    required: true,
    message: 'Enter the verification code',
    trigger: 'blur',
  },
}

// Implementation note.
const { doLogin, captcha } = useAuthFetch()
const captchaRef = ref({
  captcha: '',
  uuid: '',
})
const appName = 'ACOJ'

async function loadCaptcha() {
  const { data } = await captcha()
  if (data) {
    captchaRef.value = data
    formData.value.uuid = captchaRef.value.uuid
  }
}
// RefreshVerification Code
function refreshCaptcha() {
  loadCaptcha()
  formData.value.captchaCode = ''
  formData.value.uuid = ''
}
loadCaptcha()

const router = useRouter()
const useToken = useTokenStore()
async function handleLogin() {
  const { data: token } = await doLogin(formData.value) || { data: null }
  if (token) {
    useToken.setToken(token)
    if (useToken.isLogined) {
      router.push('/')
    }
  }
}

const version = import.meta.env.VITE_VERSION
</script>

<template>
  <div
    class="h-screen w-screen flex flex-col justify-center items-center"
  >
    <NCard
      class="w-auto filter-bg"
      :bordered="false"
    >
      <div class="flex flex-col items-center gap-2 mb-2">
        <div class="text-center text-xl">
          {{ appName }}
        </div>
        <NText
          depth="3"
          class="text-center text-3"
        >
          Version {{ version }}
        </NText>
      </div>
      <NForm
        ref="formRef"
        :label-width="80"
        :model="formData"
        :rules="formRules"
        class="w-[300px]"
        :show-label="false"
      >
        <NFormItem
          label="Username"
          path="username"
        >
          <NInput
            v-model:value="formData.username"
            placeholder="Enter your username"
          />
        </NFormItem>
        <NFormItem
          label="Password"
          path="password"
        >
          <NInput
            v-model:value="formData.password"
            type="password"
            placeholder="Enter your password"
          />
        </NFormItem>
        <NFormItem
          label="Verification Code"
          path="captchaCode"
        >
          <div class="flex items-center gap-2">
            <NInput
              v-model:value="formData.captchaCode"
              placeholder="Enter the verification code"
              class="flex-1"
            />
            <div
              class="h-[34px] cursor-pointer"
              @click="refreshCaptcha"
            >
              <img
                v-if="captchaRef.captcha"
                :src="captchaRef.captcha"
                alt="Verification Code"
                class="h-full"
              >
              <div
                v-else
                class="h-full w-[100px] bg-gray-100 flex items-center justify-center text-gray-400"
              >
                Click to refresh
              </div>
            </div>
          </div>
        </NFormItem>
      </NForm>
      <div class="text-center">
        <NButton
          block
          type="primary"
          @click="handleLogin"
        >
          Sign In
        </NButton>
      </div>
      <div class="mt-4 flex justify-between items-center">
        <NButton text>
          <NText depth="3">
            Reset Password
          </NText>
        </NButton>
        <div class="flex items-center gap-2">
          <NText depth="3">
            Don't have an account?
          </NText>
          <NButton
            text
            type="primary"
            @click="$router.push('/register')"
          >
            Sign Up
          </NButton>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-center items-center">
          <NText
            class="text-3"
            depth="3"
          >
            © 2024-2025 by Charlie Zhang
          </NText>
        </div>
      </template>
    </NCard>
  </div>
</template>

<style scoped>
/* .filter-bg {
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.8);
} */
</style>
