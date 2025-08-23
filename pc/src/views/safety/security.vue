<script setup>
// 注意：按照要求，这里不实现交互功能
// 所有数据都直接在模板中硬编码，方便后续修改
</script>

<template>
  <!-- 主内容区 -->
  <div class="container mx-auto px-4 py-8">
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <!-- 侧边导航 -->
      <div class="lg:col-span-1">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden sticky top-24">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              账户中心
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              管理您的个人信息和账户设置
            </p>
          </div>

          <nav class="p-2">
            <a href="#profile" class="flex items-center px-4 py-3 text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 rounded-lg mb-1">
              <i class="fa fa-user-circle w-5 text-center mr-3" />
              <span>个人资料</span>
            </a>
            <a href="#security" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-lock w-5 text-center mr-3" />
              <span>安全设置</span>
            </a>
            <a href="#notifications" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-bell w-5 text-center mr-3" />
              <span>通知设置</span>
            </a>
            <a href="#preferences" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-cog w-5 text-center mr-3" />
              <span>偏好设置</span>
            </a>
            <a href="#billing" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg mb-1 transition-colors">
              <i class="fa fa-credit-card w-5 text-center mr-3" />
              <span>付费服务</span>
            </a>
            <a href="#connected" class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg transition-colors">
              <i class="fa fa-link w-5 text-center mr-3" />
              <span>关联账号</span>
            </a>
          </nav>

          <div class="p-4 border-t border-gray-200 dark:border-gray-700">
            <!-- <button class="w-full flex items-center justify-center px-4 py-2 text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg transition-colors">
              <i class="fa fa-trash mr-2" />
              <span>注销账户</span>
            </button> -->
              <!-- <n-button type="error" block>
                  注销账号
                </n-button> -->
          </div>
        </div>
      </div>

      <!-- 主要设置内容 -->
      <div class="lg:col-span-3 space-y-8">
        <!-- 个人资料设置 -->
        <div id="profile" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              个人资料
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              更新您的个人信息和公开资料
            </p>
          </div>

          <form class="p-6 space-y-6">
            <!-- 头像设置 -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium">
                头像设置
              </h3>
              <div class="flex flex-wrap items-center gap-6">
                <div class="w-24 h-24 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
                  <img src="https://picsum.photos/seed/user123/200/200" alt="当前头像" class="w-full h-full object-cover">
                </div>
                <div class="flex flex-col gap-3">
<div>
                    <n-button type="primary">
                  上传新头像
                </n-button>
