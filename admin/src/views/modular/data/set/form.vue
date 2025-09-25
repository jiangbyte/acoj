<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataSetFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables/v1'
import MDEditor from '@/components/common/editor/md/Editor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataSetDefaultData, dataSetAdd, dataSetEdit } = useDataSetFetch()
const formData = ref<any>({ ...dataSetDefaultData })
const rules = {
  setType: [
    { required: true, message: '请输入题集类型', type: 'number', trigger: ['input', 'blur'] },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: '请输入封面', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  ],
  categoryId: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  difficulty: [
    { required: true, message: '请输入难度', type: 'number', trigger: ['input', 'blur'] },
  ],
  exJson: [
    // { required: true, message: '请输入额外的信息', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataSetDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataSetEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await dataSetAdd(formData.value)
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

const difficultyOptions = ref()
const categoryOptions = ref()
const setTypeOptions = ref()
const { sysDictOptions } = useSysDictFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
async function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  // 获取下拉列表数据
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    // 将其中的value 转换为 number，其余保留
    difficultyOptions.value = difficultyData.map((item: any) => {
      return {
        value: Number(item.value),
        label: item.label,
      }
    })
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  const { data: setTypeData } = await sysDictOptions({ dictType: 'PROBLEM_SET_TYPE' })
  if (setTypeData) {
    setTypeOptions.value = setTypeData.map((item: any) => ({
      value: Number(item.value),
      label: item.label,
    }))
  }
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" default-width="1200" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="题集类型" path="setType">
          <!-- <NInputNumber v-model:value="formData.setType" :min="0" :max="100" placeholder="请输入题集类型" /> -->
          <NSelect
            v-model:value="formData.setType"
            placeholder="请选择分类"
            :options="setTypeOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="封面" path="cover">
          <!-- <NInput v-model:value="formData.cover" placeholder="请输入封面" /> -->
          <FileUpload v-model="formData.cover" :is-image="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="描述" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="请输入描述" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="分类" path="categoryId">
          <!-- <NInput v-model:value="formData.categoryId" placeholder="请输入分类" /> -->
          <NSelect
            v-model:value="formData.categoryId"
            placeholder="请选择分类"
            :options="categoryOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="难度" path="difficulty">
          <!-- <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="请输入难度" /> -->
          <NSelect
            v-model:value="formData.difficulty"
            placeholder="请选择难度"
            :options="difficultyOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="开始时间" path="startTime">
          <NDatePicker v-model:value="formData.startTime" type="datetime" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="结束时间" path="endTime">
          <NDatePicker v-model:value="formData.endTime" type="datetime" />
        </NFormItem>
        <!-- 输入框 -->
        <!-- <NFormItem label="额外的信息" path="exJson">
          <NInput v-model:value="formData.exJson" placeholder="请输入额外的信息" />
        </NFormItem> -->
        <!-- Boolean 选择框 -->
        <NFormItem label="是否可见" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否使用AI" path="useAi">
          <NRadioGroup v-model:value="formData.useAi">
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
