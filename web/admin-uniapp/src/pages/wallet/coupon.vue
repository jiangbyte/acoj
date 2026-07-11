<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="优惠券" :placeholder="true" :border="true"></u-navbar>

    <view class="px-[16rpx] mt-[16rpx]">
      <view class="bg-white rounded-xl p-4 mb-[16rpx]">
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-base font-semibold text-gray-900">可用优惠券</text>
          <text class="text-sm text-blue-500">使用说明</text>
        </view>

        <view
          v-for="(item, index) in couponList"
          :key="index"
          class="mb-[16rpx]"
        >
          <view
            class="rounded-xl overflow-hidden"
            :class="item.bgClass"
            @click="handleUse(item)"
          >
            <view class="flex">
              <view class="flex-1 p-4">
                <view class="flex items-center justify-between mb-[16rpx]">
                  <text class="text-2xl font-bold" :class="item.color">{{
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

      <view class="bg-white rounded-xl p-4">
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-base font-semibold text-gray-900"
            >已使用/已过期</text
          >
          <text class="text-xs text-gray-400">共{{ usedCount }}张</text>
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

const couponList = ref([
  {
    id: 1,
    name: '全场通用券',
    amount: '¥50',
    condition: '满 299 元可用',
    expireDate: '2026-05-01',
    status: '使用',
    bgClass: 'bg-gradient-to-r from-red-50 to-orange-50',
    color: 'text-red-500',
    range: '全平台通用',
  },
  {
    id: 2,
    name: '新人专享券',
    amount: '¥100',
    condition: '满 599 元可用',
    expireDate: '2026-04-30',
    status: '使用',
    bgClass: 'bg-gradient-to-r from-blue-50 to-indigo-50',
    color: 'text-blue-500',
    range: '新人专享',
  },
  {
    id: 3,
    name: '数码品类券',
    amount: '¥20',
    condition: '满 199 元可用',
    expireDate: '2026-04-25',
    status: '使用',
    bgClass: 'bg-gradient-to-r from-green-50 to-emerald-50',
    color: 'text-green-500',
    range: '数码 3C 类目',
  },
  {
    id: 4,
    name: '周末狂欢券',
    amount: '¥80',
    condition: '满 499 元可用',
    expireDate: '2026-04-28',
    status: '使用',
    bgClass: 'bg-gradient-to-r from-purple-50 to-pink-50',
    color: 'text-purple-500',
    range: '全平台通用',
  },
  {
    id: 5,
    name: '运费券',
    amount: '¥10',
    condition: '无门槛',
    expireDate: '2026-05-10',
    status: '使用',
    bgClass: 'bg-gradient-to-r from-teal-50 to-cyan-50',
    color: 'text-teal-500',
    range: '抵扣运费',
  },
])

const usedList = ref([
  {
    id: 101,
    name: '限时秒杀券',
    amount: '¥30',
    condition: '满 299 元可用',
    status: '已使用',
    useTime: '2026-04-10',
  },
  {
    id: 102,
    name: '周末促销券',
    amount: '¥15',
    condition: '满 199 元可用',
    status: '已过期',
    expireDate: '2026-04-05',
  },
  {
    id: 103,
    name: '春节特惠券',
    amount: '¥200',
    condition: '满 999 元可用',
    status: '已使用',
    useTime: '2026-02-10',
  },
])

const usedCount = ref(2)

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
