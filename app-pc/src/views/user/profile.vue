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
    // Implementation note.
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
        window.$message.success('Profile updated')
      }
    })
  }
}

function updateAvatar(objectName: string, displayUrl?: string) {
  // Implementation note.
  if (displayUrl) {
    profileData.value.avatar = displayUrl
  }
  // Implementation note.
  useSysUserFetch().sysUserUpdateAvatar({
    id: profileData.value.id,
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
    profileData.value.background = displayUrl
  }
  useSysUserFetch().sysUserUpdateBackground({
    id: profileData.value.id,
    img: objectName,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('Cover image updated')
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
      message: 'Enter your current password',
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
      message: 'Enter your username',
      trigger: ['input', 'blur'],
    },
  ],
  nickname: [
    {
      required: true,
      message: 'Enter your nickname',
      trigger: ['input', 'blur'],
    },
  ],
  email: [
    {
      required: true,
      message: 'Enter your email',
      trigger: ['input', 'blur'],
    },
  ],
  quote: [
    {
      required: true,
      message: 'Enter a bio',
      trigger: ['input', 'blur'],
    },
  ],
  gender: [
    {
      required: true,
      message: 'Select a gender',
      // trigger: ['input', 'blur'],
    },
  ],
}
</script>

<template>
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:6"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <!-- Section -->
      <n-gi span="1 l:2">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h3 class="pb-0 mb-0">
                Avatar
              </n-h3>
            </template>
            <n-space align="center" :wrap="false">
              <div class="w-24 h-24 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
                <SafeImage :src="profileData?.avatar" alt="Current avatar" class="w-full h-full" object-fit="cover" />
              </div>
              <n-space vertical>
                <FileUploadButton
                  v-model="profileData.avatar"
                  type="primary"
                  :is-image="true"
                  :enable-crop="true"
                  :crop-aspect="1"
                  buttontext="Upload New Avatar"
                  @success="updateAvatar"
                />
                <n-text depth="3">
                  Recommended: 200×200 px, JPG or PNG, up to 2 MB
                </n-text>
              </n-space>
            </n-space>
          </n-card>

          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h3 class="pb-0 mb-0">
                Profile Cover
              </n-h3>
            </template>
            <n-space vertical>
              <div class="relative h-40 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
                <SafeImage :src="profileData?.background" class="w-full h-full" object-fit="cover" />
                <div class="absolute right-3 bottom-3 px-3 py-1.5 text-sm transition-colors">
                  <FileUploadButton v-model="profileData.background" :is-image="true" buttontext="Change Cover" type="primary" @success="updateBackground" />
                </div>
              </div>
              <n-text depth="3" class="w-full">
                Recommended: 1920×500 px, JPG or PNG, up to 5 MB
              </n-text>
            </n-space>
          </n-card>
        </NSpace>
      </n-gi>
      <!-- Section -->
      <n-gi span="1 l:4">
        <NSpace
          vertical
          :size="24"
        >
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h3 class="pb-0 mb-0">
                Profile
              </n-h3>
            </template>
            <n-form
              ref="formRef"
              inline
              :label-width="80"
              :model="profileData"
              :rules="profileRules"
            >
              <n-grid cols="4 m:8" :x-gap="24" responsive="screen">
                <n-form-item-gi :span="4" label="Username" path="username">
                  <n-input v-model:value="profileData.username" placeholder="Enter your username" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="4" label="Nickname" path="nickname">
                  <n-input v-model:value="profileData.nickname" placeholder="Enter your nickname" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="4" label="Email" path="email">
                  <n-input v-model:value="profileData.email" placeholder="Enter your email" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="4" label="Student ID" path="studentNumber">
                  <n-input v-model:value="profileData.studentNumber" placeholder="Enter your student ID" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="4" label="Phone" path="telephone">
                  <n-input v-model:value="profileData.telephone" placeholder="Enter your phone number" :disabled="!isEdit" />
                </n-form-item-gi>
                <n-form-item-gi :span="4" label="Gender" path="gender">
                  <NSelect
                    v-model:value="profileData.gender"
                    placeholder="Select a gender"
                    :options="genderRef"
                    :disabled="!isEdit"
                  />
                </n-form-item-gi>
                <n-form-item-gi span="12 m:24" label="Bio" path="quote">
                  <n-input v-model:value="profileData.quote" type="textarea" placeholder="Enter a bio" :disabled="!isEdit" />
                </n-form-item-gi>
              </n-grid>
            </n-form>
            <template #footer>
              <n-flex justify="end">
                <n-button v-if="isEdit" @click="isEdit = false">
                  Cancel
                </n-button>
                <n-button v-if="!isEdit" type="primary" @click="isEdit = true">
                  Edit
                </n-button>
                <n-button v-if="isEdit" type="primary" @click="updateProfile">
                  Save Changes
                </n-button>
              </n-flex>
            </template>
          </n-card>
        </NSpace>
      </n-gi>

      <n-gi span="6 l:6">
        <NSpace
          vertical
          :size="24"
        >
          <n-card size="small" class="rounded-xl">
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
      </n-gi>
    </n-grid>
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
              <n-input v-model:value="passwordData.oldPassword" placeholder="Enter your current password" show-password-on="click" type="password" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="New Password" path="newPassword">
              <n-input v-model:value="passwordData.newPassword" placeholder="Enter a new password" show-password-on="click" type="password" />
            </n-form-item-gi>
            <n-form-item-gi :span="12" label="Confirm Password" path="confirmPassword">
              <n-input v-model:value="passwordData.confirmPassword" placeholder="Confirm your new password" show-password-on="click" type="password" />
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
  </main>
</template>

<style scoped>
/* Component styles. */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* Component styles. */
img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
}

/* Component styles. */
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

/* Component styles. */
input:checked + label {
  background-color: #3b82f6;
}

input:checked + label:after {
  transform: translateX(6px);
}
</style>
