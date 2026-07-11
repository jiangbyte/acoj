<template>
  <view class="min-h-screen">
    <u-navbar :is-back="false" title="我的" :placeholder="true" :border="true">
    </u-navbar>

    <view
      class="bg-gradient-to-r from-blue-500 to-blue-600 px-[16rpx] pt-[32rpx] pb-[64rpx]"
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
      <view class="flex items-center gap-4 px-4">
        <u-avatar
          :src="userInfo.avatar"
          size="98"
          :border="2"
          border-color="#ffffff"
        ></u-avatar>
        <view class="flex-1">
          <text class="text-white text-xl font-semibold">{{
            userInfo.nickname
          }}</text>
          <view class="flex items-center gap-2 mt-[16rpx]">
            <text class="text-white/80 text-xs">ID: {{ userInfo.id }}</text>
            <u-tag
              text="普通会员"
              size="mini"
              plain
              shape="circle"
              plainFill
            ></u-tag>
          </view>
        </view>
        <u-icon name="edit-pen" color="#ffffff" size="38"></u-icon>
      </view>

      <view
        class="flex items-center justify-around bg-white/20 rounded-2xl mt-6 mx-4 py-4 backdrop-blur-sm"
      >
        <view class="text-center">
          <text class="text-white text-xl font-bold">{{
            userInfo.favoriteCount
          }}</text>
          <text class="text-white/80 text-xs block mt-[8rpx]">收藏</text>
        </view>
        <view class="text-center">
          <text class="text-white text-xl font-bold">{{
            userInfo.followCount
          }}</text>
          <text class="text-white/80 text-xs block mt-[8rpx]">关注</text>
        </view>
        <view class="text-center">
          <text class="text-white text-xl font-bold">{{
            userInfo.fansCount
          }}</text>
          <text class="text-white/80 text-xs block mt-[8rpx]">粉丝</text>
        </view>
      </view>
    </view>

    <view class="px-[16rpx] -mt-[32rpx]">
      <u-grid :col="4" :border="false" hover-class="none">
        <u-grid-item
          v-for="(item, index) in walletMenu"
          :key="index"
          @click="handleWalletClick(item)"
        >
          <view class="h-[42rpx] flex items-center justify-center">
            <u-icon :name="item.icon" size="52" :color="item.color"></u-icon>
          </view>
          <text class="text-xs text-gray-700 mt-[16rpx]">{{ item.name }}</text>
        </u-grid-item>
      </u-grid>
    </view>

    <view class="h-[96rpx] px-[16rpx] flex items-center">
      <u-section
        title="我的服务"
        :show-line="false"
        :right="false"
        :font-size="32"
      ></u-section>
    </view>

    <view class="px-[16rpx]">
      <u-grid :col="4" :border="false" bg-color="#ffffff">
        <u-grid-item
          v-for="(item, index) in serviceMenu"
          :key="index"
          @click="handleWalletClick(item)"
        >
          <view class="h-[42rpx] flex items-center justify-center">
            <u-icon :name="item.icon" size="52" :color="item.color"></u-icon>
          </view>
          <text class="text-xs text-gray-700 mt-[16rpx]">{{ item.name }}</text>
        </u-grid-item>
      </u-grid>
    </view>

    <view class="mt-[16rpx] px-[16rpx]">
      <view class="bg-white">
        <u-cell-group :border="false">
          <u-cell-item
            v-for="(item, index) in menuList"
            :key="index"
            :icon="item.icon"
            :title="item.title"
            :value="item.rightText"
            :icon-size="42"
            :border="index !== menuList.length - 1"
            @click="handleMenuClick(item)"
          >
          </u-cell-item>
        </u-cell-group>
      </view>
    </view>

    <view class="mt-[42rpx] pb-[32rpx]">
      <text class="text-center text-xs text-gray-400 block leading-6">
        © 2026 Tea Platform All Rights Reserved.
      </text>
      <text class="text-center text-xs text-gray-400 block mt-[8rpx]">
        Built by Charlie
      </text>
    </view>

    <custom-tabbar></custom-tabbar>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useTabbarStore } from '@/store/TabbarStore'
import CustomTabbar from '@/components/custom-tabbar/custom-tabbar.vue'

const tabbarStore = useTabbarStore()

onShow(() => {
  tabbarStore.setCurrentIndex(2)
})

const userInfo = ref({
  avatar:
    'https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg',
  nickname: '花紫草大先生',
  id: '214602320',
  favoriteCount: 346,
  followCount: 12,
  fansCount: 128,
})

const walletMenu = ref([
  {
    name: '余额',
    icon: 'rmb-circle',
    color: '#10b981',
    bgClass: 'bg-green-50',
  },
  { name: '优惠券', icon: 'coupon', color: '#f59e0b', bgClass: 'bg-yellow-50' },
  { name: '积分', icon: 'integral', color: '#8b5cf6', bgClass: 'bg-purple-50' },
  { name: '红包', icon: 'red-packet', color: '#ef4444', bgClass: 'bg-red-50' },
])

const serviceMenu = ref([
  { name: '客服', icon: 'account', color: '#3b82f6' },
  { name: '反馈', icon: 'edit-pen', color: '#10b981' },
  { name: '分享', icon: 'share', color: '#f59e0b' },
  { name: '关于', icon: 'info-circle', color: '#8b5cf6' },
])

const menuList = ref([
  {
    key: 'login',
    title: '登录页面（测试）',
    icon: 'account',
    rightText: '',
    badge: false,
  },
  {
    key: 'register',
    title: '注册页面（测试）',
    icon: 'account-fill',
    rightText: '',
    badge: false,
  },
  {
    key: 'address',
    title: '收货地址',
    icon: 'map',
    rightText: '',
    badge: false,
  },
  {
    key: 'history',
    title: '浏览记录',
    icon: 'eye',
    rightText: '',
    badge: false,
  },
  {
    key: 'help',
    title: '帮助中心',
    icon: 'question-circle',
    rightText: '',
    badge: false,
  },
  {
    key: 'contact',
    title: '联系我们',
    icon: 'phone',
    rightText: '',
    badge: true,
  },
  {
    key: 'setting',
    title: '设置',
    icon: 'setting',
    rightText: '',
    badge: false,
  },
])

const handleWalletClick = (item: any) => {
  const walletPages: Record<string, string> = {
    余额: '/pages/wallet/balance',
    优惠券: '/pages/wallet/coupon',
    积分: '/pages/wallet/points',
    红包: '/pages/wallet/redpacket',
  }
  const page = walletPages[item.name]
  if (page) {
    uni.navigateTo({
      url: page,
    })
  }
}

const handleServiceClick = (item: any) => {
  const servicePages: Record<string, string> = {
    客服: '/pages/contact/contact',
    反馈: '/pages/help/help',
    分享: '',
    关于: '/pages/setting/setting',
  }
  const page = servicePages[item.name]
  if (page) {
    uni.navigateTo({
      url: page,
    })
  } else {
    uni.showToast({
      title: '功能开发中',
      icon: 'none',
    })
  }
}

const handleMenuClick = (item: any) => {
  const menuPages: Record<string, string> = {
    login: '/pages/auth/login/login',
    register: '/pages/auth/register/register',
    address: '/pages/address/address',
    history: '/pages/history/history',
    help: '/pages/help/help',
    contact: '/pages/contact/contact',
    setting: '/pages/setting/setting',
  }
  const page = menuPages[item.key]
  if (page) {
    uni.navigateTo({
      url: page,
    })
  } else {
    console.log('点击菜单：', item.title)
  }
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
