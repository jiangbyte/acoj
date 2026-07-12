<template>
  <Layout title="账号管理" back>
    <view class="flex flex-col">
      <view class="bg-white mx-4 mt-3 rounded-lg overflow-hidden">
        <view class="flex items-center gap-2 p-2">
          <u-search v-model="keyword" placeholder="搜索账号/姓名" :show-action="false" @search="onSearch" class="flex-1" />
          <u-button text="新增" type="primary" icon="plus" @click="openCreate" />
        </view>
      </view>

      <view v-for="item in records" :key="item.id" class="mx-4 mt-3 bg-white rounded-lg overflow-hidden">
        <view class="flex items-center justify-between px-4 py-3 border-b">
          <view class="flex flex-col">
            <text class="text-base font-bold text-gray-900">{{ item.account }}</text>
            <text class="text-xs text-gray-500">{{ item.name || '-' }} · {{ item.email || '-' }}</text>
          </view>
          <view class="flex gap-1">
            <u-button text="详情" size="mini" plain @click="openDetail(item)" />
            <u-button text="编辑" size="mini" plain @click="openEdit(item)" />
            <u-button text="删除" size="mini" plain type="error" @click="confirmDelete(item)" />
          </view>
        </view>
        <view class="px-4 py-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
          <text>类型: {{ item.account_type || '-' }}</text>
          <text>手机: {{ item.phone || '-' }}</text>
          <text>状态: {{ item.account_status || '-' }}</text>
          <text>{{ formatDateTime(item.updated_at) }}</text>
        </view>
      </view>

      <u-loadmore :status="loadStatus" />
    </view>

    <u-popup :show="formVisible" mode="bottom" :safe-area-inset-bottom="true" @close="formVisible = false">
      <view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;">
        <text class="block text-lg font-bold mb-4">{{ editingId ? '编辑账号' : '新增账号' }}</text>
        <u-form :model="form">
          <u-form-item label="账号" required><u-input v-model="form.account" placeholder="请输入账号" border="none" /></u-form-item>
          <u-form-item v-if="!editingId" label="密码" required><u-input v-model="form.password" type="password" placeholder="请输入密码" border="none" /></u-form-item>
          <u-form-item v-else label="新密码"><u-input v-model="form.password" type="password" placeholder="留空不修改" border="none" /></u-form-item>
          <u-form-item label="账号类型" required>
            <view class="w-full" @click="openPicker('account_type', dictOptions('ACCOUNT_TYPE'))"><u-input :value="dictLabel('ACCOUNT_TYPE', form.account_type)" placeholder="请选择" disabled border="none" /></view>
          </u-form-item>
          <u-form-item label="状态">
            <view class="w-full" @click="openPicker('account_status', dictOptions('ACCOUNT_STATUS'))"><u-input :value="dictLabel('ACCOUNT_STATUS', form.account_status)" placeholder="请选择" disabled border="none" /></view>
          </u-form-item>
          <u-form-item label="姓名"><u-input v-model="form.name" placeholder="请输入姓名" border="none" /></u-form-item>
          <u-form-item label="昵称"><u-input v-model="form.nickname" placeholder="请输入昵称" border="none" /></u-form-item>
          <u-form-item label="手机号"><u-input v-model="form.phone" placeholder="请输入手机号" border="none" /></u-form-item>
          <u-form-item label="邮箱"><u-input v-model="form.email" placeholder="请输入邮箱" border="none" /></u-form-item>
        </u-form>
        <u-button text="保存" type="primary" :loading="saving" @click="doSave" class="mt-4" />
      </view>
    </u-popup>

    <u-popup :show="detailVisible" mode="bottom" :safe-area-inset-bottom="true" @close="detailVisible = false">
      <view class="bg-white rounded-t-lg p-4" style="max-height: 70vh; overflow-y: auto;">
        <text class="block text-lg font-bold mb-4">账号详情</text>
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
import { accountApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { dictList, dictTypeData } from '@/utils/dict'
import { displayValue, formatDateTime } from '@/utils/format'
import { encryptPasswords } from '@/utils/security'
import { fallbackDicts } from '@/config/resource'

const authStore = useAuthStore()
const records = ref<any[]>([])
const keyword = ref('')
const current = ref(1)
const total = ref(0)
const loading = ref(false)
const formVisible = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const editingId = ref('')
const detail = ref<any>({})
const form = ref<Record<string, any>>({})
const pickerShow = ref(false)
const pickerField = ref('')
const pickerColumns = ref<any[]>([[]])

const detailFields = ['id', 'account', 'account_type', 'account_status', 'name', 'nickname', 'phone', 'email', 'title', 'employee_no', 'created_at', 'updated_at']
const loadStatus = computed(() => loading.value ? 'loading' : records.value.length >= total.value ? 'nomore' : 'loadmore')

onLoad(() => { refresh() })
onPullDownRefresh(async () => { await refresh(); uni.stopPullDownRefresh() })

async function refresh() { current.value = 1; await loadPage(false) }

async function loadPage(append: boolean) {
  loading.value = true
  try {
    const params: any = { current: current.value, size: 20 }
    if (keyword.value) params.account = keyword.value
    const page = await accountApi.page(params)
    records.value = append ? [...records.value, ...(page.records ?? [])] : (page.records ?? [])
    total.value = page.total ?? 0
  } finally { loading.value = false }
}

function onSearch() { refresh() }

function dictOptions(code: string) { return dictList(code).length ? dictList(code) : fallbackDicts[code] ?? [] }
function dictLabel(code: string, val: any) { return dictOptions(code).find((o: any) => String(o.value) === String(val))?.label ?? val ?? '' }
function displayVal(v: any) { return displayValue(formatDateTime(v) === v ? v : formatDateTime(v) || v) }
function detailLabel(p: string) { const m: Record<string, string> = { id: 'ID', account: '账号', account_type: '账号类型', account_status: '状态', name: '姓名', nickname: '昵称', phone: '手机号', email: '邮箱', title: '职务', employee_no: '工号', created_at: '创建时间', updated_at: '更新时间' }; return m[p] ?? p }

function openPicker(field: string, options: any[]) { pickerField.value = field; pickerColumns.value = [options]; pickerShow.value = true }
function onPickerConfirm(e: any) { const v = e.value?.[0]; if (pickerField.value && v) form.value[pickerField.value] = v.value; pickerShow.value = false }

function openCreate() { editingId.value = ''; form.value = { account_type: 'ADMIN', account_status: 'ENABLED' }; formVisible.value = true }
function openEdit(item: any) { editingId.value = item.id; form.value = { ...item, password: '' }; formVisible.value = true }
function openDetail(item: any) { detail.value = item; detailVisible.value = true }

async function doSave() {
  saving.value = true
  try {
    const payload: any = { ...form.value }
    if (!payload.password && !editingId.value) { uni.showToast({ title: '请输入密码', icon: 'none' }); return }
    if (payload.password) { const sec = await encryptPasswords({ password: payload.password }); payload.password = sec.values.password; payload.password_key_id = sec.password_key_id }
    if (editingId.value) { await accountApi.update({ ...payload, id: editingId.value }) } else { await accountApi.create(payload) }
    formVisible.value = false; await refresh()
  } finally { saving.value = false }
}

function confirmDelete(item: any) { uni.showModal({ title: '确认删除', content: `删除 ${item.account}？`, success: async (r: any) => { if (r.confirm) { await accountApi.remove({ ids: [item.id] }); await refresh() } } }) }
</script>
