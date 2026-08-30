<!-- Author: Charlie -->

<script setup lang="ts">
import { authApi } from '@/api'
import { useAuthStore } from '@/stores'
import { onMounted, reactive } from 'vue'
import '../profile.css'

const authStore = useAuthStore()

const state = reactive({
  loading: false,
  savingProfile: false,
  me: null as any,
  profileForm: {
    nickname: '',
    signature: '',
    remark: '',
  },
})

onMounted(async () => {
  await refresh()
})

async function refresh() {
  state.loading = true
  try {
    const data = await authStore.refreshUserInfo()
    state.me = data
    syncForms(data)
  } finally {
    state.loading = false
  }
}

function syncForms(data: any) {
  const currentProfile = data?.profile ?? {}
  state.profileForm.nickname = data?.nickname ?? currentProfile.nickname ?? ''
  state.profileForm.signature = currentProfile.signature ?? ''
  state.profileForm.remark = currentProfile.remark ?? ''
}

async function saveProfile() {
  state.savingProfile = true
  try {
    await authApi.updateProfile({
      nickname: state.profileForm.nickname || null,
      signature: state.profileForm.signature || null,
      remark: state.profileForm.remark || null,
    })
    await refresh()
    window.$message.success('保存成功')
  } finally {
    state.savingProfile = false
  }
}

defineExpose({ refresh })
</script>

<template>
  <NSpin :show="state.loading">
    <NForm
      class="profile-form profile-form--narrow w-full min-w-0"
      label-placement="top"
    >
      <NFormItem label="账号">
        <NInput
          :value="state.me?.account"
          disabled
        />
        <template #feedback>
          <span class="profile__hint">登录账号不可修改。</span>
        </template>
      </NFormItem>
      <NFormItem label="昵称">
        <NInput v-model:value="state.profileForm.nickname" />
      </NFormItem>
      <NFormItem label="个性签名">
        <NInput
          v-model:value="state.profileForm.signature"
          type="textarea"
          :rows="3"
          placeholder="一句话介绍自己"
        />
      </NFormItem>
      <NFormItem label="备注">
        <NInput
          v-model:value="state.profileForm.remark"
          type="textarea"
          :rows="3"
        />
      </NFormItem>
      <NFormItem :show-label="false">
        <NButton
          type="primary"
          :loading="state.savingProfile"
          @click="saveProfile"
        >
          更新资料
        </NButton>
      </NFormItem>
    </NForm>
  </NSpin>
</template>
