<template>
  <view class="min-h-screen bg-gray-50">
    <view
      class="bg-gradient-to-b from-blue-500 to-blue-600 px-[64rpx] pt-[120rpx] pb-[80rpx] relative overflow-hidden"
    >
      <!-- 装饰圆形 -->
      <view
        class="absolute top-[-40rpx] right-[-40rpx] w-[200rpx] h-[200rpx] rounded-full bg-white/10"
      ></view>
      <view
        class="absolute top-[100rpx] right-[60rpx] w-[80rpx] h-[80rpx] rounded-full bg-white/10"
      ></view>
      <view
        class="absolute bottom-[20rpx] left-[-60rpx] w-[160rpx] h-[160rpx] rounded-full bg-white/10"
      ></view>

      <!-- 装饰小圆点 -->
      <view
        class="absolute top-[160rpx] left-[100rpx] w-[16rpx] h-[16rpx] rounded-full bg-white/20"
      ></view>
      <view
        class="absolute top-[80rpx] right-[200rpx] w-[12rpx] h-[12rpx] rounded-full bg-white/20"
      ></view>
      <view
        class="absolute top-[200rpx] right-[120rpx] w-[20rpx] h-[20rpx] rounded-full bg-white/20"
      ></view>

      <text
        class="text-[48rpx] font-bold text-white block mb-[16rpx] relative z-10"
        >欢迎登录</text
      >
      <text class="text-[28rpx] text-white/80 relative z-10"
        >登录账号以继续使用</text
      >
    </view>

    <view class="px-[64rpx] pt-[48rpx] pb-[64rpx]">
      <u-form :model="form" ref="uFormRef" label-width="0">
        <view class="mb-[48rpx]">
          <text class="text-[32rpx] font-semibold text-gray-900 block mb-[8rpx]"
            >账号密码登录</text
          >
          <text class="text-[26rpx] text-gray-400">请输入您的账号信息</text>
        </view>

        <u-form-item :border-bottom="true">
          <template #left>
            <view class="flex items-center gap-[16rpx]">
              <u-icon name="account-fill" size="40" color="#3b82f6"></u-icon>
              <text class="text-[28rpx] text-gray-700">账号</text>
            </view>
          </template>
          <u-input
            v-model="form.username"
            placeholder="请输入账号"
            :clearable="true"
          ></u-input>
        </u-form-item>

        <u-form-item :border-bottom="true" class="mt-[32rpx]">
          <template #left>
            <view class="flex items-center gap-[16rpx]">
              <u-icon name="lock-fill" size="40" color="#3b82f6"></u-icon>
              <text class="text-[28rpx] text-gray-700">密码</text>
            </view>
          </template>
          <u-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :password-icon="true"
          ></u-input>
        </u-form-item>

        <view class="flex items-center justify-between mt-[32rpx] mb-[48rpx]">
          <label class="flex items-center gap-[12rpx]">
            <u-checkbox
              v-model="form.remember"
              :round="true"
              size="28"
              shape="circle"
            ></u-checkbox>
            <text class="text-[26rpx] text-gray-500">记住账号</text>
          </label>
          <text class="text-[28rpx] text-blue-500" @click="handleForgotPassword"
            >忘记密码？</text
          >
        </view>

        <u-button
          text="登录"
          type="primary"
          shape="circle"
          size="large"
          @click="handleLogin"
          :loading="loading"
        ></u-button>

        <view class="flex items-center justify-center mt-[48rpx] gap-[16rpx]">
          <text class="text-[28rpx] text-gray-500">还没有账号？</text>
          <text
            class="text-[28rpx] text-blue-500 font-medium"
            @click="handleRegister"
            >立即注册</text
          >
        </view>

        <view class="flex items-center gap-[32rpx] mt-[64rpx]">
          <view class="flex-1 h-[1rpx] bg-gray-200"></view>
          <text class="text-[26rpx] text-gray-400">其他登录方式</text>
          <view class="flex-1 h-[1rpx] bg-gray-200"></view>
        </view>

        <view class="flex items-center justify-center gap-[120rpx] mt-[64rpx]">
          <wechat-login
            @success="handleWechatLoginSuccess"
            @fail="handleWechatLoginFail"
          ></wechat-login>

          <view @click="handlePhoneLogin">
            <u-icon name="phone-fill" size="72" color="#3b82f6"></u-icon>
          </view>
        </view>
      </u-form>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import WechatLogin from '../components/wechat-login.vue'

const uFormRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  password: '',
  remember: false,
})

const handleLogin = () => {
  if (!form.value.username.trim()) {
    uni.showToast({
      title: '请输入账号',
      icon: 'none',
    })
    return
  }

  if (!form.value.password) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none',
    })
    return
  }

  loading.value = true

  setTimeout(() => {
    loading.value = false
    uni.showToast({
      title: '登录成功',
      icon: 'success',
    })

    setTimeout(() => {
      uni.switchTab({
        url: '/pages/index/index',
      })
    }, 1500)
  }, 1500)
}

const handleForgotPassword = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleRegister = () => {
  uni.navigateTo({
    url: '/pages/auth/register/register',
    animationType: 'slide-in-right',
    animationDuration: 300,
  })
}

const handleWechatLoginSuccess = (userInfo: any) => {
  console.log('微信登录成功：', userInfo)
  // 可以在这里处理登录成功后的逻辑
}

const handleWechatLoginFail = (error: string) => {
  console.error('微信登录失败：', error)
}

const handlePhoneLogin = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
