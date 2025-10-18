<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataProblemFetch } from '@/composables/v1'
import MDViewer from '@/components/common/editor/md/Viewer.vue'
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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="展示ID">
          {{ formData.displayId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分类">
          {{ formData.categoryId }}
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
          <!-- {{ formData.testCase }} -->
          <NCode
            :code="JSON.stringify(formData.testCase, null, 2)"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="开放语言">
          <!-- {{ formData.allowedLanguages }} -->
          <n-space align="center">
            <n-tag v-for="(item, index) in FormatLanguages(formData.allowedLanguages)" :key="index">
              {{ item }}
            </n-tag>
          </n-space>
        </NDescriptionsItem>
        <NDescriptionsItem label="难度">
          {{ formData.difficultyName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="阈值">
          {{ formData.threshold }}
        </NDescriptionsItem>
        <NDescriptionsItem label="使用模板">
          {{ formData.useTemplateName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="模板代码">
          <n-space vertical>
            <n-card v-for="(item, index) in formData.codeTemplate" :key="index" size="small">
              <template #header>
                {{ item.language.charAt(0).toUpperCase() + item.language.slice(1).toLowerCase() }} 语言
              </template>

              <NSpace vertical size="large">
                <!-- 前置代码 -->
                <n-card v-if="item.prefix" size="small" title="前置代码">
                  <NCode
                    :code="item.prefix"
                    :language="item.language"
                    word-wrap
                    show-line-numbers
                  />
                </n-card>

                <!-- 模板代码 -->
                <n-card v-if="item.template" size="small" title="模板代码">
                  <NCode
                    :code="item.template"
                    :language="item.language"
                    word-wrap
                    show-line-numbers
                  />
                </n-card>

                <!-- 后置代码 -->
                <n-card v-if="item.suffix" size="small" title="后置代码">
                  <NCode
                    :code="item.suffix"
                    :language="item.language"
                    word-wrap
                    show-line-numbers
                  />
                </n-card>

                <!-- 如果没有代码 -->
                <n-empty
                  v-if="!item.prefix && !item.template && !item.suffix"
                  size="small"
                  description="暂无代码模板"
                />
              </NSpace>
            </n-card>
          </n-space>
        </NDescriptionsItem>
        <NDescriptionsItem label="是否公开">
          {{ formData.isPublicName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="上架">
          {{ formData.isVisibleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="使用AI">
          {{ formData.useAiName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="解决">
          {{ formData.solved }}
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
