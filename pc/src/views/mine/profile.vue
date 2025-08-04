<script lang="ts" setup>
import { useAuthFetch } from '@/composables'
import { NAvatar, NSpace, NTag } from 'naive-ui'

const { getProfile } = useAuthFetch()

// 假设的题目列表数据
const solvedProblems = [
  { id: '1', title: '两数之和', difficulty: '简单', tags: ['数组', '哈希表'] },
  { id: '2', title: '反转链表', difficulty: '中等', tags: ['链表'] },
  { id: '3', title: '二叉树的中序遍历', difficulty: '中等', tags: ['树', '深度优先搜索'] },
]

// 假设的标签列表数据
const tags = [
  { id: '1', name: '数组', count: 23 },
  { id: '2', name: '哈希表', count: 15 },
  { id: '3', name: '链表', count: 18 },
  { id: '4', name: '树', count: 12 },
  { id: '5', name: '动态规划', count: 8 },
]

// 假设的题集列表数据
const problemSets = [
  { id: '1', title: 'LeetCode 热题 HOT 100', count: 100 },
  { id: '2', title: '剑指 Offer', count: 75 },
  { id: '3', title: '算法入门', count: 50 },
]

const profile = ref()
async function loadData() {
  const { data } = await getProfile()
  if (data) {
    profile.value = data
    console.log(profile.value)
  }
}
loadData()
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <!-- 背景图片和用户信息部分 -->
    <div class="user-header" :style="{ backgroundImage: `url(${profile?.background})` }">
      <div class="user-info-overlay">
        <div class="user-avatar">
          <NAvatar
            round
            :size="100"
            :src="profile?.avatar"
            object-fit="cover"
          />
        </div>
        <div class="user-details">
          <h1 class="user-name">
            {{ profile?.nickname || profile?.username }}
          </h1>
          <div class="user-meta">
            <NSpace>
              <NTag :bordered="false" type="info">
                {{ profile?.genderName }}
              </NTag>
              <NTag :bordered="false" type="success">
                {{ profile?.groupName }}
              </NTag>
            </NSpace>
          </div>
          <p class="user-quote">
            {{ profile?.quote }}
          </p>
        </div>
      </div>
    </div>

    <!-- 用户内容部分 -->
    <NSpace vertical>
      12123
    </NSpace>
  </div>
</template>

<style scoped>
.user-header {
  position: relative;
  height: 300px;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
}

.user-info-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  display: flex;
  align-items: flex-end;
  gap: 24px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-details {
  color: white;
}

.user-name {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.user-meta {
  margin-bottom: 12px;
}

.user-quote {
  font-size: 16px;
  margin: 0;
  font-style: italic;
}

.user-content {
  padding: 0 16px;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
