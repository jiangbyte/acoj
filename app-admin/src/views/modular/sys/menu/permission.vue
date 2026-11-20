<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem } from 'naive-ui'
import { useAuthFetch, useSysMenuFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const formData = ref({
  id: null,
  permissions: [],
})
const rules = {

}
function doClose() {
  emit('close')
  show.value = false
  formData.value = {
    id: null,
    permissions: [],
  }
}

const isEdit = ref(false)
const { sysMenuAssignMenuPermission } = useSysMenuFetch()
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      const { success } = await sysMenuAssignMenuPermission(formData.value)
      if (success) {
        window.$message.success('Processed successfully')
      }
      //   if (isEdit.value) {
      //     const { success } = await sysMenuEdit(formData.value)
      //     if (success) {
      //       window.$message.success('Updated successfully')
      //     }
      //   }
      //   else {
      //     const { success } = await sysMenuAdd(formData.value)
      //     if (success) {
      //       window.$message.success('Created successfully')
      //     }
      //   }
      emit('submit', true)
      doClose()
      show.value = false
      loading.value = false
    }
    else {
      //
    }
  })
}

const permissionOptions = ref([])
function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value.id = row?.id
  formData.value.permissions = row?.exJson || []

  useAuthFetch().permissionList().then(({ data }) => {
    // permissionOptions.value = data
    // Process data: disable the first level, format second-level names as name(value), and use permissions for children
    permissionOptions.value = data.map(item => ({
      label: `${item.controllerName}(${item.controller})`,
      key: item.controller,
      disabled: true,
      children: item.permissions?.map(child => ({
        label: `${child.methodName}(${child.permission})`,
        key: child.permission,
      })) || [],
    }))
    console.log(permissionOptions.value)
  })
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem label="MenuPermissions" path="permissions">
          <n-tree
            v-model:checked-keys="formData.permissions"
            block-line
            :data="permissionOptions"
            expand-on-click
            checkable
            default-expand-all
          />

          <!-- {{ formData }} -->
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            Save
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
