<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑职位' : '新增职位'"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <a-form-item label="职位编码" name="code">
        <a-input v-model:value="form.code" placeholder="留空自动生成" :disabled="isEdit" />
      </a-form-item>
      <a-form-item
        label="职位名称"
        name="name"
        :rules="[{ required: true, message: '请输入职位名称' }]"
      >
        <a-input v-model:value="form.name" placeholder="请输入职位名称" />
      </a-form-item>
      <a-form-item
        label="职位类别"
        name="category"
        :rules="[{ required: true, message: '请选择职位类别' }]"
      >
        <DictSelect
          v-model="form.category"
          type-code="POSITION_CATEGORY"
          placeholder="请选择职位类别"
        />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <DictSelect v-model="form.status" type-code="SYS_STATUS" placeholder="请选择状态" />
        <div class="text-[12px] text-gray-400 leading-tight mt-1">
          禁用后仅不可被选择，不影响已绑定的数据
        </div>
      </a-form-item>
      <a-form-item label="排序" name="sort_code">
        <a-input-number
          v-model:value="form.sort_code"
          :min="0"
          :max="9999"
          style="width: 100%"
          placeholder="排序值"
        />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入职位描述" :rows="3" />
      </a-form-item>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { fetchPositionDetail, fetchPositionCreate, fetchPositionModify } from '@/api/position'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'
import DictSelect from '@/components/form/DictSelect.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)

const initialForm = () => ({
  code: '',
  name: '',
  category: undefined,
  group_id: '0',
  status: 'ENABLED',
  sort_code: 0,
  description: '',
})

const form = reactive(initialForm())

async function doOpen(row?: any, defaultGroupId?: string) {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    const { data } = await fetchPositionDetail({ id: row.id })
    if (data) {
      Object.assign(form, data)
    }
  } else {
    isEdit.value = false
    currentId.value = null
    Object.assign(form, initialForm())
    form.group_id = defaultGroupId
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await fetchPositionModify({ ...f, id: currentId.value })
  } else {
    return await fetchPositionCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
