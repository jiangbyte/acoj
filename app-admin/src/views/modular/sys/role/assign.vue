<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NDrawer, NDrawerContent, NIcon, NTag } from 'naive-ui'
import { useSysMenuFetch, useSysRoleFetch } from '@/composables/v1'
import { Icon } from '@iconify/vue/offline'

const emit = defineEmits(['close', 'submit'])
const loading = ref(false)
const formRef = ref()
const roleIdR = ref('')
const show = ref(false)
const assignResource = ref<string[]>([])
const authResource = ref<any>([])
function doClose() {
  emit('close')
  show.value = false
}
async function doSubmit() {
  loading.value = true

  useSysRoleFetch().sysMenuAssign({
    roleId: roleIdR.value,
    menuIds: assignResource.value,
  }).then(({ success }) => {
    if (success) {
      window.$message.success('Assigned successfully')
      emit('submit', true)
      doClose()
      show.value = false
      loading.value = false
    }
  })
}

// function doOpen(row: any) {
//   show.value = true
//   assignResource.value = row.assignResource
//   roleIdR.value = row.id

//   console.log(assignResource.value)

//   useSysMenuFetch().sysMenuAuthList().then(({ data }) => {
//     authResource.value = data.filter((item: any) => item.visible)
//   })
// }
const isLoading = ref(false)
function doOpen(row: any) {
  show.value = true
  assignResource.value = row.assignResource
  roleIdR.value = row.id

  console.log(assignResource.value)

  isLoading.value = true
  useSysMenuFetch().sysMenuAuthList().then(({ data }) => {
    // Build the tree
    const menuTree = buildMenuTree(data)

    // Recursively filter children when their parent is hidden
    authResource.value = filterInvisibleMenus(menuTree)
    isLoading.value = false
  })
}

// Build the tree
function buildMenuTree(flatMenus: any[]) {
  const menuMap = new Map()
  const tree: any[] = []

  // First pass: store menu items in a map and initialize children arrays
  flatMenus.forEach((menu) => {
    menuMap.set(menu.id, { ...menu, children: [] })
  })

  // Second pass: build parent-child relationships
  flatMenus.forEach((menu) => {
    const menuItem = menuMap.get(menu.id)

    if (menu.pid && menu.pid !== '0') {
      const parent = menuMap.get(menu.pid)
      if (parent) {
        parent.children.push(menuItem)
      }
    }
    else {
      tree.push(menuItem)
    }
  })

  return tree
}

// Recursively filter hidden menus
function filterInvisibleMenus(menus: any[]): any[] {
  return menus
    .filter(menu => menu.visible) // Keep only visible menus
    .map((menu) => {
      // Clone menu items to avoid modifying source data
      const filteredMenu = { ...menu }

      // Recursively filter child menus
      if (filteredMenu.children && filteredMenu.children.length > 0) {
        filteredMenu.children = filterInvisibleMenus(filteredMenu.children)
      }

      return filteredMenu
    })
}

defineExpose({
  doOpen,
})

// const checkedRowKeys = ref<string[]>([])
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  // {
  //   title: 'Parent Menu ID',
  //   key: 'pid',
  // },
  {
    title: 'Menu',
    key: 'icon',
    // render: (row: any) => {
    //   return h(NIcon, { size: '18' }, { default: () => h(Icon, { icon: row.icon }) })
    // },
    render: (row: any) => {
      return h('div', { class: 'inline-flex items-center justify-center' }, [
        h(NIcon, { }, { default: () => h(Icon, { icon: row.icon }) }),
        h('span', { class: 'ml-2' }, row.title || row.name),
      ])
    },
  },
  {
    title: 'Menu Type',
    key: 'menuTypeName',
    render: (row) => {
      return h(NTag, {
        type: row.menuType === 0 ? 'default' : 'warning',
      }, () => row.menuTypeName)
    },
  },
  // {
  //   title: 'Menu Title',
  //   key: 'title',
  //   // width: 120,
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Name',
    key: 'name',
    ellipsis: {
      tooltip: true,
    },
  },
  // {
  //   title: 'Route Path',
  //   key: 'path',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  // {
  //   title: 'Component Path',
  //   key: 'componentPath',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: 'Sort Order',
    key: 'sort',
    width: 80,
  },
  // {
  //   title: 'Visible',
  //   key: 'visibleName',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, {
  //       type: row.visible ? 'primary' : 'error',
  //     }, () => row.visibleName)
  //   },
  // },
  // {
  //   title: 'Cache',
  //   key: 'keepAliveName',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, {
  //       type: row.keepAlive ? 'primary' : 'error',
  //     }, () => row.keepAliveName)
  //   },
  // },
  // {
  //   title: 'Pinned',
  //   key: 'pinedName',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, {
  //       type: row.pined ? 'primary' : 'error',
  //     }, () => row.pinedName)
  //   },
  // },
  // {
  //   title: 'Extra Information',
  //   key: 'exJson',
  // },
]
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Resource Assignment">
      <NDataTable
        v-model:checked-row-keys="assignResource"
        :columns="columns"
        :data="authResource"
        :bordered="false"
        :loading="isLoading"
        :row-key="(row: any) => row.id"
        :cascade="false"
        flex-height
        class="flex-1 h-full"
      />
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            Save
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
