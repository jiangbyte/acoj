<script lang="ts" setup>
import { useSysBannerFetch } from '@/composables'
// 列表数据
const listData = ref([] as any[])

// 获取列表数据
async function loadData() {
  try {
    const { sysBannerLatest10 } = useSysBannerFetch()
    const { data } = await sysBannerLatest10()

    if (data) {
      listData.value = data
    }
    else {
      listData.value = []
    }
  }
  catch {}
}

loadData()

function openLink(url: string) {
  window.open(url, '_blank')
}
</script>

<template>
  <div class="my-section">
    <n-carousel
      autoplay
      show-arrow
      draggable
    >
      <div
        v-for="item in listData"
        :key="item.id"
        class="carousel-item"
      >
        <img
          class="carousel-img"
          :src="item.banner"
        >
        <div class="carousel-caption">
          <div class="caption-title">
            {{ item.title }}
          </div>
          <div class="caption-subtitle">
            {{ item.subtitle }}
          </div>
          <n-button
            v-if="item.buttonText"
            type="primary"
            round
            @click="openLink(item.toUrl || '#')"
          >
            {{ item.buttonText || '了解更多' }}
            <template #icon>
              <icon-park-outline-hand-right />
            </template>
          </n-button>
        </div>
      </div>
    </n-carousel>
  </div>
</template>

<style scoped>
.my-section {
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
}

.carousel-item {
  position: relative;
  height: 280px;
}

@media (max-width: 768px) {
  .carousel-item {
    height: 250px;
  }

  .carousel-caption {
    padding: 40px 20px;
  }
}

@media (max-width: 480px) {
  .carousel-item {
    height: 250px;
  }

  .carousel-caption {
    padding: 30px 15px;
  }
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 10px 40px 40px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
  z-index: 1;
}

.caption-title {
  font-size: 1.5rem;
  margin: 0;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.caption-subtitle {
  font-size: 1.0rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 4px 0 8px;
  max-width: 600px;
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
