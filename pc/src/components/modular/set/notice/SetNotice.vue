<script lang="ts" setup>
import { useSysNoticeFetch } from '@/composables'

const listData = ref()
const loading = ref(true)

const { sysNoticeLatest10 } = useSysNoticeFetch()

// 模拟获取公告数据
async function loadData() {
  loading.value = true
  const { data } = await sysNoticeLatest10()
  if (data) {
    listData.value = data
    listData.value = listData.value.sort((a: any, b: any) => (a.sort || 0) - (b.sort || 0))
    loading.value = false
  }
}

loadData()
</script>

<template>
  <n-card hoverable class="content-card" size="small">
    <template #header>
      <n-space align="center" justify="start">
        <div>
          最新公告
        </div>
      </n-space>
    </template>

    <div v-if="loading" class="flex flex-col gap-2">
      <n-skeleton height="25px" width="33%" />
      <n-skeleton height="15px" text :repeat="2" />
    </div>

    <n-list v-else hoverable>
      <n-list-item
        v-for="item in listData"
        :key="item.id"
      >
        <n-flex align="center" :wrap="false">
          <n-flex align="center">
            <div class="flex flex-col">
              <h4>
                <n-ellipsis line-clamp="1">
                  {{ item.title }}
                </n-ellipsis>
              </h4>
              <n-ellipsis line-clamp="2" :tooltip="false">
                {{ item.content }}
              </n-ellipsis>
              <n-space align="center" justify="space-between" class="mt-2">
                <n-tag size="small" type="info" round>
                  最新
                </n-tag>
                <n-space align="center">
                  <n-icon class="mt-1.2">
                    <icon-park-outline-time />
                  </n-icon>
                  <n-time :time="item.createTime" />
                </n-space>
              </n-space>
            </div>
          </n-flex>
        </n-flex>
      </n-list-item>
    </n-list>
  </n-card>
</template>

<style scoped>
.notice-cover {
  border-radius: 6px;
  overflow: hidden;
}
</style>
