<script lang="ts" setup>
import { CameraOutline, MailOutline } from '@vicons/ionicons5'
import { useAuthFetch } from '@/composables'

const message = useMessage()

// 用户信息
const userInfo = reactive({
  avatar: 'https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg',
  username: 'coder123',
  nickname: '编程小王子',
  email: 'coder@example.com',
  college: '1',
  major: '1',
  studentId: '20230001',
  signature: 'Stay hungry, stay foolish',
  emailNotification: true,
  contestNotification: false,
})

// 安全信息
const securityInfo = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 学校选项
const collegeOptions = [
  { label: '清华大学', value: '1' },
  { label: '北京大学', value: '2' },
  { label: '浙江大学', value: '3' },
  { label: '复旦大学', value: '4' },
]

// 专业选项
const majorOptions = [
  { label: '计算机科学与技术', value: '1' },
  { label: '软件工程', value: '2' },
  { label: '人工智能', value: '3' },
  { label: '电子信息工程', value: '4' },
]

// 头像上传相关
const showAvatarModal = ref(false)
const avatarFile = ref(null)

function handleAvatarChange(options: any) {
  const { file } = options
  avatarFile.value = file.file
  return false
}

function saveAvatar() {
  // 这里应该是上传头像的API调用
  message.success('头像更新成功')
  showAvatarModal.value = false
}

// 保存个人信息
function saveProfile() {
  // 这里应该是调用API保存个人信息
  message.success('个人信息更新成功')
}

// 修改密码
function changePassword() {
  if (securityInfo.newPassword !== securityInfo.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  // 这里应该是调用API修改密码
  message.success('密码修改成功')
  securityInfo.oldPassword = ''
  securityInfo.newPassword = ''
  securityInfo.confirmPassword = ''
}

// 标签页
const activeTab = ref('profile')

const { getProfile } = useAuthFetch()

const profile = ref()
const profileFormData = ref({})
async function loadData() {
  const { data } = await getProfile()
  if (data) {
    profile.value = data
    profileFormData.value = { ...data }
  }
}
loadData()
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <n-grid cols="12 m:24" :x-gap="16" :y-gap="16" responsive="screen">
      <!-- 左侧筛选区域 -->
      <n-gi span="12 m:4">
        <n-card class="content-card" size="small" hoverable>
          <n-flex vertical align="center">
            <n-avatar
              round
              :size="80"
              :src=" profile?.avatar"
              class="mb-2 cursor-pointer"
              @click="showAvatarModal = true"
            />
            <n-text>
              {{ profile?.nickname }}
            </n-text>
            <n-text depth="3">
              {{ profile?.quote }}
            </n-text>
            <n-divider />
            <n-button
              block
              :type="activeTab === 'profile' ? 'primary' : 'default'"
              @click="activeTab = 'profile'"
            >
              个人信息
            </n-button>

            <n-button
              block
              :type="activeTab === 'security' ? 'primary' : 'default'"
              @click="activeTab = 'security'"
            >
              安全设置
            </n-button>
          </n-flex>
        </n-card>
      </n-gi>
      <n-gi span="12 m:20">
        <n-card v-if="activeTab === 'profile'" class="content-card min-h-[calc(100vh-100px)]" size="small" hoverable>
          <n-form label-placement="left" label-width="auto" :model="userInfo">
            <div class="flex items-center mb-6">
              <n-avatar
                round
                :size="64"
                :src="profileFormData.avatar"
                class="mr-4 cursor-pointer"
                @click="showAvatarModal = true"
              />
              <n-button @click="showAvatarModal = true">
                <template #icon>
                  <n-icon :component="CameraOutline" />
                </template>
                更换头像
              </n-button>
            </div>

            <n-form-item label="用户名">
              <n-input :value="profileFormData.username" disabled />
            </n-form-item>

            <n-form-item label="昵称">
              <n-input v-model:value="profileFormData.nickname" placeholder="请输入昵称" />
            </n-form-item>

            <n-form-item label="电子邮箱">
              <n-input v-model:value="profileFormData.email" placeholder="请输入邮箱">
                <template #prefix>
                  <n-icon :component="MailOutline" />
                </template>
              </n-input>
            </n-form-item>

            <n-form-item label="个性签名">
              <n-input
                v-model:value="profileFormData.quote"
                type="textarea"
                placeholder="请输入个性签名"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </n-form-item>

            <!-- <n-form-item label="邮件通知">
              <n-switch v-model:value="userInfo.emailNotification" />
            </n-form-item>

            <n-form-item label="比赛提醒">
              <n-switch v-model:value="userInfo.contestNotification" />
            </n-form-item> -->

            <div class="flex justify-end mt-6">
              <n-button type="primary" @click="saveProfile">
                保存设置
              </n-button>
            </div>
          </n-form>
        </n-card>
        <n-card v-if="activeTab === 'security'" class="content-card min-h-[calc(100vh-100px)]" size="small" hoverable>
          <n-alert type="info" class="mb-6">
            定期修改密码可以提高账号安全性
          </n-alert>

          <n-form label-placement="left" label-width="auto" :model="securityInfo">
            <n-form-item label="当前密码">
              <n-input
                v-model:value="securityInfo.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password-on="click"
              />
            </n-form-item>

            <n-form-item label="新密码">
              <n-input
                v-model:value="securityInfo.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password-on="click"
              />
            </n-form-item>

            <n-form-item label="确认密码">
              <n-input
                v-model:value="securityInfo.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password-on="click"
              />
            </n-form-item>

            <div class="flex justify-end mt-6">
              <n-button type="primary" @click="changePassword">
                修改密码
              </n-button>
            </div>
          </n-form>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 头像上传模态框 -->
    <n-modal v-model:show="showAvatarModal" preset="card" title="更换头像" style="width: 500px">
      <div class="flex flex-col items-center">
        <n-upload
          accept="image/*"
          :default-upload="false"
          class="mb-4"
          @change="handleAvatarChange"
        >
          <n-button>选择图片</n-button>
        </n-upload>

        <div v-if="avatarFile" class="mb-4">
          <img
            :src="URL.createObjectURL(avatarFile)"
            class="w-48 h-48 rounded-full object-cover border"
            alt="预览"
          >
        </div>

        <div class="flex gap-4">
          <n-button @click="showAvatarModal = false">
            取消
          </n-button>
          <n-button type="primary" :disabled="!avatarFile" @click="saveAvatar">
            保存
          </n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>

</style>
