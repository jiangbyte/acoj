<template>
  <view class="min-h-screen bg-gray-50">
    <u-navbar title="帮助中心" :placeholder="true" :border="true"></u-navbar>

    <view class="px-[16rpx] mt-[16rpx]">
      <view class="bg-white rounded-xl overflow-hidden">
        <u-cell-group :border="false">
          <u-cell-item
            v-for="(item, index) in helpList"
            :key="index"
            :title="item.title"
            :label="item.desc"
            :border="index !== helpList.length - 1"
            :is-link="true"
            @click="handleItemClick(item)"
          >
            <template #icon>
              <view class="flex items-center gap-[16rpx]">
                <u-icon
                  :name="item.icon"
                  size="40"
                  :color="item.color"
                ></u-icon>
              </view>
            </template>
          </u-cell-item>
        </u-cell-group>
      </view>

      <view class="mt-[32rpx] bg-white rounded-xl p-4">
        <view class="flex items-center justify-center gap-[16rpx]">
          <u-icon name="service" size="48" color="#3b82f6"></u-icon>
          <text class="text-base font-semibold text-gray-900">在线客服</text>
        </view>
        <text class="text-sm text-gray-500 block text-center mt-[16rpx]"
          >工作日 9:00-18:00</text
        >
        <u-button
          text="联系客服"
          type="primary"
          shape="circle"
          size="large"
          class="mt-[32rpx]"
          @click="handleContact"
        ></u-button>
      </view>

      <view class="mt-[32rpx] bg-white rounded-xl p-4">
        <view class="flex items-center justify-center gap-[16rpx]">
          <u-icon name="phone-fill" size="48" color="#10b981"></u-icon>
          <text class="text-base font-semibold text-gray-900">电话咨询</text>
        </view>
        <text class="text-sm text-gray-500 block text-center mt-[16rpx]"
          >400-xxx-xxxx</text
        >
        <u-button
          text="拨打电话"
          type="success"
          shape="circle"
          size="large"
          class="mt-[32rpx]"
          @click="handleCall"
        ></u-button>
      </view>
    </view>

    <u-modal
      v-model="showDetailModal"
      :title="selectedItem?.title"
      :content="selectedItem?.content"
      :show-cancel-button="false"
      @confirm="showDetailModal = false"
    ></u-modal>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showDetailModal = ref(false)
const selectedItem = ref<any>(null)

