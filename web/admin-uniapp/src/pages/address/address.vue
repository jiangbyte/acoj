<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="收货地址" :placeholder="true" :border="true">
      <template #right>
        <view class="px-[32rpx]" @click="handleAdd">
          <u-icon name="plus" size="40" color="#3b82f6"></u-icon>
        </view>
      </template>
    </u-navbar>

    <view class="px-[16rpx] mt-[16rpx]">
      <u-swipe-action
        :show="showSwipe"
        v-for="(item, index) in addressList"
        :key="index"
        :options="swipeOptions"
        @click="handleEdit(item)"
        @close="handleClose"
      >
        <view
          class="bg-white rounded-xl p-4 mb-[16rpx]"
          :class="{ 'border-2 border-blue-500': item.isDefault }"
        >
          <view class="flex items-start justify-between">
            <view class="flex-1">
              <view class="flex items-center gap-[16rpx] mb-[16rpx] flex-wrap">
                <text class="text-base font-semibold text-gray-900">{{
                  item.name
                }}</text>
                <text class="text-base font-semibold text-gray-900">{{
                  item.phone
                }}</text>
                <u-tag
                  v-if="item.isDefault"
                  text="默认"
                  size="mini"
                  type="primary"
                  shape="circle"
                ></u-tag>
                <u-tag
                  v-if="item.tag"
                  :text="item.tag"
                  size="mini"
                  :type="item.tag === '家' ? 'warning' : 'primary'"
                  shape="circle"
                ></u-tag>
              </view>
              <text class="text-sm text-gray-600 leading-relaxed">{{
                item.address
              }}</text>
            </view>
            <view class="flex items-center gap-[16rpx]">
              <u-icon
                name="edit-pen"
                size="36"
                color="#6b7280"
                @click.stop="handleEdit(item)"
              ></u-icon>
              <u-icon
                name="trash"
                size="36"
                color="#ef4444"
                @click.stop="handleDelete(item, index)"
              ></u-icon>
            </view>
          </view>
        </view>
      </u-swipe-action>

      <view
        v-if="addressList.length === 0"
        class="flex flex-col items-center justify-center py-[128rpx]"
      >
        <u-empty mode="address" text="暂无收货地址" :margin-top="80"></u-empty>
      </view>
    </view>

    <u-modal
      v-model="showDeleteModal"
      :title="'提示'"
      :content="'确定要删除该收货地址吗？'"
      :show-cancel-button="true"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    ></u-modal>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showSwipe = ref(false)
const showDeleteModal = ref(false)
const deletingIndex = ref(-1)

const swipeOptions = ref([
  {
    text: '删除',
    style: {
      backgroundColor: '#ef4444',
    },
  },
])

const addressList = ref([
  {
    id: 1,
    name: '张三',
    phone: '138****1234',
    address: '北京市朝阳区三里屯街道 xx 小区 1 号楼 101 室',
    isDefault: true,
    tag: '家',
  },
  {
    id: 2,
    name: '李四',
    phone: '139****5678',
    address: '上海市浦东新区张江路 xx 号 xx 大厦 B 座 2003 室',
    isDefault: false,
    tag: '公司',
  },
  {
    id: 3,
    name: '王五',
    phone: '137****9012',
    address: '广州市天河区天河路 xx 广场 A 座 15 楼 1502 室',
    isDefault: false,
    tag: '',
  },
  {
    id: 4,
    name: '赵六',
    phone: '136****3456',
    address: '深圳市南山区科技园 xx 大厦 8 楼 808 室',
    isDefault: false,
    tag: '公司',
  },
  {
    id: 5,
    name: '孙七',
    phone: '135****7890',
    address: '杭州市西湖区文一路 xx 花园 3 栋 2 单元 301 室',
    isDefault: false,
    tag: '家',
  },
])

const handleAdd = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleEdit = (item: any) => {
  console.log('编辑地址：', item)
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleDelete = (item: any, index: number) => {
  deletingIndex.value = index
  showDeleteModal.value = true
}

const confirmDelete = () => {
  if (deletingIndex.value > -1) {
    addressList.value.splice(deletingIndex.value, 1)
    uni.showToast({
      title: '删除成功',
      icon: 'success',
    })
  }
  showDeleteModal.value = false
  deletingIndex.value = -1
}

const cancelDelete = () => {
  showDeleteModal.value = false
  deletingIndex.value = -1
}

const handleClose = () => {
  showSwipe.value = false
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
