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
    // { required: true, message: 'Enter Title', trigger: ['input', 'blur'] },
  ],
  banner: [
    { required: true, message: 'Enter Banner', trigger: ['input', 'blur'] },
  ],
  buttonText: [
    // { required: true, message: 'Enter Button Text', trigger: ['input', 'blur'] },
  ],
  jumpModule: [
    { required: true, message: 'Enter Navigation Module', trigger: ['input', 'blur'] },
  ],
  jumpType: [
    // { required: true, message: 'Enter Navigation Type', trigger: ['input', 'blur'] },
  ],
  jumpTarget: [
    { required: true, message: 'Enter Navigation Target', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
  subtitle: [
    // { required: true, message: 'Enter Subtitle', trigger: ['input', 'blur'] },
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
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysBannerAdd(formData.value)
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

// Show URL in JUMP_TYPE only when formData JUMP_MODULE selects an external link
watch(() => formData.value.jumpModule, (val) => {
  // Handle all three statuses consistently
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
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Banner" path="banner">
          <FileUpload v-model="formData.banner" :is-image="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Title" path="title">
          <NInput v-model:value="formData.title" placeholder="Enter Title" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Subtitle" path="subtitle">
          <NInput v-model:value="formData.subtitle" placeholder="Enter Subtitle" />
        </NFormItem>
        <NFormItem label="SubtitleVisible" path="isVisibleSubtitle">
          <NRadioGroup v-model:value="formData.isVisibleSubtitle">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Button Text" path="buttonText">
          <NInput v-model:value="formData.buttonText" placeholder="Enter Button Text" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Show Button" path="isVisibleButton">
          <NRadioGroup v-model:value="formData.isVisibleButton">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Navigation Module" path="jumpModule">
          <!-- <NInput v-model:value="formData.jumpModule" placeholder="Enter Navigation Module" /> -->
          <NSelect
            v-model:value="formData.jumpModule"
            placeholder="Select Navigation Module"
            :options="jumpModuleRef"
          />
        </NFormItem>
        <!-- Text input -->
        <NFormItem v-if="!['NOTDO', 'PAGE_PROBLEM', 'PAGE_SET'].includes(formData.jumpModule)" label="Navigation Type" path="jumpType">
          <NInput v-model:value="formData.jumpType" placeholder="Navigation Type" :disabled="true" />
          <!-- <NSelect
            v-model:value="formData.jumpType"
            placeholder="Select Navigation Type"
            :options="jumpTypeRef"
            :disabled="!formData.jumpModule"
          /> -->
        </NFormItem>
        <!-- Text input -->
        <NFormItem v-if="!['NOTDO', 'PAGE_PROBLEM', 'PAGE_SET'].includes(formData.jumpModule)" label="Navigation Target" path="jumpTarget">
          <NInput v-if="formData.jumpModule === 'HREF' " v-model:value="formData.jumpTarget" placeholder="Enter Navigation TargetURL" :disabled="!formData.jumpModule" />
          <NSelect
            v-else
            v-model:value="formData.jumpTarget"
            placeholder="Select Navigation Target"
            label-field="name"
            value-field="id"
            filterable
            :options="jumpTargetListOptions || []"
            :disabled="!formData.jumpModule"
            @search="(val) => loadJumpTarget(val)"
          />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem v-if="formData.jumpModule !== 'NOTDO'" label="Open in New Window" path="targetBlank">
          <NRadioGroup v-model:value="formData.targetBlank" :disabled="isDisableTargetBlank">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Text input -->
        <!-- <NFormItem label="Link" path="toUrl">
          <NInput v-model:value="formData.toUrl" placeholder="Enter Link" />
        </NFormItem> -->
        <!-- Boolean selector -->
        <!-- Number input -->
        <NFormItem label="Sort Order" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="Enter Sort Order" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Published" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
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
