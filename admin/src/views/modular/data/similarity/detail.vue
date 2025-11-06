<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useTaskSimilarityFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { taskSimilarityDefaultData } = useTaskSimilarityFetch()
const formData = ref<any>({ ...taskSimilarityDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskSimilarityDefaultData }
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
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="任务ID">
          {{ formData.taskId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="手动">
          {{ formData.taskTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交模块">
          {{ formData.moduleTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="编程语言">
          {{ formData.languageName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似度">
          {{ formData.similarity }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交用户">
          <!-- {{ formData.submitUser }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.submitUserAvatar"
            />
            <NText>{{ formData.submitUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="源代码">
          <!-- {{ formData.submitCode }} -->
          <NCode
            :code="formData.submitCode"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="源代码长度">
          {{ formData.submitCodeLength }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交ID">
          {{ formData.submitId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交时间">
          <NTime :time="Number(formData.submitTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="提交用户Token名称">
          <!-- {{ formData.submitTokenName }} -->
          <NCode
            :code="JSON.stringify(formData.submitTokenName, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="提交用户Token内容">
          <!-- {{ formData.submitTokenTexts }} -->
          <NCode
            :code="JSON.stringify(formData.submitTokenTexts, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="样本用户">
          <!-- {{ formData.originUser }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.originUserAvatar"
            />
            <NText>{{ formData.originUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="样本源代码">
          <!-- {{ formData.originCode }} -->
          <NCode
            :code="formData.originCode"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="样本源代码长度">
          {{ formData.originCodeLength }}
        </NDescriptionsItem>
        <NDescriptionsItem label="样本提交ID">
          {{ formData.originId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="样本提交时间">
          <NTime :time="Number(formData.originTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="样本用户Token名称">
          <!-- {{ formData.originTokenName }} -->
          <NCode
            :code="JSON.stringify(formData.originTokenName, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="样本用户Token内容">
          <!-- {{ formData.originTokenTexts }} -->
          <NCode
            :code="JSON.stringify(formData.originTokenTexts, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建者">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.createUserAvatar"
            />
            <NText>{{ formData.createUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="更新时间">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="更新人">
          <!-- {{ formData.updateUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.updateUserAvatar"
            />
            <NText>{{ formData.updateUserName }}</NText>
          </NSpace>
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
