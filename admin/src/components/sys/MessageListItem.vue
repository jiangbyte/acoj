<!-- Author: Charlie -->

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    time?: string
    excerpt?: string
    icon: string
    isRead?: boolean
    kindLabel?: string
    kindType?: 'default' | 'error' | 'primary' | 'info' | 'success' | 'warning'
    severityLabel?: string
    avatarSize?: number
  }>(),
  {
    isRead: false,
    kindType: 'default',
    avatarSize: 32,
  },
)
</script>

<template>
  <NThing
    content-indented
    class="msg-list-item"
  >
    <template #avatar>
      <NBadge
        :dot="!isRead"
        :processing="!isRead"
        type="info"
      >
        <NAvatar
          round
          :size="avatarSize"
        >
          <NovaIcon
            :icon="icon"
            :size="avatarSize / 2"
            :style="{
              color: isRead ? 'var(--text-color-3)' : 'var(--primary-color)',
            }"
          />
        </NAvatar>
      </NBadge>
    </template>

    <template #header>
      <NEllipsis class="msg-list-item__title">
        <NText
          :depth="isRead ? 3 : 1"
          :strong="!isRead"
        >
          {{ title }}
        </NText>
      </NEllipsis>
    </template>

    <template
      v-if="time"
      #header-extra
    >
      <span class="msg-list-item__time">{{ time }}</span>
    </template>

    <template #description>
      <div class="msg-list-item__body">
        <NSpace
          v-if="kindLabel || severityLabel"
          :size="6"
          :wrap="false"
          class="msg-list-item__tags"
        >
          <NTag
            v-if="kindLabel"
            size="small"
            :bordered="false"
            :type="kindType"
          >
            {{ kindLabel }}
          </NTag>
          <NTag
            v-if="severityLabel"
            size="small"
            :bordered="false"
          >
            {{ severityLabel }}
          </NTag>
        </NSpace>
        <NEllipsis
          v-if="excerpt"
          :line-clamp="1"
          :tooltip="false"
          class="msg-list-item__excerpt"
        >
          <NText depth="3">
            {{ excerpt }}
          </NText>
        </NEllipsis>
      </div>
    </template>
  </NThing>
</template>

<style scoped>
.msg-list-item :deep(.n-thing-header) {
  margin-bottom: 2px;
}

.msg-list-item :deep(.n-thing-header__extra) {
  align-self: flex-start;
}

.msg-list-item__title {
  min-width: 0;
  padding-right: 8px;
}

.msg-list-item__time {
  flex-shrink: 0;
  font-size: 12px;
  line-height: 1.4;
  color: var(--text-color-3);
  white-space: nowrap;
}

.msg-list-item__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.msg-list-item__tags {
  min-width: 0;
}

.msg-list-item__excerpt {
  font-size: 12px;
  line-height: 1.5;
}
</style>
