<template>
  <AppDrawerForm
    :open="open"
    :title="isEdit ? '编辑资源' : (parentId ? '新增子级资源' : '新增资源')"
    :form="form"
    :on-submit="handleSubmit"
    @close="handleClose"
    @success="emit('success')"
  >
    <template #default>
      <!-- ── 基本信息 ── -->
      <a-divider orientation="left" class="!text-[13px] !mt-0">基本信息</a-divider>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="资源名称" name="name" :rules="[{ required: true, message: '请输入资源名称' }]">
            <a-input v-model:value="form.name" placeholder="请输入资源名称" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="资源编码" name="code" :rules="[{ required: true, message: '请输入资源编码' }]">
            <a-input v-model:value="form.code" placeholder="请输入资源编码" :disabled="isEdit" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="资源分类" name="category" :rules="[{ required: true, message: '请选择资源分类' }]">
            <a-select v-model:value="form.category" placeholder="请选择资源分类">
              <a-select-option value="BACKEND_MENU">后台菜单</a-select-option>
              <a-select-option value="FRONTEND_MENU">前台菜单</a-select-option>
              <a-select-option value="BACKEND_BUTTON">后台按钮</a-select-option>
              <a-select-option value="FRONTEND_BUTTON">前台按钮</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="资源类型" name="type" :rules="[{ required: true, message: '请选择资源类型' }]">
            <a-select v-model:value="form.type" placeholder="请选择资源类型" @change="handleTypeChange">
              <a-select-option value="DIRECTORY">目录</a-select-option>
              <a-select-option value="MENU">菜单</a-select-option>
              <a-select-option value="BUTTON">按钮</a-select-option>
              <a-select-option value="INTERNAL_LINK">内链</a-select-option>
              <a-select-option value="EXTERNAL_LINK">外链</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="父资源" name="parent_id">
            <a-tree-select
              v-model:value="form.parent_id"
              :tree-data="resourceTreeData"
              :field-names="{ children: 'children', label: 'name', value: 'id' }"
              placeholder="无（根级）"
              allow-clear
              :disabled="!!parentId"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- ── 路由配置（目录、菜单、内链） ── -->
      <template v-if="isRouteType">
        <a-divider orientation="left" class="!text-[13px]">路由配置</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="路由路径" name="route_path">
              <a-input v-model:value="form.route_path" placeholder="如 /sys/user" />
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="isMenuType">
            <a-form-item label="组件路径" name="component_path">
              <a-input v-model:value="form.component_path" placeholder="如 sys/user/index" />
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="isMenuType">
            <a-form-item label="重定向" name="redirect_path">
              <a-input v-model:value="form.redirect_path" placeholder="重定向路径" />
            </a-form-item>
          </a-col>
        </a-row>
      </template>

      <!-- ── 显示配置（目录、菜单、内链） ── -->
      <template v-if="isRouteType">
        <a-divider orientation="left" class="!text-[13px]">显示配置</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="图标" name="icon">
              <a-input v-model:value="form.icon" placeholder="图标名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="颜色" name="color">
              <a-input v-model:value="form.color" placeholder="资源颜色" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="是否可见" name="is_visible">
              <a-switch
                :checked="form.is_visible !== 'NO'"
                @change="onVisibleChange"
                checked-children="是"
                un-checked-children="否"
              />
            </a-form-item>
          </a-col>
