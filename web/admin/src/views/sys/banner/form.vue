<script setup lang="ts">
import { message } from 'ant-design-vue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { bannerApi } from '@/apis/sys'

const emit = defineEmits<{ successful: [] }>()
const { t } = useI18n()

const statusOptions = [
  { labelKey: 'sys.options.enabled', value: 'ENABLED' },
  { labelKey: 'sys.options.disabled', value: 'DISABLED' },
]
const categoryOptions = [
  { labelKey: 'sys.options.home', value: 'HOME' },
  { labelKey: 'sys.options.login', value: 'LOGIN' },
  { labelKey: 'sys.options.workplace', value: 'WORKPLACE' },
  { labelKey: 'sys.options.notice', value: 'NOTICE' },
  { labelKey: 'sys.options.adminDashboard', value: 'ADMIN_DASHBOARD' },
  { labelKey: 'sys.options.systemUpgrade', value: 'SYSTEM_UPGRADE' },
]
const typeOptions = [
  { labelKey: 'sys.options.carousel', value: 'CAROUSEL' },
  { labelKey: 'sys.options.hero', value: 'HERO' },
  { labelKey: 'sys.options.noticeBar', value: 'NOTICE' },
  { labelKey: 'sys.options.card', value: 'CARD' },
  { labelKey: 'sys.options.popup', value: 'POPUP' },
  { labelKey: 'sys.options.sidebar', value: 'SIDEBAR' },
]
const positionOptions = [
  { labelKey: 'sys.options.homeTop', value: 'HOME_TOP' },
  { labelKey: 'sys.options.homeMiddle', value: 'HOME_MIDDLE' },
  { labelKey: 'sys.options.homeBottom', value: 'HOME_BOTTOM' },
  { labelKey: 'sys.options.loginSide', value: 'LOGIN_SIDE' },
  { labelKey: 'sys.options.workplaceTop', value: 'WORKPLACE_TOP' },
  { labelKey: 'sys.options.noticeArea', value: 'NOTICE_AREA' },
  { labelKey: 'sys.options.adminTop', value: 'ADMIN_TOP' },
  { labelKey: 'sys.options.adminSidebar', value: 'ADMIN_SIDEBAR' },
]
const displayScopeOptions = [
  { labelKey: 'sys.options.portal', value: 'PORTAL' },
  { labelKey: 'sys.options.admin', value: 'ADMIN' },
  { labelKey: 'sys.options.app', value: 'APP' },
]
const linkTypeOptions = [
  { labelKey: 'sys.options.url', value: 'URL' },
  { labelKey: 'sys.options.route', value: 'ROUTE' },
  { labelKey: 'sys.options.none', value: 'NONE' },
]

const visible = ref(false)
const saving = ref(false)
const form = ref<Record<string, any>>({})

function onOpen(record?: Record<string, any>) {
  visible.value = true
  if (!record) {
    form.value = {
      title: '',
      image: '',
      url: '',
      link_type: 'URL',
      summary: '',
      description: '',
      category: 'HOME',
      type: 'CAROUSEL',
      position: 'HOME_TOP',
      display_scope: 'PORTAL',
      sort: 0,
      status: 'ENABLED',
    }
    return
  }

  bannerApi.bannerDetail({ id: record.id }).then((detail) => {
    form.value = {
      ...detail,
      url: detail.url || '',
      summary: detail.summary || '',
      description: detail.description || '',
      active_time: detail.start_at && detail.end_at ? [detail.start_at, detail.end_at] : undefined,
    }
  })
}

function onClose() {
  form.value = {}
  visible.value = false
}

async function onSubmit() {
  const data = form.value
  const title = String(data.title || '').trim()
  const image = String(data.image || '').trim()

  if (!title || !image) {
    message.warning(t('sys.bannerRequired'))
    return
  }

  saving.value = true
  try {
    const [startAt, endAt] = data.active_time || []
    data.title = title
    data.image = image
    data.sort = Number(data.sort) || 0
    data.start_at = startAt || null
    data.end_at = endAt || null
    await bannerApi.submitForm(data, Boolean(data.id))
    message.success(t(data.id ? 'sys.bannerUpdated' : 'sys.bannerCreated'))
    onClose()
    emit('successful')
  } finally {
    saving.value = false
  }
}

