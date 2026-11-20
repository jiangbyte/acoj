<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataProblemFetch, useSysCategoryFetch, useSysDictFetch, useSysTagFetch } from '@/composables/v1'
import MDEditor from '@/components/common/editor/md/MarkdownEditor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataProblemDefaultData, dataProblemAdd, dataProblemEdit } = useDataProblemFetch()
const formData = ref<any>({ ...dataProblemDefaultData })
const rules = {
  displayId: [
    // { required: true, message: '请输入展示ID', trigger: ['input', 'blur'] },
  ],
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
  allowedLanguages: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择语言',
  }],
  tagIds: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择标签',
  }],
  difficulty: [
    { required: true, message: '请输入难度', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: '请输入阈值', type: 'number', trigger: ['input', 'blur'] },
  ],
  codeTemplate: [
    // { required: true, message: '请输入模板代码', trigger: ['input', 'blur'] },
  ],
  solved: [
    // { required: true, message: '请输入解决', type: 'number', trigger: ['input', 'blur'] },
  ],
  // useAi: [{
  //   required: true,
  //   trigger: 'change',
  //   message: '请选择',
  // }],
  // isPublic: [{
  //   required: true,
  //   trigger: 'change',
  //   message: '请选择',
  // }],
  // isVisible: [{
  //   required: true,
  //   trigger: 'change',
  //   message: '请选择',
  // }],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataProblemDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataProblemEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await dataProblemAdd(formData.value)
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

const { sysDictOptions } = useSysDictFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysTagOptions } = useSysTagFetch()
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

function handleTagUpdate(newValue: string[], option: any) {
  if (newValue.length > 2) {
    window.$message.warning(`最多只能选择 ${2} 个标签`)
    // 截断
    formData.value.tagIds = formData.value.tagIds.slice(0, 2)
    return
  }
  formData.value.tagIds = newValue
}
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
        <!-- <NFormItem label="展示ID" path="displayId">
          <NInput v-model:value="formData.displayId" placeholder="请输入展示ID" />
        </NFormItem> -->
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
        <NFormItem label="标签" path="tagIds">
          <!-- <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="请输入难度" /> -->
          <NSelect
            v-model:value="formData.tagIds"
            placeholder="请选择标签"
            :options="tagOptions"
            clearable
            multiple
            remote
            @update:value="handleTagUpdate"
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
          <NInputNumber v-model:value="formData.maxTime" :min="0" :max="1000000" placeholder="请输入时间限制" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="内存限制" path="maxMemory">
          <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="1000000" placeholder="请输入内存限制" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="描述" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="请输入描述" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- 输入框 -->
        <!-- <NFormItem label="测试用例" path="testCase">
          <n-alert type="warning">
            即将舍弃使用
          </n-alert>
          <NDynamicInput
            v-model:value="formData.testCase"
            show-sort-button
            @create="() => ({
              input: '',
              output: '',
            })"
          >
            <template #default="{ value, index }">
              <div style="display: flex; flex-direction: column; width: 100%; gap: 12px;">
                <div>
                  <n-tag type="warning">
                    # {{ index + 1 }}
                  </n-tag>
                </div>
                <div style="display: flex; align-items: center; width: 100%; gap: 12px;">
                  <NInput
                    v-model:value="value.input" type="textarea" placeholder="请输入用例输入" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                  <NInput
                    v-model:value="value.output" type="textarea" placeholder="请输入用例输出" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                </div>
              </div>
            </template>
          </NDynamicInput>
        </NFormItem> -->
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
        <NFormItem label="阈值" path="threshold">
          <!-- <NInputNumber v-model:value="formData.threshold" :min="0" :max="1.0" placeholder="请输入阈值" :step="0.1" :precision="1" /> -->
          <NSlider
            v-model:value="formData.threshold"
            :step="0.1"
            :min="0.5"
            :max="1.0"
            :marks="{ 0.5: '0.5', 0.8: '0.8', 1.0: '1.0' }"
          />
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
        <!-- Boolean 选择框 -->
        <!-- <NFormItem label="使用模板" path="useTemplate">
          <NRadioGroup v-model:value="formData.useTemplate">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem> -->
        <!-- 输入框 -->
        <!-- <NFormItem label="模板代码" path="codeTemplate">
          <NDynamicInput
            v-model:value="formData.codeTemplate"
            show-sort-button
            @create="() => ({
              language: null,
              prefix: '',
              template: '',
              suffix: '',
            })"
          >
            <template #default="{ value }">
              <div style="display: flex; width: 100%; flex-direction: column; gap: 12px;">
                <NSelect
                  v-model:value="value.language"
                  placeholder="请选择语言"
                  :options="allowLanguageOptions"
                  clearable
                  remote
                />
                <div style="display: flex; align-items: center; width: 100%; gap: 12px;">
                  <NInput
                    v-model:value="value.prefix" type="textarea" placeholder="请输入模板前缀" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                  <NInput
                    v-model:value="value.template" type="textarea" placeholder="请输入模板内容" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                  <NInput
                    v-model:value="value.suffix" type="textarea" placeholder="请输入模板后缀" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                </div>
              </div>
            </template>
          </NDynamicInput>
        </NFormItem> -->
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
        <!-- Boolean 选择框 -->
        <NFormItem label="是否上架" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <n-alert type="warning" class="mb-4">
          <n-tag type="primary" size="small">
            公开
          </n-tag> 与 <n-tag type="primary" size="small">
            上架
          </n-tag> 处理 Web 题库显示，均开启时显示该题。<n-tag type="primary" size="small">
            公开
          </n-tag> 标记是否为题集中私有题目。题集组题时受是否<n-tag type="primary" size="small">
            上架
          </n-tag> 标记限制。
        </n-alert>
        <!-- 数字输入 -->
        <!-- <NFormItem label="解决" path="solved">
          <NInputNumber v-model:value="formData.solved" :min="0" :max="100" placeholder="请输入解决" />
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
