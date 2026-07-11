<template>
  <view @click="handleWechatLogin">
    <u-icon name="weixin-fill" size="72" color="#10b981"></u-icon>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  success: [userInfo: any]
  fail: [error: string]
}>()

const loading = ref(false)

// 获取当前平台信息
const platform = uni.getSystemInfoSync().platform
const appName = uni.getSystemInfoSync().uniPlatform

// 微信登录主函数
const handleWechatLogin = async () => {
  if (loading.value) return

  loading.value = true

  try {
    // 判断平台
    if (appName === 'mp-weixin') {
      // 微信小程序登录
      await wechatMiniProgramLogin()
    } else if (appName === 'app') {
      // App 登录
      await appWechatLogin()
    } else if (appName === 'web' || appName === 'h5') {
      // H5/Web 登录
      await h5WechatLogin()
    } else {
      uni.showToast({
        title: '当前平台不支持微信登录',
        icon: 'none',
      })
      emit('fail', '当前平台不支持微信登录')
    }
  } catch (error: any) {
    console.error('微信登录失败：', error)
    emit('fail', error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 微信小程序微信登录（一键授权）
 */
const wechatMiniProgramLogin = async () => {
  return new Promise((resolve, reject) => {
    // 检查是否支持 uni.getUserProfile
    if (typeof uni.getUserProfile === 'function') {
      // 方案一：使用 uni.getUserProfile（推荐，基础库 2.10.4+）
      uni.getUserProfile({
        desc: '用于完善用户资料', // 声明获取用户个人信息的目的
        success: async (profileRes) => {
          console.log('用户授权成功：', profileRes.userInfo)

          // 2. 获取登录 code
          uni.login({
            provider: 'weixin',
            success: async (loginRes) => {
              if (loginRes.code) {
                try {
                  // 3. 将 code 和用户信息发送到后端服务器
                  const response = await requestLogin({
                    code: loginRes.code,
                    encryptedData: profileRes.encryptedData,
                    iv: profileRes.iv,
                    rawData: profileRes.rawData,
                    signature: profileRes.signature,
                    platform: 'wechat_mini',
                  })

                  uni.showToast({
                    title: '登录成功',
                    icon: 'success',
                  })

                  saveUserInfo(response.userInfo)
                  emit('success', response.userInfo)
                  resolve(response.userInfo)
                } catch (error: any) {
                  uni.showToast({
                    title: error.message || '登录失败',
                    icon: 'none',
                  })
                  reject(error)
                }
              } else {
                uni.showToast({
                  title: '登录失败',
                  icon: 'none',
                })
                reject(new Error('获取 code 失败'))
              }
            },
            fail: (err) => {
              uni.showToast({
                title: '登录取消',
                icon: 'none',
              })
              reject(new Error('用户取消登录'))
            },
          })
        },
        fail: (err) => {
          // 用户拒绝授权
          console.log('用户拒绝授权', err)
          uni.showToast({
            title: '需要授权才能登录',
            icon: 'none',
          })
          reject(new Error('用户拒绝授权'))
        },
      })
    } else {
      // 方案二：降级处理，使用 uni.getUserInfo（旧版本）
      console.log('uni.getUserProfile 不支持，使用 uni.getUserInfo')

      uni.getUserInfo({
        success: async (userInfoRes) => {
          console.log('用户授权成功（旧版）：', userInfoRes.userInfo)

          uni.login({
            provider: 'weixin',
            success: async (loginRes) => {
              if (loginRes.code) {
                try {
                  const response = await requestLogin({
                    code: loginRes.code,
                    encryptedData: userInfoRes.encryptedData,
                    iv: userInfoRes.iv,
                    rawData: userInfoRes.rawData,
                    signature: userInfoRes.signature,
                    platform: 'wechat_mini',
                  })

                  uni.showToast({
                    title: '登录成功',
                    icon: 'success',
                  })

                  saveUserInfo(response.userInfo)
                  emit('success', response.userInfo)
                  resolve(response.userInfo)
                } catch (error: any) {
                  uni.showToast({
                    title: error.message || '登录失败',
                    icon: 'none',
                  })
                  reject(error)
                }
              } else {
                reject(new Error('获取 code 失败'))
              }
            },
            fail: () => {
              reject(new Error('用户取消登录'))
            },
          })
        },
        fail: (err) => {
          console.log('用户拒绝授权（旧版）', err)
          reject(new Error('用户拒绝授权'))
        },
      })
    }
  })
}

/**
 * App 微信登录
 */
const appWechatLogin = async () => {
  return new Promise((resolve, reject) => {
    // 1. 调用微信授权登录
    uni.login({
      provider: 'weixin',
      success: async (loginRes) => {
        try {
          // 2. 获取用户信息
          const userInfo = loginRes.authResult.userInfo

          // 3. 发送到后端验证
          const response = await requestLogin({
            openid: loginRes.authResult.openid,
            access_token: loginRes.authResult.access_token,
            platform: 'wechat_app',
          })

          uni.showToast({
            title: '登录成功',
            icon: 'success',
          })

          saveUserInfo(response.userInfo)
          emit('success', response.userInfo)
          resolve(response.userInfo)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '登录失败',
            icon: 'none',
          })
          reject(error)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '登录取消',
          icon: 'none',
        })
        reject(new Error('用户取消登录'))
      },
    })
  })
}

