<template>
  <div class="app-container">
    <template v-if="!isAdminRoute">
      <header class="header">
        <el-menu
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          router
        >
          <div class="logo-container">
            <img src="./assets/crane-logo.svg" alt="和庆酒店" class="logo" />
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
            <el-menu-item index="/user/bookings">预订记录</el-menu-item>
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
            <img src="./assets/crane-logo.svg" alt="和庆酒店" class="footer-logo-img" />
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
                <a href="#" class="social-icon"><i class="el-icon-s-custom"></i></a>
                <a href="#" class="social-icon"><i class="el-icon-s-promotion"></i></a>
                <a href="#" class="social-icon"><i class="el-icon-s-opportunity"></i></a>
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

const route = useRoute()
const router = useRouter()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))

// 检查用户是否登录
const isLoggedIn = computed(() => {
  return localStorage.getItem('userToken') !== null
})

// 获取用户名
const userName = computed(() => {
  return localStorage.getItem('userName') || '用户'
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
    
    // 清除登录信息
    localStorage.removeItem('userToken')
    localStorage.removeItem('userName')
    
    ElMessage.success('已退出登录')
    
    // 如果当前在需要登录的页面，则跳转到首页
    if (route.meta.requiresAuth) {
      router.push('/')
    }
  } catch (error) {
    // 用户取消操作
  }
}
</script>

<style>
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
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.nav-menu {
  display: flex;
  align-items: center;
  height: 70px;
  padding: 0 50px;
  background-color: transparent !important;
  border-bottom: none !important;
}

.nav-menu .el-menu-item {
  font-size: 16px;
  letter-spacing: 1px;
  padding: 0 20px;
  font-weight: 500;
  color: var(--text-color) !important;
  height: 70px;
  line-height: 70px;
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

.logo-container {
  display: flex;
  align-items: center;
  padding-right: 40px;
}

.logo {
  height: 40px;
  margin-right: 10px;
}

.hotel-name {
  font-size: 22px;
  font-weight: bold;
  color: var(--secondary-color);
  font-family: "Times New Roman", "SimSun", serif;
  letter-spacing: 1px;
}

.flex-grow {
  flex-grow: 1;
}

.book-now-btn {
  margin-left: 20px;
  padding: 10px 20px;
  font-size: 14px;
  letter-spacing: 1px;
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  transition: all 0.3s ease;
}

.book-now-btn:hover {
  background-color: #b58d40;
  border-color: #b58d40;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
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
}

.social-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: #e0e0e0;
  transition: all 0.3s ease;
}

.social-icon:hover {
  background-color: var(--primary-color);
  color: #ffffff;
  transform: translateY(-3px);
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
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 8px;
  background-color: #409EFF;
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