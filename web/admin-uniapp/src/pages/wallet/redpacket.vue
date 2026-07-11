<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="我的红包" :placeholder="true" :border="true"></u-navbar>

    <view
      class="bg-gradient-to-b from-red-500 to-red-600 px-[32rpx] pt-[64rpx] pb-[80rpx]"
    >
      <view class="flex items-center justify-between mb-[32rpx]">
        <view>
          <text class="text-white/80 text-sm block mb-[16rpx]">可用红包</text>
          <text class="text-white text-[64rpx] font-bold">3</text>
        </view>
        <view
          class="w-[160rpx] h-[160rpx] bg-white/20 rounded-full flex items-center justify-center"
        >
          <u-icon name="red-packet" size="80" color="#ffffff"></u-icon>
        </view>
      </view>
      <view class="flex items-center gap-[24rpx] mt-[32rpx]">
        <view
          class="flex-1 bg-white/20 backdrop-blur-sm rounded-xl py-4 px-6"
          @click="handleReceive"
        >
          <text class="text-white text-base font-semibold block">领红包</text>
        </view>
        <view
          class="flex-1 bg-white/20 backdrop-blur-sm rounded-xl py-4 px-6"
          @click="handleSend"
        >
          <text class="text-white text-base font-semibold block">发红包</text>
        </view>
      </view>
    </view>

    <view class="px-[16rpx] -mt-[40rpx]">
      <view class="bg-white rounded-xl p-4">
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-base font-semibold text-gray-900">可用红包</text>
          <text class="text-xs text-gray-400">共{{ availableCount }}个</text>
        </view>

        <view
          v-for="(item, index) in redPacketList"
          :key="index"
          class="mb-[16rpx]"
        >
          <view
            class="rounded-xl overflow-hidden bg-gradient-to-r from-red-50 to-orange-50 border-2 border-red-100"
            @click="handleUse(item)"
          >
            <view class="flex">
              <view class="flex-1 p-4">
                <view class="flex items-center justify-between mb-[16rpx]">
                  <text class="text-2xl font-bold text-red-500">{{
                    item.amount
                  }}</text>
                  <text class="text-xs text-gray-500">{{
                    item.condition
                  }}</text>
                </view>
                <text class="text-sm text-gray-700 block mb-[8rpx]">{{
                  item.name
                }}</text>
                <text class="text-xs text-gray-400"
                  >有效期至{{ item.expireDate }}</text
                >
              </view>
              <view class="w-[160rpx] flex items-center justify-center">
                <u-button
                  :text="item.status"
                  :type="item.status === '使用' ? 'primary' : 'default'"
                  shape="circle"
                  size="small"
                  :disabled="item.status !== '使用'"
                ></u-button>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="bg-white rounded-xl p-4 mt-[16rpx]">
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-base font-semibold text-gray-900">已使用红包</text>
          <text class="text-xs text-gray-400">共{{ usedCount }}个</text>
        </view>

        <view v-for="(item, index) in usedList" :key="index" class="mb-[16rpx]">
          <view class="rounded-xl overflow-hidden bg-gray-100 opacity-60">
            <view class="flex">
              <view class="flex-1 p-4">
                <view class="flex items-center justify-between mb-[16rpx]">
                  <text class="text-2xl font-bold text-gray-400">{{
                    item.amount
                  }}</text>
                  <text class="text-xs text-gray-500">{{
                    item.condition
                  }}</text>
                </view>
                <text class="text-sm text-gray-500 block mb-[8rpx]">{{
                  item.name
                }}</text>
                <text class="text-xs text-gray-400">{{ item.status }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const availableCount = ref(3)
const usedCount = ref(2)

const redPacketList = ref([
  {
    name: '新人红包',
    amount: '¥10',
    condition: '无门槛',
    expireDate: '2026-05-01',
    status: '使用',
  },
  {
    name: '节日红包',
    amount: '¥5',
    condition: '满 99 元可用',
    expireDate: '2026-04-30',
    status: '使用',
  },
  {
    name: '签到红包',
    amount: '¥3',
    condition: '无门槛',
    expireDate: '2026-04-25',
    status: '使用',
  },
])

const usedList = ref([
  {
    name: '限时红包',
    amount: '¥8',
    condition: '满 59 元可用',
    status: '已使用',
  },
  {
    name: '活动红包',
    amount: '¥6',
    condition: '满 39 元可用',
    status: '已过期',
  },
])

const handleReceive = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleSend = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleUse = (item: any) => {
  if (item.status === '使用') {
    uni.showToast({
      title: '功能开发中',
      icon: 'none',
    })
  }
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
