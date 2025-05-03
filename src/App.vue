<template>
  <div class="app-container">
    <template v-if="!isAdminRoute">
      <header class="header">
        <el-menu
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          router
          :default-active="currentRoute"
        >
          <div class="logo-container">
            <img src="./assets/icons/crane-logo.svg" alt="鹤清酒店" class="logo" />
            <span class="hotel-name">鹤清酒店</span>
          </div>
          <div class="flex-grow" />
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/about">关于我们</el-menu-item>
          <el-menu-item index="/rooms">客房预订</el-menu-item>
          <el-menu-item index="/contact">联系我们</el-menu-item>
          
          <!-- 用户登录状态 -->
          <template v-if="isLoggedIn">
            <el-menu-item index="/user">个人中心</el-menu-item>
            <el-menu-item index="/user/membership">会员中心</el-menu-item>
            <el-menu-item index="" @click="handleLogout">退出登录</el-menu-item>
          </template>
          <template v-else>
            <el-menu-item index="" @click="goToLogin">登录</el-menu-item>
            <el-menu-item index="" @click="goToRegister">注册</el-menu-item>
          </template>
          
          <el-button type="primary" class="book-now-btn" @click="$router.push('/booking')">立即预订</el-button>
        </el-menu>
      </header>
      <main class="main-content">
        <router-view v-if="!$route.meta.requiresAuth || isLoggedIn"></router-view>
        <el-empty v-else description="请先登录后查看此页面" />
      </main>
      <footer class="app-footer">
        <div class="footer-content">
          <div class="footer-logo">
            <img src="./assets/icons/crane-logo.svg" alt="鹤清酒店" class="footer-logo-img" />
            <div class="footer-brand">
              <span class="footer-hotel-name">鹤清酒店</span>
              <p class="footer-slogan">仙居雅境 · 尊享至臻</p>
            </div>
          </div>
          <div class="footer-links">
            <div class="footer-section">
              <h4>联系我们</h4>
              <p><i class="el-icon-location"></i> 江西省南昌市八一公园旁</p>
              <p><i class="el-icon-phone"></i> 0791-88888888</p>
              <p><i class="el-icon-message"></i> info@heqinghotel.com</p>
            </div>
            <div class="footer-section">
              <h4>快速导航</h4>
              <ul>
                <li><router-link to="/">首页</router-link></li>
                <li><router-link to="/about">关于我们</router-link></li>
                <li><router-link to="/rooms">客房预订</router-link></li>
                <li><router-link to="/contact">联系我们</router-link></li>
              </ul>
            </div>
            <div class="footer-section">
              <h4>关注我们</h4>
              <div class="social-icons">
                <a href="#" class="social-icon wechat">
                  <img src="./assets/wechat.svg" alt="微信" />
                </a>
                <a href="#" class="social-icon weibo">
                  <img src="./assets/weibo.svg" alt="微博" />
                </a>
                <a href="#" class="social-icon douyin">
                  <img src="./assets/douyin.svg" alt="抖音" />
                </a>
              </div>
            </div>
          </div>
        </div>
        <div class="copyright">
          <p>© 2024 鹤清酒店 版权所有</p>
        </div>
      </footer>
    </template>
    <template v-else>
      <router-view></router-view>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))

// 获取当前路由路径作为激活菜单项
const currentRoute = computed(() => route.path)

// 检查用户是否登录
const isLoggedIn = computed(() => authStore.isLoggedIn)

// 获取用户名
const userName = computed(() => {
  return authStore.username || '个人中心'
})

// 跳转到用户中心
const goToUserProfile = () => {
  router.push('/user')
}

// 跳转到会员中心
const goToMembership = () => {
  router.push('/user/membership')
}

// 跳转到预订记录
const goToBookings = () => {
  router.push('/user/bookings')
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.logout()
    
    ElMessage.success('已退出登录')
    
    // 如果当前在需要登录的页面，则跳转到首页
    if (route.meta.requiresAuth) {
      router.push('/')
    }
  } catch (error) {
    if (error !== 'cancel') {
       ElMessage.info('取消退出');
    }
  }
}
</script>

