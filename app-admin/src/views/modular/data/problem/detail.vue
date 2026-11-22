<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataProblemFetch } from '@/composables/v1'
import MDViewer from '@/components/common/editor/md/MarkdownViewer.vue'
import { FormatLanguages } from '@/utils'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataProblemDefaultData } = useDataProblemFetch()
const formData = ref<any>({ ...dataProblemDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataProblemDefaultData }
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
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Display ID">
          {{ formData.displayId }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Category">
          {{ formData.categoryId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Title">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Source">
          {{ formData.source }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Link">
          {{ formData.url }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Time Limit">
          {{ formData.maxTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Memory Limit">
          {{ formData.maxMemory }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Description">
          <MDViewer :model-value="formData.description" />
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Test Cases">
          <NCode
            :code="JSON.stringify(formData.testCase, null, 2)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Allowed Languages">
          <!-- {{ formData.allowedLanguages }} -->
          <n-space align="center">
            <n-tag v-for="(item, index) in FormatLanguages(formData.allowedLanguages)" :key="index">
              {{ item }}
            </n-tag>
          </n-space>
        </NDescriptionsItem>
        <NDescriptionsItem label="Difficulty">
          {{ formData.difficultyName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Threshold">
          {{ formData.threshold }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Use Template">
          {{ formData.useTemplateName }}
        </NDescriptionsItem> -->
        <!-- <NDescriptionsItem label="Template Code">
          <n-space vertical>
            <n-card v-for="(item, index) in formData.codeTemplate" :key="index" size="small">
              <template #header>
                {{ item.language.charAt(0).toUpperCase() + item.language.slice(1).toLowerCase() }} Language
              </template>

              <NSpace vertical size="large">
                <n-card v-if="item.prefix" size="small" title="Prefix Code">
                  <NCode
                    :code="item.prefix"
                    :language="item.language"
                    word-wrap
                    show-line-numbers
                  />
                </n-card>
                <n-card v-if="item.template" size="small" title="Template Code">
                  <NCode
                    :code="item.template"
                    :language="item.language"
                    word-wrap
                    show-line-numbers
                  />
                </n-card>
                <n-card v-if="item.suffix" size="small" title="Suffix Code">
                  <NCode
                    :code="item.suffix"
                    :language="item.language"
                    word-wrap
                    show-line-numbers
                  />
                </n-card>
                <n-empty
                  v-if="!item.prefix && !item.template && !item.suffix"
                  size="small"
                  description="No code template"
                />
              </NSpace>
            </n-card>
          </n-space>
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Public">
          {{ formData.isPublicName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Published">
          {{ formData.isVisibleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Use AI">
          {{ formData.useAiName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Solved">
          {{ formData.solved }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
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
            <SafeAvatar
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
