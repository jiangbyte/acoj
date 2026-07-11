<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="浏览记录" :placeholder="true" :border="true">
      <template #right>
        <view class="px-[32rpx]" @click="handleClear">
          <u-icon name="trash" size="38" color="#ef4444"></u-icon>
        </view>
      </template>
    </u-navbar>

    <view class="px-[16rpx] mt-[16rpx]">
      <view
        v-if="historyList.length > 0"
        class="bg-white rounded-xl p-4 mb-[16rpx]"
      >
        <view class="flex items-center justify-between mb-[24rpx]">
          <text class="text-sm text-gray-500"
            >共{{ historyList.length }}条记录</text
          >
          <text class="text-xs text-blue-500" @click="handleClear"
            >清空记录</text
          >
        </view>
      </view>
      <view
        v-else
        class="flex flex-col items-center justify-center py-[128rpx]"
      >
        <u-empty mode="history" text="暂无浏览记录" :margin-top="80"></u-empty>
      </view>

      <u-waterfall v-model="historyList" ref="uWaterfallRef">
        <template v-slot:left="{ leftList }">
          <view
            v-for="(item, index) in leftList"
            :key="index"
            class="bg-white rounded-2xl overflow-hidden shadow-sm mb-[16rpx] mr-[8rpx]"
            @click="handleItemClick(item)"
          >
            <view class="relative">
              <image
                :src="item.image"
                mode="aspectFill"
                class="w-full h-[320rpx]"
              ></image>
              <view
                v-if="item.tag"
                class="absolute top-[16rpx] left-[16rpx] bg-red-500 text-white text-xs px-[16rpx] py-1 rounded-full"
              >
                {{ item.tag }}
              </view>
            </view>
            <view class="p-3">
              <text
                class="text-sm text-gray-900 line-clamp-2 block mb-[16rpx]"
                >{{ item.name }}</text
              >
              <view class="flex items-center justify-between">
                <view class="flex items-baseline gap-1">
                  <text class="text-red-500 text-lg font-semibold">¥</text>
                  <text class="text-red-500 text-xl font-bold">{{
                    item.price
                  }}</text>
                </view>
                <view
                  class="bg-red-50 text-red-500 text-xs px-[16rpx] py-1 rounded-full"
                >
                  {{ item.date }}
                </view>
              </view>
            </view>
          </view>
        </template>
        <template v-slot:right="{ rightList }">
          <view
            v-for="(item, index) in rightList"
            :key="index"
            class="bg-white rounded-2xl overflow-hidden shadow-sm mb-[16rpx] ml-[8rpx]"
            @click="handleItemClick(item)"
          >
            <view class="relative">
              <image
                :src="item.image"
                mode="aspectFill"
                class="w-full h-[320rpx]"
              ></image>
              <view
                v-if="item.tag"
                class="absolute top-[16rpx] left-[16rpx] bg-red-500 text-white text-xs px-[16rpx] py-1 rounded-full"
              >
                {{ item.tag }}
              </view>
            </view>
            <view class="p-3">
              <text
                class="text-sm text-gray-900 line-clamp-2 block mb-[16rpx]"
                >{{ item.name }}</text
              >
              <view class="flex items-center justify-between">
                <view class="flex items-baseline gap-1">
                  <text class="text-red-500 text-lg font-semibold">¥</text>
                  <text class="text-red-500 text-xl font-bold">{{
                    item.price
                  }}</text>
                </view>
                <view
                  class="bg-red-50 text-red-500 text-xs px-[16rpx] py-1 rounded-full"
                >
                  {{ item.date }}
                </view>
              </view>
            </view>
          </view>
        </template>
      </u-waterfall>
    </view>

    <u-modal
      v-model="showClearModal"
      :title="'提示'"
      :content="'确定要清空所有浏览记录吗？'"
      :show-cancel-button="true"
      @confirm="confirmClear"
      @cancel="cancelClear"
    ></u-modal>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showClearModal = ref(false)
const uWaterfallRef = ref()

