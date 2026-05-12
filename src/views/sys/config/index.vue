<template>
  <a-card size="small">
    <a-tabs v-model:active-key="activeKey" size="small">
      <a-tab-pane key="sysBase" tab="系统基础配置">
        <sys-base />
      </a-tab-pane>
      <a-tab-pane key="security" tab="安全配置">
        <security />
      </a-tab-pane>
      <a-tab-pane key="fileConfig" tab="文件配置">
        <file-config />
      </a-tab-pane>
      <a-tab-pane key="bizConfig" tab="其他配置">
        <biz-config />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysConfig' })
import { ref } from 'vue'
import SysBase from './components/sysBase.vue'
import Security from './components/security.vue'
import FileConfig from './components/fileConfig/index.vue'
import BizConfig from './components/bizConfig/index.vue'

const activeKey = ref('sysBase')
</script>
