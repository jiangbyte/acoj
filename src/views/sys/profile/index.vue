<template>
  <div class="flex flex-col gap-2">
    <a-row :gutter="12">
      <!-- Left: Avatar & Basic Info -->
      <a-col :xs="24" :md="7" class="mb-2 md:mb-0">
        <a-card :bordered="false">
          <div class="flex flex-col items-center gap-4 py-2">
            <!-- Avatar -->
            <div
              class="w-28 h-28 rounded-full overflow-hidden cursor-pointer ring-2 ring-$border-color-split hover:ring-$primary-color transition-all duration-200 relative group"
              @click="openCrop"
            >
              <img
                v-if="displayAvatar"
                :src="displayAvatar"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center text-4xl font-medium text-white"
                :style="{ background: avatarBg }"
              >
                {{ avatarLetter }}
              </div>
              <div
                class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <CameraOutlined class="text-white text-xl" />
              </div>
            </div>

            <!-- Nickname & Account -->
            <div class="text-center w-full">
              <div class="text-base font-medium text-$text-color">{{ userInfo?.nickname || userInfo?.account || '-' }}</div>
              <div class="text-sm text-$text-color-secondary mt-0.5">{{ userInfo?.account }}</div>
              <div v-if="userInfo?.motto" class="text-sm text-$text-color-secondary italic mt-2">"{{ userInfo.motto }}"</div>
            </div>

            <!-- Info List -->
            <div class="w-full text-sm text-$text-color-secondary space-y-2 pt-3 border-t border-$border-color-split">
              <div class="flex items-center gap-2">
                <span class="w-16 shrink-0">性别</span>
                <span class="text-$text-color">{{ userInfo?.gender ? $dict.label('GENDER', userInfo.gender) : '-' }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-16 shrink-0">生日</span>
                <span class="text-$text-color">{{ userInfo?.birthday || '-' }}</span>
                <span v-if="age" class="text-xs text-$text-color-secondary">({{ age }}岁)</span>
              </div>
              <a-divider class="my-1.5" />
              <div class="flex items-center gap-2">
                <span class="w-16 shrink-0">组织</span>
                <span class="text-$text-color">{{ userInfo?.org_name || '-' }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-16 shrink-0">职位</span>
                <span class="text-$text-color">{{ userInfo?.position_name || '-' }}</span>
              </div>
              <a-divider class="my-1.5" />
              <div class="flex items-center gap-2">
                <span class="w-16 shrink-0">最后登录</span>
                <span class="text-$text-color text-xs">{{ userInfo?.last_login_at || '-' }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- Right: Tabs -->
      <a-col :xs="24" :md="17">
        <a-card :bordered="false">
          <a-tabs v-model:activeKey="activeTab" size="small">
            <a-tab-pane key="info" tab="基础信息">
              <a-form :model="basicForm" layout="vertical">
                <a-row :gutter="16">
                  <a-col :xs="24" :md="12">
                    <a-form-item label="昵称" name="nickname">
                      <a-input v-model:value="basicForm.nickname" placeholder="请输入昵称" maxlength="32" show-count :disabled="!basicEditing" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="性别" name="gender">
                      <DictSelect v-model="basicForm.gender" type-code="GENDER" placeholder="请选择" allow-clear :disabled="!basicEditing" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24">
                    <a-form-item label="签名" name="motto">
                      <a-textarea v-model:value="basicForm.motto" placeholder="一句话介绍自己" :rows="2" maxlength="32" show-count :disabled="!basicEditing" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="生日" name="birthday">
                      <a-date-picker v-model:value="basicForm.birthday" value-format="YYYY-MM-DD" class="w-full" :disabled="!basicEditing" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="GitHub" name="github">
                      <a-input v-model:value="basicForm.github" placeholder="GitHub 用户名" :disabled="!basicEditing" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <div class="flex gap-2 justify-end">
                  <a-button v-if="!basicEditing" type="primary" ghost @click="basicEditing = true">编辑</a-button>
                  <template v-else>
                    <a-button @click="cancelBasicEdit">取消</a-button>
                    <a-button type="primary" :loading="basicSaving" @click="handleSaveBasic">保存</a-button>
                  </template>
                </div>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="account" tab="账号信息">
              <a-form :model="form" layout="vertical">
                <a-alert
                  v-if="accountEditing"
                  message="修改用户名可能导致部分功能异常，请谨慎操作"
                  type="warning"
                  show-icon
                  class="mb-4"
                />
                <a-form-item label="用户名" name="account">
                  <a-input v-model:value="form.account" placeholder="请输入用户名" :disabled="!accountEditing" />
                </a-form-item>
                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="form.email" placeholder="请输入邮箱" :disabled="!accountEditing" />
                </a-form-item>
                <a-form-item label="手机号" name="phone">
                  <a-input v-model:value="form.phone" placeholder="请输入手机号" :disabled="!accountEditing" />
                </a-form-item>
                <div class="flex gap-2 justify-end">
                  <a-button v-if="!accountEditing" type="primary" ghost @click="accountEditing = true">编辑</a-button>
                  <template v-else>
                    <a-button @click="cancelAccountEdit">取消</a-button>
                    <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
                  </template>
                </div>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="security" tab="账户安全">
              <a-form :model="passwordForm" layout="vertical">
                <a-form-item label="当前密码" name="currentPassword">
                  <a-input-password v-model:value="passwordForm.currentPassword" placeholder="请输入当前密码" :disabled="!securityEditing" />
                </a-form-item>
                <a-form-item label="新密码" name="newPassword">
                  <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" :disabled="!securityEditing" />
                </a-form-item>
                <a-form-item label="确认新密码" name="confirmPassword">
                  <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" :disabled="!securityEditing" />
                </a-form-item>
                <div class="flex gap-2 justify-end">
                  <a-button v-if="!securityEditing" type="primary" ghost @click="securityEditing = true">编辑</a-button>
                  <template v-else>
                    <a-button @click="cancelSecurityEdit">取消</a-button>
                    <a-button type="primary" :loading="passwordSaving" @click="handleUpdatePassword">更新密码</a-button>
                  </template>
                </div>
              </a-form>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <!-- Avatar Crop Modal -->
    <a-modal
      :open="cropVisible"
      title="裁剪头像"
      ok-text="确认"
      cancel-text="取消"
      :width="600"
      :confirm-loading="cropLoading"
      destroy-on-close
      @ok="confirmCrop"
      @cancel="cropVisible = false"
    >
      <div v-if="cropImage" class="flex gap-6 items-start">
        <div class="w-80 flex-shrink-0">
          <Cropper
            ref="cropperRef"
            :src="cropImage"
            :debounce="false"
            :stencil-props="{ aspectRatio: 1 }"
            class="max-h-80"
            @change="onCropChange"
          />
        </div>
        <div class="flex flex-col items-center gap-2 pt-2 flex-shrink-0 min-w-20">
          <span class="text-xs text-$text-color-secondary">预览</span>
          <Preview
            :width="80"
            :height="80"
            :image="cropResult.image"
            :coordinates="cropResult.coordinates"
            class="rounded-full overflow-hidden"
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysProfile' })
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { CameraOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/store'
import { fetchUserUpdateProfile, fetchUserUpdateAvatar, fetchUserUpdatePassword } from '@/api/user'
import DictSelect from '@/components/form/DictSelect.vue'
import { Cropper, Preview } from 'vue-advanced-cropper'
import 'vue-advanced-cropper/dist/style.css'

const auth = useAuthStore()
const userInfo = computed(() => auth.userInfo)
const activeTab = ref('info')

const age = computed(() => {
  if (!userInfo.value?.birthday) return null
  const today = new Date()
  const birth = new Date(userInfo.value.birthday)
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--
  return age
})

const basicForm = ref({
  nickname: '',
  motto: '',
  gender: undefined as string | undefined,
  birthday: undefined as string | undefined,
  github: '',
})
const basicSaving = ref(false)
const basicEditing = ref(false)

const form = ref({
  account: '',
  email: '',
  phone: '',
})
const saving = ref(false)
const accountEditing = ref(false)

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordSaving = ref(false)
const securityEditing = ref(false)

const displayAvatar = ref('')

// Switch to account tab when entering, populate form
onMounted(() => {
  if (userInfo.value?.avatar) {
    displayAvatar.value = userInfo.value.avatar
  }
  initForm()
  useDictStore().loadDict()
})

function initForm() {
  const u = userInfo.value || {}
  basicForm.value = {
    nickname: u.nickname || '',
    motto: u.motto || '',
    gender: u.gender || undefined,
    birthday: u.birthday || undefined,
    github: u.github || '',
  }
  form.value = {
    account: u.account || '',
    email: u.email || '',
    phone: u.phone || '',
  }
}

function cancelBasicEdit() {
  initForm()
  basicEditing.value = false
}

function cancelAccountEdit() {
  initForm()
  accountEditing.value = false
}

function cancelSecurityEdit() {
  passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  securityEditing.value = false
}

async function handleSaveBasic() {
  basicSaving.value = true
  const { success } = await fetchUserUpdateProfile(basicForm.value)
  if (success) {
    message.success('保存成功')
    await auth.fetchUserInfo()
    basicEditing.value = false
  }
  basicSaving.value = false
}

async function handleSave() {
  saving.value = true
  const { success } = await fetchUserUpdateProfile(form.value)
  if (success) {
    message.success('保存成功')
    await auth.fetchUserInfo()
    accountEditing.value = false
  }
  saving.value = false
}

async function handleUpdatePassword() {
  const { currentPassword, newPassword, confirmPassword } = passwordForm.value
  if (!currentPassword || !newPassword || !confirmPassword) {
    message.warning('请填写完整的密码信息')
    return
  }
  if (newPassword !== confirmPassword) {
    message.warning('两次输入的新密码不一致')
    return
  }
  passwordSaving.value = true
  const { success } = await fetchUserUpdatePassword({
    current_password: auth.encryptPassword(currentPassword),
    new_password: auth.encryptPassword(newPassword),
  })
  if (success) {
    message.success('密码更新成功')
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
    securityEditing.value = false
  }
  passwordSaving.value = false
}

// ── Avatar ──
const avatarColors = ['#1677ff', '#52c41a', '#faad14', '#722ed1', '#eb2f96', '#fa8c16', '#13c2c2']

const avatarLetter = computed(() => {
  const name = userInfo.value?.nickname || userInfo.value?.account || ''
  return name.charAt(0).toUpperCase() || '?'
})

const avatarBg = computed(() => {
  const name = userInfo.value?.nickname || userInfo.value?.account || ''
  const idx = name.charCodeAt(0) || 0
  return avatarColors[idx % avatarColors.length]
})

// Cropper
const cropVisible = ref(false)
const cropImage = ref<string | null>(null)
const cropLoading = ref(false)
const cropperRef = ref<InstanceType<typeof Cropper> | null>(null)

const cropResult = ref<{ image: any; coordinates: any }>({
  image: null,
  coordinates: null,
})

function openCrop() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e: any) => {
    const file = e.target?.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = (ev) => {
      cropImage.value = ev.target?.result as string
      cropVisible.value = true
    }
    reader.readAsDataURL(file)
  }
  input.click()
}

function onCropChange({ coordinates, image }: { coordinates: any; image: any }) {
  cropResult.value = { coordinates, image }
}

async function confirmCrop() {
  cropLoading.value = true
  const result = cropperRef.value?.getResult()
  const canvas = result?.canvas
  if (!canvas) {
    cropLoading.value = false
    return
  }
  const base64 = canvas.toDataURL('image/png')
  const { success } = await fetchUserUpdateAvatar({ avatar: base64 })
  if (success) {
    message.success('头像更新成功')
    displayAvatar.value = base64
    cropVisible.value = false
    await auth.fetchUserInfo()
  }
  cropLoading.value = false
}

import { useDictStore } from '@/store'
</script>
