<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'
import { resourceApi } from '@/api'
import { createTagColor, hasPermission, renderButtonIcon, translateLocale } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { NButton, NFlex, NTag } from 'naive-ui'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ModalButtonForm from './ModalButtonForm.vue'

const { t } = useI18n()
const formModalRef = ref<any>(null)
const state = reactive({
  showModal: false,
  loading: false,
  parent: {} as any,
  buttons: [] as any[],
})

const modalTitle = computed(() =>
  state.parent?.name
    ? `${t('resource.iam.resource.button_permissions')} - ${translateLocale(state.parent.locale_key, state.parent.name)}`
    : t('resource.iam.resource.button_permissions'),
)

const columns = computed<DataTableColumns<any>>(() => [
  {
    title: t('resource.iam.resource.name'),
    key: 'name',
    minWidth: 160,
    render: (row) => translateLocale(row.locale_key, row.name),
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.resource.code'),
    key: 'code',
    minWidth: 160,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: t('resource.iam.resource.permission_key'),
    key: 'permission_key',
    minWidth: 220,
    ellipsis: {
      tooltip: true,
    },
    render: (row) => row.permission_key || '-',
  },
  {
    title: t('resource.iam.resource.data_scope'),
    key: 'data_scope',
    width: 140,
    render: (row) => dictTypeData('DATA_SCOPE', row.data_scope) || row.data_scope || '-',
  },
  {
    title: t('resource.iam.resource.sort'),
    key: 'sort',
    width: 90,
  },
  {
    title: t('common.often.status'),
    key: 'status',
    width: 110,
    render: (row) => (
      <NTag color={createTagColor(dictTypeColor('COMMON_STATUS', row.status))} bordered={false}>
        {dictTypeData('COMMON_STATUS', row.status) || row.status}
      </NTag>
    ),
  },
  {
    title: t('common.often.operation'),
    key: 'actions',
    width: 130,
    fixed: 'right',
    render: (row) => (
      <NFlex size={12}>
        {hasPermission('iam:resource:update') && hasPermission('iam:resource:grant') ? (
          <NButton type="primary" size="small" text={true} onClick={() => openForm(row)}>
            {renderButtonIcon('icon-park-outline:edit')}
          </NButton>
        ) : null}
        {hasPermission('iam:resource:delete') ? (
          <NButton type="error" size="small" text={true} onClick={() => confirmDelete(row.id)}>
            {renderButtonIcon('icon-park-outline:delete')}
          </NButton>
        ) : null}
      </NFlex>
    ),
  },
])

async function openModal(parent: any) {
  state.parent = parent ?? {}
  state.buttons = []
  state.showModal = true
  await fetchButtons()
}

async function fetchButtons() {
  if (!state.parent?.id) {
    state.buttons = []
    return
  }
  state.loading = true
  try {
    const response = await resourceApi.buttonPage({
      parent_id: state.parent.id,
      current: 1,
      size: 100,
    })
    state.buttons = response.data?.records ?? []
  } finally {
    state.loading = false
  }
}

function closeModal() {
  state.showModal = false
}

function openForm(row?: any) {
  formModalRef.value?.openModal(state.parent, row)
}

function confirmDelete(id: string) {
  window.$dialog.warning({
    title: t('common.often.delete'),
    draggable: true,
    maskClosable: false,
    content: t('resource.iam.resource.delete_button_confirm'),
    positiveText: t('common.confirm'),
    negativeText: t('common.cancel'),
    onPositiveClick: () => deleteButton(id),
  })
}

async function deleteButton(id: string) {
  await resourceApi.buttonRemove({ ids: [id] })
  window.$message.success(t('common.often.delete_success'))
  await fetchButtons()
}

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="modalTitle"
    style="width: min(980px, calc(100vw - 32px))"
    :segmented="{ content: true, action: true }"
  >
    <NFlex vertical class="min-h-0">
      <NFlex justify="space-between" align="center">
        <NFlex>
          <NButton
            v-if="hasPermission('iam:resource:create') && hasPermission('iam:resource:grant')"
            type="primary"
            text
            :title="t('resource.iam.resource.add_button')"
            :aria-label="t('resource.iam.resource.add_button')"
            @click="openForm()"
          >
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:plus" />
              </NIcon>
            </template>
          </NButton>
          <NButton text :title="t('common.reload')" :aria-label="t('common.reload')" :loading="state.loading" @click="fetchButtons">
            <template #icon>
              <NIcon>
                <Icon icon="icon-park-outline:reload" />
              </NIcon>
            </template>
          </NButton>
        </NFlex>
      </NFlex>

      <NDataTable
        class="mt-12px"
        :row-key="(row) => row.id"
        :columns="columns"
        :data="state.buttons"
        :loading="state.loading"
        :bordered="true"
        :single-line="false"
        :scroll-x="900"
        max-height="calc(100vh - 300px)"
      />
    </NFlex>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          {{ t('common.close') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>

  <ModalButtonForm ref="formModalRef" @saved="fetchButtons" />
</template>
