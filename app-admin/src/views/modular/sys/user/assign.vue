<script lang="ts" setup>
import { NDrawer, NDrawerContent } from 'naive-ui'
import { useSysRoleFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const loading = ref(false)
const userIdR = ref('')
const show = ref(false)
const authRoles = ref<any>([])
const assignRoles = ref<any>([])
function doClose() {
  emit('close')
  show.value = false
}
async function doSubmit() {
  loading.value = true

  useSysRoleFetch().sysRoleAssign({
    userId: userIdR.value,
    roleIds: assignRoles.value,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('分配成功')
      emit('submit', true)
      doClose()
      show.value = false
      loading.value = false
    }
  })
}

const isLoading = ref(false)
function doOpen(row: any) {
  show.value = true
  assignRoles.value = row.assignRoles
  userIdR.value = row.id

  isLoading.value = true
  useSysRoleFetch().sysRoleAuthRoles1().then(({ data }) => {
    authRoles.value = data
    isLoading.value = false
  })
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="角色分配">
      <n-spin :show="isLoading">
        <n-checkbox-group v-model:value="assignRoles">
          <n-space vertical>
            <n-checkbox
              v-for="item in authRoles"
              :key="item.id"
              :value="item.id"
              :label="`${item.name} (${item.description})`"
              :disabled="item.isOpen"
            />
          </n-space>
        </n-checkbox-group>
      </n-spin>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            保存
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
