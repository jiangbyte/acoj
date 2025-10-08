<script setup lang="ts">
import { useAuthFetch, useSysUserFetch } from '@/composables/v1'
import type { FormInst, FormItemRule } from 'naive-ui'

const { sysUserDefaultData } = useSysUserFetch()
const formRef = ref<FormInst | null>(null)
const passwordFormRef = ref<FormInst | null>(null)
const profileData = ref(sysUserDefaultData)
const isEdit = ref(false)
async function loadData() {
  // getProfile().then(({ data }) => {
  //   if (data) {
  //     profileData.value = data
  //   }
  // })

  useAuthFetch().getProfile().then(({ data }) => {
    if (data) {
      profileData.value = data
    }
  })
}
loadData()

function updateProfile() {
  if (formRef.value) {
    formRef.value.validate().then((valid) => {
      if (valid) {
        useSysUserFetch().sysUserUpdateProfile(profileData.value).then(() => {
          loadData()
        })
        isEdit.value = false
        window.$message.success('个人资料更新成功')
      }
    })
  }
}

function updateAvatar(url: string) {
  useSysUserFetch().sysUserUpdateAvatar({
    id: profileData.value.id,
    img: url,
  }).then(({ data }) => {
    if (data) {
      window.$message.success('头像更新成功')
    }
  })
}
function updateBackground(url: string) {
  useSysUserFetch().sysUserUpdateBackground({
    id: profileData.value.id,
    img: url,
  }).then(({ data }) => {
    if (data) {
      window.$message.success('背景图更新成功')
    }
  })
}

