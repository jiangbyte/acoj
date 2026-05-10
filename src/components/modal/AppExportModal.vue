<template>
  <a-modal
    :open="open"
    title="导出数据"
    :width="460"
    :confirm-loading="exporting"
    destroy-on-close
    @ok="handleExport"
    @cancel="handleClose"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item label="导出范围" name="exportType">
        <a-radio-group v-model:value="form.exportType">
          <a-radio value="current">当前页数据</a-radio>
          <a-radio value="selected" :disabled="!hasSelected">
            已选数据{{ hasSelected ? ` (${selectedKeys!.length} 条)` : '' }}
          </a-radio>
          <a-radio value="all">全部数据</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="文件名称" name="filename">
        <a-input v-model:value="form.filename" placeholder="导出文件名（可选）" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'

interface ExportForm {
  exportType: string
  filename: string
}

const props = defineProps<{
  open: boolean
  selectedKeys?: string[]
}>()

const emit = defineEmits<{
  close: []
  export: [params: { export_type: string; selected_id?: string; filename?: string }]
}>()

const exporting = ref(false)
const form = reactive<ExportForm>({
  exportType: 'current',
  filename: '',
})

const hasSelected = computed(() => (props.selectedKeys?.length ?? 0) > 0)

// Reset loading state when modal is closed (by parent or user)
watch(
  () => props.open,
  val => {
    if (!val) {
      exporting.value = false
    }
  }
)

async function handleExport() {
  if (form.exportType === 'selected' && !hasSelected.value) {
    message.warning('请先选择要导出的数据')
    return
  }

  exporting.value = true
  const params: any = { export_type: form.exportType }
  if (form.exportType === 'selected') {
    params.selected_id = props.selectedKeys?.join(',')
  }
  if (form.filename) {
    params.filename = form.filename
  }
  emit('export', params)
}

function handleClose() {
  form.exportType = 'current'
  form.filename = ''
  emit('close')
}

defineExpose({
  done: () => {
    exporting.value = false
  },
})
</script>
