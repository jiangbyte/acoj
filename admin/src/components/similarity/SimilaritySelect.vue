<script lang="ts" setup>
import { useSysDictFetch, useSysGroupFetch } from '@/composables/v1'
import type { SelectOption } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NForm, NInputNumber } from 'naive-ui'
import { v4 as uuidv4 } from 'uuid'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const allowLanguageOptions = ref()
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])
const formData = ref({
  problemId: [] as string[],
  setId: '',
  language: null,
  isSet: false,
  taskId: '',
  groupId: null,
  createTime: Date.now(),
  batchTaskId: '',
  config: {
    minSampleSize: 0,
    recentSampleSize: 0,
    minMatchLength: 15,
  },
})
const rules = {
  problemId: [
    // { required: true, message: '请输入题集ID', trigger: ['input', 'blur'] },
  ],
  setId: [
  ],
  language: [
  ],
  isSet: [
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = {
    problemId: [],
    setId: '',
    language: null,
    isSet: false,
    taskId: '',
    groupId: null,
    createTime: Date.now(),
    batchTaskId: '',
    config: {
      minSampleSize: 0,
      recentSampleSize: 0,
      minMatchLength: 15,
    },
  }
}

async function doSubmit() {
  window.$dialog.warning({
    title: '提示',
    content: '功能维护中...',
    positiveText: '确定',
  })
  // formRef.value?.validate(async (errors: any) => {
  //   if (!errors) {
  //     loading.value = true
  //     useTaskSimilarityFetch().taskSimilarityBatch(formData.value).then(({ success }) => {
  //       if (success) {
  //         window.$message.success('提交成功')
  //       }
  //     })
  //     emit('submit', true)
  //     doClose()
  //     show.value = false
  //     loading.value = false
  //   }
  //   else {
  //     //
  //   }
  // })
}

const isSetCollectRef = ref(false)
const isProblemCollectRef = ref(false)
function doOpen(sId: string = '', pid: string = '', isSetCollect: boolean = false, isProblemCollect: boolean = false) {
  show.value = true
  isSetCollectRef.value = isSetCollect
  formData.value.isSet = isProblemCollect
  isProblemCollectRef.value = isProblemCollect
  formData.value.batchTaskId = `task-${uuidv4()}`
  if (!isProblemCollect && pid) {
    formData.value.problemId = [pid]
  }
  if (!isSetCollect && sId) {
    formData.value.setId = sId
  }

  useSysDictFetch().sysDictOptions({ dictType: 'ALLOW_LANGUAGE' }).then(({ data }) => {
    allowLanguageOptions.value = data
  })

  //   isEdit.value = edit
  //   formData.value = Object.assign(formData.value, row)
  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="报告参数配置">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isSetCollectRef" label="题目" path="setId">
          <NSelect
            v-model:value="formData.setId"
            placeholder="请选择题集"
            clearable
            remote
          />
        </NFormItem>
        <NFormItem v-if="!isSetCollectRef && isProblemCollectRef" label="题目" path="problemId">
          <NSelect
            v-model:value="formData.problemId"
            placeholder="请选择题目"
            clearable
            multiple
            remote
          />
        </NFormItem>
        <NFormItem label="检测语言" path="allowedLanguages">
          <NSelect
            v-model:value="formData.language"
            placeholder="请选择开放语言"
            :options="allowLanguageOptions"
            clearable
            remote
          />
        </NFormItem>
        <NFormItem label="敏感度" path="config.minMatchLength">
          <NInputNumber v-model:value="formData.config.minMatchLength" placeholder="请输入敏感度" />
        </NFormItem>

        <NFormItem label="用户组" path="groupId">
          <n-tree-select
            v-model:value="formData.groupId"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            取消
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            开始检测
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