/**
 * H5 微信登录
 */
const h5WechatLogin = async () => {
  return new Promise((resolve, reject) => {
    // 1. 构建微信 OAuth2.0 授权链接
    // 需要配置微信开放平台的回调地址
    const appId = 'YOUR_WECHAT_APP_ID' // 替换成你的微信开放平台 AppID
    const redirectUri = encodeURIComponent(window.location.href)
    const scope = 'snsapi_userinfo' // 或 snsapi_login（网站应用）

    const wechatAuthUrl = `https://open.weixin.qq.com/connect/qrconnect?appid=${appId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}&state=STATE#wechat_redirect`

    // 2. 跳转到微信授权页面
    window.location.href = wechatAuthUrl

    // 3. 授权后会回调回当前页面，带上 code 参数
    // 需要在页面加载时检查是否有 code 参数
    checkWechatCode()
  })
}

/**
 * 检查 URL 中是否有微信回调的 code 参数（H5 专用）
 */
const checkWechatCode = () => {
  const urlParams = new URLSearchParams(window.location.search)
  const code = urlParams.get('code')
  const state = urlParams.get('state')

  if (code) {
    // 清除 URL 中的 code 参数
    window.history.replaceState({}, '', window.location.pathname)

    // 调用后端 API 换取用户信息
    requestLogin({
      code,
      platform: 'wechat_h5',
    })
      .then((response) => {
        uni.showToast({
          title: '登录成功',
          icon: 'success',
        })

        saveUserInfo(response.userInfo)
        emit('success', response.userInfo)
      })
      .catch((error) => {
        uni.showToast({
          title: error.message || '登录失败',
          icon: 'none',
        })
        emit('fail', error.message)
      })
  }
}

/**
 * 请求后端登录 API
 */
const requestLogin = async (data: any) => {
  // TODO: 替换成你的后端 API
  // 示例：
  // const response = await uni.request({
  //   url: 'https://your-api.com/api/login/wechat',
  //   method: 'POST',
  //   data,
  // })
  // return response.data

  // 模拟返回
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        userInfo: {
          nickname: '微信用户',
          avatar: 'https://wx.qlogo.cn/mmopen/vi_32/0',
          openid: data.openid || data.code,
        },
      })
    }, 1000)
  })
}

/**
 * 保存用户信息
 */
const saveUserInfo = (userInfo: any) => {
  uni.setStorageSync('userInfo', userInfo)
  uni.setStorageSync('token', 'mock_token_' + Date.now())
}

defineExpose({
  handleWechatLogin,
})
</script>

<style lang="scss" scoped></style>