const showPasswordModal = ref(false)
const passwordData = ref({
  id: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

function updatePassword() {
  passwordData.value.id = profileData.value.id
  useSysUserFetch().sysUserUpdatePassword(passwordData.value).then(({ data }) => {
    // window.$message.success('密码更新成功')
    if (data) {
      window.$message.success('密码更新成功')
      updatePasswordReset()
    }
  })
}
function updatePasswordReset() {
  passwordData.value = {
    id: '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  showPasswordModal.value = false
}
function validatePasswordSame(rule: FormItemRule, value: string): boolean {
  return value === passwordData.value.newPassword
}
function validatePasswordStartWith(
  rule: FormItemRule,
  value: string,
): boolean {
  return (
    !!passwordData.value.newPassword
    && passwordData.value.newPassword.startsWith(value)
    && passwordData.value.newPassword.length >= value.length
  )
}
const passwordRules = {
  oldPassword: [
    {
      required: true,
      message: '请输入旧密码',
      trigger: ['input', 'blur'],
    },
  ],
  newPassword: [
    {
      required: true,
      validator: validatePasswordStartWith,
      message: '两次密码输入不一致',
      trigger: ['input', 'blur'],
    },
  ],
  confirmPassword: [
    {
      required: true,
      validator: validatePasswordSame,
      message: '两次密码输入不一致',
      trigger: ['blur', 'password-input'],
    },
  ],
}

const profileRules = {
  nickname: [
    {
      required: true,
      message: '请输入昵称',
      trigger: ['input', 'blur'],
    },
  ],
  email: [
    {
      required: true,
      message: '请输入邮箱',
      trigger: ['input', 'blur'],
    },
  ],
  quote: [
    {
      required: true,
      message: '请输入个性签名',
      trigger: ['input', 'blur'],
    },
  ],
}
</script>

<template>
  <!-- 主内容区 -->
  <div class="container mx-auto px-4 py-8">
    <div class="grid grid-cols-1 lg:grid-cols-1 gap-8">
      <!-- 侧边导航 -->
      <!-- <div class="lg:col-span-1">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden sticky top-24">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              账户中心
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              管理您的个人信息和账户设置
            </p>
          </div>

          <nav class="p-2">
            <a href="#profile" class="flex items-center px-4 py-3 text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 rounded-lg mb-1">
              <i class="fa fa-user-circle w-5 text-center mr-3" />
              <span>个人资料</span>
            </a>
            <a href="#security" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-lock w-5 text-center mr-3" />
              <span>安全设置</span>
            </a>
            <a href="#notifications" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-bell w-5 text-center mr-3" />
              <span>通知设置</span>
            </a>
            <a href="#preferences" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-cog w-5 text-center mr-3" />
              <span>偏好设置</span>
            </a>
            <a href="#billing" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-credit-card w-5 text-center mr-3" />
              <span>付费服务</span>
            </a>
            <a href="#connected" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg transition-colors">
              <i class="fa fa-link w-5 text-center mr-3" />
              <span>关联账号</span>
            </a>
          </nav>
        </div>
      </div> -->

      <!-- 主要设置内容 -->
      <!-- <div class="lg:col-span-3 space-y-8"> -->
      <div class="lg:col-span-1 space-y-8">
        <!-- 个人资料设置 -->
        <div id="profile" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              个人资料
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              更新您的个人信息和公开资料
            </p>
          </div>

          <form class="p-6 space-y-6">
            <!-- 头像设置 -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium">
                头像设置
              </h3>
              <div class="flex flex-wrap items-center gap-6">
                <div class="w-24 h-24 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
                  <img :src="profileData?.avatar" alt="当前头像" class="w-full h-full object-cover">
                </div>
                <div class="flex flex-col gap-3">
                  <FileUploadButton v-model="profileData.avatar" :is-image="true" buttontext="上传新头像" @success="updateAvatar" />
                  <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md">
                    推荐尺寸: 200x200像素，支持JPG、PNG格式，文件大小不超过2MB
                  </p>
                </div>
              </div>
            </div>

            <!-- 背景图设置 -->
            <div class="space-y-4 pt-4 border-t border-gray-200 dark:border-gray-700">
              <h3 class="text-lg font-medium">
                封面背景
              </h3>
              <div class="relative h-50 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
                <img :src="profileData?.background" class="w-full h-full object-cover">
                <div class="absolute right-3 bottom-3 px-3 py-1.5 text-sm transition-colors">
                  <FileUploadButton v-model="profileData.background" :is-image="true" buttontext="更换封面" @success="updateBackground" />
                </div>
              </div>
              <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md">
                推荐尺寸: 1920x500像素，支持JPG、PNG格式，文件大小不超过5MB
              </p>
            </div>

            <!-- 基本信息 -->
            <n-form
              ref="formRef"
              inline
              :label-width="80"
              :model="profileData"
              :rules="profileRules"
            >
              <n-grid cols="12 m:24" :x-gap="24" responsive="screen">
                <n-form-item-gi :span="12" label="用户名" path="username">
                  <n-input v-model:value="profileData.username" placeholder="请输入用户名" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="12" label="昵称" path="nickname">
                  <n-input v-model:value="profileData.nickname" placeholder="请输入昵称" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="12" label="电子邮箱" path="email">
                  <n-input v-model:value="profileData.email" placeholder="请输入电子邮箱" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="12" label="学号" path="studentNumber">
                  <n-input v-model:value="profileData.studentNumber" placeholder="请输入学号" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="12" label="手机号" path="telephone">
                  <n-input v-model:value="profileData.telephone" placeholder="请输入手机号" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="12" label="性别" path="gender">
                  <!-- <n-select v-model:value="profileData.gender" placeholder="请选择性别" /> -->
                  <n-radio-group v-model:value="profileData.gender" :disabled="!isEdit">
                    <n-space>
                      <n-radio :key="0" :value="0">
                        未知
                      </n-radio>
                      <n-radio :key="1" :value="1">
                        男
                      </n-radio>
                      <n-radio :key="2" :value="2">
                        女
                      </n-radio>
                    </n-space>
                  </n-radio-group>
                </n-form-item-gi>
                <n-form-item-gi span="12 m:24" label="个人签名" path="quote">
                  <n-input v-model:value="profileData.quote" type="textarea" placeholder="请输入个人签名" :disabled="!isEdit" />
                </n-form-item-gi>
              </n-grid>
            </n-form>

            <!-- 保存按钮 -->
            <div class="flex justify-end gap-3 border-t border-gray-200 dark:border-gray-700">
              <n-button v-if="isEdit" @click="isEdit = false">
                取消
              </n-button>
              <n-button v-if="!isEdit" type="primary" @click="isEdit = true">
                编辑
              </n-button>
              <n-button v-if="isEdit" type="primary" @click="updateProfile">
                保存更改
              </n-button>
            </div>
          </form>
        </div>

        <!-- 安全设置 -->
        <div id="security" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              安全设置
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              管理您的账户安全和登录选项
            </p>
          </div>

          <div class="p-6 space-y-6">
            <!-- 密码修改 -->
            <div class="p-5 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <div class="flex flex-wrap justify-between items-center gap-4">
                <div>
                  <h3 class="font-medium">
                    密码
                  </h3>
                  <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                    建议定期更换密码以保证账户安全
                  </p>
                </div>
                <n-button @click="showPasswordModal = true">
                  修改密码
                </n-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <n-modal
      v-model:show="showPasswordModal"
      :mask-closable="false"
      preset="card"
      style="width: 400px;"
      title="密码修改"
      @negative-click="() => { showPasswordModal = false }"
      @update:show="updatePasswordReset"
    >
      <n-flex vertical>
        <n-form
          ref="passwordFormRef"
          inline
          :label-width="80"
          :model="passwordData"
          :rules="passwordRules"
        >
          <n-grid :cols="12" :x-gap="24">
            <n-form-item-gi :span="12" label="旧密码" path="oldPassword">
              <n-input v-model:value="passwordData.oldPassword" placeholder="请输入旧密码" show-password-on="click" type="password" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="新密码" path="newPassword">
              <n-input v-model:value="passwordData.newPassword" placeholder="请输入新密码" show-password-on="click" type="password" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="确认密码" path="confirmPassword">
              <n-input v-model:value="passwordData.confirmPassword" placeholder="请确认新密码" show-password-on="click" type="password" />
            </n-form-item-gi>
          </n-grid>
        </n-form>
        <n-flex justify="end" align="center">
          <n-button @click="updatePasswordReset">
            取消
          </n-button>
          <n-button type="primary" @click="updatePassword">
            确认修改
          </n-button>
        </n-flex>
      </n-flex>
    </n-modal>
  </div>
</template>

<style scoped>
/* 基础样式补充 */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* 图片悬停效果 */
img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
}

/* 导航栏滚动效果 */
header {
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

header.scrolled {
  background-color: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.dark header.scrolled {
  background-color: #1f2937;
}

/* 开关按钮样式 */
input:checked + label {
  background-color: #3b82f6;
}

input:checked + label:after {
  transform: translateX(6px);
}
</style>
