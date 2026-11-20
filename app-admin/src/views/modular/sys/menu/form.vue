<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysMenuFetch } from '@/composables/v1'
import { Icon } from '@iconify/vue/offline'
import CodeEditor from '@/components/common/editor/code/CodeEditor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysMenuDefaultData, sysMenuAdd, sysMenuEdit } = useSysMenuFetch()
const formData = ref<any>({ ...sysMenuDefaultData })
const rules = {
  pid: [
    // { required: true, message: 'Enter Parent Menu ID', trigger: ['input', 'blur'] },
  ],
  name: [
    { required: true, message: 'Enter Menu Name', trigger: ['input', 'blur'] },
  ],
  path: [
    { required: true, message: 'Enter Route Path', trigger: ['input', 'blur'] },
  ],
  // componentPath: [
  //   { required: true, message: 'Enter Component Path', trigger: ['input', 'blur'] },
  // ],
  title: [
    { required: true, message: 'Enter Menu Title', trigger: ['input', 'blur'] },
  ],
  icon: [
    { required: true, message: 'Enter Icon', trigger: ['input', 'blur'] },
  ],
  sort: [
    // { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
  menuType: [
    { required: true, message: 'Enter Menu Type', type: 'number', trigger: ['input', 'blur'] },
  ],
  // exJson: [
  //   { required: true, message: 'Enter Extra Information', trigger: ['input', 'blur'] },
  // ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysMenuDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysMenuEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysMenuAdd(formData.value)
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

const menuOptions = ref()
function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  useSysMenuFetch().sysMenuTreeList().then(({ data }) => {
    if (data) {
      console.log(data)
      menuOptions.value = data
    }
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
        <NFormItem v-if="isEdit" label="Menu ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter Menu ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Icon" path="icon">
          <NIcon size="20" class="mr-2">
            <Icon :icon="formData.icon" />
          </NIcon>
          <NInput v-model:value="formData.icon" placeholder="Enter Icon" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Menu Title" path="title">
          <NInput v-model:value="formData.title" placeholder="Enter Menu Title" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Menu Name" path="name">
          <NInput v-model:value="formData.name" placeholder="Enter Menu Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Parent Menu" path="pid">
          <!-- <NInput v-model:value="formData.pid" placeholder="Enter Parent Menu ID" /> -->
          <n-tree-select
            v-model:value="formData.pid"
            :options="menuOptions"
            label-field="title"
            key-field="id"
            :indent="12"
            clearable
          />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Route Path" path="path">
          <NInput v-model:value="formData.path" placeholder="Enter Route Path" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Component Path" path="componentPath">
          <NInput v-model:value="formData.componentPath" placeholder="Enter Component Path">
            <template #prefix>
              <n-text depth="3">
                @/src/view
              </n-text>
            </template>
          </NInput>
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Sort Order" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="Enter Sort Order" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Cache" path="keepAlive">
          <NRadioGroup v-model:value="formData.keepAlive">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Visible" path="visible">
          <NRadioGroup v-model:value="formData.visible">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Pinned" path="pined">
          <NRadioGroup v-model:value="formData.pined">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Menu Type" path="menuType">
          <!-- <NInputNumber v-model:value="formData.menuType" :min="0" :max="100" placeholder="Enter Menu Type" /> -->
          <NRadioGroup v-model:value="formData.menuType">
            <NRadio :value="0">
              Directory
            </NRadio>
            <NRadio :value="1">
              Menu
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="Header Parameters" path="parameters">
          <!-- <NInput v-model:value="formData.parameters" type="textarea" placeholder="Enter Header Parameters" /> -->
          <n-flex vertical class="w-full">
            <CodeEditor
              v-model="formData.parameters"
              :language="String('json')"
              style="height: 300px;"
            />
            <n-alert type="info" show-icon>
              Use JSON format. Available parameters:<br>
              href_inner: External link (embedded in current page)<br>
              href_outter: External link (open in new window)
            </n-alert>
          </n-flex>
        </NFormItem>
        <!-- Text input -->
        <!-- <NFormItem label="Extra Information" path="exJson">
          <NInput v-model:value="formData.exJson" placeholder="Enter Extra Information" />
        </NFormItem> -->
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
