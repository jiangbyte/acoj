<template>
  <Layout title="部门管理" back>
    <view class="flex flex-col">
      <view class="bg-white mx-4 mt-3 rounded-lg overflow-hidden">
        <view class="flex items-center gap-2 p-2">
          <u-search v-model="keyword" placeholder="搜索部门名称/编码" :show-action="false" @search="onSearch" class="flex-1" />
          <u-button text="新增" type="primary" icon="plus" @click="openCreate" />
        </view>
      </view>

      <view v-for="item in records" :key="item.id" class="mx-4 mt-3 bg-white rounded-lg overflow-hidden">
        <view class="flex items-center justify-between px-4 py-3 border-b">
          <view class="flex flex-col">
            <text class="text-base font-bold text-gray-900">{{ item.name }}</text>
            <text class="text-xs text-gray-500">{{ item.code || '-' }} · {{ item.category || '-' }}</text>
          </view>
          <view class="flex gap-1">
            <u-button text="详情" size="mini" plain @click="openDetail(item)" />
            <u-button text="编辑" size="mini" plain @click="openEdit(item)" />
            <u-button text="删除" size="mini" plain type="error" @click="confirmDelete(item)" />
          </view>
        </view>
        <view class="px-4 py-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
          <text>排序: {{ item.sort ?? '-' }}</text>
          <text>虚拟: {{ item.is_virtual ? '是' : '否' }}</text>
          <text>状态: {{ dictLabel('COMMON_STATUS', item.status) }}</text>
          <text>{{ formatDateTime(item.updated_at) }}</text>
        </view>
      </view>
      <u-loadmore :status="loadStatus" />
    </view>

    <u-popup :show="formVisible" mode="bottom" :safe-area-inset-bottom="true" @close="formVisible = false">
      <view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;">
        <text class="block text-lg font-bold mb-4">{{ editingId ? '编辑部门' : '新增部门' }}</text>
        <u-form :model="form">
          <u-form-item label="部门名称" required><u-input v-model="form.name" placeholder="请输入名称" border="none" /></u-form-item>
          <u-form-item label="部门编码" required><u-input v-model="form.code" placeholder="请输入编码" border="none" /></u-form-item>
          <u-form-item label="分类"><u-input v-model="form.category" placeholder="请输入分类" border="none" /></u-form-item>
          <u-form-item label="上级部门ID"><u-input v-model="form.parent_id" placeholder="请输入上级部门ID" border="none" /></u-form-item>
          <u-form-item label="负责人ID"><u-input v-model="form.master_id" placeholder="请输入负责人ID" border="none" /></u-form-item>
          <u-form-item label="副负责人ID"><u-input v-model="form.deputy_master_id" placeholder="请输入副负责人ID" border="none" /></u-form-item>
          <u-form-item label="排序"><u-number-box v-model="form.sort" :min="0" /></u-form-item>
          <u-form-item label="虚拟部门"><u-switch v-model="form.is_virtual" /></u-form-item>
          <u-form-item label="状态"><view class="w-full" @click="openPicker('status', dictOptions('COMMON_STATUS'))"><u-input :value="dictLabel('COMMON_STATUS', form.status)" placeholder="请选择" disabled border="none" /></view></u-form-item>
        </u-form>
        <u-button text="保存" type="primary" :loading="saving" @click="doSave" class="mt-4" />
      </view>
    </u-popup>

    <u-popup :show="detailVisible" mode="bottom" :safe-area-inset-bottom="true" @close="detailVisible = false">
      <view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;">
        <text class="block text-lg font-bold mb-4">部门详情</text>
        <u-cell-group :border="false">
          <u-cell-item v-for="f in detailFields" :key="f" :title="detailLabel(f)" :value="displayVal(detail[f])" :arrow="false" />
        </u-cell-group>
      </view>
    </u-popup>

    <u-picker :show="pickerShow" :columns="pickerColumns" key-name="label" @confirm="onPickerConfirm" @close="pickerShow = false" />
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { deptApi } from '@/api'
import { dictList } from '@/utils/dict'
import { displayValue, formatDateTime } from '@/utils/format'
import { fallbackDicts } from '@/config/resource'

const records = ref<any[]>([])
const keyword = ref('')
const current = ref(1); const total = ref(0); const loading = ref(false)
const formVisible = ref(false); const detailVisible = ref(false); const saving = ref(false)
const editingId = ref(''); const detail = ref<any>({})
const form = ref<Record<string, any>>({})
const pickerShow = ref(false); const pickerField = ref(''); const pickerColumns = ref<any[]>([[]])
const detailFields = ['id', 'name', 'code', 'category', 'parent_id', 'master_id', 'deputy_master_id', 'sort', 'is_virtual', 'status', 'created_at', 'updated_at']
const loadStatus = computed(() => loading.value ? 'loading' : records.value.length >= total.value ? 'nomore' : 'loadmore')

onLoad(() => { refresh() })
onPullDownRefresh(async () => { await refresh(); uni.stopPullDownRefresh() })

async function refresh() { current.value = 1; await loadPage(false) }
async function loadPage(append: boolean) {
  loading.value = true
  try { const params: any = { current: current.value, size: 20 }; if (keyword.value) params.name = keyword.value; const page = await deptApi.page(params); records.value = append ? [...records.value, ...(page.records ?? [])] : (page.records ?? []); total.value = page.total ?? 0 } finally { loading.value = false }
}
function onSearch() { refresh() }
function dictOptions(code: string) { return dictList(code).length ? dictList(code) : fallbackDicts[code] ?? [] }
function dictLabel(code: string, val: any) { return dictOptions(code).find((o: any) => String(o.value) === String(val))?.label ?? val ?? '' }
function displayVal(v: any) { const dt = formatDateTime(v); return displayValue(dt !== v ? dt : v) }
function detailLabel(p: string) { const m: Record<string, string> = { id: 'ID', name: '名称', code: '编码', category: '分类', parent_id: '上级部门ID', master_id: '负责人ID', deputy_master_id: '副负责人ID', sort: '排序', is_virtual: '虚拟部门', status: '状态', created_at: '创建时间', updated_at: '更新时间' }; return m[p] ?? p }
function openPicker(field: string, options: any[]) { pickerField.value = field; pickerColumns.value = [options]; pickerShow.value = true }
function onPickerConfirm(e: any) { const v = e.value?.[0]; if (pickerField.value && v) form.value[pickerField.value] = v.value; pickerShow.value = false }
function openCreate() { editingId.value = ''; form.value = { sort: 99, status: 'ENABLED', is_virtual: false }; formVisible.value = true }
function openEdit(item: any) { editingId.value = item.id; form.value = { ...item }; formVisible.value = true }
function openDetail(item: any) { detail.value = item; detailVisible.value = true }
async function doSave() { saving.value = true; try { const p: any = { ...form.value }; if (editingId.value) { await deptApi.update({ ...p, id: editingId.value }) } else { await deptApi.create(p) }; formVisible.value = false; await refresh() } finally { saving.value = false } }
function confirmDelete(item: any) { uni.showModal({ title: '确认删除', content: `删除 ${item.name}？`, success: async (r: any) => { if (r.confirm) { await deptApi.remove({ ids: [item.id] }); await refresh() } } }) }
</script>