</div>
                  <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md">
                    推荐尺寸: 200x200像素，支持JPG、PNG格式，文件大小不超过2MB
                  </p>
                </div>
              </div>
            </div>

            <!-- 背景图设置 -->
            <div class="space-y-4 pt-4 border-t border-gray-200 dark:border-gray-700">
              <h3 class="text-lg font-medium">
                封面背景
              </h3>
              <div class="relative h-40 rounded-xl overflow-hidden border-2 border-gray-200 dark:border-gray-700">
                <img src="https://picsum.photos/seed/codepattern/1200/400" alt="当前封面背景" class="w-full h-full object-cover">
                
                <n-button  class="absolute right-3 bottom-3 px-3 py-1.5 backdrop-blur-sm text-white rounded-lg text-sm transition-colors">
              更换封面
            </n-button>
              </div>
              <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md">
                推荐尺寸: 1920x500像素，支持JPG、PNG格式，文件大小不超过5MB
              </p>
            </div>

            <!-- 基本信息 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t border-gray-200 dark:border-gray-700">
              <div>
                <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">用户名</label>
                <n-input />
                <!-- <input
                  id="username"
                  type="text"
                  value="code_warrior"
                  class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                >
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  用户名只能包含字母、数字和下划线，修改后需要重新登录
                </p> -->
              </div>

              <div>
                <label for="displayName" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">显示名称</label>
             <n-input />
                <!-- <input
                  id="displayName"
                  type="text"
                  value="代码战士"
                  class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                > -->
              </div>

              <div>
                <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">电子邮箱</label>
                 <n-input />
                <!-- <input
                  id="email"
                  type="email"
                  value="code@example.com"
                  class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                >
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  用于登录和接收通知
                </p> -->
              </div>

              <div>
                <label for="gender" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">性别</label>
                <n-select />
                <!-- <select
                  id="gender"
                  class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                >
                  <option value="male">
                    男
                  </option>
                  <option value="female">
                    女
                  </option>
                  <option value="other">
                    其他
                  </option>
                  <option value="hidden">
                    不公开
                  </option>
                </select> -->
              </div>

              <div>
                <label for="location" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">所在地</label>
              <n-input />
                <!-- <input
                  id="location"
                  type="text"
                  value="上海，中国"
                  class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                > -->
              </div>

              <div>
                <label for="website" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">个人网站</label>
             <n-input />
                <!-- <input
                  id="website"
                  type="url"
                  value="https://codewarrior.dev"
                  class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                > -->
              </div>
            </div>

            <!-- 个人简介 -->
            <div class="pt-4 border-t border-gray-200 dark:border-gray-700">
              <label for="bio" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">个人简介</label>
               <n-input type="textarea" />
              <!-- <textarea
                id="bio"
                rows="4"
                class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
              >热爱编程与算法，享受解决问题的乐趣。主要研究方向包括动态规划、图论和分布式系统。欢迎交流学习经验和技术心得！</textarea>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                最多可输入500个字符，支持Markdown格式
              </p> -->
            </div>

            <!-- 社交媒体链接 -->
            <div class="pt-4 border-t border-gray-200 dark:border-gray-700">
              <h3 class="text-lg font-medium mb-4">
                社交媒体
              </h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label for="github" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center">
                    <i class="fa fa-github mr-2" /> GitHub
                  </label>
                <n-input />
                  <!-- <input
                    id="github"
                    type="url"
                    value="https://github.com/codewarrior"
                    class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  > -->
                </div>

                <div>
                  <label for="twitter" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center">
                    <i class="fa fa-twitter mr-2" /> Twitter
                  </label>
                  <n-input />
                  <!-- <input
                    id="twitter"
                    type="url"
                    value="https://twitter.com/codewarrior"
                    class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  > -->
                </div>

                <div>
                  <label for="linkedin" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center">
                    <i class="fa fa-linkedin mr-2" /> LinkedIn
                  </label>
                   <n-input />
                  <!-- <input
                    id="linkedin"
                    type="url"
                    value="https://linkedin.com/in/codewarrior"
                    class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  > -->
                </div>

                <div>
                  <label for="blog" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center">
                    <i class="fa fa-globe mr-2" /> 个人博客
                  </label>
                  <n-input />
                  <!-- <input
                    id="blog"
                    type="url"
                    value=""
                    class="w-full px-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  > -->
                </div>
              </div>
            </div>

            <!-- 保存按钮 -->
            <div class="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
            <n-button>
              取消
            </n-button>
            <n-button type="primary">
              保存更改
            </n-button>
              <!-- <button type="button" class="px-6 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors">
                取消
              </button>
              <button type="submit" class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors">
                保存更改
              </button> -->
            </div>
          </form>
        </div>

        <!-- 安全设置 -->
        <div id="security" class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-xl font-bold">
              安全设置
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
              管理您的账户安全和登录选项
            </p>
          </div>

          <div class="p-6 space-y-6">
            <!-- 密码修改 -->
            <div class="p-5 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <div class="flex flex-wrap justify-between items-start gap-4">
                <div>
                  <h3 class="font-medium">
                    密码
                  </h3>
                  <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                    上次更新于 2023-01-15，建议定期更换密码以保证账户安全
                  </p>
                </div>
                <n-button>
                  修改密码
                </n-button>
              </div>
            </div>

            <!-- 两步验证 -->
            <div class="p-5 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <div class="flex flex-wrap justify-between items-start gap-4">
                <div>
                  <h3 class="font-medium">
                    两步验证
                  </h3>
                  <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                    开启后，登录时需要输入密码和验证码，提高账户安全性
                  </p>
                </div>
                <div class="relative inline-block w-12 h-6">
                  <n-switch />
                  <!-- <input id="twoFactor" type="checkbox" class="opacity-0 w-0 h-0">
                  <label for="twoFactor" class="absolute cursor-pointer top-0 left-0 right-0 bottom-0 bg-gray-300 dark:bg-gray-600 rounded-full transition-colors after:absolute after:content-[''] after:h-5 after:w-5 after:left-0.5 after:bottom-0.5 after:bg-white after:rounded-full after:transition-all" /> -->
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 基础样式补充 */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* 图片悬停效果 */
img {
  transition: transform 0.3s ease;
}

img:hover {
  transform: scale(1.03);
}

/* 导航栏滚动效果 */
header {
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

header.scrolled {
  background-color: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.dark header.scrolled {
  background-color: #1f2937;
}

/* 开关按钮样式 */
input:checked + label {
  background-color: #3b82f6;
}

input:checked + label:after {
  transform: translateX(6px);
}
</style>
