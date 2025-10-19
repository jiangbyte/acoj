<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NDrawer, NDrawerContent, NIcon, NTag } from 'naive-ui'
import { useSysMenuFetch, useSysRoleFetch } from '@/composables/v1'
import { Icon } from '@iconify/vue'

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
      window.$message.success('分配成功')
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
function doOpen(row: any) {
  show.value = true
  assignResource.value = row.assignResource
  roleIdR.value = row.id

  console.log(assignResource.value)

  useSysMenuFetch().sysMenuAuthList().then(({ data }) => {
    // 构建树形结构
    const menuTree = buildMenuTree(data)

    // 递归过滤：如果父级不可见，子级也要隐藏
    authResource.value = filterInvisibleMenus(menuTree)
  })
}

// 构建树形结构
function buildMenuTree(flatMenus: any[]) {
  const menuMap = new Map()
  const tree: any[] = []

  // 第一遍：将所有菜单项存入map，并初始化children数组
  flatMenus.forEach((menu) => {
    menuMap.set(menu.id, { ...menu, children: [] })
  })

  // 第二遍：构建父子关系
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

// 递归过滤不可见菜单
function filterInvisibleMenus(menus: any[]): any[] {
  return menus
    .filter(menu => menu.visible) // 只保留可见的菜单
    .map((menu) => {
      // 深拷贝菜单项，避免修改原始数据
      const filteredMenu = { ...menu }

      // 递归过滤子菜单
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
  //   title: '父菜单ID',
  //   key: 'pid',
  // },
  {
    title: '菜单',
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
    title: '菜单类型',
    key: 'menuTypeName',
    render: (row) => {
      return h(NTag, {
        type: row.menuType === 0 ? 'default' : 'warning',
      }, () => row.menuTypeName)
    },
  },
  // {
  //   title: '菜单标题',
  //   key: 'title',
  //   // width: 120,
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: '名称',
    key: 'name',
    ellipsis: {
      tooltip: true,
    },
  },
  // {
  //   title: '路由路径',
  //   key: 'path',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  // {
  //   title: '组件路径',
  //   key: 'componentPath',
  //   ellipsis: {
  //     tooltip: true,
  //   },
  // },
  {
    title: '排序',
    key: 'sort',
    width: 80,
  },
  // {
  //   title: '可见',
  //   key: 'visibleName',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, {
  //       type: row.visible ? 'primary' : 'error',
  //     }, () => row.visibleName)
  //   },
  // },
  // {
  //   title: '缓存',
  //   key: 'keepAliveName',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, {
  //       type: row.keepAlive ? 'primary' : 'error',
  //     }, () => row.keepAliveName)
  //   },
  // },
  // {
  //   title: '固定',
  //   key: 'pinedName',
  //   width: 80,
  //   render: (row) => {
  //     return h(NTag, {
  //       type: row.pined ? 'primary' : 'error',
  //     }, () => row.pinedName)
  //   },
  // },
  // {
  //   title: '额外信息',
  //   key: 'exJson',
  // },
]
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="资源分配">
      <NDataTable
        v-model:checked-row-keys="assignResource"
        :columns="columns"
        :data="authResource"
        :bordered="false"
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
            关闭
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            保存
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
