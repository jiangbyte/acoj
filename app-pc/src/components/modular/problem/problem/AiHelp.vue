<script lang="ts" setup>
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const testContent2 = ref(`### 测试标题 
哈希表方法的时间复杂度是 $O(n)$，原因如下：

- 我们只需要遍历一次数组，这个过程是 $O(n)$ 时间复杂度
- 在哈希表中进行插入和查找操作的平均时间复杂度是 $O(1)$
- 整个算法的时间复杂度由遍历数组的过程主导，因此是 $O(n)$

需要注意的是，这是平均时间复杂度。在最坏情况下，如果发生大量哈希冲突，时间复杂度可能接近 $O(n^2)$，但在实际应用中，哈希表的表现通常非常接近 $O(1)$ 的查找和插入时间。

\`\`\`cpp
int main() {
  return 0; 
} 
\`\`\` 

`)

const testContent = ref('为什么哈希表方法的时间复杂度是 O(n)？能详细解释一下吗？')
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <!-- 对话内容区域 -->
    <n-flex vertical class="h-104 overflow-scroll">
      <div v-for="value in 20" :key="value">
        <n-card size="small">
          <n-flex vertical :size="4">
            <div class="flex items-center justify-between">
              <n-tag type="primary" size="small">
                提问/解析/优化
              </n-tag>
              <n-tag size="small">
                2025-05-12
              </n-tag>
            </div>
            <n-text>
              {{ testContent }}
            </n-text>
          </n-flex>
        </n-card>
        <MdViewer :model-value="testContent2" />
      </div>
    </n-flex>

    <!-- 输入区域 -->
    <n-card size="small">
      <NFlex
        vertical
        :size="8"
      >
        <n-flex justify="space-between">
          <n-flex align="center">
            <n-button
              type="primary"
              size="small"
            >
              生成解题思路
            </n-button>
            <n-button
              size="small"
            >
              优化解析
            </n-button>
          </n-flex>
          <n-flex align="center">
            <n-text depth="3">
              解析风格
            </n-text>
            <n-select
              size="small" class="w-50" placeholder="请选择解析风格"
            />
          </n-flex>
        </n-flex>
        <n-flex>
          <n-button round size="tiny">
            解释时间复杂度
          </n-button>
          <n-button round size="tiny">
            有哪些边界情况需要考虑
          </n-button>
          <n-button round size="tiny">
            分析我的代码
          </n-button>
        </n-flex>
        <NInput
          type="textarea"
          placeholder="遇到问题？向AI描述你的疑问或需要帮助的地方。例如：如何优化这段代码的时间复杂度？或者：为什么我的代码在这个测试用例上失败了？"
          :autosize="{ minRows: 4, maxRows: 4 }"
        />
        <NFlex
          justify="end"
          align="center"
        >
          <NButton
            type="primary"
            size="small"
          >
            <template #icon>
              <icon-park-outline-send />
            </template>
            发送
          </NButton>
          <NButton
            secondary
            type="primary"
            size="small"
          >
            <template #icon>
              <icon-park-outline-refresh />
            </template>
            重试
          </NButton>
          <NButton
            secondary
            size="small"
            type="error"
          >
            <template #icon>
              <icon-park-outline-clear />
            </template>
            清空
          </NButton>
        </NFlex>
      </NFlex>
    </n-card>
  </NFlex>
</template>

<style scoped>

</style>
