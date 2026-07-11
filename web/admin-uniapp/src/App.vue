<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

onLaunch(() => {
  const authStore = useAuthStore()
  if (!authStore.isLogin) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
  }
})
</script>
<style lang="scss">
@import 'uview-pro/index.scss';

page {
  background: #f8f9fa;
  color: #111827;
  font-size: 14px;
}
</style>
