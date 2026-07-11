<template>
  <Layout title="个人资料" back>
    <view>
      <u-card :show-head="false">
        <template #body>
          <FormFields v-model="form" :fields="fields" mode="update" />
        </template>
        <template #foot>
          <u-button
            text="保存资料"
            type="primary"
            :loading="loading"
            @click="save"
          ></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import FormFields from '@/components/common/FormFields.vue'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { FieldConfig } from '@/config/resource'

const authStore = useAuthStore()
const loading = ref(false)
const form = ref<Record<string, any>>({})
const fields: FieldConfig[] = [
  { prop: 'name', label: '姓名' },
  { prop: 'nickname', label: '昵称' },
  { prop: 'avatar', label: '头像', type: 'image' },
  { prop: 'signature', label: '签名', type: 'textarea' },
  { prop: 'title', label: '职务' },
  { prop: 'employee_no', label: '工号' },
  { prop: 'remark', label: '备注', type: 'textarea' },
]

onShow(() => {
  form.value = { ...(authStore.userInfo?.profile ?? authStore.userInfo ?? {}) }
})

async function save() {
  loading.value = true
  try {
    await authApi.updateProfile({ ...form.value })
    await authStore.refreshUserInfo()
    uni.showToast({ title: '已保存', icon: 'success' })
    uni.navigateBack()
  } finally {
    loading.value = false
  }
}
</script>
