<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysBannerFetch, useSysDictFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysBannerDefaultData, sysBannerAdd, sysBannerEdit } = useSysBannerFetch()
const formData = ref<any>({ ...sysBannerDefaultData })
const rules = {
  title: [
    // { required: true, message: '请输入标题', trigger: ['input', 'blur'] },
  ],
  banner: [
    { required: true, message: '请输入横幅', trigger: ['input', 'blur'] },
  ],
  buttonText: [
    // { required: true, message: '请输入按钮文字', trigger: ['input', 'blur'] },
  ],
  jumpModule: [
    { required: true, message: '请输入跳转模块', trigger: ['input', 'blur'] },
  ],
  jumpType: [
    // { required: true, message: '请输入跳转类别', trigger: ['input', 'blur'] },
  ],
  jumpTarget: [
    { required: true, message: '请输入跳转目标', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
  subtitle: [
    // { required: true, message: '请输入子标题', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysBannerDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysBannerEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysBannerAdd(formData.value)
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

const jumpModuleRef = ref()
const jumpTypeRef = ref()
function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  useSysDictFetch().sysDictOptions({ dictType: 'JUMP_MODULE' }).then(({ data }) => {
    jumpModuleRef.value = data
  })
  // useSysDictFetch().sysDictOptions({ dictType: 'JUMP_TYPE' }).then(({ data }) => {
  //   jumpTypeRef.value = data
  // })
}
defineExpose({
  doOpen,
})

const isDisableTargetBlank = ref(false)
const jumpTargetListOptions = ref()

// 如果formDataJUMP_MODULE选择外链，JUMP_TYPE才显示URL
watch(() => formData.value.jumpModule, (val) => {
  // 当状态为三种之一时，统一处理
  const isSpecialState = ['NOTDO', 'PAGE_PROBLEM', 'PAGE_SET'].includes(val)

  if (val === 'HREF') {
    useSysDictFetch().sysDictOptions({ dictType: 'JUMP_TYPE' }).then(({ data }) => {
      jumpTypeRef.value = data.filter((item: any) => item.value === 'URL')
      formData.value.targetBlank = true
      isDisableTargetBlank.value = true
      formData.value.jumpType = 'URL'
    })
  }
  else if (isSpecialState) {
    formData.value.jumpType = ''
    formData.value.jumpTarget = ''
    formData.value.targetBlank = false
    jumpTargetListOptions.value = []
    jumpTypeRef.value = []
  }
  else {
    // useSysBannerFetch().sysBannerJumpTargetList({ jumpModule: val, keyword: '' }).then(({ data }) => {
    //   jumpTargetListOptions.value = data
    // })
    loadJumpTarget('', val)

    useSysDictFetch().sysDictOptions({ dictType: 'JUMP_TYPE' }).then(({ data }) => {
      jumpTypeRef.value = data.filter((item: any) => item.value !== 'URL')
      formData.value.targetBlank = false
      isDisableTargetBlank.value = false
      formData.value.jumpType = 'ID'
    })
  }
})

function loadJumpTarget(value: string, jm: string = formData.value.jumpModule) {
  useSysBannerFetch().sysBannerJumpTargetList({ jumpModule: jm, keyword: value }).then(({ data }) => {
    jumpTargetListOptions.value = data
  })
}
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
        <NFormItem label="横幅" path="banner">
          <FileUpload v-model="formData.banner" :is-image="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="子标题" path="subtitle">
          <NInput v-model:value="formData.subtitle" placeholder="请输入子标题" />
        </NFormItem>
        <NFormItem label="子标题是否可见" path="isVisibleSubtitle">
          <NRadioGroup v-model:value="formData.isVisibleSubtitle">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="按钮文字" path="buttonText">
          <NInput v-model:value="formData.buttonText" placeholder="请输入按钮文字" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="按钮是否可见" path="isVisibleButton">
          <NRadioGroup v-model:value="formData.isVisibleButton">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="跳转模块" path="jumpModule">
          <!-- <NInput v-model:value="formData.jumpModule" placeholder="请输入跳转模块" /> -->
          <NSelect
            v-model:value="formData.jumpModule"
            placeholder="请选择跳转模块"
            :options="jumpModuleRef"
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem v-if="!['NOTDO', 'PAGE_PROBLEM', 'PAGE_SET'].includes(formData.jumpModule)" label="跳转类别" path="jumpType">
          <NInput v-model:value="formData.jumpType" placeholder="跳转类别" :disabled="true" />
          <!-- <NSelect
            v-model:value="formData.jumpType"
            placeholder="请选择跳转类别"
            :options="jumpTypeRef"
            :disabled="!formData.jumpModule"
          /> -->
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem v-if="!['NOTDO', 'PAGE_PROBLEM', 'PAGE_SET'].includes(formData.jumpModule)" label="跳转目标" path="jumpTarget">
          <NInput v-if="formData.jumpModule === 'HREF' " v-model:value="formData.jumpTarget" placeholder="请输入跳转目标URL" :disabled="!formData.jumpModule" />
          <NSelect
            v-else
            v-model:value="formData.jumpTarget"
            placeholder="请选择跳转目标"
            label-field="name"
            value-field="id"
            filterable
            :options="jumpTargetListOptions || []"
            :disabled="!formData.jumpModule"
            @search="(val) => loadJumpTarget(val)"
          />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem v-if="formData.jumpModule !== 'NOTDO'" label="新窗口打开" path="targetBlank">
          <NRadioGroup v-model:value="formData.targetBlank" :disabled="isDisableTargetBlank">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <!-- <NFormItem label="链接" path="toUrl">
          <NInput v-model:value="formData.toUrl" placeholder="请输入链接" />
        </NFormItem> -->
        <!-- Boolean 选择框 -->
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="上架" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
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
