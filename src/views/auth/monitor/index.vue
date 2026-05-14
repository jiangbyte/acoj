<template>
  <div class="flex flex-col gap-2">
    <SessionAnalyse />
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="b" tab="B端在线用户">
          <BSessionTab />
        </a-tab-pane>
        <a-tab-pane key="c" tab="C端在线用户">
          <CSessionTab />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SysAuthMonitor' })
import { ref } from 'vue'
import SessionAnalyse from './analyse.vue'
import BSessionTab from './bTab.vue'
import CSessionTab from './cTab.vue'

const activeKey = ref('b')
</script>
