<template>
  <Layout :title="grantConfig?.label || '授权'" back>
    <view>
      <u-card>
        <template #head>
          <CardHead
            :title="grantConfig?.label || '授权'"
            :sub-title="`${selectedIds.length} 项已选择`"
          />
        </template>
        <template #body>
          <u-search
            v-model="keyword"
            placeholder="搜索"
            :show-action="false"
            @search="loadOptions"
          ></u-search>
          <u-cell-group :border="false">
            <u-cell-item
              v-for="item in filteredOptions"
              :key="optionId(item)"
              :title="optionName(item)"
              :label="optionId(item)"
              :arrow="false"
              @click="toggle(item)"
            >
              <u-checkbox
                :checked="selectedIds.includes(optionId(item))"
              ></u-checkbox>
            </u-cell-item>
          </u-cell-group>
        </template>
      </u-card>
      <u-card :show-head="false">
        <template #body>
          <u-button
            text="保存授权"
            type="primary"
            :loading="loading"
            @click="save"
          ></u-button>
        </template>
      </u-card>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import CardHead from '@/components/common/CardHead.vue'
import { accountApi, deptApi, groupApi, resourceApi, roleApi } from '@/api'
import {
  adminResourceApis,
  resourceConfigs,
  type GrantActionConfig,
  type ResourceKey,
} from '@/config/resource'
import { flattenTree } from '@/utils/tree'

const resource = ref<ResourceKey>('account')
const id = ref('')
const grantKey = ref('')
const loading = ref(false)
const keyword = ref('')
const options = ref<any[]>([])
const selectedIds = ref<string[]>([])
const grantConfig = computed<GrantActionConfig | undefined>(
  () =>
    resourceConfigs[resource.value].actions.find(
      (item) => item.grant?.key === grantKey.value
    )?.grant
)
const filteredOptions = computed(() => {
  if (!keyword.value) {
    return options.value
  }
  return options.value.filter((item) =>
    `${optionName(item)} ${optionId(item)}`
      .toLowerCase()
      .includes(keyword.value.toLowerCase())
  )
})

onLoad(async (query: any) => {
  resource.value = query.resource || 'account'
  id.value = query.id || ''
  grantKey.value = query.grant || ''
  await Promise.all([loadOwned(), loadOptions()])
})

async function loadOwned() {
  const grant = grantConfig.value
  if (!grant) {
    return
  }
  const data = await ownRequest(grant)
  if (grant.mode === 'dept') {
    selectedIds.value = (data.grant_info_list ?? []).map(
      (item: any) => item.dept_id
    )
  } else if (grant.mode === 'resource') {
    selectedIds.value = (data.grant_info_list ?? []).map(
      (item: any) => item.resource_id
    )
  } else if (grant.mode === 'permission') {
    selectedIds.value = (data.grant_info_list ?? []).map(
      (item: any) => item.permission_key
    )
  } else {
    selectedIds.value = data[grant.selectedField || grant.idField] ?? []
  }
}

async function loadOptions() {
  const grant = grantConfig.value
  if (!grant) {
    return
  }
  if (grant.mode === 'resource') {
    const data = await ownRequest(grant)
    options.value = flattenTree(
      (data.modules ?? []).flatMap((module: any) => module.resources ?? [])
    )
  } else if (grant.mode === 'permission') {
    const data = await ownRequest(grant)
    options.value = data.permissions ?? (await resourceApi.permissionRegistry())
  } else if (grant.targetResource === 'account') {
    options.value =
      (await accountApi.page({ current: 1, size: 100, account: keyword.value }))
        .records ?? []
  } else if (grant.targetResource === 'role') {
    options.value =
      (await roleApi.page({ current: 1, size: 100, name: keyword.value }))
        .records ?? []
  } else if (grant.targetResource === 'group') {
    options.value =
      (await groupApi.page({ current: 1, size: 100, name: keyword.value }))
        .records ?? []
  } else if (grant.targetResource === 'dept') {
    options.value = flattenTree(await deptApi.tree({ name: keyword.value }))
  }
}

