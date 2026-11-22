<script lang="ts" setup>
import loginBg from '@/assets/images/login/login-bg.png'
import loginGif from '@/assets/images/login/login-min.gif'

defineProps<{
  title: string
  subtitle: string
  backgroundUrl?: string
}>()
</script>

<template>
  <div
    class="login-container"
    :style="{ backgroundImage: `url(${backgroundUrl || loginBg})` }"
  >
    <div class="auth-shell">
      <aside class="box-item desc">
        <div class="welcome">
          <p>{{ title }}</p>
          <p class="sub-welcome">
            {{ subtitle }}
          </p>
        </div>
        <img
          class="welcome-img"
          :src="loginGif"
          alt=""
        >
      </aside>
      <section class="box-item login">
        <slot />
        <footer class="auth-footer">
          © 2024-2025 by Charlie Zhang
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.auth-shell {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 16px 73px 8px rgba(203, 203, 203, 0.2);
}

.box-item {
  width: 460px;
  min-height: 600px;
  background: #ffffff;
}

.box-item.desc {
  border-radius: 12px 0 0 12px;
  padding: 23px 25px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.box-item.login {
  border-radius: 0 12px 12px 0;
  padding: 34px 42px 28px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.welcome {
  padding-top: 35px;
  width: 100%;
  text-align: center;
}

.welcome > p {
  margin: 0;
  color: #333333;
  line-height: 1.4;
  letter-spacing: 0.26px;
  text-align: center;
  font-weight: 700;
  font-size: 26px;
}

.welcome .sub-welcome {
  margin: 12px auto 0;
  padding: 10px 14px;
  display: inline-block;
  width: auto;
  max-width: 100%;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  color: #ffffff;
  line-height: 1.4;
  letter-spacing: 0.26px;
  opacity: 0.96;
  background: #1748fd;
  border-radius: 22px 22px 2px 22px;
}

.welcome-img {
  width: 350px;
  max-width: 100%;
  height: auto;
  margin-top: 24px;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #a1aebe;
}

@media (max-width: 960px) {
  .auth-shell {
    flex-direction: column;
    width: min(460px, 100%);
  }

  .box-item {
    width: 100%;
    min-height: auto;
  }

  .box-item.desc {
    border-radius: 12px 12px 0 0;
    padding-bottom: 8px;
  }

  .box-item.login {
    border-radius: 0 0 12px 12px;
  }

  .welcome-img {
    width: 240px;
  }
}
</style>