const helpList = ref([
  {
    icon: 'question-circle',
    color: '#3b82f6',
    title: '新手指南',
    desc: '注册登录、购物流程、新手福利',
    content:
      '【注册账号】\n1. 点击首页右下角"我的"\n2. 选择登录/注册\n3. 填写手机号和验证码完成注册\n\n【购物流程】\n1. 在首页浏览商品，可通过分类筛选\n2. 点击商品查看详情，选择规格\n3. 加入购物车或直接购买\n4. 填写收货地址，提交订单\n5. 选择支付方式完成支付\n\n【新手福利】\n- 新人注册送 100 元优惠券包\n- 首单立减 10 元\n- 新人专享价商品',
  },
  {
    icon: 'shopping-cart',
    color: '#f59e0b',
    title: '订单问题',
    desc: '订单查询、取消订单、订单状态',
    content:
      '【订单查询】\n1. 在"我的"页面点击"我的订单"\n2. 可按状态筛选：全部/待付款/待发货/待收货/已完成\n3. 点击订单查看详情\n\n【取消订单】\n1. 待支付订单：可直接点击"取消订单"\n2. 已支付订单：需联系客服处理\n3. 已发货订单：需拒收或申请退货\n\n【订单状态说明】\n- 待支付：订单待付款，30 分钟后自动取消\n- 待发货：已付款，等待商家发货\n- 待收货：已发货，等待确认收货\n- 已完成：交易完成，可评价\n- 已取消：订单已取消',
  },
  {
    icon: 'truck',
    color: '#10b981',
    title: '物流配送',
    desc: '发货时间、物流查询、配送范围',
    content:
      '【发货时间】\n1. 工作日 16:00 前付款，当天发货\n2. 工作日 16:00 后付款，次日发货\n3. 节假日顺延\n\n【物流查询】\n1. 在订单详情页查看物流信息\n2. 实时跟踪物流状态\n3. 支持主流快递公司\n\n【配送范围】\n1. 全国包邮 (新疆、西藏、港澳台除外)\n2. 部分偏远地区需补运费差价\n\n【配送时间】\n1. 一般城市：3-5 个工作日\n2. 偏远地区：5-7 个工作日\n3. 特殊情况 (天气、节假日) 可能延迟',
  },
  {
    icon: 'undo',
    color: '#ef4444',
    title: '退换货政策',
    desc: '7 天无理由、退换流程、运费说明',
    content:
      '【7 天无理由退换】\n1. 自签收之日起 7 天内\n2. 商品未使用，包装完好\n3. 不影响二次销售\n\n【退换流程】\n1. 在"我的订单"申请售后\n2. 选择退换类型和原因\n3. 上传凭证图片\n4. 等待商家审核\n5. 审核通过后寄回商品\n6. 仓库验收后退款/换货\n\n【运费说明】\n- 质量问题：我方承担往返运费\n- 非质量问题：买家承担寄回运费\n- 换货：我方承担重新发货运费\n\n【退款时间】\n- 原路退回，1-3 个工作日到账',
  },
  {
    icon: 'wallet',
    color: '#8b5cf6',
    title: '支付问题',
    desc: '支付方式、退款、支付安全',
    content:
      '【支付方式】\n1. 微信支付\n2. 支付宝\n3. 银行卡/信用卡\n4. 余额支付\n5. 组合支付 (余额 + 微信/支付宝)\n\n【退款说明】\n1. 原路退回原则\n2. 退款时间：1-3 个工作日\n3. 退款状态可在订单详情查看\n\n【支付安全】\n1. 采用 SSL 加密技术\n2. 符合 PCI DSS 安全标准\n3. 不支持货到付款\n\n【支付失败处理】\n1. 检查网络连接\n2. 确认支付密码正确\n3. 检查银行卡余额\n4. 更换支付方式',
  },
  {
    icon: 'account',
    color: '#ec4899',
    title: '账户管理',
    desc: '修改资料、密码、账户安全',
    content:
      '【修改资料】\n1. "我的"页面 → 右上角编辑\n2. 可修改：昵称、头像、性别\n3. 手机号需验证后修改\n\n【密码找回】\n1. 登录页点击"忘记密码"\n2. 输入注册手机号\n3. 输入验证码\n4. 设置新密码\n\n【账户安全】\n1. 建议绑定手机号\n2. 建议绑定邮箱\n3. 定期修改密码\n4. 开启登录保护\n\n【注销账号】\n1. 联系客服申请\n2. 需满足注销条件\n3. 注销后数据不可恢复',
  },
  {
    icon: 'gift',
    color: '#14b8a6',
    title: '会员制度',
    desc: '会员等级、权益、升级规则',
    content:
      '【会员等级】\n1. 普通会员：注册即享\n2. VIP 会员：累计消费满 500 元\n3. SVIP 会员：累计消费满 2000 元\n\n【会员权益】\n- 普通会员：积分奖励\n- VIP 会员：95 折、专属客服、生日礼包\n- SVIP 会员：9 折、免费试用、专属活动\n\n【升级规则】\n1. 按累计消费金额升级\n2. 有效期 12 个月\n3. 到期后根据消费保级或降级\n\n【积分规则】\n1. 1 元=1 积分\n2. 积分可兑换优惠券、礼品\n3. 积分有效期 12 个月',
  },
])

const handleItemClick = (item: any) => {
  selectedItem.value = item
  showDetailModal.value = true
}

const handleContact = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}

const handleCall = () => {
  uni.makePhoneCall({
    phoneNumber: '400xxxxxxx',
  })
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f9fafb;
}
</style>
