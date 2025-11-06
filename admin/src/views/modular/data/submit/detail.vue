<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataSubmitFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataSubmitDefaultData } = useDataSubmitFetch()
const formData = ref<any>({ ...dataSubmitDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataSubmitDefaultData }
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
        <NDescriptionsItem label="用户">
          <!-- {{ formData.userId }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.userAvatar"
            />
            <NText>{{ formData.userIdName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="提交模块">
          {{ formData.moduleTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="编程语言">
          {{ formData.languageName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="源代码">
          <NCode
            :code="formData.code"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="源代码长度">
          {{ formData.codeLength }}
        </NDescriptionsItem>
        <NDescriptionsItem label="执行类型">
          {{ formData.submitTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最大耗时">
          {{ formData.maxTime }} ms
        </NDescriptionsItem>
        <NDescriptionsItem label="最大内存使用">
          {{ formData.maxMemory }} KB
        </NDescriptionsItem>
        <NDescriptionsItem label="执行结果消息">
          {{ formData.message }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="测试用例结果">
          <NCode
            :code="JSON.stringify(formData.testCase, null, 2)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem> -->
        <NDescriptionsItem label="执行状态">
          {{ formData.statusName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="流程流转完成">
          {{ formData.isFinishName }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="相似度">
          {{ formData.similarity * 100 }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似检测任务ID">
          {{ formData.taskId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="报告ID">
          {{ formData.reportId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似分级">
          {{ formData.similarityCategoryName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似检测任务ID">
          {{ formData.judgeTaskId }}
        </NDescriptionsItem> -->
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
