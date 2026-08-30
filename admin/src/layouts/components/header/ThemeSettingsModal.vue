<!-- Author: Charlie -->
<!-- 主题配置：居中 Modal；定高滚动 + 两列栅格 -->
<script setup lang="ts">
import { THEME_COLOR_PRESETS } from '@/stores/app/defaults'
import { useAppStore } from '@/stores'
import type { FormStyle } from '@/stores/app/defaults'

const show = defineModel<boolean>('show', { default: false })

const appStore = useAppStore()

const formStyleOptions: { label: string; value: FormStyle }[] = [
  { label: '抽屉', value: 'drawer' },
  { label: '弹窗', value: 'modal' },
]

const colorModeOptions = [
  { label: '浅色', value: 'light', tip: '亮色主题风格', style: 'theme-settings__style--light' },
  { label: '深色', value: 'dark', tip: '暗色侧栏风格', style: 'theme-settings__style--dark' },
  { label: '系统', value: 'auto', tip: '跟随系统主题', style: 'theme-settings__style--auto' },
] as const

const switchRows: { label: string; key: 'showBreadcrumb' | 'showTabbar' | 'collapsed' | 'accordionMenu' | 'showWatermark' }[] = [
  { label: '面包屑', key: 'showBreadcrumb' },
  { label: '多标签', key: 'showTabbar' },
  { label: '折叠菜单', key: 'collapsed' },
  { label: '菜单排他展开', key: 'accordionMenu' },
  { label: '登录用户水印', key: 'showWatermark' },
]

function pickColor(color: string) {
  appStore.setPrimaryColor(color)
}

function onFormStyle(value: FormStyle) {
  appStore.setFormStyle(value)
}

function onRounded(value: boolean) {
  appStore.setRoundedStyle(value)
}

function onGrayMode(value: boolean) {
  appStore.grayMode = value
  appStore.syncDocumentAppearance()
}

function reset() {
  appStore.resetAppearance()
}
</script>

<template>
  <NModal
    v-model:show="show"
    preset="card"
    title="主题配置"
    :mask-closable="true"
    class="theme-settings-modal"
    style="width: 560px"
    :segmented="{ content: true, action: true }"
  >
    <NScrollbar class="theme-settings__scroll">
      <div class="theme-settings">
        <h3 class="theme-settings__title">
          整体风格
        </h3>
        <div class="theme-settings__styles">
          <NTooltip
            v-for="opt in colorModeOptions"
            :key="opt.value"
            placement="top"
          >
            <template #trigger>
              <button
                type="button"
                class="theme-settings__style"
                :class="[opt.style, { 'is-active': appStore.storeColorMode === opt.value }]"
                @click="appStore.setColorMode(opt.value)"
              >
                <NovaIcon
                  v-if="appStore.storeColorMode === opt.value"
                  icon="icon-park-outline:check"
                  :size="14"
                  class="theme-settings__style-check"
                />
              </button>
            </template>
            {{ opt.tip }}
          </NTooltip>
        </div>

        <h3 class="theme-settings__title">
          主题色
        </h3>
        <div class="theme-settings__colors">
          <NTooltip
            v-for="item in THEME_COLOR_PRESETS"
            :key="item.color"
            placement="top"
          >
            <template #trigger>
              <button
                type="button"
                class="theme-settings__swatch"
                :style="{ backgroundColor: item.color }"
                @click="pickColor(item.color)"
              >
                <NovaIcon
                  v-if="appStore.primaryColor.toLowerCase() === item.color.toLowerCase()"
                  icon="icon-park-outline:check"
                  :size="12"
                  class="theme-settings__check"
                />
              </button>
            </template>
            {{ item.key }}
          </NTooltip>
        </div>

        <NDivider class="theme-settings__divider" />

        <NGrid
          :cols="2"
          :x-gap="24"
          :y-gap="0"
        >
          <NGi
            v-for="row in switchRows"
            :key="row.key"
          >
            <div class="theme-settings__row">
              <span class="theme-settings__label">{{ row.label }}</span>
              <NSwitch v-model:value="appStore[row.key]" />
            </div>
          </NGi>

          <NGi>
            <div class="theme-settings__row">
              <span class="theme-settings__label">圆角风格</span>
              <NSwitch
                :value="appStore.roundedStyle"
                @update:value="onRounded"
              />
            </div>
          </NGi>

          <NGi>
            <div class="theme-settings__row">
              <span class="theme-settings__label">灰色模式</span>
              <NSwitch
                :value="appStore.grayMode"
                @update:value="onGrayMode"
              />
            </div>
          </NGi>

          <NGi>
            <div class="theme-settings__row">
              <span class="theme-settings__label">表单/详情风格</span>
              <NSelect
                class="theme-settings__form-style"
                size="small"
                :value="appStore.formStyle"
                :options="formStyleOptions"
                @update:value="onFormStyle"
              />
            </div>
          </NGi>
        </NGrid>

        <NAlert
          class="theme-settings__alert"
          type="warning"
          :bordered="false"
        >
          以上配置可实时预览，刷新后仍会保留。
        </NAlert>
      </div>
    </NScrollbar>

    <template #action>
      <div class="theme-settings__action">
        <NButton @click="reset">
          恢复默认
        </NButton>
        <NButton
          type="primary"
          @click="show = false"
        >
          关闭
        </NButton>
      </div>
    </template>
  </NModal>
</template>

<style scoped>
.theme-settings__scroll {
  height: min(420px, calc(100vh - 220px));
  max-height: min(420px, calc(100vh - 220px));
}

.theme-settings {
  padding-right: 4px;
}

.theme-settings__title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  color: var(--n-text-color);
}

.theme-settings__styles {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 36px;
  margin-bottom: 20px;
}

.theme-settings__style {
  position: relative;
  width: 44px;
  height: 36px;
  padding: 0;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: var(--border-radius);
  box-shadow: 0 1px 2.5px 0 rgb(0 0 0 / 18%);
  cursor: pointer;
  background: #ebeef1;
}

.theme-settings__style::before {
  position: absolute;
  top: 0;
  left: 0;
  width: 33%;
  height: 100%;
  content: '';
  background: #001529;
}

.theme-settings__style::after {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 25%;
  content: '';
  background: #fff;
}

.theme-settings__style--light::before {
  background: #001529;
}

.theme-settings__style--dark::before {
  background: #001529;
}

.theme-settings__style--dark::after {
  background: #001529;
}

.theme-settings__style--auto::before {
  background: #1677ff;
}

.theme-settings__style--auto::after {
  background: #fff;
}

.theme-settings__style.is-active {
  outline: 2px solid var(--primary-color);
  outline-offset: 1px;
}

.theme-settings__style-check {
  position: absolute;
  right: 4px;
  bottom: 4px;
  z-index: 2;
  color: var(--primary-color);
}

.theme-settings__colors {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 8px;
  min-height: 28px;
  margin-bottom: 8px;
}

.theme-settings__swatch {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  display: grid;
  place-items: center;
  padding: 0;
}

.theme-settings__check {
  color: #fff;
}

.theme-settings__divider {
  margin: 12px 0 8px !important;
}

.theme-settings__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  gap: 12px;
}

.theme-settings__label {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  line-height: 22px;
  color: var(--n-text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.theme-settings__form-style {
  width: 108px;
  flex-shrink: 0;
}

.theme-settings__alert {
  margin-top: 12px;
}

.theme-settings__action {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  height: 32px;
}
</style>
