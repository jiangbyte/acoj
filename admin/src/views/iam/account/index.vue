<!-- Author: Charlie -->

<script setup lang="ts">
import { computed, reactive, type Component } from 'vue'
import { ACCOUNT_TYPE_TABS, DEFAULT_ACCOUNT_TYPE, type AccountType } from '@/constants/account'
import AdminAccountPanel from './components/admin/AccountPanel.vue'
import PortalAccountPanel from './components/portal/AccountPanel.vue'

const panelMap: Record<AccountType, Component> = {
  ADMIN: AdminAccountPanel,
  PORTAL: PortalAccountPanel,
}

const state = reactive({
  accountType: DEFAULT_ACCOUNT_TYPE as AccountType,
})

const activePanel = computed(() => panelMap[state.accountType])

const tabOptions = ACCOUNT_TYPE_TABS.map((item) => ({
  key: item.key,
  label: item.label,
}))

function handleAccountTypeChange(value: string) {
  state.accountType = value as AccountType
}
</script>

<template>
  <div class="account-panel h-full min-h-0">
    <KeepAlive>
      <component
        :is="activePanel"
        :key="state.accountType"
        :account-type="state.accountType"
        :tab-options="tabOptions"
        @update:account-type="handleAccountTypeChange"
      />
    </KeepAlive>
  </div>
</template>

<style scoped>
.account-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.account-panel > * {
  flex: 1;
  min-height: 0;
}
</style>
