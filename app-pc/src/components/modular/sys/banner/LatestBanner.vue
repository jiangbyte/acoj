<!-- <script lang="ts" setup>
import { AesCrypto } from '@/utils'

defineProps<{
  bannerListData: any
}>()

const router = useRouter()
function openLink(item: any) {
  const isOpenNew = item.targetBlank
  if (item.jumpModule === 'HREF') {
    // HREF默认跳转外部链接
    window.open(item.jumpTarget, '_blank')
  }
  if (item.jumpModule === 'NOTICE') {
    router.push({
      name: 'notice_detail',
      query: { notice: AesCrypto.encrypt(item.jumpTarget) },
    })
  }
  if (item.jumpModule === 'PERSONAL') {
    router.push({
      name: 'user',
      query: { userId: AesCrypto.encrypt(item.jumpTarget) },
    })
  }
  if (item.jumpModule === 'PROBLEM') {
    router.push({
      name: 'problem_submit',
      query: { problemId: AesCrypto.encrypt(item.jumpTarget) },
    })
  }
  if (item.jumpModule === 'SET') {
    router.push({
      name: 'proset_detail',
      query: { set: AesCrypto.encrypt(item.jumpTarget) },
    })
  }
}
</script> -->
<script lang="ts" setup>
import { AesCrypto } from '@/utils'

defineProps<{
  bannerListData: any
}>()

const router = useRouter()

function openLink(item: any) {
  const isOpenNew = item.targetBlank

  // 统一处理路由跳转，支持新页面打开
  const handleRoute = (routeConfig: any, isExternal = false) => {
    if (isOpenNew && !isExternal) {
      // 在新页面打开内部路由
      const route = router.resolve(routeConfig)
      window.open(route.href, '_blank')
    }
    else if (isOpenNew && isExternal) {
      // 外部链接直接在新窗口打开
      window.open(routeConfig, '_blank')
    }
    else if (!isOpenNew && !isExternal) {
      // 在当前页面打开内部路由
      router.push(routeConfig)
    }
    else {
      // 外部链接在当前页面打开
      window.location.href = routeConfig
    }
  }

  switch (item.jumpModule) {
    case 'HREF':
      // HREF默认跳转外部链接
      handleRoute(item.jumpTarget, true)
      break
    case 'NOTICE':
      handleRoute({
        name: 'notice_detail',
        query: { notice: AesCrypto.encrypt(item.jumpTarget) },
      })
      break
    case 'PERSONAL':
      handleRoute({
        name: 'user',
        query: { userId: AesCrypto.encrypt(item.jumpTarget) },
      })
      break
    case 'PROBLEM':
      handleRoute({
        name: 'problem_submit',
        query: { problemId: AesCrypto.encrypt(item.jumpTarget) },
      })
      break
    case 'PAGE_PROBLEM':
      handleRoute({
        name: 'problems',
      })
      break
    case 'SET':
      handleRoute({
        name: 'proset_detail',
        query: { set: AesCrypto.encrypt(item.jumpTarget) },
      })
      break
    case 'PAGE_SET':
      handleRoute({
        name: 'sets',
      })
      break
    case 'NOTDO':
      break
    default:
      console.warn('Unknown jump module:', item.jumpModule)
  }
}
</script>

<template>
  <section class="rounded-xl overflow-hidden shadow-lg">
    <div class="my-section">
      <n-carousel
        autoplay
        show-arrow
        draggable
      >
        <div
          v-for="item in bannerListData"
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
            <div v-if="item.isVisibleSubtitle" class="caption-subtitle">
              {{ item.subtitle }}
            </div>
            <n-button
              v-if="item.isVisibleButton"
              type="primary"
              round
              @click="openLink(item)"
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
  </section>
</template>

<style scoped>
/* 基础样式补充，Unocss未覆盖的部分 */
/* 确保平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 链接样式统一 */
a {
  text-decoration: none;
}

/* 图片加载过渡 */
img {
  transition: opacity 0.3s ease-in-out;
}

img.loading {
  opacity: 0.5;
}

img.loaded {
  opacity: 1;
}

/* img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
} */

.my-section {
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
}

.carousel-item {
  position: relative;
  height: 320px;
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
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
