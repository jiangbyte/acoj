<script lang="ts" setup>
import type { SelectOption, SelectRenderLabel, SelectRenderTag } from 'naive-ui'
import { NAvatar, NButton, NDrawer, NDrawerContent, NEllipsis, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysGroupFetch, useSysUserFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysGroupDefaultData, sysGroupAdd, sysGroupEdit } = useSysGroupFetch()
const userOptionsLoading = ref(false)
const userOptions = ref<SelectOption[]>([])
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])
const formData = ref<any>({ ...sysGroupDefaultData })
const rules = {
  parentId: [
    { required: true, message: '请选择父级用户组', trigger: ['input', 'blur'] },
  ],
  name: [
    { required: true, message: '请输入名称', trigger: ['input', 'blur'] },
  ],
  code: [
    // { required: true, message: '请输入编码', trigger: ['input', 'blur'] },
  ],
  description: [
    // { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  ],
  sort: [
    // { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
  adminId: [
    // { required: true, message: '请输入负责人', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysGroupDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysGroupEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysGroupAdd(formData.value)
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

async function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  // const { data: data1 } = await sysUserOptions({ keyword: '' })
  // if (data1) {
  //   userOptions.value = data1
  //   userOptionsLoading.value = false
  // }

  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })
  // 如果编辑模式且已有adminId，则初始化显示名称
  if (edit && row?.adminId && row?.adminIdName) {
    userOptions.value = [{
      value: row.adminId,
      label: row.adminIdName,
    }]
    // groupOptions.value = [{
    //   value: row.parentId,
    //   label: row.parentIdName,
    // }]

    handleUserSearch('')
  }
}
defineExpose({
  doOpen,
})

const { sysUserOptions } = useSysUserFetch()
async function handleUserSearch(query: string) {
  userOptionsLoading.value = true
  const { data } = await sysUserOptions({ keyword: query })
  if (data) {
    userOptions.value = data
    userOptionsLoading.value = false
  }
}

const renderSingleSelectTag: SelectRenderTag = ({ option }) => {
  return h(
    'div',
    {
      style: {
        display: 'flex',
        alignItems: 'center',
      },
    },
    [
      h(NAvatar, {
        src: String(option.avatar),
        round: true,
        size: 24,
        style: {
          marginRight: '12px',
        },
      }),
      option.nickname as string,
    ],
  )
}

const renderLabel: SelectRenderLabel = (option) => {
  return h(
    'div',
    {
      style: {
        display: 'flex',
        alignItems: 'center',
      },
    },
    [
      h(NAvatar, {
        src: String(option.avatar),
        round: true,
        size: 'small',
      }),
      h(
        'div',
        {
          style: {
            marginLeft: '12px',
            padding: '4px 0',
          },
        },
        [
          h('div', null, [option.nickname as string]),
          h(
            NEllipsis,
            { lineClamp: 1 },
            {
              default: () => option.quote as string,
            },
          ),
        ],
      ),
    ],
  )
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="用户组" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入用户组" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="父级用户组" path="parentId">
          <!-- <NInput v-model:value="formData.parentId" placeholder="请输入父级用户组" /> -->
          <!-- <NSelect
            v-model:value="formData.parentId"
            filterable
            placeholder="搜索用户组"
            :options="groupOptions"
            :loading="groupOptionsLoading"
            clearable
            remote
            @search="handleGroupSearch"
          /> -->
          <n-tree-select
            v-model:value="formData.parentId"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
          />
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
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="负责人" path="adminId">
          <!-- <NInput v-model:value="formData.adminId" placeholder="请输入负责人" /> -->
          <NSelect
            v-model:value="formData.adminId"
            filterable
            placeholder="选择负责人"
            :options="userOptions"
            :loading="userOptionsLoading"
            :render-label="renderLabel"
            :render-tag="renderSingleSelectTag"
            label-field="nickname"
            value-field="id"
            clearable
            @search="handleUserSearch"
          />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="系统组" path="groupType">
          <NRadioGroup v-model:value="formData.groupType">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
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
