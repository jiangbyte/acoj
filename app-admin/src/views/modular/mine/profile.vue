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
    // Convert value to a number
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
function updateAvatar(objectName: string, displayUrl?: string) {
  // 1. Update the local avatar first to avoid a blank state while waiting for the API
  if (displayUrl) {
    userInfo.value.avatar = displayUrl
  }
  // 2. Persist the change (the backend updates the signed-in user)
  useSysUserFetch().sysUserUpdateAvatar({
    id: userInfo.value.id,
    img: objectName,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('Avatar updated')
      loadData()
      window.dispatchEvent(new CustomEvent('acoj:profile-updated'))
    }
  })
}
function updateBackground(objectName: string, displayUrl?: string) {
  if (displayUrl) {
    userInfo.value.background = displayUrl
  }
  useSysUserFetch().sysUserUpdateBackground({
    id: userInfo.value.id,
    img: objectName,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('Background updated')
      loadData()
      window.dispatchEvent(new CustomEvent('acoj:profile-updated'))
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
          // window.$message.success('Password updated')
          if (success) {
            window.$message.success('Password updated')
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
      message: 'Enter current password',
      trigger: ['input', 'blur'],
    },
  ],
  newPassword: [
    {
      required: true,
      validator: validatePasswordStartWith,
      message: 'Passwords do not match',
      trigger: ['input', 'blur'],
    },
  ],
  confirmPassword: [
    {
      required: true,
      validator: validatePasswordSame,
      message: 'Passwords do not match',
      trigger: ['blur', 'password-input'],
    },
  ],
}

const profileRules = {
  username: [
    {
      required: true,
      message: 'Enter username',
      trigger: ['input', 'blur'],
    },
  ],
  nickname: [
    {
      required: true,
      message: 'Enter Nickname',
      trigger: ['input', 'blur'],
    },
  ],
  email: [
    {
      required: true,
      message: 'Enter Email',
      trigger: ['input', 'blur'],
    },
  ],
  quote: [
    {
      required: true,
      message: 'Enter your bio',
      trigger: ['input', 'blur'],
    },
  ],
  gender: [
    {
      required: true,
      message: 'Select Gender',
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
        window.$message.success('Profile updated')
      }
    })
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <n-card size="small" class="flex-1 pb-11">
      <NSpace vertical size="large">
        <!-- Basic information card -->
        <NCard
          title="Basic Information"
          class="relative"
          size="small"
          :style="{
            backgroundImage: userInfo.background ? `linear-gradient(to left bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,0) 0, rgba(255,255,255,1) 65%), url(${userInfo.background})` : '',
            backgroundSize: 'cover',
            backgroundPosition: 'right top',
            backgroundRepeat: 'no-repeat',
          }"
        >
          <!-- Change-background button positioned absolutely -->
          <div class="absolute top-4 right-4">
            <FileUploadTextButton v-model="userInfo.background" color="white" :is-image="true" buttontext="Change Background" @success="updateBackground" />
          </div>
          <NSpace vertical size="large">
            <NSpace align="center" :wrap="false">
              <NSpace vertical align="center" size="small">
                <NAvatar
                  round
                  :size="90"
                  :src="userInfo.avatar"
                  object-fit="cover"
                />
                <FileUploadTextButton
                  v-model="userInfo.avatar"
                  type="primary"
                  :is-image="true"
                  :enable-crop="true"
                  :crop-aspect="1"
                  buttontext="Change Avatar"
                  @success="updateAvatar"
                />
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
                    Registered At: <n-time :time="Number(userInfo.createTime)" />
                  </NText>
                  <NText depth="3">
                    Last Sign In: <n-time :time="Number(userInfo.loginTime)" />
                  </NText>
                </n-space>
              </NSpace>
            </NSpace>

            <NSpace size="small">
              <NText strong>
                Phone Number
              </NText>
              <NText depth="3">
                {{ userInfo?.telephone }}
              </NText>
            </NSpace>
            <NSpace size="small">
              <NText strong>
                Email Address
              </NText>
              <NText depth="3">
                {{ userInfo?.email }}
              </NText>
            </NSpace>
            <NSpace size="small">
              <NText strong>
                Gender
              </NText>
              <NText depth="3">
                {{ userInfo?.genderName }}
              </NText>
            </NSpace>
            <NSpace size="small">
              <NText strong>
                Bio
              </NText>
              <NText depth="3">
                {{ userInfo?.quote }}
              </NText>
            </NSpace>
          </NSpace>

          <template #footer>
            <NSpace justify="end">
              <NButton type="primary" @click="() => { showProfileModal = true, isEdit = true }">
                Edit Profile
              </NButton>
            </NSpace>
          </template>
        </NCard>

        <n-card size="small">
          <template #header>
            <n-h3 class="pb-0 mb-0">
              Security
            </n-h3>
          </template>
          <n-space align="center" justify="space-between">
            <n-text depth="3" class="w-full">
              Change your password regularly to keep your account secure
            </n-text>
            <n-button type="warning" @click="showPasswordModal = true">
              Change Password
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
      title="Edit Profile"
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
            <n-form-item-gi :span="12" label="username" path="username">
              <n-input v-model:value="userInfo.username" placeholder="Enter username" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Nickname" path="nickname">
              <n-input v-model:value="userInfo.nickname" placeholder="Enter Nickname" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Email" path="email">
              <n-input v-model:value="userInfo.email" placeholder="Enter Email" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Student ID" path="studentNumber">
              <n-input v-model:value="userInfo.studentNumber" placeholder="Enter Student ID" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Phone Number" path="telephone">
              <n-input v-model:value="userInfo.telephone" placeholder="Enter Phone Number" :disabled="!isEdit" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Gender" path="gender">
              <!-- <n-select v-model:value="userInfo.gender" placeholder="Select Gender" /> -->
              <!-- <n-radio-group v-model:value="userInfo.gender" :disabled="!isEdit">
                <n-space>
                  <n-radio :key="0" :value="0">
                    Unknown
                  </n-radio>
                  <n-radio :key="1" :value="1">
                    Male
                  </n-radio>
                  <n-radio :key="2" :value="2">
                    Female
                  </n-radio>
                </n-space>
              </n-radio-group> -->

              <NSelect
                v-model:value="userInfo.gender"
                placeholder="Select Gender"
                :options="genderRef"
                :disabled="!isEdit"
              />
            </n-form-item-gi>
            <n-form-item-gi span="12 m:24" label="Bio" path="quote">
              <n-input v-model:value="userInfo.quote" type="textarea" placeholder="Enter Bio" :disabled="!isEdit" />
            </n-form-item-gi>
          </n-grid>
        </n-form>
        <n-flex justify="end" align="center">
          <n-button @click="updateProfileReset">
            Cancel
          </n-button>
          <!-- <n-button v-if="!isEdit" type="primary" @click="isEdit = true">
            Edit
          </n-button> -->
          <n-button type="primary" @click="updateProfile">
            Save Changes
          </n-button>
        </n-flex>
      </n-flex>
    </n-modal>

    <n-modal
      v-model:show="showPasswordModal"
      :mask-closable="false"
      preset="card"
      style="width: 400px;"
      title="Change Password"
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
            <n-form-item-gi :span="12" label="Current Password" path="oldPassword">
              <n-input v-model:value="passwordData.oldPassword" placeholder="Enter current password" show-password-on="click" type="password" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="New Password" path="newPassword">
              <n-input v-model:value="passwordData.newPassword" placeholder="Enter New Password" show-password-on="click" type="password" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Confirmpassword" path="confirmPassword">
              <n-input v-model:value="passwordData.confirmPassword" placeholder="Confirm new password" show-password-on="click" type="password" />
            </n-form-item-gi>
          </n-grid>
        </n-form>
        <n-flex justify="end" align="center">
          <n-button @click="updatePasswordReset">
            Cancel
          </n-button>
          <n-button type="primary" @click="updatePassword">
            Confirm Change
          </n-button>
        </n-flex>
      </n-flex>
    </n-modal>
  </div>
</template>

<style scoped>

</style>