function ownRequest(grant: GrantActionConfig) {
  if (resource.value === 'account' && grant.key === 'grantRole')
    return accountApi.ownRole(id.value)
  if (resource.value === 'account' && grant.key === 'grantGroup')
    return accountApi.ownGroup(id.value)
  if (resource.value === 'account' && grant.key === 'grantDept')
    return accountApi.ownDept(id.value)
  if (resource.value === 'account' && grant.key === 'grantResource')
    return accountApi.ownResource(id.value)
  if (resource.value === 'account' && grant.key === 'grantPermission')
    return accountApi.ownPermission(id.value)
  if (resource.value === 'role' && grant.key === 'grantUser')
    return roleApi.ownUser(id.value)
  if (resource.value === 'role' && grant.key === 'grantResource')
    return roleApi.ownResource(id.value)
  if (resource.value === 'role' && grant.key === 'grantPermission')
    return roleApi.ownPermission(id.value)
  if (resource.value === 'group' && grant.key === 'grantUser')
    return groupApi.ownUser(id.value)
  if (resource.value === 'group' && grant.key === 'grantRole')
    return groupApi.ownRole(id.value)
  if (resource.value === 'group' && grant.key === 'grantResource')
    return groupApi.ownResource(id.value)
  if (resource.value === 'group' && grant.key === 'grantPermission')
    return groupApi.ownPermission(id.value)
  return Promise.resolve({})
}

function optionId(item: any) {
  return String(
    item.id ?? item.resource_id ?? item.permission_key ?? item.code ?? ''
  )
}

function optionName(item: any) {
  return (
    item.name ||
    item.label ||
    item.permission_name ||
    item.permission_key ||
    item.account ||
    item.code ||
    optionId(item)
  )
}

function toggle(item: any) {
  const value = optionId(item)
  selectedIds.value = selectedIds.value.includes(value)
    ? selectedIds.value.filter((idValue) => idValue !== value)
    : [...selectedIds.value, value]
}

async function save() {
  const grant = grantConfig.value
  if (!grant) {
    return
  }
  loading.value = true
  try {
    const payload = buildPayload(grant)
    await grantRequest(grant, payload)
    uni.showToast({ title: '已保存', icon: 'success' })
    uni.navigateBack()
  } finally {
    loading.value = false
  }
}

function buildPayload(grant: GrantActionConfig) {
  if (grant.mode === 'dept') {
    return {
      id: id.value,
      grant_info_list: selectedIds.value.map((dept_id) => ({
        dept_id,
        is_primary: false,
      })),
    }
  }
  if (grant.mode === 'resource') {
    return {
      id: id.value,
      grant_info_list: selectedIds.value.map((resource_id) => ({
        resource_id,
        permission_keys: [],
      })),
    }
  }
  if (grant.mode === 'permission') {
    return {
      id: id.value,
      grant_info_list: selectedIds.value.map((permission_key) => ({
        permission_key,
        data_scope: 'SELF',
        custom_scope_dept_ids: [],
      })),
    }
  }
  return { id: id.value, [grant.idField]: selectedIds.value }
}

function grantRequest(grant: GrantActionConfig, payload: any) {
  if (resource.value === 'account' && grant.key === 'grantRole')
    return accountApi.grantRole(payload)
  if (resource.value === 'account' && grant.key === 'grantGroup')
    return accountApi.grantGroup(payload)
  if (resource.value === 'account' && grant.key === 'grantDept')
    return accountApi.grantDept(payload)
  if (resource.value === 'account' && grant.key === 'grantResource')
    return accountApi.grantResource(payload)
  if (resource.value === 'account' && grant.key === 'grantPermission')
    return accountApi.grantPermission(payload)
  if (resource.value === 'role' && grant.key === 'grantUser')
    return roleApi.grantUser(payload)
  if (resource.value === 'role' && grant.key === 'grantResource')
    return roleApi.grantResource(payload)
  if (resource.value === 'role' && grant.key === 'grantPermission')
    return roleApi.grantPermission(payload)
  if (resource.value === 'group' && grant.key === 'grantUser')
    return groupApi.grantUser(payload)
  if (resource.value === 'group' && grant.key === 'grantRole')
    return groupApi.grantRole(payload)
  if (resource.value === 'group' && grant.key === 'grantResource')
    return groupApi.grantResource(payload)
  if (resource.value === 'group' && grant.key === 'grantPermission')
    return groupApi.grantPermission(payload)
  return adminResourceApis[resource.value].update(payload)
}
</script>
