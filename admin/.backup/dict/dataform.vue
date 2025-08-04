<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysDictDataFetch, useSysDictTypeFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysDictDataDefaultData, sysDictDataAdd, sysDictDataEdit } = useSysDictDataFetch()
const { sysDictTypeTreeOptions } = useSysDictTypeFetch()
const formData = ref<any>({ ...sysDictDataDefaultData })
const rules = {
  typeId: [
    { required: true, message: '请输入类型', trigger: ['input', 'blur'] },
  ],
  dictLabel: [
    { required: true, message: '请输入标签', trigger: ['input', 'blur'] },
  ],
  dictValue: [
    { required: true, message: '请输入值', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysDictDataDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysDictDataEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysDictDataAdd(formData.value)
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

function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
}
defineExpose({
  doOpen,
})

// 字典类型树数据
const treeData = ref<any[]>([])
const dataTypeKeyword = ref('')
async function loadData() {
  const { data } = await sysDictTypeTreeOptions({
    keyword: dataTypeKeyword.value,
  })
  if (data) {
    treeData.value = data
  }
}
loadData()
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="类型" path="typeId">
          <!-- <NInput v-model:value="formData.typeId" placeholder="请输入类型" /> -->
          <NTreeSelect
            v-model:value="formData.typeId"
            :options="treeData"
            label-field="label"
            key-field="value"
            placeholder="请选择类型"
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="标签" path="dictLabel">
          <NInput v-model:value="formData.dictLabel" placeholder="请输入标签" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="值" path="dictValue">
          <NInput v-model:value="formData.dictValue" placeholder="请输入值" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="默认状态" path="isDefault">
          <NRadioGroup v-model:value="formData.isDefault">
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
