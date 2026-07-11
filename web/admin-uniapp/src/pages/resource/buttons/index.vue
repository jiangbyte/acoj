<template>
  <Layout title="按钮权限" back>
    <template #right>
      <u-icon name="plus" @click="openCreate"></u-icon>
    </template>
    <view>
      <u-card :title="resourceName || '资源'">
        <template #body>
          <u-cell-group :border="false">
            <u-cell-item
              v-for="item in records"
              :key="item.id"
              :title="item.name"
              :label="`${item.code || '-'} ${item.permission_key || ''}`"
              :arrow="false"
            >
              <view class="button-actions">
                <u-button text="编辑" plain @click="openEdit(item)"></u-button>
                <u-button text="删除" plain @click="remove(item)"></u-button>
              </view>
            </u-cell-item>
          </u-cell-group>
          <u-empty
            v-if="!records.length"
            mode="list"
            text="暂无按钮权限"
          ></u-empty>
        </template>
      </u-card>
    </view>

    <u-popup
      :show="formVisible"
      mode="bottom"
      :safe-area-inset-bottom="true"
      @close="formVisible = false"
    >
      <u-card :title="editingId ? '编辑按钮权限' : '新增按钮权限'">
        <template #body>
          <FormFields
            v-model="form"
            :fields="fields"
            :mode="editingId ? 'update' : 'create'"
          />
        </template>
        <template #foot>
          <u-button
            text="保存"
            type="primary"
            :loading="loading"
            @click="save"
          ></u-button>
        </template>
      </u-card>
    </u-popup>
  </Layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import FormFields from '@/components/common/FormFields.vue'
import { resourceApi } from '@/api'
import type { FieldConfig } from '@/config/resource'

const resourceId = ref('')
const resourceName = ref('')
const records = ref<any[]>([])
const formVisible = ref(false)
const loading = ref(false)
const editingId = ref('')
const form = ref<Record<string, any>>({})
const fields: FieldConfig[] = [
  { prop: 'name', label: '按钮名称', required: true },
  { prop: 'code', label: '按钮编码', required: true },
  { prop: 'permission_key', label: '权限标识', required: true },
  {
    prop: 'data_scope',
    label: '数据范围',
    type: 'select',
    dictCode: 'DATA_SCOPE',
    defaultValue: 'SELF',
  },
  { prop: 'sort', label: '排序', type: 'number', defaultValue: 99 },
  {
    prop: 'status',
    label: '状态',
    type: 'radio',
    dictCode: 'COMMON_STATUS',
    defaultValue: 'ENABLED',
  },
  { prop: 'description', label: '描述', type: 'textarea' },
]

onLoad(async (query: any) => {
  resourceId.value = query.resourceId || ''
  resourceName.value = decodeURIComponent(query.name || '')
  await loadButtons()
})

async function loadButtons() {
  const page = await resourceApi.buttonPage({
    parent_id: resourceId.value,
    current: 1,
    size: 100,
  })
  records.value = page.records ?? []
}

function openCreate() {
  editingId.value = ''
  form.value = {
    parent_id: resourceId.value,
    data_scope: 'SELF',
    sort: 99,
    status: 'ENABLED',
  }
  formVisible.value = true
}

function openEdit(item: any) {
  editingId.value = item.id
  form.value = { ...item }
  formVisible.value = true
}

async function save() {
  loading.value = true
  try {
    const payload = { ...form.value, parent_id: resourceId.value }
    if (editingId.value) {
      await resourceApi.buttonUpdate({ ...payload, id: editingId.value })
    } else {
      await resourceApi.buttonCreate(payload)
    }
    formVisible.value = false
    await loadButtons()
  } finally {
    loading.value = false
  }
}

function remove(item: any) {
  uni.showModal({
    title: '确认删除',
    content: `删除 ${item.name}？`,
    success: async (res) => {
      if (res.confirm) {
        await resourceApi.buttonRemove({ ids: [item.id] })
        await loadButtons()
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.button-actions {
  display: flex;
  gap: 8px;
}
</style>
