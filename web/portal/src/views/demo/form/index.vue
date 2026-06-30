<template>
  <n-space vertical size="large">
    <n-page-header :title="t('resource.demo.form')" :subtitle="t('resource.demo.form_subtitle')" />

    <n-card :bordered="false">
      <n-form label-placement="left" label-width="90" :model="form">
        <n-form-item :label="t('resource.demo.problem_title')">
          <n-input v-model:value="form.title" :placeholder="t('resource.demo.placeholder.problem_title')" />
        </n-form-item>
        <n-form-item :label="t('resource.demo.difficulty')">
          <n-select v-model:value="form.level" :options="levelOptions" />
        </n-form-item>
        <n-form-item :label="t('resource.demo.visibility')">
          <n-switch v-model:value="form.visible" />
        </n-form-item>
        <n-form-item :label="t('resource.demo.tags')">
          <n-dynamic-tags v-model:value="form.tags" />
        </n-form-item>
        <n-form-item :label="t('resource.demo.description')">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :placeholder="t('resource.demo.placeholder.description')"
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
  { label: t('resource.demo.easy'), value: 'easy' },
  { label: t('resource.demo.medium'), value: 'medium' },
  { label: t('resource.demo.hard'), value: 'hard' },
])

watch(
  locale,
  () => {
    form.title = t('resource.demo.sample_problem')
    form.tags = [t('resource.demo.array'), t('resource.demo.sort')]
  },
  { immediate: true },
)
</script>
