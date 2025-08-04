<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useProProblemFetch } from '@/composables'
import MDViewer from '@/components/common/editor/md/Viewer.vue'

const emit = defineEmits(['close'])
const show = ref(false)
const { proProblemDefaultData } = useProProblemFetch()
const formData = ref<any>({ ...proProblemDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proProblemDefaultData }
}

function doOpen(row: any) {
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分类">
          {{ formData.categoryName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="来源">
          {{ formData.source }}
        </NDescriptionsItem>
        <NDescriptionsItem label="链接">
          {{ formData.url }}
        </NDescriptionsItem>
        <NDescriptionsItem label="时间限制">
          {{ formData.maxTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="内存限制">
          {{ formData.maxMemory }}
        </NDescriptionsItem>
        <NDescriptionsItem label="描述">
          <MDViewer :model-value="formData.description" />
        </NDescriptionsItem>
        <NDescriptionsItem label="用例">
          <NDescriptions v-for="(item, index) in formData.testCase" :key="index">
            <NDescriptionsItem :label="`用例 第${index + 1}组`">
              <NDescriptions>
                <NDescriptionsItem label="输入">
                  <NCode :code="item.input" />
                <!-- <CodeEditor
                  v-model="item.input"
                  width="390px"
                  height="100px"
                  :options="{
                    readOnly: true,
                  }"
                /> -->
                </NDescriptionsItem>
                <NDescriptionsItem label="输出">
                  <NCode :code="item.output" />
                <!-- <CodeEditor
                  v-model="item.output"
                  width="390px"
                  height="100px"
                  :options="{
                    readOnly: true,
                  }"
                /> -->
                </NDescriptionsItem>
              </NDescriptions>
            </NDescriptionsItem>
          </NDescriptions>
        </NDescriptionsItem>
        <NDescriptionsItem label="开放语言">
          <!-- {{ formData.allowedLanguages }} -->
          <NP v-for="item in formData.allowedLanguages" :key="item">
            {{ item }}
          </NP>
        </NDescriptionsItem>
        <NDescriptionsItem label="难度">
          {{ formData.difficultyName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="使用模板">
          {{ formData.useTemplateName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="模板代码">
          <!-- {{ formData.codeTemplate }} -->
          <NDescriptions v-for="(item, index) in formData.codeTemplate" :key="index">
            <NDescriptionsItem :label="`模板 第${index + 1}组`">
              <NDescriptions>
                <NDescriptionsItem label="语言">
                  {{ item.language }}
                </NDescriptionsItem>
              </NDescriptions>
              <NDescriptions>
                <NDescriptionsItem label="前缀">
                  <NCode :code="item.prefix" />
                </NDescriptionsItem>
                <NDescriptionsItem label="模板">
                  <NCode :code="item.template" />
                </NDescriptionsItem>
                <NDescriptionsItem label="后缀">
                  <NCode :code="item.suffix" />
                </NDescriptionsItem>
              </NDescriptions>
            </NDescriptionsItem>
          </NDescriptions>
        </NDescriptionsItem>
        <NDescriptionsItem label="解决">
          {{ formData.solved }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建者">
          {{ formData.createUserName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="更新时间">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="更新人">
          {{ formData.updateUserName }}
        </NDescriptionsItem>
      </NDescriptions>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
