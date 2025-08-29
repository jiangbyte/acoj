<script lang="ts" setup>
import { useProSetFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables'
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import MDEditor from '@/components/common/editor/md/Editor.vue'

const emit = defineEmits(['close', 'submit'])
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysDictOptions } = useSysDictFetch()

const categoryOptions = ref()
const difficultyOptions = ref()
const setTypeOptions = ref()

const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proSetDefaultData, proSetAdd, proSetEdit } = useProSetFetch()
const formData = ref<any>({ ...proSetDefaultData })
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
  config: [
    // { required: true, message: '请输入配置信息', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proSetEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proSetAdd(formData.value)
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

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  // 获取难度下拉列表数据
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    difficultyOptions.value = difficultyData.map((item: any) => ({
      value: Number(item.value),
      label: item.label,
    }))
  }

  // 获取题集类型下拉列表数据
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
  <NDrawer v-model:show="show" placement="right" resizable :default-width="1200" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="题集类型" path="setType">
          <NSelect
            v-model:value="formData.setType"
            placeholder="请选择类型"
            :options="setTypeOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="封面" path="cover">
          <FileUpload v-model="formData.cover" :is-image="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="描述" path="description">
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="分类" path="categoryId">
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
          <!-- <NDatePicker type="datetime" v-model:value="formData.startTime" /> -->
          <n-date-picker v-model:value="formData.startTime" type="datetime" clearable />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="结束时间" path="endTime">
          <!-- <NDatePicker type="datetime" v-model:value="formData.endTime" /> -->
          <n-date-picker v-model:value="formData.startTime" type="datetime" clearable />
        </NFormItem>
        <!--        &lt;!&ndash; 输入框 &ndash;&gt; -->
        <!--        <NFormItem label="配置信息" path="config"> -->
        <!--          <NInput v-model:value="formData.config" placeholder="请输入配置信息" /> -->
        <!--        </NFormItem> -->
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
