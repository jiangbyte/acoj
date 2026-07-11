<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="我的余额" :placeholder="true" :border="true">
      <template #right>
        <view class="px-[32rpx]" @click="handleRecord">
          <text class="text-sm text-blue-500">明细</text>
        </view>
      </template>
    </u-navbar>

    <view
      class="bg-gradient-to-b from-blue-500 to-blue-600 px-[32rpx] pt-[64rpx] pb-[80rpx]"
    >
      <text class="text-white/80 text-sm block mb-[16rpx]">可用余额 (元)</text>
      <text class="text-white text-[64rpx] font-bold">1,256.00</text>
      <view class="flex items-center gap-[24rpx] mt-[32rpx]">
        <view
          class="flex-1 bg-white/20 backdrop-blur-sm rounded-xl py-4 px-6"
          @click="handleRecharge"
        >
          <text class="text-white text-base font-semibold block">充值</text>
        </view>
        <view
          class="flex-1 bg-white/20 backdrop-blur-sm rounded-xl py-4 px-6"
          @click="handleWithdraw"
        >
          <text class="text-white text-base font-semibold block">提现</text>
        </view>
      </view>
    </view>

    <view class="px-[16rpx] -mt-[40rpx]">
      <view class="bg-white rounded-xl p-4">
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-base font-semibold text-gray-900">最近记录</text>
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
            <view class="flex items-center gap-[24rpx]">
              <view
                class="w-[80rpx] h-[80rpx] rounded-xl flex items-center justify-center"
                :class="item.type === 'income' ? 'bg-green-50' : 'bg-red-50'"
              >
                <u-icon
                  :name="item.type === 'income' ? 'arrow-down' : 'arrow-up'"
                  size="36"
                  :color="item.type === 'income' ? '#10b981' : '#ef4444'"
                ></u-icon>
              </view>
              <view>
                <text class="text-sm text-gray-900 block">{{
                  item.title
                }}</text>
                <text class="text-xs text-gray-400">{{ item.time }}</text>
              </view>
            </view>
            <text
              class="text-base font-semibold"
              :class="
                item.type === 'income' ? 'text-green-500' : 'text-red-500'
              "
            >
              {{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const records = ref([
  {
    id: 1,
    title: '余额充值',
    time: '2026-04-18 15:30',
    amount: '500.00',
    type: 'income',
    icon: 'arrow-down',
    status: '成功',
  },
  {
    id: 2,
    title: '购物消费 - 智能手表',
    time: '2026-04-17 10:20',
    amount: '299.00',
    type: 'expense',
    icon: 'arrow-up',
    status: '成功',
  },
  {
    id: 3,
    title: '退款入账 - 订单取消',
    time: '2026-04-16 14:15',
    amount: '89.00',
    type: 'income',
    icon: 'arrow-down',
    status: '成功',
  },
  {
    id: 4,
    title: '提现到银行卡',
    time: '2026-04-15 09:00',
    amount: '200.00',
    type: 'expense',
    icon: 'arrow-up',
    status: '成功',
  },
  {
    id: 5,
    title: '余额充值',
    time: '2026-04-14 16:45',
    amount: '1000.00',
    type: 'income',
    icon: 'arrow-down',
    status: '成功',
  },
  {
    id: 6,
    title: '购物消费 - 蓝牙耳机',
    time: '2026-04-13 11:30',
    amount: '199.00',
    type: 'expense',
    icon: 'arrow-up',
    status: '成功',
  },
  {
    id: 7,
    title: '签到奖励',
    time: '2026-04-12 08:00',
    amount: '5.00',
    type: 'income',
    icon: 'arrow-down',
    status: '成功',
  },
  {
    id: 8,
    title: '优惠券抵扣',
    time: '2026-04-11 14:20',
    amount: '50.00',
    type: 'expense',
    icon: 'arrow-up',
    status: '成功',
  },
])

const handleRecharge = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleWithdraw = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleRecord = () => {
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
