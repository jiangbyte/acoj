<script lang="ts" setup>
import { useSysDictFetch, useSysUserFetch } from '@/composables/v1'
import type { FormInst, FormItemRule } from 'naive-ui'

const showEditModal = ref(false)

const { sysUserDefaultData } = useSysUserFetch()
const userInfo = ref(sysUserDefaultData)
const formRef = ref<FormInst | null>(null)
const passwordFormRef = ref<FormInst | null>(null)
const isEdit = ref(false)
const genderRef = ref()
function loadData() {
  useSysUserFetch().getProfile().then(({ data }) => {
    userInfo.value = data

    if (!data.loginTime) {
      userInfo.value.loginTime = Date.now()
    }
    if (!data.createTime) {
      userInfo.value.createTime = Date.now()
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
const showProfileModal = ref(false)
function updateProfileReset() {
  useSysUserFetch().getProfile().then(({ data }) => {
    userInfo.value = data

    if (!data.loginTime) {
      userInfo.value.loginTime = Date.now()
    }
    if (!data.createTime) {
      userInfo.value.createTime = Date.now()
    }
  })
  showProfileModal.value = false
}
function updateAvatar(url: string) {
  console.log('avatar URL', url)
  useSysUserFetch().sysUserUpdateAvatar({
    id: userInfo.value.id,
    img: url,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('头像更新成功')
    }
  })
}
function updateBackground(url: string) {
  console.log('backgroundImage URL', url)
  useSysUserFetch().sysUserUpdateBackground({
    id: userInfo.value.id,
    img: url,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('背景更新成功')
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

function updateProfile() {
  if (formRef.value) {
    formRef.value.validate().then((valid) => {
      if (valid) {
        useSysUserFetch().sysUserUpdateProfile(userInfo.value).then(() => {
          loadData()
        })
        isEdit.value = false
        showProfileModal.value = false
        window.$message.success('个人资料更新成功')
      }
    })
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <n-card size="small" class="flex-1 pb-11">
      <NSpace vertical size="large">
        <!-- 基本信息卡片 -->
        <NCard
          title="基本信息"
          class="relative"
          size="small"
          :style="{
            backgroundImage: userInfo.background ? `linear-gradient(to left bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,0) 0, rgba(255,255,255,1) 65%), url(${userInfo.background})` : '',
            backgroundSize: 'cover',
            backgroundPosition: 'right top',
            backgroundRepeat: 'no-repeat',
          }"
        >
          <!-- 更换背景按钮 - 使用 absolute 定位 -->
          <div class="absolute top-4 right-4">
            <FileUploadTextButton v-model="userInfo.background" color="white" :is-image="true" buttontext="更换背景" @success="updateBackground" />
          </div>
          <NSpace vertical size="large">
            <NSpace align="center" :wrap="false">
              <NSpace vertical align="center" size="small">
                <NAvatar
                  round
                  :size="90"
                  :src="userInfo.avatar"
                />
                <FileUploadTextButton v-model="userInfo.avatar" type="primary" :is-image="true" buttontext="更换头像" @success="updateAvatar" />
              </NSpace>

              <NSpace vertical size="small" style="margin-left: 20px;">
                <NSpace align="center">
                  <h2 style="margin: 0">
                    {{ userInfo?.nickname }}
                  </h2>
                  <NTag size="small" type="info">
                    {{ userInfo?.username }}
                  </NTag>
                </NSpace>
                <NSpace vertical>
                  <NTag type="success">
                    {{ userInfo?.groupIdName }}
                  </NTag>
                  <n-space>
                    <n-tag v-for="(item, index) in userInfo?.roleNames" :key="index">
                      {{ item }}
                    </n-tag>
                  </n-space>
                </NSpace>
                <n-space align="center">
                  <NText depth="3">
                    注册时间: <n-time :time="Number(userInfo.createTime)" />
                  </NText>
                  <NText depth="3">
                    上次登录时间: <n-time :time="Number(userInfo.loginTime)" />
                  </NText>
                </n-space>
              </NSpace>
            </NSpace>

            <NSpace size="small">
              <NText strong>
                手机号码
              </NText>
              <NText depth="3">
                {{ userInfo?.telephone }}
              </NText>
            </NSpace>
            <NSpace size="small">
              <NText strong>
                邮箱地址
              </NText>
              <NText depth="3">
                {{ userInfo?.email }}
              </NText>
            </NSpace>
            <NSpace size="small">
              <NText strong>
                性别
              </NText>
              <NText depth="3">
                {{ userInfo?.genderName }}
              </NText>
            </NSpace>
            <NSpace size="small">
              <NText strong>
                个性签名
              </NText>
              <NText depth="3">
                {{ userInfo?.quote }}
              </NText>
            </NSpace>
          </NSpace>

          <template #footer>
            <NSpace justify="end">
              <NButton type="primary" @click="() => { showProfileModal = true, isEdit = true }">
                编辑信息
              </NButton>
            </NSpace>
          </template>
        </NCard>

        <n-card size="small" >
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
      </NSpace>
    </n-card>

    <n-modal
      v-model:show="showProfileModal"
      :mask-closable="false"
      preset="card"
      style="width: 600px;"
      title="信息修改"
      @negative-click="() => { showProfileModal = false }"
      @update:show="updateProfileReset"
    >
      <n-flex vertical>
        <n-form
          ref="formRef"
          inline
          :label-width="80"
          :model="userInfo"
          :rules="profileRules"
        >
          <n-grid cols="12 m:24" :x-gap="24" responsive="screen">
            <n-form-item-gi :span="12" label="用户名" path="username">
              <n-input v-model:value="userInfo.username" placeholder="请输入用户名" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="昵称" path="nickname">
              <n-input v-model:value="userInfo.nickname" placeholder="请输入昵称" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="电子邮箱" path="email">
              <n-input v-model:value="userInfo.email" placeholder="请输入电子邮箱" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="学号" path="studentNumber">
              <n-input v-model:value="userInfo.studentNumber" placeholder="请输入学号" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="手机号" path="telephone">
              <n-input v-model:value="userInfo.telephone" placeholder="请输入手机号" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="性别" path="gender">
              <!-- <n-select v-model:value="userInfo.gender" placeholder="请选择性别" /> -->
              <!-- <n-radio-group v-model:value="userInfo.gender" :disabled="!isEdit">
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
              </n-radio-group> -->

              <NSelect
                v-model:value="userInfo.gender"
                placeholder="请选择性别"
                :options="genderRef"
                :disabled="!isEdit"
              />
            </n-form-item-gi>
            <n-form-item-gi span="12 m:24" label="个人签名" path="quote">
              <n-input v-model:value="userInfo.quote" type="textarea" placeholder="请输入个人签名" :disabled="!isEdit" />
            </n-form-item-gi>
          </n-grid>
        </n-form>
        <n-flex justify="end" align="center">
          <n-button @click="updateProfileReset">
            取消
          </n-button>
          <!-- <n-button v-if="!isEdit" type="primary" @click="isEdit = true">
            编辑
          </n-button> -->
          <n-button type="primary" @click="updateProfile">
            保存更改
          </n-button>
        </n-flex>
      </n-flex>
    </n-modal>

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

</style>
