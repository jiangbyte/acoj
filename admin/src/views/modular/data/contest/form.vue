<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataContestFetch, useSysDictFetch } from '@/composables/v1'
import MDEditor from '@/components/common/editor/md/MarkdownEditor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataContestDefaultData, dataContestAdd, dataContestEdit } = useDataContestFetch()
const formData = ref<any>({ ...dataContestDefaultData })
const rules = {
  title: [
    { required: true, message: '请输入竞赛标题', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入竞赛描述', trigger: ['input', 'blur'] },
  ],
  contestType: [
    // { required: true, message: '请输入竞赛类型: ACM/OI/OIO', trigger: ['input', 'blur'] },
  ],
  ruleType: [
    { required: true, message: '请输入规则类型', trigger: ['input', 'blur'] },
  ],
  category: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: '请输入封面', trigger: ['input', 'blur'] },
  ],
  maxTeamMembers: [
    { required: true, message: '请输入最大团队成员数', type: 'number', trigger: ['input', 'blur'] },
  ],
  password: [
    { required: true, message: '请输入访问密码', trigger: ['input', 'blur'] },
  ],
  frozenTime: [
    { required: true, message: '请输入封榜时间(分钟)', type: 'number', trigger: ['input', 'blur'] },
  ],
  penaltyTime: [
    { required: true, message: '请输入罚时(分钟)', type: 'number', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择语言',
  }],
  status: [
    { required: true, message: '请输入状态', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataContestDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataContestEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await dataContestAdd(formData.value)
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

const allowLanguageOptions = ref()
const contestTypeOptions = ref()
const { sysDictOptions } = useSysDictFetch()
async function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  const { data: allowLanguageData } = await sysDictOptions({ dictType: 'ALLOW_LANGUAGE' })
  if (allowLanguageData) {
    allowLanguageOptions.value = allowLanguageData
  }

  const { data: contestTypeData } = await sysDictOptions({ dictType: 'CONTEST_TYPE' })
  if (contestTypeData) {
    contestTypeOptions.value = contestTypeData
  }
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" :default-width="800" resizable @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="竞赛标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入竞赛标题" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="竞赛描述" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="请输入竞赛描述" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="竞赛类型" path="contestType">
          <!-- <NInput v-model:value="formData.contestType" placeholder="请输入竞赛类型" /> -->
          <NSelect
            v-model:value="formData.contestType"
            placeholder="请选择竞赛类型"
            :options="contestTypeOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="规则类型" path="ruleType">
          <NInput v-model:value="formData.ruleType" placeholder="请输入规则类型" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="分类" path="category">
          <NInput v-model:value="formData.category" placeholder="请输入分类" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="封面" path="cover">
          <FileUpload v-model="formData.cover" :is-image="true" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="最大团队成员数" path="maxTeamMembers">
          <NInputNumber v-model:value="formData.maxTeamMembers" :min="0" :max="100" placeholder="请输入最大团队成员数" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否团队赛" path="isTeamContest">
          <NRadioGroup v-model:value="formData.isTeamContest">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否公开" path="isPublic">
          <NRadioGroup v-model:value="formData.isPublic">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="访问密码" path="password">
          <NInput v-model:value="formData.password" placeholder="请输入访问密码" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="报名开始时间" path="registerStartTime">
          <NDatePicker v-model:value="formData.registerStartTime" type="datetime" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="报名结束时间" path="registerEndTime">
          <NDatePicker v-model:value="formData.registerEndTime" type="datetime" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="竞赛开始时间" path="contestStartTime">
          <NDatePicker v-model:value="formData.contestStartTime" type="datetime" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="竞赛结束时间" path="contestEndTime">
          <NDatePicker v-model:value="formData.contestEndTime" type="datetime" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="封榜时间(分钟)" path="frozenTime">
          <NInputNumber v-model:value="formData.frozenTime" :min="0" :max="100" placeholder="请输入封榜时间(分钟)" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="罚时(分钟)" path="penaltyTime">
          <NInputNumber v-model:value="formData.penaltyTime" :min="0" :max="100" placeholder="请输入罚时(分钟)" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="允许语言" path="allowedLanguages">
          <NSelect
            v-model:value="formData.allowedLanguages"
            placeholder="请选择允许语言"
            :options="allowLanguageOptions"
            multiple
            clearable
            remote
          />
        </NFormItem>
        <!-- 输入框 -->
<!--        <NFormItem label="状态" path="status">-->
<!--          <NInput v-model:value="formData.status" placeholder="请输入状态" />-->
<!--        </NFormItem>-->
        <!-- 数字输入 -->
<!--        <NFormItem label="排序" path="sort">-->
<!--          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />-->
<!--        </NFormItem>-->
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
