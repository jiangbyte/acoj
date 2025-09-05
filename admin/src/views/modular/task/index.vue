<script lang="ts" setup>
const current = ref<number | undefined>(1)
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <n-card size="small">
      <n-steps v-model:current="current" class="w-full">
        <n-step
          title="第一步：设置检测配置"
          description="设置选择阈值、样本集范围等参数"
        />
        <n-step
          title="第二步：选择题目或题集"
          description="选择你想检测的题目或题集"
        />
        <n-step
          title="第三步：选择用户范围"
          description="选择你想检测的用户范围"
        />
        <n-step
          title="第四步：生成检测任务"
          description="生成检测任务执行中"
        />
        <n-step
          title="第五步：完成"
          description="查看生成的检测报告"
        />
      </n-steps>
    </n-card>
    <NCard size="small" class="flex-1">
      <n-tabs type="line" animated>
        <n-tab-pane name="1" tab="阈值配置">
          <n-radio-group>
            <n-radio
              value="1"
              label="全局阈值"
            />
            <n-radio
              value="2"
              label="题目阈值"
            />
          </n-radio-group>
        </n-tab-pane>
        <n-tab-pane name="2" tab="样本范围">
          Hey Jude
        </n-tab-pane>
      </n-tabs>
    </NCard>
  </div>
</template>

<style scoped>

</style>