<style>
/* Global Element Plus Theme Override */
:root {
  --el-color-primary: #C0A062;
  --el-color-primary-light-3: #D3B88A; /* For hover effects, adjust lightness if needed */
  /* You might need to define other light shades if components use them */
  /* --el-color-primary-light-5: #E6D0B0; */
  /* --el-color-primary-light-7: #F3E6D5; */
  /* --el-color-primary-light-8: #F8F0E5; */
  /* --el-color-primary-light-9: #FCF8F2; */
  --el-color-primary-dark-2: #A78B54;  /* For active/focus effects, adjust darkness if needed */

  /* Ensure Button colors inherit from the primary color */
  --el-button-primary-bg-color: var(--el-color-primary);
  --el-button-primary-border-color: var(--el-color-primary);
  --el-button-primary-hover-bg-color: var(--el-color-primary-light-3);
  --el-button-primary-hover-border-color: var(--el-color-primary-light-3);
  --el-button-primary-active-bg-color: var(--el-color-primary-dark-2);
  --el-button-primary-active-border-color: var(--el-color-primary-dark-2);
  
  /* You might need to override other component variables if they don't inherit properly */
}

/* Add any other global non-scoped styles here */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '\\5FAE\8F6F\96C5\9ED1', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f8f5f0; /* Example global background */
}

/* Example: Global link styling */
a {
  color: var(--el-color-primary); /* Use the new primary color for links */
  text-decoration: none;
}
a:hover {
  color: var(--el-color-primary-light-3);
  text-decoration: underline;
}

:root {
  --primary-color: #c59d5f;
  --secondary-color: #1a1a1a;
  --light-color: #f8f8f8;
  --border-color: #e0e0e0;
  --text-color: #333333;
  --text-light: #666666;
}

body {
  margin: 0;
  padding: 0;
  font-family: "Microsoft YaHei", "SimSun", serif;
  color: var(--text-color);
  background-color: #ffffff;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(to right, rgba(255, 252, 245, 0.95), rgba(248, 240, 222, 0.95), rgba(255, 252, 245, 0.95));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 3px 15px rgba(197, 157, 95, 0.15);
  border-bottom: 1px solid rgba(197, 157, 95, 0.2);
}

.header::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(to right, #c59d5f, #ddbf85, #c59d5f);
}

.nav-menu {
  display: flex;
  align-items: center;
  height: 76px;
  padding: 0 50px;
  background-color: transparent !important;
  border-bottom: none !important;
}

.nav-menu .el-menu-item {
  font-size: 16px;
  letter-spacing: 1.5px;
  padding: 0 22px;
  font-weight: 500;
  color: #3a3a3a !important;
  height: 76px;
  line-height: 76px;
  position: relative;
  transition: all 0.3s ease;
}

.nav-menu .el-menu-item.is-active {
  color: var(--primary-color) !important;
  font-weight: bold;
  border-bottom: 2px solid var(--primary-color) !important;
}

.nav-menu .el-menu-item:hover {
  color: var(--primary-color) !important;
  background-color: transparent !important;
}

.nav-menu .el-menu-item::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--primary-color);
  transition: all 0.3s ease;
  transform: translateX(-50%);
  opacity: 0;
}

.nav-menu .el-menu-item:hover::after {
  width: 70%;
  opacity: 0.7;
}

.nav-menu .el-menu-item.is-active::after {
  width: 100%;
  opacity: 1;
}

.logo-container {
  display: flex;
  align-items: center;
  padding-right: 40px;
  position: relative;
}

.logo-container::after {
  content: "";
  position: absolute;
  right: 15px;
  top: 50%;
  height: 30px;
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(197, 157, 95, 0.3), transparent);
  transform: translateY(-50%);
}

.logo {
  height: 44px;
  margin-right: 12px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: all 0.3s ease;
}

