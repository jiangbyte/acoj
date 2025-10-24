<script lang="ts" setup>
import { useSysConfigFetch } from '@/composables/v1'

interface SysConfigItem {
  id: string
  configType: string
  name: string
  code: string
  value: string
  componentType: string
  description: string
  currentValue?: any
}

const listData = ref<SysConfigItem[]>([])
const listParam = ref({
  keyword: '',
})
const formRef = ref()
const rules = {
  keyword: [],
}

const { sysConfigList, sysConfigEdit } = useSysConfigFetch()

const loading = ref(false)
const savingIds = ref<Set<string>>(new Set())

async function loadData() {
  loading.value = true
  const { data } = await sysConfigList(listParam.value)
  if (data) {
    // configType 为 '5' 的项（系统配置）
    const filteredColumns = data.filter((col: any) => col.configType === '5')
    listData.value = filteredColumns.map((item: SysConfigItem) => ({
      ...item,
      // 初始化每个项目的当前值，根据组件类型进行数据转换
      currentValue: convertValueForComponent(item.value, item.componentType),
    }))
    loading.value = false
  }
}

// 根据组件类型转换值
function convertValueForComponent(value: string, componentType: string): any {
  switch (componentType) {
    case '1': // 单选框
      return value === 'true' || value === '1'
    case '4': // 数字输入
    case '5': // 数字输入
      return value ? Number(value) : null
    case '6': // 文件上传
      return value || ''
    default:
      return value || ''
  }
}

// 将值转换回字符串格式用于保存
function convertValueForSave(value: any, componentType: string): string {
  switch (componentType) {
    case '1': // 单选框
      return value ? 'true' : 'false'
    case '4': // 数字输入
    case '5': // 数字输入
      return value?.toString() || ''
    case '6': // 文件上传
      return value || ''
    default:
      return value || ''
  }
}

loadData()

async function doSubmit(item: SysConfigItem) {
  savingIds.value.add(item.id)

  const submitData = {
    id: item.id,
    value: convertValueForSave(item.currentValue, item.componentType),
  }

  try {
    const { success } = await sysConfigEdit(submitData)
    if (success) {
      window.$message.success(`${item.name} 修改成功`)
      // 更新原始值
      item.value = submitData.value
    }
  }
  catch (error) {
    console.error('保存失败:', error)
    window.$message.error(`${item.name} 修改失败`)
  }
  finally {
    savingIds.value.delete(item.id)
  }
}

// 检查值是否有变化
function hasValueChanged(item: SysConfigItem): boolean {
  const originalValue = convertValueForComponent(item.value, item.componentType)
  return item.currentValue !== originalValue
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NCard size="small" class="flex-1">
      <NScrollbar class="h-[calc(100vh-9.5rem)]">
        <n-form
          ref="formRef"
          :rules="rules"
          label-placement="top"
          class="max-w-150"
        >
          <n-grid
            cols="1"
            :x-gap="24"
            :y-gap="16"
            responsive="screen"
          >
            <n-gi
              v-for="item in listData"
              :key="item.id"
            >
              <n-card size="small" class="w-full">
                <n-form-item :show-label="false" :path="`config_${item.id}`">
                  <div class="flex flex-col gap-3">
                    <div class="flex-1">
                      <!-- 单选框组 -->
                      <NRadioGroup
                        v-if="item.componentType === '1'"
                        v-model:value="item.currentValue"
                      >
                        <NRadio :value="true">
                          是
                        </NRadio>
                        <NRadio :value="false">
                          否
                        </NRadio>
                      </NRadioGroup>

                      <!-- 文本输入 -->
                      <n-input
                        v-else-if="item.componentType === '2'"
                        v-model:value="item.currentValue"
                        :placeholder="`请输入${item.name}`"
                      />

                      <!-- 文本域 -->
                      <n-input
                        v-else-if="item.componentType === '3'"
                        v-model:value="item.currentValue"
                        type="textarea"
                        :placeholder="`请输入${item.name}`"
                        :rows="3"
                      />

                      <!-- 数字输入 -->
                      <n-input-number
                        v-else-if="item.componentType === '4'"
                        v-model:value="item.currentValue"
                        :placeholder="`请输入${item.name}`"
                        class="w-full"
                      />

                      <!-- 数字输入（另一种类型） -->
                      <n-input-number
                        v-else-if="item.componentType === '5'"
                        v-model:value="item.currentValue"
                        :placeholder="`请输入${item.name}`"
                        :step="0.01"
                        class="w-full"
                      />

                      <!-- 文件上传 -->
                      <FileUpload
                        v-else-if="item.componentType === '6'"
                        v-model="item.currentValue"
                        :is-image="true"
                      />

                      <!-- 未知组件类型的回退 -->
                      <n-input
                        v-else
                        v-model:value="item.currentValue"
                        :placeholder="`请输入${item.name}`"
                      />
                    </div>
                  </div>
                </n-form-item>
                <template #header>
                  {{ item.name }}
                </template>
                <template #footer>
                  <n-ellipsis :line-clamp="1">
                    {{ item.description ? item.description : '暂无描述' }}
                  </n-ellipsis>
                </template>
                <template #header-extra>
                  <n-button
                    type="primary"
                    :loading="savingIds.has(item.id)"
                    :disabled="!hasValueChanged(item)"
                    @click="doSubmit(item)"
                  >
                    {{ savingIds.has(item.id) ? '保存中...' : '保存' }}
                  </n-button>
                </template>
              </n-card>
            </n-gi>
          </n-grid>
        </n-form>
      </NScrollbar>
    </NCard>
  </div>
</template>

<style scoped>
/* .config-item {
  transition: all 0.3s ease;
}

.config-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
} */
</style>