defineExpose({
  onOpen,
})
</script>

<template>
  <ADrawer v-model:open="visible" :title="form.id ? t('sys.editBanner') : t('sys.createBanner')" width="620" @close="onClose">
    <AForm layout="vertical" :model="form">
      <AFormItem :label="t('sys.title')" required>
        <AInput v-model:value="form.title" :placeholder="t('sys.bannerTitlePlaceholder')" />
      </AFormItem>
      <AFormItem :label="t('sys.imageUrl')" required>
        <AInput v-model:value="form.image" placeholder="https://example.com/banner.png" />
      </AFormItem>
      <AFormItem :label="t('sys.url')">
        <AInput v-model:value="form.url" :placeholder="t('sys.urlPlaceholder')" />
      </AFormItem>
      <AFormItem :label="t('sys.linkType')">
        <ASelect v-model:value="form.link_type">
          <ASelectOption v-for="item in linkTypeOptions" :key="item.value" :value="item.value">
            {{ t(item.labelKey) }}
          </ASelectOption>
        </ASelect>
      </AFormItem>
      <AFormItem :label="t('sys.summary')"><AInput v-model:value="form.summary" /></AFormItem>
      <AFormItem :label="t('sys.description')"><ATextarea v-model:value="form.description" :rows="3" /></AFormItem>
      <ARow :gutter="16">
        <ACol :span="12">
          <AFormItem :label="t('sys.category')">
            <ASelect v-model:value="form.category">
              <ASelectOption v-for="item in categoryOptions" :key="item.value" :value="item.value">
                {{ t(item.labelKey) }}
              </ASelectOption>
            </ASelect>
          </AFormItem>
        </ACol>
        <ACol :span="12">
          <AFormItem :label="t('common.type')">
            <ASelect v-model:value="form.type">
              <ASelectOption v-for="item in typeOptions" :key="item.value" :value="item.value">
                {{ t(item.labelKey) }}
              </ASelectOption>
            </ASelect>
          </AFormItem>
        </ACol>
      </ARow>
      <ARow :gutter="16">
        <ACol :span="12">
          <AFormItem :label="t('sys.position')">
            <ASelect v-model:value="form.position">
              <ASelectOption v-for="item in positionOptions" :key="item.value" :value="item.value">
                {{ t(item.labelKey) }}
              </ASelectOption>
            </ASelect>
          </AFormItem>
        </ACol>
        <ACol :span="12">
          <AFormItem :label="t('sys.displayScope')">
            <ASelect v-model:value="form.display_scope">
              <ASelectOption v-for="item in displayScopeOptions" :key="item.value" :value="item.value">
                {{ t(item.labelKey) }}
              </ASelectOption>
            </ASelect>
          </AFormItem>
        </ACol>
      </ARow>
      <ARow :gutter="16">
        <ACol :span="12">
          <AFormItem :label="t('sys.sort')"><AInputNumber v-model:value="form.sort" class="w-full" :min="0" /></AFormItem>
        </ACol>
        <ACol :span="12">
          <AFormItem :label="t('common.status')">
            <ASelect v-model:value="form.status">
              <ASelectOption v-for="item in statusOptions" :key="item.value" :value="item.value">
                {{ t(item.labelKey) }}
              </ASelectOption>
            </ASelect>
          </AFormItem>
        </ACol>
      </ARow>
      <AFormItem :label="t('sys.activeTime')">
        <ARangePicker v-model:value="form.active_time" class="w-full" show-time value-format="YYYY-MM-DDTHH:mm:ssZ" />
      </AFormItem>
    </AForm>
    <template #footer>
      <ASpace>
        <AButton @click="onClose">{{ t('common.cancel') }}</AButton>
        <AButton type="primary" :loading="saving" @click="onSubmit">{{ t('common.save') }}</AButton>
      </ASpace>
    </template>
  </ADrawer>
</template>
