<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useTaskSimilarityFetch } from '@/composables/v1'
import { FormatModuleType } from '@/utils'
import CodeCompare from '@/components/similarity/CodeCompare.vue'

const emit = defineEmits(['close'])
const show = ref(false)
const { taskSimilarityDefaultData, taskSimilarityDetail } = useTaskSimilarityFetch()
const formData = ref<any>({ ...taskSimilarityDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskSimilarityDefaultData }
}

async function doOpen(row: any) {
  show.value = true
  formData.value = row
  if (row?.id) {
    const { data } = await taskSimilarityDetail({ id: row.id })
    if (data)
      formData.value = { ...row, ...data }
  }
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="1100" @after-leave="doClose">
    <NDrawerContent title="Details">
      <CodeCompare
        :left-code="formData.submitCode"
        :right-code="formData.originCode"
        :left-tokens="formData.submitTokenTexts"
        :right-tokens="formData.originTokenTexts"
        :tiles="formData.matchTiles"
        :language="formData.language"
      />
      <NDescriptions label-placement="left" bordered :column="1" class="mt-4">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Task ID">
          {{ formData.taskId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Manual">
          {{ formData.taskTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Problem">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission Module">
          {{ FormatModuleType(formData.moduleType) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Language">
          {{ formData.languageName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Similarity">
          {{ formData.similarity }}
        </NDescriptionsItem>
        <NDescriptionsItem label="SubmissionUser">
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
        <NDescriptionsItem label="Source Code">
          <!-- {{ formData.submitCode }} -->
          <NCode
            :code="formData.submitCode"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Source Length">
          {{ formData.submitCodeLength }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission ID">
          {{ formData.submitId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submitted At">
          <NTime :time="Number(formData.submitTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission User Token Name">
          <!-- {{ formData.submitTokenName }} -->
          <NCode
            :code="JSON.stringify(formData.submitTokenName, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission User Token Content">
          <!-- {{ formData.submitTokenTexts }} -->
          <NCode
            :code="JSON.stringify(formData.submitTokenTexts, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample User">
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
        <NDescriptionsItem label="Sample Source Code">
          <!-- {{ formData.originCode }} -->
          <NCode
            :code="formData.originCode"
            :language="formData.language"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample Source Length">
          {{ formData.originCodeLength }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample Submission ID">
          {{ formData.originId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample Submitted At">
          <NTime :time="Number(formData.originTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample User Token Name">
          <!-- {{ formData.originTokenName }} -->
          <NCode
            :code="JSON.stringify(formData.originTokenName, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Sample User Token Content">
          <!-- {{ formData.originTokenTexts }} -->
          <NCode
            :code="JSON.stringify(formData.originTokenTexts, null)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
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
        <NDescriptionsItem label="Updated At">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated By">
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
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
