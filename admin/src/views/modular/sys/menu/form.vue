<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysMenuFetch } from '@/composables/v1'
import { Icon } from '@iconify/vue'
import CodeEditor from '@/components/common/editor/code/Editor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysMenuDefaultData, sysMenuAdd, sysMenuEdit } = useSysMenuFetch()
const formData = ref<any>({ ...sysMenuDefaultData })
const rules = {
  pid: [
    // { required: true, message: '请输入父菜单ID', trigger: ['input', 'blur'] },
  ],
  name: [
    { required: true, message: '请输入菜单名称', trigger: ['input', 'blur'] },
  ],
  path: [
    { required: true, message: '请输入路由路径', trigger: ['input', 'blur'] },
  ],
  // componentPath: [
  //   { required: true, message: '请输入组件路径', trigger: ['input', 'blur'] },
  // ],
  title: [
    { required: true, message: '请输入菜单标题', trigger: ['input', 'blur'] },
  ],
  icon: [
    { required: true, message: '请输入图标', trigger: ['input', 'blur'] },
  ],
  sort: [
    // { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
  menuType: [
    { required: true, message: '请输入菜单类型', type: 'number', trigger: ['input', 'blur'] },
  ],
  // exJson: [
  //   { required: true, message: '请输入额外信息', trigger: ['input', 'blur'] },
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
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysMenuAdd(formData.value)
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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="菜单ID" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入菜单ID" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="图标" path="icon">
          <NIcon size="20" class="mr-2">
            <Icon :icon="formData.icon" />
          </NIcon>
          <NInput v-model:value="formData.icon" placeholder="请输入图标" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="菜单标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入菜单标题" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="菜单名称" path="name">
          <NInput v-model:value="formData.name" placeholder="请输入菜单名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="父菜单" path="pid">
          <!-- <NInput v-model:value="formData.pid" placeholder="请输入父菜单ID" /> -->
          <n-tree-select
            v-model:value="formData.pid"
            :options="menuOptions"
            label-field="title"
            key-field="id"
            :indent="12"
            clearable
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="路由路径" path="path">
          <NInput v-model:value="formData.path" placeholder="请输入路由路径" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="组件路径" path="componentPath">
         <NInput v-model:value="formData.componentPath" placeholder="请输入组件路径" >
            <template #prefix>
              <n-text depth="3">
            @/src/view
          </n-text>
              </template>
          </NInput>
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="缓存" path="keepAlive">
          <NRadioGroup v-model:value="formData.keepAlive">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="可见" path="visible">
          <NRadioGroup v-model:value="formData.visible">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="固定" path="pined">
          <NRadioGroup v-model:value="formData.pined">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="菜单类型" path="menuType">
          <!-- <NInputNumber v-model:value="formData.menuType" :min="0" :max="100" placeholder="请输入菜单类型" /> -->
          <NRadioGroup v-model:value="formData.menuType">
            <NRadio :value="0">
              目录
            </NRadio>
            <NRadio :value="1">
              菜单
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="头部参数" path="parameters">
          <!-- <NInput v-model:value="formData.parameters" type="textarea" placeholder="请输入头部参数" /> -->
          <n-flex vertical class="w-full">
            <CodeEditor
              v-model="formData.parameters"
              :language="String('json')"
              style="height: 300px;"
            />
            <n-alert type="info" show-icon>
              请使用JSON格式，可用参数：<br>
              href_inner: 外部链接（当前页面内嵌）<br>
              href_outter: 外部链接（新窗口打开）
            </n-alert>
          </n-flex>
        </NFormItem>
        <!-- 输入框 -->
        <!-- <NFormItem label="额外信息" path="exJson">
          <NInput v-model:value="formData.exJson" placeholder="请输入额外信息" />
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
