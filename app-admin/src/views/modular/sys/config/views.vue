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
    // configType items with value 5 (system configuration)
    const filteredColumns = data.filter((col: any) => col.configType === '5')
    listData.value = filteredColumns.map((item: SysConfigItem) => ({
      ...item,
      // Initialize each item value and convert it based on component type
      currentValue: convertValueForComponent(item.value, item.componentType),
    }))
    loading.value = false
  }
}

// Convert values by component type
function convertValueForComponent(value: string, componentType: string): any {
  switch (componentType) {
    case '1': // Radio
      return value === 'true' || value === '1'
    case '4': // Number input
    case '5': // Number input
      return value ? Number(value) : null
    case '6': // FileUpload
      return value || ''
    default:
      return value || ''
  }
}

// Convert values back to strings for saving
function convertValueForSave(value: any, componentType: string): string {
  switch (componentType) {
    case '1': // Radio
      return value ? 'true' : 'false'
    case '4': // Number input
    case '5': // Number input
      return value?.toString() || ''
    case '6': // FileUpload
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
      window.$message.success(`${item.name} Updated successfully`)
      // Update the original value
      item.value = submitData.value
    }
  }
  catch (error) {
    console.error('Save failed:', error)
    window.$message.error(`${item.name} Update failed`)
  }
  finally {
    savingIds.value.delete(item.id)
  }
}

// Check whether the value changed
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
                      <!-- Radio group -->
                      <NRadioGroup
                        v-if="item.componentType === '1'"
                        v-model:value="item.currentValue"
                      >
                        <NRadio :value="true">
                          Yes
                        </NRadio>
                        <NRadio :value="false">
                          No
                        </NRadio>
                      </NRadioGroup>

                      <!-- Text input -->
                      <n-input
                        v-else-if="item.componentType === '2'"
                        v-model:value="item.currentValue"
                        :placeholder="`Enter ${item.name}`"
                      />

                      <!-- Textarea -->
                      <n-input
                        v-else-if="item.componentType === '3'"
                        v-model:value="item.currentValue"
                        type="textarea"
                        :placeholder="`Enter ${item.name}`"
                        :rows="3"
                      />

                      <!-- Number input -->
                      <n-input-number
                        v-else-if="item.componentType === '4'"
                        v-model:value="item.currentValue"
                        :placeholder="`Enter ${item.name}`"
                        class="w-full"
                      />

                      <!-- Number input（alternate type） -->
                      <n-input-number
                        v-else-if="item.componentType === '5'"
                        v-model:value="item.currentValue"
                        :placeholder="`Enter ${item.name}`"
                        :step="0.01"
                        class="w-full"
                      />

                      <!-- FileUpload -->
                      <FileUpload
                        v-else-if="item.componentType === '6'"
                        v-model="item.currentValue"
                        :is-image="true"
                      />

                      <!-- Fallback for unknown component types -->
                      <n-input
                        v-else
                        v-model:value="item.currentValue"
                        :placeholder="`Enter ${item.name}`"
                      />
                    </div>
                  </div>
                </n-form-item>
                <template #header>
                  {{ item.name }}
                </template>
                <template #footer>
                  <n-ellipsis :line-clamp="1">
                    {{ item.description ? item.description : 'No description' }}
                  </n-ellipsis>
                </template>
                <template #header-extra>
                  <n-button
                    type="primary"
                    :loading="savingIds.has(item.id)"
                    :disabled="!hasValueChanged(item)"
                    @click="doSubmit(item)"
                  >
                    {{ savingIds.has(item.id) ? 'Saving...' : 'Save' }}
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
