<template>
  <div class="flex items-center justify-center h-screen bg-gray-50">
    <a-card class="w-96">
      <h2 class="text-center text-2xl mb-6">Hei Admin</h2>
      <a-form :model="form" layout="vertical" @finish="handleLogin">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item v-if="captcha" label="验证码" name="captcha" :rules="[{ required: true, message: '请输入验证码' }]">
          <div class="flex gap-2">
            <a-input v-model:value="form.captcha" placeholder="验证码" />
            <img :src="captcha" class="w-28 h-8 cursor-pointer" @click="loadCaptcha" />
          </div>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        </a-form-item>
      </a-form>
      <div class="text-center">
        <router-link to="/auth/register">注册账号</router-link>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { fetchCaptcha } from '@/service/api/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const captcha = ref('')
const captchaId = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: '',
})

async function loadCaptcha() {
  const { data } = await fetchCaptcha()
  if (data) {
    captcha.value = data.captcha_base64 || data.captcha_image
    captchaId.value = data.captcha_id
  }
}

async function handleLogin() {
  loading.value = true
  try {
    const ok = await auth.login(form.username, form.password, form.captcha, captchaId.value)
    if (ok) {
      const redirect = (route.query.redirect as string) || import.meta.env.VITE_HOME_PATH || '/dashboard'
      router.push(redirect)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>
