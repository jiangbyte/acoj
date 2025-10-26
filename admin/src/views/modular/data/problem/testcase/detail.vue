<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NSpin, NTime } from 'naive-ui'
import { useDataTestCaseFetch } from '@/composables/v1'

const emit = defineEmits(['close'])

// 懒加载重量级组件
const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
  // loader: () =>
  //   new Promise((resolve) => {
  //     // 模拟 3 秒延迟
  //     setTimeout(() => {
  //       resolve(import('@/components/common/editor/code/CodeEditor.vue'))
  //     }, 3000)
  //   }),
  loadingComponent: {
    setup() {
      return () => h('div', {
        class: 'h-full p-4',
        style: {
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        },
      }, [
        h(NSpin, { size: 'small', description: '代码预览器加载中...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const show = ref(false)
const { dataTestCaseDefaultData } = useDataTestCaseFetch()
const formData = ref<any>({ ...dataTestCaseDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataTestCaseDefaultData }
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
        <NDescriptionsItem label="题目">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用例标识">
          {{ formData.caseSign }}
        </NDescriptionsItem>
        <NDescriptionsItem label="输入数据">
          <!-- {{ formData.inputData }} -->
          <CodeEditor :model-value="formData.inputData" height="200px" />
        </NDescriptionsItem>
        <NDescriptionsItem label="期望输出">
          <CodeEditor :model-value="formData.expectedOutput" height="200px" />
          <!-- {{ formData.expectedOutput }} -->
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="输入文件路径">
          {{ formData.inputFilePath }}
        </NDescriptionsItem>
        <NDescriptionsItem label="输入文件大小">
          {{ formData.inputFileSize }}
        </NDescriptionsItem>
        <NDescriptionsItem label="输出文件路径">
          {{ formData.outputFilePath }}
        </NDescriptionsItem>
        <NDescriptionsItem label="输出文件大小">
          {{ formData.outputFileSize }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否样例">
          {{ formData.isSampleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分值">
          {{ formData.score }}
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
