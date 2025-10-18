<script lang="ts" setup>
import { NButton, NCard, NForm, NFormItem, NInput, NText } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { useTokenStore, useUserStore } from '@/stores'
import { useAuthFetch, useSysConfigFetch } from '@/composables/v1'

// 表单数据
const formRef = ref<FormInst | null>(null)
const formData = ref({
  username: '',
  password: '',
  email: '',
  captchaCode: '',
  uuid: '',
  platform: 'ADMIN',
})
const formRules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur',
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur',
  },
  email: {
    required: true,
    message: '请输入邮箱',
    trigger: 'blur',
  },
  captchaCode: {
    required: true,
    message: '请输入验证码',
    trigger: 'blur',
  },
}

// 请求处理
const { captcha, getProfileNoe } = useAuthFetch()
const { getValueByCode } = useSysConfigFetch()
const captchaRef = ref({
  captcha: '',
  uuid: '',
})
const appName = ref('')
const appAdminLoginBG = ref('')

async function loadCaptcha() {
  const { data } = await captcha()
  if (data) {
    captchaRef.value = data
    formData.value.uuid = captchaRef.value.uuid
  }
}
// 刷新验证码
function refreshCaptcha() {
  loadCaptcha()
  formData.value.captchaCode = ''
  formData.value.uuid = ''
}
async function loadData() {
  await loadCaptcha()
  const [appConfig, bgConfig] = await Promise.all([
    getValueByCode({ code: 'APP_NAME' }),
    getValueByCode({ code: 'APP_PC_REGISTER_BACKGROUND' }),
  ])

  // 处理配置项
  appName.value = appConfig.data ?? appName.value
  appAdminLoginBG.value = bgConfig.data ?? appAdminLoginBG.value
}
loadData()

const router = useRouter()
const useToken = useTokenStore()
const useUser = useUserStore()
async function handleRegister(e: MouseEvent) {
  e.preventDefault()
  formRef.value?.validate((errors) => {
    if (!errors) {
      useAuthFetch().doRegister(formData.value).then(({ data }) => {
        if (data) {
          useToken.setToken(data)
          if (useToken.isLogined) {
            router.push('/')
          }
          getProfileNoe().then(({ data }) => {
            useUser.setUserId(data.id)
          })
        }
      })
    }
    else {
      window.$message.error('请填写完整信息')
    }
  })
}

const version = import.meta.env.VITE_VERSION
</script>

<template>
  <div
    class="h-screen w-screen flex flex-col justify-center items-center"
    :style="appAdminLoginBG ? { background: `url(${appAdminLoginBG}) no-repeat center center`, backgroundSize: 'cover' } : {}"
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
        @keypress.enter="handleRegister"
      >
        <NFormItem
          label="用户名"
          path="username"
        >
          <NInput
            v-model:value="formData.username"
            placeholder="请输入用户名"
          />
        </NFormItem>
        <NFormItem
          label="邮箱"
          path="email"
        >
          <NInput
            v-model:value="formData.email"
            placeholder="请输入邮箱"
          />
        </NFormItem>
        <NFormItem
          label="密码"
          path="password"
        >
          <NInput
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
          />
        </NFormItem>
        <NFormItem
          label="验证码"
          path="captchaCode"
        >
          <div class="flex items-center gap-2">
            <NInput
              v-model:value="formData.captchaCode"
              placeholder="请输入验证码"
              class="flex-1"
            />
            <div
              class="h-[34px] cursor-pointer"
              @click="refreshCaptcha"
            >
              <img
                v-if="captchaRef.captcha"
                :src="captchaRef.captcha"
                alt="验证码"
                class="h-full"
              >
              <div
                v-else
                class="h-full w-[100px] bg-gray-100 flex items-center justify-center text-gray-400"
              >
                点击刷新
              </div>
            </div>
          </div>
        </NFormItem>
      </NForm>
      <div class="text-center">
        <NButton
          block
          type="primary"
          @click="handleRegister"
        >
          注册
        </NButton>
      </div>
      <div class="mt-4 flex justify-between items-center">
        <!-- <NButton text>
          <NText depth="3" @click="$router.push('/forget')">
            重置密码
          </NText>
        </NButton> -->
        <div class="flex items-center gap-2">
          <NText depth="3">
            已有账号？
          </NText>
          <NButton
            text
            type="primary"
            @click="$router.push('/login')"
          >
            登录
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
