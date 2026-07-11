<template>
  <view class="min-h-screen">
    <u-navbar
      title="发布"
      :placeholder="true"
      :border="true"
      :custom-back="backToHome"
    >
    </u-navbar>

    <view class="px-[16rpx] bg-white">
      <u-form :model="form" ref="uFormRef" label-width="140">
        <u-form-item>
          <u-textarea
            v-model="content"
            type="textarea"
            placeholder="分享你的想法..."
            :maxlength="500"
            :count="true"
          ></u-textarea>
        </u-form-item>

        <u-form-item label="图片/视频">
          <u-upload
            :fileList="images"
            :maxCount="9"
            :multiple="true"
            @afterRead="afterRead"
            @delete="deleteImage"
          ></u-upload>
        </u-form-item>

        <u-form-item label="位置">
          <view class="flex items-center justify-between w-full">
            <text class="text-[28rpx] text-gray-500">不显示位置</text>
            <u-switch
              v-model="hideLocation"
              :active-color="'#3b82f6'"
            ></u-switch>
          </view>
        </u-form-item>

        <u-form-item label="可见性">
          <picker :range="visibilityOptions" @change="onVisibilityChange">
            <view class="flex items-center">
              <text class="text-[28rpx] text-gray-500">{{
                visibilityOptions[visibilityIndex]
              }}</text>
            </view>
          </picker>
        </u-form-item>
      </u-form>
    </view>

    <view class="mt-[32rpx] px-[16rpx]">
      <u-button
        text="发布"
        type="primary"
        shape="circle"
        size="large"
        @click="handlePublish"
      ></u-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useTabbarStore } from '@/store/TabbarStore'
import CustomTabbar from '@/components/custom-tabbar/custom-tabbar.vue'

const tabbarStore = useTabbarStore()

onShow(() => {
  tabbarStore.setCurrentIndex(1)
})

function backToHome() {
  uni.switchTab({
    url: '/pages/index/index',
  })
}

const uFormRef = ref()
const content = ref('')
const images = ref<any[]>([])
const topic = ref('')
const hideLocation = ref(false)
const visibilityIndex = ref(0)
const visibilityOptions = ref(['公开', '好友可见', '私密'])

const form = ref({
  content: '',
  images: [],
  topic: '',
})

const afterRead = (event: any) => {
  const { file } = event
  images.value = [...images.value, ...file]
}

const deleteImage = (event: any) => {
  const { index } = event
  images.value.splice(index, 1)
}

const onVisibilityChange = (e: any) => {
  visibilityIndex.value = e.detail.value
}

const handlePublish = () => {
  if (!content.value.trim() && images.value.length === 0) {
    uni.showToast({
      title: '请输入内容或添加图片',
      icon: 'none',
    })
    return
  }

  console.log('发布内容：', {
    content: content.value,
    images: images.value,
    topic: topic.value,
    hideLocation: hideLocation.value,
    visibility: visibilityOptions.value[visibilityIndex.value],
  })

  uni.showToast({
    title: '发布成功',
    icon: 'success',
  })

  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
