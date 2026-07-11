<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="设置" :placeholder="true" :border="true"></u-navbar>

    <view class="px-[16rpx] mt-[16rpx]">
      <view class="bg-white rounded-xl overflow-hidden mb-[16rpx]">
        <u-cell-group :border="false">
          <u-cell-item
            title="账号与安全"
            :border="true"
            :is-link="true"
            icon="lock-fill"
            :icon-size="40"
            @click="handleSecurity"
          ></u-cell-item>
          <u-cell-item
            title="隐私设置"
            :border="true"
            :is-link="true"
            icon="eye"
            :icon-size="40"
            @click="handlePrivacy"
          ></u-cell-item>
          <u-cell-item
            title="消息通知"
            :border="false"
            :is-link="true"
            icon="bell"
            :icon-size="40"
            @click="handleNotification"
          >
            <template #right>
              <u-switch
                v-model="notificationEnabled"
                :active-color="'#3b82f6'"
                size="40"
              ></u-switch>
            </template>
          </u-cell-item>
        </u-cell-group>
      </view>

      <view class="bg-white rounded-xl overflow-hidden mb-[16rpx]">
        <u-cell-group :border="false">
          <u-cell-item
            title="清除缓存"
            :border="true"
            :is-link="true"
            icon="trash"
            :icon-size="40"
            @click="handleClearCache"
          >
            <template #right>
              <text class="text-sm text-gray-500">{{ cacheSize }}</text>
            </template>
          </u-cell-item>
          <u-cell-item
            title="检查更新"
            :border="true"
            :is-link="true"
            icon="download"
            :icon-size="40"
            @click="handleCheckUpdate"
          >
            <template #right>
              <text class="text-sm text-gray-500">{{ appVersion }}</text>
            </template>
          </u-cell-item>
          <u-cell-item
            title="深色模式"
            :border="true"
            :is-link="true"
            icon="moon"
            :icon-size="40"
            @click="handleDarkMode"
          >
            <template #right>
              <u-switch
                v-model="darkModeEnabled"
                :active-color="'#6b7280'"
                size="40"
              ></u-switch>
            </template>
          </u-cell-item>
          <u-cell-item
            title="字体大小"
            :border="false"
            :is-link="true"
            icon="font"
            :icon-size="40"
            @click="handleFontSize"
          >
            <template #right>
              <text class="text-sm text-gray-500">{{ fontSizeText }}</text>
            </template>
          </u-cell-item>
        </u-cell-group>
      </view>

      <view class="bg-white rounded-xl overflow-hidden mb-[16rpx]">
        <u-cell-group :border="false">
          <u-cell-item
            title="语言设置"
            :border="true"
            :is-link="true"
            icon="translate"
            :icon-size="40"
            @click="handleLanguage"
          >
            <template #right>
              <text class="text-sm text-gray-500">简体中文</text>
            </template>
          </u-cell-item>
          <u-cell-item
            title="关于我们"
            :border="true"
            :is-link="true"
            icon="info-circle"
            :icon-size="40"
            @click="handleAbout"
          ></u-cell-item>
          <u-cell-item
            title="用户协议"
            :border="true"
            :is-link="true"
            icon="file-text"
            :icon-size="40"
            @click="handleAgreement"
          ></u-cell-item>
          <u-cell-item
            title="隐私政策"
            :border="false"
            :is-link="true"
            icon="shield"
            :icon-size="40"
            @click="handlePrivacyPolicy"
          ></u-cell-item>
        </u-cell-group>
      </view>

      <view class="mt-[48rpx]">
        <u-button
          text="退出登录"
          type="error"
          shape="circle"
          size="large"
          @click="handleLogout"
        ></u-button>
      </view>
    </view>

    <u-modal
      v-model="showLogoutModal"
      :title="'提示'"
      :content="'确定要退出登录吗？'"
      :show-cancel-button="true"
      @confirm="confirmLogout"
      @cancel="cancelLogout"
    ></u-modal>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const notificationEnabled = ref(true)
