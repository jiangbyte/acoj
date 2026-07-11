<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="联系我们" :placeholder="true" :border="true"></u-navbar>

    <view class="px-[16rpx] mt-[16rpx]">
      <view class="bg-white rounded-xl p-6 mb-[16rpx]">
        <view class="flex flex-col items-center">
          <image
            src="https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg"
            mode="aspectFill"
            class="w-[160rpx] h-[160rpx] rounded-full mb-[24rpx]"
          ></image>
          <text class="text-xl font-bold text-gray-900">花紫草大先生</text>
          <text class="text-sm text-gray-500 mt-[8rpx]">创始人兼 CEO</text>
        </view>
      </view>

      <view class="bg-white rounded-xl overflow-hidden mb-[16rpx]">
        <u-cell-group :border="false">
          <u-cell-item
            title="公司名称"
            :value="'某某科技有限公司'"
            :border="true"
            :icon="'shop'"
            :icon-size="40"
          ></u-cell-item>
          <u-cell-item
            title="客服电话"
            :value="'400-xxx-xxxx'"
            :border="true"
            :icon="'phone-fill'"
            :icon-size="40"
            :is-link="true"
            @click="handleCall"
          ></u-cell-item>
          <u-cell-item
            title="客服邮箱"
            :value="'support@example.com'"
            :border="true"
            :icon="'email-fill'"
            :icon-size="40"
            :is-link="true"
            @click="handleEmail"
          ></u-cell-item>
          <u-cell-item
            title="公司地址"
            :value="'北京市朝阳区 xx 街道 xx 大厦'"
            :border="false"
            :icon="'map'"
            :icon-size="40"
            :is-link="true"
            @click="handleAddress"
          ></u-cell-item>
        </u-cell-group>
      </view>

      <view class="bg-white rounded-xl p-6 mb-[16rpx]">
        <text class="text-base font-semibold text-gray-900 block mb-[24rpx]"
          >官方微信</text
        >
        <view class="flex justify-center">
          <image
            src="https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=400&h=400&fit=crop"
            mode="aspectFill"
            class="w-[320rpx] h-[320rpx]"
          ></image>
        </view>
        <text class="text-xs text-gray-500 block text-center mt-[16rpx]"
          >扫码关注官方公众号</text
        >
      </view>

      <view class="bg-white rounded-xl p-6">
        <text class="text-base font-semibold text-gray-900 block mb-[24rpx]"
          >社交媒体</text
        >
        <view class="grid grid-cols-3 gap-[24rpx]">
          <view
            class="flex flex-col items-center"
            @click="handleSocialClick('微信')"
          >
            <view
              class="w-[96rpx] h-[96rpx] bg-green-50 rounded-2xl flex items-center justify-center mb-[16rpx]"
            >
              <u-icon name="wechat-fill" size="52" color="#22c55e"></u-icon>
            </view>
            <text class="text-xs text-gray-700">微信</text>
          </view>
          <view
            class="flex flex-col items-center"
            @click="handleSocialClick('微博')"
          >
            <view
              class="w-[96rpx] h-[96rpx] bg-red-50 rounded-2xl flex items-center justify-center mb-[16rpx]"
            >
              <u-icon name="weibo" size="52" color="#ef4444"></u-icon>
            </view>
            <text class="text-xs text-gray-700">微博</text>
          </view>
          <view
            class="flex flex-col items-center"
            @click="handleSocialClick('抖音')"
          >
            <view
              class="w-[96rpx] h-[96rpx] bg-gray-50 rounded-2xl flex items-center justify-center mb-[16rpx]"
            >
              <u-icon name="camera-fill" size="52" color="#000000"></u-icon>
            </view>
            <text class="text-xs text-gray-700">抖音</text>
          </view>
        </view>
      </view>

      <view class="mt-[48rpx] pb-[32rpx]">
        <u-button
          text="在线咨询"
          type="primary"
          shape="circle"
          size="large"
          @click="handleConsult"
        ></u-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const companyInfo = ref({
  name: '某某科技有限公司',
  phone: '400-888-8888',
  email: 'support@teaplatform.com',
  address: '北京市朝阳区三里屯 SOHO 塔 3 座 18 层',
  workTime: '周一至周五 9:00-18:00',
  founded: '2020 年',
  capital: '1000 万元',
})

const handleCall = () => {
  uni.makePhoneCall({
    phoneNumber: companyInfo.value.phone,
    fail: () => {
      uni.showToast({
        title: '拨打失败',
        icon: 'none',
      })
    },
  })
}

const handleEmail = () => {
  uni.setClipboardData({
    data: companyInfo.value.email,
    success: () => {
      uni.showModal({
        title: '邮箱已复制',
        content: `${companyInfo.value.email}\n已复制到剪贴板，您可以在邮件应用中粘贴使用`,
        showCancel: false,
      })
    },
  })
}

const handleAddress = () => {
  uni.showModal({
    title: '公司地址',
    content: companyInfo.value.address,
    showCancel: false,
    confirmText: '去导航',
    success: (res) => {
      if (res.confirm) {
        uni.openLocation({
          latitude: 39.9349,
          longitude: 116.4535,
          name: companyInfo.value.name,
          address: companyInfo.value.address,
        })
      }
    },
  })
}

const handleSocialClick = (platform: string) => {
  const socialInfo: Record<string, string> = {
    微信: 'teaplatform2020',
    微博: '@某某科技',
    抖音: '@某某科技官方',
  }
  uni.showModal({
    title: `${platform}账号`,
    content: socialInfo[platform] || '敬请期待',
    showCancel: false,
  })
}

const handleConsult = () => {
  uni.showModal({
    title: '在线客服',
    content:
      '客服工作时间为工作日 9:00-18:00\n当前为非工作时间，请留言或发送邮件至 support@teaplatform.com',
    showCancel: false,
    confirmText: '知道了',
  })
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
