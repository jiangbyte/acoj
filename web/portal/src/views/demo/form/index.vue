<template>
  <n-space vertical size="large">
    <n-page-header :title="t('demo.formTitle')" :subtitle="t('demo.formSubtitle')" />

    <n-card :bordered="false">
      <n-form label-placement="left" label-width="90" :model="form">
        <n-form-item :label="t('demo.problemTitle')">
          <n-input v-model:value="form.title" :placeholder="t('demo.problemTitlePlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('demo.difficulty')">
          <n-select v-model:value="form.level" :options="levelOptions" />
        </n-form-item>
        <n-form-item :label="t('demo.visibility')">
          <n-switch v-model:value="form.visible" />
        </n-form-item>
        <n-form-item :label="t('demo.tags')">
          <n-dynamic-tags v-model:value="form.tags" />
        </n-form-item>
        <n-form-item :label="t('demo.description')">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :placeholder="t('demo.descriptionPlaceholder')"
          />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary">{{ t('common.save') }}</n-button>
            <n-button>{{ t('common.reset') }}</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale, t } = useI18n()

const form = reactive({
  title: '',
  level: 'medium',
  visible: true,
  tags: [] as string[],
  description: '',
})

const levelOptions = computed(() => [
  { label: t('demo.easy'), value: 'easy' },
  { label: t('demo.medium'), value: 'medium' },
  { label: t('demo.hard'), value: 'hard' },
])

watch(
  locale,
  () => {
    form.title = t('demo.sampleProblem')
    form.tags = [t('demo.array'), t('demo.sort')]
  },
  { immediate: true },
)
</script>
