<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="我的积分" :placeholder="true" :border="true">
      <template #right>
        <view class="px-[32rpx]" @click="handleRule">
          <text class="text-sm text-blue-500">规则</text>
        </view>
      </template>
    </u-navbar>

    <view
      class="bg-gradient-to-b from-purple-500 to-purple-600 px-[32rpx] pt-[64rpx] pb-[80rpx]"
    >
      <view class="flex items-center justify-between mb-[32rpx]">
        <view>
          <text class="text-white/80 text-sm block mb-[16rpx]">可用积分</text>
          <text class="text-white text-[64rpx] font-bold">2,580</text>
        </view>
        <view
          class="w-[160rpx] h-[160rpx] bg-white/20 rounded-full flex items-center justify-center"
        >
          <u-icon name="star" size="80" color="#ffffff"></u-icon>
        </view>
      </view>
      <view class="flex items-center gap-[24rpx] mt-[32rpx]">
        <view
          class="flex-1 bg-white/20 backdrop-blur-sm rounded-xl py-4 px-6"
          @click="handleEarn"
        >
          <text class="text-white text-base font-semibold block">赚积分</text>
        </view>
        <view
          class="flex-1 bg-white/20 backdrop-blur-sm rounded-xl py-4 px-6"
          @click="handleExchange"
        >
          <text class="text-white text-base font-semibold block">兑积分</text>
        </view>
      </view>
    </view>

    <view class="px-[16rpx] -mt-[40rpx]">
      <view class="bg-white rounded-xl p-4 mb-[16rpx]">
        <text class="text-base font-semibold text-gray-900 block mb-[24rpx]"
          >积分任务</text
        >
        <view v-for="(item, index) in tasks" :key="index">
          <view
            class="flex items-center justify-between py-3"
            :class="{ 'border-b border-gray-100': index !== tasks.length - 1 }"
          >
            <view class="flex items-center gap-[24rpx]">
              <view
                class="w-[80rpx] h-[80rpx] rounded-xl flex items-center justify-center bg-purple-50"
              >
                <u-icon :name="item.icon" size="36" color="#8b5cf6"></u-icon>
              </view>
              <view class="flex-1">
                <text class="text-sm text-gray-900 block">{{
                  item.title
                }}</text>
                <text class="text-xs text-gray-400"
                  >+{{ item.points }}积分</text
                >
              </view>
            </view>
            <u-button
              :text="item.status"
              :type="item.status === '去完成' ? 'primary' : 'default'"
              shape="circle"
              size="small"
              :disabled="item.status !== '去完成'"
            ></u-button>
          </view>
        </view>
      </view>

      <view class="bg-white rounded-xl p-4">
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-base font-semibold text-gray-900">积分记录</text>
          <text class="text-xs text-blue-500" @click="handleMore"
            >查看更多</text
          >
        </view>

        <view v-for="(item, index) in records" :key="index">
          <view
            class="flex items-center justify-between py-3"
            :class="{
              'border-b border-gray-100': index !== records.length - 1,
            }"
          >
            <view>
              <text class="text-sm text-gray-900 block">{{ item.title }}</text>
              <text class="text-xs text-gray-400">{{ item.time }}</text>
            </view>
            <text class="text-base font-semibold text-purple-500"
              >+{{ item.points }}</text
            >
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tasks = ref([
  {
    id: 1,
    icon: 'sign',
    title: '每日签到',
    desc: '每日签到领积分',
    points: 10,
    status: '去完成',
  },
  {
    id: 2,
    icon: 'cart',
    title: '购物消费',
    desc: '每消费 1 元得 1 积分',
    points: 100,
    status: '已完成',
  },
  {
    id: 3,
    icon: 'share',
    title: '分享商品',
    desc: '分享给好友得积分',
    points: 5,
    status: '去完成',
  },
  {
    id: 4,
    icon: 'edit-pen',
    title: '发表评价',
    desc: '评价商品得积分',
    points: 20,
    status: '去完成',
  },
  {
    id: 5,
    icon: 'clock',
    title: '限时任务',
    desc: '完成指定商品购买',
    points: 50,
    status: '去完成',
  },
  {
    id: 6,
    icon: 'gift',
    title: '邀请好友',
    desc: '邀请好友注册得积分',
    points: 100,
    status: '去完成',
  },
])

const records = ref([
  {
    id: 1,
    title: '每日签到',
    time: '2026-04-18 09:00',
    points: 10,
    type: 'income',
  },
  {
    id: 2,
    title: '购物奖励 - 智能手表',
    time: '2026-04-17 15:30',
    points: 299,
    type: 'income',
  },
  {
    id: 3,
    title: '评价奖励',
    time: '2026-04-16 10:20',
    points: 20,
    type: 'income',
  },
  {
    id: 4,
    title: '积分兑换优惠券',
    time: '2026-04-15 14:00',
    points: -100,
    type: 'expense',
  },
  {
    id: 5,
    title: '分享商品奖励',
    time: '2026-04-14 11:30',
    points: 15,
    type: 'income',
  },
  {
    id: 6,
    title: '积分兑换礼品',
    time: '2026-04-13 16:45',
    points: -500,
    type: 'expense',
  },
  {
    id: 7,
    title: '新人奖励',
    time: '2026-04-10 08:00',
    points: 200,
    type: 'income',
  },
])

const handleRule = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleEarn = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleExchange = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleMore = () => {
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