const historyList = ref([
  {
    id: 1,
    image:
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop',
    name: '智能手表 运动健康监测 长续航 多功能防水',
    price: '299',
    originalPrice: '599',
    time: '10:30',
    date: '今天',
    tag: '热销',
  },
  {
    id: 2,
    image:
      'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop',
    name: '无线蓝牙耳机 降噪高音质 长续航',
    price: '199',
    originalPrice: '399',
    time: '09:15',
    date: '今天',
    tag: '新品',
  },
  {
    id: 3,
    image:
      'https://images.unsplash.com/photo-1572569028738-411a508d8066?w=400&h=400&fit=crop',
    name: '便携充电宝 20000mAh 快充 双向快充',
    price: '89',
    originalPrice: '159',
    time: '15:20',
    date: '昨天',
    tag: '',
  },
  {
    id: 4,
    image:
      'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=400&h=400&fit=crop',
    name: '机械键盘 青轴背光 游戏办公 无线双模',
    price: '359',
    originalPrice: '599',
    time: '10:30',
    date: '昨天',
    tag: '爆款',
  },
  {
    id: 5,
    image:
      'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&h=400&fit=crop',
    name: '智能手环 心率睡眠监测 运动计步',
    price: '129',
    originalPrice: '229',
    time: '14:15',
    date: '3 天前',
    tag: '热销',
  },
  {
    id: 6,
    image:
      'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=400&h=400&fit=crop',
    name: '笔记本电脑支架 铝合金升降 折叠便携',
    price: '159',
    originalPrice: '259',
    time: '09:00',
    date: '3 天前',
    tag: '',
  },
  {
    id: 7,
    image:
      'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400&h=400&fit=crop',
    name: '智能音箱 语音助手 高保真音质 WiFi 蓝牙',
    price: '399',
    originalPrice: '699',
    time: '16:45',
    date: '上周',
    tag: '新品',
  },
  {
    id: 8,
    image:
      'https://images.unsplash.com/photo-1591337676887-a217a6970a8a?w=400&h=400&fit=crop',
    name: 'iPhone 手机壳 防摔保护套 透明硅胶',
    price: '59',
    originalPrice: '99',
    time: '11:20',
    date: '上周',
    tag: '爆款',
  },
  {
    id: 9,
    image:
      'https://images.unsplash.com/photo-1561154464-82e9adf32764?w=400&h=400&fit=crop',
    name: '无线充电器 快充支架 适用苹果华为',
    price: '79',
    originalPrice: '129',
    time: '13:30',
    date: '上周',
    tag: '',
  },
  {
    id: 10,
    image:
      'https://images.unsplash.com/photo-1585298723682-7115561c51b7?w=400&h=400&fit=crop',
    name: '蓝牙鼠标 静音办公 无线双模 可充电',
    price: '99',
    originalPrice: '169',
    time: '10:00',
    date: '2 周前',
    tag: '热销',
  },
  {
    id: 11,
    image:
      'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400&h=400&fit=crop',
    name: '显示器支架 升降旋转 桌面收纳',
    price: '189',
    originalPrice: '299',
    time: '15:10',
    date: '2 周前',
    tag: '',
  },
  {
    id: 12,
    image:
      'https://images.unsplash.com/photo-1574360742524-2c6b5c5e1d9e?w=400&h=400&fit=crop',
    name: '智能摄像头 高清夜视 远程监控 双向语音',
    price: '149',
    originalPrice: '249',
    time: '09:30',
    date: '2 周前',
    tag: '新品',
  },
])

const handleClear = () => {
  showClearModal.value = true
}

const confirmClear = () => {
  historyList.value = []
  showClearModal.value = false
  uni.showToast({
    title: '已清空',
    icon: 'success',
  })
}

const cancelClear = () => {
  showClearModal.value = false
}

const handleItemClick = (item: any) => {
  console.log('点击商品：', item)
  uni.showToast({
    title: '商品详情开发中',
    icon: 'none',
  })
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
