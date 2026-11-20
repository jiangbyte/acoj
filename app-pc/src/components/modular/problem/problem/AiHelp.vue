<script lang="ts" setup>
import MdViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const testContent2 = ref(`### Test Title
The hash table approach has $O(n)$ time complexity because:

- We only traverse the array once, which takes $O(n)$ time
- Hash table insertions and lookups take $O(1)$ on average
- The array traversal dominates the algorithm, so the overall complexity is $O(n)$

This is the average complexity. In the worst case, many hash collisions can approach $O(n^2)$, but hash table lookups and insertions are typically close to $O(1)$ in practice.

\`\`\`cpp
int main() {
  return 0; 
} 
\`\`\` 

`)

const testContent = ref('Why is the hash table approach O(n)? Please explain in detail.')
</script>

<template>
  <NFlex
    vertical
    :size="0"
  >
    <!-- Section -->
    <n-flex vertical class="h-104 overflow-scroll">
      <div v-for="value in 20" :key="value">
        <n-card size="small">
          <n-flex vertical :size="4">
            <div class="flex items-center justify-between">
              <n-tag type="primary" size="small">
                Ask / Analyze / Optimize
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

    <!-- Section -->
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
              Generate Solution
            </n-button>
            <n-button
              size="small"
            >
              Improve Analysis
            </n-button>
          </n-flex>
          <n-flex align="center">
            <n-text depth="3">
              Analysis Style
            </n-text>
            <n-select
              size="small" class="w-50" placeholder="Select an analysis style"
            />
          </n-flex>
        </n-flex>
        <n-flex>
          <n-button round size="tiny">
            Explain Complexity
          </n-button>
          <n-button round size="tiny">
            What edge cases should I consider?
          </n-button>
          <n-button round size="tiny">
            Analyze My Code
          </n-button>
        </n-flex>
        <NInput
          type="textarea"
          placeholder="Need help? Ask AI a question, such as how to improve this code or why it fails a test case."
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
            Send
          </NButton>
          <NButton
            secondary
            type="primary"
            size="small"
          >
            <template #icon>
              <icon-park-outline-refresh />
            </template>
            Retry
          </NButton>
          <NButton
            secondary
            size="small"
            type="error"
          >
            <template #icon>
              <icon-park-outline-clear />
            </template>
            Clear
          </NButton>
        </NFlex>
      </NFlex>
    </n-card>
  </NFlex>
</template>

<style scoped>

</style>
