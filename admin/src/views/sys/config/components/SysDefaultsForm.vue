<!-- Author: Charlie -->

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import ConfigSectionLayout from './ConfigSectionLayout.vue'
import { loadByCategory, saveByKeys } from '../composables/useConfigForm'

const CATEGORY = 'SYS'

const state = reactive({
  loading: false,
  saving: false,
  appName: 'HEI',
  copyrightText: '',
  copyrightUrl: '',
  icpNumber: '',
  icpUrl: 'https://beian.miit.gov.cn/',
  psbNumber: '',
  psbUrl: '',
  snapshot: '',
})

onMounted(() => {
  void reload()
})

function snapshotOf() {
  return JSON.stringify({
    appName: state.appName,
    copyrightText: state.copyrightText,
    copyrightUrl: state.copyrightUrl,
    icpNumber: state.icpNumber,
    icpUrl: state.icpUrl,
    psbNumber: state.psbNumber,
    psbUrl: state.psbUrl,
  })
}

async function reload() {
  state.loading = true
  try {
    const map = await loadByCategory(CATEGORY)
    state.appName = map.APP_NAME || 'HEI'
    state.copyrightText = map.COPYRIGHT_TEXT || ''
    state.copyrightUrl = map.COPYRIGHT_URL || ''
    state.icpNumber = map.SITE_ICP_NUMBER || ''
    state.icpUrl = map.SITE_ICP_URL || 'https://beian.miit.gov.cn/'
    state.psbNumber = map.SITE_PSB_NUMBER || ''
    state.psbUrl = map.SITE_PSB_URL || ''
    state.snapshot = snapshotOf()
  } finally {
    state.loading = false
  }
}

function reset() {
  if (!state.snapshot) return
  Object.assign(state, JSON.parse(state.snapshot))
}

async function save() {
  state.saving = true
  try {
    await saveByKeys([
      {
        config_key: 'APP_NAME',
        config_value: state.appName.trim(),
        category: CATEGORY,
      },
      {
        config_key: 'COPYRIGHT_TEXT',
        config_value: state.copyrightText,
        category: CATEGORY,
      },
      {
        config_key: 'COPYRIGHT_URL',
        config_value: state.copyrightUrl,
        category: CATEGORY,
      },
      {
        config_key: 'SITE_ICP_NUMBER',
        config_value: state.icpNumber.trim(),
        category: CATEGORY,
      },
      {
        config_key: 'SITE_ICP_URL',
        config_value: state.icpUrl.trim(),
        category: CATEGORY,
      },
      {
        config_key: 'SITE_PSB_NUMBER',
        config_value: state.psbNumber.trim(),
        category: CATEGORY,
      },
      {
        config_key: 'SITE_PSB_URL',
        config_value: state.psbUrl.trim(),
        category: CATEGORY,
      },
    ])
    window.$message.success('保存成功')
    state.snapshot = snapshotOf()
  } finally {
    state.saving = false
  }
}
</script>

<template>
  <NSpin :show="state.loading">
    <ConfigSectionLayout
      description="配置应用名称、版权文案与 ICP/公安备案信息。Admin、Portal 等端通过公开接口统一获取。"
      :saving="state.saving"
      @save="save"
      @reset="reset"
    >
      <NForm
        class="sys-config-form"
        label-placement="top"
      >
        <NFormItem label="应用名称">
          <NInput
            v-model:value="state.appName"
            placeholder="HEI"
          />
        </NFormItem>

        <NDivider title-placement="left">
          版权信息
        </NDivider>
        <NFormItem label="版权文案">
          <NInput v-model:value="state.copyrightText" />
        </NFormItem>
        <NFormItem label="版权链接">
          <NInput
            v-model:value="state.copyrightUrl"
            placeholder="https://"
          />
        </NFormItem>

        <NDivider title-placement="left">
          备案信息
        </NDivider>
        <NFormItem label="ICP 备案号">
          <NInput
            v-model:value="state.icpNumber"
            placeholder="京ICP备xxxxxxxx号"
          />
        </NFormItem>
        <NFormItem label="ICP 备案链接">
          <NInput
            v-model:value="state.icpUrl"
            placeholder="https://beian.miit.gov.cn/"
          />
        </NFormItem>
        <NFormItem label="公安备案号">
          <NInput
            v-model:value="state.psbNumber"
            placeholder="京公网安备 xxxxxxxx号"
          />
        </NFormItem>
        <NFormItem label="公安备案链接">
          <NInput
            v-model:value="state.psbUrl"
            placeholder="https://www.beian.gov.cn/..."
          />
        </NFormItem>
      </NForm>
    </ConfigSectionLayout>
  </NSpin>
</template>