<a-col :span="8" v-if="isMenuType">
            <a-form-item label="是否缓存" name="is_cache">
              <a-switch
                :checked="form.is_cache === 'YES'"
                @change="onCacheChange"
                checked-children="是"
                un-checked-children="否"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8" v-if="isMenuType">
            <a-form-item label="是否固定" name="is_affix">
              <a-switch
                :checked="form.is_affix === 'YES'"
                @change="onAffixChange"
                checked-children="是"
                un-checked-children="否"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8" v-if="isMenuType">
            <a-form-item label="显示面包屑" name="is_breadcrumb">
              <a-switch
                :checked="form.is_breadcrumb !== 'NO'"
                @change="onBreadcrumbChange"
                checked-children="是"
                un-checked-children="否"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </template>

      <!-- ── 外链（外链类型 或 菜单含外链） ── -->
      <template v-if="form.type === 'EXTERNAL_LINK'">
        <a-divider orientation="left" class="!text-[13px]">外链配置</a-divider>
        <a-form-item label="外链地址" name="external_url">
          <a-input v-model:value="form.external_url" placeholder="https://" />
        </a-form-item>
      </template>

      <!-- ── 其他 ── -->
      <a-divider orientation="left" class="!text-[13px]">其他</a-divider>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="排序" name="sort_code">
            <a-input-number v-model:value="form.sort_code" :min="0" :max="9999" style="width: 100%" placeholder="排序值" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="状态" name="status">
            <a-select v-model:value="form.status" placeholder="请选择状态">
              <a-select-option value="ENABLED">启用</a-select-option>
              <a-select-option value="DISABLED">禁用</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="描述" name="description">
            <a-textarea v-model:value="form.description" placeholder="资源描述" :rows="3" />
          </a-form-item>
        </a-col>
      </a-row>
    </template>
  </AppDrawerForm>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { fetchResourceDetail, fetchResourceCreate, fetchResourceModify, fetchResourceTree } from '@/api/resource'
import AppDrawerForm from '@/components/form/AppDrawerForm.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isEdit = ref(false)
const currentId = ref<string | null>(null)
const parentId = ref<string | undefined>(undefined)
const resourceTreeData = ref<any[]>([])

const isRouteType = computed(() =>
  ['DIRECTORY', 'MENU', 'INTERNAL_LINK'].includes(form.type)
)
const isMenuType = computed(() =>
  ['MENU', 'INTERNAL_LINK'].includes(form.type)
)

const initialForm = () => ({
  code: '',
  name: '',
  category: '' as string,
  type: '' as string,
  parent_id: undefined as string | undefined,
  route_path: '',
  component_path: '',
  redirect_path: '',
  icon: '',
  color: '',
  is_visible: 'YES',
  is_cache: 'NO',
  is_affix: 'NO',
  is_breadcrumb: 'YES',
  external_url: '',
  sort_code: 0,
  status: 'ENABLED',
  description: '',
})

const form = reactive(initialForm())

function handleTypeChange() {
  if (!isRouteType.value) {
    form.route_path = ''
    form.component_path = ''
    form.redirect_path = ''
    form.icon = ''
    form.color = ''
    form.is_visible = 'YES'
    form.is_cache = 'NO'
    form.is_affix = 'NO'
    form.is_breadcrumb = 'YES'
  }
  if (form.type !== 'EXTERNAL_LINK') {
    form.external_url = ''
  }
}

function onVisibleChange(checked: boolean) { form.is_visible = checked ? 'YES' : 'NO' }
function onCacheChange(checked: boolean) { form.is_cache = checked ? 'YES' : 'NO' }
function onAffixChange(checked: boolean) { form.is_affix = checked ? 'YES' : 'NO' }
function onBreadcrumbChange(checked: boolean) { form.is_breadcrumb = checked ? 'YES' : 'NO' }

async function loadTree() {
  try {
    const { data } = await fetchResourceTree()
    resourceTreeData.value = data || []
  } catch { /* ignore */ }
}

async function doOpen(row?: any, pId?: string) {
  await loadTree()
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    parentId.value = undefined
    const { data } = await fetchResourceDetail({ id: row.id })
    if (data) {
      Object.assign(form, data)
    }
  } else {
    isEdit.value = false
    currentId.value = null
    parentId.value = pId
    Object.assign(form, initialForm())
    if (pId) form.parent_id = pId
  }
  emit('update:open', true)
}

async function handleSubmit(f: any) {
  if (currentId.value) {
    return await fetchResourceModify({ ...f, id: currentId.value })
  } else {
    return await fetchResourceCreate(f)
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
