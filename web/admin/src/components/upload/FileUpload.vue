<script setup lang="ts">
import { Icon } from '@iconify/vue/offline'
import { fileApi } from '@/api'
import { resolveFileUrl } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = withDefaults(
  defineProps<{
    value?: string | null
    accept?: string
    buttonText?: string
    icon?: string
    mode?: 'button' | 'icon'
    preview?: 'image' | 'video' | 'file'
    compact?: boolean
  }>(),
  {
    value: '',
    accept: '',
    buttonText: '',
    icon: '',
    mode: 'button',
    preview: 'file',
    compact: false,
  },
)

const emit = defineEmits<{
  'update:value': [value: string]
  uploaded: [file: any]
}>()

const { t } = useI18n()
const inputRef = ref<HTMLInputElement | null>(null)
const state = reactive({
  loading: false,
  fileName: '',
})

const currentUrl = computed(() => resolveFileUrl(props.value))
const currentName = computed(() => state.fileName || props.value || t('common.upload.empty'))
const uploadText = computed(() => props.buttonText || t('common.upload.select'))
const actionIcon = computed(() => props.icon || 'icon-park-outline:upload')

function triggerUpload() {
  inputRef.value?.click()
}

function clearValue() {
  state.fileName = ''
  emit('update:value', '')
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  state.loading = true
  try {
    const response = await fileApi.upload(file)
    const uploaded = response.data ?? {}
    state.fileName = uploaded.original_name || file.name
    emit('update:value', uploaded.url || uploaded.object_name || '')
    emit('uploaded', uploaded)
    window.$message.success(t('common.upload.success'))
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <div class="file-upload" :class="{ 'file-upload--compact': compact }">
    <input ref="inputRef" class="file-upload__input" type="file" :accept="accept" @change="handleFileChange" />

    <div v-if="!compact && preview === 'image' && currentUrl" class="file-upload__image">
      <NImage :src="currentUrl" object-fit="cover" :alt="currentName" width="160" height="90" />
    </div>
    <video
      v-else-if="!compact && preview === 'video' && currentUrl"
      class="file-upload__video"
      controls
      :src="currentUrl"
    />
    <NEllipsis v-else-if="!compact" class="file-upload__name">
      {{ currentName }}
    </NEllipsis>

    <div class="file-upload__actions" :class="{ 'file-upload__actions--compact': compact }">
      <NButton
        v-if="mode === 'icon'"
        text
        :loading="state.loading"
        :title="uploadText"
        :aria-label="uploadText"
        @click="triggerUpload"
      >
        <template #icon>
          <NIcon>
            <Icon :icon="actionIcon" />
          </NIcon>
        </template>
      </NButton>
      <NButton v-else size="small" :loading="state.loading" @click="triggerUpload">
        <template v-if="icon" #icon>
          <NIcon>
            <Icon :icon="icon" />
          </NIcon>
        </template>
        {{ uploadText }}
      </NButton>
      <NButton v-if="value" size="small" text type="error" @click="clearValue">
        {{ t('common.upload.clear') }}
      </NButton>
    </div>
  </div>
</template>

<style scoped>
.file-upload {
  display: grid;
  gap: 8px;
  width: 100%;
  min-width: 0;
}

.file-upload--compact {
  display: inline-flex;
  align-items: center;
  width: auto;
}

.file-upload__input {
  display: none;
}

.file-upload__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.file-upload__actions--compact {
  gap: 0;
}

.file-upload__image,
.file-upload__video {
  width: 160px;
  max-width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
  background: var(--body-color);
}

.file-upload__video {
  aspect-ratio: 16 / 9;
}

.file-upload__name {
  max-width: 100%;
  color: var(--text-color-3);
}
</style>
