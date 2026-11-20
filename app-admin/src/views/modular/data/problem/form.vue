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
    // { required: true, message: 'Enter Display ID', trigger: ['input', 'blur'] },
  ],
  categoryId: [
    { required: true, message: 'Enter Category', trigger: ['input', 'blur'] },
  ],
  title: [
    { required: true, message: 'Enter Title', trigger: ['input', 'blur'] },
  ],
  source: [
    // { required: true, message: 'Enter Source', trigger: ['input', 'blur'] },
  ],
  url: [
    // { required: true, message: 'Enter Link', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: 'Enter Time Limit', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: 'Enter Memory Limit', type: 'number', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: 'Enter Description', trigger: ['input', 'blur'] },
  ],
  testCase: [
    // { required: true, message: 'Enter Test Cases', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: 'Select Language',
  }],
  tagIds: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: 'Select Tag',
  }],
  difficulty: [
    { required: true, message: 'Enter Difficulty', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: 'Enter Threshold', type: 'number', trigger: ['input', 'blur'] },
  ],
  codeTemplate: [
    // { required: true, message: 'Enter Template Code', trigger: ['input', 'blur'] },
  ],
  solved: [
    // { required: true, message: 'Enter Solved', type: 'number', trigger: ['input', 'blur'] },
  ],
  // useAi: [{
  //   required: true,
  //   trigger: 'change',
  //   message: 'Select ',
  // }],
  // isPublic: [{
  //   required: true,
  //   trigger: 'change',
  //   message: 'Select ',
  // }],
  // isVisible: [{
  //   required: true,
  //   trigger: 'change',
  //   message: 'Select ',
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
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await dataProblemAdd(formData.value)
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

  // Load dropdown options
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    // Convert value to a number and keep the other fields
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
    window.$message.warning(`You can select up to ${2} tags`)
    // Truncate
    formData.value.tagIds = formData.value.tagIds.slice(0, 2)
    return
  }
  formData.value.tagIds = newValue
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" :default-width="800" resizable @after-leave="doClose">
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <!-- <NFormItem label="Display ID" path="displayId">
          <NInput v-model:value="formData.displayId" placeholder="Enter Display ID" />
        </NFormItem> -->
        <!-- Text input -->
        <NFormItem label="Category" path="categoryId">
          <!-- <NInput v-model:value="formData.categoryId" placeholder="Enter Category" /> -->
          <NSelect
            v-model:value="formData.categoryId"
            placeholder="Select Category"
            :options="categoryOptions"
            clearable
            remote
          />
        </NFormItem>
        <NFormItem label="Tag" path="tagIds">
          <!-- <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="Enter Difficulty" /> -->
          <NSelect
            v-model:value="formData.tagIds"
            placeholder="Select Tag"
            :options="tagOptions"
            clearable
            multiple
            remote
            @update:value="handleTagUpdate"
          />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Title" path="title">
          <NInput v-model:value="formData.title" placeholder="Enter Title" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Source" path="source">
          <NInput v-model:value="formData.source" placeholder="Enter Source" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Link" path="url">
          <NInput v-model:value="formData.url" placeholder="Enter Link" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Time Limit" path="maxTime">
          <NInputNumber v-model:value="formData.maxTime" :min="0" :max="1000000" placeholder="Enter Time Limit" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Memory Limit" path="maxMemory">
          <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="1000000" placeholder="Enter Memory Limit" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Description" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="Enter Description" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- Text input -->
        <!-- <NFormItem label="Test Cases" path="testCase">
          <n-alert type="warning">
            Deprecated and will be removed
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
                    v-model:value="value.input" type="textarea" placeholder="Enter Test Case Input" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                  <NInput
                    v-model:value="value.output" type="textarea" placeholder="Enter Test Case Output" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                </div>
              </div>
            </template>
          </NDynamicInput>
        </NFormItem> -->
        <!-- Text input -->
        <NFormItem label="Allowed Languages" path="allowedLanguages">
          <!-- <NInput v-model:value="formData.allowedLanguages" placeholder="Enter Allowed Languages" /> -->
          <NSelect
            v-model:value="formData.allowedLanguages"
            placeholder="Select Allowed Languages"
            :options="allowLanguageOptions"
            multiple
            clearable
            remote
          />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Difficulty" path="difficulty">
          <!-- <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="Enter Difficulty" /> -->
          <NSelect
            v-model:value="formData.difficulty"
            placeholder="Select Difficulty"
            :options="difficultyOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Threshold" path="threshold">
          <!-- <NInputNumber v-model:value="formData.threshold" :min="0" :max="1.0" placeholder="Enter Threshold" :step="0.1" :precision="1" /> -->
          <NSlider
            v-model:value="formData.threshold"
            :step="0.1"
            :min="0.5"
            :max="1.0"
            :marks="{ 0.5: '0.5', 0.8: '0.8', 1.0: '1.0' }"
          />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Use AI" path="useAi">
          <NRadioGroup v-model:value="formData.useAi">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean selector -->
        <!-- <NFormItem label="Use Template" path="useTemplate">
          <NRadioGroup v-model:value="formData.useTemplate">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem> -->
        <!-- Text input -->
        <!-- <NFormItem label="Template Code" path="codeTemplate">
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
                  placeholder="Select Language"
                  :options="allowLanguageOptions"
                  clearable
                  remote
                />
                <div style="display: flex; align-items: center; width: 100%; gap: 12px;">
                  <NInput
                    v-model:value="value.prefix" type="textarea" placeholder="Enter Template Prefix" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                  <NInput
                    v-model:value="value.template" type="textarea" placeholder="Enter Template Content" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                  <NInput
                    v-model:value="value.suffix" type="textarea" placeholder="Enter Template Suffix" :autosize="{
                      minRows: 3,
                      maxRows: 3,
                    }"
                  />
                </div>
              </div>
            </template>
          </NDynamicInput>
        </NFormItem> -->
        <!-- Boolean selector -->
        <NFormItem label="Public" path="isPublic">
          <NRadioGroup v-model:value="formData.isPublic">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
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
        <n-alert type="warning" class="mb-4">
          <n-tag type="primary" size="small">
            Public
          </n-tag> and <n-tag type="primary" size="small">
            Published
          </n-tag> control display in the web problem library. The problem appears when both are enabled.<n-tag type="primary" size="small">
            Public
          </n-tag> marks a problem as private to a set. Set composition is restricted by the<n-tag type="primary" size="small">
            Published
          </n-tag> setting.
        </n-alert>
        <!-- Number input -->
        <!-- <NFormItem label="Solved" path="solved">
          <NInputNumber v-model:value="formData.solved" :min="0" :max="100" placeholder="Enter Solved" />
        </NFormItem> -->
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
