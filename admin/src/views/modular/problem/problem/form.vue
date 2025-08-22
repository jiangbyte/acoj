<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysCategoryFetch, useProProblemFetch, useSysTagFetch, useSysDictFetch } from '@/composables'
import MDEditor from '@/components/common/editor/md/Editor.vue'
import CodeEditor from '@/components/common/editor/code/Editor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proProblemDefaultData, proProblemAdd, proProblemEdit } = useProProblemFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysDictOptions } = useSysDictFetch()
const { sysTagOptions } = useSysTagFetch()

const formData = ref<any>({ ...proProblemDefaultData })
const rules = {
  categoryId: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: ['input', 'blur'] },
  ],
  source: [
    // { required: true, message: '请输入来源', trigger: ['input', 'blur'] },
  ],
  url: [
    // { required: true, message: '请输入链接', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: '请输入时间限制', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: '请输入内存限制', type: 'number', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  ],
  testCase: [
    // { required: true, message: '请输入用例', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [
    // { required: true, message: '请输入开放语言', trigger: ['input', 'blur'] },
  ],
  difficulty: [
    { required: true, message: '请输入难度', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: '请输入阈值', type: 'number', trigger: ['input', 'blur'] },
  ],
  codeTemplate: [
    // { required: true, message: '请输入模板代码', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proProblemDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proProblemEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proProblemAdd(formData.value)
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
const allowLanguageOptions = ref()
const categoryOptions = ref()
const tagOptions = ref()
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

  const { data: allowLanguageData } = await sysDictOptions({ dictType: 'ALLOW_LANGUAGE' })
  if (allowLanguageData) {
    allowLanguageOptions.value = allowLanguageData
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  const { data: tagData } = await sysTagOptions({})
  if (tagData) {
    tagOptions.value = tagData
  }
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" :default-width="1200" resizable @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
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
        <!-- 输入框 -->
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="来源" path="source">
          <NInput v-model:value="formData.source" placeholder="请输入来源" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="链接" path="url">
          <NInput v-model:value="formData.url" placeholder="请输入链接" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="时间限制" path="maxTime">
          <NInputNumber v-model:value="formData.maxTime" :min="0" :max="10000" placeholder="请输入时间限制" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="内存限制" path="maxMemory">
          <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="10000" placeholder="请输入内存限制" />
        </NFormItem>
        <!-- 阈值输入 -->
        <NFormItem label="阈值" path="threshold">
          <NInputNumber v-model:value="formData.threshold" :min="0" :max="1.0" placeholder="请输入阈值" :step="0.1" :precision="1" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="描述" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="请输入描述" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用例" path="testCase">
          <!-- <NInput v-model:value="formData.testCase" placeholder="请输入用例" /> -->
          <NDynamicInput
            v-model:value="formData.testCase"
            @create="() => ({
              input: '',
              output: '',
            })"
          >
            <template #default="{ value }">
              <div class="flex w-full">
                <NFormItem label="输入">
                  <CodeEditor
                    v-model="value.input"
                    width="390px"
                    height="100px"
                  />
                </NFormItem>
                <NFormItem label="输出">
                  <CodeEditor
                    v-model="value.output"
                    width="390px"
                    height="100px"
                  />
                </NFormItem>
              </div>
            </template>
          </NDynamicInput>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="开放语言" path="allowedLanguages">
          <!-- <NInput v-model:value="formData.allowedLanguages" placeholder="请输入开放语言" /> -->
          <NSelect
            v-model:value="formData.allowedLanguages"
            placeholder="请选择开放语言"
            :options="allowLanguageOptions"
            multiple
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
        <!-- 数字输入 -->
        <NFormItem label="标签" path="tagIds">
          <!-- <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="请输入难度" /> -->
          <NSelect
            v-model:value="formData.tagIds"
            placeholder="请选择标签"
            :options="tagOptions"
            clearable
            multiple
            remote
          />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="使用模板" path="useTemplate">
          <NRadioGroup v-model:value="formData.useTemplate">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="模板代码" path="codeTemplate">
          <!-- <NInput v-model:value="formData.codeTemplate" placeholder="请输入模板代码" /> -->
          <NDynamicInput
            v-model:value="formData.codeTemplate"
            @create="() => ({
              language: '',
              prefix: '',
              template: '',
              suffix: '',
            })"
          >
            <template #default="{ value }">
              <div class="flex flex-col w-full">
                <NFormItem label="语言">
                  <NSelect
                    v-model:value="value.language"
                    placeholder="请选择语言"
                    :options="allowLanguageOptions"
                    clearable
                    remote
                  />
                </NFormItem>
                <div class="flex w-full">
                  <NFormItem label="前缀">
                    <CodeEditor
                      v-model="value.prefix"
                      width="280px"
                      height="100px"
                    />
                  </NFormItem>
                  <NFormItem label="模板">
                    <CodeEditor
                      v-model="value.template"
                      width="280px"
                      height="100px"
                    />
                  </NFormItem>
                  <NFormItem label="后缀">
                    <CodeEditor
                      v-model="value.suffix"
                      width="280px"
                      height="100px"
                    />
                  </NFormItem>
                </div>
              </div>
            </template>
          </NDynamicInput>
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
