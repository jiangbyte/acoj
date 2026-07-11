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
        >欢迎注册</text
      >
      <text class="text-[28rpx] text-white/80 relative z-10"
        >创建账号以开始使用</text
      >
    </view>

    <view class="px-[64rpx] pt-[48rpx] pb-[64rpx]">
      <u-form :model="form" ref="uFormRef" label-width="0">
        <view class="mb-[48rpx]">
          <text class="text-[32rpx] font-semibold text-gray-900 block mb-[8rpx]"
            >填写注册信息</text
          >
          <text class="text-[26rpx] text-gray-400">请使用真实信息注册</text>
        </view>

        <u-form-item border="bottom" :border-bottom="true">
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

        <u-form-item border="bottom" :border-bottom="true" class="mt-[32rpx]">
          <template #left>
            <view class="flex items-center gap-[16rpx]">
              <u-icon name="phone-fill" size="40" color="#3b82f6"></u-icon>
              <text class="text-[28rpx] text-gray-700">手机号</text>
            </view>
          </template>
          <u-input
            v-model="form.phone"
            type="number"
            placeholder="请输入手机号"
            :maxlength="11"
          ></u-input>
        </u-form-item>

        <u-form-item border="bottom" :border-bottom="true" class="mt-[32rpx]">
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

        <u-form-item border="bottom" :border-bottom="true" class="mt-[32rpx]">
          <template #left>
            <view class="flex items-center gap-[16rpx]">
              <u-icon name="lock-fill" size="40" color="#3b82f6"></u-icon>
              <text class="text-[28rpx] text-gray-700">确认密码</text>
            </view>
          </template>
          <u-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :password-icon="true"
          ></u-input>
        </u-form-item>

        <view class="flex items-center gap-[12rpx] mt-[32rpx] mb-[48rpx]">
          <u-checkbox
            v-model="form.agree"
            :round="true"
            size="28"
            shape="circle"
          ></u-checkbox>
          <text class="text-[26rpx] text-gray-500">
            我已阅读并同意
            <text class="text-blue-500" @click="handleAgreeClick"
              >《用户协议》</text
            >
            和
            <text class="text-blue-500" @click="handlePrivacyClick"
              >《隐私政策》</text
            >
          </text>
        </view>

        <u-button
          text="注册"
          type="primary"
          shape="circle"
          size="large"
          @click="handleRegister"
          :loading="loading"
        ></u-button>

        <view class="flex items-center justify-center mt-[48rpx] gap-[16rpx]">
          <text class="text-[28rpx] text-gray-500">已有账号？</text>
          <text
            class="text-[28rpx] text-blue-500 font-medium"
            @click="handleLogin"
            >立即登录</text
          >
        </view>
      </u-form>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const uFormRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agree: false,
})

const handleRegister = () => {
  // 验证表单
  if (!form.value.username.trim()) {
    uni.showToast({
      title: '请输入账号',
      icon: 'none',
    })
    return
  }

  if (!form.value.phone) {
    uni.showToast({
      title: '请输入手机号',
      icon: 'none',
    })
    return
  }

  if (form.value.phone.length !== 11) {
    uni.showToast({
      title: '手机号格式不正确',
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

  if (form.value.password.length < 6) {
    uni.showToast({
      title: '密码长度不能少于 6 位',
      icon: 'none',
    })
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    uni.showToast({
      title: '两次密码输入不一致',
      icon: 'none',
    })
    return
  }

  if (!form.value.agree) {
    uni.showToast({
      title: '请先同意用户协议和隐私政策',
      icon: 'none',
    })
    return
  }

  loading.value = true

  // 模拟注册请求
  setTimeout(() => {
    loading.value = false
    uni.showToast({
      title: '注册成功',
      icon: 'success',
    })

    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/index/index',
      })
    }, 1500)
  }, 1500)
}

const handleLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/login/login',
  })
}

const handleAgreeClick = () => {
  uni.showToast({
    title: '查看用户协议',
    icon: 'none',
  })
}

const handlePrivacyClick = () => {
  uni.showToast({
    title: '查看隐私政策',
    icon: 'none',
  })
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