.logo:hover {
  filter: drop-shadow(0 4px 8px rgba(197, 157, 95, 0.3));
  transform: translateY(-2px);
}

.hotel-name {
  font-size: 24px;
  font-weight: bold;
  color: #1a1a1a;
  font-family: "SimSun", serif;
  letter-spacing: 2px;
  position: relative;
  background: linear-gradient(to right, #a17d38, #c59d5f, #ddbf85, #c59d5f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.flex-grow {
  flex-grow: 1;
}

.book-now-btn {
  margin-left: 20px;
  padding: 12px 25px;
  font-size: 15px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #c59d5f, #ddbf85);
  border-color: #c59d5f;
  transition: all 0.4s ease;
  box-shadow: 0 3px 8px rgba(197, 157, 95, 0.2);
  font-weight: 500;
  border-radius: 4px;
}

.book-now-btn:hover {
  background: linear-gradient(135deg, #b58d40, #c59d5f);
  border-color: #b58d40;
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(197, 157, 95, 0.3);
}

.main-content {
  flex: 1;
  padding-top: 70px;
}

.app-footer {
  background-color: #1a1a1a;
  color: #e0e0e0;
  padding: 60px 0 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.footer-logo {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.footer-logo-img {
  height: 60px;
  filter: invert(1);
  margin-right: 15px;
}

.footer-brand {
  display: flex;
  flex-direction: column;
}

.footer-hotel-name {
  font-size: 24px;
  font-weight: bold;
  color: #ffffff;
  font-family: "Times New Roman", "SimSun", serif;
  letter-spacing: 1px;
}

.footer-slogan {
  margin: 5px 0 0;
  font-size: 14px;
  color: var(--primary-color);
  letter-spacing: 1px;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  width: 100%;
  margin-top: 20px;
}

.footer-section {
  margin-bottom: 30px;
  min-width: 200px;
  flex: 1;
  padding-right: 30px;
}

.footer-section h4 {
  color: #ffffff;
  margin-bottom: 15px;
  font-size: 18px;
  position: relative;
  padding-bottom: 10px;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background-color: var(--primary-color);
}

.footer-section p {
  margin: 10px 0;
  display: flex;
  align-items: center;
}

.footer-section i {
  margin-right: 10px;
  color: var(--primary-color);
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section ul li {
  margin-bottom: 10px;
}

.footer-section ul li a {
  color: #e0e0e0;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-section ul li a:hover {
  color: var(--primary-color);
}

.social-icons {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.social-icon {
  display: block;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  overflow: hidden;
}

.social-icon img {
  width: 24px;
  height: 24px;
  transition: all 0.3s ease;
}

.social-icon:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.social-icon:hover img {
  transform: scale(1.2);
}

.copyright {
  text-align: center;
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 30px;
  font-size: 14px;
  color: #999;
}

@media (max-width: 768px) {
  .nav-menu {
    padding: 0 20px;
  }
  
  .footer-content {
    flex-direction: column;
  }
  
  .footer-links {
    flex-direction: column;
  }
}

.user-dropdown {
  margin-left: 15px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5px 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  margin: 0 auto 8px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #c59d5f 0%, #e2c9a6 100%);
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 10px rgba(197, 157, 95, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.user-avatar::before {
  content: '鹤';
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  font-family: "SimSun", serif;
  font-weight: 300;
}

.user-avatar:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(197, 157, 95, 0.3);
  border-color: rgba(255, 255, 255, 0.9);
}
.user-name {
  font-size: 14px;
  color: #303133;
  margin-right: 4px;
}

.dropdown-link {
  text-decoration: none;
  color: #606266;
  display: block;
}

.dropdown-link:hover {
  color: #409EFF;
}

.login-btn, .register-btn {
  font-size: 14px;
  padding: 0 10px;
  margin-left: 5px;
  color: var(--text-color);
}

.login-btn:hover, .register-btn:hover {
  color: var(--primary-color);
}
</style>