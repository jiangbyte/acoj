<script setup lang="ts">
// import { useFile } from '@/composables'
import type { ToolbarNames, UploadImgCallBack } from 'md-editor-v3'
import { MdEditor } from 'md-editor-v3'
// https://imzbf.github.io/md-editor-v3/zh-CN/docs
import 'md-editor-v3/lib/style.css'

const model = defineModel<string>()

// const { upload } = useFile()

const toolbarsExclude: ToolbarNames[] = [
  'mermaid',
  'katex',
  'github',
  'htmlPreview',
  'catalog',
]

async function uploadImage(files: Array<File>, callBack: UploadImgCallBack) {
  // // Iterate over the file array
  // for (let i = 0; i < files.length; i++) {
  //   const file = files[i]
  //   const formData = new FormData()
  //   formData.append('file', file)
  //   const { data, success } = await upload(formData)
  //   if (success && data) {
  //     window.$message.success('Successfully upload')
  //     console.log(data) // Get the uploaded image URL

  //     const imageData = {
  //       url: data,
  //       alt: data,
  //       title: data,
  //     }

  //     callBack(Array.of(imageData))
  //   }
  //   else {
  //     window.$message.error('Fail to upload')
  //   }
  // }
}
</script>

<template>
  <MdEditor
    v-model="model" :toolbars-exclude="toolbarsExclude" @upload-img="uploadImage"
  />
</template>

<style scoped></style>
