<script lang="ts" setup>
import type { SelectOption } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysDictFetch, useSysGroupFetch, useSysRoleFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysRoleDefaultData, sysRoleAdd, sysRoleEdit } = useSysRoleFetch()
const formData = ref<any>({ ...sysRoleDefaultData })
const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: ['input', 'blur'] },
  ],
  code: [
    { required: true, message: '请输入编码', trigger: ['input', 'blur'] },
  ],
  dataScope: [
    { required: true, message: '请选择数据范围', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  ],
  level: [
    { required: true, message: '请输入角色层级', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysRoleDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysRoleEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysRoleAdd(formData.value)
        if (success) {
          window.$message.success('新增成功')
        }
      }
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

const dataScopeRef = ref()
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])
function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  useSysDictFetch().sysDictOptions({ dictType: 'DATA_SCOPE' }).then(({ data }) => {
    dataScopeRef.value = data
  })
  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })
}
defineExpose({
  doOpen,
})

watch(
  () => formData.value.dataScope,
  (newValue) => {
    if (newValue !== 'SPECIFIED_GROUP') {
      formData.value.assignGroupIds = []
    }
  },
  { immediate: true },
)
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="名称" path="name">
          <NInput v-model:value="formData.name" placeholder="请输入名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="编码" path="code">
          <NInput v-model:value="formData.code" placeholder="请输入编码" :disabled="isEdit" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" placeholder="请输入描述" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="数据范围" path="dataScope">
          <!-- <NInput v-model:value="formData.dataScope" placeholder="请输入描述" /> -->
          <NSelect
            v-model:value="formData.dataScope"
            placeholder="请选择数据范围"
            :options="dataScopeRef"
          />
        </NFormItem>
        <NFormItem v-if="formData.dataScope === 'SPECIFIED_GROUP'" label="用户组" path="assignGroupIds">
          <n-tree-select
            v-model:value="formData.assignGroupIds"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
            multiple
          />
        </NFormItem>
        <!-- 数字输入 -->
        <!-- <NFormItem label="角色层级" path="level">
          <NInputNumber v-model:value="formData.level" :min="0" :max="100" placeholder="请输入角色层级" />
        </NFormItem> -->
      </NForm>
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
