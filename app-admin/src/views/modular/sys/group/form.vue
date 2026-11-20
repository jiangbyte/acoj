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
    { required: true, message: 'Select Parent User Group', trigger: ['input', 'blur'] },
  ],
  name: [
    { required: true, message: 'Enter Name', trigger: ['input', 'blur'] },
  ],
  code: [
    // { required: true, message: 'Enter Code', trigger: ['input', 'blur'] },
  ],
  description: [
    // { required: true, message: 'Enter Description', trigger: ['input', 'blur'] },
  ],
  sort: [
    // { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
  adminId: [
    // { required: true, message: 'Enter Owner', trigger: ['input', 'blur'] },
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
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysGroupAdd(formData.value)
        if (success) {
          window.$message.success('Created successfully')
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
  // In edit mode, initialize the display name when adminId exists
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
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="User Group" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter User Group" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Parent User Group" path="parentId">
          <!-- <NInput v-model:value="formData.parentId" placeholder="Enter Parent User Group" /> -->
          <!-- <NSelect
            v-model:value="formData.parentId"
            filterable
            placeholder="SearchUser Group"
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
        <!-- Text input -->
        <NFormItem label="Name" path="name">
          <NInput v-model:value="formData.name" placeholder="Enter Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Code" path="code">
          <NInput v-model:value="formData.code" placeholder="Enter Code" :disabled="isEdit" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Description" path="description">
          <NInput v-model:value="formData.description" placeholder="Enter Description" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Sort Order" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="Enter Sort Order" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Owner" path="adminId">
          <!-- <NInput v-model:value="formData.adminId" placeholder="Enter Owner" /> -->
          <NSelect
            v-model:value="formData.adminId"
            filterable
            placeholder="Select an owner"
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
        <!-- Boolean selector -->
        <NFormItem label="System Group" path="groupType">
          <NRadioGroup v-model:value="formData.groupType">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
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
