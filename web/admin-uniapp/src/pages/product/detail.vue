<template>
  <view class="min-h-screen bg-gray-50 pb-[160rpx]">
    <u-navbar title="商品详情" :placeholder="true" :border="true">
      <template #right>
        <view class="px-[32rpx]">
          <u-icon name="share" size="40" color="#374151"></u-icon>
        </view>
      </template>
    </u-navbar>

    <view class="bg-white">
      <image
        :src="product.image"
        mode="aspectFill"
        class="w-full h-[750rpx]"
      ></image>
    </view>

    <view class="bg-white mt-[16rpx] p-4">
      <view class="flex items-baseline gap-[16rpx] mb-[16rpx]">
        <text class="text-red-500 text-lg font-semibold">¥</text>
        <text class="text-red-500 text-[48rpx] font-bold">{{
          product.price
        }}</text>
        <text class="text-gray-400 text-sm line-through"
          >¥{{ product.originalPrice }}</text
        >
      </view>
      <text class="text-base text-gray-900 block mb-[16rpx]">{{
        product.name
      }}</text>
      <view class="flex items-center gap-[16rpx]">
        <u-tag text="热销" type="error" size="mini" shape="circle"></u-tag>
        <text class="text-xs text-gray-500">已售{{ product.sales }}</text>
      </view>
    </view>

    <view class="bg-white mt-[16rpx] p-4">
      <view class="flex items-center justify-between mb-[24rpx]">
        <text class="text-base font-semibold text-gray-900">选择规格</text>
        <u-icon name="arrow-right" size="32" color="#9ca3af"></u-icon>
      </view>
      <view class="flex flex-wrap gap-[16rpx]">
        <u-tag
          v-for="(spec, index) in specs"
          :key="index"
          :text="spec"
          :type="selectedSpec === spec ? 'primary' : 'default'"
          plain
          shape="circle"
          @click="selectSpec(spec)"
        ></u-tag>
      </view>
    </view>

    <view class="bg-white mt-[16rpx] p-4">
      <text class="text-base font-semibold text-gray-900 block mb-[16rpx]"
        >商品详情</text
      >
      <view class="text-sm text-gray-600 leading-relaxed">
        <text>{{ product.description }}</text>
      </view>
    </view>

    <view class="bg-white mt-[16rpx] p-4">
      <view class="flex items-center justify-between mb-[24rpx]">
        <text class="text-base font-semibold text-gray-900">用户评价</text>
        <view class="flex items-center gap-[8rpx]">
          <text class="text-xs text-gray-500">好评率 98%</text>
          <u-icon name="arrow-right" size="32" color="#9ca3af"></u-icon>
        </view>
      </view>
      <view v-for="(item, index) in reviews" :key="index">
        <view
          class="flex items-start gap-[24rpx] py-3"
          :class="{ 'border-b border-gray-100': index !== reviews.length - 1 }"
        >
          <u-avatar :src="item.avatar" size="60"></u-avatar>
          <view class="flex-1">
            <text class="text-sm text-gray-900 block mb-[8rpx]">{{
              item.username
            }}</text>
            <u-rate
              :value="item.rating"
              :count="5"
              size="28"
              :readonly="true"
            ></u-rate>
            <text class="text-sm text-gray-600 block mt-[16rpx]">{{
              item.content
            }}</text>
            <text class="text-xs text-gray-400 block mt-[8rpx]">{{
              item.time
            }}</text>
          </view>
        </view>
      </view>
    </view>

    <view
      class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 px-[32rpx] py-[24rpx] flex items-center justify-between"
    >
      <view class="flex items-center gap-[48rpx]">
        <view class="flex flex-col items-center" @click="handleFavorite">
          <u-icon
            :name="isFavorite ? 'heart-fill' : 'heart'"
            size="44"
            :color="isFavorite ? '#ef4444' : '#9ca3af'"
          ></u-icon>
          <text class="text-xs text-gray-500 mt-[4rpx]">收藏</text>
        </view>
        <view class="flex flex-col items-center" @click="handleCart">
          <u-icon name="cart" size="44" color="#9ca3af"></u-icon>
          <text class="text-xs text-gray-500 mt-[4rpx]">购物车</text>
        </view>
      </view>
      <view class="flex items-center gap-[24rpx]">
        <u-button
          text="加入购物车"
          type="default"
          shape="circle"
          size="large"
          @click="handleCart"
        ></u-button>
        <u-button
          text="立即购买"
          type="primary"
          shape="circle"
          size="large"
          @click="handleBuyNow"
        ></u-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isFavorite = ref(false)
const selectedSpec = ref('标准版')

const product = ref({
  image:
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop',
  name: '智能手表 运动健康监测 长续航 多功能防水',
  price: '299',
  originalPrice: '599',
  sales: '1000+',
  description:
    '这是一款高性能智能手表，支持心率监测、血氧检测、睡眠监测等多种健康功能。采用 AMOLED 高清屏幕，续航可达 14 天。支持 50 米防水，适合游泳佩戴。内置 GPS 定位，运动轨迹精准记录。',
})

const specs = ref([
  { name: '标准版', price: '299', stock: 100, desc: '基础功能齐全' },
  { name: '豪华版', price: '399', stock: 50, desc: '新增 NFC 支付' },
  { name: '旗舰版', price: '499', stock: 20, desc: 'eSIM 独立通话' },
])

const reviews = ref([
  {
    id: 1,
    avatar:
      'https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg',
    username: '张***3',
    rating: 5,
    content:
      '手表很好用，功能齐全，续航能力强，性价比很高！心率监测很准确，睡眠监测也很详细。',
    time: '2026-04-15',
    spec: '标准版',
    images: [
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&h=200&fit=crop',
    ],
    useful: 156,
  },
  {
    id: 2,
    avatar:
      'https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg',
    username: '李***8',
    rating: 5,
    content: '外观漂亮，做工精细，监测数据准确，值得购买！物流很快，包装完好。',
    time: '2026-04-12',
    spec: '豪华版',
    images: [],
    useful: 89,
  },
  {
    id: 3,
    avatar:
      'https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg',
    username: '王***5',
    rating: 4,
    content:
      '整体不错，就是表带有点硬，需要适应一下。功能都很实用，特别是运动模式很多。',
    time: '2026-04-10',
    spec: '标准版',
    images: [
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&h=200&fit=crop',
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&h=200&fit=crop',
    ],
    useful: 45,
  },
  {
    id: 4,
    avatar:
      'https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg',
    username: '刘***6',
    rating: 5,
    content: '非常满意的一次购物！手表质量很好，功能强大，客服态度也很好。',
    time: '2026-04-08',
    spec: '旗舰版',
    images: [],
    useful: 67,
  },
  {
    id: 5,
    avatar:
      'https://www.keaitupian.cn/cjpic/frombd/0/253/3730063558/1625161022.jpg',
    username: '陈***9',
    rating: 5,
    content:
      '第二次购买了，上次买的自己用，这次买来送朋友。朋友很喜欢，说功能很实用。',
    time: '2026-04-05',
    spec: '豪华版',
    images: [
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&h=200&fit=crop',
    ],
    useful: 123,
  },
])

const selectSpec = (spec: string) => {
  selectedSpec.value = spec
}

const handleFavorite = () => {
  isFavorite.value = !isFavorite.value
  uni.showToast({
    title: isFavorite.value ? '已收藏' : '已取消收藏',
    icon: 'none',
  })
}

const handleCart = () => {
  uni.showToast({
    title: '已加入购物车',
    icon: 'success',
  })
}

const handleBuyNow = () => {
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