const darkModeEnabled = ref(false)
const showLogoutModal = ref(false)
const cacheSize = ref('128MB')
const appVersion = ref('v1.0.0')
const fontSizeIndex = ref(1)
const fontSizeOptions = ref(['小', '标准', '大', '超大'])

const handleSecurity = () => {
  uni.showModal({
    title: '账号与安全',
    content:
      '【登录密码】\n•••••••••• 已设置\n\n【支付密码】\n•••••••••• 已设置\n\n【手机号】\n138****1234 已绑定\n\n【邮箱】\n未绑定 建议绑定',
    confirmText: '去设置',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '功能开发中',
          icon: 'none',
        })
      }
    },
  })
}

const handlePrivacy = () => {
  uni.showModal({
    title: '隐私设置',
    content:
      '【个性化推荐】\n开启后，我们将根据您的浏览记录推荐商品\n\n【浏览记录】\n记录您的浏览历史，方便再次查看\n\n【订单可见性】\n设置订单对他人可见程度',
    confirmText: '去设置',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '功能开发中',
          icon: 'none',
        })
      }
    },
  })
}

const handleNotification = () => {
  uni.showToast({
    title: notificationEnabled.value ? '已开启通知' : '已关闭通知',
    icon: 'none',
  })
}

const handleClearCache = () => {
  uni.showLoading({
    title: '清理中',
  })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '清理成功',
      icon: 'success',
    })
  }, 1000)
}

const handleCheckUpdate = () => {
  uni.showToast({
    title: '已是最新版本',
    icon: 'none',
  })
}

const handleDarkMode = () => {
  darkModeEnabled.value = !darkModeEnabled.value
  uni.showToast({
    title: darkModeEnabled.value ? '已开启深色模式' : '已关闭深色模式',
    icon: 'none',
  })
}

const fontSizeText = computed(() => {
  return fontSizeOptions.value[fontSizeIndex.value]
})

const handleFontSize = () => {
  uni.showActionSheet({
    itemList: fontSizeOptions.value,
    success: (res) => {
      fontSizeIndex.value = res.tapIndex
      uni.showToast({
        title: `字体大小已设置为${fontSizeOptions.value[res.tapIndex]}`,
        icon: 'none',
      })
    },
  })
}

const handleLanguage = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleAbout = () => {
  uni.showModal({
    title: '关于我们',
    content:
      '某某科技有限公司\n\n版本：v1.0.0\n大小：128MB\n更新：2026-04-19\n\n© 2026 Tea Platform\nAll Rights Reserved.',
    showCancel: false,
  })
}

const handleAgreement = () => {
  uni.showModal({
    title: '用户协议',
    content:
      '欢迎使用某某科技产品！\n\n在开始使用我们的服务之前，请您仔细阅读并同意本用户协议。\n\n【服务条款】\n1. 本平台提供电子商务服务\n2. 用户需年满 18 周岁\n3. 遵守相关法律法规\n\n【用户权利】\n1. 享受购物服务\n2. 参与促销活动\n3. 申请售后服务\n\n【用户义务】\n1. 提供真实信息\n2. 文明购物\n3. 不恶意差评',
    showCancel: false,
    confirmText: '同意',
  })
}

const handlePrivacyPolicy = () => {
  uni.showModal({
    title: '隐私政策',
    content:
      '我们非常重视您的隐私保护！\n\n【信息收集】\n1. 账号信息：手机号、昵称\n2. 设备信息：设备型号、系统版本\n3. 使用信息：浏览记录、订单信息\n\n【信息使用】\n1. 提供商品和服务\n2. 发送订单通知\n3. 改进产品质量\n\n【信息保护】\n1. 严格保密措施\n2. 不向第三方泄露\n3. 符合法律法规',
    showCancel: false,
    confirmText: '知道了',
  })
}

const handleLogout = () => {
  showLogoutModal.value = true
}

const confirmLogout = () => {
  showLogoutModal.value = false
  uni.showToast({
    title: '已退出登录',
    icon: 'success',
  })
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/index/index',
    })
  }, 1500)
}

const cancelLogout = () => {
  showLogoutModal.value = false
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
