<script setup lang="ts">
import { useAuthFetch, useSysDictFetch, useSysUserFetch } from '@/composables/v1'
import type { FormInst, FormItemRule } from 'naive-ui'

const { sysUserDefaultData } = useSysUserFetch()
const formRef = ref<FormInst | null>(null)
const passwordFormRef = ref<FormInst | null>(null)
const profileData = ref(sysUserDefaultData)
const isEdit = ref(false)
const genderRef = ref()
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

  useSysDictFetch().sysDictOptions({ dictType: 'SYS_GENDER' }).then(({ data }) => {
    // genderRef.value = data
    // 对value进行num转换
    genderRef.value = data.map((item: { label: string, value: number }) => {
      item.value = Number(item.value)
      return item
    })
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
  console.log('avatar URL', url)
  useSysUserFetch().sysUserUpdateAvatar({
    id: profileData.value.id,
    img: url,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('头像更新成功')
    }
  })
}
function updateBackground(url: string) {
  useSysUserFetch().sysUserUpdateBackground({
    id: profileData.value.id,
    img: url,
  }).then(({ success }) => {
    if (success) {
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
  if (passwordFormRef.value) {
    passwordFormRef.value.validate().then((valid) => {
      if (valid) {
        passwordData.value.id = userInfo.value.id
        useSysUserFetch().sysUserUpdatePassword(passwordData.value).then(({ success }) => {
          // window.$message.success('密码更新成功')
          if (success) {
            window.$message.success('密码更新成功')
            updatePasswordReset()
          }
        })
      }
    })
  }
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
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: ['input', 'blur'],
    },
  ],
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
  gender: [
    {
      required: true,
      message: '请选择性别',
      // trigger: ['input', 'blur'],
    },
  ],
}
</script>

<template>
  <!-- 主内容区 -->
  <div class="container mx-auto px-2 py-6">
    <n-space vertical :size="16">
      <n-card size="small" class="rounded-xl">
        <template #header>
          <n-h3 class="pb-0 mb-0">
            头像设置
          </n-h3>
        </template>
        <n-space align="center">
          <div class="w-24 h-24 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
            <img :src="profileData?.avatar" alt="当前头像" class="w-full h-full object-cover">
          </div>
          <n-space vertical>
            <FileUploadButton v-model="profileData.avatar" type="primary" :is-image="true" buttontext="上传新头像" @success="updateAvatar" />
            <n-text depth="3">
              推荐尺寸: 200x200像素，支持JPG、PNG格式，文件大小不超过2MB
            </n-text>
          </n-space>
        </n-space>
      </n-card>

      <n-card size="small" class="rounded-xl">
        <template #header>
          <n-h3 class="pb-0 mb-0">
            封面背景
          </n-h3>
        </template>
        <n-space vertical>
          <div class="relative h-50 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
            <img :src="profileData?.background" class="w-full h-full object-cover">
            <div class="absolute right-3 bottom-3 px-3 py-1.5 text-sm transition-colors">
              <FileUploadButton v-model="profileData.background" :is-image="true" buttontext="更换封面" type="primary" @success="updateBackground" />
            </div>
          </div>
          <n-text depth="3" class="w-full">
            推荐尺寸: 1920x500像素，支持JPG、PNG格式，文件大小不超过5MB
          </n-text>
        </n-space>
      </n-card>

      <n-card size="small" class="rounded-xl">
        <template #header>
          <n-h3 class="pb-0 mb-0">
            个人信息
          </n-h3>
        </template>
        <n-form
          ref="formRef"
          inline
          :label-width="80"
          :model="profileData"
          :rules="profileRules"
        >
          <n-grid cols="4 m:12" :x-gap="24" responsive="screen">
            <n-form-item-gi :span="4" label="用户名" path="username">
              <n-input v-model:value="profileData.username" placeholder="请输入用户名" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="4" label="昵称" path="nickname">
              <n-input v-model:value="profileData.nickname" placeholder="请输入昵称" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="4" label="电子邮箱" path="email">
              <n-input v-model:value="profileData.email" placeholder="请输入电子邮箱" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="4" label="学号" path="studentNumber">
              <n-input v-model:value="profileData.studentNumber" placeholder="请输入学号" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="4" label="手机号" path="telephone">
              <n-input v-model:value="profileData.telephone" placeholder="请输入手机号" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="4" label="性别" path="gender">
              <NSelect
                v-model:value="profileData.gender"
                placeholder="请选择性别"
                :options="genderRef"
                :disabled="!isEdit"
              />
            </n-form-item-gi>
            <n-form-item-gi span="12 m:24" label="个人签名" path="quote">
              <n-input v-model:value="profileData.quote" type="textarea" placeholder="请输入个人签名" :disabled="!isEdit" />
            </n-form-item-gi>
          </n-grid>
        </n-form>
        <template #footer>
          <n-flex justify="end">
            <n-button v-if="isEdit" @click="isEdit = false">
              取消
            </n-button>
            <n-button v-if="!isEdit" type="primary" @click="isEdit = true">
              编辑
            </n-button>
            <n-button v-if="isEdit" type="primary" @click="updateProfile">
              保存更改
            </n-button>
          </n-flex>
        </template>
      </n-card>

      <n-card size="small" class="rounded-xl">
        <template #header>
          <n-h3 class="pb-0 mb-0">
            安全设置
          </n-h3>
        </template>
        <n-space align="center" justify="space-between">
          <n-text depth="3" class="w-full">
            建议定期更换密码以保证账户安全
          </n-text>
          <n-button type="warning" @click="showPasswordModal = true">
            修改密码
          </n-button>
        </n-space>
      </n-card>
    </n-space>

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
