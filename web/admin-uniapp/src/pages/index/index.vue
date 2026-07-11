<template>
  <view class="min-h-screen py-[16rpx]">
    <u-navbar :is-back="false" title="首页" :placeholder="true" :border="true">
      <template #left>
        <view class="flex items-center gap-2 px-[40rpx]">
          <u-icon name="search" size="38" color="#374151"></u-icon>
          <u-icon name="scan" size="38" color="#374151"></u-icon>
        </view>
      </template>
    </u-navbar>

    <view class="w-full">
      <u-swiper
        :list="swiperList"
        keyName="image"
        :showTitle="false"
        :height="320"
        :loop="true"
        :autoplay="true"
        indicatorMode="dot"
        :current="0"
        :effect3d="true"
        @change="swiperChange"
      ></u-swiper>
    </view>

    <view class="bg-white mt-[16rpx] flex items-center">
      <u-grid :col="4" :border="false">
        <u-grid-item v-for="(item, index) in categoryList" :key="index">
          <view class="h-[56rpx] flex items-center justify-center">
            <u-icon :name="item.icon" size="52" :color="item.color"></u-icon>
          </view>
          <text class="text-xs text-gray-700 mt-[16rpx]">{{ item.name }}</text>
        </u-grid-item>
      </u-grid>
    </view>

    <view class="h-[96rpx] px-[16rpx] flex items-center">
      <u-section
        title="热门推荐"
        sub-title="查看更多"
        :show-line="false"
        :arrow="true"
        @click="handleMoreClick"
        :font-size="32"
      ></u-section>
    </view>

    <view class="px-[16rpx]">
      <u-waterfall v-model="flowList" ref="uWaterfallRef">
        <template v-slot:left="{ leftList }">
          <view
            v-for="(item, index) in leftList"
            :key="index"
            class="bg-white rounded-2xl overflow-hidden shadow-sm mb-[16rpx] mr-[8rpx]"
            @click="handleProductClick(item)"
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
                  已售{{ item.sales }}
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
            @click="handleProductClick(item)"
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
                  已售{{ item.sales }}
                </view>
              </view>
            </view>
          </view>
        </template>
      </u-waterfall>

      <u-loadmore
        :status="loadStatus"
        @loadmore="loadmore"
        bg-color="transparent"
      ></u-loadmore>
    </view>

    <custom-tabbar></custom-tabbar>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { useTabbarStore } from '@/store/TabbarStore'
import CustomTabbar from '@/components/custom-tabbar/custom-tabbar.vue'

const tabbarStore = useTabbarStore()

onShow(() => {
  tabbarStore.setCurrentIndex(0)
})

const swiperList = ref([
  {
    image:
      'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&h=600&fit=crop',
  },
  {
    image:
      'https://images.unsplash.com/photo-1607082349566-187342175e2f?w=800&h=600&fit=crop',
  },
  {
    image:
      'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=800&h=600&fit=crop',
  },
])

const categoryList = ref([
  { name: '限时秒杀', icon: 'clock', color: '#ef4444' },
  { name: '优惠券', icon: 'coupon', color: '#f59e0b' },
  { name: '品牌特卖', icon: 'bag-fill', color: '#8b5cf6' },
  { name: '新品上市', icon: 'star', color: '#10b981' },
  { name: '拼团购', icon: 'account-fill', color: '#ec4899' },
  { name: '会员专区', icon: 'gift', color: '#3b82f6' },
  { name: '签到领币', icon: 'calendar', color: '#14b8a6' },
  { name: '全部分类', icon: 'grid', color: '#6b7280' },
])

const flowList = ref<any[]>([])
const uWaterfallRef = ref()
const loadStatus = ref('loadmore')
const page = ref(1)
const hasMore = ref(true)

const baseProducts = [
  {
    image:
      'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop',
    name: '智能手表 运动健康监测 长续航',
    price: '299',
    sales: '1000+',
    tag: '热销',
  },
  {
    image:
      'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop',
    name: '无线蓝牙耳机 降噪高音质',
    price: '199',
    sales: '2000+',
    tag: '新品',
  },
  {
    image:
      'https://images.unsplash.com/photo-1572569028738-411a508d8066?w=400&h=400&fit=crop',
    name: '便携充电宝 20000mAh 快充',
    price: '89',
    sales: '5000+',
    tag: '',
  },
  {
    image:
      'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=400&h=400&fit=crop',
    name: '机械键盘 青轴背光 游戏办公',
    price: '359',
    sales: '800+',
    tag: '爆款',
  },
  {
    image:
      'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&h=400&fit=crop',
    name: '智能手环 心率睡眠监测',
    price: '129',
    sales: '3000+',
    tag: '热销',
  },
  {
    image:
      'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=400&h=400&fit=crop',
    name: '笔记本电脑支架 铝合金升降',
    price: '159',
    sales: '1500+',
    tag: '',
  },
  {
    image:
      'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400&h=400&fit=crop',
    name: '智能音箱 语音助手 高保真音质',
    price: '399',
    sales: '600+',
    tag: '新品',
  },
  {
    image:
      'https://images.unsplash.com/photo-1591337676887-a217a6970a8a?w=400&h=400&fit=crop',
    name: 'iPhone 手机壳 防摔保护套',
    price: '59',
    sales: '8000+',
    tag: '爆款',
  },
]

const loadProducts = () => {
  if (!hasMore.value) {
    loadStatus.value = 'nomore'
    return
  }

  loadStatus.value = 'loading'

  setTimeout(() => {
    const newProducts = baseProducts.map((item) => ({
      ...item,
      id: `${page.value}-${Math.random()}`,
    }))

    flowList.value = [...flowList.value, ...newProducts]
    page.value++

    if (page.value > 5) {
      hasMore.value = false
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  }, 500)
}

const loadmore = () => {
  loadProducts()
}

const swiperChange = (e: number) => {
  console.log('当前轮播图索引：', e)
}

const handleMoreClick = () => {
  console.log('点击了查看更多')
}

const handleProductClick = (item: any) => {
  uni.navigateTo({
    url: `/pages/product/detail?productId=${item.id}&name=${encodeURIComponent(item.name)}&price=${item.price}&image=${encodeURIComponent(item.image)}`,
  })
}

onReachBottom(() => {
  if (hasMore.value) {
    loadProducts()
  }
})

loadProducts()
</script>

<style lang="scss" scoped></style>
